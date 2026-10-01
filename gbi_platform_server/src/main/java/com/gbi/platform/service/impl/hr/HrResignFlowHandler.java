package com.gbi.platform.service.impl.hr;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.hr.HrTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 离职申请审批回调处理器（hr_resign）
 * 审批通过 → 更新员工状态为离职，停用关联账号
 * 审批驳回 → 申请置已驳回
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrResignFlowHandler implements FlowBizHandler {

    private final HrTransferService transferService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_HR_RESIGN;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("HrResignFlowHandler.onPass: resignApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onResignApproved(sourceId);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("HrResignFlowHandler.onReject: resignApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onResignRejected(sourceId);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("HrResignFlowHandler.onCancel: resignApplyId={}, instanceId={}", sourceId, instanceId);
    }
}
