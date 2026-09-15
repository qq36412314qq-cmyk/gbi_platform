package com.gbi.platform.dto.hr;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入职申请提交入参
 *
 * @author gbi
 */
@Schema(description = "入职申请提交入参")
@Data
public class EntryApplyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "员工工号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "员工工号不能为空")
    @Size(max = 32)
    private String employeeNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64)
    private String name;

    @Schema(description = "身份证号（AES密文）")
    @Size(max = 64)
    private String idCardNo;

    @Schema(description = "手机号（AES密文）")
    @Size(max = 32)
    private String phone;

    @Schema(description = "性别 1男 2女")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthdate;

    @Schema(description = "计划入职日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划入职日期不能为空")
    private LocalDate entryDate;

    @Schema(description = "用工类型 1正式 2试用期 3劳务派遣 4临时工")
    private Integer employmentType;

    @Schema(description = "目标组织ID")
    private Long orgId;

    @Schema(description = "目标岗位ID")
    private Long postId;

    @Schema(description = "基本工资")
    private BigDecimal basicSalary;

    @Schema(description = "工资卡号（密文）")
    @Size(max = 64)
    private String bankAccount;

    @Schema(description = "是否自动创建系统账号 0否 1是")
    private Integer autoCreateUser;

    @Schema(description = "备注")
    @Size(max = 500)
    private String remark;

    /**
     * 工作经历和学业经历JSON数据
     * JSON格式示例：
     * {
     *   "workExps": [
     *     {
     *       "companyName": "某某科技有限公司",
     *       "position": "软件工程师",
     *       "department": "研发部",
     *       "startDate": "2020-03-01",
     *       "endDate": "2023-06-30",
     *       "isCurrent": 0,
     *       "reasonForLeaving": "个人发展",
     *       "remark": ""
     *     },
     *     {
     *       "companyName": "某某互联网公司",
     *       "position": "前端开发",
     *       "department": "产品部",
     *       "startDate": "2018-07-01",
     *       "endDate": null,
     *       "isCurrent": 1,
     *       "reasonForLeaving": null,
     *       "remark": ""
     *     }
     *   ],
     *   "eduExps": [
     *     {
     *       "schoolName": "某某大学",
     *       "degree": "本科",
     *       "major": "计算机科学与技术",
     *       "educationLevel": "全日制",
     *       "startDate": "2014-09-01",
     *       "graduationDate": "2018-06-30",
     *       "isGraduated": 1,
     *       "certificateNo": "12345678",
     *       "remark": ""
     *     }
     *   ]
     * }
     */
    @Schema(description = "工作经历和学业经历JSON数据")
    private String experienceData;
}
