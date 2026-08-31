package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA会议室返回")
public class OaMeetingRoomVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description = "主键ID") private Long id;
    @Schema(description = "所属子公司ID") private Long companyId;
    @Schema(description = "会议室名称") private String roomName;
    @Schema(description = "位置描述") private String location;
    @Schema(description = "容纳人数") private Integer capacity;
    @Schema(description = "设施配置JSON") private String facilities;
    @Schema(description = "状态 0停用 1启用") private Integer status;
    @Schema(description = "备注") private String remark;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}
