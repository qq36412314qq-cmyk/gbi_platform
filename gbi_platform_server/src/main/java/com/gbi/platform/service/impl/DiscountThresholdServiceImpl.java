package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.DiscountThresholdDTO;
import com.gbi.platform.dto.DiscountThresholdQueryDTO;
import com.gbi.platform.entity.BizDiscountThreshold;
import com.gbi.platform.mapper.BizDiscountThresholdMapper;
import com.gbi.platform.service.DiscountThresholdService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.DiscountThresholdVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠审批阈值配置服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountThresholdServiceImpl implements DiscountThresholdService {

    private final BizDiscountThresholdMapper thresholdMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<DiscountThresholdVO> page(DiscountThresholdQueryDTO dto) {
        LambdaQueryWrapper<BizDiscountThreshold> wrapper = new LambdaQueryWrapper<BizDiscountThreshold>()
                .eq(dto.getBizType() != null, BizDiscountThreshold::getBizType, dto.getBizType())
                .eq(dto.getThresholdType() != null, BizDiscountThreshold::getThresholdType, dto.getThresholdType())
                .eq(dto.getStatus() != null, BizDiscountThreshold::getStatus, dto.getStatus())
                .orderByDesc(BizDiscountThreshold::getId);
        Page<BizDiscountThreshold> page = thresholdMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<DiscountThresholdVO> vos = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(vos, page.getTotal(), dto.getPageNum().longValue(), dto.getPageSize().longValue(), page.getPages());
    }

    @Override
    public void add(DiscountThresholdDTO dto) {
        BizDiscountThreshold threshold = new BizDiscountThreshold();
        threshold.setCompanyId(UserContext.getLoginUser().getCompanyId());
        threshold.setBizType(dto.getBizType());
        threshold.setThresholdType(dto.getThresholdType());
        threshold.setThresholdValue(dto.getThresholdValue());
        threshold.setRequireAudit(dto.getRequireAudit() != null ? dto.getRequireAudit() : CommonConst.STATUS_ENABLED);
        threshold.setStatus(CommonConst.STATUS_ENABLED);
        threshold.setRemark(dto.getRemark());
        threshold.setCreateBy(UserContext.getUserIdOrZero());
        thresholdMapper.insert(threshold);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_ADD, String.valueOf(threshold.getId()), null, threshold);
    }

    @Override
    public void update(DiscountThresholdDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("阈值ID不能为空");
        }
        BizDiscountThreshold db = thresholdMapper.selectById(dto.getId());
        if (db == null) {
            throw new BizException("阈值配置不存在");
        }
        db.setBizType(dto.getBizType());
        db.setThresholdType(dto.getThresholdType());
        db.setThresholdValue(dto.getThresholdValue());
        db.setRequireAudit(dto.getRequireAudit());
        db.setRemark(dto.getRemark());
        db.setUpdateBy(UserContext.getUserIdOrZero());
        thresholdMapper.updateById(db);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_UPDATE, String.valueOf(dto.getId()), db, db);
    }

    @Override
    public void delete(Long id) {
        BizDiscountThreshold db = thresholdMapper.selectById(id);
        if (db == null) {
            throw new BizException("阈值配置不存在");
        }
        thresholdMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), db, null);
    }

    private DiscountThresholdVO toVO(BizDiscountThreshold t) {
        DiscountThresholdVO vo = new DiscountThresholdVO();
        vo.setId(t.getId());
        vo.setCompanyId(t.getCompanyId());
        vo.setBizType(t.getBizType());
        vo.setBizTypeText(bizTypeText(t.getBizType()));
        vo.setThresholdType(t.getThresholdType());
        vo.setThresholdTypeText(thresholdTypeText(t.getThresholdType()));
        vo.setThresholdValue(t.getThresholdValue());
        vo.setRequireAudit(t.getRequireAudit());
        vo.setRequireAuditText(Objects.equals(t.getRequireAudit(), CommonConst.STATUS_ENABLED) ? "是" : "否");
        vo.setStatus(t.getStatus());
        vo.setStatusText(Objects.equals(t.getStatus(), CommonConst.STATUS_ENABLED) ? "启用" : "停用");
        vo.setRemark(t.getRemark());
        vo.setCreateBy(t.getCreateBy());
        vo.setCreateTime(t.getCreateTime());
        vo.setUpdateBy(t.getUpdateBy());
        vo.setUpdateTime(t.getUpdateTime());
        return vo;
    }

    private String bizTypeText(String bizType) {
        if (bizType == null) {
            return "";
        }
        return switch (bizType) {
            case CommonConst.BIZ_TYPE_RENT -> "租赁费";
            case CommonConst.BIZ_TYPE_PROPERTY_FEE -> "物业费";
            case CommonConst.BIZ_TYPE_WATER_ELEC -> "水电费";
            case CommonConst.BIZ_TYPE_KINDERGARTEN -> "幼儿园费";
            default -> bizType;
        };
    }

    private String thresholdTypeText(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case CommonConst.THRESHOLD_TYPE_WAIVE_MONTHS -> "免租期上限";
            case CommonConst.THRESHOLD_TYPE_MIN_RATE -> "折扣率下限";
            case CommonConst.THRESHOLD_TYPE_MAX_DEDUCT -> "减免金额上限";
            case CommonConst.THRESHOLD_TYPE_MAX_FIXED -> "定额上限";
            case CommonConst.THRESHOLD_TYPE_MAX_RATIO -> "占比上限";
            default -> String.valueOf(type);
        };
    }
}