package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典类型新增/编辑入参（对齐前端 DictTypeDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "字典类型入参")
public class DictTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典类型ID（编辑时必填）")
    private Long id;

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字典编码须为小写字母/数字/下划线")
    @Size(max = 64, message = "字典编码不能超过64字符")
    private String dictCode;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 128, message = "字典名称不能超过128字符")
    private String dictName;

    @Schema(description = "状态 0禁用 1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}
