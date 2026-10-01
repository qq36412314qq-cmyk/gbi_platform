package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑加班申请入参（仅草稿/待审批状态可编辑）
 *
 * @author gbi
 */
@Data
public class HrOvertimeApplyEditDTO {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    private BigDecimal expectedHours;

    private String reason;
}
