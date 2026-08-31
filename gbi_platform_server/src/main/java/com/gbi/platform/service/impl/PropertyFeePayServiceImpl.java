package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.PropertyFeePayDTO;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.entity.PropertyFeeBill;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.entity.WaterElecPayRecord;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.mapper.PropertyFeeBillMapper;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.mapper.WaterElecPayRecordMapper;
import com.gbi.platform.service.PropertyFeePayService;
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
import java.util.List;

/**
 * 物业费缴费服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyFeePayServiceImpl implements PropertyFeePayService {

    private final PropertyFeeBillMapper billMapper;
    private final BizFeeBillMapper bizFeeBillMapper;
    private final WaterElecPayRecordMapper payRecordMapper;
    private final BizFinanceFlowMapper financeFlowMapper;
    private final AuditLogUtil auditLogUtil;
    private final RecvPayPlanService recvPayPlanService;
    private final FlowNoGenerator flowNoGenerator;
    private final StallInfoMapper stallInfoMapper;
    private final StallTenantMapper stallTenantMapper;
    private final StallCategoryMapper categoryMapper;
    private final MarketInfoMapper marketInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(PropertyFeePayDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        checkIdempotent(companyId, dto.getRequestId());

        // 前端传入的是 biz_fee_bill.id，通过 sourceBillId 解析原始账单ID
        BizFeeBill bizBill = bizFeeBillMapper.selectById(dto.getBillId());
        if (bizBill == null) {
            throw new BizException("账单不存在或已删除");
        }
        Long sourceBillId = bizBill.getSourceBillId();
        if (sourceBillId == null) {
            throw new BizException("账单源账单ID不存在");
        }
        PropertyFeeBill bill = billMapper.selectById(sourceBillId);
        if (bill == null) {
            throw new BizException("原始账单不存在或已删除");
        }
        if (!companyId.equals(bill.getCompanyId())) {
            throw new BizException("无权操作该账单");
        }
        if (bill.getPayStatus() == CommonConst.BILL_PAY_STATUS_PAID) {
            throw new BizException("账单已缴清，无需重复缴费");
        }

        WaterElecPayRecord record = buildPayRecord(companyId, bill, dto, loginUser);

        try {
            payRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }

        PropertyFeeBill billUpdate = new PropertyFeeBill();
        billUpdate.setId(sourceBillId);
        billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        billUpdate.setPayTime(LocalDateTime.now());
        billMapper.updateById(billUpdate);

        BizFinanceFlow flow = insertFinanceFlow(companyId, bill, record, dto.getPayType());

        WaterElecPayRecord flowUpdate = new WaterElecPayRecord();
        flowUpdate.setId(record.getId());
        flowUpdate.setFlowNo(flow.getFlowNo());
        payRecordMapper.updateById(flowUpdate);

        recvPayPlanService.writeOffByBillId(companyId, sourceBillId, flow, CommonConst.WRITEOFF_TYPE_PAY, "物业费缴费核销");

        auditLogUtil.record(CommonConst.MODULE_PROPERTY_FEE, CommonConst.OPER_TYPE_PAY,
                String.valueOf(record.getId()), null, record);

        log.info("物业费缴费成功：billId={}, recordId={}, amount={}", dto.getBillId(), record.getId(), bill.getAmount());
        // 同步更新 biz_fee_bill 缴费状态
        BizFeeBill paySync = new BizFeeBill();
        paySync.setId(bizBill.getId());
        paySync.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        paySync.setPayTime(LocalDateTime.now());
        bizFeeBillMapper.updateById(paySync);
    }

    private WaterElecPayRecord buildPayRecord(Long companyId, PropertyFeeBill bill,
                                               PropertyFeePayDTO dto, LoginUser loginUser) {
        WaterElecPayRecord record = new WaterElecPayRecord();
        record.setCompanyId(companyId);
        record.setBillId(bill.getId());
        record.setStallId(bill.getStallId());
        record.setMerchantId(bill.getMerchantId());
        record.setPayAmount(bill.getAmount());
        record.setPayType(dto.getPayType());
        record.setRequestId(dto.getRequestId());
        record.setRecordType(CommonConst.PAY_RECORD_TYPE_PAY);
        record.setRefundStatus(CommonConst.REFUND_STATUS_NONE);
        record.setRemark(dto.getRemark());
        record.setCreateBy(loginUser.getUserId());

        StallInfo stall = bill.getStallId() != null ? stallInfoMapper.selectById(bill.getStallId()) : null;
        record.setStallNumber(stall == null ? null : stall.getStallNumber());
        record.setStallName(stall == null ? null : stall.getStallName());
        record.setStallMarketName(stall == null ? null : getMarketNameById(stall.getMarketId()));
        record.setCategoryName(stall == null || stall.getStallCategoryId() == null ? null
                : getCategoryNameById(stall.getStallCategoryId()));
        record.setMerchantName(bill.getMerchantId() != null ? getTenantNameById(bill.getMerchantId()) : null);
        return record;
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

    private String getTenantNameById(Long tenantId) {
        if (tenantId == null) return null;
        StallTenant t = stallTenantMapper.selectById(tenantId);
        return t != null ? t.getTenantName() : null;
    }

    private void checkIdempotent(Long companyId, String requestId) {
        Long count = payRecordMapper.selectCount(
                new LambdaQueryWrapper<WaterElecPayRecord>()
                        .eq(WaterElecPayRecord::getCompanyId, companyId)
                        .eq(WaterElecPayRecord::getRequestId, requestId));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }
    }

    private BizFinanceFlow insertFinanceFlow(Long companyId, PropertyFeeBill bill, WaterElecPayRecord record, Integer payType) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setBusinessType(CommonConst.BIZ_TYPE_PROPERTY_FEE);
        flow.setBillId(String.valueOf(bill.getId()));
        flow.setStallId(bill.getStallId());
        if (record != null) {
            flow.setStallNumber(record.getStallNumber());
            flow.setStallName(record.getStallName());
            flow.setStallMarketName(record.getStallMarketName());
            flow.setCategoryName(record.getCategoryName());
            flow.setMerchantName(record.getMerchantName());
        }
        flow.setOriginalAmount(bill.getAmount());
        flow.setDiscountAmount(BigDecimal.ZERO);
        flow.setRealAmount(bill.getAmount());
        flow.setPayType(payType);
        flow.setFlowType(CommonConst.FLOW_TYPE_INCOME);
        flow.setStatus(1);
        flow.setFlowStatus(CommonConst.FLOW_STATUS_NORMAL);
        flow.setRemark("物业费缴费");
        flow.setCreateBy(UserContext.getUserIdOrZero());
        financeFlowMapper.insert(flow);
        return flow;
    }
}