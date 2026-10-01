package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 自动识别结果 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "自动识别结果")
public class HrOvertimeAutoDetectVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "识别状态：SUCCESS/RUNNING/FAILED")
    private String status;
    @Schema(description = "识别总条数（参与计算的考勤记录数）")
    private Integer totalRecords;
    @Schema(description = "新增记录数")
    private Integer newRecords;
    @Schema(description = "更新记录数")
    private Integer updatedRecords;
    @Schema(description = "跳过记录数")
    private Integer skippedRecords;
    @Schema(description = "错误信息")
    private String errorMsg;
}
