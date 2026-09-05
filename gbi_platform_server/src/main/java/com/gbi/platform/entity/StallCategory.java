package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁分类实体：stall_category
 * 租赁标的分类（商铺/仓库/车位等），子公司可自定义增删
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_stall_category")
public class StallCategory extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 分类名称（商铺/仓库/车位等，可自定义） */
    private String categoryName;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
