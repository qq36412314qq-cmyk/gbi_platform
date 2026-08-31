package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * UI 主题配置实体：sys_ui_theme
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ui_theme")
public class SysUiTheme extends BaseEntity {

    /** 所属子公司ID，0集团全局配置 */
    private Long companyId;

    /** 系统主色十六进制 */
    private String primaryColor;

    /** 布局模式 side/top */
    private String layoutMode;

    /** 卡片圆角像素 */
    private Integer cardRadius;

    /** 暗黑模式 0关闭 1开启 */
    private Integer darkMode;
}
