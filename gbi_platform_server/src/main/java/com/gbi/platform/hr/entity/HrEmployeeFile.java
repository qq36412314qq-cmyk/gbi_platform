package com.gbi.platform.hr.entity;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工附件实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_file")
public class HrEmployeeFile extends BaseEntity {
    private Long companyId;
    private Long employeeId;
    private Integer fileType;
    private String fileName;
    private String fileUrl;
}
