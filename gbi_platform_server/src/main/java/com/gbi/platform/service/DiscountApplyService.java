package com.gbi.platform.service;

import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.PageVO;

import java.math.BigDecimal;

/**
 * 优惠申请服务（租赁合同高频使用，报销/采购无优惠策略仅金额阈值校验）
 * 流程：合同保存计算优惠 → 集团阈值判定 → 超阈值发起 contract_discount 审批 → 通过后合同生效并生成收款计划
 *
 * @author gbi
 */
public interface DiscountApplyService {

    /**
     * 合同优惠申请：构建申请单 + 快照 + 阈值判定
     * need_audit=1 时自动发起 contract_discount 审批（状态=审批中），否则直接通过并生成收款计划
     *
     * @return 优惠申请ID
     */
    Long createForContract(Long contractId, Long policyId, Integer waiveMonths, BigDecimal discountRate,
                           BigDecimal deductAmount, String remark);

    PageVO<DiscountApplyVO> page(DiscountApplyQueryDTO dto);

    DiscountApplyVO detail(Long id);

    /** 撤销申请（仅草稿/审批中） */
    void cancel(Long id);

    /**
     * 审批结果回调（ContractDiscountFlowHandler 调用）
     * result = 2 通过（生成收款计划）/ 3 驳回 / 4 作废
     */
    void handleFlowResult(Long flowInstanceId, int result);
}