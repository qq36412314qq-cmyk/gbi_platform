package com.gbi.platform.service;

import com.gbi.platform.dto.PlanAdjustDTO;
import com.gbi.platform.dto.PlanTerminateDTO;
import com.gbi.platform.dto.RecvPayPlanQueryDTO;
import com.gbi.platform.dto.ReconcileQueryDTO;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.PropertyFeeBill;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RecvPayPlanDetailVO;
import com.gbi.platform.vo.RecvPayPlanVO;
import com.gbi.platform.vo.ReconcileDiffVO;

import java.math.BigDecimal;

/**
 * 应收应付计划服务（全系统唯一应收应付台账）
 *
 * @author gbi
 */
public interface RecvPayPlanService {

    /** 计划分页（按方向/类型/状态/铺位/期次/红冲筛选） */
    PageVO<RecvPayPlanVO> page(RecvPayPlanQueryDTO dto);

    /** 计划详情（含关联账单 bill_plan_rel、核销分摊） */
    RecvPayPlanDetailVO detail(Long planId);

    /** 按合同生成收款计划（幂等） */
    void generateByContract(Long contractId);

    /** 人工调账 */
    void adjust(PlanAdjustDTO dto);

    /** 计划作废/终止 */
    void terminatePlan(PlanTerminateDTO dto);

    /** 审批通过后执行调账 */
    void applyApprovedAdjust(Long planId, BigDecimal adjustAmount, String remark);

    /** 审批通过后执行作废 */
    void applyApprovedVoid(Long planId, String remark);

    /** 合同退租红冲链 */
    void redChainForContract(Long contractId);

    /** 收付款核销 */
    boolean writeOff(Long planId, Long financeFlowId, BigDecimal amount, String billType, Long billId,
                     int writeoffType, String remark);

    /** 自动对账引擎 */
    PageVO<ReconcileDiffVO> reconcile(ReconcileQueryDTO dto);

    /** 计划导出 CSV */
    String export(RecvPayPlanQueryDTO dto);

    /** 按水电账单生成应收计划 */
    Long generatePlanForBill(WaterElecBill bill);

    /** 按物业费账单生成应收计划 */
    Long generatePlanForPropertyBill(PropertyFeeBill bill);

    /** 按统一账单生成应收计划 */
    Long generatePlanForBizFeeBill(BizFeeBill bill);

    /** 根据账单ID核销应收应付计划 */
    void writeOffByBillId(Long companyId, Long billId, BizFinanceFlow flow, int writeoffType, String remark);
}
