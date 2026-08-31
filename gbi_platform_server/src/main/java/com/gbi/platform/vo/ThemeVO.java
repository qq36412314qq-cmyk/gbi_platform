package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * UI 主题返回（对齐前端 UiThemeDTO）
 *
 * @author gbi
 */
@Data
@Schema(description = "UI主题配置")
public class ThemeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主题ID")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局")
    private Long companyId;

    @Schema(description = "系统主色十六进制")
    private String primaryColor;

    @Schema(description = "布局模式 side/top")
    private String layoutMode;

    @Schema(description = "卡片圆角像素")
    private Integer cardRadius;

    @Schema(description = "暗黑模式 0关闭 1开启")
    private Integer darkMode;
}
