package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电费月度账单返回（按收费类别分行）
 *
 * @author gbi
 */
@Data
@Schema(description = "水电费月度账单返回")
public class WaterElecBillVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

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

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "账单月份 yyyy-MM")
    private String billMonth;

    @Schema(description = "收费类别 2物业费 3水费 4电费")
    private Integer category;

    @Schema(description = "收费类别文本")
    private String categoryText;

    @Schema(description = "上期抄表读数")
    private BigDecimal prevMeterRead;

    @Schema(description = "用量（水/电为读数差）")
    private BigDecimal usage;

    @Schema(description = "计费单价快照")
    private BigDecimal unitPrice;

    @Schema(description = "账单总应收金额")
    private BigDecimal totalAmount;

    @Schema(description = "缴费状态 0待缴 1已缴 2部分缴费")
    private Integer payStatus;

    @Schema(description = "缴费状态文本")
    private String payStatusText;

    @Schema(description = "缴费完成时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
