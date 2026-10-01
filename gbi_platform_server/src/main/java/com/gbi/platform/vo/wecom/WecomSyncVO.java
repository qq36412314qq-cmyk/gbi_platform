package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微同步记录 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "企微同步记录")
public class WecomSyncVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "所属公司ID")
    private Long companyId;

    @Schema(description = "同步类型")
    private String syncType;

    @Schema(description = "同步方向 1系统→企微 2企微→系统")
    private Integer syncDirection;

    @Schema(description = "源数据ID")
    private String sourceId;

    @Schema(description = "目标数据ID")
    private String targetId;

    @Schema(description = "同步状态 0待同步 1成功 2失败")
    private Integer syncStatus;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "同步时间")
    private LocalDateTime syncTime;
}
