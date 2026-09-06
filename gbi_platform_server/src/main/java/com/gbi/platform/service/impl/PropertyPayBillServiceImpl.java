package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.PayBillCreateDTO;
import com.gbi.platform.dto.PayBillPayDTO;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.BizPayOrder;
import com.gbi.platform.entity.BizPayOrderItem;
import com.gbi.platform.entity.FeeItem;
import com.gbi.platform.entity.FeeRule;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.entity.PropertyFeeBill;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.mapper.StallContractMapper;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.BizPayOrderItemMapper;
import com.gbi.platform.mapper.BizPayOrderMapper;
import com.gbi.platform.mapper.FeeItemMapper;
import com.gbi.platform.mapper.FeeRuleMapper;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.mapper.PropertyFeeBillMapper;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.mapper.WaterElecBillMapper;
import com.gbi.platform.service.PropertyPayBillService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FlowNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 缴费单服务实现（合并缴费）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyPayBillServiceImpl implements PropertyPayBillService {

    private final BizPayOrderMapper payOrderMapper;
    private final BizPayOrderItemMapper payOrderItemMapper;
    private final BizFeeBillMapper bizFeeBillMapper;
    private final PropertyFeeBillMapper propertyFeeBillMapper;
    private final WaterElecBillMapper waterElecBillMapper;
    private final BizFinanceFlowMapper financeFlowMapper;
    private final StallInfoMapper stallInfoMapper;
    private final StallTenantMapper stallTenantMapper;
    private final StallCategoryMapper categoryMapper;
    private final MarketInfoMapper marketInfoMapper;
    private final FlowNoGenerator flowNoGenerator;
    private final RecvPayPlanService recvPayPlanService;
    private final AuditLogUtil auditLogUtil;
    private final FeeRuleMapper feeRuleMapper;
    private final FeeItemMapper feeItemMapper;
    private final StallContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPayBill(PayBillCreateDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        // 1. 查询统一账单
        List<BizFeeBill> bills = bizFeeBillMapper.selectBatchIds(dto.getBizFeeBillIds());
        if (bills.isEmpty()) {
            throw new BizException("未找到对应的统一账单");
        }
        // 过滤已缴费的账单
        List<BizFeeBill> unpaidBills = bills.stream()
                .filter(b -> b.getPayStatus() == null || b.getPayStatus() == CommonConst.BILL_PAY_STATUS_UNPAID
                        || b.getPayStatus() == CommonConst.BILL_PAY_STATUS_PART)
                .collect(Collectors.toList());
        if (unpaidBills.isEmpty()) {
            throw new BizException("所选账单已全部缴清，无需重复缴费");
        }

        // 2. 取第一个账单的铺位信息（合并缴费假设同铺位或同商户）
        BizFeeBill first = unpaidBills.get(0);

        // 3. 确定商户ID（源账单为空时从合约兜底查询）
        Long merchantId = resolveMerchantId(first.getStallId(), first.getMerchantId());

        // 4. 创建缴费单
        BizPayOrder payOrder = new BizPayOrder();
        payOrder.setCompanyId(companyId);
        payOrder.setPayBillNo(flowNoGenerator.generate(companyId));
        payOrder.setSourceType("fee_bill");
        payOrder.setSourceId(first.getSourceBillId());
        payOrder.setStallId(first.getStallId());
        payOrder.setMerchantId(merchantId);
        // 计算总额
        BigDecimal totalAmount = unpaidBills.stream()
                .map(b -> b.getRealAmount() != null ? b.getRealAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        payOrder.setTotalAmount(totalAmount);
        payOrder.setPaidAmount(BigDecimal.ZERO);
        payOrder.setUnpaidAmount(totalAmount);
        payOrder.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        payOrder.setRemark(dto.getRemark());
        payOrderMapper.insert(payOrder);
        Long payOrderId = payOrder.getId();

        // 4. 创建缴费单明细
        for (BizFeeBill bill : unpaidBills) {
            BizPayOrderItem item = new BizPayOrderItem();
            item.setPayBillId(payOrderId);
            item.setBillId(bill.getSourceBillId());
            item.setBizType(bill.getBizType());
            item.setRuleName(lookupRuleName(bill.getRuleId(), bill.getBizType()));
            item.setFeeItemType(lookupFeeItemName(bill.getRuleId(), bill.getBizType()));
            item.setBillMonth(bill.getBillMonth());
            item.setAmount(bill.getRealAmount() != null ? bill.getRealAmount() : BigDecimal.ZERO);
            item.setDiscountAmount(bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO);
            item.setPaidAmount(BigDecimal.ZERO);
            item.setUnpaidAmount(bill.getRealAmount() != null ? bill.getRealAmount() : BigDecimal.ZERO);
            payOrderItemMapper.insert(item);
        }

        log.info("创建缴费单成功：payOrderId={}, 明细数={}, 总金额={}",
                payOrderId, unpaidBills.size(), totalAmount);
        return payOrderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(PayBillPayDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        // 1. 查询缴费单
        BizPayOrder payOrder = payOrderMapper.selectById(dto.getPayBillId());
        if (payOrder == null) {
            throw new BizException("缴费单不存在");
        }
        if (!companyId.equals(payOrder.getCompanyId())) {
            throw new BizException("无权操作该缴费单");
        }
        if (payOrder.getPayStatus() == CommonConst.FINANCE_PAY_ORDER_STATUS_DONE) {
            throw new BizException("缴费单已缴清，无需重复缴费");
        }

        // 2. 查询缴费单明细
        List<BizPayOrderItem> items = payOrderItemMapper.selectList(
                new LambdaQueryWrapper<BizPayOrderItem>()
                        .eq(BizPayOrderItem::getPayBillId, dto.getPayBillId()));

        // 3. 更新缴费单状态
        BizPayOrder updateOrder = new BizPayOrder();
        updateOrder.setId(dto.getPayBillId());
        updateOrder.setPayStatus(CommonConst.FINANCE_PAY_ORDER_STATUS_DONE);
        updateOrder.setPaidAmount(payOrder.getTotalAmount());
        updateOrder.setUnpaidAmount(BigDecimal.ZERO);
        updateOrder.setPayTime(LocalDateTime.now());
        payOrderMapper.updateById(updateOrder);

        // 4. 逐条明细处理
        for (BizPayOrderItem item : items) {
            // 4a. 更新统一账单状态（按 sourceBillId + bizType + companyId 唯一匹配）
            BizFeeBill bizBill = bizFeeBillMapper.selectOne(
                    new LambdaQueryWrapper<BizFeeBill>()
                            .eq(BizFeeBill::getSourceBillId, item.getBillId())
                            .eq(BizFeeBill::getBizType, item.getBizType())
                            .eq(BizFeeBill::getCompanyId, companyId));
            if (bizBill != null) {
                BizFeeBill billUpdate = new BizFeeBill();
                billUpdate.setId(bizBill.getId());
                billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
                billUpdate.setPayTime(LocalDateTime.now());
                bizFeeBillMapper.updateById(billUpdate);
            }

            // 4b. 更新源账单状态
            if (CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(item.getBizType())) {
                PropertyFeeBill srcBill = propertyFeeBillMapper.selectById(item.getBillId());
                if (srcBill != null) {
                    PropertyFeeBill update = new PropertyFeeBill();
                    update.setId(item.getBillId());
                    update.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
                    update.setPayTime(LocalDateTime.now());
                    propertyFeeBillMapper.updateById(update);

                    // 应收应付核销
                    BizFinanceFlow flow = buildFinanceFlow(companyId, loginUser, payOrder, item, dto);
                    financeFlowMapper.insert(flow);
                    recvPayPlanService.writeOffByBillId(companyId, item.getBillId(), flow,
                            CommonConst.WRITEOFF_TYPE_PAY, "合并缴费-物业费核销");
                }
            } else if (CommonConst.BIZ_TYPE_WATER_ELEC.equals(item.getBizType())) {
                WaterElecBill srcBill = waterElecBillMapper.selectById(item.getBillId());
                if (srcBill != null) {
                    WaterElecBill update = new WaterElecBill();
                    update.setId(item.getBillId());
                    update.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
                    update.setPayTime(LocalDateTime.now());
                    waterElecBillMapper.updateById(update);

                    // 应收应付核销
                    BizFinanceFlow flow = buildFinanceFlow(companyId, loginUser, payOrder, item, dto);
                    financeFlowMapper.insert(flow);
                    if (srcBill.getPlanId() != null) {
                        recvPayPlanService.writeOffByBillId(companyId, item.getBillId(), flow,
                                CommonConst.WRITEOFF_TYPE_PAY, "合并缴费-水电费核销");
                    }
                }
            }
        }

        // 5. 审计日志
        auditLogUtil.record(CommonConst.MODULE_PROPERTY_FEE, CommonConst.OPER_TYPE_PAY,
                String.valueOf(dto.getPayBillId()), null, "合并缴费，金额=" + payOrder.getTotalAmount());

        log.info("合并缴费成功：payOrderId={}, 总金额={}, 明细数={}",
                dto.getPayBillId(), payOrder.getTotalAmount(), items.size());
    }

    /**
     * 构建财务流水记录
     */
    private BizFinanceFlow buildFinanceFlow(Long companyId, LoginUser loginUser,
                                            BizPayOrder payOrder, BizPayOrderItem item,
                                            PayBillPayDTO dto) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setBusinessType(item.getBizType());
        flow.setBillId(String.valueOf(item.getBillId()));
        flow.setStallId(payOrder.getStallId());
        flow.setMerchantId(payOrder.getMerchantId());
        // 查询铺位快照
        if (payOrder.getStallId() != null) {
            StallInfo stall = stallInfoMapper.selectById(payOrder.getStallId());
            if (stall != null) {
                flow.setStallNumber(stall.getStallNumber());
                flow.setStallName(stall.getStallName());
                flow.setStallMarketName(getMarketNameById(stall.getMarketId()));
                flow.setCategoryName(getCategoryNameById(stall.getStallCategoryId()));
            }
        }
        if (payOrder.getMerchantId() != null) {
            StallTenant tenant = stallTenantMapper.selectById(payOrder.getMerchantId());
            flow.setMerchantName(tenant != null ? tenant.getTenantName() : null);
        }
        flow.setOriginalAmount(item.getAmount());
        flow.setDiscountAmount(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO);
        flow.setRealAmount(item.getAmount());
        flow.setPayType(dto.getPayType());
        flow.setFlowType(CommonConst.FLOW_TYPE_INCOME);
        flow.setStatus(1);
        flow.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_NORMAL);
        flow.setRemark(dto.getRemark() != null ? dto.getRemark() : "合并缴费");
        flow.setCreateBy(loginUser.getUserId());
        return flow;
    }

    /**
     * 确定商户ID：源账单有值直接返回，否则从合约查询租户ID作为兜底
     */
    private Long resolveMerchantId(Long stallId, Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        if (stallId == null) {
            return null;
        }
        StallContract activeContract = contractMapper.selectOne(
                new LambdaQueryWrapper<StallContract>()
                        .eq(StallContract::getStallId, stallId)
                        .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE)
                        .last("LIMIT 1"));
        if (activeContract != null && activeContract.getTenantId() != null) {
            return activeContract.getTenantId();
        }
        return null;
    }

    private String getMarketNameById(Long marketId) {
        if (marketId == null) return null;
        MarketInfo m = marketInfoMapper.selectById(marketId);
        return m != null ? m.getMarketName() : null;
    }

    private String getCategoryNameById(Long categoryId) {
        if (categoryId == null) return null;
        StallCategory cat = categoryMapper.selectById(categoryId);
        return cat != null ? cat.getCategoryName() : null;
    }

    /**
     * 查询规则名称
     */
    private String lookupRuleName(Long ruleId, String bizType) {
        if (ruleId != null) {
            FeeRule rule = feeRuleMapper.selectById(ruleId);
            if (rule != null && rule.getRuleName() != null) {
                return rule.getRuleName();
            }
        }
        // 降级：根据业务类型返回默认名称
        return CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(bizType) ? "物业费" : "水电费";
    }

    /**
     * 查询收费项名称
     */
    private String lookupFeeItemName(Long ruleId, String bizType) {
        if (ruleId != null) {
            FeeRule rule = feeRuleMapper.selectById(ruleId);
            if (rule != null && rule.getFeeItemId() != null) {
                FeeItem item = feeItemMapper.selectById(rule.getFeeItemId());
                if (item != null && item.getFeeItemName() != null) {
                    return item.getFeeItemName();
                }
            }
        }
        // 降级：根据业务类型返回默认名称
        return CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(bizType) ? "物业费" : "水电费";
    }
}