package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.*;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.hr.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 加班管理服务接口：申请 / 记录 / 补偿 / 配置
 *
 * @author gbi
 */
public interface HrOvertimeService {

    // ==================== 加班申请 ====================
    /** 分页查询申请列表 */
    PageVO<HrOvertimeApplyVO> pageApply(HrOvertimeApplyQueryDTO dto);

    /** 新增加班申请（含发起审批流） */
    Long addApply(HrOvertimeApplyAddDTO dto);

    /** 编辑申请（仅待审批状态可编辑） */
    void editApply(HrOvertimeApplyEditDTO dto);

    /** 删除申请（仅待审批状态可删除） */
    void deleteApply(Long id);

    /** 撤回申请（仅待审批状态可撤回） */
    void revokeApply(Long id);

    // ==================== 加班记录 ====================
    /** 分页查询记录列表 */
    PageVO<HrOvertimeRecordVO> pageRecord(HrOvertimeRecordQueryDTO dto);

    /** 确认/驳回加班记录 */
    void confirmRecord(HrOvertimeRecordConfirmDTO dto);

    /** 手动触发自动识别（昨日） */
    HrOvertimeAutoDetectVO autoDetect();

    /** 手动触发指定日期自动识别 */
    HrOvertimeAutoDetectVO autoDetectByDate(LocalDate date);

    /** 导出加班记录 */
    void exportRecords(HrOvertimeRecordQueryDTO dto, HttpServletResponse response) throws IOException;

    // ==================== 加班补偿 ====================
    /** 分页查询补偿台账 */
    PageVO<HrOvertimeCompensateVO> pageCompensate(HrOvertimeCompensateQueryDTO dto);

    /** 核算当月加班补偿 */
    void calculateCompensate(HrOvertimeCompensateCalculateDTO dto);

    /** 发放加班费 */
    void payCompensate(Long id);

    /** 导出补偿明细 */
    void exportCompensate(HrOvertimeCompensateQueryDTO dto, HttpServletResponse response) throws IOException;

    // ==================== 配置管理 ====================
    /** 查询当前公司加班配置 */
    SysOvertimeConfigVO getConfig();

    /** 保存加班配置 */
    void saveConfig(SysOvertimeConfigSaveDTO dto);

    /** 更新配置状态（启用/禁用） */
    void updateConfigStatus(Long id, Integer status);
}
