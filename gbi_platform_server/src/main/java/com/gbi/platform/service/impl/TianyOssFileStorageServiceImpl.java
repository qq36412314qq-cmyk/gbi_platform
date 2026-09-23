package com.gbi.platform.service.impl;

import com.gbi.platform.common.constant.FileStorageConst;
import com.gbi.platform.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 天翼云ZOS文件存储驱动实现
 * 基于 AWS S3 SDK v2（天翼云兼容S3协议）
 * 依赖：software.amazon.awssdk:s3 + regions（需在 pom.xml 中添加依赖）
 * 配置项：sys_config 中 file.oss-tianyi.*
 *
 * @author gbi
 */
@Slf4j
@Component
public class TianyOssFileStorageServiceImpl implements FileStorageService {

    @Override
    public String storageType() {
        return "tianyoss";
    }

    @Override
    public void upload(String fileKey, InputStream inputStream, String contentType, long fileSize) {
        // TODO: 集成 AWS S3 SDK v2
        // 需添加依赖：software.amazon.awssdk:s3 + software.amazon.awssdk:regions
        log.warn("天翼云ZOS驱动暂未实现，请添加 AWS S3 SDK v2 依赖并完善此方法");
        throw new UnsupportedOperationException("天翼云ZOS存储驱动尚未集成，请先完成SDK依赖和配置");
    }

    @Override
    public String getPreviewUrl(String fileKey) {
        // TODO: 生成S3预签名URL
        log.warn("天翼云ZOS预览URL生成暂未实现");
        throw new UnsupportedOperationException("天翼云ZOS预览URL生成尚未集成");
    }

    @Override
    public void delete(String fileKey) {
        // TODO: 调用AWS S3 SDK删除对象
        log.warn("天翼云ZOS删除功能暂未实现");
        throw new UnsupportedOperationException("天翼云ZOS删除功能尚未集成");
    }

    @Override
    public boolean matches(String expectedType) {
        return FileStorageConst.STORAGE_TYPE_TIANYOSS.equals(expectedType);
    }
}
