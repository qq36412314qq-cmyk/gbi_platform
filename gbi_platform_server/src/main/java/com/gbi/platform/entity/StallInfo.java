package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 铺位基础信息实体：stall_info
 * 租赁标的（商铺/仓库/车位等），分类可自定义（stall_category_id）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_stall_info")
public class StallInfo extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 关联市场ID */
    private Long marketId;

    /** 租赁分类ID（关联 stall_category，可自定义） */
    private Long stallCategoryId;

    /** 铺位编号 */
    private String stallNumber;

    /** 铺位名称 */
    private String stallName;

    /** 铺位面积(平方米) */
    private BigDecimal stallArea;

    /** 铺位状态 0空置 1已租 2欠费 3即将到期 */
    private Integer status;

    /** 铺位备注 */
    private String remark;
}
