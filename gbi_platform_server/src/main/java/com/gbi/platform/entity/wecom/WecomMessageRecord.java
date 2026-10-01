package com.gbi.platform.entity.wecom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企微消息记录实体，对应表 wecom_message_record（追加型记录表，无 update 字段）
 *
 * @author gbi
 */
@Data
@TableName("wecom_message_record")
public class WecomMessageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属公司ID */
    private Long companyId;

    /** 企业微信消息ID（幂等键） */
    private String msgId;

    /** 消息类型: text/markdown/news */
    private String msgType;

    /** 发送者UserID */
    private String fromUser;

    /** 接收者UserID */
    private String toUser;

    /** 应用ID */
    private Integer agentId;

    /** 消息内容 */
    private String content;

    /** 媒体文件ID */
    private String mediaId;

    /** 发送状态 0待发送 1成功 2失败 */
    private Integer sendStatus;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 逻辑删除 0正常 1删除 */
    @TableLogic
    private Integer isDelete;
}
