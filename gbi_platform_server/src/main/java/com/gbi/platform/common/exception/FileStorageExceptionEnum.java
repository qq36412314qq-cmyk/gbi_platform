package com.gbi.platform.common.exception;

import lombok.Getter;

/**
 * 文件存储异常枚举
 *
 * @author gbi
 */
@Getter
public enum FileStorageExceptionEnum {

    STORAGE_TYPE_NOT_CONFIGURED(10001, "文件存储类型未配置，请检查 sys_config.file.storage.type"),
    STORAGE_DRIVER_NOT_FOUND(10002, "未找到对应的文件存储驱动"),
    FILE_EMPTY(10003, "上传文件不能为空"),
    FILE_TOO_LARGE(10004, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(10005, "不支持的文件类型，仅允许上传图片或文档"),
    FILE_KEY_GENERATE_FAILED(10006, "生成文件路径失败"),
    STORAGE_UPLOAD_FAILED(10007, "文件上传失败"),
    FILE_NOT_FOUND(10008, "文件记录不存在"),
    FILE_ALREADY_DELETED(10009, "文件已被删除"),
    COMPANY_ID_MISMATCH(10010, "无权访问该文件（租户隔离）"),
    MD5_DUPLICATE(10011, "检测到相同文件已存在（MD5重复）"),
    CONFIG_KEY_NOT_FOUND(10012, "配置项不存在：%s"),
    ENCRYPTION_ERROR(10013, "AES加解密失败"),
    ;

    private final int code;
    private final String message;

    FileStorageExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String withArgs(Object... args) {
        String msg = this.message;
        for (Object arg : args) {
            msg = msg.replaceFirst("%s", String.valueOf(arg));
        }
        return msg;
    }
}
