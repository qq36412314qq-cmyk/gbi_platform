package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体：sys_dict_type（集团全局）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /** 字典类型编码 */
    private String dictCode;

    /** 字典类型名称 */
    private String dictName;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
