package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 新增租赁铺位入参（商铺/仓库/车位等，分类可自定义）
 *
 * @author gbi
 */
@Data
@Schema(description = "新增租赁铺位入参")
public class StallAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联市场ID")
    private Long marketId;

    @Schema(description = "租赁分类ID（关联 stall_category，可自定义）")
    private Long stallCategoryId;

    @Schema(description = "铺位编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "铺位编号不能为空")
    @Size(max = 64, message = "铺位编号不能超过64字符")
    private String stallNumber;

    @Schema(description = "铺位名称")
    @Size(max = 128, message = "铺位名称不能超过128字符")
    private String stallName;

    @Schema(description = "铺位面积(平方米)")
    @Digits(integer = 8, fraction = 2, message = "面积最多两位小数")
    private BigDecimal stallArea;

    @Schema(description = "铺位备注")
    @Size(max = 500, message = "铺位备注不能超过500字符")
    private String remark;

    @Schema(description = "绑定收费规则ID集合（biz_fee_rule，同一收费类型限选一条；可空=不绑定）")
    private List<Long> ruleIds;
}