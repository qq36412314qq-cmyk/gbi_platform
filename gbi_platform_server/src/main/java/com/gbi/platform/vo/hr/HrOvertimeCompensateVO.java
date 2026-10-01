package com.gbi.platform.vo.hr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班补偿台账 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "加班补偿台账")
public class HrOvertimeCompensateVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "台账ID")
    private Long id;
    @Schema(description = "所属公司ID")
    private Long companyId;
    @Schema(description = "员工ID")
    private Long employeeId;
    @Schema(description = "员工姓名")
    private String employeeName;
    @Schema(description = "基准月薪")
    private BigDecimal basicSalary;
    @Schema(description = "补偿月份")
    private String compensateMonth;
    @Schema(description = "累计加班时长（小时）")
    private BigDecimal totalHours;
    @Schema(description = "工作日加班时长")
    private BigDecimal workdayHours;
    @Schema(description = "休息日加班时长")
    private BigDecimal restdayHours;
    @Schema(description = "法定节假日加班时长")
    private BigDecimal holidayHours;
    @Schema(description = "已使用时长")
    private BigDecimal usedHours;
    @Schema(description = "剩余可使用时长")
    private BigDecimal remainHours;
    /** 补偿方式 1调休 2加班费 3混合 */
    @Schema(description = "补偿方式")
    private Integer compensateType;
    @Schema(description = "加班费金额（元）")
    private BigDecimal overtimeAmount;
    /** 发放状态 0待发放 1已发放 2已取消 */
    @Schema(description = "发放状态")
    private Integer payStatus;
    @Schema(description = "发放时间")
    private LocalDateTime payTime;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
