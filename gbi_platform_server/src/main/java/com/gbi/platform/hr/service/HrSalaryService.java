package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrSalaryService {
    PageVO<HrSalaryArchiveVO> pageArchive(Long pageNum, Long pageSize, Long employeeId);
    void addArchive(SalaryArchiveDTO dto);
    void updateArchive(SalaryArchiveDTO dto);
    void deleteArchive(Long id);
    PageVO<HrSalaryMonthVO> pageMonth(Long pageNum, Long pageSize, Long employeeId, String salaryMonth);
    void generateMonth(SalaryMonthDTO dto);
    void payMonth(Long id);
    List<HrSalaryMonthVO> exportMonth(Long employeeId, String salaryMonth);
}
