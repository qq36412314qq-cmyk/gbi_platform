package com.gbi.platform.service;

import java.io.InputStream;

/**
 * 文件存储驱动统一接口（适配器模式）
 * 三个实现：LocalFileStorageServiceImpl / AliOssFileStorageServiceImpl / TianyOssFileStorageServiceImpl
 *
 * @author gbi
 */
public interface FileStorageService {

    /**
     * 存储类型标识：local / alioss / tianyoss
     */
    String storageType();

    /**
     * 上传文件到存储
     *
     * @param fileKey     跨存储统一相对路径（不含域名），由调用方生成
     * @param inputStream 文件输入流
     * @param contentType MIME类型
     * @param fileSize    文件大小（字节）
     */
    void upload(String fileKey, InputStream inputStream, String contentType, long fileSize);

    /**
     * 获取文件预览URL
     * <p>local → 返回静态资源路径；alioss/tianyoss → 返回临时签名URL</p>
     *
     * @param fileKey 文件相对路径
     * @return 可访问的完整URL
     */
    String getPreviewUrl(String fileKey);

    /**
     * 从存储中删除文件
     *
     * @param fileKey 文件相对路径
     */
    void delete(String fileKey);

    /**
     * 校验当前驱动是否与指定类型匹配
     *
     * @param expectedType 期望的存储类型
     * @return true=匹配
     */
    boolean matches(String expectedType);
}
