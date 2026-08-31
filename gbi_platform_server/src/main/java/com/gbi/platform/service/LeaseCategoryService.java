package com.gbi.platform.service;

import com.gbi.platform.dto.CategoryDTO;
import com.gbi.platform.dto.CategoryQueryDTO;
import com.gbi.platform.vo.CategoryVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;

/**
 * 租赁分类服务（商铺/仓库/车位等，子公司可自定义）
 *
 * @author gbi
 */
public interface LeaseCategoryService {

    /** 全量列表（启用状态优先，供摊位/合同下拉选择） */
    List<CategoryVO> list(CategoryQueryDTO dto);

    /** 分类分页 */
    PageVO<CategoryVO> page(CategoryQueryDTO dto);

    /** 新增分类（同公司同名唯一） */
    void add(CategoryDTO dto);

    /** 编辑分类 */
    void update(CategoryDTO dto);

    /** 删除分类（被摊位引用禁止删除，逻辑删除） */
    void delete(Long id);
}