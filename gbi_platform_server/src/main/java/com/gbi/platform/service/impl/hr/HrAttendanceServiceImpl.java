package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.*;
import com.gbi.platform.entity.OaClockRecord;
import com.gbi.platform.entity.SysConfig;
import com.gbi.platform.entity.hr.*;
import com.gbi.platform.entity.sys.SysHolidayConfig;
import com.gbi.platform.entity.sys.SysWorkweekConfig;
import com.gbi.platform.mapper.OaClockRecordMapper;
import com.gbi.platform.mapper.SysConfigMapper;
import com.gbi.platform.mapper.hr.*;
import com.gbi.platform.mapper.sys.SysHolidayConfigMapper;
import com.gbi.platform.mapper.sys.SysWorkweekConfigMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.hr.HrAttendanceService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.hr.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤管理服务实现：支持休息日配置、节假日、班次管理、免考勤员工、异常检测
 * 数据表：hr_attendance_record、sys_workweek_config、sys_holiday_config、hr_employee_shift、hr_attendance_exception
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrAttendanceServiceImpl implements HrAttendanceService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final HrAttendanceRecordMapper attendanceRecordMapper;
    private final OaClockRecordMapper clockRecordMapper;
    private final HrEmployeeMapper employeeMapper;
    private final HrPostMapper postMapper;
    private final SysWorkweekConfigMapper workweekConfigMapper;
    private final SysHolidayConfigMapper holidayConfigMapper;
    private final HrEmployeeShiftMapper shiftMapper;
    private final AttendanceExceptionMapper exceptionMapper;
    private final SysConfigMapper configMapper;
    private final ConfigService configService;
    private final AuditLogUtil auditLogUtil;
    // ==================== 基础考勤操作 ====================

    /** 分页查询考勤记录 */
    @Override
    public PageVO<HrAttendanceVO> pageAttendance(AttendanceQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrAttendanceRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HrAttendanceRecord> wrapper = new LambdaQueryWrapper<HrAttendanceRecord>()
                .eq(HrAttendanceRecord::getCompanyId, loginUser.getCompanyId())
                .eq(dto.getEmployeeId() != null, HrAttendanceRecord::getEmployeeId, dto.getEmployeeId())
                .eq(dto.getAttendanceMonth() != null, HrAttendanceRecord::getAttendanceMonth, dto.getAttendanceMonth())
                .orderByDesc(HrAttendanceRecord::getAttendanceDay);
        Page<HrAttendanceRecord> result = attendanceRecordMapper.selectPage(page, wrapper);
        List<HrAttendanceVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /** 同步考勤（保留原有简单逻辑，兼容旧调用） */
    @Override
    public int syncMonthly(String attendanceMonth) {
        if (attendanceMonth == null || attendanceMonth.isEmpty()) {
            attendanceMonth = LocalDate.now().format(MONTH_FMT);
        }
        LoginUser loginUser = UserContext.getLoginUser();
        LocalDate monthStart = LocalDate.parse(attendanceMonth + "-01");
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<OaClockRecord> records = clockRecordMapper.selectList(
                new LambdaQueryWrapper<OaClockRecord>()
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
            attendance.setWorkDays(1);
            attendance.setActualDays(1);
            attendance.setCreateBy(loginUser.getUserId());
            try {
                attendanceRecordMapper.insert(attendance);
            } catch (Exception e) {
                HrAttendanceRecord exist = attendanceRecordMapper.selectOne(
                        new LambdaQueryWrapper<HrAttendanceRecord>()
                                .eq(HrAttendanceRecord::getEmployeeId, record.getUserId())
                                .eq(HrAttendanceRecord::getAttendanceDay, record.getClockTime().toLocalDate())
                                .eq(HrAttendanceRecord::getIsDelete, 0));
                if (exist != null) {
                    exist.setClockInTime(record.getClockTime());
                    exist.setClockType(toClockType(record));
                    exist.setLateMinutes(calcLateMinutes(record));
                    attendanceRecordMapper.updateById(exist);
                }
                count++;
            }
        }
        log.info("attendance sync done: month={}, count={}", attendanceMonth, count);
        return count;
    }

    /** 导出考勤记录 */
    @Override
    public List<HrAttendanceVO> exportAttendance(Long employeeId, String attendanceMonth) {
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<HrAttendanceRecord> wrapper = new LambdaQueryWrapper<HrAttendanceRecord>()
                .eq(HrAttendanceRecord::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, HrAttendanceRecord::getEmployeeId, employeeId)
                .eq(attendanceMonth != null, HrAttendanceRecord::getAttendanceMonth, attendanceMonth)
                .orderByDesc(HrAttendanceRecord::getAttendanceDay);
        return attendanceRecordMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }
    // ==================== 高级同步（预览+执行） ====================

    /**
     * 同步预览：不写入数据库，计算并返回预览结果
     * @param month 考勤月份 yyyy-MM
     * @param employeeScope 员工范围 0=全部 1=指定
     * @param employeeIds 指定员工ID列表
     * @param syncType 同步类型
     * @return 预览统计信息
     */
    @Override
    public Map<String, Object> syncPreview(String month, Integer employeeScope, List<Long> employeeIds, Integer syncType) {
        LoginUser loginUser = UserContext.getLoginUser();
        List<HrEmployee> employees = getTargetEmployees(employeeScope, employeeIds, loginUser.getCompanyId());
        LocalDate monthStart = LocalDate.parse(month + "-01");
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<LocalDate> workDays = calcWorkDaysForMonth(monthStart, monthEnd, loginUser.getCompanyId());

        int clockRecordCount = countClockRecords(employees, monthStart, monthEnd, loginUser.getCompanyId());
        int totalRecords = employees.size() * workDays.size();
        int fullAttendanceCount = 0, normalCount = 0, lateCount = 0, absentCount = 0;

        for (HrEmployee emp : employees) {
            if (isExemptAttendance(emp)) { fullAttendanceCount += workDays.size(); continue; }
            List<OaClockRecord> clockRecords = queryClockRecords(emp.getUserId(), monthStart, monthEnd, loginUser.getCompanyId());
            Map<LocalDate, OaClockRecord> dayMap = aggregateClockInRecords(clockRecords);
            for (LocalDate workDay : workDays) {
                OaClockRecord clockIn = dayMap.get(workDay);
                if (clockIn == null) absentCount++;
                else { int lateMins = calcLateMinutesFromClock(clockIn, emp, workDay); if (lateMins > 0) lateCount++; else normalCount++; }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("workDays", workDays.size());
        result.put("clockRecords", clockRecordCount);
        result.put("willCreate", totalRecords);
        result.put("fullAttendance", fullAttendanceCount);
        result.put("normal", normalCount);
        result.put("late", lateCount);
        result.put("absent", absentCount);
        result.put("employeeCount", employees.size());
        return result;
    }

    /**
     * 执行考勤同步：删除当月旧记录后重新生成，支持免考勤/班次/节假日处理
     * @param month 考勤月份
     * @param employeeScope 员工范围
     * @param employeeIds 指定员工ID列表
     * @param syncType 同步类型
     * @return 新增记录总数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncMonthlyAdvanced(String month, Integer employeeScope, List<Long> employeeIds, Integer syncType) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();
        LocalDate monthStart = LocalDate.parse(month + "-01");
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        log.info("[syncMonthlyAdvanced] 开始同步: month={}, employeeScope={}, syncType={}", month, employeeScope, syncType);

        // 1. 查询目标员工（在职：正式+试用）
        List<HrEmployee> employees = getTargetEmployees(employeeScope, employeeIds, companyId);
        if (employees.isEmpty()) throw new BizException("没有符合条件的员工");
        log.info("[syncMonthlyAdvanced] 找到{}名目标员工", employees.size());

        // 2. 重新同步时先清除当月记录
        if (syncType != null && syncType == 2) {
            attendanceRecordMapper.delete(new LambdaQueryWrapper<HrAttendanceRecord>()
                    .eq(HrAttendanceRecord::getCompanyId, companyId)
                    .eq(HrAttendanceRecord::getAttendanceMonth, month));
            log.info("[syncMonthlyAdvanced] 清除旧记录: month={}", month);
        }

        // 3. 计算当月工作日
        List<LocalDate> workDays = calcWorkDaysForMonth(monthStart, monthEnd, companyId);
        log.info("[syncMonthlyAdvanced] 工作日数: {}", workDays.size());
        int lateThreshold = getConfigInt("attendance.late_threshold", 30);
        int absentThreshold = getConfigInt("attendance.absent_threshold", 480);
        int earlyThreshold = getConfigInt("attendance.early_threshold", 30);

        int totalCount = 0, fullAttendanceCount = 0, normalCount = 0, lateCount = 0, absentCount = 0;

        // 4. 遍历员工，逐日生成考勤记录（单个员工失败不影响整体）
        for (HrEmployee employee : employees) {
            try {
                if (isExemptAttendance(employee)) {
                    for (LocalDate workDay : workDays) {
                        upsertAttendanceRecord(employee, month, workDay, null, null, 1, 0, loginUser.getUserId());
                        fullAttendanceCount++;
                    }
                    continue;
                }
                List<OaClockRecord> clockRecords = queryClockRecords(employee.getUserId(), monthStart, monthEnd, companyId);
                Map<LocalDate, List<OaClockRecord>> dayRecordsMap = aggregateByDate(clockRecords);
                for (LocalDate workDay : workDays) {
                    List<OaClockRecord> dayRecords = dayRecordsMap.getOrDefault(workDay, Collections.emptyList());
                    Integer clockType, lateMinutes = 0;
                    LocalDateTime clockInTime = null, clockOutTime = null;
                    if (!dayRecords.isEmpty()) {
                        OaClockRecord earliest = dayRecords.stream().min(Comparator.comparing(OaClockRecord::getClockTime)).orElse(null);
                        OaClockRecord latest = dayRecords.stream().max(Comparator.comparing(OaClockRecord::getClockTime)).orElse(null);
                        if (earliest != null) clockInTime = earliest.getClockTime();
                        if (latest != null) clockOutTime = latest.getClockTime();
                        ShiftConfigVO shift = getEmployeeShift(employee.getId(), workDay);
                        clockType = calculateClockType(clockInTime, clockOutTime, shift, lateThreshold, absentThreshold, earlyThreshold);
                        lateMinutes = calcLateMinutesInternal(clockInTime, shift.getShiftStartTime(), lateThreshold);
                    } else {
                        clockType = 4;
                    }
                    upsertAttendanceRecord(employee, month, workDay, clockInTime, clockOutTime, clockType, lateMinutes, loginUser.getUserId());
                    totalCount++;
                    if (clockType == 1) normalCount++;
                    else if (clockType == 2) lateCount++;
                    else if (clockType == 4 || clockType == 5) absentCount++;
                }
            } catch (Exception e) {
                // 单个员工失败记录警告后继续，不影响其他员工
                log.warn("[syncMonthlyAdvanced] 员工{}同步失败: {}", employee.getId(), e.getMessage(), e);
            }
        }

        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "同步打卡", month, null,
                Map.of("employeeCount", employees.size(), "totalRecords", totalCount,
                        "fullAttendance", fullAttendanceCount, "normal", normalCount,
                        "late", lateCount, "absent", absentCount));
        log.info("attendance sync done: month={}, employeeCount={}, totalRecords={}, fullAttendance={}, normal={}, late={}, absent={}",
                month, employees.size(), totalCount, fullAttendanceCount, normalCount, lateCount, absentCount);

        // 5. 异常检测独立执行，失败不影响考勤数据
        try {
            detectAttendanceExceptions(month, companyId, lateThreshold, absentThreshold, earlyThreshold);
        } catch (Exception e) {
            log.warn("[syncMonthlyAdvanced] 异常检测失败（不影响考勤数据）: {}", e.getMessage(), e);
        }
        return totalCount;
    }
    // ==================== 考勤异常管理 ====================

    /** 分页查询考勤异常记录 */
    @Override
    public PageVO<AttendanceExceptionVO> pageExceptions(Integer pageNum, Integer pageSize, Long employeeId, Integer exceptionType, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<AttendanceException> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AttendanceException> wrapper = new LambdaQueryWrapper<AttendanceException>()
                .eq(AttendanceException::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, AttendanceException::getEmployeeId, employeeId)
                .eq(exceptionType != null, AttendanceException::getExceptionType, exceptionType)
                .eq(status != null, AttendanceException::getStatus, status)
                .orderByDesc(AttendanceException::getCreateTime);
        Page<AttendanceException> result = exceptionMapper.selectPage(page, wrapper);
        List<AttendanceExceptionVO> voList = result.getRecords().stream().map(this::toExceptionVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /** 处理考勤异常（确认/豁免/忽略） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleException(Long id, Integer handleType, String handleRemark, Long handlerId) {
        AttendanceException exception = exceptionMapper.selectById(id);
        if (exception == null) throw new BizException("异常记录不存在");
        AttendanceException before = new AttendanceException();
        org.springframework.beans.BeanUtils.copyProperties(exception, before);
        exception.setStatus(handleType);
        exception.setHandleBy(handlerId);
        exception.setHandleTime(LocalDateTime.now());
        exception.setHandleRemark(handleRemark);
        exceptionMapper.updateById(exception);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "处理考勤异常", String.valueOf(id), before, exception);
        log.info("attendance exception handled: id={}, type={}", id, handleType);
    }

    /** 手动触发异常检测 */
    @Override
    public void detectExceptions(String month) {
        LoginUser loginUser = UserContext.getLoginUser();
        detectAttendanceExceptions(month, loginUser.getCompanyId(), 30, 480, 30);
    }

    // ==================== 考勤配置管理 ====================

    /** 获取考勤配置参数列表 */
    @Override
    public Map<String, String> getAttendanceConfigs() {
        List<String> keys = Arrays.asList(
                "attendance.late_threshold", "attendance.absent_threshold", "attendance.early_threshold",
                "attendance.normal_clockin_start", "attendance.normal_clockout_end",
                "attendance.absent_reminder_threshold", "attendance.late_frequent_threshold",
                "attendance.shift_morning_start", "attendance.shift_morning_end",
                "attendance.shift_evening_start", "attendance.shift_evening_end",
                "attendance.shift_night_start", "attendance.shift_night_end",
                "attendance.exempt_enabled", "attendance.reminder_enabled", "attendance.reminder_method");
        return configService.getValuesByKeys(keys);
    }

    /** 批量更新考勤配置参数 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceConfigs(Map<String, String> configs) {
        LoginUser loginUser = UserContext.getLoginUser();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getCompanyId, CommonConst.COMPANY_ROOT)
                    .eq(SysConfig::getConfigKey, entry.getKey());
            SysConfig config = configMapper.selectOne(wrapper);
            if (config == null) {
                config = new SysConfig();
                config.setCompanyId(CommonConst.COMPANY_ROOT);
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                config.setConfigName(entry.getKey());
                config.setCreateBy(loginUser.getUserId());
                configMapper.insert(config);
            } else {
                config.setConfigValue(entry.getValue());
                configMapper.updateById(config);
            }
        }
        log.info("attendance configs updated by user={}", loginUser.getUserId());
    }
    // ==================== 休息日配置管理 ====================

    /** 分页查询休息日配置 */
    @Override
    public PageVO<SysWorkweekConfigVO> pageWorkweekConfigs(Integer pageNum, Integer pageSize, Long companyId) {
        Page<SysWorkweekConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysWorkweekConfig> wrapper = new LambdaQueryWrapper<SysWorkweekConfig>()
                .eq(companyId != null, SysWorkweekConfig::getCompanyId, companyId)
                .orderByDesc(SysWorkweekConfig::getId);
        Page<SysWorkweekConfig> result = workweekConfigMapper.selectPage(page, wrapper);
        List<SysWorkweekConfigVO> voList = result.getRecords().stream().map(this::toWorkweekConfigVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /** 查询休息日配置列表（不分页） */
    @Override
    public List<SysWorkweekConfigVO> listWorkweekConfigs() {
        List<SysWorkweekConfig> list = workweekConfigMapper.selectList(
                new LambdaQueryWrapper<SysWorkweekConfig>()
                        .orderByDesc(SysWorkweekConfig::getCompanyId)
                        .orderByAsc(SysWorkweekConfig::getId));
        return list.stream().map(this::toWorkweekConfigVO).collect(Collectors.toList());
    }

    /** 新增休息日配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addWorkweekConfig(SysWorkweekConfigDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        SysWorkweekConfig config = new SysWorkweekConfig();
        config.setCompanyId(dto.getCompanyId() != null ? dto.getCompanyId() : loginUser.getCompanyId());
        config.setConfigName(dto.getConfigName());
        config.setWorkweekType(dto.getWorkweekType());
        config.setRestDayPattern(dto.getRestDayPattern());
        config.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConst.STATUS_ENABLED);
        config.setRemark(dto.getRemark());
        config.setCreateBy(loginUser.getUserId());
        workweekConfigMapper.insert(config);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "新增休息日配置", String.valueOf(config.getId()), null, config);
        log.info("workweek config added: id={}, name={}", config.getId(), config.getConfigName());
        return config.getId();
    }

    /** 更新休息日配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkweekConfig(SysWorkweekConfigDTO dto) {
        SysWorkweekConfig config = workweekConfigMapper.selectById(dto.getId());
        if (config == null) throw new BizException("休息日配置不存在");
        SysWorkweekConfig before = new SysWorkweekConfig();
        org.springframework.beans.BeanUtils.copyProperties(config, before);
        config.setConfigName(dto.getConfigName());
        config.setWorkweekType(dto.getWorkweekType());
        config.setRestDayPattern(dto.getRestDayPattern());
        config.setStatus(dto.getStatus());
        config.setRemark(dto.getRemark());
        config.setUpdateBy(UserContext.getLoginUser().getUserId());
        workweekConfigMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "更新休息日配置", String.valueOf(config.getId()), before, config);
    }

    /** 删除休息日配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkweekConfig(Long id) {
        SysWorkweekConfig config = workweekConfigMapper.selectById(id);
        if (config == null) throw new BizException("休息日配置不存在");
        workweekConfigMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "删除休息日配置", String.valueOf(id), config, null);
    }

    /** 设为默认休息日配置（禁用同公司其他配置） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultWorkweekConfig(Long id) {
        SysWorkweekConfig config = workweekConfigMapper.selectById(id);
        if (config == null) throw new BizException("休息日配置不存在");
        workweekConfigMapper.update(null, new LambdaUpdateWrapper<SysWorkweekConfig>()
                .eq(SysWorkweekConfig::getCompanyId, config.getCompanyId())
                .ne(SysWorkweekConfig::getId, id)
                .set(SysWorkweekConfig::getStatus, CommonConst.STATUS_DISABLED));
        config.setStatus(CommonConst.STATUS_ENABLED);
        workweekConfigMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "设置默认休息日配置", String.valueOf(id), null, config);
        log.info("default workweek config set: id={}", id);
    }

    // ==================== 节假日配置管理 ====================

    /** 分页查询节假日配置 */
    @Override
    public PageVO<SysHolidayConfigVO> pageHolidayConfigs(Integer pageNum, Integer pageSize, Long companyId, Integer year, Integer holidayType) {
        Page<SysHolidayConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysHolidayConfig> wrapper = new LambdaQueryWrapper<SysHolidayConfig>()
                .eq(companyId != null, SysHolidayConfig::getCompanyId, companyId)
                .eq(holidayType != null, SysHolidayConfig::getHolidayType, holidayType)
                .and(year != null, w -> w.apply("YEAR(holiday_date) = {0}", year))
                .orderByDesc(SysHolidayConfig::getHolidayDate);
        Page<SysHolidayConfig> result = holidayConfigMapper.selectPage(page, wrapper);
        List<SysHolidayConfigVO> voList = result.getRecords().stream().map(this::toHolidayConfigVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /** 批量导入节假日配置（幂等：已存在则跳过） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImportHolidays(List<SysHolidayConfigDTO> holidayList) {
        LoginUser loginUser = UserContext.getLoginUser();
        int inserted = 0;
        for (SysHolidayConfigDTO dto : holidayList) {
            LambdaQueryWrapper<SysHolidayConfig> check = new LambdaQueryWrapper<SysHolidayConfig>()
                    .eq(SysHolidayConfig::getCompanyId, dto.getCompanyId())
                    .eq(SysHolidayConfig::getHolidayDate, dto.getHolidayDate());
            Long exist = holidayConfigMapper.selectCount(check);
            if (exist != null && exist > 0) continue;
            SysHolidayConfig holiday = new SysHolidayConfig();
            holiday.setCompanyId(dto.getCompanyId());
            holiday.setHolidayDate(dto.getHolidayDate());
            holiday.setHolidayName(dto.getHolidayName());
            holiday.setHolidayType(dto.getHolidayType());
            holiday.setIsWorkday(dto.getIsWorkday());
            holiday.setStatus(CommonConst.STATUS_ENABLED);
            holiday.setRemark(dto.getRemark());
            holiday.setCreateBy(loginUser.getUserId());
            holidayConfigMapper.insert(holiday);
            inserted++;
        }
        log.info("holiday config batch imported: total={}, inserted={}", holidayList.size(), inserted);
    }

    /** 查询节假日配置列表（不分页，按年份过滤） */
    @Override
    public List<SysHolidayConfigVO> listHolidayConfigs(Integer year) {
        LambdaQueryWrapper<SysHolidayConfig> wrapper = new LambdaQueryWrapper<SysHolidayConfig>()
                .orderByAsc(SysHolidayConfig::getHolidayDate);
        if (year != null) {
            wrapper.apply("YEAR(holiday_date) = {0}", year);
        }
        return holidayConfigMapper.selectList(wrapper).stream().map(this::toHolidayConfigVO).collect(Collectors.toList());
    }
    // ==================== 员工班次配置管理 ====================

    /** 查询员工当日班次配置 */
    @Override
    public ShiftConfigVO getEmployeeShift(Long employeeId, LocalDate date) {
        List<HrEmployeeShift> shifts = shiftMapper.selectActiveShifts(employeeId, date);
        if (!shifts.isEmpty()) return toShiftConfigVO(shifts.get(0));
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee != null && employee.getPostId() != null) {
            HrPost post = postMapper.selectById(employee.getPostId());
            if (post != null && post.getShiftType() != null) return toShiftConfigVOFromType(post.getShiftType());
        }
        return toShiftConfigVOFromType(1);
    }

    /** 查询员工班次配置列表 */
    @Override
    public List<HrEmployeeShiftVO> listEmployeeShifts(Long employeeId) {
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<HrEmployeeShift> wrapper = new LambdaQueryWrapper<HrEmployeeShift>()
                .eq(HrEmployeeShift::getCompanyId, loginUser.getCompanyId())
                .orderByDesc(HrEmployeeShift::getStartDate);
        if (employeeId != null && employeeId > 0) {
            wrapper.eq(HrEmployeeShift::getEmployeeId, employeeId);
        }
        List<HrEmployeeShift> shifts = shiftMapper.selectList(wrapper);

        // 批量查询员工姓名，避免 N+1
        Set<Long> empIds = shifts.stream().map(HrEmployeeShift::getEmployeeId).collect(Collectors.toSet());
        Map<Long, String> empNameMap = new HashMap<>();
        if (!empIds.isEmpty()) {
            employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>().in(HrEmployee::getId, empIds))
                    .forEach(e -> empNameMap.put(e.getId(), e.getName()));
        }

        return shifts.stream().map(shift -> {
            HrEmployeeShiftVO vo = toShiftVO(shift);
            vo.setEmployeeName(empNameMap.getOrDefault(shift.getEmployeeId(), ""));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 新增员工班次配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEmployeeShift(HrEmployeeShiftDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrEmployeeShift shift = new HrEmployeeShift();
        shift.setCompanyId(dto.getCompanyId());
        shift.setEmployeeId(dto.getEmployeeId());
        shift.setShiftType(dto.getShiftType());
        shift.setShiftStartTime(dto.getShiftStartTime());
        shift.setShiftEndTime(dto.getShiftEndTime());
        shift.setStartDate(dto.getStartDate());
        shift.setEndDate(dto.getEndDate());
        shift.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConst.STATUS_ENABLED);
        shift.setRemark(dto.getRemark());
        shift.setCreateBy(loginUser.getUserId());
        shiftMapper.insert(shift);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "新增员工班次", String.valueOf(shift.getId()), null, shift);
        log.info("employee shift added: employeeId={}, shiftType={}", dto.getEmployeeId(), dto.getShiftType());
    }

    /** 更新员工班次配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployeeShift(HrEmployeeShiftDTO dto) {
        HrEmployeeShift shift = shiftMapper.selectById(dto.getId());
        if (shift == null) throw new BizException("班次配置不存在");
        HrEmployeeShift before = new HrEmployeeShift();
        org.springframework.beans.BeanUtils.copyProperties(shift, before);
        shift.setShiftType(dto.getShiftType());
        shift.setShiftStartTime(dto.getShiftStartTime());
        shift.setShiftEndTime(dto.getShiftEndTime());
        shift.setStartDate(dto.getStartDate());
        shift.setEndDate(dto.getEndDate());
        shift.setStatus(dto.getStatus());
        shift.setRemark(dto.getRemark());
        shift.setUpdateBy(UserContext.getLoginUser().getUserId());
        shiftMapper.updateById(shift);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "更新员工班次", String.valueOf(shift.getId()), before, shift);
    }

    /** 删除员工班次配置 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployeeShift(Long id) {
        HrEmployeeShift shift = shiftMapper.selectById(id);
        if (shift == null) throw new BizException("班次配置不存在");
        shiftMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_ATTENDANCE, "删除员工班次", String.valueOf(id), shift, null);
    }
    // ==================== 私有辅助方法 ====================

    /** 获取目标员工列表（在职：正式+试用） */
    private List<HrEmployee> getTargetEmployees(Integer employeeScope, List<Long> employeeIds, Long companyId) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getCompanyId, companyId)
                .in(HrEmployee::getEmployeeStatus, Arrays.asList(1, 2));
        if (employeeScope != null && employeeScope == 1 && employeeIds != null && !employeeIds.isEmpty()) {
            wrapper.in(HrEmployee::getId, employeeIds);
        }
        return employeeMapper.selectList(wrapper);
    }

    /** 计算当月工作日列表 */
    private List<LocalDate> calcWorkDaysForMonth(LocalDate monthStart, LocalDate monthEnd, Long companyId) {
        List<LocalDate> workDays = new ArrayList<>();
        LocalDate current = monthStart;
        while (!current.isAfter(monthEnd)) {
            if (isWorkday(current, companyId)) workDays.add(current);
            current = current.plusDays(1);
        }
        return workDays;
    }

    /** 判断某日期是否为工作日（不含免考勤） */
    private boolean isWorkday(LocalDate date, Long companyId) {
        List<SysHolidayConfig> holidays = holidayConfigMapper.selectByDateRange(date, date, companyId);
        if (!holidays.isEmpty()) return holidays.get(0).getIsWorkday() == 1;
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    /** 查询员工当月OA打卡记录 */
    private List<OaClockRecord> queryClockRecords(Long userId, LocalDate monthStart, LocalDate monthEnd, Long companyId) {
        return clockRecordMapper.selectList(
                new LambdaQueryWrapper<OaClockRecord>()
                        .eq(OaClockRecord::getCompanyId, companyId)
                        .eq(OaClockRecord::getUserId, userId)
                        .ge(OaClockRecord::getClockTime, monthStart.atStartOfDay())
                        .le(OaClockRecord::getClockTime, monthEnd.plusDays(1).atStartOfDay()));
    }

    /** 按日期聚合打卡记录 */
    private Map<LocalDate, List<OaClockRecord>> aggregateByDate(List<OaClockRecord> records) {
        return records.stream().collect(Collectors.groupingBy(r -> r.getClockTime().toLocalDate()));
    }

    /** 按日期聚合上卡记录（取每日最早一条） */
    private Map<LocalDate, OaClockRecord> aggregateClockInRecords(List<OaClockRecord> records) {
        Map<LocalDate, OaClockRecord> map = new HashMap<>();
        for (OaClockRecord record : records) {
            LocalDate day = record.getClockTime().toLocalDate();
            OaClockRecord existing = map.get(day);
            if (existing == null || record.getClockTime().isBefore(existing.getClockTime())) map.put(day, record);
        }
        return map;
    }

    /** 统计打卡记录数 */
    private int countClockRecords(List<HrEmployee> employees, LocalDate monthStart, LocalDate monthEnd, Long companyId) {
        Set<Long> userIds = employees.stream().map(HrEmployee::getUserId).collect(Collectors.toSet());
        if (userIds.isEmpty()) return 0;
        Long cnt = clockRecordMapper.selectCount(
                new LambdaQueryWrapper<OaClockRecord>()
                        .in(OaClockRecord::getUserId, userIds)
                        .ge(OaClockRecord::getClockTime, monthStart.atStartOfDay())
                        .le(OaClockRecord::getClockTime, monthEnd.plusDays(1).atStartOfDay()));
        return cnt == null ? 0 : cnt.intValue();
    }

    /** 判断员工是否免考勤 */
    private boolean isExemptAttendance(HrEmployee employee) {
        return employee.getExemptAttendance() != null && employee.getExemptAttendance() == 1;
    }

    /** 计算打卡类型 1=正常 2=迟到 3=早退 4=缺卡 5=旷工 */
    private Integer calculateClockType(LocalDateTime clockIn, LocalDateTime clockOut, ShiftConfigVO shift,
                                       int lateThreshold, int absentThreshold, int earlyThreshold) {
        if (clockIn == null) return 4;
        LocalTime clockInTime = clockIn.toLocalTime();
        long lateMinutes = 0;
        if (!clockInTime.isBefore(shift.getShiftStartTime()) && !clockInTime.equals(shift.getShiftStartTime())) {
            lateMinutes = Duration.between(shift.getShiftStartTime(), clockInTime).toMinutes();
        }
        if (lateMinutes >= absentThreshold) return 5;
        if (lateMinutes > lateThreshold) return 2;
        if (clockOut != null) {
            LocalTime clockOutTime = clockOut.toLocalTime();
            long earlyMinutes = 0;
            if (!clockOutTime.isAfter(shift.getShiftEndTime())) {
                earlyMinutes = Duration.between(clockOutTime, shift.getShiftEndTime()).toMinutes();
            }
            if (earlyMinutes > earlyThreshold) return 3;
        }
        return 1;
    }

    /** 内部迟到分钟计算 */
    private int calcLateMinutesInternal(LocalDateTime clockIn, LocalTime shiftStart, int lateThreshold) {
        if (clockIn == null || shiftStart == null) return 0;
        LocalTime clockTime = clockIn.toLocalTime();
        if (clockTime.isBefore(shiftStart) || clockTime.equals(shiftStart)) return 0;
        return (int) Duration.between(shiftStart, clockTime).toMinutes();
    }

    /** 预览用迟到分钟计算 */
    private int calcLateMinutesFromClock(OaClockRecord clockIn, HrEmployee employee, LocalDate workDay) {
        if (clockIn == null || clockIn.getClockTime() == null) return 0;
        ShiftConfigVO shift = getEmployeeShift(employee.getId(), workDay);
        return calcLateMinutesInternal(clockIn.getClockTime(), shift.getShiftStartTime(), 30);
    }

    /** 保存或更新考勤记录（幂等，按员工+日期+公司维度唯一） */
    private void upsertAttendanceRecord(HrEmployee employee, String attendanceMonth, LocalDate attendanceDay,
                                         LocalDateTime clockInTime, LocalDateTime clockOutTime,
                                         Integer clockType, Integer lateMinutes, Long createBy) {
        LambdaQueryWrapper<HrAttendanceRecord> wrapper = new LambdaQueryWrapper<HrAttendanceRecord>()
                .eq(HrAttendanceRecord::getCompanyId, employee.getCompanyId())
                .eq(HrAttendanceRecord::getEmployeeId, employee.getId())
                .eq(HrAttendanceRecord::getAttendanceDay, attendanceDay);
        HrAttendanceRecord exist = attendanceRecordMapper.selectOne(wrapper);
        HrAttendanceRecord record;
        if (exist != null) {
            record = exist;
            record.setClockInTime(clockInTime);
            record.setClockOutTime(clockOutTime);
            record.setClockType(clockType);
            record.setLateMinutes(lateMinutes);
            record.setUpdateTime(LocalDateTime.now());
            attendanceRecordMapper.updateById(record);
        } else {
            record = new HrAttendanceRecord();
            record.setCompanyId(employee.getCompanyId());
            record.setEmployeeId(employee.getId());
            record.setEmployeeName(employee.getName());
            record.setAttendanceMonth(attendanceMonth);
            record.setAttendanceDay(attendanceDay);
            record.setClockInTime(clockInTime);
            record.setClockOutTime(clockOutTime);
            record.setClockType(clockType);
            record.setLateMinutes(lateMinutes);
            record.setWorkDays(1);
            record.setActualDays((clockType != null && clockType != 4 && clockType != 5) ? 1 : 0);
            record.setCreateBy(createBy);
            attendanceRecordMapper.insert(record);
        }
    }
    /** 检测考勤异常并写入异常记录 */
    private void detectAttendanceExceptions(String month, Long companyId, int lateThreshold, int absentThreshold, int earlyThreshold) {
        LocalDate monthStart = LocalDate.parse(month + "-01");
        int absentRemindThreshold = getConfigInt("attendance.absent_reminder_threshold", 3);
        int lateFrequentThreshold = getConfigInt("attendance.late_frequent_threshold", 5);

        // 1. 连续缺卡检测
        List<HrAttendanceRecord> absentRecords = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<HrAttendanceRecord>()
                        .eq(HrAttendanceRecord::getCompanyId, companyId)
                        .eq(HrAttendanceRecord::getAttendanceMonth, month)
                        .eq(HrAttendanceRecord::getClockType, 4));
        Map<Long, List<HrAttendanceRecord>> byEmployee = absentRecords.stream().collect(Collectors.groupingBy(HrAttendanceRecord::getEmployeeId));
        for (Map.Entry<Long, List<HrAttendanceRecord>> entry : byEmployee.entrySet()) {
            if (entry.getValue().size() >= absentRemindThreshold) {
                createExceptionRecord(companyId, entry.getKey(), 1, monthStart,
                        entry.getValue().size(), entry.getValue().stream().map(r -> r.getAttendanceDay().toString()).collect(Collectors.toList()));
            }
        }

        // 2. 月度迟到频繁检测
        List<HrAttendanceRecord> lateRecords = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<HrAttendanceRecord>()
                        .eq(HrAttendanceRecord::getCompanyId, companyId)
                        .eq(HrAttendanceRecord::getAttendanceMonth, month)
                        .eq(HrAttendanceRecord::getClockType, 2));
        Map<Long, List<HrAttendanceRecord>> lateByEmp = lateRecords.stream().collect(Collectors.groupingBy(HrAttendanceRecord::getEmployeeId));
        for (Map.Entry<Long, List<HrAttendanceRecord>> entry : lateByEmp.entrySet()) {
            if (entry.getValue().size() >= lateFrequentThreshold) {
                createExceptionRecord(companyId, entry.getKey(), 2, monthStart,
                        entry.getValue().size(), entry.getValue().stream().map(r -> r.getAttendanceDay().toString()).collect(Collectors.toList()));
            }
        }

        // 3. 旷工检测
        List<HrAttendanceRecord> workAbsentRecords = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<HrAttendanceRecord>()
                        .eq(HrAttendanceRecord::getCompanyId, companyId)
                        .eq(HrAttendanceRecord::getAttendanceMonth, month)
                        .eq(HrAttendanceRecord::getClockType, 5));
        Set<Long> workAbsentEmps = new HashSet<>();
        for (HrAttendanceRecord r : workAbsentRecords) {
            if (!workAbsentEmps.contains(r.getEmployeeId())) {
                workAbsentEmps.add(r.getEmployeeId());
                createExceptionRecord(companyId, r.getEmployeeId(), 3, monthStart, 1,
                        Collections.singletonList(r.getAttendanceDay().toString()));
            }
        }
        log.info("attendance exception detection done: month={}, absent={}, lateFrequent={}, workAbsent={}",
                month, byEmployee.entrySet().stream().filter(e -> e.getValue().size() >= absentRemindThreshold).count(),
                lateByEmp.entrySet().stream().filter(e -> e.getValue().size() >= lateFrequentThreshold).count(),
                workAbsentEmps.size());
    }

    /** 创建异常记录（幂等） */
    private void createExceptionRecord(Long companyId, Long employeeId, int exceptionType, LocalDate date, int detailCount, List<String> detailDates) {
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return;
        LambdaQueryWrapper<AttendanceException> check = new LambdaQueryWrapper<AttendanceException>()
                .eq(AttendanceException::getCompanyId, companyId)
                .eq(AttendanceException::getEmployeeId, employeeId)
                .eq(AttendanceException::getExceptionType, exceptionType)
                .eq(AttendanceException::getExceptionDate, date);
        Long exist = exceptionMapper.selectCount(check);
        if (exist != null && exist > 0) return;
        AttendanceException exception = new AttendanceException();
        exception.setCompanyId(companyId);
        exception.setEmployeeId(employeeId);
        exception.setEmployeeName(employee.getName());
        exception.setExceptionType(exceptionType);
        exception.setExceptionDate(date);
        exception.setDetailCount(detailCount);
        try { exception.setDetailJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(detailDates)); }
        catch (Exception e) { log.warn("序列化异常详情失败: {}", e.getMessage()); }
        exception.setStatus(0);
        exceptionMapper.insert(exception);
        log.info("attendance exception created: employee={}, type={}, date={}", employee.getName(), exceptionType, date);
    }

    /** 获取配置参数整数值 */
    private int getConfigInt(String key, int defaultValue) {
        String val = configService.getValueByKey(key);
        if (val == null) return defaultValue;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultValue; }
    }

    /** 根据班次类型获取默认时间配置 */
    private ShiftConfig getShiftConfigByType(Integer shiftType) {
        switch (shiftType) {
            case 2: return new ShiftConfig(2, LocalTime.parse(getConfigStr("attendance.shift_morning_start", "08:00")),
                    LocalTime.parse(getConfigStr("attendance.shift_morning_end", "17:00")));
            case 3: return new ShiftConfig(3, LocalTime.parse(getConfigStr("attendance.shift_evening_start", "14:00")),
                    LocalTime.parse(getConfigStr("attendance.shift_evening_end", "23:00")));
            case 4: return new ShiftConfig(4, LocalTime.parse(getConfigStr("attendance.shift_night_start", "22:00")),
                    LocalTime.parse(getConfigStr("attendance.shift_night_end", "07:00")));
            default: return new ShiftConfig(1, LocalTime.parse(getConfigStr("attendance.normal_clockin_start", "08:30")),
                    LocalTime.parse(getConfigStr("attendance.normal_clockout_end", "18:30")));
        }
    }

    /** 获取配置参数字符串值 */
    private String getConfigStr(String key, String defaultValue) {
        String val = configService.getValueByKey(key);
        return val != null ? val : defaultValue;
    }
    // ==================== VO转换方法 ====================

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
        long minutes = Duration.between(LocalTime.of(9, 0), clock).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    private String toClockTypeText(Integer type) {
        if (type == null) return null;
        switch (type) { case 1: return "正常"; case 2: return "迟到"; case 3: return "早退"; case 4: return "缺卡"; case 5: return "旷工"; default: return "未知"; }
    }

    private AttendanceExceptionVO toExceptionVO(AttendanceException entity) {
        AttendanceExceptionVO vo = new AttendanceExceptionVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setExceptionType(entity.getExceptionType());
        vo.setExceptionTypeText(toExceptionTypeText(entity.getExceptionType()));
        vo.setExceptionDate(entity.getExceptionDate());
        vo.setDetailCount(entity.getDetailCount());
        vo.setDetailJson(entity.getDetailJson());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(toStatusText(entity.getStatus()));
        vo.setHandleBy(entity.getHandleBy());
        vo.setHandleTime(entity.getHandleTime());
        vo.setHandleRemark(entity.getHandleRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toExceptionTypeText(Integer type) {
        if (type == null) return null;
        switch (type) { case 1: return "连续缺卡"; case 2: return "月度迟到频繁"; case 3: return "旷工"; case 4: return "早退频繁"; default: return "未知"; }
    }

    private String toStatusText(Integer status) {
        if (status == null) return null;
        switch (status) { case 0: return "待处理"; case 1: return "已确认"; case 2: return "已豁免"; case 3: return "已忽略"; default: return "未知"; }
    }

    private SysWorkweekConfigVO toWorkweekConfigVO(SysWorkweekConfig entity) {
        SysWorkweekConfigVO vo = new SysWorkweekConfigVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setConfigName(entity.getConfigName());
        vo.setWorkweekType(entity.getWorkweekType());
        vo.setWorkweekTypeText(toWorkweekTypeText(entity.getWorkweekType()));
        vo.setRestDayPattern(entity.getRestDayPattern());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(entity.getStatus() != null && entity.getStatus() == 1 ? "启用" : "禁用");
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toWorkweekTypeText(Integer type) {
        if (type == null) return null;
        switch (type) { case 1: return "单休"; case 2: return "双休"; case 3: return "做五休二"; case 4: return "做六休一"; case 5: return "综合工时"; default: return "未知"; }
    }

    private SysHolidayConfigVO toHolidayConfigVO(SysHolidayConfig entity) {
        SysHolidayConfigVO vo = new SysHolidayConfigVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setHolidayDate(entity.getHolidayDate());
        vo.setHolidayName(entity.getHolidayName());
        vo.setHolidayType(entity.getHolidayType());
        vo.setHolidayTypeText(toHolidayTypeText(entity.getHolidayType()));
        vo.setIsWorkday(entity.getIsWorkday());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String toHolidayTypeText(Integer type) {
        if (type == null) return null;
        switch (type) { case 1: return "法定假日"; case 2: return "调休日"; case 3: return "补班日"; default: return "未知"; }
    }

    private ShiftConfigVO toShiftConfigVO(HrEmployeeShift shift) {
        ShiftConfigVO vo = new ShiftConfigVO();
        vo.setShiftType(shift.getShiftType());
        vo.setShiftTypeText(toShiftTypeText(shift.getShiftType()));
        vo.setShiftStartTime(shift.getShiftStartTime());
        vo.setShiftEndTime(shift.getShiftEndTime());
        return vo;
    }

    private ShiftConfigVO toShiftConfigVOFromType(Integer shiftType) {
        ShiftConfig shift = getShiftConfigByType(shiftType);
        ShiftConfigVO vo = new ShiftConfigVO();
        vo.setShiftType(shift.getShiftType());
        vo.setShiftTypeText(toShiftTypeText(shift.getShiftType()));
        vo.setShiftStartTime(shift.getStartTime());
        vo.setShiftEndTime(shift.getEndTime());
        return vo;
    }

    private HrEmployeeShiftVO toShiftVO(HrEmployeeShift shift) {
        HrEmployeeShiftVO vo = new HrEmployeeShiftVO();
        vo.setId(shift.getId());
        vo.setCompanyId(shift.getCompanyId());
        vo.setEmployeeId(shift.getEmployeeId());
        vo.setShiftType(shift.getShiftType());
        vo.setShiftTypeText(toShiftTypeText(shift.getShiftType()));
        vo.setShiftStartTime(shift.getShiftStartTime());
        vo.setShiftEndTime(shift.getShiftEndTime());
        vo.setStartDate(shift.getStartDate());
        vo.setEndDate(shift.getEndDate());
        vo.setStatus(shift.getStatus());
        vo.setRemark(shift.getRemark());
        vo.setCreateTime(shift.getCreateTime());
        return vo;
    }

    private String toShiftTypeText(Integer type) {
        if (type == null) return null;
        switch (type) { case 1: return "标准工时"; case 2: return "早班"; case 3: return "晚班"; case 4: return "夜班"; case 5: return "综合工时"; default: return "未知"; }
    }

    /** 班次配置内部类 */
    @lombok.Data
    public static class ShiftConfig {
        private Integer shiftType;
        private LocalTime startTime;
        private LocalTime endTime;
        public ShiftConfig(Integer shiftType, LocalTime startTime, LocalTime endTime) {
            this.shiftType = shiftType;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}