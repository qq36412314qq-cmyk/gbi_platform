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
 * 收费类型新增/编辑入参（租金/物业费/水费/电费/押金/其他等）
 *
 * @author gbi
 */
@Data
@Schema(description = "收费类型新增/编辑入参")
public class FeeItemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收费类型ID（编辑时必填）")
    private Long id;

    @Schema(description = "收费类型名称（租金/物业费/水费/电费/押金/其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收费类型名称不能为空")
    @Size(max = 128, message = "收费类型名称不能超过128字符")
    private String feeItemName;

    @Schema(description = "收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收费类别不能为空")
    @Min(value = 1, message = "收费类别编码范围为1-6")
    @Max(value = 6, message = "收费类别编码范围为1-6")
    private Integer categoryType;

    @Schema(description = "计量单位（元/月、元/平米、元/吨等）")
    @Size(max = 32, message = "计量单位不能超过32字符")
    private String calcUnit;

    @Schema(description = "收费类型备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}