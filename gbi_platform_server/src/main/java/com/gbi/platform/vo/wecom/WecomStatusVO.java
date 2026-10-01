package com.gbi.platform.vo.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企微同步开关状态 VO
 *
 * @author gbi
 */
@Data
@Schema(description = "企微同步状态")
public class WecomStatusVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "打卡数据同步开关")
    private Boolean attendanceEnabled;

    @Schema(description = "通讯录同步开关")
    private Boolean contactEnabled;

    @Schema(description = "打卡同步Cron表达式")
    private String attendanceCron;

    @Schema(description = "通讯录同步Cron表达式")
    private String contactCron;
}
