package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 摊位下拉选项（供水电表绑定、合同选择等跨模块联动下拉使用）
 * 包含摊位编号/名称/所属市场/租赁分类，company_id 由多租户拦截器自动隔离
 *
 * @author gbi
 */
@Data
@Schema(description = "摊位下拉选项")
public class StallOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "摊位ID")
    private Long id;

    @Schema(description = "摊位编号")
    private String stallNumber;

    @Schema(description = "摊位名称")
    private String stallName;

    @Schema(description = "所属市场ID")
    private Long marketId;

    @Schema(description = "所属市场名称")
    private String marketName;

    @Schema(description = "租赁分类ID")
    private Long stallCategoryId;

    @Schema(description = "租赁分类名称")
    private String categoryName;

    @Schema(description = "摊位面积(平方米，按面积收费规则计算用)")
    private java.math.BigDecimal stallArea;
}