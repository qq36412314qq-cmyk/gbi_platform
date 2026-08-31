package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置实体：sys_config
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /** 所属子公司ID，0集团全局参数 */
    private Long companyId;

    /** 参数key */
    private String configKey;

    /** 参数值 */
    private String configValue;

    /** 参数显示名称 */
    private String configName;

    /** 备注说明 */
    private String remark;
}
