package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 物业费线下缴费入参
 * requestId 幂等：同公司同 requestId 重复提交直接拦截
 *
 * @author gbi
 */
@Data
@Schema(description = "物业费线下缴费入参")
public class PropertyFeePayDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联账单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账单ID不能为空")
    private Long billId;

    @Schema(description = "支付渠道 1微信 2支付宝 3线下现金", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付渠道不能为空")
    @Min(value = 1, message = "支付渠道不合法")
    @Max(value = 3, message = "支付渠道不合法")
    private Integer payType;

    @Schema(description = "幂等请求ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{8,64}$", message = "请求ID须为8-64位字母/数字/下划线/横线")
    private String requestId;

    @Schema(description = "备注说明")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}
