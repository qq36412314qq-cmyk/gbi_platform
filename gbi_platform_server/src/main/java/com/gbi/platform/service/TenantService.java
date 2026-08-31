package com.gbi.platform.service;

import com.gbi.platform.dto.TenantAddDTO;
import com.gbi.platform.dto.TenantQueryDTO;
import com.gbi.platform.dto.TenantUpdateDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.TenantVO;

/**
 * 租户档案服务（物业管理-租户管理）
 *
 * @author gbi
 */
public interface TenantService {

    /** 租户分页查询 */
    PageVO<TenantVO> page(TenantQueryDTO dto);

    /** 新增租户（company_id 从登录上下文赋值，证件类字段密文存储） */
    void add(TenantAddDTO dto);

    /** 编辑租户 */
    void update(TenantUpdateDTO dto);

    /** 删除租户（逻辑删除，有生效合同禁止删除） */
    void delete(Long id);

    /** 租户是否存在（供合同新增等跨模块校验） */
    boolean existsById(Long id);
}