package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收费规则返回（含收费类型名称）
 *
 * @author gbi
 */
@Data
@Schema(description = "收费规则返回")
public class FeeRuleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "关联收费类型ID")
    private Long feeItemId;

    @Schema(description = "收费类型名称")
    private String feeItemName;

    @Schema(description = "收费方式 1定额 2按面积")
    private Integer calcMode;

    @Schema(description = "收费方式文本")
    private String calcModeText;

    @Schema(description = "单价（定额=固定金额/周期；按面积=每平米单价）")
    private BigDecimal price;

    @Schema(description = "收费周期 1按年 2按月 3按日")
    private Integer periodType;

    @Schema(description = "收费周期文本")
    private String periodTypeText;

    @Schema(description = "滞纳金百分比")
    private BigDecimal overdueRate;

    @Schema(description = "规则状态 0停用 1启用")
    private Integer status;

    @Schema(description = "规则备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}