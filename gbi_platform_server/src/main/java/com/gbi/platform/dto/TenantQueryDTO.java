package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租户档案分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "租户档案分页查询入参")
public class TenantQueryDTO implements Serializable {

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

    @Schema(description = "租户名称（模糊）")
    private String tenantName;

    @Schema(description = "租户类型 1个体工商户 2企业 3个人")
    private Integer tenantType;

    @Schema(description = "联系电话（脱敏查询）")
    private String contactPhone;

    @Schema(description = "状态 0停用 1正常")
    private Integer status;
}