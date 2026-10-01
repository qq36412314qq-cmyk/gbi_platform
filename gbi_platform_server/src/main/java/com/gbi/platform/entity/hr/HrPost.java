package com.gbi.platform.entity.hr;

import com.gbi.platform.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_post")
public class HrPost extends BaseEntity {
    private Long companyId;
    private String postName;
    private String postCode;
    private String postLevel;
    /** 岗位默认休息日配置ID */
    private Long workweekConfigId;
    /** 班次类型 1标准工时 2早班 3晚班 4夜班 5综合工时 */
    private Integer shiftType;
    private Long deptId;
    private Integer status;
    private String remark;
}
