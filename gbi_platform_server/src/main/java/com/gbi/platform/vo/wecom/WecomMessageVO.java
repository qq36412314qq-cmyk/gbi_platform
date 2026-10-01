package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微消息记录 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "企微消息记录")
public class WecomMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "所属公司ID")
    private Long companyId;

    @Schema(description = "企业微信消息ID")
    private String msgId;

    @Schema(description = "消息类型")
    private String msgType;

    @Schema(description = "发送者UserID")
    private String fromUser;

    @Schema(description = "接收者UserID")
    private String toUser;

    @Schema(description = "应用ID")
    private Integer agentId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "媒体文件ID")
    private String mediaId;

    @Schema(description = "发送状态 0待发送 1成功 2失败")
    private Integer sendStatus;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
}
