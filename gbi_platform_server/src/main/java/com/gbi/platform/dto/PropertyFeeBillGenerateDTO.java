package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 物业费批量/单条生成入参
 *
 * @author gbi
 */
@Data
@Schema(description = "物业费账单生成入参")
public class PropertyFeeBillGenerateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账单月份 yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账单月份不能为空")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "账单月份格式须为 yyyy-MM")
    private String billMonth;

    @Schema(description = "市场ID（批量生成时按市场筛选，null=全部）")
    private Long marketId;

    @Schema(description = "指定摊位ID（单条生成时使用，批量时忽略）")
    private Long stallId;
}
