package com.gbi.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件列表VO
 *
 * @author gbi
 */
@Data
public class FileVO {

    /** 文件记录ID */
    private Long id;

    /** 跨存储统一相对路径 */
    private String fileKey;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME类型 */
    private String fileType;

    /** 文件后缀 */
    private String fileExt;

    /** 存储类型快照 */
    private String storageType;

    /** MD5值 */
    private String md5;

    /** 业务类型 */
    private String bizType;

    /** 关联业务单据ID */
    private Long bizId;

    /** 预览URL */
    private String previewUrl;

    /** 创建时间 */
    private LocalDateTime createTime;
}
