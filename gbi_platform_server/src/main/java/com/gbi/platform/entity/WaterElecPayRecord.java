package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电物业缴费记录实体：water_elec_pay_record
 * 线下缴费/线上回调统一落账，requestId 唯一键保证幂等，退费反向标记
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("property_water_elec_pay_record")
public class WaterElecPayRecord extends BaseEntity {

    /** 所属子公司ID */
    private Long companyId;

    /** 关联账单ID */
    private Long billId;

    /** 摊位ID */
    private Long stallId;

    /** 商户ID */
    private Long merchantId;

    /** 摊位编号快照 */
    private String stallNumber;

    /** 摊位名称快照 */
    private String stallName;

    /** 所属市场名称快照 */
    private String stallMarketName;

    /** 租赁分类名称快照 */
    private String categoryName;

    /** 商户名称快照 */
    private String merchantName;

    /** 缴费/退费金额 */
    private BigDecimal payAmount;

    /** 支付渠道 1微信 2支付宝 3线下现金 */
    private Integer payType;

    /** 幂等请求ID（前端生成，唯一键防重复提交） */
    private String requestId;

    /** 记录类型 1缴费 2退费 */
    private Integer recordType;

    /** 退费状态 0未退 1已退（仅缴费记录使用） */
    private Integer refundStatus;

    /** 退费完成时间 */
    private LocalDateTime refundTime;

    /** 退费记录ID（record_type=2 时指向原缴费记录） */
    private Long refundRecordId;

    /** 备注说明 */
    private String remark;

    /** 关联财务流水单号 */
    private String flowNo;
}
