package com.gbi.platform.service.wecom;

import com.gbi.platform.dto.wecom.WecomConfigEditDTO;
import com.gbi.platform.dto.wecom.WecomConfigQueryDTO;
import com.gbi.platform.dto.wecom.WecomSyncTriggerDTO;
import com.gbi.platform.dto.wecom.WecomTenantEditDTO;
import com.gbi.platform.vo.wecom.WecomConfigVO;
import com.gbi.platform.vo.wecom.WecomStatusVO;
import com.gbi.platform.vo.wecom.WecomSyncTriggerVO;
import com.gbi.platform.vo.wecom.WecomTenantVO;
import com.gbi.platform.vo.wecom.WecomTokenVO;

import java.util.List;

/**
 * 企微配置管理服务接口
 * 负责：sys_config 业务配置 CRUD、wecom_tenant_config 管理、Token 刷新、同步触发、状态查询
 *
 * @author gbi
 */
public interface WecomConfigService {

    /** 查询企微配置列表（按 type 过滤：tenant/sys/all） */
    List<WecomConfigVO> listConfigs(WecomConfigQueryDTO dto);

    /** 获取指定公司的企微租户配置 */
    WecomTenantVO getTenant(Long companyId);

    /** 编辑 sys_config 业务配置（含审计日志） */
    void editConfig(WecomConfigEditDTO dto);

    /** 编辑企微租户 corpId 配置（含缓存刷新） */
    void editTenantConfig(WecomTenantEditDTO dto);

    /** 主动刷新企微 access_token */
    WecomTokenVO refreshToken(Long companyId);

    /** 获取同步开关及 Cron 状态 */
    WecomStatusVO getStatus();

    /** 手动触发指定类型同步（异步执行，立即返回状态） */
    WecomSyncTriggerVO triggerSync(WecomSyncTriggerDTO dto);
}
