package com.gbi.platform.hr.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial; import java.io.Serializable;
import java.time.LocalDateTime;
@Schema(description="岗位视图")
@Data public class HrPostVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description="主键ID") private Long id;
    @Schema(description="公司ID") private Long companyId;
    @Schema(description="岗位名称") private String postName;
    @Schema(description="岗位编码") private String postCode;
    @Schema(description="岗位职级") private String postLevel;
    @Schema(description="所属部门ID") private Long deptId;
    @Schema(description="状态 0禁用 1启用") private Integer status;
    @Schema(description="状态文本") private String statusText;
    @Schema(description="备注") private String remark;
    @Schema(description="创建时间") private LocalDateTime createTime;
}
