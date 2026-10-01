package com.gbi.platform.service.impl.wecom;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.config.WecomProperties;
import com.gbi.platform.dto.wecom.WecomConfigEditDTO;
import com.gbi.platform.dto.wecom.WecomConfigQueryDTO;
import com.gbi.platform.dto.wecom.WecomSyncTriggerDTO;
import com.gbi.platform.dto.wecom.WecomTenantEditDTO;
import com.gbi.platform.entity.SysConfig;
import com.gbi.platform.entity.wecom.WecomTenantConfig;
import com.gbi.platform.mapper.SysConfigMapper;
import com.gbi.platform.mapper.wecom.WecomTenantConfigMapper;
import com.gbi.platform.service.wecom.WecomConfigService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.wecom.WecomConfigVO;
import com.gbi.platform.vo.wecom.WecomStatusVO;
import com.gbi.platform.vo.wecom.WecomSyncTriggerVO;
import com.gbi.platform.vo.wecom.WecomTenantVO;
import com.gbi.platform.vo.wecom.WecomTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 企微配置管理服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WecomConfigServiceImpl implements WecomConfigService {

    private final SysConfigMapper configMapper;
    private final WecomTenantConfigMapper tenantConfigMapper;
    private final AuditLogUtil auditLogUtil;
    private final WecomProperties wecomProperties;

    /** corpId 缓存：companyId -> corpId（避免高频 DB 查询） */
    private final ConcurrentHashMap<Long, String> corpIdCache = new ConcurrentHashMap<>();

    // ==================== 配置查询 ====================

    @Override
    public List<WecomConfigVO> listConfigs(WecomConfigQueryDTO dto) {
        Long companyId = (dto.getCompanyId() != null && dto.getCompanyId() > 0)
                ? dto.getCompanyId() : CommonConst.COMPANY_ROOT;
        String type = StringUtils.hasText(dto.getType()) ? dto.getType() : "all";

        List<WecomConfigVO> result = new ArrayList<>();

        if ("all".equals(type) || "sys".equals(type)) {
            LambdaQueryWrapper<SysConfig> sysWrapper = new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getCompanyId, companyId)
                    .like(SysConfig::getConfigKey, "wecom.")
                    .orderByAsc(SysConfig::getConfigKey);
            List<SysConfig> sysList = configMapper.selectList(sysWrapper);
            result.addAll(sysList.stream().map(this::toConfigVO).collect(Collectors.toList()));
        }

        if ("all".equals(type) || "tenant".equals(type)) {
            LambdaQueryWrapper<WecomTenantConfig> tenantWrapper = new LambdaQueryWrapper<WecomTenantConfig>()
                    .eq(WecomTenantConfig::getCompanyId, companyId)
                    .eq(WecomTenantConfig::getStatus, CommonConst.STATUS_ENABLED)
                    .last("LIMIT 1");
            WecomTenantConfig tenant = tenantConfigMapper.selectOne(tenantWrapper);
            if (tenant != null) {
                WecomConfigVO vo = new WecomConfigVO();
                vo.setId(tenant.getId());
                vo.setCompanyId(tenant.getCompanyId());
                vo.setConfigKey("wecom.tenant.corpId");
                vo.setConfigName("企业微信企业ID");
                vo.setConfigValue(maskCorpId(tenant.getCorpId()));
                vo.setRemark(tenant.getRemark());
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public WecomTenantVO getTenant(Long companyId) {
        Long targetId = (companyId != null && companyId > 0) ? companyId : CommonConst.COMPANY_ROOT;
        WecomTenantConfig tenant = tenantConfigMapper.selectOne(
                new LambdaQueryWrapper<WecomTenantConfig>()
                        .eq(WecomTenantConfig::getCompanyId, targetId)
                        .last("LIMIT 1")
        );
        if (tenant == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(),
                    "未找到企微租户配置，companyId=" + targetId + "，请先执行初始化SQL");
        }
        WecomTenantVO vo = new WecomTenantVO();
        vo.setId(tenant.getId());
        vo.setCompanyId(tenant.getCompanyId());
        vo.setCorpId(maskCorpId(tenant.getCorpId()));
        vo.setStatus(tenant.getStatus());
        vo.setRemark(tenant.getRemark());
        return vo;
    }

    // ==================== 配置编辑 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editConfig(WecomConfigEditDTO dto) {
        SysConfig config = configMapper.selectById(dto.getId());
        if (config == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "配置不存在，id=" + dto.getId());
        }
        SysConfig before = new SysConfig();
        BeanUtil.copyProperties(config, before);
        config.setConfigValue(dto.getConfigValue());
        configMapper.updateById(config);
        auditLogUtil.record(CommonConst.MODULE_SYS, "企微配置变更",
                String.valueOf(config.getId()), before, config);
        log.info("企微配置已更新，id={}, key={}", config.getId(), config.getConfigKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editTenantConfig(WecomTenantEditDTO dto) {
        Long targetId = (dto.getCompanyId() != null && dto.getCompanyId() > 0)
                ? dto.getCompanyId() : CommonConst.COMPANY_ROOT;

        WecomTenantConfig tenant = tenantConfigMapper.selectOne(
                new LambdaQueryWrapper<WecomTenantConfig>()
                        .eq(WecomTenantConfig::getCompanyId, targetId)
                        .last("LIMIT 1")
        );

        if (tenant == null) {
            tenant = new WecomTenantConfig();
            tenant.setCompanyId(targetId);
            tenant.setCorpId(dto.getCorpId());
            tenant.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConst.STATUS_ENABLED);
            tenant.setRemark(dto.getRemark());
            tenantConfigMapper.insert(tenant);
            auditLogUtil.record(CommonConst.MODULE_SYS, "企微租户配置新增",
                    String.valueOf(tenant.getId()), null, tenant);
        } else {
            WecomTenantConfig before = new WecomTenantConfig();
            BeanUtil.copyProperties(tenant, before);
            tenant.setCorpId(dto.getCorpId());
            if (dto.getStatus() != null) {
                tenant.setStatus(dto.getStatus());
            }
            if (StringUtils.hasText(dto.getRemark())) {
                tenant.setRemark(dto.getRemark());
            }
            tenantConfigMapper.updateById(tenant);
            auditLogUtil.record(CommonConst.MODULE_SYS, "企微租户配置变更",
                    String.valueOf(tenant.getId()), before, tenant);
        }

        refreshCorpIdCache(targetId, dto.getCorpId());
        log.info("企微租户配置已更新，companyId={}, corpId尾号={}", targetId,
                dto.getCorpId().substring(Math.max(0, dto.getCorpId().length() - 4)));
    }

    // ==================== Token 管理 ====================

    @Override
    public WecomTokenVO refreshToken(Long companyId) {
        Long targetId = (companyId != null && companyId > 0) ? companyId : CommonConst.COMPANY_ROOT;
        String corpId = getCorpId(targetId);
        if (!StringUtils.hasText(corpId)) {
            throw new BizException(ResultCode.ERROR.getCode(),
                    "corpId 未配置，请先在「企微配置管理-基础配置」中填写 corpId");
        }

        // TODO: Phase 2 实现真实企微API调用，当前返回占位值
        String mockToken = "mock-token-" + System.currentTimeMillis();
        long now = System.currentTimeMillis() / 1000;
        WecomTokenVO tokenVO = new WecomTokenVO();
        tokenVO.setAccessToken(mockToken);
        tokenVO.setExpiresIn(7200L);
        // 简化：直接设置过期时间
        tokenVO.setExpireTime(java.time.LocalDateTime.now().plusHours(2));
        log.info("企微 access_token 刷新（占位），companyId={}, token前缀=***", targetId,
                mockToken.substring(0, Math.min(8, mockToken.length())));
        return tokenVO;
    }

    // ==================== 状态 & 同步触发 ====================

    @Override
    public WecomStatusVO getStatus() {
        WecomStatusVO vo = new WecomStatusVO();
        vo.setAttendanceEnabled(Boolean.parseBoolean(getConfigValue("wecom.sync.attendance.enabled", "0")));
        vo.setContactEnabled(Boolean.parseBoolean(getConfigValue("wecom.sync.contact.enabled", "0")));
        vo.setAttendanceCron(getConfigValue("wecom.sync.attendance.cron", "0 0 1 * * ?"));
        vo.setContactCron(getConfigValue("wecom.sync.contact.cron", "0 0 2 * * ?"));
        return vo;
    }

    @Override
    public WecomSyncTriggerVO triggerSync(WecomSyncTriggerDTO dto) {
        log.info("手动触发企微同步，syncType={}, companyId={}", dto.getSyncType(), dto.getCompanyId());
        // TODO: Phase 4/5 实现具体同步逻辑（通讯录/打卡）
        WecomSyncTriggerVO vo = new WecomSyncTriggerVO();
        vo.setStatus("SUCCESS");
        vo.setMessage("同步任务已触发（占位，待实现具体同步逻辑）");
        vo.setProcessedCount(0);
        return vo;
    }

    // ==================== 私有方法 ====================

    private WecomConfigVO toConfigVO(SysConfig config) {
        WecomConfigVO vo = new WecomConfigVO();
        vo.setId(config.getId());
        vo.setCompanyId(config.getCompanyId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigName(config.getConfigName());
        vo.setConfigValue(maskSensitive(config.getConfigKey(), config.getConfigValue()));
        vo.setRemark(config.getRemark());
        return vo;
    }

    /** 脱敏 AES Key 等敏感字段：展示前4位+**** */
    private String maskSensitive(String key, String value) {
        if (value == null) return "";
        boolean isSensitive = key != null
                && (key.contains("secret") || key.contains("aes") || key.contains("key"));
        if (!isSensitive) return value;
        if (value.length() <= 4) return "****";
        return value.substring(0, 4) + "****";
    }

    /** 脱敏 corpId：展示前4位+后4位，中间掩码 */
    private String maskCorpId(String corpId) {
        if (corpId == null || corpId.length() <= 8) return corpId == null ? "" : "****";
        return corpId.substring(0, 4) + "****" + corpId.substring(corpId.length() - 4);
    }

    /** 从 sys_config 读取集团级（company_id=0）配置值 */
    private String getConfigValue(String key, String defaultValue) {
        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCompanyId, CommonConst.COMPANY_ROOT)
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        return config != null && StringUtils.hasText(config.getConfigValue())
                ? config.getConfigValue() : defaultValue;
    }

    /** 获取指定公司的 corpId（缓存优先，miss 则查DB） */
    private String getCorpId(Long companyId) {
        Long key = companyId != null && companyId > 0 ? companyId : CommonConst.COMPANY_ROOT;
        String cached = corpIdCache.get(key);
        if (cached != null) return cached;

        WecomTenantConfig tenant = tenantConfigMapper.selectOne(
                new LambdaQueryWrapper<WecomTenantConfig>()
                        .eq(WecomTenantConfig::getCompanyId, key)
                        .eq(WecomTenantConfig::getStatus, CommonConst.STATUS_ENABLED)
                        .last("LIMIT 1")
        );
        if (tenant != null && StringUtils.hasText(tenant.getCorpId())) {
            corpIdCache.put(key, tenant.getCorpId());
            return tenant.getCorpId();
        }
        log.warn("企微 corpId 未配置，companyId={}，API 调用将失败", key);
        return "";
    }

    /** 刷新 corpId 缓存 */
    private void refreshCorpIdCache(Long companyId, String corpId) {
        Long key = companyId != null && companyId > 0 ? companyId : CommonConst.COMPANY_ROOT;
        corpIdCache.put(key, corpId);
    }
}
