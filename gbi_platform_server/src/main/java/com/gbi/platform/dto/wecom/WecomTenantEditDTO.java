package com.gbi.platform.dto.wecom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企微租户配置编辑入参
 *
 * @author gbi
 */
@Data
public class WecomTenantEditDTO {

    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    @NotBlank(message = "corpId不能为空")
    private String corpId;

    private Integer status;

    private String remark;
}
