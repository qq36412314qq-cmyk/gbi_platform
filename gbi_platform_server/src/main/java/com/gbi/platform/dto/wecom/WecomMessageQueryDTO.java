package com.gbi.platform.dto.wecom;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 企微消息记录分页查询入参
 *
 * @author gbi
 */
@Data
public class WecomMessageQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String msgType;
    private Integer sendStatus;
    private String toUser;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
