package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.*;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 城市字典实体：sys_city
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_city")
public class HrCity extends BaseEntity {

    /** 城市编码，如 BJ/GZ/SZ */
    private String cityCode;

    /** 城市名称，如 北京/广州/深圳 */
    private String cityName;

    /** 省份 */
    private String province;

    /** 排序 */
    private Integer sort;

    /** 状态 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
