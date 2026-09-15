package com.gbi.platform.service.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gbi.platform.dto.hr.HrAnnualRecalcDTO;
import com.gbi.platform.vo.hr.HrSocialCalcDetailVO;

import java.util.Map;

/**
 * 社保核算服务接口
 */
public interface HrSocialCalcService {

    /**
     * 分页查询核算明细
     */
    IPage<HrSocialCalcDetailVO> page(Integer pageNum, Integer pageSize, String cityCode,
                                      String salaryMonth, Long companyId);

    /**
     * 触发年度基数重算
     */
    Map<String, Object> triggerAnnualRecalc(HrAnnualRecalcDTO dto, Long companyId);

    /**
     * 月度薪资核算（生成hr_salary_month+hr_social_calc_detail）
     */
    void generateMonthSalary(String salaryMonth, Long companyId);
}
