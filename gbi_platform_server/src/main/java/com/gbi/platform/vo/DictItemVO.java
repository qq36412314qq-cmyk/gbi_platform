package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据返回（对齐前端 DictItemVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "字典数据项")
public class DictItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典数据ID")
    private Long id;

    @Schema(description = "字典类型ID")
    private Long dictTypeId;

    @Schema(description = "字典显示文本")
    private String dictValue;

    @Schema(description = "字典存储值")
    private String dictKey;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态0禁用1启用")
    private Integer status;
}
