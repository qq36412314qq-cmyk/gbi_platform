package com.gbi.platform.dto.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工学业经历新增/编辑入参
 *
 * @author gbi
 */
@Schema(description = "员工学业经历入参")
@Data
public class EduExpDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    @Schema(description = "学历：本科/硕士/博士/大专/其他")
    private String degree;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "教育形式：全日制/在职/自考等")
    private String educationLevel;

    @Schema(description = "入学时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入学时间不能为空")
    private LocalDate startDate;

    @Schema(description = "毕业时间")
    private LocalDate graduationDate;

    @Schema(description = "是否已毕业 1是 0否 默认0")
    private Integer isGraduated;

    @Schema(description = "学位证书号")
    private String certificateNo;

    @Schema(description = "备注")
    private String remark;
}
