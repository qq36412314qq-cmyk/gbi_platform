package com.gbi.platform.service.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gbi.platform.entity.hr.HrSalaryGrade;

import java.math.BigDecimal;

public interface HrSalaryGradeService extends IService<HrSalaryGrade> {
    /** 分页查询 */
    IPage<HrSalaryGrade> pageByCompany(Long companyId, String keyword, Integer pageNum, Integer pageSize);
    /** 带宽校验：薪资是否在带宽内 */
    boolean isInBand(String gradeCode, BigDecimal salary, Long companyId);
}
