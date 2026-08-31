package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租赁分类返回（商铺/仓库/车位等，子公司自定义）
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁分类返回")
public class CategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}