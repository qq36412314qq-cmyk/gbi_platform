package com.gbi.platform.dto.hr;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 加班申请分页查询入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeApplyQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long employeeId;
    private String employeeName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate overtimeDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate overtimeDateEnd;
    /** 状态 0待审批 1已通过 2已驳回 3已撤回 4已取消 */
    private Integer status;
}
