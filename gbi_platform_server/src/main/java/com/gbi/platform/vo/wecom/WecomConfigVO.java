package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企微配置列表 VO（AES Key 等敏感字段脱敏展示）
 *
 * @author gbi
 */
@Data
@Schema(description = "企微配置项")
public class WecomConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "所属公司ID，0=集团全局")
    private Long companyId;

    @Schema(description = "配置Key")
    private String configKey;

    @Schema(description = "配置名称")
    private String configName;

    /**
     * 配置值脱敏：AES Key 等敏感字段展示为掩码形式
     * 由 WecomConfigService 在转换时自动处理
     */
    @Schema(description = "配置值（敏感字段已脱敏）")
    private String configValue;

    @Schema(description = "备注")
    private String remark;
}
