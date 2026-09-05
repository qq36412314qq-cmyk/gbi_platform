package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠申请单表实体：biz_discount_apply
 * policy_snapshot 策略快照固化审批后计算依据，不回溯；
 * 超集团阈值 need_audit=1 自动发起 contract_discount 审批
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_discount_apply")
public class BizDiscountApply extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 申请编号 */
    private String applyNo;

    /** 优惠策略ID */
    private Long policyId;

    /** 策略快照JSON（审批后计算依据，固化不回溯） */
    private String policySnapshot;

    /** 来源类型 contract */
    private String sourceType;

    /** 来源单据ID（合同ID） */
    private String sourceId;

    /** 合同编号（冗余便于列表展示） */
    private String contractNo;

    /** 铺位ID */
    private Long stallId;

    /** 租户ID */
    private Long tenantId;

    /** 申请免租期月数 */
    private Integer waiveMonths;

    /** 申请折扣率% */
    private BigDecimal discountRate;

    /** 申请减免金额 */
    private BigDecimal deductAmount;

    /** 优惠总额（免租折算+折扣+减免） */
    private BigDecimal discountAmount;

    /** 是否需审批 超集团阈值自动置1 */
    private Integer needAudit;

    /** 关联审批实例ID */
    private Long flowInstanceId;

    /** 申请状态 0草稿 1审批中 2通过 3驳回 4作废 */
    private Integer applyStatus;

    /** 申请人用户ID */
    private Long applyUserId;

    /** 审批完成时间 */
    private LocalDateTime auditTime;

    /** 备注 */
    private String remark;
}