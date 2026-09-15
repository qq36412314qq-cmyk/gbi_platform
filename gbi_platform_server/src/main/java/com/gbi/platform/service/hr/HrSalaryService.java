package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.*;
import com.gbi.platform.vo.hr.*;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrSalaryService {
    PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId);
    /** 薪资档案分页（支持关键字、薪酬级别编码筛选） */
    PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId, String keyword, String gradeCode);
    /** 提交薪资档案变更去审批 */
    void submitArchiveAudit(Long archiveId);
    void addArchive(SalaryArchiveDTO dto);
    void updateArchive(SalaryArchiveDTO dto);
    void deleteArchive(Long id);
    PageVO<HrSalaryMonthVO> pageMonth(Long pageNum, Long pageSize, Long employeeId, String salaryMonth);
    void generateMonth(SalaryMonthDTO dto);
    void payMonth(Long id);
    List<HrSalaryMonthVO> exportMonth(Long employeeId, String salaryMonth);
}
