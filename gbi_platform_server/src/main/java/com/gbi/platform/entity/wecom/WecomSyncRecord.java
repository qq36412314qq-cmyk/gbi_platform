package com.gbi.platform.entity.wecom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企微同步记录实体，对应表 wecom_sync_record（追加型记录表，无 update 字段）
 *
 * @author gbi
 */
@Data
@TableName("wecom_sync_record")
public class WecomSyncRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属公司ID */
    private Long companyId;

    /** 同步类型: contact/attendance/leave/overtime */
    private String syncType;

    /** 同步方向 1系统→企微 2企微→系统 */
    private Integer syncDirection;

    /** 源数据ID */
    private String sourceId;

    /** 目标数据ID */
    private String targetId;

    /** 同步状态 0待同步 1成功 2失败 */
    private Integer syncStatus;

    /** 重试次数 */
    private Integer retryCount;

    /** 错误信息 */
    private String errorMsg;

    /** 同步时间 */
    private LocalDateTime syncTime;

    /** 逻辑删除 0正常 1删除 */
    @TableLogic
    private Integer isDelete;
}
