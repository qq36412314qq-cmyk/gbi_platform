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
 * 系统参数新增/编辑入参（对齐前端 ConfigDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "系统参数入参")
public class ConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "参数ID（编辑时必填）")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局")
    @NotNull(message = "所属公司不能为空")
    private Long companyId;

    @Schema(description = "参数key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数key不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_.]*$", message = "参数key须为小写字母/数字/下划线/点")
    @Size(max = 128, message = "参数key不能超过128字符")
    private String configKey;

    @Schema(description = "参数值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数值不能为空")
    @Size(max = 1000, message = "参数值不能超过1000字符")
    private String configValue;

    @Schema(description = "参数显示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 128, message = "参数名称不能超过128字符")
    private String configName;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}
