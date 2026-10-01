package com.gbi.platform.dto.hr;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 加班记录分页查询入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeRecordQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long employeeId;
    private String employeeName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate overtimeDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate overtimeDateEnd;
    /** 来源类型 1手动申请 2自动识别 */
    private Integer sourceType;
    /** 确认状态 0待确认 1已确认 2已驳回 */
    private Integer confirmStatus;
}
