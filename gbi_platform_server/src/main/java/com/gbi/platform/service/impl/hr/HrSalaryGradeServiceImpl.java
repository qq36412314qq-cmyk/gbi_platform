package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.entity.hr.HrSalaryGrade;
import com.gbi.platform.mapper.hr.HrSalaryGradeMapper;
import com.gbi.platform.service.hr.HrSalaryGradeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class HrSalaryGradeServiceImpl extends ServiceImpl<HrSalaryGradeMapper, HrSalaryGrade> implements HrSalaryGradeService {

    @Override
    public IPage<HrSalaryGrade> pageByCompany(Long companyId, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<HrSalaryGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSalaryGrade::getCompanyId, companyId)
               .eq(HrSalaryGrade::getStatus, 1)
               .eq(HrSalaryGrade::getIsDelete, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(HrSalaryGrade::getGradeCode, keyword)
                            .or().like(HrSalaryGrade::getGradeName, keyword));
        }
        wrapper.orderByAsc(HrSalaryGrade::getGradeLevel);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean isInBand(String gradeCode, BigDecimal salary, Long companyId) {
        if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        HrSalaryGrade grade = getOne(new LambdaQueryWrapper<HrSalaryGrade>()
                .eq(HrSalaryGrade::getCompanyId, companyId)
                .eq(HrSalaryGrade::getGradeCode, gradeCode)
                .eq(HrSalaryGrade::getStatus, 1)
                .eq(HrSalaryGrade::getIsDelete, 0)
                .last("LIMIT 1"));
        if (grade == null) return false;
        BigDecimal min = grade.getBandMin() != null ? grade.getBandMin() : BigDecimal.ZERO;
        BigDecimal max = grade.getBandMax() != null ? grade.getBandMax() : BigDecimal.valueOf(Double.MAX_VALUE);
        return salary.compareTo(min) >= 0 && salary.compareTo(max) <= 0;
    }
}
