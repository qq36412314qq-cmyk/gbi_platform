package com.gbi.platform.service.impl;

import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.exception.FileStorageExceptionEnum;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 文件存储驱动工厂：读取 sys_config.file.storage.type 动态获取激活驱动
 * 支持热刷新：通过 refresh() 方法重新加载配置，无需重启服务
 *
 * @author gbi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageFactory {

    private final List<FileStorageService> storageServices;
    private final ConfigService configService;

    /** 当前激活的存储驱动，原子引用支持线程安全热刷新 */
    private final AtomicReference<FileStorageService> activeService = new AtomicReference<>();

    /**
     * 初始化：启动时根据配置加载默认驱动
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 获取当前激活的存储驱动
     * 若配置未设置则降级到 local 驱动
     */
    public FileStorageService getFileStorage() {
        FileStorageService service = activeService.get();
        if (service == null) {
            log.warn("文件存储驱动未初始化，降级使用 local 驱动");
            return storageServices.stream()
                    .filter(s -> s.matches("local"))
                    .findFirst()
                    .orElseThrow(() -> new BizException(FileStorageExceptionEnum.STORAGE_DRIVER_NOT_FOUND.getMessage()));
        }
        return service;
    }

    /**
     * 热刷新：重新读取 sys_config 并切换驱动
     * 由后台修改配置后手动调用，或定时任务轮询调用
     */
    public void refresh() {
        try {
            String rawType = configService.getValueByKey("file.storage.type");
            final String storageType;
            if (rawType == null || rawType.isBlank()) {
                storageType = "local";
                log.warn("file.storage.type 未配置，默认使用 local 驱动");
            } else {
                storageType = rawType;
            }
            FileStorageService service = storageServices.stream()
                    .filter(s -> s.matches(storageType))
                    .findFirst()
                    .orElseThrow(() -> new BizException(
                            FileStorageExceptionEnum.STORAGE_DRIVER_NOT_FOUND.getMessage()));
            activeService.set(service);
            log.info("文件存储驱动已切换到: {}", storageType);
        } catch (Exception e) {
            log.error("文件存储驱动切换失败，保持当前驱动", e);
        }
    }
}
