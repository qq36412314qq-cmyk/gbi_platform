package com.gbi.platform.dto;

import lombok.Data;

/**
 * 文件上传入参DTO
 *
 * @author gbi
 */
@Data
public class FileUploadDTO {

    /** 业务类型，默认 hr_entry */
    private String bizType = "hr_entry";

    /** 关联业务单据ID，默认0 */
    private Long bizId;
}
