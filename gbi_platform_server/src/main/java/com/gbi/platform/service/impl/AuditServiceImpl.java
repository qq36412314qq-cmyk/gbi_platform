package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.AuditLogQueryDTO;
import com.gbi.platform.entity.SysAuditLog;
import com.gbi.platform.mapper.SysAuditLogMapper;
import com.gbi.platform.service.AuditService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.AuditLogVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审计日志服务实现：只读查询 + CSV 导出（日志永久归档）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysAuditLogMapper auditLogMapper;

    private final AuditLogUtil auditLogUtil;

    /** 上传根目录（application.yml gbi.upload-dir） */
    @Value("${gbi.upload-dir:./upload}")
    private String uploadDir;

    @Override
    public PageVO<AuditLogVO> page(AuditLogQueryDTO dto) {
        Page<SysAuditLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<SysAuditLog> wrapper = buildWrapper(dto, true);
        Page<SysAuditLog> result = auditLogMapper.selectPage(page, wrapper);
        List<AuditLogVO> voList = result.getRecords().stream().map(a -> {
            AuditLogVO vo = new AuditLogVO();
            vo.setId(a.getId());
            vo.setCompanyId(a.getCompanyId());
            vo.setOperUserId(a.getOperUserId());
            vo.setOperUserName(a.getOperUserName());
            vo.setOperIp(a.getOperIp());
            vo.setOperModule(a.getOperModule());
            vo.setOperType(a.getOperType());
            vo.setBizId(a.getBizId());
            vo.setBeforeJson(a.getBeforeJson());
            vo.setAfterJson(a.getAfterJson());
            vo.setCreateTime(a.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public String export(AuditLogQueryDTO dto) {
        // 最多导出 5 万条，防止大表导出拖垮服务
        LambdaQueryWrapper<SysAuditLog> wrapper = buildWrapper(dto, true);
        wrapper.last("LIMIT 50000");
        List<SysAuditLog> list = auditLogMapper.selectList(wrapper);
        // 生成 CSV（UTF-8 BOM，Excel 可直接打开）
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("ID,操作人,所属公司ID,操作模块,操作类型,关联单据,操作IP,操作时间,操作前快照,操作后快照\n");
        for (SysAuditLog a : list) {
            sb.append(a.getId()).append(',')
                    .append(escapeCsv(a.getOperUserName())).append(',')
                    .append(a.getCompanyId()).append(',')
                    .append(escapeCsv(a.getOperModule())).append(',')
                    .append(escapeCsv(a.getOperType())).append(',')
                    .append(escapeCsv(a.getBizId())).append(',')
                    .append(escapeCsv(a.getOperIp())).append(',')
                    .append(a.getCreateTime() == null ? "" : TIME_FMT.format(a.getCreateTime())).append(',')
                    .append(escapeCsv(a.getBeforeJson())).append(',')
                    .append(escapeCsv(a.getAfterJson())).append('\n');
        }
        String fileName = "audit_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        try {
            Path exportDir = Paths.get(uploadDir, "export");
            Files.createDirectories(exportDir);
            Files.writeString(exportDir.resolve(fileName), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("审计日志导出失败", e);
            throw new BizException("导出失败，请稍后重试");
        }
        // 导出行为本身记审计
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_EXPORT, fileName, null, null);
        return "/upload/export/" + fileName;
    }

    /**
     * 组装查询条件：模块/类型/操作人模糊/时间范围；非超管强制过滤本公司
     */
    private LambdaQueryWrapper<SysAuditLog> buildWrapper(AuditLogQueryDTO dto, boolean orderByIdDesc) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
                .eq(StringUtils.hasText(dto.getOperModule()), SysAuditLog::getOperModule, dto.getOperModule())
                .eq(StringUtils.hasText(dto.getOperType()), SysAuditLog::getOperType, dto.getOperType())
                .like(StringUtils.hasText(dto.getOperUserName()), SysAuditLog::getOperUserName, dto.getOperUserName());
        if (StringUtils.hasText(dto.getStartTime())) {
            wrapper.ge(SysAuditLog::getCreateTime, LocalDateTime.parse(dto.getStartTime(), TIME_FMT));
        }
        if (StringUtils.hasText(dto.getEndTime())) {
            wrapper.le(SysAuditLog::getCreateTime, LocalDateTime.parse(dto.getEndTime(), TIME_FMT));
        }
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null && !loginUser.isSuperAdmin()) {
            wrapper.eq(SysAuditLog::getCompanyId, loginUser.getCompanyId());
        }
        if (orderByIdDesc) {
            wrapper.orderByDesc(SysAuditLog::getId);
        }
        return wrapper;
    }

    /**
     * CSV 字段转义（含逗号/引号/换行时包裹双引号）
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}