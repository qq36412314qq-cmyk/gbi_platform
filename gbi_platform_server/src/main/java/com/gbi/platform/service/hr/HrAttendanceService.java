package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.AttendanceQueryDTO;
import com.gbi.platform.vo.hr.HrAttendanceVO;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrAttendanceService {
    PageVO<HrAttendanceVO> pageAttendance(AttendanceQueryDTO dto);
    int syncMonthly(String attendanceMonth);
    List<HrAttendanceVO> exportAttendance(Long employeeId, String attendanceMonth);
}
