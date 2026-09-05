package com.gbi.platform.dto;

import lombok.Data;

/**
 * 缴费单分页查询DTO
 *
 * @author gbi
 */
@Data
public class PayOrderQueryDTO {

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 缴费单编号（模糊） */
    private String payBillNo;

    /** 来源类型 fee_bill/water_elec */
    private String sourceType;

    /** 缴费状态 0待缴 1已缴 2部分缴费 */
    private Integer payStatus;

    /** 所属子公司ID（0=集团全量） */
    private Long companyId;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;
}