package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 社保公积金参数配置实体：hr_social_param_config
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_social_param_config")
public class HrSocialParamConfig extends BaseEntity {

    /** 所属公司ID，0=集团全局 */
    private Long companyId;

    /** 城市编码 */
    private String cityCode;

    /** 险种编码 */
    private String insuranceCode;

    /** 行业编码，仅工伤保险等按行业浮动险种填写 */
    private String industryCode;

    /** 生效起始日期 */
    private LocalDate periodStart;

    /** 生效截止日期，NULL表示持续有效 */
    private LocalDate periodEnd;

    /** 缴费基数下限 */
    private BigDecimal baseMin;

    /** 缴费基数上限 */
    private BigDecimal baseMax;

    /** 个人缴纳比例(%) */
    private BigDecimal personalRate;

    /** 单位缴纳比例(%) */
    private BigDecimal companyRate;

    /** 是否当前有效 0否 1是 */
    private Integer isActive;

    /** 备注 */
    private String remark;
}
