package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企微租户配置 VO（corpId 脱敏展示）
 *
 * @author gbi
 */
@Data
@Schema(description = "企微租户配置")
public class WecomTenantVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "公司ID，0=集团")
    private Long companyId;

    /**
     * corpId 脱敏：仅展示前4位+后4位，中间掩码
     * 编辑模式（WecomTenantEditDTO）传明文，查看模式返回掩码
     */
    @Schema(description = "企业微信企业ID（脱敏）")
    private String corpId;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
