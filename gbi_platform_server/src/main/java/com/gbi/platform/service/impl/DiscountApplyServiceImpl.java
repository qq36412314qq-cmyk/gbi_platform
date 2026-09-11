package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.DiscountApplyDTO;
import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.entity.BizDiscountApply;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.engine.discount.DiscountCalculator;
import com.gbi.platform.engine.discount.DiscountCalculatorFactory;
import com.gbi.platform.mapper.BizDiscountApplyMapper;
import com.gbi.platform.mapper.BizDiscountPolicyMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.DiscountCalcVO;
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
 * 支持多业务类型：租赁/物业/水电/幼儿园
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
    private static final String SNAPSHOT_KEY_FIXED = "fixedAmount";
    private static final String SNAPSHOT_KEY_TIER = "tierConfig";

    private final BizDiscountApplyMapper applyMapper;
    private final BizDiscountPolicyMapper policyMapper;
    private final LeaseContractService leaseContractService;
    private final FlowEngineService flowEngineService;
    private final RecvPayPlanService recvPayPlanService;
    private final ConfigService configService;
    private final AuditLogUtil auditLogUtil;
    private final DiscountCalculatorFactory calculatorFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createForContract(Long contractId, Long policyId, Integer waiveMonths, BigDecimal discountRate,
                                  BigDecimal deductAmount, String remark) {
        // 兼容现有合同优惠逻辑，biz_type 固定为 rent
        DiscountApplyDTO dto = new DiscountApplyDTO();
        dto.setBizType(CommonConst.BIZ_TYPE_RENT);
        dto.setSourceType(CommonConst.PLAN_SOURCE_CONTRACT);
        dto.setSourceId(String.valueOf(contractId));
        dto.setPolicyId(policyId);
        dto.setStallId(leaseContractService.getContractById(contractId).getStallId());
        dto.setTenantId(leaseContractService.getContractById(contractId).getTenantId());
        dto.setStartMonth(leaseContractService.getContractById(contractId).getStartTime().toString().substring(0, 7));
        dto.setEndMonth(leaseContractService.getContractById(contractId).getEndTime().toString().substring(0, 7));
        dto.setWaiveMonths(waiveMonths);
        dto.setDiscountRate(discountRate);
        dto.setDeductAmount(deductAmount);
        dto.setRemark(remark);
        return createApply(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApply(DiscountApplyDTO dto) {
        // 1. 校验来源单据存在性
        validateSource(dto);
        
        // 2. 获取或构建申请单
        BizDiscountApply apply = buildApply(dto);
        
        // 3. 计算优惠金额
        BigDecimal discountAmount = calculateDiscount(apply);
        apply.setDiscountAmount(discountAmount);
        
        // 4. 计算实收金额
        BigDecimal originalAmount = dto.getOriginalAmount() != null ? dto.getOriginalAmount() : BigDecimal.ZERO;
        apply.setOriginalAmount(originalAmount);
        apply.setRealAmount(originalAmount.subtract(discountAmount).max(BigDecimal.ZERO));
        
        // 5. 阈值判断
        int needAudit = judgeThreshold(apply, originalAmount);
        apply.setNeedAudit(needAudit);
        
        // 6. 生成申请编号
        apply.setApplyNo("DA-" + apply.getCompanyId() + "-" + LocalDate.now().format(SEQ_DATE_FMT) + "-"
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000)));
        apply.setApplyUserId(UserContext.getUserIdOrZero());
        apply.setCreateBy(UserContext.getUserIdOrZero());
        
        // 7. 根据阈值决定是否发起审批
        if (needAudit == CommonConst.APPLY_NEED_AUDIT_YES) {
            Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_CONTRACT_DISCOUNT,
                    apply.getBizType(), apply.getSourceId(),
                    apply.getBizType() + "优惠申请（" + dto.getStartMonth() + "-" + dto.getEndMonth() + "）");
            apply.setFlowInstanceId(instanceId);
            apply.setApplyStatus(CommonConst.APPLY_STATUS_AUDITING);
        } else {
            apply.setApplyStatus(CommonConst.APPLY_STATUS_PASS);
            apply.setAuditTime(LocalDateTime.now());
        }
        
        applyMapper.insert(apply);
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, CommonConst.OPER_TYPE_ADD, String.valueOf(apply.getId()), null, apply);
        return apply.getId();
    }

    @Override
    public PageVO<DiscountApplyVO> page(DiscountApplyQueryDTO dto) {
        LambdaQueryWrapper<BizDiscountApply> wrapper = new LambdaQueryWrapper<BizDiscountApply>()
                .eq(StringUtils.hasText(dto.getBizType()), BizDiscountApply::getBizType, dto.getBizType())
                .eq(StringUtils.hasText(dto.getSourceType()), BizDiscountApply::getSourceType, dto.getSourceType())
                .like(StringUtils.hasText(dto.getSourceNo()), BizDiscountApply::getSourceNo, dto.getSourceNo())
                .eq(dto.getApplyStatus() != null, BizDiscountApply::getApplyStatus, dto.getApplyStatus())
                .eq(dto.getNeedAudit() != null, BizDiscountApply::getNeedAudit, dto.getNeedAudit())
                .eq(dto.getStallId() != null, BizDiscountApply::getStallId, dto.getStallId())
                .eq(StringUtils.hasText(dto.getStartMonth()), BizDiscountApply::getStartMonth, dto.getStartMonth())
                .eq(StringUtils.hasText(dto.getEndMonth()), BizDiscountApply::getEndMonth, dto.getEndMonth())
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
            // 审批通过，生成应收应付计划
            if (CommonConst.BIZ_TYPE_RENT.equals(apply.getBizType())) {
                recvPayPlanService.generateByContract(Long.valueOf(apply.getSourceId()));
            }
        } else {
            apply.setApplyStatus(result);
            apply.setAuditTime(LocalDateTime.now());
            applyMapper.updateById(apply);
        }
        auditLogUtil.record(CommonConst.MODULE_DISCOUNT, "审批回调", String.valueOf(apply.getId()), null,
                "审批结果=" + result);
    }

    @Override
    public DiscountCalcVO calcPreview(DiscountApplyDTO dto) {
        // 构建临时申请单用于计算
        BizDiscountApply apply = buildApply(dto);
        BigDecimal originalAmount = dto.getOriginalAmount() != null ? dto.getOriginalAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = calculateDiscount(apply);
        
        DiscountCalcVO vo = new DiscountCalcVO();
        vo.setOriginalAmount(originalAmount);
        vo.setDiscountAmount(discountAmount);
        vo.setRealAmount(originalAmount.subtract(discountAmount).max(BigDecimal.ZERO));
        return vo;
    }

    /* ------------------------------ 内部方法 ------------------------------ */

    private void validateSource(DiscountApplyDTO dto) {
        if (CommonConst.PLAN_SOURCE_CONTRACT.equals(dto.getSourceType())) {
            if (leaseContractService.getContractById(Long.valueOf(dto.getSourceId())) == null) {
                throw new BizException("合同不存在");
            }
        }
        // 其他业务类型来源校验可由具体业务Service实现
    }

    private BizDiscountApply buildApply(DiscountApplyDTO dto) {
        BizDiscountApply apply = new BizDiscountApply();
        apply.setCompanyId(UserContext.getLoginUser().getCompanyId());
        apply.setBizType(dto.getBizType());
        apply.setSourceType(dto.getSourceType());
        apply.setSourceId(dto.getSourceId());
        apply.setSourceNo(dto.getSourceNo());
        apply.setPolicyId(dto.getPolicyId());
        apply.setStallId(dto.getStallId());
        apply.setTenantId(dto.getTenantId());
        apply.setStartMonth(dto.getStartMonth());
        apply.setEndMonth(dto.getEndMonth());
        
        // 计算总月数
        if (dto.getStartMonth() != null && dto.getEndMonth() != null) {
            YearMonth start = YearMonth.parse(dto.getStartMonth());
            YearMonth end = YearMonth.parse(dto.getEndMonth());
            apply.setTotalMonths((int) ChronoUnit.MONTHS.between(start, end) + 1);
        }
        
        apply.setWaiveMonths(dto.getWaiveMonths());
        apply.setDiscountRate(dto.getDiscountRate());
        apply.setDeductAmount(dto.getDeductAmount());
        apply.setFixedAmount(dto.getFixedAmount());
        apply.setTierConfig(dto.getTierConfig());
        apply.setRemark(dto.getRemark());
        
        // 构建策略快照
        if (dto.getPolicyId() != null) {
            BizDiscountPolicy policy = policyMapper.selectById(dto.getPolicyId());
            if (policy != null) {
                apply.setPolicySnapshot(buildSnapshot(policy));
            }
        }
        
        return apply;
    }

    private BigDecimal calculateDiscount(BizDiscountApply apply) {
        DiscountCalculator calculator = calculatorFactory.getCalculator(apply.getDiscountType());
        return calculator.calculate(apply.getOriginalAmount() != null ? apply.getOriginalAmount() : BigDecimal.ZERO, apply);
    }

    private int judgeThreshold(BizDiscountApply apply, BigDecimal originalAmount) {
        // 读取阈值配置
        int waiveLimit = readInt("discount." + apply.getBizType() + ".waive_months_limit", 3);
        BigDecimal minRate = readDecimal("discount." + apply.getBizType() + ".min_rate_limit", new BigDecimal("80.00"));
        BigDecimal maxDeduct = readDecimal("discount." + apply.getBizType() + ".deduct_limit", new BigDecimal("5000.00"));
        BigDecimal ratioLimit = readDecimal("discount." + apply.getBizType() + ".ratio_limit", new BigDecimal("30.00"));
        
        // 免租期阈值
        if (apply.getWaiveMonths() != null && apply.getWaiveMonths() > waiveLimit) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        
        // 折扣率阈值
        if (apply.getDiscountRate() != null && apply.getDiscountRate().compareTo(minRate) < 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        
        // 减免金额阈值
        if (apply.getDeductAmount() != null && apply.getDeductAmount().compareTo(maxDeduct) > 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        
        // 定额优惠阈值
        if (apply.getFixedAmount() != null && apply.getFixedAmount().compareTo(maxDeduct) > 0) {
            return CommonConst.APPLY_NEED_AUDIT_YES;
        }
        
        // 占比阈值
        if (originalAmount.compareTo(BigDecimal.ZERO) > 0 && apply.getDiscountAmount() != null) {
            BigDecimal ratio = apply.getDiscountAmount().multiply(new BigDecimal("100"))
                    .divide(originalAmount, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(ratioLimit) > 0) {
                return CommonConst.APPLY_NEED_AUDIT_YES;
            }
        }
        
        return CommonConst.APPLY_NEED_AUDIT_NO;
    }

    private String buildSnapshot(BizDiscountPolicy policy) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("waiveMonths", policy.getWaiveMonths());
        snapshot.put("discountRate", policy.getDiscountRate() != null ? policy.getDiscountRate().toPlainString() : null);
        snapshot.put("deductAmount", policy.getDeductAmount() != null ? policy.getDeductAmount().toPlainString() : null);
        snapshot.put("fixedAmount", policy.getFixedAmount() != null ? policy.getFixedAmount().toPlainString() : null);
        snapshot.put("tierConfig", policy.getTierConfig());
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
        vo.setBizType(apply.getBizType());
        vo.setBizTypeText(bizTypeText(apply.getBizType()));
        vo.setPolicyId(apply.getPolicyId());
        if (apply.getPolicyId() != null) {
            BizDiscountPolicy policy = policyMapper.selectById(apply.getPolicyId());
            vo.setPolicyName(policy == null ? null : policy.getPolicyName());
        }
        vo.setSourceType(apply.getSourceType());
        vo.setSourceId(apply.getSourceId());
        vo.setSourceNo(apply.getSourceNo());
        vo.setContractNo(apply.getContractNo());
        vo.setStallId(apply.getStallId());
        vo.setTenantId(apply.getTenantId());
        vo.setStartMonth(apply.getStartMonth());
        vo.setEndMonth(apply.getEndMonth());
        vo.setTotalMonths(apply.getTotalMonths());
        vo.setWaiveMonths(apply.getWaiveMonths());
        vo.setDiscountRate(apply.getDiscountRate());
        vo.setDeductAmount(apply.getDeductAmount());
        vo.setFixedAmount(apply.getFixedAmount());
        vo.setTierConfig(apply.getTierConfig());
        vo.setOriginalAmount(apply.getOriginalAmount());
        vo.setDiscountAmount(apply.getDiscountAmount());
        vo.setRealAmount(apply.getRealAmount());
        vo.setNeedAudit(apply.getNeedAudit());
        vo.setNeedAuditText(Objects.equals(apply.getNeedAudit(), CommonConst.APPLY_NEED_AUDIT_YES) ? "需审批" : "直接生效");
        vo.setFlowInstanceId(apply.getFlowInstanceId());
        vo.setApplyStatus(apply.getApplyStatus());
        vo.setApplyStatusText(applyStatusText(apply.getApplyStatus()));
        vo.setApplyUserId(apply.getApplyUserId());
        vo.setAuditRemark(apply.getAuditRemark());
        vo.setRemark(apply.getRemark());
        vo.setAuditTime(apply.getAuditTime());
        vo.setCreateTime(apply.getCreateTime());
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