package com.gbi.platform.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 法定节假日配置实体：sys_holiday_config
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_holiday_config")
public class SysHolidayConfig extends BaseEntity {

    /** 所属公司ID，0=集团全局 */
    private Long companyId;

    /** 节假日日期 */
    private LocalDate holidayDate;

    /** 节假日名称 */
    private String holidayName;

    /** 类型 1法定假日 2调休日 3补班日 */
    private Integer holidayType;

    /** 是否为工作日 0否 1是（补班日） */
    private Integer isWorkday;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
