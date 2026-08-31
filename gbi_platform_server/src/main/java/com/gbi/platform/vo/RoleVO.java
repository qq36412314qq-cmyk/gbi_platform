package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色返回
 *
 * @author gbi
 */
@Data
@Schema(description = "角色信息")
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "所属子公司ID，0集团全局角色模板")
    private Long companyId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
