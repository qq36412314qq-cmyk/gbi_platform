package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工工作经历新增/编辑入参
 *
 * @author gbi
 */
@Schema(description = "员工工作经历入参")
@Data
public class WorkExpDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "入职时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入职时间不能为空")
    private LocalDate startDate;

    @Schema(description = "离职时间（当前在职可为空）")
    private LocalDate endDate;

    @Schema(description = "是否仍在职 1是 0否 默认0")
    private Integer isCurrent;

    @Schema(description = "离职原因")
    private String reasonForLeaving;

    @Schema(description = "备注")
    private String remark;
}
