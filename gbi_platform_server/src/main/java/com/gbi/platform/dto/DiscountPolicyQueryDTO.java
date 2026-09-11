package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 优惠策略分页查询 DTO（扩展）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper=false)
@Schema(description = "优惠策略分页查询入参")
public class DiscountPolicyQueryDTO extends PageDTO {

    @Schema(description = "策略名称模糊")
    private String policyName;

    @Schema(description = "业务类型筛选")
    private String bizType;

    @Schema(description = "优惠类型筛选")
    private Integer discountType;

    @Schema(description = "适用范围筛选")
    private Integer scopeType;

    @Schema(description = "状态筛选 0停用 1启用")
    private Integer status;
}