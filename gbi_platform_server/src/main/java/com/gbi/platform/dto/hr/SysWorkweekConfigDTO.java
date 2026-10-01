package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 休息日配置DTO
 *
 * @author gbi
 */
@Schema(description = "休息日配置DTO")
@Data
public class SysWorkweekConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（更新时必填）")
    private Long id;

    @Schema(description = "所属公司ID，0=集团全局")
    private Long companyId;

    @Schema(description = "配置名称，如：双休、单休、做五休二")
    private String configName;

    @Schema(description = "休息日类型 1单休 2双休 3做五休二 4做六休一 5综合工时")
    private Integer workweekType;

    @Schema(description = "休息日模式，如：周六日、周日、周一")
    private String restDayPattern;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
