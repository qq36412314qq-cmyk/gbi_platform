package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一审批流程流转记录表实体：flow_record（全流程留痕）
 * action：submit提交 pass通过 reject驳回 revoke撤回 transfer转交 urge催办 cc抄送 terminate终止
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_record")
public class FlowRecord extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 流程实例ID */
    private Long instanceId;

    /** 节点名称 */
    private String nodeName;

    /** 动作 submit/pass/reject/revoke/transfer/urge/cc/terminate */
    private String action;

    /** 操作人用户ID */
    private Long handlerId;

    /** 操作人姓名 */
    private String handlerName;

    /** 操作说明/审批意见 */
    private String comment;
}