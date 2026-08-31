package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.math.BigDecimal;
@Schema(description="社保公积金新增/编辑入参")
@Data public class SocialDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="社保记录ID（编辑时必填）") private Long id;
    @Schema(description="员工ID",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="员工ID不能为空") private Long employeeId;
    @Schema(description="社保缴纳基数",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="社保基数不能为空") private BigDecimal socialSecurityBase;
    @Schema(description="公积金缴纳基数",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotNull(message="公积金基数不能为空") private BigDecimal housingFundBase;
    @Schema(description="公司承担社保金额") private BigDecimal socialSecurityCompany;
    @Schema(description="个人承担社保金额") private BigDecimal socialSecurityPersonal;
    @Schema(description="公司承担公积金金额") private BigDecimal housingFundCompany;
    @Schema(description="个人承担公积金金额") private BigDecimal housingFundPersonal;
    @Schema(description="参保起始月份 yyyy-MM",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="参保起始月份不能为空") private String startMonth;
    @Schema(description="备注") private String remark;
}
