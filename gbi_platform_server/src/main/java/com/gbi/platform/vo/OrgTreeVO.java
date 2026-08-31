package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织树返回（对齐前端 OrgVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "组织树节点")
public class OrgTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "上级组织ID")
    private Long parentId;

    @Schema(description = "组织名称")
    private String orgName;

    @Schema(description = "组织类型：1集团 2子公司 3部门")
    private Integer orgType;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态0禁用1启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子节点")
    private List<OrgTreeVO> children = new ArrayList<>();
}
