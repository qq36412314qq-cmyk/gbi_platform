package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.vo.*;
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
}
