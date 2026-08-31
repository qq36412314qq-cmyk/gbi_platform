package com.gbi.platform.controller.base;

import com.gbi.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件上传接口（对齐前端 api/base.ts upload）
 * 一期本地存储 ./upload，WebConfig 已映射 /upload/** 静态访问；二期可替换 OSS
 *
 * @author gbi
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/base")
public class UploadController {

    @Value("${gbi.upload-dir:./upload}")
    private String uploadDir;

    @Operation(summary = "上传文件，返回可访问相对路径")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = LocalDate.now() + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadDir, LocalDate.now().toString());
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(Paths.get(fileName).getFileName()).toAbsolutePath());
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalName, e);
            return Result.error("上传失败，请稍后重试");
        }
        return Result.success("/upload/" + fileName);
    }
}