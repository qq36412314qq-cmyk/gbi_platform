package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.UnifiedPayDTO;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.BizPayOrder;
import com.gbi.platform.entity.BizPayOrderItem;
import com.gbi.platform.entity.FeeItem;
import com.gbi.platform.entity.FeeRule;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.entity.PropertyFeeBill;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.entity.WaterElecPayRecord;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.BizPayOrderItemMapper;
import com.gbi.platform.mapper.BizPayOrderMapper;
import com.gbi.platform.mapper.FeeItemMapper;
import com.gbi.platform.mapper.FeeRuleMapper;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.mapper.PropertyFeeBillMapper;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.mapper.StallContractMapper;
import com.gbi.platform.mapper.WaterElecBillMapper;
import com.gbi.platform.mapper.WaterElecPayRecordMapper;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.UnifiedPayService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FlowNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一缴费服务实现
 * 根据 billType 路由到物业费或水电费缴费逻辑
 * 缴费前先确保 biz_fee_bill 统一账单存在（不存在则自动创建）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedPayServiceImpl implements UnifiedPayService {

    private final BizFeeBillMapper bizFeeBillMapper;
    private final PropertyFeeBillMapper propertyFeeBillMapper;
    private final WaterElecBillMapper waterElecBillMapper;
    private final WaterElecPayRecordMapper payRecordMapper;
    private final BizFinanceFlowMapper financeFlowMapper;
    private final BizPayOrderMapper payOrderMapper;
    private final BizPayOrderItemMapper payOrderItemMapper;
    private final FeeRuleMapper feeRuleMapper;
    private final FeeItemMapper feeItemMapper;
    private final StallInfoMapper stallInfoMapper;
    private final StallTenantMapper stallTenantMapper;
    private final StallCategoryMapper categoryMapper;
    private final MarketInfoMapper marketInfoMapper;
    private final RecvPayPlanService recvPayPlanService;
    private final FlowNoGenerator flowNoGenerator;
    private final AuditLogUtil auditLogUtil;
    private final LeaseContractService leaseContractService;
    private final StallContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(UnifiedPayDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        // 幂等检查：同公司同 requestId 不可重复提交
        checkIdempotent(companyId, dto.getRequestId());

        // 查询或创建统一账单（biz_fee_bill）
        // dto.getBillId() 是源账单ID（property_fee_bill.id / water_elec_bill.id）
        BizFeeBill bizBill = findOrCreateBizBill(companyId, dto);
        if (bizBill == null) {
            throw new BizException("创建支付订单失败，请重试");
        }
        Long sourceBillId = bizBill.getSourceBillId();

        // 根据业务类型执行缴费
        if (CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(dto.getBillType())) {
            payPropertyFee(companyId, loginUser, dto, bizBill, sourceBillId);
        } else if (CommonConst.BIZ_TYPE_WATER_ELEC.equals(dto.getBillType())) {
            payWaterElec(companyId, loginUser, dto, bizBill, sourceBillId);
        } else {
            throw new BizException("不支持的业务类型：" + dto.getBillType());
        }
    }

    // -------------------------------------------------------------------------
    // 查找或创建统一账单
    // -------------------------------------------------------------------------

    /**
     * 查找或创建 biz_fee_bill 统一账单（支付订单）
     * 若 biz_fee_bill 不存在，则根据源账单自动创建，保持与同步逻辑一致
     */
    private BizFeeBill findOrCreateBizBill(Long companyId, UnifiedPayDTO dto) {
        Long billId = dto.getBillId();

        // 先判断传入的 billId 是否已是 biz_fee_bill 记录（含 sourceBillId 的非空 ID）
        BizFeeBill existingBizBill = bizFeeBillMapper.selectById(billId);
        if (existingBizBill != null && existingBizBill.getSourceBillId() != null) {
            // 传入的是统一账单 ID，直接复用
            log.info("传入为统一账单ID，直接复用：billId={}, sourceBillId={}", billId, existingBizBill.getSourceBillId());
            return existingBizBill;
        }
        Long sourceBillId = billId;

        // 再按源账单ID查询是否已存在
        BizFeeBill existing = bizFeeBillMapper.selectOne(
                new LambdaQueryWrapper<BizFeeBill>()
                        .eq(BizFeeBill::getSourceBillId, sourceBillId)
                        .eq(BizFeeBill::getCompanyId, companyId));
        if (existing != null) {
            log.info("统一账单已存在，直接复用：sourceBillId={}, bizBillId={}", sourceBillId, existing.getId());
            return existing;
        }

        // 不存在则从源账单创建
        if (CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(dto.getBillType())) {
            return createBizBillFromPropertyFee(companyId, sourceBillId);
        } else if (CommonConst.BIZ_TYPE_WATER_ELEC.equals(dto.getBillType())) {
            return createBizBillFromWaterElec(companyId, sourceBillId);
        }
        return null;
    }

    /** 从物业费账单创建统一账单 */
    private BizFeeBill createBizBillFromPropertyFee(Long companyId, Long sourceBillId) {
        PropertyFeeBill bill = propertyFeeBillMapper.selectById(sourceBillId);
        if (bill == null) {
            throw new BizException("原始物业费账单不存在或已删除");
        }
        BizFeeBill unifiedBill = new BizFeeBill();
        unifiedBill.setCompanyId(companyId);
        unifiedBill.setBizType(CommonConst.BIZ_TYPE_PROPERTY_FEE);
        unifiedBill.setStallId(bill.getStallId());
        unifiedBill.setMerchantId(bill.getMerchantId());
        unifiedBill.setBillMonth(bill.getBillMonth());
        unifiedBill.setRuleId(bill.getRuleId());
        unifiedBill.setPeriodType(bill.getPeriodType());
        unifiedBill.setOriginalAmount(bill.getAmount());
        unifiedBill.setDiscountAmount(BigDecimal.ZERO);
        unifiedBill.setAdjustAmount(BigDecimal.ZERO);
        unifiedBill.setRealAmount(bill.getAmount());
        unifiedBill.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        unifiedBill.setLockedFlag(1);
        unifiedBill.setSourceBillId(sourceBillId);
        unifiedBill.setCreateBy(UserContext.getUserIdOrZero());
        try {
            bizFeeBillMapper.insert(unifiedBill);
            log.info("创建物业费统一账单成功：sourceBillId={}, bizBillId={}", sourceBillId, unifiedBill.getId());
        } catch (DuplicateKeyException e) {
            // 并发场景下可能已存在，重新查询
            unifiedBill = bizFeeBillMapper.selectOne(
                    new LambdaQueryWrapper<BizFeeBill>()
                            .eq(BizFeeBill::getSourceBillId, sourceBillId)
                            .eq(BizFeeBill::getCompanyId, companyId));
        }
        // 关联应收应付计划
        Long planId = recvPayPlanService.generatePlanForBizFeeBill(unifiedBill);
        if (planId != null) {
            unifiedBill.setPlanId(planId);
            bizFeeBillMapper.updateById(unifiedBill);
        }
        return unifiedBill;
    }

    /** 从水电费账单创建统一账单 */
    private BizFeeBill createBizBillFromWaterElec(Long companyId, Long sourceBillId) {
        WaterElecBill bill = waterElecBillMapper.selectById(sourceBillId);
        if (bill == null) {
            throw new BizException("原始水电费账单不存在或已删除");
        }
        BizFeeBill unifiedBill = new BizFeeBill();
        unifiedBill.setCompanyId(companyId);
        unifiedBill.setBizType(CommonConst.BIZ_TYPE_WATER_ELEC);
        unifiedBill.setStallId(bill.getStallId());
        unifiedBill.setMerchantId(bill.getMerchantId());
        unifiedBill.setBillMonth(bill.getBillMonth());
        // 根据收费类别设定规则ID（水费=4，电费=2）
        Long ruleId = (bill.getCategory() != null && bill.getCategory() == CommonConst.FEE_CATEGORY_WATER) ? 4L : 2L;
        unifiedBill.setRuleId(ruleId);
        unifiedBill.setPeriodType(2);
        unifiedBill.setOriginalAmount(bill.getTotalAmount());
        unifiedBill.setDiscountAmount(BigDecimal.ZERO);
        unifiedBill.setAdjustAmount(BigDecimal.ZERO);
        unifiedBill.setRealAmount(bill.getTotalAmount());
        unifiedBill.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        unifiedBill.setLockedFlag(1);
        unifiedBill.setSourceBillId(sourceBillId);
        unifiedBill.setCreateBy(UserContext.getUserIdOrZero());
        try {
            bizFeeBillMapper.insert(unifiedBill);
            log.info("创建水电费统一账单成功：sourceBillId={}, bizBillId={}", sourceBillId, unifiedBill.getId());
        } catch (DuplicateKeyException e) {
            unifiedBill = bizFeeBillMapper.selectOne(
                    new LambdaQueryWrapper<BizFeeBill>()
                            .eq(BizFeeBill::getSourceBillId, sourceBillId)
                            .eq(BizFeeBill::getCompanyId, companyId));
        }
        // 关联应收应付计划
        Long planId = recvPayPlanService.generatePlanForBizFeeBill(unifiedBill);
        if (planId != null) {
            unifiedBill.setPlanId(planId);
            bizFeeBillMapper.updateById(unifiedBill);
        }
        return unifiedBill;
    }

    // -------------------------------------------------------------------------
    // 物业费缴费
    // -------------------------------------------------------------------------

    private void payPropertyFee(Long companyId, LoginUser loginUser, UnifiedPayDTO dto,
                                BizFeeBill bizBill, Long sourceBillId) {
        PropertyFeeBill bill = propertyFeeBillMapper.selectById(sourceBillId);
        if (bill == null) {
            throw new BizException("原始账单不存在或已删除");
        }
        if (!companyId.equals(bill.getCompanyId())) {
            throw new BizException("无权操作该账单");
        }
        if (bill.getPayStatus() == CommonConst.BILL_PAY_STATUS_PAID) {
            throw new BizException("账单已缴清，无需重复缴费");
        }

        WaterElecPayRecord record = buildPayRecord(companyId, bill, dto, loginUser, CommonConst.BIZ_TYPE_PROPERTY_FEE);

        try {
            payRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }

        // 更新源账单状态
        PropertyFeeBill billUpdate = new PropertyFeeBill();
        billUpdate.setId(sourceBillId);
        billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        billUpdate.setPayTime(LocalDateTime.now());
        propertyFeeBillMapper.updateById(billUpdate);

        // 写入财务流水
        BizFinanceFlow flow = insertFinanceFlow(companyId, CommonConst.BIZ_TYPE_PROPERTY_FEE,
                sourceBillId, bill.getStallId(), record, bill.getAmount());
        payRecordMapper.updateById(buildFlowUpdate(record.getId(), flow.getFlowNo()));

        // 应收应付核销
        recvPayPlanService.writeOffByBillId(companyId, sourceBillId, flow,
                CommonConst.WRITEOFF_TYPE_PAY, "物业费缴费核销");

        // 审计日志
        auditLogUtil.record(CommonConst.MODULE_PROPERTY_FEE, CommonConst.OPER_TYPE_PAY,
                String.valueOf(record.getId()), null, record);

        // 同步更新统一账单状态
        BizFeeBill paySync = new BizFeeBill();
        paySync.setId(bizBill.getId());
        paySync.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        paySync.setPayTime(LocalDateTime.now());
        bizFeeBillMapper.updateById(paySync);

        log.info("物业费缴费成功：bizBillId={}, sourceBillId={}, recordId={}, amount={}",
                bizBill.getId(), sourceBillId, record.getId(), bill.getAmount());
        // 缴费成功后尝试激活签约中的合同
        activateContractIfSigned(bill.getStallId());

        // 写入 finance_pay_order + finance_pay_order_item（单条缴费）
        writePayOrder(companyId, bizBill, bill, dto, record.getPayType(), flow.getFlowNo());
    }

    /**
     * 单条缴费后写入缴费单和明细
     */
    private void writePayOrder(Long companyId, BizFeeBill bizBill, PropertyFeeBill bill,
                               UnifiedPayDTO dto, Integer payType, String flowNo) {
        // 先检查是否已存在同一来源账单的缴费单
        BizPayOrder existing = payOrderMapper.selectBySourceId(bill.getId());
        if (existing != null) {
            log.info("缴费单已存在，跳过创建：payOrderId={}, sourceBillId={}", existing.getId(), bill.getId());
            // 更新缴费单状态
            BizPayOrder update = new BizPayOrder();
            update.setId(existing.getId());
            update.setPayStatus(CommonConst.FINANCE_PAY_ORDER_STATUS_DONE);
            update.setPaidAmount(bill.getAmount());
            update.setUnpaidAmount(BigDecimal.ZERO);
            update.setPayTime(LocalDateTime.now());
            payOrderMapper.updateById(update);
            return;
        }

        // 确定商户ID（源账单为空时从合约兜底查询）
        Long merchantId = resolveMerchantId(bill.getStallId(), bill.getMerchantId());

        // 创建缴费单
        BizPayOrder payOrder = new BizPayOrder();
        payOrder.setCompanyId(companyId);
        payOrder.setPayBillNo(flowNo);
        payOrder.setSourceType("fee_bill");
        payOrder.setSourceId(bill.getId());
        payOrder.setStallId(bill.getStallId());
        payOrder.setMerchantId(merchantId);
        payOrder.setTotalAmount(bill.getAmount());
        payOrder.setPaidAmount(bill.getAmount());
        payOrder.setUnpaidAmount(BigDecimal.ZERO);
        payOrder.setPayStatus(CommonConst.FINANCE_PAY_ORDER_STATUS_DONE);
        payOrder.setPayTime(LocalDateTime.now());
        payOrder.setRemark(dto.getRemark());
        payOrderMapper.insert(payOrder);

        // 创建缴费单明细
        BizPayOrderItem item = new BizPayOrderItem();
        item.setPayBillId(payOrder.getId());
        item.setBillId(bill.getId());
        item.setBizType(CommonConst.BIZ_TYPE_PROPERTY_FEE);
        item.setRuleName(lookupRuleName(null, CommonConst.BIZ_TYPE_WATER_ELEC));
        item.setFeeItemType(lookupFeeItemName(null, CommonConst.BIZ_TYPE_WATER_ELEC));
        item.setBillMonth(bill.getBillMonth());
        item.setAmount(bill.getAmount());
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setPaidAmount(bill.getAmount());
        item.setUnpaidAmount(BigDecimal.ZERO);
        payOrderItemMapper.insert(item);

        log.info("单条缴费写入缴费单成功：payOrderId={}, amount={}, merchantId={}",
                payOrder.getId(), bill.getAmount(), merchantId);
    }

    // -------------------------------------------------------------------------
    // 水电费缴费
    // -------------------------------------------------------------------------

    private void payWaterElec(Long companyId, LoginUser loginUser, UnifiedPayDTO dto,
                              BizFeeBill bizBill, Long sourceBillId) {
        WaterElecBill bill = waterElecBillMapper.selectById(sourceBillId);
        if (bill == null) {
            throw new BizException("原始账单不存在或已删除");
        }
        if (bill.getPayStatus() == CommonConst.BILL_PAY_STATUS_PAID) {
            throw new BizException("账单已缴清，无需重复缴费");
        }

        WaterElecPayRecord record = buildPayRecord(companyId, bill, dto, loginUser, CommonConst.BIZ_TYPE_WATER_ELEC);

        try {
            payRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }

        // 更新源账单状态
        WaterElecBill billUpdate = new WaterElecBill();
        billUpdate.setId(sourceBillId);
        billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        billUpdate.setPayTime(LocalDateTime.now());
        waterElecBillMapper.updateById(billUpdate);

        // 写入财务流水
        BizFinanceFlow flow = insertFinanceFlow(companyId, CommonConst.BIZ_TYPE_WATER_ELEC,
                sourceBillId, bill.getStallId(), record, bill.getTotalAmount());
        payRecordMapper.updateById(buildFlowUpdate(record.getId(), flow.getFlowNo()));

        // 应收应付核销（水电费账单关联计划ID）
        if (bill.getPlanId() != null) {
            recvPayPlanService.writeOffByBillId(companyId, sourceBillId, flow,
                    CommonConst.WRITEOFF_TYPE_PAY, "水电费缴费核销");
        }

        // 审计日志
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_PAY,
                String.valueOf(record.getId()), null, record);

        // 同步更新统一账单状态
        BizFeeBill syncBill = bizFeeBillMapper.selectOne(
                new LambdaQueryWrapper<BizFeeBill>()
                        .eq(BizFeeBill::getSourceBillId, sourceBillId)
                        .eq(BizFeeBill::getCompanyId, companyId));
        if (syncBill != null) {
            BizFeeBill update = new BizFeeBill();
            update.setId(syncBill.getId());
            update.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
            update.setPayTime(LocalDateTime.now());
            bizFeeBillMapper.updateById(update);
        }

        log.info("水电费缴费成功：bizBillId={}, sourceBillId={}, recordId={}, amount={}",
                bizBill.getId(), sourceBillId, record.getId(), bill.getTotalAmount());
        // 缴费成功后尝试激活签约中的合同
        activateContractIfSigned(bill.getStallId());

        // 写入 finance_pay_order + finance_pay_order_item（单条水电费缴费）
        writeWaterElecPayOrder(companyId, bizBill, bill, dto, record.getPayType(), flow.getFlowNo());
    }

    /**
     * 单条水电费缴费后写入缴费单和明细
     */
    private void writeWaterElecPayOrder(Long companyId, BizFeeBill bizBill, WaterElecBill bill,
                                        UnifiedPayDTO dto, Integer payType, String flowNo) {
        BizPayOrder existing = payOrderMapper.selectBySourceId(bill.getId());
        if (existing != null) {
            log.info("缴费单已存在，跳过创建：payOrderId={}, sourceBillId={}", existing.getId(), bill.getId());
            BizPayOrder update = new BizPayOrder();
            update.setId(existing.getId());
            update.setPayStatus(CommonConst.FINANCE_PAY_ORDER_STATUS_DONE);
            update.setPaidAmount(bill.getTotalAmount());
            update.setUnpaidAmount(BigDecimal.ZERO);
            update.setPayTime(LocalDateTime.now());
            payOrderMapper.updateById(update);
            return;
        }

        // 确定商户ID（源账单为空时从合约兜底查询）
        Long merchantId = resolveMerchantId(bill.getStallId(), bill.getMerchantId());

        BizPayOrder payOrder = new BizPayOrder();
        payOrder.setCompanyId(companyId);
        payOrder.setPayBillNo(flowNo);
        payOrder.setSourceType("fee_bill");
        payOrder.setSourceId(bill.getId());
        payOrder.setStallId(bill.getStallId());
        payOrder.setMerchantId(merchantId);
        payOrder.setTotalAmount(bill.getTotalAmount());
        payOrder.setPaidAmount(bill.getTotalAmount());
        payOrder.setUnpaidAmount(BigDecimal.ZERO);
        payOrder.setPayStatus(CommonConst.FINANCE_PAY_ORDER_STATUS_DONE);
        payOrder.setPayTime(LocalDateTime.now());
        payOrder.setRemark(dto.getRemark());
        payOrderMapper.insert(payOrder);

        BizPayOrderItem item = new BizPayOrderItem();
        item.setPayBillId(payOrder.getId());
        item.setBillId(bill.getId());
        item.setBizType(CommonConst.BIZ_TYPE_WATER_ELEC);
        item.setRuleName(lookupRuleName(null, CommonConst.BIZ_TYPE_WATER_ELEC));
        item.setFeeItemType(lookupFeeItemName(null, CommonConst.BIZ_TYPE_WATER_ELEC));
        item.setBillMonth(bill.getBillMonth());
        item.setAmount(bill.getTotalAmount());
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setPaidAmount(bill.getTotalAmount());
        item.setUnpaidAmount(BigDecimal.ZERO);
        payOrderItemMapper.insert(item);

        log.info("单条水电费缴费写入缴费单成功：payOrderId={}, amount={}, merchantId={}",
                payOrder.getId(), bill.getTotalAmount(), merchantId);
    }

    // -------------------------------------------------------------------------
    // 公共方法
    // -------------------------------------------------------------------------

    /** 幂等检查 */
    private void checkIdempotent(Long companyId, String requestId) {
        Long count = payRecordMapper.selectCount(
                new LambdaQueryWrapper<WaterElecPayRecord>()
                        .eq(WaterElecPayRecord::getCompanyId, companyId)
                        .eq(WaterElecPayRecord::getRequestId, requestId));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }
    }

    /**
     * 构建缴费记录（含铺位快照）
     * 物业费快照从 PropertyFeeBill 取，水电费快照从 WaterElecBill 取
     */
    private WaterElecPayRecord buildPayRecord(Long companyId, Object bill, UnifiedPayDTO dto,
                                               LoginUser loginUser, String bizType) {
        WaterElecPayRecord record = new WaterElecPayRecord();
        record.setCompanyId(companyId);
        record.setRequestId(dto.getRequestId());
        record.setPayType(dto.getPayType());
        record.setRecordType(CommonConst.PAY_RECORD_TYPE_PAY);
        record.setRefundStatus(CommonConst.REFUND_STATUS_NONE);
        record.setRemark(dto.getRemark());
        record.setCreateBy(loginUser.getUserId());

        if (CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(bizType)) {
            PropertyFeeBill feeBill = (PropertyFeeBill) bill;
            record.setBillId(feeBill.getId());
            record.setStallId(feeBill.getStallId());
            record.setMerchantId(feeBill.getMerchantId());
            record.setPayAmount(feeBill.getAmount());
            fillStallSnapshots(record, feeBill.getStallId(), feeBill.getMerchantId());
        } else if (CommonConst.BIZ_TYPE_WATER_ELEC.equals(bizType)) {
            WaterElecBill waterBill = (WaterElecBill) bill;
            record.setBillId(waterBill.getId());
            record.setStallId(waterBill.getStallId());
            record.setMerchantId(waterBill.getMerchantId());
            record.setPayAmount(waterBill.getTotalAmount());
            fillStallSnapshots(record, waterBill.getStallId(), waterBill.getMerchantId());
        }
        return record;
    }

    /** 查询并写入铺位/市场/分类/商户快照字段 */
    private void fillStallSnapshots(WaterElecPayRecord record, Long stallId, Long merchantId) {
        if (stallId != null) {
            StallInfo stall = stallInfoMapper.selectById(stallId);
            record.setStallNumber(stall == null ? null : stall.getStallNumber());
            record.setStallName(stall == null ? null : stall.getStallName());
            record.setStallMarketName(stall == null ? null : getMarketNameById(stall.getMarketId()));
            record.setCategoryName(stall == null || stall.getStallCategoryId() == null ? null
                    : getCategoryNameById(stall.getStallCategoryId()));
        }
        if (merchantId != null) {
            StallTenant tenant = stallTenantMapper.selectById(merchantId);
            record.setMerchantName(tenant == null ? null : tenant.getTenantName());
        }
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

    /** 构建财务流水记录 */
    private BizFinanceFlow insertFinanceFlow(Long companyId, String bizType, Long sourceBillId,
                                              Long stallId, WaterElecPayRecord record, BigDecimal amount) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setBusinessType(bizType);
        flow.setBillId(String.valueOf(sourceBillId));
        flow.setStallId(stallId);
        if (record != null) {
            flow.setStallNumber(record.getStallNumber());
            flow.setStallName(record.getStallName());
            flow.setStallMarketName(record.getStallMarketName());
            flow.setCategoryName(record.getCategoryName());
            flow.setMerchantName(record.getMerchantName());
        }
        flow.setOriginalAmount(amount);
        flow.setDiscountAmount(BigDecimal.ZERO);
        flow.setRealAmount(amount);
        flow.setPayType(record.getPayType());
        flow.setFlowType(CommonConst.FLOW_TYPE_INCOME);
        flow.setStatus(1);
        flow.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_NORMAL);
        flow.setRemark("统一缴费");
        flow.setCreateBy(UserContext.getUserIdOrZero());
        financeFlowMapper.insert(flow);
        return flow;
    }

    /** 更新缴费记录的流水单号 */
    private WaterElecPayRecord buildFlowUpdate(Long recordId, String flowNo) {
        WaterElecPayRecord update = new WaterElecPayRecord();
        update.setId(recordId);
        update.setFlowNo(flowNo);
        return update;
    }

    /**
     * 缴费成功后激活合同：签约中 -> 生效中
     * 根据 stallId 查询签约中状态的合同并激活
     */
    private void activateContractIfSigned(Long stallId) {
        if (stallId == null) return;
        StallContract signedContract = contractMapper.selectOne(
                new LambdaQueryWrapper<StallContract>()
                        .eq(StallContract::getStallId, stallId)
                        .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_SIGNING)
                        .last("LIMIT 1"));
        if (signedContract != null) {
            leaseContractService.activateContract(signedContract.getId());
            log.info("合同激活成功：stallId={}, contractId={}", stallId, signedContract.getId());
        }
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
        return CommonConst.BIZ_TYPE_PROPERTY_FEE.equals(bizType) ? "物业费" : "水电费";
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
        // 查询该铺位生效中的合约，取租户ID作为商户ID
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
}
