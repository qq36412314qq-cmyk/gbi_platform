package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 编辑用户入参（对齐前端 UserDTO）
 *
 * @author gbi
 */
@Data
@Schema(description = "编辑用户入参")
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,31}$", message = "账号须为字母开头，3-32位字母/数字/下划线")
    private String username;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 64, message = "真实姓名不能超过64字符")
    private String realName;

    @Schema(description = "手机号码")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Pattern(regexp = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", message = "邮箱格式不正确")
    private String email;

    @Schema(description = "头像地址")
    @Size(max = 1000, message = "头像地址过长")
    private String avatar;

    @Schema(description = "账号状态 0禁用 1正常", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账号状态不能为空")
    private Integer status;

    @Schema(description = "角色ID集合")
    private List<Long> roleIds;
}
