package com.gbi.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 集团业务一体化管控平台 - 后端唯一程序启动入口
 *
 * @author gbi
 */
@SpringBootApplication
@MapperScan({"com.gbi.platform.mapper", "com.gbi.platform.hr.mapper"})
public class GbiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GbiPlatformApplication.class, args);
    }
}
