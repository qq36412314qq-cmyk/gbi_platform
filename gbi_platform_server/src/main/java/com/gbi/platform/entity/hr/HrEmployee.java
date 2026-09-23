package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工主档案实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee")
public class HrEmployee extends BaseEntity {
    private Long companyId;
    /** 工作城市编码，关联sys_city.city_code */
    private String cityCode;
    /** 所属行业编码，关联sys_industry.industry_code */
    private String industryCode;
    private Long userId;
    private String employeeNo;
    private String name;
    private String idCardNo;
    private String phone;
    private String email;
    private Integer gender;
    private java.time.LocalDate birthdate;
    private java.time.LocalDate entryDate;
    private java.time.LocalDate regularDate;
    private java.time.LocalDate resignDate;
    private Integer employmentType;
    private Integer employeeStatus;
    private Long orgId;
    private Long postId;
    private String postLevel;
    /** 社保申报基数（年度锁定） */
    private java.math.BigDecimal socialDeclareBase;
    /** 公积金申报基数（年度锁定） */
    private java.math.BigDecimal housingFundDeclareBase;
    /** 基数生效年度，如2026，从2026-07至2027-06有效 */
    private String baseEffectiveYear;
    private String salaryGradeCode;
    private String orgName;
    private Long supervisorId;
    private String bankAccount;
    private java.math.BigDecimal socialSecurityBase;
    private java.math.BigDecimal basicSalary;
    private String remark;
    /** 免冠照片对应的sys_file.id */
    private Long photoFileId;
    /** 附件内容（富文本HTML） */
    private String attachmentContent;
}
