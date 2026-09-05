package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建缴费单入参
 * 从多条统一账单（biz_fee_bill）生成一条缴费单（finance_pay_order）
 *
 * @author gbi
 */
@Data
@Schema(description = "创建缴费单入参")
public class PayBillCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统一账单ID列表（biz_fee_bill.id）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "账单ID列表不能为空")
    private List<Long> bizFeeBillIds;

    @Schema(description = "缴费人姓名")
    @Size(max = 64, message = "缴费人姓名不能超过64字符")
    private String payerName;

    @Schema(description = "缴费人手机号")
    @Size(max = 32, message = "手机号不能超过32字符")
    private String payerPhone;

    @Schema(description = "备注说明")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}