package com.gbi.platform.service.impl.hr;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.hr.HrTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 调岗申请审批回调处理器（hr_transfer）
 * 审批通过 → 更新员工组织和岗位
 * 审批驳回 → 申请置已驳回
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrTransferFlowHandler implements FlowBizHandler {

    private final HrTransferService transferService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_HR_TRANSFER;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("HrTransferFlowHandler.onPass: transferApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onTransferApproved(sourceId);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("HrTransferFlowHandler.onReject: transferApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onTransferRejected(sourceId);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("HrTransferFlowHandler.onCancel: transferApplyId={}, instanceId={}", sourceId, instanceId);
    }
}
