package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 市场档案实体：market_info
 * 园区/商圈维度，摊位（stall_info.market_id）与市场地图（market_map.market_id）统一关联本表
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("market_info")
public class MarketInfo extends BaseEntity {

    /** 所属子公司ID，0集团模板 */
    private Long companyId;

    /** 市场名称（如汽车城A区市场） */
    private String marketName;

    /** 市场地址 */
    private String marketAddress;

    /** 市场联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}