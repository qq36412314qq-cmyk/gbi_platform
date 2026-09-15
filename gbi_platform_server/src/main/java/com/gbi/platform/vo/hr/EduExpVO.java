package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工学业经历视图对象
 *
 * @author gbi
 */
@Schema(description = "员工学业经历视图")
@Data
public class EduExpVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学历")
    private String degree;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "教育形式")
    private String educationLevel;

    @Schema(description = "入学时间")
    private LocalDate startDate;

    @Schema(description = "毕业时间")
    private LocalDate graduationDate;

    @Schema(description = "是否已毕业 1是 0否")
    private Integer isGraduated;

    @Schema(description = "是否已毕业文本")
    private String isGraduatedText;

    @Schema(description = "学位证书号")
    private String certificateNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private java.time.LocalDateTime createTime;
}
