package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电物业缴费记录返回（对齐前端 WaterElecPayRecordVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "水电物业缴费记录返回")
public class WaterElecPayRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "关联账单ID")
    private Long billId;

    @Schema(description = "摊位ID")
    private Long stallId;

    // === 快照字段 ===
    @Schema(description = "摊位编号快照")
    private String stallNumber;

    @Schema(description = "摊位名称快照")
    private String stallName;

    @Schema(description = "所属市场名称快照")
    private String stallMarketName;

    @Schema(description = "租赁分类名称快照")
    private String categoryName;

    @Schema(description = "商户名称快照")
    private String merchantName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "缴费/退费金额")
    private BigDecimal payAmount;

    @Schema(description = "支付渠道 1微信 2支付宝 3线下现金")
    private Integer payType;

    @Schema(description = "支付渠道文本")
    private String payTypeText;

    @Schema(description = "记录类型 1缴费 2退费")
    private Integer recordType;

    @Schema(description = "记录类型文本")
    private String recordTypeText;

    @Schema(description = "退费状态 0未退 1已退")
    private Integer refundStatus;

    @Schema(description = "退费完成时间")
    private LocalDateTime refundTime;

    @Schema(description = "退费记录ID（指向原缴费记录）")
    private Long refundRecordId;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "关联财务流水单号")
    private String flowNo;

    @Schema(description = "操作人姓名")
    private String createByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
