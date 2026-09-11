package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.DiscountPolicyQueryDTO;
import com.gbi.platform.entity.BizDiscountApply;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.mapper.BizDiscountApplyMapper;
import com.gbi.platform.mapper.BizDiscountPolicyMapper;
import com.gbi.platform.service.DiscountPolicyService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.DiscountPolicyVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠策略服务实现（集团模板豁免多租户过滤，子公司可见集团模板 + 本公司自建）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountPolicyServiceImpl implements DiscountPolicyService {

    private final BizDiscountPolicyMapper policyMapper;
    private final BizDiscountApplyMapper applyMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<DiscountPolicyVO> page(DiscountPolicyQueryDTO dto) {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        LambdaQueryWrapper<BizDiscountPolicy> wrapper = new LambdaQueryWrapper<BizDiscountPolicy>()
                .and(w -> w.eq(BizDiscountPolicy::getCompanyId, CommonConst.COMPANY_ROOT)
                        .or().eq(BizDiscountPolicy::getCompanyId, companyId))
                .like(StringUtils.hasText(dto.getPolicyName()), BizDiscountPolicy::getPolicyName, dto.getPolicyName())
                .eq(dto.getBizType() != null, BizDiscountPolicy::getBizType, dto.getBizType())
                .eq(dto.getDiscountType() != null, BizDiscountPolicy::getDiscountType, dto.getDiscountType())
                .eq(dto.getScopeType() != null, BizDiscountPolicy::getScopeType, dto.getScopeType())
                .eq(dto.getStatus() != null, BizDiscountPolicy::getStatus, dto.getStatus())
                .orderByDesc(BizDiscountPolicy::getSortOrder)
                .orderByDesc(BizDiscountPolicy::getId);
        Page<BizDiscountPolicy> page = policyMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<DiscountPolicyVO> vos = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(vos, page.getTotal(), dto.getPageNum().longValue(), dto.getPageSize().longValue(), page.getPages());
    }

    @Override
    public void add(BizDiscountPolicy policy) {
        if (policy == null || !StringUtils.hasText(policy.getPolicyName())) {
            throw new BizException("策略名称不能为空");
        }
        Long count = policyMapper.selectCount(new LambdaQueryWrapper<BizDiscountPolicy>()
                .eq(BizDiscountPolicy::getCompanyId, UserContext.getLoginUser().getCompanyId())
                .eq(BizDiscountPolicy::getPolicyName, policy.getPolicyName()));
        if (count != null && count > 0) {
            throw new BizException("策略名称已存在");
        }
        policy.setCompanyId(UserContext.getLoginUser().getCompanyId());
        policy.setCreateBy(UserContext.getUserIdOrZero());
        policyMapper.insert(policy);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_ADD, String.valueOf(policy.getId()), null, policy);
    }

    @Override
    public void update(BizDiscountPolicy policy) {
        if (policy == null || policy.getId() == null) {
            throw new BizException("策略ID不能为空");
        }
        BizDiscountPolicy db = policyMapper.selectById(policy.getId());
        if (db == null) {
            throw new BizException("策略不存在");
        }
        if (Objects.equals(db.getCompanyId(), CommonConst.COMPANY_ROOT)
                && !Objects.equals(UserContext.getLoginUser().getCompanyId(), CommonConst.COMPANY_ROOT)) {
            throw new BizException("集团模板策略仅集团可编辑");
        }
        policyMapper.updateById(policy);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_UPDATE, String.valueOf(policy.getId()), db, policy);
    }

    @Override
    public void delete(Long id) {
        BizDiscountPolicy db = policyMapper.selectById(id);
        if (db == null) {
            throw new BizException("策略不存在");
        }
        Long refCount = applyMapper.selectCount(new LambdaQueryWrapper<BizDiscountApply>()
                .eq(BizDiscountApply::getPolicyId, id));
        if (refCount != null && refCount > 0) {
            throw new BizException("该策略已被优惠申请引用，禁止删除");
        }
        policyMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), db, null);
    }

    private DiscountPolicyVO toVO(BizDiscountPolicy p) {
        DiscountPolicyVO vo = new DiscountPolicyVO();
        vo.setId(p.getId());
        vo.setCompanyId(p.getCompanyId());
        vo.setPolicyName(p.getPolicyName());
        vo.setBizType(p.getBizType());
        vo.setBizTypeText(bizTypeText(p.getBizType()));
        vo.setDiscountType(p.getDiscountType());
        vo.setDiscountTypeText(discountTypeText(p.getDiscountType()));
        vo.setWaiveMonths(p.getWaiveMonths());
        vo.setDiscountRate(p.getDiscountRate());
        vo.setDeductAmount(p.getDeductAmount());
        vo.setFixedAmount(p.getFixedAmount());
        vo.setTierConfig(p.getTierConfig());
        vo.setScopeType(p.getScopeType());
        vo.setScopeTypeText(scopeTypeText(p.getScopeType()));
        vo.setScopeIds(p.getScopeIds());
        vo.setStartTime(p.getStartTime());
        vo.setEndTime(p.getEndTime());
        vo.setMaxApplyMonths(p.getMaxApplyMonths());
        vo.setAutoApprove(p.getAutoApprove());
        vo.setAutoApproveText(Objects.equals(p.getAutoApprove(), CommonConst.STATUS_ENABLED) ? "自动生效" : "需审批");
        vo.setStatus(p.getStatus());
        vo.setStatusText(Objects.equals(p.getStatus(), CommonConst.STATUS_ENABLED) ? "启用" : "停用");
        vo.setSortOrder(p.getSortOrder());
        vo.setRemark(p.getRemark());
        vo.setCreateBy(p.getCreateBy());
        vo.setCreateTime(p.getCreateTime());
        vo.setUpdateBy(p.getUpdateBy());
        vo.setUpdateTime(p.getUpdateTime());
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

    private String discountTypeText(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case CommonConst.DISCOUNT_TYPE_WAIVE -> "免租期";
            case CommonConst.DISCOUNT_TYPE_RATE -> "折扣率";
            case CommonConst.DISCOUNT_TYPE_DEDUCT -> "减免金额";
            case CommonConst.DISCOUNT_TYPE_COMBO -> "组合";
            case CommonConst.DISCOUNT_TYPE_FIXED -> "定额优惠";
            case CommonConst.DISCOUNT_TYPE_TIER -> "阶梯优惠";
            default -> String.valueOf(type);
        };
    }

    private String scopeTypeText(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case CommonConst.SCOPE_TYPE_CONTRACT -> "按合同";
            case CommonConst.SCOPE_TYPE_STALL -> "按铺位";
            case CommonConst.SCOPE_TYPE_TENANT -> "按租户";
            case CommonConst.SCOPE_TYPE_MARKET -> "按市场";
            case CommonConst.SCOPE_TYPE_CATEGORY -> "按分类";
            default -> String.valueOf(type);
        };
    }
}