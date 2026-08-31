package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统参数返回（对齐前端 ConfigVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "系统参数")
public class ConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "参数ID")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局")
    private Long companyId;

    @Schema(description = "参数key")
    private String configKey;

    @Schema(description = "参数值")
    private String configValue;

    @Schema(description = "参数显示名称")
    private String configName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
