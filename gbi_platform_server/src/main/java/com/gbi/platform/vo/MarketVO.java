package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 市场档案返回视图
 *
 * @author gbi
 */
@Data
@Schema(description = "市场档案返回视图")
public class MarketVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "市场ID")
    private Long id;

    @Schema(description = "所属子公司ID，0集团模板")
    private Long companyId;

    @Schema(description = "市场名称")
    private String marketName;

    @Schema(description = "市场地址")
    private String marketAddress;

    @Schema(description = "市场联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}