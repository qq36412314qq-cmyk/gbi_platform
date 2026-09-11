package com.gbi.platform.controller.oa;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.service.OaService;
import com.gbi.platform.vo.*;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Tag(name = "OA办公模块")
@RestController
@RequestMapping("/oa")
@RequiredArgsConstructor
public class OaController {

    private final OaService oaService;

    /* ==================== 请假管理 ==================== */
    @Operation(summary = "请假申请分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_LEAVE_LIST,'')")
    @GetMapping("/leave/page")
    public Result<PageVO<OaLeaveApplyVO>> pageLeave(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer leaveType,
            @RequestParam(required = false) Integer applyStatus,
            @RequestParam(required = false) Long applyUserId) {
        return Result.success(oaService.pageLeaveApply(pageNum, pageSize, leaveType, applyStatus, applyUserId));
    }

    @Operation(summary = "请假申请详情")
    @GetMapping("/leave/{id}")
    public Result<OaLeaveApplyVO> getLeave(@PathVariable Long id) {
        return Result.success(oaService.getLeaveApply(id));
    }

    @Operation(summary = "提交请假申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_LEAVE_ADD,'')")
    @PostMapping("/leave/submit")
    public Result<Long> submitLeave(@RequestBody SubmitLeaveDTO dto) {
        return Result.success(oaService.submitLeaveApply(dto.getCompanyId(), dto.getApplyUserId(), dto.getApplyUserName(),
                dto.getLeaveType(), dto.getStartDate(), dto.getEndDate(), dto.getLeaveDays(), dto.getReason(), dto.getRemark()));
    }

    @Operation(summary = "撤销请假申请")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_LEAVE_CANCEL,'')")
    @PostMapping("/leave/cancel/{id}")
    public Result<Void> cancelLeave(@PathVariable Long id) {
        oaService.cancelLeaveApply(id);
        return Result.success();
    }

    /* ==================== 会议室管理 ==================== */
    @Operation(summary = "会议室分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_ROOM_LIST,'')")
    @GetMapping("/meeting/room/page")
    public Result<PageVO<OaMeetingRoomVO>> pageRoom(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(oaService.pageMeetingRoom(pageNum, pageSize, null, status));
    }

    @Operation(summary = "新增会议室")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_ROOM_ADD,'')")
    @PostMapping("/meeting/room/add")
    public Result<Void> addRoom(@RequestBody AddMeetingRoomDTO dto) {
        oaService.addMeetingRoom(dto.getCompanyId(), dto.getRoomName(), dto.getLocation(),
                dto.getCapacity(), dto.getFacilities(), dto.getRemark());
        return Result.success();
    }

    @Operation(summary = "编辑会议室")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_ROOM_EDIT,'')")
    @PostMapping("/meeting/room/update")
    public Result<Void> updateRoom(@RequestBody UpdateMeetingRoomDTO dto) {
        oaService.updateMeetingRoom(dto.getId(), dto.getRoomName(), dto.getLocation(),
                dto.getCapacity(), dto.getFacilities(), dto.getStatus(), dto.getRemark());
        return Result.success();
    }

    @Operation(summary = "切换会议室启用/停用状态")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_ROOM_EDIT,'')" )
    @PostMapping("/meeting/room/toggle/{id}")
    public Result<Void> toggleRoom(@PathVariable Long id) {
        oaService.toggleMeetingRoomStatus(id);
        return Result.success();
    }

    @Operation(summary = "删除会议室")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_ROOM_DELETE,'')")
    @PostMapping("/meeting/room/delete/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        oaService.deleteMeetingRoom(id);
        return Result.success();
    }

    /* ==================== 会议室预约 ==================== */
    @Operation(summary = "会议室预约分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_BOOKING_LIST,'')")
    @GetMapping("/meeting/booking/page")
    public Result<PageVO<OaMeetingBookingVO>> pageBooking(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long bookerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;
        return Result.success(oaService.pageMeetingBooking(pageNum, pageSize, roomId, bookerId, start, end));
    }

    @Operation(summary = "预约会议室")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_BOOKING_ADD,'')")
    @PostMapping("/meeting/booking/book")
    public Result<Long> bookRoom(@RequestBody BookMeetingRoomDTO dto) {
        return Result.success(oaService.bookMeetingRoom(dto.getCompanyId(), dto.getBookerId(), dto.getBookerName(),
                dto.getRoomId(), dto.getMeetingDate(), dto.getStartTime(), dto.getEndTime(),
                dto.getMeetingTitle(), dto.getAttendeeCount(), dto.getAttendeeIds(), dto.getRemark()));
    }

    @Operation(summary = "取消预约")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_MEETING_BOOKING_CANCEL,'')")
    @PostMapping("/meeting/booking/cancel/{id}")
    public Result<Void> cancelBooking(@PathVariable Long id) {
        oaService.cancelMeetingBooking(id);
        return Result.success();
    }

    /* ==================== 公告管理 ==================== */
    @Operation(summary = "公告分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_ANNOUNCEMENT_LIST,'')")
    @GetMapping("/announcement/page")
    public Result<PageVO<OaAnnouncementVO>> pageAnnouncement(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer publishStatus) {
        return Result.success(oaService.pageAnnouncement(pageNum, pageSize, null, publishStatus, null));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/announcement/{id}")
    public Result<OaAnnouncementVO> getAnnouncement(@PathVariable Long id) {
        return Result.success(oaService.getAnnouncement(id));
    }

    @Operation(summary = "创建公告")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_ANNOUNCEMENT_ADD,'')")
    @PostMapping("/announcement/create")
    public Result<Long> createAnnouncement(@RequestBody CreateAnnouncementDTO dto) {
        return Result.success(oaService.createAnnouncement(dto.getCompanyId(), dto.getTitle(), dto.getContent(),
                dto.getPublishType(), dto.getTargetDeptIds(), dto.getTargetUserIds(),
                dto.getPublisherId(), dto.getPublisherName(), dto.getRemark()));
    }

    @Operation(summary = "发布公告")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_ANNOUNCEMENT_PUBLISH,'')")
    @PostMapping("/announcement/publish/{id}")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        oaService.publishAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "撤回公告")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_ANNOUNCEMENT_RECALL,'')")
    @PostMapping("/announcement/recall/{id}")
    public Result<Void> recallAnnouncement(@PathVariable Long id) {
        oaService.recallAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "标记已读")
    @PostMapping("/announcement/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        oaService.markAnnouncementRead(id, null);
        return Result.success();
    }

    @Operation(summary = "公告阅读统计")
    @GetMapping("/announcement/{id}/read-stats")
    public Result<Map<String, Object>> getReadStats(@PathVariable Long id) {
        return Result.success(oaService.getAnnouncementReadStats(id));
    }

    /* ==================== 考勤打卡 ==================== */
    @Operation(summary = "考勤记录分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_CLOCK_LIST,'')")
    @GetMapping("/clock/page")
    public Result<PageVO<OaClockRecordVO>> pageClock(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer clockType) {
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
          LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;
        return Result.success(oaService.pageClockRecord(pageNum, pageSize, null, userId, start, end, clockType));
    }

    @Operation(summary = "打卡")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_CLOCK_ADD,'')")
    @PostMapping("/clock/in")
    public Result<Long> clockIn(@RequestBody ClockInDTO dto) {
        return Result.success(oaService.clockIn(dto.getCompanyId(), dto.getUserId(), dto.getUserName(),
                dto.getClockType(), dto.getLocationLat(), dto.getLocationLng(), dto.getLocationAddr(), dto.getDeviceType()));
    }

    /* ==================== 工作汇报 ==================== */
    @Operation(summary = "工作汇报分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_WORK_REPORT_LIST,'')")
    @GetMapping("/work-report/page")
    public Result<PageVO<OaWorkReportVO>> pageWorkReport(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) Long submitUserId,
            @RequestParam(required = false) Integer reportStatus,
            @RequestParam(required = false) String reportPeriod) {
        return Result.success(oaService.pageWorkReport(pageNum, pageSize, null, reportType, submitUserId, reportStatus, reportPeriod));
    }

    @Operation(summary = "工作汇报详情")
    @GetMapping("/work-report/{id}")
    public Result<OaWorkReportVO> getWorkReport(@PathVariable Long id) {
        return Result.success(oaService.getWorkReport(id));
    }

    @Operation(summary = "提交工作汇报")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_WORK_REPORT_ADD,'')")
    @PostMapping("/work-report/submit")
    public Result<Long> submitWorkReport(@RequestBody SubmitWorkReportDTO dto) {
        return Result.success(oaService.submitWorkReport(dto.getCompanyId(), dto.getSubmitUserId(), dto.getSubmitUserName(),
                dto.getReportType(), dto.getReportPeriod(), dto.getReportContent(), dto.getRemark()));
    }

    @Operation(summary = "撤销工作汇报")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).OA_WORK_REPORT_CANCEL,'')")
    @PostMapping("/work-report/cancel/{id}")
    public Result<Void> cancelWorkReport(@PathVariable Long id) {
        oaService.cancelWorkReport(id);
        return Result.success();
    }

    /* ==================== DTOs ==================== */
    public static class SubmitLeaveDTO {
        private Long companyId, applyUserId; private String applyUserName;
        private Integer leaveType; private LocalDate startDate, endDate;
        private BigDecimal leaveDays; private String reason, remark;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public Long getApplyUserId() { return applyUserId; } public void setApplyUserId(Long a) { applyUserId=a; }
        public String getApplyUserName() { return applyUserName; } public void setApplyUserName(String a) { applyUserName=a; }
        public Integer getLeaveType() { return leaveType; } public void setLeaveType(Integer l) { leaveType=l; }
        public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate s) { startDate=s; }
        public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate e) { endDate=e; }
        public BigDecimal getLeaveDays() { return leaveDays; } public void setLeaveDays(BigDecimal l) { leaveDays=l; }
        public String getReason() { return reason; } public void setReason(String r) { reason=r; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
    public static class AddMeetingRoomDTO {
        private Long companyId; private String roomName, location, facilities, remark;
        private Integer capacity;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public String getRoomName() { return roomName; } public void setRoomName(String r) { roomName=r; }
        public String getLocation() { return location; } public void setLocation(String l) { location=l; }
        public Integer getCapacity() { return capacity; } public void setCapacity(Integer c) { capacity=c; }
        public String getFacilities() { return facilities; } public void setFacilities(String f) { facilities=f; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
    public static class UpdateMeetingRoomDTO {
        private Long id; private String roomName, location, facilities, remark;
        private Integer capacity, status;
        public Long getId() { return id; } public void setId(Long i) { id=i; }
        public String getRoomName() { return roomName; } public void setRoomName(String r) { roomName=r; }
        public String getLocation() { return location; } public void setLocation(String l) { location=l; }
        public Integer getCapacity() { return capacity; } public void setCapacity(Integer c) { capacity=c; }
        public String getFacilities() { return facilities; } public void setFacilities(String f) { facilities=f; }
        public Integer getStatus() { return status; } public void setStatus(Integer s) { status=s; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
    public static class BookMeetingRoomDTO {
        private Long companyId, bookerId, roomId; private String bookerName, meetingTitle, attendeeIds, remark;
        private LocalDate meetingDate; private LocalTime startTime, endTime; private Integer attendeeCount;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public Long getBookerId() { return bookerId; } public void setBookerId(Long b) { bookerId=b; }
        public String getBookerName() { return bookerName; } public void setBookerName(String b) { bookerName=b; }
        public Long getRoomId() { return roomId; } public void setRoomId(Long r) { roomId=r; }
        public String getMeetingTitle() { return meetingTitle; } public void setMeetingTitle(String m) { meetingTitle=m; }
        public LocalDate getMeetingDate() { return meetingDate; } public void setMeetingDate(LocalDate m) { meetingDate=m; }
        public LocalTime getStartTime() { return startTime; } public void setStartTime(LocalTime s) { startTime=s; }
        public LocalTime getEndTime() { return endTime; } public void setEndTime(LocalTime e) { endTime=e; }
        public Integer getAttendeeCount() { return attendeeCount; } public void setAttendeeCount(Integer a) { attendeeCount=a; }
        public String getAttendeeIds() { return attendeeIds; } public void setAttendeeIds(String a) { attendeeIds=a; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
    public static class CreateAnnouncementDTO {
        private Long companyId, publisherId; private String title, content, targetDeptIds, targetUserIds, remark;
        private Integer publishType; private String publisherName;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public String getTitle() { return title; } public void setTitle(String t) { title=t; }
        public String getContent() { return content; } public void setContent(String c) { content=c; }
        public Integer getPublishType() { return publishType; } public void setPublishType(Integer p) { publishType=p; }
        public String getTargetDeptIds() { return targetDeptIds; } public void setTargetDeptIds(String t) { targetDeptIds=t; }
        public String getTargetUserIds() { return targetUserIds; } public void setTargetUserIds(String t) { targetUserIds=t; }
        public Long getPublisherId() { return publisherId; } public void setPublisherId(Long p) { publisherId=p; }
        public String getPublisherName() { return publisherName; } public void setPublisherName(String p) { publisherName=p; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
    public static class ClockInDTO {
        private Long companyId, userId; private String userName;
        private Integer clockType, deviceType;
        private java.math.BigDecimal locationLat, locationLng; private String locationAddr;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public Long getUserId() { return userId; } public void setUserId(Long u) { userId=u; }
        public String getUserName() { return userName; } public void setUserName(String u) { userName=u; }
        public Integer getClockType() { return clockType; } public void setClockType(Integer c) { clockType=c; }
        public java.math.BigDecimal getLocationLat() { return locationLat; } public void setLocationLat(java.math.BigDecimal l) { locationLat=l; }
        public java.math.BigDecimal getLocationLng() { return locationLng; } public void setLocationLng(java.math.BigDecimal l) { locationLng=l; }
        public String getLocationAddr() { return locationAddr; } public void setLocationAddr(String l) { locationAddr=l; }
        public Integer getDeviceType() { return deviceType; } public void setDeviceType(Integer d) { deviceType=d; }
    }
    public static class SubmitWorkReportDTO {
        private Long companyId, submitUserId; private String submitUserName, reportContent, remark;
        private Integer reportType; private String reportPeriod;
        public Long getCompanyId() { return companyId; } public void setCompanyId(Long c) { companyId=c; }
        public Long getSubmitUserId() { return submitUserId; } public void setSubmitUserId(Long s) { submitUserId=s; }
        public String getSubmitUserName() { return submitUserName; } public void setSubmitUserName(String s) { submitUserName=s; }
        public Integer getReportType() { return reportType; } public void setReportType(Integer r) { reportType=r; }
        public String getReportPeriod() { return reportPeriod; } public void setReportPeriod(String r) { reportPeriod=r; }
        public String getReportContent() { return reportContent; } public void setReportContent(String r) { reportContent=r; }
        public String getRemark() { return remark; } public void setRemark(String r) { remark=r; }
    }
}
