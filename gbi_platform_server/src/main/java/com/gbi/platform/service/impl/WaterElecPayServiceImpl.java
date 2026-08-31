package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.WaterElecPayDTO;
import com.gbi.platform.dto.WaterElecPayQueryDTO;
import com.gbi.platform.dto.WaterElecRefundDTO;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.entity.WaterElecPayRecord;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.WaterElecBillMapper;
import com.gbi.platform.mapper.WaterElecPayRecordMapper;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.service.UserService;
import com.gbi.platform.service.WaterElecPayService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FlowNoGenerator;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecPayRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterElecPayServiceImpl implements WaterElecPayService {

    private final WaterElecPayRecordMapper payRecordMapper;
    private final WaterElecBillMapper billMapper;
    private final BizFinanceFlowMapper financeFlowMapper;
    private final UserService userService;
    private final AuditLogUtil auditLogUtil;
    private final RecvPayPlanService recvPayPlanService;
    private final FlowNoGenerator flowNoGenerator;
    private final StallInfoMapper stallInfoMapper;
    private final StallTenantMapper stallTenantMapper;
    private final StallCategoryMapper categoryMapper;
    private final MarketInfoMapper marketInfoMapper;
    private final BizFeeBillMapper bizFeeBillMapper;

    @Override
    public PageVO<WaterElecPayRecordVO> page(WaterElecPayQueryDTO dto) {
        Page<WaterElecPayRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<WaterElecPayRecord> wrapper = new LambdaQueryWrapper<WaterElecPayRecord>()
                .eq(dto.getRecordType() != null, WaterElecPayRecord::getRecordType, dto.getRecordType())
                .eq(dto.getPayType() != null, WaterElecPayRecord::getPayType, dto.getPayType())
                .eq(dto.getBillId() != null, WaterElecPayRecord::getBillId, dto.getBillId())
                .orderByDesc(WaterElecPayRecord::getId);
        Page<WaterElecPayRecord> result = payRecordMapper.selectPage(page, wrapper);

        List<Long> userIds = result.getRecords().stream().map(WaterElecPayRecord::getCreateBy).distinct().toList();
        Map<Long, String> nameMap = userIds.isEmpty() ? Collections.emptyMap()
                : userService.mapRealNameByIds(userIds);

        List<WaterElecPayRecordVO> voList = result.getRecords().stream().map(r -> {
            WaterElecPayRecordVO vo = new WaterElecPayRecordVO();
            vo.setId(r.getId());
            vo.setCompanyId(r.getCompanyId());
            vo.setBillId(r.getBillId());
            vo.setStallId(r.getStallId());
            vo.setStallNumber(r.getStallNumber());
            vo.setStallName(r.getStallName());
            vo.setStallMarketName(r.getStallMarketName());
            vo.setCategoryName(r.getCategoryName());
            vo.setMerchantName(r.getMerchantName());
            vo.setMerchantId(r.getMerchantId());
            vo.setPayAmount(r.getPayAmount());
            vo.setPayType(r.getPayType());
            vo.setPayTypeText(payTypeText(r.getPayType()));
            vo.setRecordType(r.getRecordType());
            vo.setRecordTypeText(r.getRecordType() == null ? null
                    : (r.getRecordType() == CommonConst.PAY_RECORD_TYPE_PAY ? "缴费" : "退费"));
            vo.setRefundStatus(r.getRefundStatus());
            vo.setRefundTime(r.getRefundTime());
            vo.setRefundRecordId(r.getRefundRecordId());
            vo.setRemark(r.getRemark());
            vo.setFlowNo(r.getFlowNo());
            vo.setCreateByName(nameMap.getOrDefault(r.getCreateBy(), "用户" + r.getCreateBy()));
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(WaterElecPayDTO dto) {
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
        WaterElecBill bill = billMapper.selectById(sourceBillId);
        if (bill == null) throw new BizException("原始账单不存在或已删除");
        if (bill.getPayStatus() == CommonConst.BILL_PAY_STATUS_PAID) throw new BizException("账单已缴清，无需重复缴费");

        WaterElecPayRecord record = buildPayRecord(companyId, bill, dto, loginUser);
        try {
            payRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }

        WaterElecBill billUpdate = new WaterElecBill();
        billUpdate.setId(sourceBillId);
        billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
        billUpdate.setPayTime(LocalDateTime.now());
        billMapper.updateById(billUpdate);

        BizFinanceFlow flow = insertFinanceFlow(companyId, bill, bill.getTotalAmount(), dto.getPayType(),
                CommonConst.FLOW_TYPE_INCOME, "水电物业缴费", record);

        WaterElecPayRecord flowUpdate = new WaterElecPayRecord();
        flowUpdate.setId(record.getId());
        flowUpdate.setFlowNo(flow.getFlowNo());
        payRecordMapper.updateById(flowUpdate);

        writeOffPlan(bill, flow, CommonConst.WRITEOFF_TYPE_PAY, "缴费核销");
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_PAY,
                String.valueOf(record.getId()), null, record);

        // 同步更新 biz_fee_bill 缴费状态（使未支付订单列表正确刷新）
        Long bizBillId = bizBill.getId();
        BizFeeBill syncBill = bizFeeBillMapper.selectOne(
                new LambdaQueryWrapper<BizFeeBill>()
                        .eq(BizFeeBill::getSourceBillId, sourceBillId)
                        .eq(BizFeeBill::getSourceBillId, sourceBillId));
        if (syncBill != null) {
            BizFeeBill update = new BizFeeBill();
            update.setId(syncBill.getId());
            update.setPayStatus(CommonConst.BILL_PAY_STATUS_PAID);
            update.setPayTime(LocalDateTime.now());
            bizFeeBillMapper.updateById(update);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(WaterElecRefundDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        checkIdempotent(companyId, dto.getRequestId());

        WaterElecPayRecord original = payRecordMapper.selectById(dto.getPayRecordId());
        if (original == null) throw new BizException("缴费记录不存在");
        if (!companyId.equals(original.getCompanyId())) throw new BizException("无权操作该记录");
        if (original.getRefundStatus() == CommonConst.REFUND_STATUS_DONE) throw new BizException("该记录已退费，不可重复操作");
        if (original.getRecordType() != CommonConst.PAY_RECORD_TYPE_PAY) throw new BizException("只能对缴费记录进行退费");

        WaterElecPayRecord originalUpdate = new WaterElecPayRecord();
        originalUpdate.setId(original.getId());
        originalUpdate.setRefundStatus(CommonConst.REFUND_STATUS_DONE);
        originalUpdate.setRefundTime(LocalDateTime.now());
        payRecordMapper.updateById(originalUpdate);

        WaterElecPayRecord refundRecord = buildRefundRecord(companyId, original, dto, loginUser);
        try {
            payRecordMapper.insert(refundRecord);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }

        WaterElecBill billUpdate = new WaterElecBill();
        billUpdate.setId(original.getBillId());
        billUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        billUpdate.setPayTime(null);
        billMapper.updateById(billUpdate);

        WaterElecBill bill = billMapper.selectById(original.getBillId());
        BizFinanceFlow flow = insertFinanceFlow(companyId, bill, original.getPayAmount(), original.getPayType(),
                CommonConst.FLOW_TYPE_EXPENSE, "水电物业退费", refundRecord);
        writeOffPlan(bill, flow, CommonConst.WRITEOFF_TYPE_REFUND, "退款冲减");

        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_REFUND,
                String.valueOf(refundRecord.getId()), original, refundRecord);

        // 同步恢复 biz_fee_bill 缴费状态（退费后恢复待缴）
        Long refSourceBillId = original.getBillId();
        BizFeeBill refSyncBill = bizFeeBillMapper.selectOne(
                new LambdaQueryWrapper<BizFeeBill>()
                        .eq(BizFeeBill::getSourceBillId, refSourceBillId)
                        .eq(BizFeeBill::getCompanyId, companyId));
        if (refSyncBill != null) {
            BizFeeBill refUpdate = new BizFeeBill();
            refUpdate.setId(refSyncBill.getId());
            refUpdate.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
            refUpdate.setPayTime(null);
            bizFeeBillMapper.updateById(refUpdate);
        }
    }

    private WaterElecPayRecord buildPayRecord(Long companyId, WaterElecBill bill,
                                              WaterElecPayDTO dto, LoginUser loginUser) {
        WaterElecPayRecord record = new WaterElecPayRecord();
        record.setCompanyId(companyId);
        record.setBillId(bill.getId());
        record.setStallId(bill.getStallId());
        record.setMerchantId(bill.getMerchantId());
        record.setPayAmount(bill.getTotalAmount());
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
                : getCatNameById(stall.getStallCategoryId()));
        record.setMerchantName(bill.getMerchantId() != null ? getTenantNameById(bill.getMerchantId()) : null);
        return record;
    }

    private WaterElecPayRecord buildRefundRecord(Long companyId, WaterElecPayRecord original,
                                                  WaterElecRefundDTO dto, LoginUser loginUser) {
        WaterElecPayRecord record = new WaterElecPayRecord();
        record.setCompanyId(companyId);
        record.setBillId(original.getBillId());
        record.setStallId(original.getStallId());
        record.setMerchantId(original.getMerchantId());
        record.setStallNumber(original.getStallNumber());
        record.setStallName(original.getStallName());
        record.setStallMarketName(original.getStallMarketName());
        record.setCategoryName(original.getCategoryName());
        record.setMerchantName(original.getMerchantName());
        record.setPayAmount(original.getPayAmount());
        record.setPayType(original.getPayType());
        record.setRequestId(dto.getRequestId());
        record.setRecordType(CommonConst.PAY_RECORD_TYPE_REFUND);
        record.setRefundStatus(CommonConst.REFUND_STATUS_DONE);
        record.setRefundTime(LocalDateTime.now());
        record.setRefundRecordId(original.getId());
        record.setRemark(dto.getRemark());
        record.setCreateBy(loginUser.getUserId());
        return record;
    }

    private BizFinanceFlow insertFinanceFlow(Long companyId, WaterElecBill bill, BigDecimal amount,
                                             Integer payType, Integer flowType, String remark,
                                             WaterElecPayRecord record) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setBusinessType(CommonConst.BIZ_TYPE_WATER_ELEC);
        flow.setBillId(bill == null ? null : String.valueOf(bill.getId()));
        flow.setMerchantId(bill == null ? null : bill.getMerchantId());
        flow.setStallId(bill == null ? null : bill.getStallId());
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
        flow.setPayType(payType);
        flow.setFlowType(flowType);
        flow.setStatus(1);
        flow.setFlowStatus(CommonConst.FLOW_STATUS_NORMAL);
        flow.setTradeNo(null);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setRemark(remark);
        financeFlowMapper.insert(flow);
        return flow;
    }

    private void writeOffPlan(WaterElecBill bill, BizFinanceFlow flow, Integer writeoffType, String remark) {
        if (bill == null || bill.getPlanId() == null) return;
        BigDecimal amount = flow.getFlowType() == CommonConst.FLOW_TYPE_EXPENSE
                ? flow.getRealAmount().negate() : flow.getRealAmount();
        recvPayPlanService.writeOff(bill.getPlanId(), flow.getId(), amount,
                "water_elec", bill.getId(), writeoffType, remark);
    }

    private void checkIdempotent(Long companyId, String requestId) {
        Long count = payRecordMapper.selectCount(new LambdaQueryWrapper<WaterElecPayRecord>()
                .eq(WaterElecPayRecord::getCompanyId, companyId)
                .eq(WaterElecPayRecord::getRequestId, requestId));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.IDEMPOTENT_REPEAT.getCode(), ResultCode.IDEMPOTENT_REPEAT.getMsg());
        }
    }

    private String payTypeText(Integer payType) {
        if (payType == null) return null;
        return switch (payType) {
            case CommonConst.PAY_TYPE_WECHAT -> "微信";
            case CommonConst.PAY_TYPE_ALIPAY -> "支付宝";
            case CommonConst.PAY_TYPE_CASH -> "线下现金";
            default -> "未知";
        };
    }

    private String getCatNameById(Long categoryId) {
        if (categoryId == null) return null;
        StallCategory cat = categoryMapper.selectById(categoryId);
        return cat != null ? cat.getCategoryName() : null;
    }

    private String getMarketNameById(Long marketId) {
        if (marketId == null) return null;
        MarketInfo m = marketInfoMapper.selectById(marketId);
        return m != null ? m.getMarketName() : null;
    }

    private String getTenantNameById(Long tenantId) {
        if (tenantId == null) return null;
        StallTenant t = stallTenantMapper.selectById(tenantId);
        return t != null ? t.getTenantName() : null;
    }
}
