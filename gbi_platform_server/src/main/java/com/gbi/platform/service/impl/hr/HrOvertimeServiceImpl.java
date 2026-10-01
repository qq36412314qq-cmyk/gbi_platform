package com.gbi.platform.service.impl.hr;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.*;
import com.gbi.platform.entity.hr.*;
import com.gbi.platform.entity.sys.SysHolidayConfig;
import com.gbi.platform.mapper.hr.*;
import com.gbi.platform.mapper.sys.SysHolidayConfigMapper;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.hr.HrOvertimeService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.hr.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 加班管理服务实现
 * 支持：申请CRUD+审批流、记录自动识别、补偿核算、配置管理
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrOvertimeServiceImpl implements HrOvertimeService {

    private final HrOvertimeApplyMapper applyMapper;
    private final HrOvertimeRecordMapper recordMapper;
    private final HrOvertimeCompensateMapper compensateMapper;
    private final SysOvertimeConfigMapper configMapper;
    private final HrAttendanceRecordMapper attendanceRecordMapper;
    private final HrEmployeeShiftMapper shiftMapper;
    private final SysHolidayConfigMapper holidayConfigMapper;
    private final FlowEngineService flowEngineService;
    private final AuditLogUtil auditLogUtil;

    // ==================== 加班申请 ====================

    @Override
    public PageVO<HrOvertimeApplyVO> pageApply(HrOvertimeApplyQueryDTO dto) {
        Page<HrOvertimeApply> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HrOvertimeApply> wrapper = new LambdaQueryWrapper<HrOvertimeApply>()
                .eq(dto.getEmployeeId() != null, HrOvertimeApply::getEmployeeId, dto.getEmployeeId())
                .like(StringUtils.hasText(dto.getEmployeeName()), HrOvertimeApply::getEmployeeName, dto.getEmployeeName())
                .ge(dto.getOvertimeDateStart() != null, HrOvertimeApply::getOvertimeDate, dto.getOvertimeDateStart())
                .le(dto.getOvertimeDateEnd() != null, HrOvertimeApply::getOvertimeDate, dto.getOvertimeDateEnd())
                .eq(dto.getStatus() != null, HrOvertimeApply::getStatus, dto.getStatus())
                .orderByDesc(HrOvertimeApply::getCreateTime);
        Page<HrOvertimeApply> result = applyMapper.selectPage(page, wrapper);
        List<HrOvertimeApplyVO> voList = result.getRecords().stream()
                .map(this::toApplyVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addApply(HrOvertimeApplyAddDTO dto) {
        HrOvertimeApply apply = new HrOvertimeApply();
        apply.setCompanyId(UserContext.getCompanyIdOrZero());
        apply.setEmployeeId(dto.getEmployeeId());
        apply.setEmployeeName(dto.getEmployeeName());
        apply.setOvertimeDate(dto.getOvertimeDate());
        apply.setStartTime(dto.getStartTime());
        apply.setEndTime(dto.getEndTime());
        apply.setExpectedHours(dto.getExpectedHours());
        apply.setOvertimeType(dto.getOvertimeType() != null ? dto.getOvertimeType() : 1);
        apply.setReason(dto.getReason());
        apply.setStatus(0); // 待审批
        applyMapper.insert(apply);

        // 发起审批流
        String title = dto.getEmployeeName() + " 申请" + dto.getOvertimeDate() + " 加班 " + dto.getExpectedHours() + "小时";
        Long instanceId = flowEngineService.submit(
                CommonConst.FLOW_DEF_HR_OVERTIME_APPLY,
                "hr_overtime_apply",
                String.valueOf(apply.getId()),
                title
        );
        apply.setFlowInstanceId(instanceId);
        applyMapper.updateById(apply);

        auditLogUtil.record(CommonConst.MODULE_HR, "加班申请新增",
                String.valueOf(apply.getId()), null, apply);
        log.info("加班申请创建成功，id={}, employeeId={}, instanceId={}", apply.getId(), dto.getEmployeeId(), instanceId);
        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editApply(HrOvertimeApplyEditDTO dto) {
        HrOvertimeApply apply = applyMapper.selectById(dto.getId());
        if (apply == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "加班申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException(ResultCode.ERROR.getCode(), "仅待审批状态的申请可编辑");
        }
        HrOvertimeApply before = new HrOvertimeApply();
        BeanUtil.copyProperties(apply, before);
        if (dto.getExpectedHours() != null) {
            apply.setExpectedHours(dto.getExpectedHours());
        }
        if (StringUtils.hasText(dto.getReason())) {
            apply.setReason(dto.getReason());
        }
        applyMapper.updateById(apply);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班申请编辑",
                String.valueOf(apply.getId()), before, apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApply(Long id) {
        HrOvertimeApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "加班申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException(ResultCode.ERROR.getCode(), "仅待审批状态的申请可删除");
        }
        applyMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班申请删除", String.valueOf(id), apply, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeApply(Long id) {
        HrOvertimeApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "加班申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException(ResultCode.ERROR.getCode(), "仅待审批状态的申请可撤回");
        }
        if (apply.getFlowInstanceId() != null) {
            flowEngineService.revoke(apply.getFlowInstanceId());
        }
        apply.setStatus(3); // 已撤回
        applyMapper.updateById(apply);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班申请撤回", String.valueOf(id), apply, null);
        log.info("加班申请已撤回，id={}", id);
    }

    // ==================== 加班记录 ====================

    @Override
    public PageVO<HrOvertimeRecordVO> pageRecord(HrOvertimeRecordQueryDTO dto) {
        Page<HrOvertimeRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HrOvertimeRecord> wrapper = new LambdaQueryWrapper<HrOvertimeRecord>()
                .eq(dto.getEmployeeId() != null, HrOvertimeRecord::getEmployeeId, dto.getEmployeeId())
                .like(StringUtils.hasText(dto.getEmployeeName()), HrOvertimeRecord::getEmployeeName, dto.getEmployeeName())
                .ge(dto.getOvertimeDateStart() != null, HrOvertimeRecord::getOvertimeDate, dto.getOvertimeDateStart())
                .le(dto.getOvertimeDateEnd() != null, HrOvertimeRecord::getOvertimeDate, dto.getOvertimeDateEnd())
                .eq(dto.getSourceType() != null, HrOvertimeRecord::getSourceType, dto.getSourceType())
                .eq(dto.getConfirmStatus() != null, HrOvertimeRecord::getConfirmStatus, dto.getConfirmStatus())
                .orderByDesc(HrOvertimeRecord::getCreateTime);
        Page<HrOvertimeRecord> result = recordMapper.selectPage(page, wrapper);
        List<HrOvertimeRecordVO> voList = result.getRecords().stream()
                .map(this::toRecordVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRecord(HrOvertimeRecordConfirmDTO dto) {
        HrOvertimeRecord record = recordMapper.selectById(dto.getId());
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "加班记录不存在");
        }
        if (record.getConfirmStatus() != 0) {
            throw new BizException(ResultCode.ERROR.getCode(), "该记录已处理，无法重复操作");
        }
        HrOvertimeRecord before = new HrOvertimeRecord();
        BeanUtil.copyProperties(record, before);
        record.setConfirmStatus(dto.getConfirmStatus());
        record.setConfirmTime(LocalDateTime.now());
        if (dto.getConfirmStatus() == 1) {
            record.setStatus(1); // 有效
        } else {
            record.setStatus(3); // 已作废
        }
        recordMapper.updateById(record);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班记录" + (dto.getConfirmStatus() == 1 ? "确认" : "驳回"),
                String.valueOf(record.getId()), before, record);
        log.info("加班记录{}，id={}", dto.getConfirmStatus() == 1 ? "已确认" : "已驳回", record.getId());
    }

    @Override
    public HrOvertimeAutoDetectVO autoDetect() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return autoDetectByDate(yesterday);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HrOvertimeAutoDetectVO autoDetectByDate(LocalDate date) {
        log.info("开始自动识别加班，date={}", date);
        try {
            Long companyId = UserContext.getCompanyIdOrZero();

            // 1. 幂等检查：当日已有自动识别记录则跳过
            long existCount = recordMapper.selectCount(new LambdaQueryWrapper<HrOvertimeRecord>()
                    .eq(HrOvertimeRecord::getOvertimeDate, date)
                    .eq(HrOvertimeRecord::getCompanyId, companyId)
                    .eq(HrOvertimeRecord::getSourceType, 2));
            if (existCount > 0) {
                log.info("当日已有自动识别记录，跳过，date={}, existCount={}", date, existCount);
                HrOvertimeAutoDetectVO vo = new HrOvertimeAutoDetectVO();
                vo.setStatus("SUCCESS");
                vo.setSkippedRecords((int) existCount);
                vo.setErrorMsg("当日已有自动识别记录，跳过");
                return vo;
            }

            // 2. 读取加班配置
            SysOvertimeConfig config = getConfig(companyId);
            if (config == null || !Integer.valueOf(1).equals(config.getStatus())) {
                log.warn("加班配置未启用，跳过自动识别，companyId={}", companyId);
                return buildErrorVO("加班配置未启用或不存在");
            }
            if (!Integer.valueOf(1).equals(config.getAutoDetectEnabled())) {
                log.info("自动识别开关关闭，跳过，companyId={}", companyId);
                return buildSuccessVO(0, 0, 0, 0);
            }

            // 3. 查询当日考勤记录（有下班打卡的）
            List<HrAttendanceRecord> attendanceList = attendanceRecordMapper.selectList(
                    new LambdaQueryWrapper<HrAttendanceRecord>()
                            .eq(HrAttendanceRecord::getCompanyId, companyId)
                            .eq(HrAttendanceRecord::getAttendanceDay, date)
                            .isNotNull(HrAttendanceRecord::getClockOutTime)
                            .orderByAsc(HrAttendanceRecord::getEmployeeId));
            if (attendanceList.isEmpty()) {
                log.info("当日无有效考勤记录，date={}", date);
                return buildSuccessVO(0, 0, 0, 0);
            }

            // 4. 判断当天加班类型（工作日/休息日/法定节假日）
            int overtimeType = determineOvertimeType(date, companyId);
            log.info("日期={} 加班类型={}（1工作日 2休息日 3法定节假日）", date, overtimeType);

            // 5. 逐条计算
            int totalRecords = attendanceList.size();
            int newRecords = 0;
            int skippedRecords = 0;

            BigDecimal minHours = config.getOvertimeMinHours() != null
                    ? config.getOvertimeMinHours()
                    : BigDecimal.ZERO;
            int roundMode = config.getOvertimeRoundMode() != null
                    ? config.getOvertimeRoundMode()
                    : 2; // 默认四舍五入

            for (HrAttendanceRecord att : attendanceList) {
                // 查排班
                List<HrEmployeeShift> shifts = shiftMapper.selectActiveShifts(att.getEmployeeId(), date);
                if (shifts.isEmpty()) {
                    log.debug("员工={} 当日无生效班次，跳过", att.getEmployeeId());
                    skippedRecords++;
                    continue;
                }
                HrEmployeeShift shift = shifts.get(0);

                // 计算加班时长（分钟）
                LocalDateTime shiftEndDateTime = date.atTime(shift.getShiftEndTime());
                LocalDateTime clockOut = att.getClockOutTime();
                long seconds = Duration.between(shiftEndDateTime, clockOut).getSeconds();
                if (seconds <= 0) {
                    // 未超过班次下班时间，无加班
                    skippedRecords++;
                    continue;
                }

                // 转换为小时并取整
                double hours = seconds / 3600.0;
                BigDecimal overtimeHours = roundToHours(hours, roundMode);

                // 检查最低阈值
                if (overtimeHours.compareTo(minHours) < 0) {
                    log.debug("员工={} 加班时长{}小时低于阈值{}，跳过", att.getEmployeeId(), overtimeHours, minHours);
                    skippedRecords++;
                    continue;
                }

                // 生成加班记录
                HrOvertimeRecord record = new HrOvertimeRecord();
                record.setCompanyId(companyId);
                record.setEmployeeId(att.getEmployeeId());
                record.setEmployeeName(att.getEmployeeName());
                record.setOvertimeDate(date);
                record.setStartTime(shift.getShiftEndTime());
                record.setEndTime(clockOut.toLocalTime());
                record.setOvertimeHours(overtimeHours);
                record.setOvertimeType(overtimeType);
                record.setSourceType(2); // 自动识别
                record.setAttendRecordId(att.getId());
                record.setConfirmStatus(0); // 待确认
                record.setStatus(1); // 有效
                record.setCompensateStatus(0); // 未补偿
                record.setCreateTime(LocalDateTime.now());
                recordMapper.insert(record);
                newRecords++;
                log.debug("员工={} 识别到加班 {} 小时，类型={}", att.getEmployeeId(), overtimeHours, overtimeType);
            }

            log.info("自动识别完成，date={}, total={}, new={}, skipped={}", date, totalRecords, newRecords, skippedRecords);
            return buildSuccessVO(totalRecords, newRecords, 0, skippedRecords);

        } catch (Exception e) {
            log.error("自动识别加班异常，date={}", date, e);
            return buildErrorVO(e.getMessage());
        }
    }

    /**
     * 判断指定日期的加班类型
     *
     * @return 1=工作日 2=休息日 3=法定节假日
     */
    private int determineOvertimeType(LocalDate date, Long companyId) {
        // 优先查询节假日配置
        List<SysHolidayConfig> holidays = holidayConfigMapper.selectByDateRange(date, date, companyId);
        if (!holidays.isEmpty()) {
            SysHolidayConfig h = holidays.get(0);
            if (Integer.valueOf(1).equals(h.getHolidayType())) {
                return 3; // 法定节假日
            }
            if (Integer.valueOf(2).equals(h.getHolidayType())) {
                return 2; // 调休日=休息日
            }
            if (Integer.valueOf(3).equals(h.getHolidayType())) {
                // 补班日 → 按工作日处理
                return Integer.valueOf(1).equals(h.getIsWorkday()) ? 1 : 2;
            }
        }
        // 无节假日配置：周末为休息日
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        return (dayOfWeek == 6 || dayOfWeek == 7) ? 2 : 1;
    }

    /**
     * 按配置模式将秒数换算为小时并取整
     *
     * @param mode 1向上取整 2四舍五入 3向下取整
     */
    private BigDecimal roundToHours(double hours, int mode) {
        switch (mode) {
            case 1: return BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_UP);
            case 2: return BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
            case 3: default: return BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_DOWN);
        }
    }

    @Override
    public void exportRecords(HrOvertimeRecordQueryDTO dto, HttpServletResponse response) throws IOException {
        // TODO: Phase 7 实现 Excel 导出（使用 hutool ExcelUtil）
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"导出功能开发中，将在 Phase 7 实现\"}");
    }

    // ==================== 加班补偿 ====================

    @Override
    public PageVO<HrOvertimeCompensateVO> pageCompensate(HrOvertimeCompensateQueryDTO dto) {
        Page<HrOvertimeCompensate> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HrOvertimeCompensate> wrapper = new LambdaQueryWrapper<HrOvertimeCompensate>()
                .eq(dto.getEmployeeId() != null, HrOvertimeCompensate::getEmployeeId, dto.getEmployeeId())
                .like(StringUtils.hasText(dto.getEmployeeName()), HrOvertimeCompensate::getEmployeeName, dto.getEmployeeName())
                .eq(StringUtils.hasText(dto.getCompensateMonth()), HrOvertimeCompensate::getCompensateMonth, dto.getCompensateMonth())
                .eq(dto.getPayStatus() != null, HrOvertimeCompensate::getPayStatus, dto.getPayStatus())
                .orderByDesc(HrOvertimeCompensate::getCompensateMonth);
        Page<HrOvertimeCompensate> result = compensateMapper.selectPage(page, wrapper);
        List<HrOvertimeCompensateVO> voList = result.getRecords().stream()
                .map(this::toCompensateVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateCompensate(HrOvertimeCompensateCalculateDTO dto) {
        String month = dto.getCompensateMonth();
        Long companyId = UserContext.getCompanyIdOrZero();
        log.info("开始核算加班补偿，month={}, companyId={}", month, companyId);

        SysOvertimeConfig config = getConfig(companyId);
        if (config == null) {
            throw new BizException(ResultCode.ERROR.getCode(), "未找到加班配置，无法核算");
        }

        // TODO: Phase 2 完整实现
        // 1. 聚合当月 confirm_status=1 且 status=1 的加班记录（按员工维度）
        // 2. 关联 hr_employee 获取 basicSalary
        // 3. 计算：日薪=basicSalary/21.75，时薪=日薪/8
        // 4. 工作日加班费=时薪×hours×workdayRate；休息日=×restdayRate；节假日=×holidayRate
        // 5. 幂等写入 hr_overtime_compensate（uk_employee_month 唯一键，已存在则 UPDATE）

        auditLogUtil.record(CommonConst.MODULE_HR, "加班补偿核算", month, null, null);
        log.info("加班补偿核算完成（占位），month={}", month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payCompensate(Long id) {
        HrOvertimeCompensate comp = compensateMapper.selectById(id);
        if (comp == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "补偿记录不存在");
        }
        if (comp.getPayStatus() != 0) {
            throw new BizException(ResultCode.ERROR.getCode(), "该记录已发放，无需重复操作");
        }
        HrOvertimeCompensate before = new HrOvertimeCompensate();
        BeanUtil.copyProperties(comp, before);
        comp.setPayStatus(1); // 已发放
        comp.setPayTime(LocalDateTime.now());
        compensateMapper.updateById(comp);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班费发放", String.valueOf(id), before, comp);
        log.info("加班费发放成功，id={}, amount={}", id, comp.getOvertimeAmount());
        // TODO: Phase 2 写财务流水（biz_finance_flow）
    }

    @Override
    public void exportCompensate(HrOvertimeCompensateQueryDTO dto, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"导出功能开发中\"}");
    }

    // ==================== 配置管理 ====================

    @Override
    public SysOvertimeConfigVO getConfig() {
        Long companyId = UserContext.getCompanyIdOrZero();
        SysOvertimeConfig config = getConfig(companyId);
        if (config == null) {
            log.warn("未找到加班配置，companyId={}", companyId);
            return null;
        }
        return toConfigVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(SysOvertimeConfigSaveDTO dto) {
        SysOvertimeConfig config = configMapper.selectById(dto.getId());
        if (config == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "配置不存在");
        }
        SysOvertimeConfig before = new SysOvertimeConfig();
        BeanUtil.copyProperties(config, before);
        if (StringUtils.hasText(dto.getConfigName())) {
            config.setConfigName(dto.getConfigName());
        }
        if (dto.getOvertimeMinHours() != null) {
            config.setOvertimeMinHours(dto.getOvertimeMinHours());
        }
        if (dto.getOvertimeRoundMode() != null) {
            config.setOvertimeRoundMode(dto.getOvertimeRoundMode());
        }
        if (dto.getWorkdayRate() != null) {
            config.setWorkdayRate(dto.getWorkdayRate());
        }
        if (dto.getRestdayRate() != null) {
            config.setRestdayRate(dto.getRestdayRate());
        }
        if (dto.getHolidayRate() != null) {
            config.setHolidayRate(dto.getHolidayRate());
        }
        config.setMaxOvertimeHours(dto.getMaxOvertimeHours());
        if (dto.getCompensatePriority() != null) {
            config.setCompensatePriority(dto.getCompensatePriority());
        }
        if (dto.getStatus() != null) {
            config.setStatus(dto.getStatus());
        }
        if (StringUtils.hasText(dto.getRemark())) {
            config.setRemark(dto.getRemark());
        }
        configMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班配置保存", String.valueOf(config.getId()), before, config);
        log.info("加班配置已更新，id={}", config.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigStatus(Long id, Integer status) {
        SysOvertimeConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "配置不存在");
        }
        SysOvertimeConfig before = new SysOvertimeConfig();
        BeanUtil.copyProperties(config, before);
        config.setStatus(status);
        configMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_HR, "加班配置状态变更", String.valueOf(id), before, config);
        log.info("加班配置状态已更新，id={}, status={}", id, status);
    }

    // ==================== 私有方法 ====================

    private HrOvertimeApplyVO toApplyVO(HrOvertimeApply apply) {
        HrOvertimeApplyVO vo = new HrOvertimeApplyVO();
        vo.setId(apply.getId());
        vo.setCompanyId(apply.getCompanyId());
        vo.setEmployeeId(apply.getEmployeeId());
        vo.setEmployeeName(apply.getEmployeeName());
        vo.setOvertimeDate(apply.getOvertimeDate());
        vo.setStartTime(apply.getStartTime());
        vo.setEndTime(apply.getEndTime());
        vo.setExpectedHours(apply.getExpectedHours());
        vo.setOvertimeType(apply.getOvertimeType());
        vo.setReason(apply.getReason());
        vo.setStatus(apply.getStatus());
        vo.setFlowInstanceId(apply.getFlowInstanceId());
        vo.setCreateTime(apply.getCreateTime());
        return vo;
    }

    private HrOvertimeRecordVO toRecordVO(HrOvertimeRecord record) {
        HrOvertimeRecordVO vo = new HrOvertimeRecordVO();
        vo.setId(record.getId());
        vo.setCompanyId(record.getCompanyId());
        vo.setEmployeeId(record.getEmployeeId());
        vo.setEmployeeName(record.getEmployeeName());
        vo.setOvertimeDate(record.getOvertimeDate());
        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setOvertimeHours(record.getOvertimeHours());
        vo.setOvertimeType(record.getOvertimeType());
        vo.setSourceType(record.getSourceType());
        vo.setApplyId(record.getApplyId());
        vo.setAttendRecordId(record.getAttendRecordId());
        vo.setConfirmStatus(record.getConfirmStatus());
        vo.setConfirmTime(record.getConfirmTime());
        vo.setStatus(record.getStatus());
        vo.setCompensateStatus(record.getCompensateStatus());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private HrOvertimeCompensateVO toCompensateVO(HrOvertimeCompensate comp) {
        HrOvertimeCompensateVO vo = new HrOvertimeCompensateVO();
        vo.setId(comp.getId());
        vo.setCompanyId(comp.getCompanyId());
        vo.setEmployeeId(comp.getEmployeeId());
        vo.setEmployeeName(comp.getEmployeeName());
        vo.setBasicSalary(comp.getBasicSalary());
        vo.setCompensateMonth(comp.getCompensateMonth());
        vo.setTotalHours(comp.getTotalHours());
        vo.setWorkdayHours(comp.getWorkdayHours());
        vo.setRestdayHours(comp.getRestdayHours());
        vo.setHolidayHours(comp.getHolidayHours());
        vo.setUsedHours(comp.getUsedHours());
        vo.setRemainHours(comp.getRemainHours());
        vo.setCompensateType(comp.getCompensateType());
        vo.setOvertimeAmount(comp.getOvertimeAmount());
        vo.setPayStatus(comp.getPayStatus());
        vo.setPayTime(comp.getPayTime());
        vo.setRemark(comp.getRemark());
        vo.setCreateTime(comp.getCreateTime());
        return vo;
    }

    private SysOvertimeConfigVO toConfigVO(SysOvertimeConfig config) {
        SysOvertimeConfigVO vo = new SysOvertimeConfigVO();
        vo.setId(config.getId());
        vo.setCompanyId(config.getCompanyId());
        vo.setConfigName(config.getConfigName());
        vo.setOvertimeMinHours(config.getOvertimeMinHours());
        vo.setOvertimeRoundMode(config.getOvertimeRoundMode());
        vo.setWorkdayRate(config.getWorkdayRate());
        vo.setRestdayRate(config.getRestdayRate());
        vo.setHolidayRate(config.getHolidayRate());
        vo.setMaxOvertimeHours(config.getMaxOvertimeHours());
        vo.setCompensatePriority(config.getCompensatePriority());
        vo.setAutoDetectEnabled(config.getAutoDetectEnabled());
        vo.setAutoDetectCron(config.getAutoDetectCron());
        vo.setStatus(config.getStatus());
        vo.setRemark(config.getRemark());
        return vo;
    }

    private SysOvertimeConfig getConfig(Long companyId) {
        return configMapper.selectOne(new LambdaQueryWrapper<SysOvertimeConfig>()
                .eq(SysOvertimeConfig::getCompanyId, companyId)
                .last("LIMIT 1"));
    }

    private HrOvertimeAutoDetectVO buildSuccessVO(int totalRecords, int newRecords, int updatedRecords, int skippedRecords) {
        HrOvertimeAutoDetectVO vo = new HrOvertimeAutoDetectVO();
        vo.setStatus("SUCCESS");
        vo.setTotalRecords(totalRecords);
        vo.setNewRecords(newRecords);
        vo.setUpdatedRecords(updatedRecords);
        vo.setSkippedRecords(skippedRecords);
        return vo;
    }

    private HrOvertimeAutoDetectVO buildErrorVO(String msg) {
        HrOvertimeAutoDetectVO vo = new HrOvertimeAutoDetectVO();
        vo.setStatus("FAILED");
        vo.setErrorMsg(msg);
        return vo;
    }
}
