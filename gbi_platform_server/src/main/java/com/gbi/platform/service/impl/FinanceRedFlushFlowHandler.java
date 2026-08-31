package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 财务流水冲红审批回调
 * 审批通过 → 激活反向流水；驳回 → 恢复原流水正常状态
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceRedFlushFlowHandler implements FlowBizHandler {

    private final FinanceService financeService;
    private final BizFinanceFlowMapper financeFlowMapper;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_FINANCE_RED_FLUSH;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("冲红审批通过，sourceId={}, instanceId={}", sourceId, instanceId);
        BizFinanceFlow flow = financeFlowMapper.selectById(sourceId);
        if (flow == null) {
            log.error("冲红审批通过回调：原流水不存在，sourceId={}", sourceId);
            return;
        }
        Long redFlushFlowId = flow.getRedFlushFlowId();
        // 若 redFlushFlowId 为空（历史数据），按 flow_no 后缀 -RED 查找反向流水
        if (redFlushFlowId == null) {
            BizFinanceFlow reverse = financeFlowMapper.selectOne(
                    new LambdaQueryWrapper<BizFinanceFlow>()
                            .eq(BizFinanceFlow::getFlowNo, flow.getFlowNo() + "-RED")
                            .eq(BizFinanceFlow::getCompanyId, flow.getCompanyId()));
            if (reverse != null) {
                redFlushFlowId = reverse.getId();
                log.info("通过 flow_no 查找反向流水：flowNo={}, reverseId={}", flow.getFlowNo() + "-RED", redFlushFlowId);
            }
        }
        if (redFlushFlowId == null) {
            log.error("冲红审批通过回调：反向流水ID为空，sourceId={}", sourceId);
            return;
        }
        financeService.approveRedFlush(sourceId, redFlushFlowId);
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("冲红审批驳回，sourceId={}, instanceId={}", sourceId, instanceId);
        financeService.rejectRedFlush(sourceId);
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("冲红审批撤回，sourceId={}, instanceId={}", sourceId, instanceId);
        financeService.rejectRedFlush(sourceId);
    }
}