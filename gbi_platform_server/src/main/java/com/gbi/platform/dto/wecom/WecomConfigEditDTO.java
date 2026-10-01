package com.gbi.platform.dto.wecom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企微业务配置编辑入参（sys_config 单条编辑）
 *
 * @author gbi
 */
@Data
public class WecomConfigEditDTO {

    @NotNull(message = "配置ID不能为空")
    private Long id;

    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
