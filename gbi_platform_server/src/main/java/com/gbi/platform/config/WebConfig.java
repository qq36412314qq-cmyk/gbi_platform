package com.gbi.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置：本地文件上传访问映射（一期本地存储，后续切换 OSS 时移除）
 *
 * @author gbi
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${gbi.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
