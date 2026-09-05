package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 租赁铺位返回（含分类/市场名称与已绑定收费规则）
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁铺位返回")
public class StallVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "铺位ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "关联市场ID")
    private Long marketId;

    @Schema(description = "关联市场名称")
    private String marketName;

    @Schema(description = "租赁分类ID")
    private Long stallCategoryId;

    @Schema(description = "租赁分类名称")
    private String categoryName;

    @Schema(description = "铺位编号")
    private String stallNumber;

    @Schema(description = "铺位名称")
    private String stallName;

    @Schema(description = "铺位面积(平方米)")
    private BigDecimal stallArea;

    @Schema(description = "铺位状态 0空置 1已租 2欠费 3即将到期")
    private Integer status;

    @Schema(description = "铺位状态文本")
    private String statusText;

    @Schema(description = "铺位备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "已绑定收费规则（分页列表组装，空=未绑定）")
    private List<StallRuleRelVO> feeRules;
}