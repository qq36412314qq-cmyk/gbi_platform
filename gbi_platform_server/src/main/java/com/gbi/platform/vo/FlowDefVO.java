package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程定义视图
 *
 * @author gbi
 */
@Data
public class FlowDefVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    private String defName;

    private String defCode;

    private String bizType;

    private String nodeConfigJson;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}