package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.hr.HrHousingFundDTO;
import com.gbi.platform.entity.hr.HrHousingFundConfig;
import com.gbi.platform.mapper.hr.HrHousingFundConfigMapper;
import com.gbi.platform.service.hr.HrHousingFundConfigService;
import com.gbi.platform.vo.hr.HrHousingFundVO;
import com.gbi.platform.util.AuditLogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 公积金参数配置服务实现
 */
@Service
@RequiredArgsConstructor
public class HrHousingFundConfigServiceImpl extends ServiceImpl<HrHousingFundConfigMapper, HrHousingFundConfig> implements HrHousingFundConfigService {

    private final AuditLogUtil auditLogUtil;

    @Override
    public IPage<HrHousingFundVO> page(Integer pageNum, Integer pageSize, String cityCode, Long companyId) {
        LambdaQueryWrapper<HrHousingFundConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrHousingFundConfig::getIsDelete, 0);
        if (companyId != null) {
            wrapper.eq(HrHousingFundConfig::getCompanyId, companyId);
        } else {
            wrapper.eq(HrHousingFundConfig::getCompanyId, CommonConst.COMPANY_ROOT);
        }
        if (cityCode != null && !cityCode.isEmpty()) {
            wrapper.eq(HrHousingFundConfig::getCityCode, cityCode);
        }
        wrapper.orderByDesc(HrHousingFundConfig::getIsActive)
               .orderByDesc(HrHousingFundConfig::getPeriodStart);
        IPage<HrHousingFundConfig> page = page(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(item -> {
            HrHousingFundVO vo = new HrHousingFundVO();
            vo.setId(item.getId());
            vo.setCompanyId(item.getCompanyId());
            vo.setCityCode(item.getCityCode());
            vo.setPeriodStart(item.getPeriodStart());
            vo.setPeriodEnd(item.getPeriodEnd());
            vo.setBaseMin(item.getBaseMin());
            vo.setBaseMax(item.getBaseMax());
            vo.setEmployeeRate(item.getEmployeeRate());
            vo.setCompanyRate(item.getCompanyRate());
            vo.setIsActive(item.getIsActive());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime() != null ? item.getCreateTime().toString() : null);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HrHousingFundDTO dto, Long companyId) {
        HrHousingFundConfig config = convertToEntity(dto, companyId);
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
    public void activate(Long id, Long companyId) {
        HrHousingFundConfig newConfig = getById(id);
        if (newConfig == null) {
            throw new BizException("公积金参数配置不存在");
        }
        if (!companyId.equals(newConfig.getCompanyId())) {
            throw new BizException("无权操作该数据");
        }

        // 查找同城市的当前生效记录并停用
        LambdaQueryWrapper<HrHousingFundConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrHousingFundConfig::getCityCode, newConfig.getCityCode())
               .eq(HrHousingFundConfig::getCompanyId, companyId)
               .eq(HrHousingFundConfig::getIsActive, 1)
               .eq(HrHousingFundConfig::getIsDelete, 0);
        HrHousingFundConfig oldConfig = getOne(wrapper, false);

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
    public void delete(Long id, Long companyId) {
        HrHousingFundConfig config = getById(id);
        if (config == null) {
            throw new BizException("公积金参数配置不存在");
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

    private HrHousingFundConfig convertToEntity(HrHousingFundDTO dto, Long companyId) {
        HrHousingFundConfig config = new HrHousingFundConfig();
        config.setCompanyId(companyId);
        config.setCityCode(dto.getCityCode());
        config.setPeriodStart(dto.getPeriodStart());
        config.setPeriodEnd(dto.getPeriodEnd());
        config.setBaseMin(dto.getBaseMin());
        config.setBaseMax(dto.getBaseMax());
        config.setEmployeeRate(dto.getEmployeeRate());
        config.setCompanyRate(dto.getCompanyRate());
        config.setRemark(dto.getRemark());
        return config;
    }
}
