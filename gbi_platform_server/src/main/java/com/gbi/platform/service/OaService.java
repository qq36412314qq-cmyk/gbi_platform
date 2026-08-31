package com.gbi.platform.service;

import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.OaLeaveApplyVO;
import com.gbi.platform.vo.OaMeetingRoomVO;
import com.gbi.platform.vo.OaMeetingBookingVO;
import com.gbi.platform.vo.OaAnnouncementVO;
import com.gbi.platform.vo.OaClockRecordVO;
import com.gbi.platform.vo.OaWorkReportVO;

import java.util.List;

public interface OaService {

    /* ==================== 请假管理 ==================== */
    PageVO<OaLeaveApplyVO> pageLeaveApply(Long pageNum, Long pageSize, Integer leaveType, Integer applyStatus, Long applyUserId);
    OaLeaveApplyVO getLeaveApply(Long id);
    Long submitLeaveApply(Long companyId, Long applyUserId, String applyUserName, Integer leaveType,
                          java.time.LocalDate startDate, java.time.LocalDate endDate,
                          java.math.BigDecimal leaveDays, String reason, String remark);
    void cancelLeaveApply(Long id);

    /* ==================== 会议室管理 ==================== */
    PageVO<OaMeetingRoomVO> pageMeetingRoom(Long pageNum, Long pageSize, Long companyId, Integer status);
    void addMeetingRoom(Long companyId, String roomName, String location, Integer capacity, String facilities, String remark);
    void updateMeetingRoom(Long id, String roomName, String location, Integer capacity, String facilities, Integer status, String remark);
    void deleteMeetingRoom(Long id);

    /* ==================== 会议室预约 ==================== */
    PageVO<OaMeetingBookingVO> pageMeetingBooking(Long pageNum, Long pageSize, Long roomId, Long bookerId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    Long bookMeetingRoom(Long companyId, Long bookerId, String bookerName, Long roomId,
                         java.time.LocalDate meetingDate, java.time.LocalTime startTime,
                         java.time.LocalTime endTime, String meetingTitle,
                         Integer attendeeCount, String attendeeIds, String remark);
    void cancelMeetingBooking(Long id);

    /* ==================== 公告管理 ==================== */
    PageVO<OaAnnouncementVO> pageAnnouncement(Long pageNum, Long pageSize, Long companyId, Integer publishStatus, Long publisherId);
    OaAnnouncementVO getAnnouncement(Long id);
    Long createAnnouncement(Long companyId, String title, String content, Integer publishType,
                            String targetDeptIds, String targetUserIds,
                            Long publisherId, String publisherName, String remark);
    void publishAnnouncement(Long id);
    void recallAnnouncement(Long id);
    void markAnnouncementRead(Long announcementId, Long userId);
    boolean isAnnouncementRead(Long announcementId, Long userId);
    java.util.Map<String, Object> getAnnouncementReadStats(Long announcementId);

    /* ==================== 考勤打卡 ==================== */
    PageVO<OaClockRecordVO> pageClockRecord(Long pageNum, Long pageSize, Long companyId, Long userId,
                                             java.time.LocalDate startDate, java.time.LocalDate endDate, Integer clockType);
    Long clockIn(Long companyId, Long userId, String userName, Integer clockType,
                 java.math.BigDecimal lat, java.math.BigDecimal lng, String addr, Integer deviceType);

    /* ==================== 工作汇报 ==================== */
    PageVO<OaWorkReportVO> pageWorkReport(Long pageNum, Long pageSize, Long companyId, Integer reportType,
                                           Long submitUserId, Integer reportStatus, String reportPeriod);
    OaWorkReportVO getWorkReport(Long id);
    Long submitWorkReport(Long companyId, Long submitUserId, String submitUserName,
                          Integer reportType, String reportPeriod, String reportContent, String remark);
    void cancelWorkReport(Long id);
}
