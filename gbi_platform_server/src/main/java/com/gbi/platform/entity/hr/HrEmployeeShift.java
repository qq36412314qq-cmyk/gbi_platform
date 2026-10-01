package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 员工班次配置实体：hr_employee_shift
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_shift")
public class HrEmployeeShift extends BaseEntity {

    /** 所属公司ID */
    private Long companyId;

    /** 员工ID（关联hr_employee.id） */
    private Long employeeId;

    /** 班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时 */
    private Integer shiftType;

    /** 班次上班时间 */
    private LocalTime shiftStartTime;

    /** 班次下班时间 */
    private LocalTime shiftEndTime;

    /** 班次生效开始日期 */
    private LocalDate startDate;

    /** 班次生效结束日期，NULL表示长期有效 */
    private LocalDate endDate;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
