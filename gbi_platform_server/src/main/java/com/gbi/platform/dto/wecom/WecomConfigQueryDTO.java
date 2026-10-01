package com.gbi.platform.dto.wecom;

import lombok.Data;

/**
 * 企微配置查询入参
 *
 * @author gbi
 */
@Data
public class WecomConfigQueryDTO {
    /** 公司ID，null或0则查集团级(company_id=0) */
    private Long companyId;
    /** 配置类型：tenant=租户配置，sys=业务配置，all=全部 */
    private String type;
}
