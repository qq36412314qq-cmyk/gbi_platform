package com.gbi.platform.service.impl.hr;

import com.gbi.platform.entity.hr.HrSalaryBatchAdjust;
import com.gbi.platform.mapper.hr.HrSalaryBatchAdjustMapper;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 批量调薪审批回调处理器
 * 审批通过后才可以执行批量调薪任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchAdjustFlowHandler implements FlowBizHandler {

    private final HrSalaryBatchAdjustMapper batchMapper;

    @Override
    public String bizType() {
        return "hr_salary_batch_adjust";
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("BatchAdjustFlowHandler.onPass: batchId={}, instanceId={}", sourceId, instanceId);
        HrSalaryBatchAdjust batch = batchMapper.selectById(sourceId);
        if (batch != null) {
            batch.setStatus(4); // 已审批通过，待执行
            batch.setFlowInstanceId(null);
            batchMapper.updateById(batch);
        }
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("BatchAdjustFlowHandler.onReject: batchId={}, instanceId={}", sourceId, instanceId);
        HrSalaryBatchAdjust batch = batchMapper.selectById(sourceId);
        if (batch != null) {
            batch.setStatus(3); // 已驳回
            batchMapper.updateById(batch);
        }
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("BatchAdjustFlowHandler.onCancel: batchId={}, instanceId={}", sourceId, instanceId);
        HrSalaryBatchAdjust batch = batchMapper.selectById(sourceId);
        if (batch != null) {
            batch.setStatus(5); // 已撤回
            batchMapper.updateById(batch);
        }
    }
}
