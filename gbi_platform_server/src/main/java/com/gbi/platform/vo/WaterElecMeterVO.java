package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电表设备返回（对齐前端 WaterElecMeterVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "水电表设备返回")
public class WaterElecMeterVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "绑定摊位ID")
    private Long stallId;

    @Schema(description = "绑定摊位编号")
    private String stallNumber;

    @Schema(description = "绑定摊位名称")
    private String stallName;

    @Schema(description = "绑定摊位所属市场ID（编辑回显联动用）")
    private Long stallMarketId;

    @Schema(description = "绑定摊位所属市场名称")
    private String stallMarketName;

    @Schema(description = "绑定摊位租赁分类ID（编辑回显联动用）")
    private Long stallCategoryId;

    @Schema(description = "绑定摊位租赁分类名称")
    private String categoryName;

    @Schema(description = "智能表设备编号")
    private String meterNo;

    @Schema(description = "表类型 1水表 2电表")
    private Integer meterType;

    @Schema(description = "表类型文本")
    private String meterTypeText;

    @Schema(description = "物联网网关编码")
    private String gatewayCode;

    @Schema(description = "当前读数")
    private BigDecimal currentRead;

    @Schema(description = "账户余额")
    private BigDecimal balanceAmount;

    @Schema(description = "设备状态 0断电 1通电正常")
    private Integer status;

    @Schema(description = "设备状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
