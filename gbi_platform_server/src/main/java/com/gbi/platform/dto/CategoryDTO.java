package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增/编辑租赁分类入参（商铺/仓库/车位等，可自定义）
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁分类新增/编辑入参")
public class CategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID（编辑时必填）")
    private Long id;

    @Schema(description = "分类名称（商铺/仓库/车位等）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过64字符")
    private String categoryName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}