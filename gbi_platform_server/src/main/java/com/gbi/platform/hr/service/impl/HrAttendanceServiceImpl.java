package com.gbi.platform.hr.service.impl;

import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.AttendanceQueryDTO;
import com.gbi.platform.hr.entity.HrAttendanceRecord;
import com.gbi.platform.hr.mapper.HrAttendanceRecordMapper;
import com.gbi.platform.hr.service.HrAttendanceService;
import com.gbi.platform.hr.vo.HrAttendanceVO;
import com.gbi.platform.entity.OaClockRecord;
import com.gbi.platform.mapper.OaClockRecordMapper;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrAttendanceServiceImpl implements HrAttendanceService {

    private final OaClockRecordMapper clockRecordMapper;
    private final HrAttendanceRecordMapper attendanceRecordMapper;

    @Override
    public PageVO<HrAttendanceVO> pageAttendance(AttendanceQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        long total = attendanceRecordMapper.countByCondition(loginUser.getCompanyId(), dto.getEmployeeId(), dto.getAttendanceMonth());
        int offset = (int)((dto.getPageNum() - 1) * dto.getPageSize());
        int pageSize = dto.getPageSize().intValue();
        List<HrAttendanceRecord> records = attendanceRecordMapper.selectPage(loginUser.getCompanyId(), dto.getEmployeeId(), dto.getAttendanceMonth(), offset, pageSize);
        List<HrAttendanceVO> voList = records.stream().map(this::toVO).collect(Collectors.toList());
        long pages = total > 0 ? (total + pageSize - 1) / pageSize : 0;
        return new PageVO<>(voList, total, dto.getPageNum(), dto.getPageSize(), pages);
    }

    @Override
    public int syncMonthly(String attendanceMonth) {
        if (attendanceMonth == null || attendanceMonth.isEmpty()) {
            attendanceMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        LoginUser loginUser = UserContext.getLoginUser();
        LocalDate monthStart = LocalDate.parse(attendanceMonth + "-01");
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<OaClockRecord> records = clockRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaClockRecord>()
                        .eq(OaClockRecord::getCompanyId, loginUser.getCompanyId())
                        .ge(OaClockRecord::getClockTime, monthStart.atStartOfDay())
                        .le(OaClockRecord::getClockTime, monthEnd.plusDays(1).atStartOfDay()));
        int count = 0;
        for (OaClockRecord record : records) {
            HrAttendanceRecord attendance = new HrAttendanceRecord();
            attendance.setCompanyId(loginUser.getCompanyId());
            attendance.setEmployeeId(record.getUserId());
            attendance.setEmployeeName(record.getUserName());
            attendance.setAttendanceMonth(attendanceMonth);
            attendance.setAttendanceDay(record.getClockTime().toLocalDate());
            attendance.setClockInTime(record.getClockTime());
            attendance.setClockType(toClockType(record));
            attendance.setLateMinutes(calcLateMinutes(record));
            attendance.setWorkDays("1");
            attendance.setActualDays("1");
            attendanceRecordMapper.insert(attendance);
            count++;
        }
        log.info("attendance sync done: month={}, count={}", attendanceMonth, count);
        return count;
    }

    @Override
    public List<HrAttendanceVO> exportAttendance(Long employeeId, String attendanceMonth) {
        LoginUser loginUser = UserContext.getLoginUser();
        return attendanceRecordMapper.selectPage(loginUser.getCompanyId(), employeeId, attendanceMonth, 0, 9999).stream().map(this::toVO).collect(Collectors.toList());
    }

    private HrAttendanceVO toVO(HrAttendanceRecord entity) {
        HrAttendanceVO vo = new HrAttendanceVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setAttendanceMonth(entity.getAttendanceMonth());
        vo.setAttendanceDay(entity.getAttendanceDay());
        vo.setClockInTime(entity.getClockInTime());
        vo.setClockOutTime(entity.getClockOutTime());
        vo.setClockType(entity.getClockType());
        vo.setClockTypeText(toClockTypeText(entity.getClockType()));
        vo.setLateMinutes(entity.getLateMinutes());
        vo.setEarlyMinutes(entity.getEarlyMinutes());
        vo.setAbsent(entity.getAbsent());
        vo.setLeaveDays(entity.getLeaveDays());
        vo.setWorkDays(entity.getWorkDays());
        vo.setActualDays(entity.getActualDays());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private Integer toClockType(OaClockRecord record) {
        if (record.getClockTime() == null) return 4;
        LocalTime clock = record.getClockTime().toLocalTime();
        if (clock.isBefore(LocalTime.of(9, 0))) return 1;
        if (clock.isAfter(LocalTime.of(9, 0))) return 2;
        return 1;
    }

    private Integer calcLateMinutes(OaClockRecord record) {
        if (record.getClockTime() == null) return 0;
        LocalTime clock = record.getClockTime().toLocalTime();
        if (clock.isBefore(LocalTime.of(9, 0))) return 0;
        long minutes = java.time.Duration.between(LocalTime.of(9, 0), clock).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    private String toClockTypeText(Integer type) {
        if (type == null) return null;
        switch (type) {
            case 1: return "正常";
            case 2: return "迟到";
            case 3: return "早退";
            case 4: return "缺卡";
            default: return "未知";
        }
    }
}
