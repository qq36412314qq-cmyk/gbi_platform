package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物业费月度账单返回
 *
 * @author gbi
 */
@Data
@Schema(description = "物业费月度账单返回")
public class PropertyFeeBillVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "铺位ID")
    private Long stallId;

    @Schema(description = "铺位编号")
    private String stallNumber;

    @Schema(description = "铺位名称")
    private String stallName;

    @Schema(description = "所属市场名称")
    private String stallMarketName;

    @Schema(description = "租赁分类名称")
    private String categoryName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "租户名称（由合同关联查询）")
    private String tenantName;

    @Schema(description = "是否已生成统一账单（biz_fee_bill，source_bill_id 关联）")
    private Boolean hasFeeBill;

    @Schema(description = "账单月份 yyyy-MM")
    private String billMonth;

    @Schema(description = "计费规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "收费类型ID")
    private Long feeItemId;

    @Schema(description = "收费类型名称")
    private String feeItemName;

    @Schema(description = "收费方式 1定额 2按面积")
    private Integer calcMode;

    @Schema(description = "收费方式文本")
    private String calcModeText;

    @Schema(description = "收费周期 0不使用 1按年 2按月 3按日")
    private Integer periodType;

    @Schema(description = "收费周期文本")
    private String periodTypeText;

    @Schema(description = "用量：定额=0，按面积=面积值")
    private BigDecimal usage;

    @Schema(description = "计费单价快照")
    private BigDecimal unitPrice;

    @Schema(description = "周期系数")
    private BigDecimal periodFactor;

    @Schema(description = "本条账单金额")
    private BigDecimal amount;

    @Schema(description = "缴费状态 0待缴 1已缴 2部分缴费")
    private Integer payStatus;

    @Schema(description = "缴费状态文本")
    private String payStatusText;

    @Schema(description = "缴费完成时间")
    private LocalDateTime payTime;

    @Schema(description = "关联应收应付计划ID")
    private Long planId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}