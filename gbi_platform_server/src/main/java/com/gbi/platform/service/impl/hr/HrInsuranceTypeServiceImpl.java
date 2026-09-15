package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.entity.hr.HrInsuranceType;
import com.gbi.platform.mapper.hr.HrInsuranceTypeMapper;
import com.gbi.platform.service.hr.HrInsuranceTypeService;
import org.springframework.stereotype.Service;

@Service
public class HrInsuranceTypeServiceImpl extends ServiceImpl<HrInsuranceTypeMapper, HrInsuranceType> implements HrInsuranceTypeService {
}
