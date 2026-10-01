package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 休息日配置视图
 *
 * @author gbi
 */
@Schema(description = "休息日配置视图")
@Data
public class SysWorkweekConfigVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "休息日类型 1单休 2双休 3做五休二 4做六休一 5综合工时")
    private Integer workweekType;

    @Schema(description = "休息日类型文本")
    private String workweekTypeText;

    @Schema(description = "休息日模式")
    private String restDayPattern;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
