package com.gbi.platform.hr.entity;

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
    private Long deptId;
    private Integer status;
    private String remark;
}
