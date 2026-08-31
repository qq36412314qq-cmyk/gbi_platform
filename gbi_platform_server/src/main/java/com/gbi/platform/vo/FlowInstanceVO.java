package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程实例视图（我的申请/实例列表）
 *
 * @author gbi
 */
@Data
public class FlowInstanceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    private String instanceNo;

    private Long defId;

    private String defName;

    private String bizType;

    private String sourceType;

    private String sourceId;

    private String title;

    private Long applyUserId;

    private String applyUserName;

    private Integer instanceStatus;

    private String instanceStatusText;

    private String currentNodeName;

    private LocalDateTime submitTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;
}