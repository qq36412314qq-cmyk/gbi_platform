package com.gbi.platform.dto.wecom;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 企微同步记录分页查询入参
 *
 * @author gbi
 */
@Data
public class WecomSyncQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String syncType;
    private Integer syncDirection;
    private Integer syncStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
