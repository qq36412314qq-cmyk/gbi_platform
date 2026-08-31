package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA会议室预约返回")
public class OaMeetingBookingVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description = "主键ID") private Long id;
    @Schema(description = "所属子公司ID") private Long companyId;
    @Schema(description = "会议室ID") private Long roomId;
    @Schema(description = "会议室名称") private String roomName;
    @Schema(description = "预约人用户ID") private Long bookerId;
    @Schema(description = "预约人姓名") private String bookerName;
    @Schema(description = "会议主题") private String meetingTitle;
    @Schema(description = "会议日期") private LocalDate meetingDate;
    @Schema(description = "开始时间") private LocalTime startTime;
    @Schema(description = "结束时间") private LocalTime endTime;
    @Schema(description = "参会人数") private Integer attendeeCount;
    @Schema(description = "预约状态 0已预约 1已取消 2已完成") private Integer bookStatus;
    @Schema(description = "预约状态文本") private String bookStatusText;
    @Schema(description = "备注") private String remark;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}
