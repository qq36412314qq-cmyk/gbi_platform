package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * UI 主题保存入参（对齐前端 UiThemeDTO）
 *
 * @author gbi
 */
@Data
@Schema(description = "UI主题保存入参")
public class ThemeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主题ID（编辑时必填）")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局")
    @NotNull(message = "所属公司不能为空")
    private Long companyId;

    @Schema(description = "系统主色十六进制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "系统主色不能为空")
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "主色须为 #RRGGBB 格式")
    private String primaryColor;

    @Schema(description = "布局模式 side/top", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "布局模式不能为空")
    @Pattern(regexp = "^(side|top)$", message = "布局模式仅支持 side/top")
    private String layoutMode;

    @Schema(description = "卡片圆角像素", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "卡片圆角不能为空")
    @Min(value = 0, message = "圆角最小为0")
    @Max(value = 16, message = "圆角最大为16")
    private Integer cardRadius;

    @Schema(description = "暗黑模式 0关闭 1开启", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "暗黑模式不能为空")
    @Min(value = 0, message = "暗黑模式值非法")
    @Max(value = 1, message = "暗黑模式值非法")
    private Integer darkMode;
}
