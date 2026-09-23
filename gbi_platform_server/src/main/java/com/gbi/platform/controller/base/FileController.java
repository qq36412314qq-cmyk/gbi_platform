package com.gbi.platform.controller.base;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.service.FileService;
import com.gbi.platform.vo.FileUploadVO;
import com.gbi.platform.vo.FileVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传存储控制器：统一对外接口
 * 接口路径前缀：/base/file
 *
 * @author gbi
 */
@Slf4j
@Tag(name = "文件上传存储")
@RestController
@RequestMapping("/base/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FILE_UPLOAD, '')")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false, defaultValue = "hr_entry") String bizType,
            @RequestParam(value = "bizId", required = false, defaultValue = "0") Long bizId) {
        log.info("[FileController] 收到上传请求 - fileName={}, fileSize={}, bizType={}, bizId={}",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0,
                bizType, bizId);
        try {
            FileUploadVO vo = fileService.upload(file, bizType, bizId);
            log.info("[FileController] 上传成功 - fileId={}", vo.getFileId());
            return Result.success(vo);
        } catch (Exception e) {
            log.error("[FileController] 上传异常 - 错误={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "预览文件（302重定向）")
    @GetMapping("/preview/{fileId}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FILE_PREVIEW, '')")
    public void preview(@PathVariable Long fileId, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        String url = fileService.getPreviewUrl(fileId);
        response.sendRedirect(url);
    }

    @Operation(summary = "删除文件（敏感操作，需权限校验）")
    @DeleteMapping("/delete/{fileId}")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FILE_DELETE, '')")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return Result.success();
    }

    @Operation(summary = "查询文件列表")
    @GetMapping("/list")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FILE_LIST, '')")
    public Result<PageVO<FileVO>> list(
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        // company_id 由 UserContext 自动注入，禁止前端传入
        PageVO<FileVO> page = fileService.page(null, bizType, pageNum, pageSize);
        return Result.success(page);
    }
}
