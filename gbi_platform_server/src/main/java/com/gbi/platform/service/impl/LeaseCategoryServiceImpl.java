package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.CategoryDTO;
import com.gbi.platform.dto.CategoryQueryDTO;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.service.LeaseCategoryService;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.CategoryVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 租赁分类服务实现：商铺/仓库/车位等分类，子公司可自定义增删
 * 同公司同名唯一；删除前置校验：被铺位引用禁止删除；
 * 新增/编辑/删除强制审计（oper_module=stall_category）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseCategoryServiceImpl implements LeaseCategoryService {

    private final StallCategoryMapper categoryMapper;

    private final LeaseStallService leaseStallService;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<CategoryVO> list(CategoryQueryDTO dto) {
        LambdaQueryWrapper<StallCategory> wrapper = new LambdaQueryWrapper<StallCategory>()
                .eq(dto != null && dto.getStatus() != null, StallCategory::getStatus, dto == null ? null : dto.getStatus())
                .eq(StallCategory::getIsDelete, 0)
                .orderByAsc(StallCategory::getSortOrder)
                .orderByAsc(StallCategory::getId);
        List<StallCategory> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::toVO).toList();
    }

    @Override
    public PageVO<CategoryVO> page(CategoryQueryDTO dto) {
        Page<StallCategory> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<StallCategory> wrapper = new LambdaQueryWrapper<StallCategory>()
                .like(StringUtils.hasText(dto.getCategoryName()), StallCategory::getCategoryName, dto.getCategoryName())
                .eq(dto.getStatus() != null, StallCategory::getStatus, dto.getStatus())
                .eq(StallCategory::getIsDelete, 0)
                .orderByAsc(StallCategory::getSortOrder)
                .orderByAsc(StallCategory::getId);
        Page<StallCategory> result = categoryMapper.selectPage(page, wrapper);
        List<CategoryVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void add(CategoryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkNameUnique(null, dto.getCategoryName());

        StallCategory category = new StallCategory();
        category.setCompanyId(loginUser.getCompanyId());
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        category.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        category.setRemark(dto.getRemark());
        categoryMapper.insert(category);

        auditLogUtil.record(CommonConst.MODULE_LEASE_CATEGORY, CommonConst.OPER_TYPE_ADD,
                String.valueOf(category.getId()), null, category);
    }

    @Override
    public void update(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        StallCategory old = getExists(dto.getId());
        checkNameUnique(dto.getId(), dto.getCategoryName());

        StallCategory category = new StallCategory();
        category.setId(dto.getId());
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus());
        category.setRemark(dto.getRemark());
        categoryMapper.updateById(category);

        auditLogUtil.record(CommonConst.MODULE_LEASE_CATEGORY, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, category);
    }

    @Override
    public void delete(Long id) {
        StallCategory category = getExists(id);
        // 被铺位引用禁止删除（跨模块调用铺位 Service 接口统计）
        if (leaseStallService.countByCategoryId(id) > 0) {
            throw new BizException("该分类下存在铺位，禁止删除");
        }
        categoryMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_LEASE_CATEGORY, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), category, null);
    }

    /**
     * 同公司分类名称唯一校验
     */
    private void checkNameUnique(Long excludeId, String categoryName) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<StallCategory>()
                .eq(StallCategory::getCategoryName, categoryName)
                .eq(StallCategory::getIsDelete, 0)
                .ne(excludeId != null, StallCategory::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("分类名称已存在");
        }
    }

    private StallCategory getExists(Long id) {
        StallCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在或已删除");
        }
        return category;
    }

    private CategoryVO toVO(StallCategory c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setCategoryName(c.getCategoryName());
        vo.setSortOrder(c.getSortOrder());
        vo.setStatus(c.getStatus());
        vo.setRemark(c.getRemark());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}
