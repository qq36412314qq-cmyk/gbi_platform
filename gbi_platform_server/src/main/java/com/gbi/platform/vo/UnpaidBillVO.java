package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 未支付订单统一视图（聚合物业费账单 + 水电费账单）
 * 用于前端"未支付订单"页面展示，后续对接移动支付入口
 *
 * @author gbi
 */
@Data
@Schema(description = "未支付订单统一视图")
public class UnpaidBillVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账单主键ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "业务类型 property_fee=物业费 water_elec=水电费")
    private String businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "账单月份 yyyy-MM")
    private String billMonth;

    @Schema(description = "摊位ID")
    private Long stallId;

    @Schema(description = "摊位编号")
    private String stallNumber;

    @Schema(description = "摊位名称")
    private String stallName;

    @Schema(description = "所属市场名称")
    private String stallMarketName;

    @Schema(description = "租赁分类名称")
    private String categoryName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "缴费状态 0待缴 1已缴 2部分缴费")
    private Integer payStatus;

    @Schema(description = "缴费状态文本")
    private String payStatusText;

    @Schema(description = "缴费完成时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "源账单ID（water_elec_bill.id / property_fee_bill.id，用于缴费跳转）")

    private Long sourceBillId;
}
