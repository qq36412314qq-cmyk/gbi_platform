package com.gbi.platform.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加班记录确认/驳回入参
 *
 * @author gbi
 */
@Data
public class HrOvertimeRecordConfirmDTO {

    @NotNull(message = "记录ID不能为空")
    private Long id;

    /** 确认操作：1=确认 2=驳回 */
    @NotNull(message = "确认操作不能为空")
    private Integer confirmStatus;

    /** 驳回原因（confirmStatus=2时必填） */
    private String remark;
}
