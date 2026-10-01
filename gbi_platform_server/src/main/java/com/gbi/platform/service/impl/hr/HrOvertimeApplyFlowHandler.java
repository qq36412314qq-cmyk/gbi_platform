package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gbi.platform.entity.hr.HrOvertimeApply;
import com.gbi.platform.mapper.hr.HrOvertimeApplyMapper;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 加班申请审批回调处理器
 * 审批通过时将 hr_overtime_apply.status 更新为 1（已通过）
 * 审批驳回时更新为 2（已驳回）
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrOvertimeApplyFlowHandler implements FlowBizHandler {

    private final HrOvertimeApplyMapper applyMapper;

    @Override
    public String bizType() {
        return "hr_overtime_apply";
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("HrOvertimeApplyFlowHandler.onPass: applyId={}, instanceId={}", sourceId, instanceId);
        applyMapper.update(null, new LambdaUpdateWrapper<HrOvertimeApply>()
                .eq(HrOvertimeApply::getId, sourceId)
                .set(HrOvertimeApply::getStatus, 1)); // 1=已通过
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("HrOvertimeApplyFlowHandler.onReject: applyId={}, instanceId={}", sourceId, instanceId);
        applyMapper.update(null, new LambdaUpdateWrapper<HrOvertimeApply>()
                .eq(HrOvertimeApply::getId, sourceId)
                .set(HrOvertimeApply::getStatus, 2)); // 2=已驳回
    }
}
