package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.*;
import com.gbi.platform.mapper.*;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.OaService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OaServiceImpl implements OaService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter APPLY_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OaLeaveApplyMapper leaveApplyMapper;
    private final OaMeetingRoomMapper meetingRoomMapper;
    private final OaMeetingBookingMapper meetingBookingMapper;
    private final OaAnnouncementMapper announcementMapper;
    private final OaAnnouncementReadMapper announcementReadMapper;
    private final OaClockRecordMapper clockRecordMapper;
    private final OaWorkReportMapper workReportMapper;
    private final FlowEngineService flowEngineService;
    private final AuditLogUtil auditLogUtil;
    

    /* ==================== 请假管理 ==================== */

    @Override
    public PageVO<OaLeaveApplyVO> pageLeaveApply(Long pageNum, Long pageSize, Integer leaveType, Integer applyStatus, Long applyUserId) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaLeaveApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaLeaveApply> wrapper = new LambdaQueryWrapper<OaLeaveApply>()
                .eq(OaLeaveApply::getCompanyId, loginUser.getCompanyId())
                .eq(leaveType != null, OaLeaveApply::getLeaveType, leaveType)
                .eq(applyStatus != null, OaLeaveApply::getApplyStatus, applyStatus)
                .eq(applyUserId != null, OaLeaveApply::getApplyUserId, applyUserId)
                .orderByDesc(OaLeaveApply::getCreateTime);
        Page<OaLeaveApply> result = leaveApplyMapper.selectPage(page, wrapper);
        List<OaLeaveApplyVO> voList = result.getRecords().stream().map(this::toLeaveApplyVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public OaLeaveApplyVO getLeaveApply(Long id) {
        OaLeaveApply entity = leaveApplyMapper.selectById(id);
        if (entity == null) throw new BizException("请假申请不存在");
        return toLeaveApplyVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitLeaveApply(Long companyId, Long applyUserId, String applyUserName,
                                  Integer leaveType, LocalDate startDate, LocalDate endDate,
                                  BigDecimal leaveDays, String reason, String remark) {
        OaLeaveApply apply = new OaLeaveApply();
        apply.setCompanyId(companyId);
        apply.setApplyNo("LA" + LocalDateTime.now().format(APPLY_NO_FMT) + String.format("%04d", new Random().nextInt(10000)));
        apply.setApplyUserId(applyUserId);
        apply.setApplyUserName(applyUserName);
        apply.setLeaveType(leaveType);
        apply.setStartDate(startDate);
        apply.setEndDate(endDate);
        apply.setLeaveDays(leaveDays != null ? leaveDays : calculateLeaveDays(startDate, endDate));
        apply.setReason(reason);
        apply.setApplyStatus(CommonConst.APPLY_STATUS_DRAFT);
        apply.setRemark(remark);
        apply.setCreateBy(applyUserId);
        leaveApplyMapper.insert(apply);

        // 提交审批
        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_CONTRACT, "leave_apply",
                String.valueOf(apply.getId()),
                applyUserName + " 申请" + getLeaveTypeText(leaveType) + " " + leaveDays + "天");
        apply.setFlowInstanceId(instanceId);
        apply.setApplyStatus(CommonConst.APPLY_STATUS_AUDITING);
        leaveApplyMapper.updateById(apply);

        auditLogUtil.record(CommonConst.MODULE_FLOW, CommonConst.OPER_TYPE_SUBMIT,
                String.valueOf(apply.getId()), null, apply);
        return apply.getId();
    }

    @Override
    public void cancelLeaveApply(Long id) {
        OaLeaveApply apply = leaveApplyMapper.selectById(id);
        if (apply == null) throw new BizException("请假申请不存在");
        if (!Objects.equals(CommonConst.APPLY_STATUS_DRAFT, apply.getApplyStatus()) &&
            !Objects.equals(CommonConst.APPLY_STATUS_AUDITING, apply.getApplyStatus())) {
            throw new BizException("只有草稿或审批中的请假申请可以撤销");
        }
        apply.setApplyStatus(CommonConst.APPLY_STATUS_VOID);
        leaveApplyMapper.updateById(apply);
    }

    private OaLeaveApplyVO toLeaveApplyVO(OaLeaveApply entity) {
        OaLeaveApplyVO vo = new OaLeaveApplyVO();
        vo.setId(entity.getId());
        vo.setApplyNo(entity.getApplyNo());
        vo.setApplyUserId(entity.getApplyUserId());
        vo.setApplyUserName(entity.getApplyUserName());
        vo.setLeaveType(entity.getLeaveType());
        vo.setLeaveTypeText(getLeaveTypeText(entity.getLeaveType()));
        vo.setStartDate(entity.getStartDate());
        vo.setEndDate(entity.getEndDate());
        vo.setLeaveDays(entity.getLeaveDays());
        vo.setReason(entity.getReason());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setApplyStatus(entity.getApplyStatus());
        vo.setApplyStatusText(getApplyStatusText(entity.getApplyStatus()));
        vo.setRemark(entity.getRemark());
        vo.setCreateBy(entity.getCreateBy());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /* ==================== 会议室管理 ==================== */

    @Override
    public PageVO<OaMeetingRoomVO> pageMeetingRoom(Long pageNum, Long pageSize, Long companyId, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaMeetingRoom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaMeetingRoom> wrapper = new LambdaQueryWrapper<OaMeetingRoom>()
                .eq(OaMeetingRoom::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, OaMeetingRoom::getStatus, status)
                .orderByDesc(OaMeetingRoom::getId);
        Page<OaMeetingRoom> result = meetingRoomMapper.selectPage(page, wrapper);
        List<OaMeetingRoomVO> voList = result.getRecords().stream().map(this::toMeetingRoomVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void addMeetingRoom(Long companyId, String roomName, String location, Integer capacity, String facilities, String remark) {
        OaMeetingRoom room = new OaMeetingRoom();
        room.setCompanyId(companyId);
        room.setRoomName(roomName);
        room.setLocation(location);
        room.setCapacity(capacity);
        // 解析设施字符串，写入三个布尔标志列
        if (facilities != null) {
            String[] items = facilities.split(",");
            for (String item : items) {
                String it = item.trim().toLowerCase();
                if (it.contains("投影") || it.contains("projector")) room.setHasProjector(1);
                else if (it.contains("视频") || it.contains("会议")) room.setHasVideoConf(1);
                else if (it.contains("电话")) room.setHasPhone(1);
            }
        }
        room.setStatus(CommonConst.STATUS_ENABLED);
        room.setRemark(remark);
        meetingRoomMapper.insert(room);
    }

    @Override
    public void updateMeetingRoom(Long id, String roomName, String location, Integer capacity, String facilities, Integer status, String remark) {
        OaMeetingRoom room = meetingRoomMapper.selectById(id);
        if (room == null) throw new BizException("会议室不存在");
        room.setRoomName(roomName);
        room.setLocation(location);
        room.setCapacity(capacity);
        // 解析设施字符串，写入三个布尔标志列
        if (facilities != null) {
            String[] items = facilities.split(",");
            for (String item : items) {
                String it = item.trim().toLowerCase();
                if (it.contains("投影") || it.contains("projector")) room.setHasProjector(1);
                else if (it.contains("视频") || it.contains("会议")) room.setHasVideoConf(1);
                else if (it.contains("电话")) room.setHasPhone(1);
            }
        }
        room.setStatus(status);
        room.setRemark(remark);
        meetingRoomMapper.updateById(room);
    }

    @Override
    public void deleteMeetingRoom(Long id) {
            OaMeetingRoom room = meetingRoomMapper.selectById(id);
            if (room == null) throw new BizException("会议室不存在");
            meetingRoomMapper.deleteById(id);
        }

    @Override
    public void toggleMeetingRoomStatus(Long id) {
        OaMeetingRoom room = meetingRoomMapper.selectById(id);
        if (room == null) throw new BizException("会议室不存在");
        Integer newStatus = (room.getStatus() == null || room.getStatus() == 1) ? 0 : 1;
        meetingRoomMapper.update(null, new LambdaUpdateWrapper<OaMeetingRoom>()
                .eq(OaMeetingRoom::getId, id)
                .set(OaMeetingRoom::getStatus, newStatus));
    }

    private OaMeetingRoomVO toMeetingRoomVO(OaMeetingRoom entity) {
        OaMeetingRoomVO vo = new OaMeetingRoomVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setRoomName(entity.getRoomName());
        vo.setLocation(entity.getLocation());
        vo.setCapacity(entity.getCapacity());
        // 将三个布尔标志合并为逗号分隔字符串回显给前端
        List<String> facilityItems = new ArrayList<>();
        if (entity.getHasProjector() != null && entity.getHasProjector() == 1) facilityItems.add("投影仪");
        if (entity.getHasVideoConf() != null && entity.getHasVideoConf() == 1) facilityItems.add("视频会议");
        if (entity.getHasPhone() != null && entity.getHasPhone() == 1) facilityItems.add("电话");
        vo.setFacilities(String.join(", ", facilityItems));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /* ==================== 会议室预约 ==================== */

    @Override
    public PageVO<OaMeetingBookingVO> pageMeetingBooking(Long pageNum, Long pageSize, Long roomId, Long bookerId, LocalDate startDate, LocalDate endDate) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaMeetingBooking> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaMeetingBooking> wrapper = new LambdaQueryWrapper<OaMeetingBooking>()
                .eq(OaMeetingBooking::getCompanyId, loginUser.getCompanyId())
                .eq(roomId != null, OaMeetingBooking::getRoomId, roomId)
                .eq(bookerId != null, OaMeetingBooking::getBookerId, bookerId)
                .ge(startDate != null, OaMeetingBooking::getMeetingDate, startDate)
                .le(endDate != null, OaMeetingBooking::getMeetingDate, endDate)
                .eq(OaMeetingBooking::getBookStatus, CommonConst.BILL_PAY_STATUS_UNPAID)
                .orderByDesc(OaMeetingBooking::getCreateTime);
        Page<OaMeetingBooking> result = meetingBookingMapper.selectPage(page, wrapper);

        // 批量加载会议室名称
        List<Long> roomIds = result.getRecords().stream().map(OaMeetingBooking::getRoomId).distinct().toList();
        Map<Long, String> roomNameMap = roomIds.isEmpty() ? Collections.emptyMap()
                : meetingRoomMapper.selectBatchIds(roomIds).stream()
                        .collect(Collectors.toMap(OaMeetingRoom::getId, OaMeetingRoom::getRoomName));

        List<OaMeetingBookingVO> voList = result.getRecords().stream().map(r -> {
            OaMeetingBookingVO vo = toMeetingBookingVO(r);
            vo.setRoomName(roomNameMap.getOrDefault(r.getRoomId(), ""));
            return vo;
        }).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bookMeetingRoom(Long companyId, Long bookerId, String bookerName, Long roomId,
                                 LocalDate meetingDate, LocalTime startTime, LocalTime endTime,
                                 String meetingTitle, Integer attendeeCount, String attendeeIds, String remark) {
        // 校验会议室存在
        OaMeetingRoom room = meetingRoomMapper.selectById(roomId);
        if (room == null) throw new BizException("会议室不存在");
        if (!Objects.equals(CommonConst.STATUS_ENABLED, room.getStatus())) {
            throw new BizException("该会议室已停用");
        }
        // 校验时间冲突
        Long conflictCount = meetingBookingMapper.selectCount(new LambdaQueryWrapper<OaMeetingBooking>()
                .eq(OaMeetingBooking::getRoomId, roomId)
                .eq(OaMeetingBooking::getMeetingDate, meetingDate)
                .eq(OaMeetingBooking::getBookStatus, CommonConst.BILL_PAY_STATUS_UNPAID)
                .and(w -> w.lt(OaMeetingBooking::getStartTime, endTime).gt(OaMeetingBooking::getEndTime, startTime)));
        if (conflictCount > 0) {
            throw new BizException("该时间段已被预约，请选择其他时间");
        }
        if (attendeeCount != null && attendeeCount > room.getCapacity()) {
            throw new BizException("参会人数超过会议室容纳人数（" + room.getCapacity() + "人）");
        }

        OaMeetingBooking booking = new OaMeetingBooking();
        booking.setCompanyId(companyId);
        booking.setRoomId(roomId);
        booking.setBookerId(bookerId);
        booking.setBookerName(bookerName);
        booking.setMeetingTitle(meetingTitle);
        booking.setMeetingDate(meetingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setAttendeeCount(attendeeCount != null ? attendeeCount : 1);
        booking.setAttendeeIds(attendeeIds);
        booking.setRemark(remark);
        booking.setBookStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        meetingBookingMapper.insert(booking);
        return booking.getId();
    }

    @Override
    public void cancelMeetingBooking(Long id) {
        OaMeetingBooking booking = meetingBookingMapper.selectById(id);
        if (booking == null) throw new BizException("预约记录不存在");
        if (!Objects.equals(CommonConst.BILL_PAY_STATUS_UNPAID, booking.getBookStatus())) {
            throw new BizException("该预约已取消或已完成");
        }
        booking.setBookStatus(CommonConst.BILL_PAY_STATUS_PAID); // 1=已取消
        meetingBookingMapper.updateById(booking);
    }

    private OaMeetingBookingVO toMeetingBookingVO(OaMeetingBooking entity) {
        OaMeetingBookingVO vo = new OaMeetingBookingVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setRoomId(entity.getRoomId());
        vo.setBookerId(entity.getBookerId());
        vo.setBookerName(entity.getBookerName());
        vo.setMeetingTitle(entity.getMeetingTitle());
        vo.setMeetingDate(entity.getMeetingDate());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setAttendeeCount(entity.getAttendeeCount());
        vo.setBookStatus(entity.getBookStatus());
        vo.setBookStatusText(getBookStatusText(entity.getBookStatus()));
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /* ==================== 公告管理 ==================== */

    @Override
    public PageVO<OaAnnouncementVO> pageAnnouncement(Long pageNum, Long pageSize, Long companyId, Integer publishStatus, Long publisherId) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaAnnouncement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaAnnouncement> wrapper = new LambdaQueryWrapper<OaAnnouncement>()
                .eq(OaAnnouncement::getCompanyId, loginUser.getCompanyId())
                .eq(publishStatus != null, OaAnnouncement::getPublishStatus, publishStatus)
                .eq(publisherId != null, OaAnnouncement::getPublisherId, publisherId)
                .orderByDesc(OaAnnouncement::getCreateTime);
        Page<OaAnnouncement> result = announcementMapper.selectPage(page, wrapper);
        List<OaAnnouncementVO> voList = result.getRecords().stream().map(this::toAnnouncementVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public OaAnnouncementVO getAnnouncement(Long id) {
        OaAnnouncement entity = announcementMapper.selectById(id);
        if (entity == null) throw new BizException("公告不存在");
        return toAnnouncementVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(Long companyId, String title, String content, Integer publishType,
                                    String targetDeptIds, String targetUserIds,
                                    Long publisherId, String publisherName, String remark) {
        OaAnnouncement announcement = new OaAnnouncement();
        announcement.setCompanyId(companyId);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublishType(publishType);
        announcement.setTargetDeptIds(targetDeptIds);
        announcement.setTargetUserIds(targetUserIds);
        announcement.setPublishStatus(CommonConst.APPLY_STATUS_DRAFT);
        announcement.setPublisherId(publisherId);
        announcement.setPublisherName(publisherName);
        announcement.setRemark(remark);
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAnnouncement(Long id) {
        OaAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) throw new BizException("公告不存在");
        if (!Objects.equals(CommonConst.APPLY_STATUS_DRAFT, announcement.getPublishStatus())) {
            throw new BizException("只有草稿状态的公告可以发布");
        }
        announcement.setPublishStatus(CommonConst.STATUS_ENABLED);
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    @Override
    public void recallAnnouncement(Long id) {
        OaAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) throw new BizException("公告不存在");
        if (!Objects.equals(CommonConst.STATUS_ENABLED, announcement.getPublishStatus())) {
            throw new BizException("该公告未发布");
        }
        announcement.setPublishStatus(CommonConst.APPLY_STATUS_VOID);
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAnnouncementRead(Long announcementId, Long userId) {
        Long count = announcementReadMapper.selectCount(new LambdaQueryWrapper<OaAnnouncementRead>()
                .eq(OaAnnouncementRead::getAnnouncementId, announcementId)
                .eq(OaAnnouncementRead::getUserId, userId));
        if (count == 0) {
            OaAnnouncementRead read = new OaAnnouncementRead();
            read.setAnnouncementId(announcementId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            announcementReadMapper.insert(read);
        }
    }

    @Override
    public boolean isAnnouncementRead(Long announcementId, Long userId) {
        Long count = announcementReadMapper.selectCount(new LambdaQueryWrapper<OaAnnouncementRead>()
                .eq(OaAnnouncementRead::getAnnouncementId, announcementId)
                .eq(OaAnnouncementRead::getUserId, userId));
        return count > 0;
    }

    @Override
    public Map<String, Object> getAnnouncementReadStats(Long announcementId) {
        OaAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) throw new BizException("公告不存在");
        Long readCount = announcementReadMapper.selectCount(new LambdaQueryWrapper<OaAnnouncementRead>()
                .eq(OaAnnouncementRead::getAnnouncementId, announcementId));
        Map<String, Object> stats = new HashMap<>();
        stats.put("readCount", readCount);
        stats.put("totalTargetCount", 0);
        stats.put("readRate", BigDecimal.ZERO);
        return stats;
    }

    private OaAnnouncementVO toAnnouncementVO(OaAnnouncement entity) {
        OaAnnouncementVO vo = new OaAnnouncementVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setPublishType(entity.getPublishType());
        vo.setPublishTypeText(getPublishTypeText(entity.getPublishType()));
        vo.setPublishStatus(entity.getPublishStatus());
        vo.setPublishStatusText(getPublishStatusText(entity.getPublishStatus()));
        vo.setPublisherId(entity.getPublisherId());
        vo.setPublisherName(entity.getPublisherName());
        vo.setPublishTime(entity.getPublishTime());
        return vo;
    }

    /* ==================== 考勤打卡 ==================== */

    @Override
    public PageVO<OaClockRecordVO> pageClockRecord(Long pageNum, Long pageSize, Long companyId, Long userId,
                                                    LocalDate startDate, LocalDate endDate, Integer clockType) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaClockRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaClockRecord> wrapper = new LambdaQueryWrapper<OaClockRecord>()
                .eq(OaClockRecord::getCompanyId, loginUser.getCompanyId())
                .eq(userId != null, OaClockRecord::getUserId, userId)
                .eq(clockType != null, OaClockRecord::getClockType, clockType)
                .ge(startDate != null, OaClockRecord::getClockTime, startDate != null ? startDate.atStartOfDay() : null)
                .le(endDate != null, OaClockRecord::getClockTime, endDate != null ? endDate.atTime(23, 59, 59) : null)
                .orderByDesc(OaClockRecord::getClockTime);
        Page<OaClockRecord> result = clockRecordMapper.selectPage(page, wrapper);
        List<OaClockRecordVO> voList = result.getRecords().stream().map(this::toClockRecordVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clockIn(Long companyId, Long userId, String userName, Integer clockType,
                        BigDecimal lat, BigDecimal lng, String addr, Integer deviceType) {
        OaClockRecord record = new OaClockRecord();
        record.setCompanyId(companyId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setClockType(clockType);
        record.setClockTime(LocalDateTime.now());
        record.setLocationLat(lat);
        record.setLocationLng(lng);
        record.setLocationAddr(addr);
        
        // 判断状态（简化：PC端默认正常，移动端需配置打卡范围）
        record.setIsLate(0); record.setIsEarly(0); record.setIsAbsent(0);
        clockRecordMapper.insert(record);
        return record.getId();
    }

    private OaClockRecordVO toClockRecordVO(OaClockRecord entity) {
        OaClockRecordVO vo = new OaClockRecordVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setClockType(entity.getClockType());
        vo.setClockTypeText(getClockTypeText(entity.getClockType()));
        vo.setClockTime(entity.getClockTime());
        vo.setLocationLat(entity.getLocationLat());
        vo.setLocationLng(entity.getLocationLng());
        vo.setLocationAddr(entity.getLocationAddr());
        
        vo.setIsEarly(entity.getIsEarly()); vo.setIsLate(entity.getIsLate()); vo.setIsAbsent(entity.getIsAbsent());
        
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /* ==================== 工作汇报 ==================== */

    @Override
    public PageVO<OaWorkReportVO> pageWorkReport(Long pageNum, Long pageSize, Long companyId, Integer reportType,
                                                   Long submitUserId, Integer reportStatus, String reportPeriod) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<OaWorkReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OaWorkReport> wrapper = new LambdaQueryWrapper<OaWorkReport>()
                .eq(OaWorkReport::getCompanyId, loginUser.getCompanyId())
                .eq(reportType != null, OaWorkReport::getReportType, reportType)
                .eq(submitUserId != null, OaWorkReport::getSubmitUserId, submitUserId)
                .eq(reportStatus != null, OaWorkReport::getReportStatus, reportStatus)
                .eq(StringUtils.hasText(reportPeriod), OaWorkReport::getReportPeriod, reportPeriod)
                .orderByDesc(OaWorkReport::getCreateTime);
        Page<OaWorkReport> result = workReportMapper.selectPage(page, wrapper);
        List<OaWorkReportVO> voList = result.getRecords().stream().map(this::toWorkReportVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public OaWorkReportVO getWorkReport(Long id) {
        OaWorkReport entity = workReportMapper.selectById(id);
        if (entity == null) throw new BizException("工作汇报不存在");
        return toWorkReportVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitWorkReport(Long companyId, Long submitUserId, String submitUserName,
                                  Integer reportType, String reportPeriod, String reportContent, String remark) {
        OaWorkReport report = new OaWorkReport();
        report.setCompanyId(companyId);
        report.setReportType(reportType);
        report.setReportPeriod(reportPeriod);
        report.setSubmitUserId(submitUserId);
        report.setSubmitUserName(submitUserName);
        report.setReportContent(reportContent);
        report.setReportStatus(CommonConst.APPLY_STATUS_DRAFT);
        report.setRemark(remark);
        workReportMapper.insert(report);

        // 提交审批
        Long instanceId = flowEngineService.submit(CommonConst.FLOW_DEF_CONTRACT, "work_report",
                String.valueOf(report.getId()),
                submitUserName + " 提交" + getReportTypeText(reportType) + "：" + reportPeriod);
        report.setFlowInstanceId(instanceId);
        report.setReportStatus(CommonConst.APPLY_STATUS_AUDITING);
        workReportMapper.updateById(report);
        return report.getId();
    }

    @Override
    public void cancelWorkReport(Long id) {
        OaWorkReport report = workReportMapper.selectById(id);
        if (report == null) throw new BizException("工作汇报不存在");
        if (!Objects.equals(CommonConst.APPLY_STATUS_DRAFT, report.getReportStatus()) &&
            !Objects.equals(CommonConst.APPLY_STATUS_AUDITING, report.getReportStatus())) {
            throw new BizException("只有草稿或审批中的汇报可以撤销");
        }
        report.setReportStatus(CommonConst.APPLY_STATUS_VOID);
        workReportMapper.updateById(report);
    }

    private OaWorkReportVO toWorkReportVO(OaWorkReport entity) {
        OaWorkReportVO vo = new OaWorkReportVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setReportType(entity.getReportType());
        vo.setReportTypeText(getReportTypeText(entity.getReportType()));
        vo.setReportPeriod(entity.getReportPeriod());
        vo.setSubmitUserId(entity.getSubmitUserId());
        vo.setSubmitUserName(entity.getSubmitUserName());
        vo.setReportContent(entity.getReportContent());
        vo.setFlowInstanceId(entity.getFlowInstanceId());
        vo.setReportStatus(entity.getReportStatus());
        vo.setReportStatusText(getReportStatusText(entity.getReportStatus()));
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /* ==================== 辅助方法 ==================== */

    private BigDecimal calculateLeaveDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) return BigDecimal.ZERO;
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        return BigDecimal.valueOf(days);
    }

    private String getLeaveTypeText(Integer leaveType) {
        if (leaveType == null) return null;
        return switch (leaveType) {
            case 1 -> "年假";
            case 2 -> "事假";
            case 3 -> "病假";
            case 4 -> "调休";
            case 5 -> "产假";
            case 6 -> "其他";
            default -> "未知";
        };
    }

    private String getApplyStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "通过";
            case 3 -> "驳回";
            case 4 -> "作废";
            default -> "未知";
        };
    }

    private String getBookStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "已预约";
            case 1 -> "已取消";
            case 2 -> "已完成";
            default -> "未知";
        };
    }

    private String getPublishTypeText(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case 1 -> "全员";
            case 2 -> "指定部门";
            case 3 -> "指定人员";
            default -> "未知";
        };
    }

    private String getPublishStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已撤回";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }

    private String getClockTypeText(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case 1 -> "上班打卡";
            case 2 -> "下班打卡";
            default -> "未知";
        };
    }


    private String getReportTypeText(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case 1 -> "日报";
            case 2 -> "周报";
            case 3 -> "月报";
            default -> "未知";
        };
    }

    private String getReportStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "通过";
            case 3 -> "驳回";
            case 4 -> "作废";
            default -> "未知";
        };
    }
}

