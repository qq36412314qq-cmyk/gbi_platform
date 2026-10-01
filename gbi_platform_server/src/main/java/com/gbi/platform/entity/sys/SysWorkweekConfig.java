package com.gbi.platform.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 休息日配置实体：sys_workweek_config
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_workweek_config")
public class SysWorkweekConfig extends BaseEntity {

    /** 所属公司ID，0=集团全局 */
    private Long companyId;

    /** 配置名称，如：双休、单休、做五休二 */
    private String configName;

    /** 休息日类型 1单休 2双休 3做五休二 4做六休一 5综合工时 */
    private Integer workweekType;

    /** 休息日模式，如：周六日、周日、周一 */
    private String restDayPattern;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
