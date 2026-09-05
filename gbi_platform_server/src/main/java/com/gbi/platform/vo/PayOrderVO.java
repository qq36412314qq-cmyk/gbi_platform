package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费单主表VO（finance_pay_order 列表展示）
 *
 * @author gbi
 */
@Data
@Schema(description = "缴费单")
public class PayOrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缴费单ID")
    private Long id;

    @Schema(description = "缴费单编号")
    private String payBillNo;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "来源类型 fee_bill/water_elec")
    private String sourceType;

    @Schema(description = "来源类型文本")
    private String sourceTypeText;

    @Schema(description = "源账单ID")
    private Long sourceId;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "铺位编号")
    private String stallNumber;

    @Schema(description = "铺位名称")
    private String stallName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "应收总额")
    private BigDecimal totalAmount;

    @Schema(description = "已缴金额")
    private BigDecimal paidAmount;

    @Schema(description = "未缴金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "缴费状态 0待缴 1部分缴费 2已缴 3已退费 4已冲红 5已作废")
    private Integer payStatus;

    @Schema(description = "缴费状态文本")
    private String payStatusText;

    @Schema(description = "缴费完成时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}