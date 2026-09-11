package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Schema(description="入职申请视图")
@Data public class HrEntryApplyVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工工号") private String employeeNo;
    @Schema(description="姓名") private String name;
    @Schema(description="身份证号（脱敏）") private String idCardNo;
    @Schema(description="手机号（脱敏）") private String phone;
    @Schema(description="性别") private Integer gender;
    @Schema(description="出生日期") private LocalDate birthdate;
    @Schema(description="计划入职日期") private LocalDate entryDate;
    @Schema(description="用工类型") private Integer employmentType;
    @Schema(description="用工类型文本") private String employmentTypeText;
    @Schema(description="目标组织ID") private Long orgId;
    @Schema(description="目标岗位ID") private Long postId;
    @Schema(description="基本工资") private BigDecimal basicSalary;
    @Schema(description="工资卡号（脱敏）") private String bankAccount;
    @Schema(description="是否自动创建系统账号") private Integer autoCreateUser;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="状态 0草稿 1审批中 2已通过 3已驳回 4已撤回") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}