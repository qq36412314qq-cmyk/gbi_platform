package com.gbi.platform.service;

import com.gbi.platform.dto.OrgDTO;
import com.gbi.platform.vo.OrgTreeVO;

import java.util.List;

/**
 * 组织服务：sys_org 树形维护
 *
 * @author gbi
 */
public interface OrgService {

    /**
     * 组织树（集团管理员查全量，子公司账号自动过滤本公司）
     */
    List<OrgTreeVO> tree();

    /**
     * 新增组织
     */
    void add(OrgDTO dto);

    /**
     * 编辑组织
     */
    void update(OrgDTO dto);

    /**
     * 删除组织（有子组织不允许删除）
     */
    void delete(Long id);
}
