package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * OA会议室预约记录实体：oa_meeting_booking
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_meeting_booking")
public class OaMeetingBooking extends BaseEntity {

    /** 会议室ID */
    private Long roomId;

    /** 所属子公司ID */
    private Long companyId;

    /** 预约人用户ID */
    private Long bookerId;

    /** 预约人姓名 */
    private String bookerName;

    /** 会议主题 */
    private String meetingTitle;

    /** 会议日期 */
    private LocalDate meetingDate;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 参会人数 */
    private Integer attendeeCount;

    /** 参会人员ID集合JSON */
    private String attendeeIds;

    /** 备注 */
    private String remark;

    /** 预约状态 0已预约 1已取消 2已完成 */
    private Integer bookStatus;
}
