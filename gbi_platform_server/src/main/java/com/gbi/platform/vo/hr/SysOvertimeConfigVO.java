package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 加班配置 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "加班配置")
public class SysOvertimeConfigVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    private Long id;
    @Schema(description = "所属公司ID")
    private Long companyId;
    @Schema(description = "配置名称")
    private String configName;
    @Schema(description = "最小加班时长（小时）")
    private BigDecimal overtimeMinHours;
    /** 取整模式 1向上 2四舍五入 3向下 */
    @Schema(description = "取整模式")
    private Integer overtimeRoundMode;
    @Schema(description = "工作日加班倍数")
    private BigDecimal workdayRate;
    @Schema(description = "休息日加班倍数")
    private BigDecimal restdayRate;
    @Schema(description = "法定节假日加班倍数")
    private BigDecimal holidayRate;
    @Schema(description = "每月最大加班时长上限（NULL=不限）")
    private BigDecimal maxOvertimeHours;
    /** 补偿优先级 1调休优先 2加班费优先 */
    @Schema(description = "补偿优先级")
    private Integer compensatePriority;
    @Schema(description = "是否启用自动识别")
    private Integer autoDetectEnabled;
    @Schema(description = "自动识别Cron表达式")
    private String autoDetectCron;
    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
}
