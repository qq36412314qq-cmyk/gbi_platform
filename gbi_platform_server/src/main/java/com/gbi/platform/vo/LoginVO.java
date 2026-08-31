package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录返回
 *
 * @author gbi
 */
@Data
@Schema(description = "登录返回")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** JWT Token */
    @Schema(description = "JWT Token")
    private String token;
}
