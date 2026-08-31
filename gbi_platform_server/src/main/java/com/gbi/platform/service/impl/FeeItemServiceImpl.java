package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.FeeItemDTO;
import com.gbi.platform.dto.FeeItemQueryDTO;
import com.gbi.platform.entity.FeeItem;
import com.gbi.platform.mapper.FeeItemMapper;
import com.gbi.platform.service.FeeItemService;
import com.gbi.platform.service.FeeRuleService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.FeeItemVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 自定义收费类型服务实现：租金/物业费/水费/电费/押金/其他等，子公司可配置增删
 * 同公司同名唯一；删除前置校验：被收费规则引用禁止删除；
 * 新增/编辑/删除强制审计（oper_module=fee_item）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeItemServiceImpl implements FeeItemService {

    private final FeeItemMapper feeItemMapper;

    /** 跨模块调用收费规则 Service 接口：删除前置引用校验 */
    private final FeeRuleService feeRuleService;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<FeeItemVO> list(FeeItemQueryDTO dto) {
        LambdaQueryWrapper<FeeItem> wrapper = new LambdaQueryWrapper<FeeItem>()
                .like(StringUtils.hasText(dto.getFeeItemName()), FeeItem::getFeeItemName, dto.getFeeItemName())
                .orderByAsc(FeeItem::getId);
        return feeItemMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public PageVO<FeeItemVO> page(FeeItemQueryDTO dto) {
        Page<FeeItem> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FeeItem> wrapper = new LambdaQueryWrapper<FeeItem>()
                .like(StringUtils.hasText(dto.getFeeItemName()), FeeItem::getFeeItemName, dto.getFeeItemName())
                .orderByAsc(FeeItem::getId);
        Page<FeeItem> result = feeItemMapper.selectPage(page, wrapper);
        List<FeeItemVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void add(FeeItemDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkNameUnique(null, dto.getFeeItemName());

        FeeItem item = new FeeItem();
        item.setCompanyId(loginUser.getCompanyId());
        item.setFeeItemName(dto.getFeeItemName());
        item.setCategoryType(dto.getCategoryType());
        item.setCalcUnit(dto.getCalcUnit());
        item.setRemark(dto.getRemark());
        feeItemMapper.insert(item);

        auditLogUtil.record(CommonConst.MODULE_FEE_ITEM, CommonConst.OPER_TYPE_ADD,
                String.valueOf(item.getId()), null, item);
    }

    @Override
    public void update(FeeItemDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("收费类型ID不能为空");
        }
        FeeItem old = getExists(dto.getId());
        checkNameUnique(dto.getId(), dto.getFeeItemName());

        FeeItem item = new FeeItem();
        item.setId(dto.getId());
        item.setFeeItemName(dto.getFeeItemName());
        item.setCategoryType(dto.getCategoryType());
        item.setCalcUnit(dto.getCalcUnit());
        item.setRemark(dto.getRemark());
        feeItemMapper.updateById(item);

        auditLogUtil.record(CommonConst.MODULE_FEE_ITEM, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, item);
    }

    @Override
    public void delete(Long id) {
        FeeItem item = getExists(id);
        // 被收费规则引用禁止删除（跨模块调用收费规则 Service 接口统计）
        if (feeRuleService.countByFeeItemId(id) > 0) {
            throw new BizException("该收费类型已被收费规则引用，禁止删除");
        }
        feeItemMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_FEE_ITEM, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), item, null);
    }

    /**
     * 同公司收费类型名称唯一校验
     */
    private void checkNameUnique(Long excludeId, String feeItemName) {
        Long count = feeItemMapper.selectCount(new LambdaQueryWrapper<FeeItem>()
                .eq(FeeItem::getFeeItemName, feeItemName)
                .ne(excludeId != null, FeeItem::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("收费类型名称已存在");
        }
    }

    private FeeItem getExists(Long id) {
        FeeItem item = feeItemMapper.selectById(id);
        if (item == null) {
            throw new BizException("收费类型不存在或已删除");
        }
        return item;
    }

    private FeeItemVO toVO(FeeItem item) {
        FeeItemVO vo = new FeeItemVO();
        vo.setId(item.getId());
        vo.setFeeItemName(item.getFeeItemName());
        vo.setCategoryType(item.getCategoryType());
        vo.setCategoryTypeText(item.getCategoryType() == null ? null : categoryTypeText(item.getCategoryType()));
        vo.setCalcUnit(item.getCalcUnit());
        vo.setRemark(item.getRemark());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    /**
     * 收费类别文本
     */
    private String categoryTypeText(int categoryType) {
        return switch (categoryType) {
            case CommonConst.FEE_CATEGORY_RENT -> "租金";
            case CommonConst.FEE_CATEGORY_PROPERTY -> "物业费";
            case CommonConst.FEE_CATEGORY_WATER -> "水费";
            case CommonConst.FEE_CATEGORY_ELEC -> "电费";
            case CommonConst.FEE_CATEGORY_DEPOSIT -> "押金";
            default -> "其他";
        };
    }
}