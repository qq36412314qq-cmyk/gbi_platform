package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 优惠申请分页查询 DTO（扩展）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper=false)
@Schema(description = "优惠申请分页查询入参")
public class DiscountApplyQueryDTO extends PageDTO {

    @Schema(description = "业务类型筛选")
    private String bizType;

    @Schema(description = "来源类型筛选")
    private String sourceType;

    @Schema(description = "合同编号/单据编号模糊搜索")
    private String sourceNo;

    @Schema(description = "申请状态筛选 0草稿 1审批中 2通过 3驳回 4作废")
    private Integer applyStatus;

    @Schema(description = "是否需审批筛选 0否 1是")
    private Integer needAudit;

    @Schema(description = "铺位ID筛选")
    private Long stallId;

    @Schema(description = "优惠起始月份筛选")
    private String startMonth;

    @Schema(description = "优惠结束月份筛选")
    private String endMonth;
}