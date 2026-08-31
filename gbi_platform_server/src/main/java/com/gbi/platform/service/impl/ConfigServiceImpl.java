package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.ConfigDTO;
import com.gbi.platform.entity.SysConfig;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.mapper.SysConfigMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.ConfigVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 系统参数服务实现：sys_config（公司隔离 + 审计）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<ConfigVO> page(Integer pageNum, Integer pageSize, String configName, String configKey) {
        Page<SysConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                .like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey)
                .orderByDesc(SysConfig::getId);
        Page<SysConfig> result = configMapper.selectPage(page, wrapper);
        List<ConfigVO> voList = result.getRecords().stream().map(c -> {
            ConfigVO vo = new ConfigVO();
            vo.setId(c.getId());
            vo.setCompanyId(c.getCompanyId());
            vo.setConfigKey(c.getConfigKey());
            vo.setConfigValue(c.getConfigValue());
            vo.setConfigName(c.getConfigName());
            vo.setRemark(c.getRemark());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ConfigDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.isSuperAdmin() ? dto.getCompanyId() : loginUser.getCompanyId();
        checkKeyUnique(companyId, dto.getConfigKey(), null);
        SysConfig config = new SysConfig();
        config.setCompanyId(companyId);
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigName(dto.getConfigName());
        config.setRemark(dto.getRemark());
        configMapper.insert(config);
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_ADD,
                String.valueOf(config.getId()), null, config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ConfigDTO dto) {
        SysConfig config = configMapper.selectById(dto.getId());
        if (config == null) {
            throw new BizException("参数不存在");
        }
        checkKeyUnique(config.getCompanyId(), dto.getConfigKey(), dto.getId());
        SysConfig before = new SysConfig();
        cn.hutool.core.bean.BeanUtil.copyProperties(config, before);
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigName(dto.getConfigName());
        config.setRemark(dto.getRemark());
        configMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(config.getId()), before, config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException("参数不存在");
        }
        configMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), config, null);
    }

    @Override
    public String getValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCompanyId, CommonConst.COMPANY_ROOT)
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1");
        SysConfig config = configMapper.selectOne(wrapper);
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public Map<String, String> getValuesByKeys(List<String> configKeys) {
        if (configKeys == null || configKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        // 集团全局参数（company_id=0）子公司只读共享：显式按集团参数查询（sys_config 已加入 GLOBAL_TABLES 豁免租户过滤）
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCompanyId, CommonConst.COMPANY_ROOT)
                .in(SysConfig::getConfigKey, configKeys)
                .select(SysConfig::getConfigKey, SysConfig::getConfigValue);
        Map<String, String> map = new HashMap<>();
        for (SysConfig c : configMapper.selectList(wrapper)) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    /**
     * 参数 key 唯一校验（同公司维度）
     */
    private void checkKeyUnique(Long companyId, String configKey, Long excludeId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCompanyId, companyId)
                .eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(SysConfig::getId, excludeId);
        }
        Long exist = configMapper.selectCount(wrapper);
        if (exist != null && exist > 0) {
            throw new BizException("参数key已存在：" + configKey);
        }
    }
}