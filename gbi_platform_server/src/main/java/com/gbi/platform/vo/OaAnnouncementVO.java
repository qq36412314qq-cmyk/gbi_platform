package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA公告返回")
public class OaAnnouncementVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description = "主键ID") private Long id;
    @Schema(description = "所属子公司ID") private Long companyId;
    @Schema(description = "公告标题") private String title;
    @Schema(description = "公告内容") private String content;
    @Schema(description = "发布范围 1全员 2指定部门 3指定人员") private Integer publishType;
    @Schema(description = "发布范围文本") private String publishTypeText;
    @Schema(description = "发布状态 0草稿 1已发布 2已撤回") private Integer publishStatus;
    @Schema(description = "发布状态文本") private String publishStatusText;
    @Schema(description = "发布人用户ID") private Long publisherId;
    @Schema(description = "发布人姓名") private String publisherName;
    @Schema(description = "发布时间") private LocalDateTime publishTime;
    @Schema(description = "已读数量") private Integer readCount;
    @Schema(description = "总应读数量") private Integer totalTargetCount;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}
