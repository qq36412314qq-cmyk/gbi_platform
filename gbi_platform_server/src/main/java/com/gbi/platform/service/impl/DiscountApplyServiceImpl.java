package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.entity.BizDiscountApply;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.mapper.BizDiscountApplyMapper;
import com.gbi.platform.mapper.BizDiscountPolicyMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 优惠申请服务实现
 * 优惠计算：免租折算 + 折扣折算 + 减免，快照固化审批后计算依据；
 * 集团阈值（免租上限/折扣下限/减免上限/优惠占比）任一超限 → need_audit=1 自动发起 contract_discount 审批
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountApplyServiceImpl implements DiscountApplyService {

    private static final DateTimeFormatter SEQ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SNAPSHOT_KEY_WAIVE = "waiveMonths";
    private static final String SNAPSHOT_KEY_RATE = "discountRate";
    private static final String SNAPSHOT_KEY_DEDUCT = "deductAmount";

    private final BizDiscountApplyMapper applyMapper;
    private final BizDiscountPolicyMapper policyMapper;
    private final LeaseContractService leaseContractService;
    private final FlowEngineService flowEngineService;
    private final RecvPayPlanService recvPayPlanService;
    private final ConfigService configService;
    private final AuditLogUtil auditLogUtil;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createForContract(Long contractId, Long policyId, Integer waiveMonths, BigDecimal discountRate,
                                  BigDecimal deductAmount, String remark) {
        StallContract contract = leaseContractService.getContractById(contractId);
        if (contract == null) {
            throw new BizException("合同不存在");
        }
        BizDiscountPolicy policy = policyId == null ? null : policyMapper.selectById(policyId);
        if (policy != null && !Objects.equals(policy.getStatus(), CommonConst.STATUS_ENABLED)) {
            throw new BizException("所选优惠策略已停用");
        }

        int waive = policy != null && policy.getWaiveMonths() != null ? policy.getWaiveMonths() : (waiveMonths == null ? 0 : waiveMonths);
        BigDecimal rate = policy != null && policy.getDiscountRate() != null ? policy.getDiscountRate()
                : (discountRate == null ? new BigDecimal("100.00") : discountRate);
        BigDecimal deduct = policy != null && policy.getDeductAmount() != null ? policy.getDeductAmount()
                : (deductAmount == null ? BigDecimal.ZERO : deductAmount);

        BigDecimal rent = contract.getRentAmount() == null ? BigDecimal.ZERO : contract.getRentAmount();
        long months = ChronoUnit.MONTHS.between(YearMonth.from(contract.getStartTime()), YearMonth.from(contract.getEndTime())) + 1;
        BigDecimal totalRent = rent.multiply(new BigDecimal(months));

        // 优惠总额 = 免租折算 + 折扣折算 + 减免
        BigDecimal waiveDiscount = rent.multiply(new BigDecimal(waive)).multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal rateDiscount = rent.subtract(rent.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP))
                .multiply(new BigDecimal(months));
        BigDecimal discountAmount = waiveDiscount.add(rateDiscount).add(deduct).max(BigDecimal.ZERO);

        // 集团阈值判定
        int needAudit = judgeThreshold(waive, rate, deduct, discountAmount, totalRent, months);

        BizDiscountApply apply = new BizDiscountApply();
        apply.setCompanyId(contract.getCompanyId());
        apply.setApplyNo("DA-" + contract.getCompanyId() + "-" + LocalDate.now().format(SEQ_DATE_FMT) + "-"
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000)));
        apply.setPolicyId(policy == null ? null : policy.getId());
        apply.setPolicySnapshot(buildSnapshot(waive, rate, deduct));
        apply.setSourceType(CommonConst.PLAN_SOURCE_CONTRACT);
        apply.setSourceId(String.valueOf(contractId));
        apply.setContractNo(contract.getContractNo());
        apply.setStallId(contract.getStallId());
        apply.setTenantId(contract.getTenantId());
        apply.setWaiveMonths(waive);
        apply.setDiscountRate(rate);
        apply.setDeductAmount(deduct);
        apply.setDiscountAmount(discountAmount);
        apply.setNeedAudit(needAudit);
        apply.setApplyUserId(UserContext.getUserIdOrZero());
        apply.setRemark(remark);
        apply.setCreateBy(UserContext.getUserIdOrZero());

        if (needAudit == CommonConst.APPLY_NEED_AUDIT_YES) {
            // 超阈值：发起 contract_discount 审批，通过后由 ContractDiscountFlowHandler 回调生成计划
            Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_CONTRACT_DISCOUNT,
                    CommonConst.PLAN_SOURCE_CONTRACT, String.valueOf(contractId),
                    "合同 " + contract.getContractNo() + " 优惠申请（免租" + waive + "月/折扣" + rate + "%/减免" + deduct + "元）");
            apply.setFlowInstanceId(instanceId);
            apply.setApplyStatus(CommonConst.APPLY_STATUS_AUDITING);
            applyMapper.insert(apply);
        } else {
            // 未超阈值：直接生效，生成收款计划
            apply.setApplyStatus(CommonConst.APPLY_STATUS_PASS);
            apply.setAuditTime(LocalDateTime.now());
            applyMapper.insert(apply);
            recvPayPlanService.generateByContract(contractId);
        }
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_ADD, String.valueOf(apply.getId()), null, apply);
        return apply.getId();
    }

    @Override
    public PageVO<DiscountApplyVO> page(DiscountApplyQueryDTO dto) {
        LambdaQueryWrapper<BizDiscountApply> wrapper = new LambdaQueryWrapper<BizDiscountApply>()
                .like(StringUtils.hasText(dto.getContractNo()), BizDiscountApply::getContractNo, dto.getContractNo())
                .eq(dto.getApplyStatus() != null, BizDiscountApply::getApplyStatus, dto.getApplyStatus())
                .eq(dto.getNeedAudit() != null, BizDiscountApply::getNeedAudit, dto.getNeedAudit())
                .orderByDesc(BizDiscountApply::getCreateTime);
        Page<BizDiscountApply> page = applyMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<DiscountApplyVO> vos = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(vos, page.getTotal(), dto.getPageNum().longValue(), dto.getPageSize().longValue(), page.getPages());
    }

    @Override
    public DiscountApplyVO detail(Long id) {
        BizDiscountApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("优惠申请不存在");
        }
        return toVO(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        BizDiscountApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("优惠申请不存在");
        }
        if (apply.getApplyStatus() != CommonConst.APPLY_STATUS_DRAFT
                && apply.getApplyStatus() != CommonConst.APPLY_STATUS_AUDITING) {
            throw new BizException("仅草稿/审批中的申请可撤销");
        }
        if (apply.getFlowInstanceId() != null) {
            flowEngineService.terminate(apply.getFlowInstanceId(), "优惠申请撤销");
        }
        apply.setApplyStatus(CommonConst.APPLY_STATUS_VOID);
        applyMapper.updateById(apply);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_DELETE, String.valueOf(apply.getId()), null, "撤销优惠申请");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFlowResult(Long flowInstanceId, int result) {
        BizDiscountApply apply = applyMapper.selectOne(new LambdaQueryWrapper<BizDiscountApply>()
                .eq(BizDiscountApply::getFlowInstanceId, flowInstanceId)
                .last("LIMIT 1"));
        if (apply == null) {
            log.warn("优惠申请审批回调未找到申请 flowInstanceId={}", flowInstanceId);
            return;
        }
        if (result == CommonConst.APPLY_STATUS_PASS) {
            apply.setApplyStatus(CommonConst.APPLY_STATUS_PASS);
            apply.setAuditTime(LocalDateTime.now());
            applyMapper.updateById(apply);
            recvPayPlanService.generateByContract(Long.valueOf(apply.getSourceId()));
        } else {
            apply.setApplyStatus(result);
            apply.setAuditTime(LocalDateTime.now());
            applyMapper.updateById(apply);
        }
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, "审批回调", String.valueOf(apply.getId()), null,
                "审批结果=" + result);
    }

    /* ------------------------------ 内部方法 ------------------------------ */

    private int judgeThreshold(int waive, BigDecimal rate, BigDecimal deduct, BigDecimal discountAmount,
                               BigDecimal totalRent, long months) {
        int waiveLimit = readInt(CommonConst.CONFIG_DISCOUNT_WAIVE_MONTHS_LIMIT, 3);
        BigDecimal minRate = readDecimal(CommonConst.CONFIG_DISCOUNT_MIN_RATE_LIMIT, new BigDecimal("80.00"));
        BigDecimal maxDeduct = readDecimal(CommonConst.CONFIG_DISCOUNT_MAX_DEDUCT_LIMIT, new BigDecimal("5000.00"));
        BigDecimal ratioLimit = readDecimal(CommonConst.CONFIG_DISCOUNT_CONTRACT_RATIO_LIMIT, new BigDecimal("10.00"));

        if (waive > waiveLimit) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        if (rate.compareTo(minRate) < 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        if (deduct.compareTo(maxDeduct) > 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        if (totalRent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = discountAmount.multiply(new BigDecimal("100")).divide(totalRent, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(ratioLimit) > 0) {
                return CommonConst.APPLY_NEED_AUDIT_YES;
            }
        }
        if (months <= 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        return CommonConst.APPLY_NEED_AUDIT_NO;
    }

    private String buildSnapshot(int waive, BigDecimal rate, BigDecimal deduct) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put(SNAPSHOT_KEY_WAIVE, waive);
        snapshot.put(SNAPSHOT_KEY_RATE, rate.toPlainString());
        snapshot.put(SNAPSHOT_KEY_DEDUCT, deduct.toPlainString());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BizException("优惠快照生成失败");
        }
    }

    private int readInt(String key, int defaultValue) {
        String value = configService.getValueByKey(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal readDecimal(String key, BigDecimal defaultValue) {
        String value = configService.getValueByKey(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private DiscountApplyVO toVO(BizDiscountApply apply) {
        DiscountApplyVO vo = new DiscountApplyVO();
        vo.setId(apply.getId());
        vo.setCompanyId(apply.getCompanyId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setPolicyId(apply.getPolicyId());
        if (apply.getPolicyId() != null) {
            BizDiscountPolicy policy = policyMapper.selectById(apply.getPolicyId());
            vo.setPolicyName(policy == null ? null : policy.getPolicyName());
        }
        vo.setContractNo(apply.getContractNo());
        vo.setStallId(apply.getStallId());
        vo.setTenantId(apply.getTenantId());
        vo.setWaiveMonths(apply.getWaiveMonths());
        vo.setDiscountRate(apply.getDiscountRate());
        vo.setDeductAmount(apply.getDeductAmount());
        vo.setDiscountAmount(apply.getDiscountAmount());
        vo.setNeedAudit(apply.getNeedAudit());
        vo.setNeedAuditText(Objects.equals(apply.getNeedAudit(), CommonConst.APPLY_NEED_AUDIT_YES) ? "需审批" : "直接生效");
        vo.setFlowInstanceId(apply.getFlowInstanceId());
        vo.setApplyStatus(apply.getApplyStatus());
        vo.setApplyStatusText(applyStatusText(apply.getApplyStatus()));
        vo.setApplyUserName(apply.getApplyUserId() == null ? null : String.valueOf(apply.getApplyUserId()));
        vo.setRemark(apply.getRemark());
        vo.setAuditTime(apply.getAuditTime());
        vo.setCreateTime(apply.getCreateTime());
        return vo;
    }

    private String applyStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case CommonConst.APPLY_STATUS_DRAFT -> "草稿";
            case CommonConst.APPLY_STATUS_AUDITING -> "审批中";
            case CommonConst.APPLY_STATUS_PASS -> "通过";
            case CommonConst.APPLY_STATUS_REJECT -> "驳回";
            case CommonConst.APPLY_STATUS_VOID -> "作废";
            default -> String.valueOf(status);
        };
    }
}