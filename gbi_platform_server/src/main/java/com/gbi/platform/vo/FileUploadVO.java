package com.gbi.platform.vo;

import lombok.Data;

/**
 * 文件上传返回VO
 *
 * @author gbi
 */
@Data
public class FileUploadVO {

    /** 文件记录ID */
    private Long fileId;

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

    /** 预览URL（本地路径或签名URL） */
    private String previewUrl;

    /** MD5值 */
    private String md5;

    /** MD5重复时，已有文件的ID（仅提示，null表示无重复） */
    private Long duplicateFileId;

    /** MD5重复时，已有文件的fileKey */
    private String duplicateFileKey;
}
