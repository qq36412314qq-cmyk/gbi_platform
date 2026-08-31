package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批任务视图（待办/已办列表）
 *
 * @author gbi
 */
@Data
public class FlowTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long instanceId;

    private String instanceNo;

    private String defName;

    private String bizType;

    private String title;

    private String nodeName;

    private Integer nodeOrder;

    private Long handlerId;

    private String handlerName;

    private Integer taskStatus;

    private Integer approveResult;

    private String opinion;

    private LocalDateTime handleTime;

    private String applyUserName;

    private Integer instanceStatus;

    private LocalDateTime submitTime;

    private LocalDateTime createTime;
}