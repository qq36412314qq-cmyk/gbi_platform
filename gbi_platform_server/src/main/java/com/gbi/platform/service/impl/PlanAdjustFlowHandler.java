package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.vo.FlowInstanceDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 计划大额调账/作废审批回调（plan_adjust）
 * 审批标题编码约定：ADJUST#planId#amount#remark 或 VOID#planId#remark
 * 审批通过后按标题指令执行调账/作废（超阈值场景的兜底执行入口）
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanAdjustFlowHandler implements FlowBizHandler {

    private static final String PREFIX_ADJUST = "ADJUST#";
    private static final String PREFIX_VOID = "VOID#";

    private final RecvPayPlanService recvPayPlanService;
    private final FlowEngineService flowEngineService;

    @Override
    public String bizType() {
        return CommonConst.FLOW_DEF_PLAN_ADJUST;
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        if (sourceId == null) {
            return;
        }
        FlowInstanceDetailVO detail = flowEngineService.instanceDetail(instanceId);
        String title = detail == null || detail.getInstance() == null ? null : detail.getInstance().getTitle();
        if (!StringUtils.hasText(title)) {
            log.warn("计划审批通过但标题为空，跳过执行 planId={}", sourceId);
            return;
        }
        if (title.startsWith(PREFIX_ADJUST)) {
            String[] parts = title.split("#", 4);
            if (parts.length >= 3) {
                recvPayPlanService.applyApprovedAdjust(Long.valueOf(parts[1]),
                        new BigDecimal(parts[2]), parts.length >= 4 ? parts[3] : null);
            }
        } else if (title.startsWith(PREFIX_VOID)) {
            String[] parts = title.split("#", 3);
            recvPayPlanService.applyApprovedVoid(Long.valueOf(parts[1]), parts.length >= 3 ? parts[2] : null);
        } else {
            throw new BizException("计划审批标题指令无法识别：" + title);
        }
    }
}