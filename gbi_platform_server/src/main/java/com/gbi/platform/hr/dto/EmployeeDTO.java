package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDate;
@Schema(description="员工新增/编辑入参")
@Data public class EmployeeDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="员工ID（编辑时必填）") private Long id;
    @Schema(description="员工工号",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="员工工号不能为空") @Size(max=32) private String employeeNo;
    @Schema(description="姓名",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="姓名不能为空") @Size(max=64) private String name;
    @Schema(description="身份证号（AES密文）") @Size(max=64) private String idCardNo;
    @Schema(description="手机号（AES密文）") @Size(max=32) private String phone;
    @Schema(description="邮箱") @Size(max=128) private String email;
    @Schema(description="性别 1男 2女") private Integer gender;
    @Schema(description="出生日期") private LocalDate birthdate;
    @Schema(description="入职日期") private LocalDate entryDate;
    @Schema(description="用工类型 1正式 2试用期 3劳务派遣 4临时工") private Integer employmentType;
    @Schema(description="所属组织ID") private Long orgId;
    @Schema(description="岗位ID") private Long postId;
    @Schema(description="工资卡号（密文）") @Size(max=64) private String bankAccount;
    @Schema(description="社保公积金缴纳基数") private BigDecimal socialSecurityBase;
    @Schema(description="基本工资") private BigDecimal basicSalary;
    @Schema(description="备注") @Size(max=500) private String remark;
}
