package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节假日配置视图
 *
 * @author gbi
 */
@Schema(description = "节假日配置视图")
@Data
public class SysHolidayConfigVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "节假日日期")
    private LocalDate holidayDate;

    @Schema(description = "节假日名称")
    private String holidayName;

    @Schema(description = "类型 1法定假日 2调休日 3补班日")
    private Integer holidayType;

    @Schema(description = "类型文本")
    private String holidayTypeText;

    @Schema(description = "是否为工作日 0否 1是")
    private Integer isWorkday;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
