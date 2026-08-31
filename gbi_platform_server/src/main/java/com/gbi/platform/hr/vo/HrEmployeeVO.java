package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="员工档案视图")
@Data public class HrEmployeeVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="关联sys_user.id") private Long userId;
    @Schema(description="员工工号") private String employeeNo;
    @Schema(description="姓名") private String name;
    @Schema(description="身份证号（脱敏）") private String idCardNo;
    @Schema(description="手机号（脱敏）") private String phone;
    @Schema(description="邮箱") private String email;
    @Schema(description="性别 1男 2女") private Integer gender;
    @Schema(description="性别文本") private String genderText;
    @Schema(description="出生日期") private LocalDate birthdate;
    @Schema(description="入职日期") private LocalDate entryDate;
    @Schema(description="转正日期") private LocalDate regularDate;
    @Schema(description="离职日期") private LocalDate resignDate;
    @Schema(description="用工类型 1正式 2试用期 3劳务派遣 4临时工") private Integer employmentType;
    @Schema(description="用工类型文本") private String employmentTypeText;
    @Schema(description="状态 0待入职 1在职 2试用期 3离职 4终止合同") private Integer employeeStatus;
    @Schema(description="状态文本") private String employeeStatusText;
    @Schema(description="所属组织ID") private Long orgId;
    @Schema(description="岗位ID") private Long postId;
    @Schema(description="岗位职级快照") private String postLevel;
    @Schema(description="组织名称快照") private String orgName;
    @Schema(description="直属上级用户ID") private Long supervisorId;
    @Schema(description="工资卡号（脱敏）") private String bankAccount;
    @Schema(description="社保公积金缴纳基数") private BigDecimal socialSecurityBase;
    @Schema(description="基本工资快照") private BigDecimal basicSalary;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}
