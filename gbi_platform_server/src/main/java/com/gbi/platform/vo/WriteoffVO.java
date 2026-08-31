package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核销分摊明细视图
 *
 * @author gbi
 */
@Data
public class WriteoffVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long financeFlowId;

    private Long planId;

    private String billType;

    private Long billId;

    private BigDecimal writeoffAmount;

    private Integer writeoffType;

    private String writeoffTypeText;

    private String remark;

    private String createByName;

    private LocalDateTime createTime;
}