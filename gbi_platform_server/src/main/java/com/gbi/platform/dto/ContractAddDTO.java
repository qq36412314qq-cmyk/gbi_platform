package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增租赁合同入参（租户 + 铺位绑定，生效后铺位置为已租）
 *
 * @author gbi
 */
@Data
@Schema(description = "新增租赁合同入参")
public class ContractAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID（关联 stall_tenant）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租户不能为空")
    private Long tenantId;

    @Schema(description = "铺位ID（关联 stall_info，须为空置铺位）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "铺位不能为空")
    private Long stallId;

    @Schema(description = "月租金金额")
    @Digits(integer = 10, fraction = 2, message = "月租金最多两位小数")
    private BigDecimal rentAmount;

    @Schema(description = "押金金额")
    @Digits(integer = 10, fraction = 2, message = "押金最多两位小数")
    private BigDecimal depositAmount;

    @Schema(description = "租赁开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租赁开始日期不能为空")
    private LocalDate startTime;

    @Schema(description = "租赁到期日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租赁到期日期不能为空")
    private LocalDate endTime;

    @Schema(description = "合同附件OSS地址")
    @Size(max = 1000, message = "附件地址不能超过1000字符")
    private String attachmentUrl;

    @Schema(description = "合同备注")
    @Size(max = 500, message = "合同备注不能超过500字符")
    private String remark;

    /* ------------------------------ 优惠申请（可选，随合同提交） ------------------------------ */

    @Schema(description = "优惠策略ID（可选，传此则按策略参数计算；否则用下方手动参数）")
    private Long policyId;

    @Schema(description = "免租期月数（手动优惠参数）")
    private Integer waiveMonths;

    @Schema(description = "折扣率%（手动优惠参数，100=无折扣）")
    @Digits(integer = 3, fraction = 2, message = "折扣率最多两位小数")
    private BigDecimal discountRate;

    @Schema(description = "减免金额（手动优惠参数）")
    @Digits(integer = 10, fraction = 2, message = "减免金额最多两位小数")
    private BigDecimal deductAmount;

    @Schema(description = "优惠申请备注")
    @Size(max = 500, message = "优惠申请备注不能超过500字符")
    private String discountRemark;
}