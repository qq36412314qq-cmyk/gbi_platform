package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.entity.hr.HrCity;
import com.gbi.platform.mapper.hr.HrCityMapper;
import com.gbi.platform.service.hr.HrCityService;
import org.springframework.stereotype.Service;

@Service
public class HrCityServiceImpl extends ServiceImpl<HrCityMapper, HrCity> implements HrCityService {
}
