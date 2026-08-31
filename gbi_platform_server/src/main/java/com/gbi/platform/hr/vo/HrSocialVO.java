package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Schema(description="社保公积金视图")
@Data public class HrSocialVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="员工ID") private Long employeeId;
    @Schema(description="员工姓名") private String employeeName;
    @Schema(description="社保缴纳基数") private BigDecimal socialSecurityBase;
    @Schema(description="公积金缴纳基数") private BigDecimal housingFundBase;
    @Schema(description="公司承担社保金额") private BigDecimal socialSecurityCompany;
    @Schema(description="个人承担社保金额") private BigDecimal socialSecurityPersonal;
    @Schema(description="公司承担公积金金额") private BigDecimal housingFundCompany;
    @Schema(description="个人承担公积金金额") private BigDecimal housingFundPersonal;
    @Schema(description="参保起始月份") private String startMonth;
    @Schema(description="参保截止月份") private String endMonth;
    @Schema(description="状态 0停保 1参保") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}
