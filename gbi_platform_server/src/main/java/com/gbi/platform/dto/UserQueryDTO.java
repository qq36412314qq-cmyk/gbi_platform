package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询入参（对齐前端 UserQueryDTO）
 *
 * @author gbi
 */
@Data
@Schema(description = "用户分页查询入参")
public class UserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize;

    @Schema(description = "登录账号（模糊）")
    private String username;

    @Schema(description = "真实姓名（模糊）")
    private String realName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "账号状态")
    private Integer status;
}
