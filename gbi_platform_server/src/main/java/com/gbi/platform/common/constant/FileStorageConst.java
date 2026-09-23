package com.gbi.platform.common.constant;

import java.util.List;
import java.util.Set;

/**
 * 文件存储常量定义：后缀白名单、文件类型映射、存储类型枚举
 *
 * @author gbi
 */
public final class FileStorageConst {

    private FileStorageConst() {
    }

    /** 文件存储类型：本地磁盘 */
    public static final String STORAGE_TYPE_LOCAL = "local";
    /** 文件存储类型：阿里云OSS */
    public static final String STORAGE_TYPE_ALIOSS = "alioss";
    /** 文件存储类型：天翼云ZOS */
    public static final String STORAGE_TYPE_TIANYOSS = "tianyoss";

    /** 允许上传的文件后缀白名单（小写） */
    public static final Set<String> FILE_WHITELIST = Set.of(
            // 图片
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg",
            // 文档
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            // 压缩
            "zip", "rar", "7z", "tar", "gz"
    );

    /** 允许上传的图片后缀白名单 */
    public static final Set<String> IMAGE_WHITELIST = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /** 图片MIME类型白名单 */
    public static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml"
    );

    /** 签名URL默认有效期（秒） */
    public static final long SIGN_URL_EXPIRE_SECONDS = 3600L;

    /** file_key 路径分隔符 */
    public static final String KEY_SEPARATOR = "/";

    /** 业务类型：人事入职 */
    public static final String BIZ_TYPE_HR_ENTRY = "hr_entry";
}
