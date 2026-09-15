package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.*;
import com.gbi.platform.vo.hr.*;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrEmployeeService {
    PageVO<HrEmployeeVO> page(Long pageNum, Long pageSize, String name, String employeeNo, Integer employeeStatus);
    HrEmployeeVO get(Long id);
    Long add(EmployeeDTO dto);
    void update(EmployeeDTO dto);
    void delete(Long id);
    List<HrEmployeeVO> export(List<Long> ids);
    void createFromEntry(Long entryApplyId);

    // ==================== 工作经历 ====================
    List<WorkExpVO> getWorkExps(Long employeeId);
    void addWorkExp(WorkExpDTO dto);
    void updateWorkExp(WorkExpDTO dto);
    void deleteWorkExp(Long id);

    // ==================== 学业经历 ====================
    List<EduExpVO> getEduExps(Long employeeId);
    void addEduExp(EduExpDTO dto);
    void updateEduExp(EduExpDTO dto);
    void deleteEduExp(Long id);
}
