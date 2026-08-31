package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织部门实体：sys_org
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    /** 所属子公司ID，0集团总公司 */
    private Long companyId;

    /** 上级组织ID */
    private Long parentId;

    /** 组织部门名称 */
    private String orgName;

    /** 组织类型：1集团 2子公司 3部门 */
    private Integer orgType;

    /** 排序 */
    private Integer sortOrder;

    /** 状态0禁用1启用 */
    private Integer status;
}
