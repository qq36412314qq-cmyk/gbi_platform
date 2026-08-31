package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 统一审批任务表实体：flow_task（待办/已办）
 * 会签：一个节点多个 handler 生成多条任务，全部通过才流转
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_task")
public class FlowTask extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 流程实例ID */
    private Long instanceId;

    /** 节点名称 */
    private String nodeName;

    /** 节点顺序号（从1开始） */
    private Integer nodeOrder;

    /** 审批人用户ID（会签一个节点多条任务） */
    private Long handlerId;

    /** 审批人姓名 */
    private String handlerName;

    /** 任务状态 0待办 1已办 2已转交 3流程终止作废 */
    private Integer taskStatus;

    /** 审批结果 0驳回 1通过（仅已办节点） */
    private Integer approveResult;

    /** 审批意见 */
    private String opinion;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 转交来源任务ID */
    private Long parentTaskId;
}