package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.AttendanceQueryDTO;
import com.gbi.platform.hr.vo.HrAttendanceVO;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrAttendanceService {
    PageVO<HrAttendanceVO> pageAttendance(AttendanceQueryDTO dto);
    int syncMonthly(String attendanceMonth);
    List<HrAttendanceVO> exportAttendance(Long employeeId, String attendanceMonth);
}
