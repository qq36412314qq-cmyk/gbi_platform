package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.entity.hr.HrIndustry;
import com.gbi.platform.mapper.hr.HrIndustryMapper;
import com.gbi.platform.service.hr.HrIndustryService;
import org.springframework.stereotype.Service;

@Service
public class HrIndustryServiceImpl extends ServiceImpl<HrIndustryMapper, HrIndustry> implements HrIndustryService {
}
