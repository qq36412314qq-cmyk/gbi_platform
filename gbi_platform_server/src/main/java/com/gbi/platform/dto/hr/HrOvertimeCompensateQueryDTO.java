package com.gbi.platform.dto.hr;

import lombok.Data;

/**
 * 加班补偿台账分页查询入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeCompensateQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long employeeId;
    private String employeeName;
    private String compensateMonth;
    private Integer payStatus;
}
