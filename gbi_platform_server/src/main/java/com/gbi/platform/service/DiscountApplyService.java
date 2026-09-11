package com.gbi.platform.service;

import com.gbi.platform.dto.DiscountApplyDTO;
import com.gbi.platform.dto.DiscountApplyQueryDTO;
import com.gbi.platform.vo.DiscountApplyVO;
import com.gbi.platform.vo.DiscountCalcVO;
import com.gbi.platform.vo.PageVO;

import java.math.BigDecimal;

/**
 * 优惠申请服务（支持多业务类型：租赁/物业/水电/幼儿园）
 * 流程：提交申请 → 阈值判定 → 超阈值发起审批 → 通过后生成应收应付计划
 *
 * @author gbi
 */
public interface DiscountApplyService {

    /**
     * 合同优惠申请（兼容现有逻辑）
     */
    Long createForContract(Long contractId, Long policyId, Integer waiveMonths, BigDecimal discountRate,
                           BigDecimal deductAmount, String remark);

    /**
     * 通用优惠申请（支持多业务类型）
     */
    Long createApply(DiscountApplyDTO dto);

    PageVO<DiscountApplyVO> page(DiscountApplyQueryDTO dto);

    DiscountApplyVO detail(Long id);

    /** 撤销申请（仅草稿/审批中） */
    void cancel(Long id);

    /**
     * 审批结果回调
     */
    void handleFlowResult(Long flowInstanceId, int result);

    /**
     * 预览计算优惠金额
     */
    DiscountCalcVO calcPreview(DiscountApplyDTO dto);
}