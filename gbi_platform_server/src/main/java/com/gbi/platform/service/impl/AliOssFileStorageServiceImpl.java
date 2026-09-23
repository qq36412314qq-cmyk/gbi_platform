package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.FileStorageConst;
import com.gbi.platform.service.FileStorageService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 阿里云OSS文件存储驱动实现
 * 依赖：aliyun-sdk-oss（需在 pom.xml 中添加依赖）
 * 配置项：sys_config 中 file.oss-alibaba.*
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliOssFileStorageServiceImpl implements FileStorageService {

    private final FileStorageUtil fileStorageUtil;
    private final AuditLogUtil auditLogUtil;

    @Override
    public String storageType() {
        return "alioss";
    }

    @Override
    public void upload(String fileKey, InputStream inputStream, String contentType, long fileSize) {
        // TODO: 集成阿里云OSS SDK v3
        // 需添加依赖：com.aliyun.oss:aliyun-sdk-oss:3.17.4
        log.warn("阿里云OSS驱动暂未实现，请添加 aliyun-sdk-oss 依赖并完善此方法");
        throw new UnsupportedOperationException("阿里云OSS存储驱动尚未集成，请先完成SDK依赖和配置");
    }

    @Override
    public String getPreviewUrl(String fileKey) {
        // TODO: 生成阿里云OSS私有桶签名URL
        log.warn("阿里云OSS预览URL生成暂未实现");
        throw new UnsupportedOperationException("阿里云OSS预览URL生成尚未集成");
    }

    @Override
    public void delete(String fileKey) {
        // TODO: 调用阿里云OSS SDK删除对象
        log.warn("阿里云OSS删除功能暂未实现");
        throw new UnsupportedOperationException("阿里云OSS删除功能尚未集成");
    }

    @Override
    public boolean matches(String expectedType) {
        return FileStorageConst.STORAGE_TYPE_ALIOSS.equals(expectedType);
    }
}
