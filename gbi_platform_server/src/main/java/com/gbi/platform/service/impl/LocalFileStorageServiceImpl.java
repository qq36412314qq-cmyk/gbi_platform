package com.gbi.platform.service.impl;

import com.gbi.platform.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地磁盘文件存储驱动实现
 * 文件写入 projectRoot/upload/{fileKey}，通过 WebConfig 静态映射访问
 *
 * @author gbi
 */
@Slf4j
@Component
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${gbi.upload-dir:./upload}")
    private String uploadDir;

    @Override
    public String storageType() {
        return "local";
    }

    @Override
    public void upload(String fileKey, InputStream inputStream, String contentType, long fileSize) {
        try {
            Path target = Paths.get(uploadDir, fileKey);
            Path parentDir = target.getParent();
            log.info("[LocalUpload] 准备写入文件 - uploadDir={}, parentDir={}, fileKey={}", uploadDir, parentDir, fileKey);
            Files.createDirectories(parentDir);
            log.info("[LocalUpload] 目录创建完成，开始写入文件...");
            Files.copy(inputStream, target);
            log.info("[LocalUpload] 文件写入成功 - target={}, fileSize={}", target.toAbsolutePath(), fileSize);
        } catch (IOException e) {
            log.error("[LocalUpload] 文件写入失败 - uploadDir={}, fileKey={}, 错误={}", uploadDir, fileKey, e.getMessage(), e);
            throw new RuntimeException("本地文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPreviewUrl(String fileKey) {
        // 本地模式：返回静态资源相对路径，由 WebConfig 映射提供服务
        String url = "/upload/" + fileKey;
        log.info("[LocalStorage] 生成预览URL - fileKey={}, previewUrl={}", fileKey, url);
        return url;
    }

    @Override
    public void delete(String fileKey) {
        try {
            Path target = Paths.get(uploadDir, fileKey);
            Files.deleteIfExists(target);
            log.info("本地文件删除成功: fileKey={}", fileKey);
        } catch (IOException e) {
            log.warn("本地文件删除失败（文件可能已不存在）: fileKey={}", fileKey, e);
        }
    }

    @Override
    public boolean matches(String expectedType) {
        return "local".equals(expectedType);
    }
}
