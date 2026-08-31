package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据新增/编辑入参（对齐前端 DictDataDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "字典数据入参")
public class DictDataDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典数据ID（编辑时必填）")
    private Long id;

    @Schema(description = "字典类型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典类型不能为空")
    private Long dictTypeId;

    @Schema(description = "字典显示文本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "显示文本不能为空")
    @Size(max = 128, message = "显示文本不能超过128字符")
    private String dictValue;

    @Schema(description = "字典存储值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储值不能为空")
    @Size(max = 128, message = "存储值不能超过128字符")
    private String dictKey;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序号最小为0")
    @Max(value = 9999, message = "排序号最大为9999")
    private Integer sortOrder;

    @Schema(description = "状态0禁用1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
