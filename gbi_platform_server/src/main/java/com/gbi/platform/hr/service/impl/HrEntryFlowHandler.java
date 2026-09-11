package com.gbi.platform.hr.service.impl;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.hr.service.HrTransferService;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 入职申请审批回调处理器（hr_entry）
 * 审批通过 → 自动创建员工档案
 * 审批驳回 → 申请置已驳回
 * 撤回/终止 → 申请已处理，无需额外操作
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrEntryFlowHandler implements FlowBizHandler {

    private final HrTransferService transferService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_HR_ENTRY;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("HrEntryFlowHandler.onPass: entryApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onEntryApproved(sourceId);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("HrEntryFlowHandler.onReject: entryApplyId={}, instanceId={}", sourceId, instanceId);
        transferService.onEntryRejected(sourceId);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("HrEntryFlowHandler.onCancel: entryApplyId={}, instanceId={}", sourceId, instanceId);
    }
}