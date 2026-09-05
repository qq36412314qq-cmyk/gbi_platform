package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费单明细VO（展开展示用）
 *
 * @author gbi
 */
@Data
@Schema(description = "缴费单明细")
public class PayOrderItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "缴费单ID")
    private Long payBillId;

    @Schema(description = "源账单ID")
    private Long billId;

    @Schema(description = "业务类型 water_elec/property_fee")
    private String bizType;

    @Schema(description = "业务类型文本")
    private String bizTypeText;

    @Schema(description = "收费规则名称")
    private String ruleName;

    @Schema(description = "收费项名称")
    private String feeItemType;

    @Schema(description = "账期")
    private String billMonth;

    @Schema(description = "应收金额")
    private BigDecimal amount;

    @Schema(description = "优惠抵扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "已缴金额")
    private BigDecimal paidAmount;

    @Schema(description = "未缴金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}