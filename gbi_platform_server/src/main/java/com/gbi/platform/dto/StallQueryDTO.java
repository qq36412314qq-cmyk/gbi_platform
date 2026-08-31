package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租赁摊位分页查询入参
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁摊位分页查询入参")
public class StallQueryDTO implements Serializable {

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

    @Schema(description = "摊位编号（模糊）")
    private String stallNumber;

    @Schema(description = "租赁分类ID")
    private Long stallCategoryId;

    @Schema(description = "摊位状态 0空置 1已租 2欠费 3即将到期")
    private Integer status;

    @Schema(description = "关联市场ID")
    private Long marketId;
}