package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账号不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "密码不能为空")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
