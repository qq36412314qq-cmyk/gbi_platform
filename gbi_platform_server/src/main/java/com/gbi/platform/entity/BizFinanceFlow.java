package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 全域财务资金流水实体：biz_finance_flow
 * 全集团唯一资金数据源，禁止修改、禁止删除、禁止逻辑删除（实体不含 is_delete 自动豁免）
 * 快照字段写入时固化，符合审计合规要求
 *
 * @author gbi
 */
@Data
@TableName("finance_pay_flow")
public class BizFinanceFlow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属子公司ID */
    private Long companyId;

    /** 业务类型 rent租金/water_elec水电/deposit押金/marketing营销抵扣 */
    private String businessType;

    /** 关联业务单据ID */
    private String billId;

    /** 关联应收应付计划ID（biz_recv_pay_plan，核销时写入，对账底座） */
    private Long planId;

    /** 商户ID */
    private Long merchantId;

    /** 摊位ID */
    private Long stallId;

    /** 摊位编号快照（写入时固化，禁止修改） */
    private String stallNumber;

    /** 摊位名称快照（写入时固化，禁止修改） */
    private String stallName;

    /** 所属市场名称快照（写入时固化，禁止修改） */
    private String stallMarketName;

    /** 租赁分类名称快照（写入时固化，禁止修改） */
    private String categoryName;

    /** 商户名称快照（写入时固化，禁止修改） */
    private String merchantName;

    /** 缴费人姓名（写入时固化，禁止修改） */
    private String payerName;

    /** 缴费人手机号（写入时固化） */
    private String payerPhone;

    /** 缴费人公司名称（企业缴费时写入，个人为 null） */
    private String payerCompanyName;

    /** 缴费人类型 1个人 2企业（写入时固化） */
    private Integer payerType;

    /** 关联合同编号（写入时固化） */
    private String contractNo;

    /** 关联合同ID（语义化冗余，便于按合同ID查询） */
    private Long contractId;

    /** 应收原价金额 */
    private BigDecimal originalAmount;

    /** 优惠抵扣金额 */
    private BigDecimal discountAmount;

    /** 实际实收金额 */
    private BigDecimal realAmount;

    /** 支付渠道 1微信 2支付宝 3线下现金 */
    private Integer payType;

    /** 流水类型 1收入 2支出退费 */
    private Integer flowType;

    /** 流水状态 1正常 */
    private Integer status;

    /** 冲红/作废状态 1正常 2冲红中 3已冲红 4已作废 */
    private Integer flowStatus;

    /** 冲红反向流水ID（已冲红时写入） */
    private Long redFlushFlowId;

    /** 作废原因 */
    private String voidReason;

    /** 第三方支付交易号 */
    private String tradeNo;

    /** 财务流水单号（YO+公司编码+日期+流水序号） */
    private String flowNo;

    /** 流水备注说明 */
    private String remark;

    /** 操作人用户ID */
    private Long createBy;

    /** 流水生成时间 */
    private LocalDateTime createTime;
}
