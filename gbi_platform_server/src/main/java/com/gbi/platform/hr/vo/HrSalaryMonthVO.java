package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Schema(description="月度薪资核算视图")
@Data public class HrSalaryMonthVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="核算月份 yyyy-MM") private String salaryMonth;
    @Schema(description="基本工资快照") private BigDecimal basicSalary;
    @Schema(description="绩效工资快照") private BigDecimal performanceSalary;
    @Schema(description="补贴合计快照") private BigDecimal allowanceAmount;
    @Schema(description="社保扣款快照") private BigDecimal socialSecurity;
    @Schema(description="公积金扣款快照") private BigDecimal housingFund;
    @Schema(description="个税快照") private BigDecimal taxAmount;
    @Schema(description="其他扣款快照") private BigDecimal deductionAmount;
    @Schema(description="应发合计") private BigDecimal grossAmount;
    @Schema(description="实发合计") private BigDecimal netAmount;
    @Schema(description="发放状态 0未发放 1已发放 2发放失败") private Integer payStatus;
    @Schema(description="发放状态文本") private String payStatusText;
    @Schema(description="实际发放时间") private LocalDateTime payTime;
    @Schema(description="流程实例ID") private Long flowInstanceId;
    @Schema(description="关联应收应付计划ID") private Long planId;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}
