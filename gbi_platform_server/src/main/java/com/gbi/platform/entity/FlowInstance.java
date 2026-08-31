package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 统一审批流程实例表实体：flow_instance
 * 状态机：0审批中 1通过 2驳回 3撤回 4终止
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_instance")
public class FlowInstance extends BaseEntity {

    /** 所属子公司ID（业务单据归属公司） */
    private Long companyId;

    /** 流程实例编号 */
    private String instanceNo;

    /** 流程定义ID */
    private Long defId;

    /** 流程名称快照 */
    private String defName;

    /** 业务类型（contract/contract_discount等） */
    private String bizType;

    /** 来源单据类型（contract/reimburse/purchase/plan） */
    private String sourceType;

    /** 来源单据ID */
    private String sourceId;

    /** 审批标题（单据摘要） */
    private String title;

    /** 申请人用户ID */
    private Long applyUserId;

    /** 申请人姓名 */
    private String applyUserName;

    /** 实例状态 0审批中 1通过 2驳回 3撤回 4终止 */
    private Integer instanceStatus;

    /** 当前节点名称 */
    private String currentNodeName;

    /** 当前节点审批人ID集合JSON */
    private String currentHandlers;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 完成时间 */
    private LocalDateTime finishTime;
}