package com.gbi.platform.service.impl.wecom;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.dto.wecom.WecomSyncQueryDTO;
import com.gbi.platform.entity.wecom.WecomSyncRecord;
import com.gbi.platform.mapper.wecom.WecomSyncRecordMapper;
import com.gbi.platform.service.wecom.WecomSyncService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomSyncVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企微同步记录服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WecomSyncServiceImpl implements WecomSyncService {

    private final WecomSyncRecordMapper syncRecordMapper;

    @Override
    public PageVO<WecomSyncVO> pageSyncs(WecomSyncQueryDTO dto) {
        Page<WecomSyncRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<WecomSyncRecord> wrapper = new LambdaQueryWrapper<WecomSyncRecord>()
                .eq(StringUtils.hasText(dto.getSyncType()), WecomSyncRecord::getSyncType, dto.getSyncType())
                .eq(dto.getSyncDirection() != null, WecomSyncRecord::getSyncDirection, dto.getSyncDirection())
                .eq(dto.getSyncStatus() != null, WecomSyncRecord::getSyncStatus, dto.getSyncStatus())
                .ge(dto.getStartTime() != null, WecomSyncRecord::getSyncTime, dto.getStartTime())
                .le(dto.getEndTime() != null, WecomSyncRecord::getSyncTime, dto.getEndTime())
                .orderByDesc(WecomSyncRecord::getSyncTime);
        Page<WecomSyncRecord> result = syncRecordMapper.selectPage(page, wrapper);
        List<WecomSyncVO> voList = result.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public WecomSyncVO getSyncDetail(Long id) {
        WecomSyncRecord record = syncRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "同步记录不存在");
        }
        return toVO(record);
    }

    @Override
    public void exportSyncs(WecomSyncQueryDTO dto, HttpServletResponse response) throws IOException {
        // TODO: Phase 7 实现 Excel 导出（使用 hutool ExcelUtil）
        log.info("企微同步记录导出请求，查询条件: syncType={}, syncStatus={}", dto.getSyncType(), dto.getSyncStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"导出功能开发中，将在 Phase 7 实现\"}");
    }

    private WecomSyncVO toVO(WecomSyncRecord record) {
        WecomSyncVO vo = new WecomSyncVO();
        vo.setId(record.getId());
        vo.setCompanyId(record.getCompanyId());
        vo.setSyncType(record.getSyncType());
        vo.setSyncDirection(record.getSyncDirection());
        vo.setSourceId(record.getSourceId());
        vo.setTargetId(record.getTargetId());
        vo.setSyncStatus(record.getSyncStatus());
        vo.setRetryCount(record.getRetryCount());
        vo.setErrorMsg(record.getErrorMsg());
        vo.setSyncTime(record.getSyncTime());
        return vo;
    }
}
