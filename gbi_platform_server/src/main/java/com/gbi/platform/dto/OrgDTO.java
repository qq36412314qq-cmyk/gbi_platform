package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织新增/编辑入参（对齐前端 OrgDTO，id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "组织入参")
public class OrgDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID（编辑时必填）")
    private Long id;

    @Schema(description = "上级组织ID，0顶级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "上级组织不能为空")
    @Min(value = 0, message = "上级组织ID非法")
    private Long parentId;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "组织名称不能为空")
    @Size(max = 128, message = "组织名称不能超过128字符")
    private String orgName;

    @Schema(description = "组织类型：1集团 2子公司 3部门", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "组织类型不能为空")
    @Min(value = 1, message = "组织类型非法")
    @Max(value = 3, message = "组织类型非法")
    private Integer orgType;

    @Schema(description = "所属子公司ID，后端自动计算，前端禁止传参")
    private Long companyId;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序号最小为0")
    @Max(value = 9999, message = "排序号最大为9999")
    private Integer sortOrder;

    @Schema(description = "状态0禁用1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
