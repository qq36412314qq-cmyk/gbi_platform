package com.gbi.platform.service.impl.hr;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.hr.HrTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 转正申请审批回调处理器（hr_regular）
 * 审批通过 → 更新员工状态为正式，设置转正日期
 * 审批驳回 → 申请置已驳回
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrRegularFlowHandler implements FlowBizHandler {

    private final HrTransferService transferService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_HR_REGULAR;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("HrRegularFlowHandler.onPass: regularApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onRegularApproved(sourceId);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("HrRegularFlowHandler.onReject: regularApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onRegularRejected(sourceId);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("HrRegularFlowHandler.onCancel: regularApplyId={}, instanceId={}", sourceId, instanceId);
    }
}
