package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义收费类型实体：biz_fee_item
 * 收费类型（租金/物业费/水费/电费/押金/其他等），子公司可配置，company_id 自动隔离
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_fee_item")
public class FeeItem extends BaseEntity {

    /** 所属子公司ID，0集团模板 */
    private Long companyId;

    /** 收费类型名称（租金/物业费/水费/电费/押金/其他） */
    private String feeItemName;

    /** 收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他（编码稳定，账单生成按编码匹配） */
    private Integer categoryType;

    /** 计量单位（元/月、元/平米、元/吨等） */
    private String calcUnit;

    /** 收费类型备注 */
    private String remark;
}