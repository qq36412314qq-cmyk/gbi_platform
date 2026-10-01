package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 新增加班申请入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeApplyAddDTO {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotBlank(message = "员工姓名不能为空")
    private String employeeName;

    @NotNull(message = "加班日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate overtimeDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "预计时长不能为空")
    private BigDecimal expectedHours;

    /** 加班类型 1工作日 2休息日 3法定节假日，null 则按日期自动判断 */
    private Integer overtimeType;

    @NotBlank(message = "加班事由不能为空")
    private String reason;
}
