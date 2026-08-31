package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 物业费账单预览返回（不创建账单，仅计算预估金额）
 *
 * @author gbi
 */
@Data
@Schema(description = "物业费账单预览返回")
public class PropertyFeeBillPreviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "摊位ID")
    private Long stallId;

    @Schema(description = "摊位编号")
    private String stallNumber;

    @Schema(description = "摊位名称")
    private String stallName;

    @Schema(description = "所属市场名称")
    private String stallMarketName;

    @Schema(description = "计费方式 1定额 2按面积")
    private Integer calcMode;

    @Schema(description = "计费方式文本")
    private String calcModeText;

    @Schema(description = "收费周期 1按年 2按月 3按日")
    private Integer periodType;

    @Schema(description = "收费周期文本")
    private String periodTypeText;

    @Schema(description = "用量（定额=0，按面积=面积值）")
    private BigDecimal usage;

    @Schema(description = "计费单价")
    private BigDecimal unitPrice;

    @Schema(description = "周期系数")
    private BigDecimal periodFactor;

    @Schema(description = "预估金额")
    private BigDecimal amount;

    @Schema(description = "是否已有账单")
    private Boolean hasExisting;
}
