package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 节假日配置DTO
 *
 * @author gbi
 */
@Schema(description = "节假日配置DTO")
@Data
public class SysHolidayConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（更新时必填）")
    private Long id;

    @Schema(description = "所属公司ID，0=集团全局")
    private Long companyId;

    @Schema(description = "节假日日期")
    private LocalDate holidayDate;

    @Schema(description = "节假日名称")
    private String holidayName;

    @Schema(description = "类型 1法定假日 2调休日 3补班日")
    private Integer holidayType;

    @Schema(description = "是否为工作日 0否 1是（补班日）")
    private Integer isWorkday;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
