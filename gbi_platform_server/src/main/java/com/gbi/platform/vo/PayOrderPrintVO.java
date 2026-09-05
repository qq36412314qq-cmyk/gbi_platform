package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 缴费单打印数据VO（含主表 + 明细列表）
 *
 * @author gbi
 */
@Data
@Schema(description = "缴费单打印数据")
public class PayOrderPrintVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缴费单主表信息")
    private PayOrderVO payOrder;

    @Schema(description = "缴费单明细列表")
    private List<PayOrderItemVO> items;
}