package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微 Token 刷新结果 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "企微Token信息")
public class WecomTokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "access_token")
    private String accessToken;

    @Schema(description = "有效期（秒），默认7200")
    private Long expiresIn;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}
