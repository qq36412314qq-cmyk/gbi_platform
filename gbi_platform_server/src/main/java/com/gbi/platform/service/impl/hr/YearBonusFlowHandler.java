package com.gbi.platform.service.impl.hr;

import com.gbi.platform.entity.hr.HrYearBonus;
import com.gbi.platform.mapper.hr.HrYearBonusMapper;
import com.gbi.platform.service.FlowBizHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 年终奖审批回调处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YearBonusFlowHandler implements FlowBizHandler {

    private final HrYearBonusMapper yearBonusMapper;

    @Override
    public String bizType() {
        return "hr_year_bonus";
    }

    @Override
    public void onPass(Long sourceId, Long instanceId) {
        log.info("YearBonusFlowHandler.onPass: bonusId={}, instanceId={}", sourceId, instanceId);
        HrYearBonus bonus = yearBonusMapper.selectById(sourceId);
        if (bonus != null) {
            bonus.setApplyStatus(1);
            bonus.setFlowInstanceId(null);
            yearBonusMapper.updateById(bonus);
        }
    }

    @Override
    public void onReject(Long sourceId, Long instanceId) {
        log.info("YearBonusFlowHandler.onReject: bonusId={}, instanceId={}", sourceId, instanceId);
        HrYearBonus bonus = yearBonusMapper.selectById(sourceId);
        if (bonus != null) {
            bonus.setApplyStatus(2);
            yearBonusMapper.updateById(bonus);
        }
    }

    @Override
    public void onCancel(Long sourceId, Long instanceId) {
        log.info("YearBonusFlowHandler.onCancel: bonusId={}, instanceId={}", sourceId, instanceId);
        HrYearBonus bonus = yearBonusMapper.selectById(sourceId);
        if (bonus != null) {
            bonus.setApplyStatus(3);
            yearBonusMapper.updateById(bonus);
        }
    }
}
