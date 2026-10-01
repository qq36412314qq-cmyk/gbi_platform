package com.gbi.platform.entity.wecom;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企微租户配置实体，对应表 wecom_tenant_config
 * 支持多租户多企微实例，company_id=0 为集团默认
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wecom_tenant_config")
public class WecomTenantConfig extends BaseEntity {

    /** 所属公司ID，0=集团全局默认 */
    private Long companyId;

    /** 企业微信企业ID（corpId） */
    private String corpId;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
