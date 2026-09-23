package com.gbi.platform.service;

import com.gbi.platform.vo.FileUploadVO;
import com.gbi.platform.vo.FileVO;
import com.gbi.platform.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件业务服务接口
 *
 * @author gbi
 */
public interface FileService {

    /**
     * 上传文件：校验 → MD5查重提示 → 生成file_key → 存储 → 入库 → 审计日志
     *
     * @param file    上传文件
     * @param bizType 业务类型，默认 hr_entry
     * @param bizId   关联业务ID，默认 0
     * @return 上传结果VO（含fileId、previewUrl等）
     */
    FileUploadVO upload(MultipartFile file, String bizType, Long bizId);

    /**
     * 获取文件预览URL（内部调用，Controller层做302重定向）
     *
     * @param fileId 文件记录ID
     * @return 完整预览URL
     */
    String getPreviewUrl(Long fileId);

    /**
     * 删除文件（逻辑删除 + 物理删除存储文件 + 审计日志）
     * 注意：删除为敏感操作，需在Controller层完成二次审核校验后再调用
     *
     * @param fileId 文件记录ID
     */
    void deleteFile(Long fileId);

    /**
     * 分页查询文件列表（强制company_id隔离）
     *
     * @param companyId 租户ID（可由UserContext自动注入）
     * @param bizType   业务类型（可选）
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    PageVO<FileVO> page(Long companyId, String bizType, Integer pageNum, Integer pageSize);
}
