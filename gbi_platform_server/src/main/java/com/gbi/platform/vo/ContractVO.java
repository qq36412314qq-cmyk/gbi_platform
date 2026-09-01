package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租赁合同返回（含租户名称、摊位编号、分类名称、所属市场、收费规则周期）
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁合同返回")
public class ContractVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同ID")
    private Long id;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "摊位ID")
    private Long stallId;

    @Schema(description = "摊位编号")
    private String stallNumber;

    @Schema(description = "摊位名称")
    private String stallName;

    @Schema(description = "租赁分类名称")
    private String categoryName;

    @Schema(description = "所属市场名称")
    private String marketName;

    @Schema(description = "租金金额")
    private BigDecimal rentAmount;

    @Schema(description = "押金金额")
    private BigDecimal depositAmount;

    @Schema(description = "租赁开始日期")
    private LocalDate startTime;

    @Schema(description = "租赁到期日期")
    private LocalDate endTime;

    @Schema(description = "合同状态 1生效中 2已退租 3已到期")
    private Integer contractStatus;

    @Schema(description = "合同状态文本")
    private String contractStatusText;

    @Schema(description = "合同附件OSS地址")
    private String attachmentUrl;

    @Schema(description = "合同备注")
    private String remark;

    @Schema(description = "租金收费规则周期类型 0不使用 1按年 2按月 3按日")
    private Integer rentPeriodType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}