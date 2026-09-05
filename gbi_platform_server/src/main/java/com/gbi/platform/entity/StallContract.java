package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 租赁合同实体：stall_contract
 * 租户与摊位租赁合同，生效后摊位置为已租，退租审计留痕
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_stall_contract")
public class StallContract extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 合同编号 */
    private String contractNo;

    /** 商户ID（兼容旧字段，新租赁流程优先使用租户ID） */
    private Long merchantId;

    /** 租户ID（关联 stall_tenant） */
    private Long tenantId;

    /** 摊位ID */
    private Long stallId;

    /** 月租金金额 */
    private BigDecimal rentAmount;

    /** 押金金额 */
    private BigDecimal depositAmount;

    /** 租赁开始日期 */
    private LocalDate startTime;

    /** 租赁到期日期 */
    private LocalDate endTime;

    /** 合同状态 0签约中（创建后待缴费） 1生效中（缴费完成后） 2已退租 3已到期 */
    private Integer contractStatus;

    /** 关联审批实例ID（作废终止/大额优惠审批，flow_instance） */
    private Long flowInstanceId;

    /** 合同附件OSS地址 */
    private String attachmentUrl;

    /** 合同备注 */
    private String remark;
}
