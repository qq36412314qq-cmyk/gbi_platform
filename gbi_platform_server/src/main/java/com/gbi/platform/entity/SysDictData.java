package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体：sys_dict_data（集团全局）
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /** 字典类型ID */
    private Long dictTypeId;

    /** 字典显示文本 */
    private String dictValue;

    /** 字典存储值 */
    private String dictKey;

    /** 排序 */
    private Integer sortOrder;

    /** 状态0禁用1启用 */
    private Integer status;
}
