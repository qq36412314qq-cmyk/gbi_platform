package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.hr.HrSocialParamDTO;
import com.gbi.platform.entity.hr.HrInsuranceType;
import com.gbi.platform.entity.hr.HrSocialParamConfig;
import com.gbi.platform.mapper.hr.HrInsuranceTypeMapper;
import com.gbi.platform.mapper.hr.HrSocialParamMapper;
import com.gbi.platform.service.hr.HrSocialParamService;
import com.gbi.platform.vo.hr.HrSocialParamVO;
import com.gbi.platform.util.AuditLogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社保公积金参数配置服务实现
 */
@Service
@RequiredArgsConstructor
public class HrSocialParamServiceImpl extends ServiceImpl<HrSocialParamMapper, HrSocialParamConfig> implements HrSocialParamService {

    private final AuditLogUtil auditLogUtil;
    private final HrInsuranceTypeMapper insuranceTypeMapper;

    @Override
    public IPage<HrSocialParamVO> page(Integer pageNum, Integer pageSize, String cityCode,
                                        String insuranceCode, Long companyId) {
        LambdaQueryWrapper<HrSocialParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSocialParamConfig::getIsDelete, 0);
        if (companyId != null) {
            wrapper.eq(HrSocialParamConfig::getCompanyId, companyId);
        } else {
            wrapper.eq(HrSocialParamConfig::getCompanyId, CommonConst.COMPANY_ROOT);
        }
        if (cityCode != null && !cityCode.isEmpty()) {
            wrapper.eq(HrSocialParamConfig::getCityCode, cityCode);
        }
        if (insuranceCode != null && !insuranceCode.isEmpty()) {
            wrapper.eq(HrSocialParamConfig::getInsuranceCode, insuranceCode);
        }
        wrapper.orderByDesc(HrSocialParamConfig::getIsActive)
               .orderByDesc(HrSocialParamConfig::getPeriodStart);
        IPage<HrSocialParamConfig> configPage = page(new Page<>(pageNum, pageSize), wrapper);

        // 批量查询险种名称
        List<String> insuranceCodes = configPage.getRecords().stream()
                .map(HrSocialParamConfig::getInsuranceCode)
                .distinct()
                .collect(Collectors.toList());
        List<HrInsuranceType> insuranceTypes = insuranceTypeMapper.selectList(
                new LambdaQueryWrapper<HrInsuranceType>().in(HrInsuranceType::getInsuranceCode, insuranceCodes));
        Map<String, String> insuranceNameMap = insuranceTypes.stream()
                .collect(Collectors.toMap(HrInsuranceType::getInsuranceCode, HrInsuranceType::getInsuranceName, (a, b) -> a));

        return configPage.convert(item -> {
            HrSocialParamVO vo = new HrSocialParamVO();
            vo.setId(item.getId());
            vo.setCompanyId(item.getCompanyId());
            vo.setCityCode(item.getCityCode());
            vo.setInsuranceCode(item.getInsuranceCode());
            vo.setInsuranceName(insuranceNameMap.get(item.getInsuranceCode()));
            vo.setIndustryCode(item.getIndustryCode());
            vo.setPeriodStart(item.getPeriodStart());
            vo.setPeriodEnd(item.getPeriodEnd());
            vo.setBaseMin(item.getBaseMin());
            vo.setBaseMax(item.getBaseMax());
            vo.setPersonalRate(item.getPersonalRate());
            vo.setCompanyRate(item.getCompanyRate());
            vo.setIsActive(item.getIsActive());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime() != null ? item.getCreateTime().toString() : null);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HrSocialParamDTO dto, Long companyId) {
        HrSocialParamConfig config = convertToEntity(dto, companyId);
        config.setIsActive(1);
        save(config);
        auditLogUtil.record(
                CommonConst.MODULE_HR_SOCIAL_PARAM,
                CommonConst.OPER_TYPE_PARAM_ADD,
                String.valueOf(config.getId()),
                null,
                config
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(HrSocialParamDTO dto, Long companyId) {
        HrSocialParamConfig config = getById(dto.getId());
        if (config == null) { throw new BizException("记录不存在"); }
        config.setCityCode(dto.getCityCode());
        config.setInsuranceCode(dto.getInsuranceCode());
        config.setIndustryCode(dto.getIndustryCode());
        config.setPeriodStart(dto.getPeriodStart());
        config.setPeriodEnd(dto.getPeriodEnd());
        config.setBaseMin(dto.getBaseMin());
        config.setBaseMax(dto.getBaseMax());
        config.setPersonalRate(dto.getPersonalRate());
        config.setCompanyRate(dto.getCompanyRate());
        config.setRemark(dto.getRemark());
        updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id, Long companyId) {
        HrSocialParamConfig newConfig = getById(id);
        if (newConfig == null) {
            throw new BizException("参数配置不存在");
        }
        if (!companyId.equals(newConfig.getCompanyId())) {
            throw new BizException("无权操作该数据");
        }

        // 查找同城市同险种当前生效记录并停用
        LambdaQueryWrapper<HrSocialParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrSocialParamConfig::getCityCode, newConfig.getCityCode())
               .eq(HrSocialParamConfig::getInsuranceCode, newConfig.getInsuranceCode())
               .eq(HrSocialParamConfig::getIndustryCode, newConfig.getIndustryCode())
               .eq(HrSocialParamConfig::getCompanyId, companyId)
               .eq(HrSocialParamConfig::getIsActive, 1)
               .eq(HrSocialParamConfig::getIsDelete, 0);
        HrSocialParamConfig oldConfig = getOne(wrapper, false);

        if (oldConfig != null && !oldConfig.getId().equals(id)) {
            oldConfig.setIsActive(0);
            updateById(oldConfig);
            auditLogUtil.record(
                    CommonConst.MODULE_HR_SOCIAL_PARAM,
                    CommonConst.OPER_TYPE_PARAM_DEACTIVATE,
                    String.valueOf(oldConfig.getId()),
                    oldConfig,
                    oldConfig
            );
        }

        newConfig.setIsActive(1);
        updateById(newConfig);
        auditLogUtil.record(
                CommonConst.MODULE_HR_SOCIAL_PARAM,
                CommonConst.OPER_TYPE_PARAM_ACTIVATE,
                String.valueOf(newConfig.getId()),
                null,
                newConfig
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggle(Long id, Long companyId) {
        HrSocialParamConfig config = getById(id);
        if (config == null) {
            throw new BizException("参数配置不存在");
        }
        if (!companyId.equals(config.getCompanyId())) {
            throw new BizException("无权操作该数据");
        }
        // 切换状态：生效中->已停用，已停用->生效中
        Integer newStatus = config.getIsActive() == 1 ? 0 : 1;
        config.setIsActive(newStatus);
        updateById(config);
        auditLogUtil.record(
                CommonConst.MODULE_HR_SOCIAL_PARAM,
                newStatus == 1 ? CommonConst.OPER_TYPE_PARAM_ACTIVATE : CommonConst.OPER_TYPE_PARAM_DEACTIVATE,
                String.valueOf(id),
                config,
                config
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long companyId) {
        HrSocialParamConfig config = getById(id);
        if (config == null) {
            throw new BizException("参数配置不存在");
        }
        if (!companyId.equals(config.getCompanyId())) {
            throw new BizException("无权操作该数据");
        }
        removeById(id);
        auditLogUtil.record(
                CommonConst.MODULE_HR_SOCIAL_PARAM,
                CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id),
                config,
                null
        );
    }

    private HrSocialParamConfig convertToEntity(HrSocialParamDTO dto, Long companyId) {
        HrSocialParamConfig config = new HrSocialParamConfig();
        config.setCompanyId(companyId);
        config.setCityCode(dto.getCityCode());
        config.setInsuranceCode(dto.getInsuranceCode());
        config.setIndustryCode(dto.getIndustryCode());
        config.setPeriodStart(dto.getPeriodStart());
        config.setPeriodEnd(dto.getPeriodEnd());
        config.setBaseMin(dto.getBaseMin());
        config.setBaseMax(dto.getBaseMax());
        config.setPersonalRate(dto.getPersonalRate());
        config.setCompanyRate(dto.getCompanyRate());
        config.setRemark(dto.getRemark());
        return config;
    }
}
