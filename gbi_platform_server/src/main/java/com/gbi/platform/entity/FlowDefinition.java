package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一审批流程定义表实体：flow_definition
 * 集团全局模板 company_id=0，子公司只读复用；
 * node_config_json 节点配置由 FlowConfigUtil 解析并过滤脚本后入库
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_definition")
public class FlowDefinition extends BaseEntity {

    /** 所属子公司ID，0=集团全局模板 */
    private Long companyId;

    /** 流程名称 */
    private String defName;

    /** 流程编码（唯一）contract/contract_discount/contract_terminate/plan_adjust等 */
    private String defCode;

    /** 适用业务类型 */
    private String bizType;

    /** 节点配置JSON */
    private String nodeConfigJson;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注（大额流程定义标注适用金额阈值） */
    private String remark;
}