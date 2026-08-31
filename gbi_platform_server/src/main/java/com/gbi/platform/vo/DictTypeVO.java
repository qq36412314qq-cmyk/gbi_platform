package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型返回（对齐前端 DictTypeVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "字典类型")
public class DictTypeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典类型ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String dictCode;

    @Schema(description = "字典类型名称")
    private String dictName;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
