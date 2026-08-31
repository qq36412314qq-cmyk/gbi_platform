package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程流转记录视图（全流程留痕）
 *
 * @author gbi
 */
@Data
public class FlowRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long instanceId;

    private String nodeName;

    private String action;

    private String actionText;

    private Long handlerId;

    private String handlerName;

    private String comment;

    private LocalDateTime createTime;
}