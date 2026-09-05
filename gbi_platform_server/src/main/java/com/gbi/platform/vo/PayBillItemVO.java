package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 缴费单明细VO
 *
 * @author gbi
 */
@Data
@Schema(description = "缴费单明细VO")
public class PayBillItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "缴费单ID")
    private Long payBillId;

    @Schema(description = "关联账单ID")
    private Long billId;

    @Schema(description = "业务类型文本")
    private String bizTypeText;

    @Schema(description = "收费规则名称")
    private String ruleName;

    @Schema(description = "收费项名称")
    private String feeItemName;

    @Schema(description = "账单周期标识")
    private String billMonth;

    @Schema(description = "应收金额")
    private BigDecimal amount;

    @Schema(description = "已缴金额")
    private BigDecimal paidAmount;

    @Schema(description = "未缴金额")
    private BigDecimal unpaidAmount;
}