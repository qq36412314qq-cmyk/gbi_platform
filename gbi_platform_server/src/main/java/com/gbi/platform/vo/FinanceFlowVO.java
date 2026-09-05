package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务流水返回（对齐前端 FinanceFlowVO，只读不可编辑）
 *
 * @author gbi
 */
@Data
@Schema(description = "财务流水返回")
public class FinanceFlowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "业务类型 rent/water_elec/deposit/marketing")
    private String businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "关联业务单据ID")
    private String billId;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "铺位ID")
    private Long stallId;

    // === 物业快照 ===
    @Schema(description = "铺位编号快照")
    private String stallNumber;

    @Schema(description = "铺位名称快照")
    private String stallName;

    @Schema(description = "所属市场名称快照")
    private String stallMarketName;

    @Schema(description = "租赁分类名称快照")
    private String categoryName;

    @Schema(description = "商户名称快照")
    private String merchantName;

    // === 非物业快照（缴费人/合同） ===
    @Schema(description = "缴费人姓名")
    private String payerName;

    @Schema(description = "缴费人手机号")
    private String payerPhone;

    @Schema(description = "缴费人公司名称")
    private String payerCompanyName;

    @Schema(description = "缴费人类型 1个人 2企业")
    private Integer payerType;

    @Schema(description = "缴费人类型文本")
    private String payerTypeText;

    @Schema(description = "关联合同编号")
    private String contractNo;

    @Schema(description = "关联合同ID")
    private Long contractId;

    @Schema(description = "应收原价金额")
    private BigDecimal originalAmount;

    @Schema(description = "优惠抵扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "实际实收金额")
    private BigDecimal realAmount;

    @Schema(description = "支付渠道 1微信 2支付宝 3线下现金")
    private Integer payType;

    @Schema(description = "支付渠道文本")
    private String payTypeText;

    @Schema(description = "流水类型 1收入 2支出退费")
    private Integer flowType;

    @Schema(description = "流水类型文本")
    private String flowTypeText;

    @Schema(description = "冲红/作废状态 1正常 2冲红中 3已冲红 4已作废")
    private Integer flowStatus;

    @Schema(description = "冲红/作废状态文本")
    private String flowStatusText;

    @Schema(description = "冲红反向流水ID")
    private Long redFlushFlowId;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "第三方支付交易号")
    private String tradeNo;

    @Schema(description = "财务流水单号")
    private String flowNo;

    @Schema(description = "流水备注说明")
    private String remark;

    @Schema(description = "操作人用户ID")
    private Long createBy;

    @Schema(description = "流水生成时间")
    private LocalDateTime createTime;
}
