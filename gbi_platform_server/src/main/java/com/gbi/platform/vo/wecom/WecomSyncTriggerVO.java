package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 手动触发同步结果 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "同步触发结果")
public class WecomSyncTriggerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "执行状态：RUNNING/SUCCESS/FAILED")
    private String status;

    @Schema(description = "执行结果描述")
    private String message;

    @Schema(description = "本次处理条数")
    private Integer processedCount;
}
