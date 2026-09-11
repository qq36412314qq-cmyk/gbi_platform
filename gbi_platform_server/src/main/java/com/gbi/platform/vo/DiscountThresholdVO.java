package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 阈值配置视图
 *
 * @author gbi
 */
@Data
@Schema(description = "阈值配置视图")
public class DiscountThresholdVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "阈值ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务类型文本")
    private String bizTypeText;

    @Schema(description = "阈值类型")
    private Integer thresholdType;

    @Schema(description = "阈值类型文本")
    private String thresholdTypeText;

    @Schema(description = "阈值数值")
    private BigDecimal thresholdValue;

    @Schema(description = "是否需审批")
    private Integer requireAudit;

    @Schema(description = "是否需审批文本")
    private String requireAuditText;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人用户ID")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人用户ID")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}