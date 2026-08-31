package com.gbi.platform.hr.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
@Schema(description="岗位新增/编辑入参")
@Data public class PostDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="岗位ID（编辑时必填）") private Long id;
    @Schema(description="岗位名称",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="岗位名称不能为空") @Size(max=64) private String postName;
    @Schema(description="岗位编码",requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="岗位编码不能为空") @Size(max=32) private String postCode;
    @Schema(description="岗位职级") @Size(max=32) private String postLevel;
    @Schema(description="所属部门ID") private Long deptId;
    @Schema(description="状态 0禁用 1启用") private Integer status;
    @Schema(description="备注") @Size(max=500) private String remark;
}
