package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 租赁合同大额优惠审批回调（contract_discount）
 * 审批通过 → 优惠申请置通过并生成收款计划；驳回 → 申请驳回；撤回/终止 → 申请作废
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDiscountFlowHandler implements FlowBizHandler {

    private final DiscountApplyService discountApplyService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_CONTRACT_DISCOUNT;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        discountApplyService.handleFlowResult(instanceId, CommonConst.APPLY_STATUS_PASS);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        discountApplyService.handleFlowResult(instanceId, CommonConst.APPLY_STATUS_REJECT);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        discountApplyService.handleFlowResult(instanceId, CommonConst.APPLY_STATUS_VOID);
    }
}