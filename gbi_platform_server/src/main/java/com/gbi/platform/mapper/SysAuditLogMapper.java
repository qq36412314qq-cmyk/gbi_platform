package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper（只新增/查询，禁止删除）
 *
 * @author gbi
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
