package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务营收汇总返回（按公司/业务类型/收支方向聚合）
 *
 * @author gbi
 */
@Data
@Schema(description = "财务营收汇总返回")
public class FinanceSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属子公司ID（集团视图区分公司）")
    private Long companyId;

    @Schema(description = "业务类型 rent/water_elec/deposit/marketing")
    private String businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "流水类型 1收入 2支出退费")
    private Integer flowType;

    @Schema(description = "流水类型文本")
    private String flowTypeText;

    @Schema(description = "汇总笔数")
    private Long flowCount;

    @Schema(description = "汇总金额")
    private BigDecimal totalAmount;
}
