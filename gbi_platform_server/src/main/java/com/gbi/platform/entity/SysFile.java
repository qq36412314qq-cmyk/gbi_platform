package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统文件记录实体：sys_file
 * 多租户隔离（company_id）+ 逻辑删除
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    /** 租户ID，0=集团全局 */
    private Long companyId;

    /** 业务类型：contract/oa/hr/finance/property/audit 等 */
    private String bizType;

    /** 关联业务单据ID，0表示无关联 */
    private Long bizId;

    /** 跨存储统一相对路径唯一标识（不含域名），格式：{companyId}/{bizType}/{yyyyMMdd}/{uuid}.{ext} */
    private String fileKey;

    /** 文件MD5值，用于去重提示和完整性校验 */
    private String md5;

    /** 原始文件名（含后缀） */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME类型，如 image/png application/pdf */
    private String fileType;

    /** 文件后缀，如 jpg pdf */
    private String fileExt;

    /** 存储类型快照（审计追溯用，运行时不以此为驱动依据）：local/alioss/tianyoss */
    private String storageType;
}
