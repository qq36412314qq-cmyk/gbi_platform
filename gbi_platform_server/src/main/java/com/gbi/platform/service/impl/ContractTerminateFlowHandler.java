package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.RecvPayPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 合同作废/终止审批回调（contract_terminate）
 * 审批通过后执行红冲链（旧计划作废 + 反向冲销计划 + 负向核销 + 反向资金流水）
 * 一期合同退租为即时执行，本处理器为二期接入审批流预留
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractTerminateFlowHandler implements FlowBizHandler {

    private final RecvPayPlanService recvPayPlanService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_CONTRACT_TERMINATE;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        recvPayPlanService.redChainForContract(sourceId);
    }
}