package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 险种字典实体：sys_insurance_type
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_insurance_type")
public class HrInsuranceType extends BaseEntity {

    /** 险种编码，如 PENSION/MEDICAL/UNEMPLOYMENT/WORK_INJURY/MATERNITY/LONG_CARE */
    private String insuranceCode;

    /** 险种名称 */
    private String insuranceName;

    /** 分类 1法定五险 2补充福利 3试点险种 */
    private Integer insuranceType;

    /** 是否含个人缴纳部分 0否 1是 */
    private Integer personalShare;

    /** 是否含单位缴纳部分 0否 1是 */
    private Integer companyShare;

    /** 备注 */
    private String remark;
}
