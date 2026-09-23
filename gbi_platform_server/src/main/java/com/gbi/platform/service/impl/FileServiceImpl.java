package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.constant.FileStorageConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.exception.FileStorageExceptionEnum;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.SysFile;
import com.gbi.platform.mapper.SysFileMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.FileService;
import com.gbi.platform.util.FileStorageUtil;
import com.gbi.platform.vo.FileUploadVO;
import com.gbi.platform.vo.FileVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件业务服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final SysFileMapper fileMapper;
    private final FileStorageFactory fileStorageFactory;
    private final FileStorageUtil fileStorageUtil;
    private final ConfigService configService;
    private final com.gbi.platform.util.AuditLogUtil auditLogUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadVO upload(MultipartFile file, String bizType, Long bizId) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();
        log.info("[FileUpload] 开始上传 - companyId={}, userId={}, fileName={}, fileSize={}, bizType={}",
                companyId, loginUser.getUserId(), file.getOriginalFilename(), file.getSize(), bizType);

        // ① 校验文件非空
        if (file == null || file.isEmpty()) {
            log.warn("[FileUpload] 文件为空");
            throw new BizException(FileStorageExceptionEnum.FILE_EMPTY.getMessage());
        }

        // ② 校验文件大小
        long maxSize = parseMaxSize();
        long fileSize = file.getSize();
        log.info("[FileUpload] 文件大小校验 - fileSize={}MB, maxSize={}MB", fileSize / 1024 / 1024, maxSize / 1024 / 1024);
        if (fileSize > maxSize) {
            throw new BizException(FileStorageExceptionEnum.FILE_TOO_LARGE.withArgs(maxSize / 1024 / 1024));
        }

        // ③ 校验后缀白名单
        String fileName = file.getOriginalFilename();
        String ext = fileStorageUtil.extractExt(fileName);
        log.info("[FileUpload] 后缀校验 - fileName={}, ext={}, allowed={}", fileName, ext, fileStorageUtil.isAllowedExt(ext));
        if (!fileStorageUtil.isAllowedExt(ext)) {
            throw new BizException(FileStorageExceptionEnum.FILE_TYPE_NOT_ALLOWED.getMessage());
        }

        // ④ 校验MIME类型（二次校验）
        boolean mimeOk = fileStorageUtil.isAllowedMime(file);
        log.info("[FileUpload] MIME校验 - contentType={}, allowed={}", file.getContentType(), mimeOk);
        if (!mimeOk) {
            throw new BizException(FileStorageExceptionEnum.FILE_TYPE_NOT_ALLOWED.getMessage());
        }

        // ⑤ 计算MD5
        String md5 = fileStorageUtil.md5(file);
        log.info("[FileUpload] MD5计算完成 - md5={}", md5);

        // ⑥ MD5查重：仅提示，不强制复用
        LambdaQueryWrapper<SysFile> md5Wrapper = new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getMd5, md5)
                .eq(SysFile::getCompanyId, companyId)
                .eq(SysFile::getIsDelete, 0)
                .select(SysFile::getId, SysFile::getFileKey)
                .last("LIMIT 1");
        SysFile existingFile = fileMapper.selectOne(md5Wrapper);
        if (existingFile != null) {
            log.info("MD5查重提示：文件已存在，fileId={}，允许继续上传", existingFile.getId());
        }

        // ⑦ 生成 file_key
        String safeBizType = StringUtils.hasText(bizType) ? bizType : FileStorageConst.BIZ_TYPE_HR_ENTRY;
        Long safeBizId = bizId != null ? bizId : 0L;
        String fileKey = fileStorageUtil.generateFileKey(companyId, safeBizType, ext);
        log.info("[FileUpload] file_key生成完成 - fileKey={}", fileKey);

        // ⑧ 获取当前驱动并上传
        String storageType = fileStorageFactory.getFileStorage().storageType();
        String cleanFileName = fileStorageUtil.sanitizeFileName(fileName);
        log.info("[FileUpload] 开始写入存储 - storageType={}, fileKey={}, cleanFileName={}", storageType, fileKey, cleanFileName);
        try {
            fileStorageFactory.getFileStorage().upload(fileKey, file.getInputStream(), file.getContentType(), fileSize);
            log.info("[FileUpload] 存储写入成功");
        } catch (IOException e) {
            log.error("[FileUpload] 文件上传到存储失败: fileKey={}, 错误={}", fileKey, e.getMessage(), e);
            throw new BizException(FileStorageExceptionEnum.STORAGE_UPLOAD_FAILED.getMessage());
        }

        // ⑨ 入库（事务内）
        SysFile sysFile = new SysFile();
        sysFile.setCompanyId(companyId);
        sysFile.setBizType(safeBizType);
        sysFile.setBizId(safeBizId);
        sysFile.setFileKey(fileKey);
        sysFile.setMd5(md5);
        sysFile.setFileName(cleanFileName);
        sysFile.setFileSize(fileSize);
        sysFile.setFileType(file.getContentType() != null ? file.getContentType() : "");
        sysFile.setFileExt(ext);
        sysFile.setStorageType(storageType);
        sysFile.setCreateBy(loginUser.getUserId());
        log.info("[FileUpload] 开始写入数据库 - sysFile.id={}", sysFile.getId());
        fileMapper.insert(sysFile);
        log.info("[FileUpload] 数据库写入成功 - fileId={}", sysFile.getId());

        // ⑩ 记录审计日志
        auditLogUtil.record(CommonConst.MODULE_FILE, CommonConst.OPER_TYPE_FILE_UPLOAD,
                String.valueOf(sysFile.getId()), null, sysFile);

        // ⑪ 返回结果
        String previewUrl = fileStorageFactory.getFileStorage().getPreviewUrl(fileKey);
        log.info("[FileUpload] 上传完成 - fileId={}, previewUrl={}", sysFile.getId(), previewUrl);
        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(sysFile.getId());
        vo.setFileKey(fileKey);
        vo.setFileName(cleanFileName);
        vo.setFileSize(fileSize);
        vo.setFileType(file.getContentType() != null ? file.getContentType() : "");
        vo.setFileExt(ext);
        vo.setStorageType(storageType);
        vo.setPreviewUrl(previewUrl);
        vo.setMd5(md5);
        if (existingFile != null) {
            vo.setDuplicateFileId(existingFile.getId());
            vo.setDuplicateFileKey(existingFile.getFileKey());
        }
        return vo;
    }

    @Override
    public String getPreviewUrl(Long fileId) {
        LoginUser loginUser = UserContext.getLoginUser();
        log.info("[FilePreview] 开始获取预览 - fileId={}, companyId={}", fileId, loginUser.getCompanyId());
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            log.error("[FilePreview] 文件不存在 - fileId={}", fileId);
            throw new BizException(FileStorageExceptionEnum.FILE_NOT_FOUND.getMessage());
        }
        // 强制公司隔离
        if (!loginUser.getCompanyId().equals(sysFile.getCompanyId())) {
            log.error("[FilePreview] 公司隔离校验失败 - requestCompany={}, fileCompany={}", loginUser.getCompanyId(), sysFile.getCompanyId());
            throw new BizException(FileStorageExceptionEnum.COMPANY_ID_MISMATCH.getMessage());
        }
        String fileKey = sysFile.getFileKey();
        String storageType = sysFile.getStorageType();
        log.info("[FilePreview] 文件记录已找到 - fileId={}, fileKey={}, storageType={}, fileName={}", fileId, fileKey, storageType, sysFile.getFileName());
        String previewUrl = fileStorageFactory.getFileStorage().getPreviewUrl(fileKey);
        log.info("[FilePreview] 预览URL生成完成 - fileId={}, previewUrl={}", fileId, previewUrl);
        auditLogUtil.record(CommonConst.MODULE_FILE, CommonConst.OPER_TYPE_FILE_PREVIEW,
                String.valueOf(fileId), null, null);
        return previewUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        LoginUser loginUser = UserContext.getLoginUser();
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new BizException(FileStorageExceptionEnum.FILE_NOT_FOUND.getMessage());
        }
        if (!loginUser.getCompanyId().equals(sysFile.getCompanyId())) {
            throw new BizException(FileStorageExceptionEnum.COMPANY_ID_MISMATCH.getMessage());
        }

        // 物理删除存储文件
        try {
            fileStorageFactory.getFileStorage().delete(sysFile.getFileKey());
        } catch (Exception e) {
            log.warn("存储文件删除失败（已逻辑删除数据库记录）: fileKey={}", sysFile.getFileKey(), e);
        }

        // 逻辑删除数据库记录
        sysFile.setIsDelete(1);
        sysFile.setUpdateBy(loginUser.getUserId());
        fileMapper.updateById(sysFile);

        auditLogUtil.record(CommonConst.MODULE_FILE, CommonConst.OPER_TYPE_FILE_DELETE,
                String.valueOf(fileId), sysFile, null);
        log.info("文件删除成功: fileId={}, fileKey={}", fileId, sysFile.getFileKey());
    }

    @Override
    public PageVO<FileVO> page(Long companyId, String bizType, Integer pageNum, Integer pageSize) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long effectiveCompanyId = companyId != null ? companyId : loginUser.getCompanyId();
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getCompanyId, effectiveCompanyId)
                .like(StringUtils.hasText(bizType), SysFile::getBizType, bizType)
                .orderByDesc(SysFile::getCreateTime);
        Page<SysFile> result = fileMapper.selectPage(page, wrapper);
        List<FileVO> voList = result.getRecords().stream().map(f -> {
            FileVO vo = new FileVO();
            vo.setId(f.getId());
            vo.setFileKey(f.getFileKey());
            vo.setFileName(f.getFileName());
            vo.setFileSize(f.getFileSize());
            vo.setFileType(f.getFileType());
            vo.setFileExt(f.getFileExt());
            vo.setStorageType(f.getStorageType());
            vo.setMd5(f.getMd5());
            vo.setBizType(f.getBizType());
            vo.setBizId(f.getBizId());
            vo.setCreateTime(f.getCreateTime());
            vo.setPreviewUrl(fileStorageFactory.getFileStorage().getPreviewUrl(f.getFileKey()));
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /**
     * 解析最大文件大小配置（默认20MB）
     */
    private long parseMaxSize() {
        try {
            String maxStr = configService.getValueByKey("file.max-size");
            if (StringUtils.hasText(maxStr)) {
                return Long.parseLong(maxStr) * 1024 * 1024;
            }
        } catch (NumberFormatException e) {
            log.warn("file.max-size 配置无效，使用默认值20MB");
        }
        return 20L * 1024 * 1024;
    }
}
