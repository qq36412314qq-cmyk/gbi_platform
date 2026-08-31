package com.gbi.platform.service;

import com.gbi.platform.dto.AuditLogQueryDTO;
import com.gbi.platform.vo.AuditLogVO;
import com.gbi.platform.vo.PageVO;

/**
 * 审计日志服务：只读查询 + 导出（永久归档，禁止删除）
 *
 * @author gbi
 */
public interface AuditService {

    /**
     * 审计日志分页（子公司账号只看本公司，集团管理员全量）
     */
    PageVO<AuditLogVO> page(AuditLogQueryDTO dto);

    /**
     * 导出日志：生成 CSV 文件到上传目录，返回下载地址
     */
    String export(AuditLogQueryDTO dto);
}
