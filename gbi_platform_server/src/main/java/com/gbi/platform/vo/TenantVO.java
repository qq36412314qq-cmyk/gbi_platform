package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户档案返回（手机号/证件/账号敏感字段脱敏）
 *
 * @author gbi
 */
@Data
@Schema(description = "租户档案返回")
public class TenantVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "租户类型 1个体工商户 2企业")
    private Integer tenantType;

    @Schema(description = "租户类型文本")
    private String tenantTypeText;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话（脱敏）")
    private String contactPhone;

    @Schema(description = "身份证号码（脱敏）")
    private String idCardNo;

    @Schema(description = "统一社会信用代码")
    private String socialCreditCode;

    @Schema(description = "对公银行账号（脱敏后四位）")
    private String bankAccount;

    @Schema(description = "联系地址")
    private String address;

    @Schema(description = "状态 0停用 1正常")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}