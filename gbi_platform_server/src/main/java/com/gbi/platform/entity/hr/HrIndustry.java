package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 行业字典实体：sys_industry
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_industry")
public class HrIndustry extends BaseEntity {

    /** 行业编码 */
    private String industryCode;

    /** 行业名称 */
    private String industryName;

    /** 工伤保险行业基准费率(%) */
    private BigDecimal workInjuryRateBase;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
