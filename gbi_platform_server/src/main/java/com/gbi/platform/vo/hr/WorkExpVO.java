package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工工作经历视图对象
 *
 * @author gbi
 */
@Schema(description = "员工工作经历视图")
@Data
public class WorkExpVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "入职时间")
    private LocalDate startDate;

    @Schema(description = "离职时间")
    private LocalDate endDate;

    @Schema(description = "是否仍在职 1是 0否")
    private Integer isCurrent;

    @Schema(description = "是否仍在职文本")
    private String isCurrentText;

    @Schema(description = "离职原因")
    private String reasonForLeaving;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private java.time.LocalDateTime createTime;
}
