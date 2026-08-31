package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户档案实体：stall_tenant
 * 商户/租户入驻建档，支持个体工商户与企业，证件类字段密文存储
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stall_tenant")
public class StallTenant extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 租户名称 */
    private String tenantName;

    /** 租户类型 1个体工商户 2企业 */
    private Integer tenantType;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话（密文存储，返回脱敏） */
    private String contactPhone;

    /** 身份证号码（AES密文） */
    private String idCardNo;

    /** 统一社会信用代码 */
    private String socialCreditCode;

    /** 对公银行账号（密文） */
    private String bankAccount;

    /** 联系地址 */
    private String address;

    /** 微信小程序openid */
    private String miniOpenid;

    /** 状态 0停用 1正常 */
    private Integer status;

    /** 备注 */
    private String remark;
}
