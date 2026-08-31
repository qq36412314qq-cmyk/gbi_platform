package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 计划详情视图（含关联账单 bill_plan_rel、核销分摊、审批轨迹）
 *
 * @author gbi
 */
@Data
public class RecvPayPlanDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private RecvPayPlanVO plan;

    /** 关联账单-计划分摊明细 */
    private List<BillPlanRelVO> billPlanRels;

    /** 核销分摊明细 */
    private List<WriteoffVO> writeoffs;
}