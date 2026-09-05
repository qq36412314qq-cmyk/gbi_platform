package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 收费规则-铺位绑定关联实体：biz_fee_rule_stall_rel
 * 铺位多选收费规则（同收费类型限选一条，service 层校验）；
 * override_* 字段为单铺位特殊规则覆盖预留（本期界面不提供）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_fee_rule_stall_rel")
public class FeeRuleStallRel extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 收费规则ID（biz_fee_rule） */
    private Long ruleId;

    /** 铺位ID（stall_info） */
    private Long stallId;

    /** 是否特殊覆盖 0普通绑定 1单铺位覆盖（预留） */
    private Integer overrideFlag;

    /** 覆盖单价（预留，override_flag=1时生效） */
    private BigDecimal overridePrice;

    /** 覆盖配置JSON（预留，后端过滤脚本后入库） */
    private String overrideConfigJson;
}