package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编辑租户档案入参
 *
 * @author gbi
 */
@Data
@Schema(description = "编辑租户档案入参")
public class TenantUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租户ID不能为空")
    private Long id;

    @Schema(description = "租户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 128, message = "租户名称不能超过128字符")
    private String tenantName;

    @Schema(description = "租户类型 1个体工商户 2企业 3个人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租户类型不能为空")
    private Integer tenantType;

    @Schema(description = "联系人")
    @Size(max = 64, message = "联系人不能超过64字符")
    private String contactPerson;

    @Schema(description = "联系电话")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Schema(description = "身份证号码")
    @Size(max = 64, message = "身份证号码长度不合法")
    private String idCardNo;

    @Schema(description = "统一社会信用代码")
    @Size(max = 64, message = "统一社会信用代码不能超过64字符")
    private String socialCreditCode;

    @Schema(description = "对公银行账号")
    @Size(max = 64, message = "银行账号不能超过64字符")
    private String bankAccount;

    @Schema(description = "联系地址")
    @Size(max = 500, message = "联系地址不能超过500字符")
    private String address;

    @Schema(description = "微信小程序openid")
    @Size(max = 128, message = "openid不能超过128字符")
    private String miniOpenid;

    @Schema(description = "状态 0停用 1正常")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}