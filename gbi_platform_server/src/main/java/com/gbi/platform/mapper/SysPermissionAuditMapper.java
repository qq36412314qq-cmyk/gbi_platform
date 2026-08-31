package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.entity.SysPermissionAudit;
import com.gbi.platform.vo.PermissionAuditVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 权限二级复核 Mapper（联查申请人/被授权人/复核人姓名）
 *
 * @author gbi
 */
@Mapper
public interface SysPermissionAuditMapper extends BaseMapper<SysPermissionAudit> {

    /**
     * 复核记录分页联查（sys_user 逻辑删除表，join 时手工过滤 is_delete）
     * 一期 permissionType 固定 ROLE（表结构未含该字段，二期扩展）
     */
    @Select({
            "<script>",
            "SELECT pa.id, pa.company_id, pa.target_user_id, pa.apply_user_id,",
            "       u.real_name  AS apply_user_name,",
            "       'ROLE'       AS permission_type,",
            "       tu.real_name AS target_name,",
            "       pa.apply_reason AS change_desc,",
            "       pa.permission_list, pa.apply_reason, pa.audit_status, pa.audit_comment,",
            "       au.real_name AS audit_user_name, pa.apply_time, pa.audit_time",
            "FROM sys_permission_audit pa",
            "LEFT JOIN sys_user u  ON pa.apply_user_id  = u.id  AND u.is_delete = 0",
            "LEFT JOIN sys_user tu ON pa.target_user_id = tu.id AND tu.is_delete = 0",
            "LEFT JOIN sys_user au ON pa.audit_user_id  = au.id AND au.is_delete = 0",
            "<where>",
            "  <if test='auditStatus != null'> AND pa.audit_status = #{auditStatus} </if>",
            "  <if test='applyUserName != null and applyUserName != \"\"'> AND u.real_name LIKE CONCAT('%', #{applyUserName}, '%') </if>",
            "  <if test='companyId != null'> AND pa.company_id = #{companyId} </if>",
            "</where>",
            "ORDER BY pa.id DESC",
            "</script>"
    })
    Page<PermissionAuditVO> selectPageVO(Page<PermissionAuditVO> page,
                                         @Param("auditStatus") Integer auditStatus,
                                         @Param("applyUserName") String applyUserName,
                                         @Param("companyId") Long companyId);
}