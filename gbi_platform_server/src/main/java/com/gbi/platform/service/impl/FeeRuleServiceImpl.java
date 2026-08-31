package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.FeeRuleDTO;
import com.gbi.platform.dto.FeeRuleQueryDTO;
import com.gbi.platform.entity.FeeItem;
import com.gbi.platform.entity.FeeRule;
import com.gbi.platform.mapper.FeeItemMapper;
import com.gbi.platform.mapper.FeeRuleMapper;
import com.gbi.platform.service.FeeRuleService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.FeeRuleOptionVO;
import com.gbi.platform.vo.FeeRuleVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 自定义收费规则服务实现：规则调用收费类型，收费方式/周期/滞纳金可配置
 * 同公司规则名唯一；收费类型必须存在（company 自动隔离，禁止跨公司引用）；
 * 新增/编辑/删除强制审计（oper_module=fee_rule）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeRuleServiceImpl implements FeeRuleService {

    private final FeeRuleMapper feeRuleMapper;

    /** 跨模块调用收费类型 Mapper 封装：组装收费类型名称 + 引用校验 */
    private final FeeItemMapper feeItemMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<FeeRuleVO> page(FeeRuleQueryDTO dto) {
        Page<FeeRule> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FeeRule> wrapper = new LambdaQueryWrapper<FeeRule>()
                .like(StringUtils.hasText(dto.getRuleName()), FeeRule::getRuleName, dto.getRuleName())
                .eq(dto.getFeeItemId() != null, FeeRule::getFeeItemId, dto.getFeeItemId())
                .eq(dto.getStatus() != null, FeeRule::getStatus, dto.getStatus())
                .orderByDesc(FeeRule::getId);
        Page<FeeRule> result = feeRuleMapper.selectPage(page, wrapper);

        // 批量组装收费类型名称
        Map<Long, FeeItem> itemMap = loadItemMap(result.getRecords());
        List<FeeRuleVO> voList = result.getRecords().stream()
                .map(r -> toVO(r, itemMap)).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void add(FeeRuleDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkRuleNameUnique(null, dto.getRuleName());
        checkFeeItemExists(dto.getFeeItemId());

        FeeRule rule = new FeeRule();
        rule.setCompanyId(loginUser.getCompanyId());
        rule.setRuleName(dto.getRuleName());
        rule.setFeeItemId(dto.getFeeItemId());
        rule.setCalcMode(dto.getCalcMode());
        rule.setPrice(dto.getPrice());
        rule.setPeriodType(dto.getPeriodType());
        rule.setOverdueRate(dto.getOverdueRate() == null ? BigDecimal.ZERO : dto.getOverdueRate());
        rule.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        rule.setRemark(dto.getRemark());
        feeRuleMapper.insert(rule);

        auditLogUtil.record(CommonConst.MODULE_FEE_RULE, CommonConst.OPER_TYPE_ADD,
                String.valueOf(rule.getId()), null, rule);
    }

    @Override
    public void update(FeeRuleDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("规则ID不能为空");
        }
        FeeRule old = getExists(dto.getId());
        checkRuleNameUnique(dto.getId(), dto.getRuleName());
        checkFeeItemExists(dto.getFeeItemId());

        FeeRule rule = new FeeRule();
        rule.setId(dto.getId());
        rule.setRuleName(dto.getRuleName());
        rule.setFeeItemId(dto.getFeeItemId());
        rule.setCalcMode(dto.getCalcMode());
        rule.setPrice(dto.getPrice());
        rule.setPeriodType(dto.getPeriodType());
        rule.setOverdueRate(dto.getOverdueRate() == null ? BigDecimal.ZERO : dto.getOverdueRate());
        rule.setStatus(dto.getStatus());
        rule.setRemark(dto.getRemark());
        feeRuleMapper.updateById(rule);

        auditLogUtil.record(CommonConst.MODULE_FEE_RULE, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, rule);
    }

    @Override
    public void delete(Long id) {
        FeeRule rule = getExists(id);
        feeRuleMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_FEE_RULE, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), rule, null);
    }

    @Override
    public long countByFeeItemId(Long feeItemId) {
        Long count = feeRuleMapper.selectCount(new LambdaQueryWrapper<FeeRule>()
                .eq(FeeRule::getFeeItemId, feeItemId));
        return count == null ? 0L : count;
    }

    @Override
    public List<FeeRuleOptionVO> listRuleOptions() {
        // 仅启用规则可被绑定
        List<FeeRule> rules = feeRuleMapper.selectList(new LambdaQueryWrapper<FeeRule>()
                .eq(FeeRule::getStatus, CommonConst.STATUS_ENABLED)
                .orderByAsc(FeeRule::getId));
        return toOptionVOList(rules);
    }

    @Override
    public List<FeeRuleOptionVO> listOptionsByIds(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // selectBatchIds 受 company_id 拦截器约束，跨公司规则查不到
        List<FeeRule> rules = feeRuleMapper.selectBatchIds(ruleIds);
        return toOptionVOList(rules);
    }

    /**
     * 规则 → 下拉选项（批量组装收费类型名称）
     */
    private List<FeeRuleOptionVO> toOptionVOList(List<FeeRule> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, FeeItem> itemMap = loadItemMap(rules);
        return rules.stream().map(r -> {
            FeeItem item = itemMap.get(r.getFeeItemId());
            FeeRuleOptionVO vo = new FeeRuleOptionVO();
            vo.setId(r.getId());
            vo.setRuleName(r.getRuleName());
            vo.setFeeItemId(r.getFeeItemId());
            vo.setFeeItemName(item == null ? null : item.getFeeItemName());
            vo.setCategoryType(item == null ? null : item.getCategoryType());
            vo.setCalcMode(r.getCalcMode());
            vo.setCalcModeText(r.getCalcMode() == null ? null : switch (r.getCalcMode()) {
                case 1 -> "定额";
                case 2 -> "按面积";
                default -> "未知";
            });
            vo.setPrice(r.getPrice());
vo.setPeriodType(r.getPeriodType());
        vo.setPeriodTypeText(r.getPeriodType() == null ? null : switch (r.getPeriodType()) {
            case 0 -> "不使用周期";
            case 1 -> "按年";
            case 2 -> "按月";
            case 3 -> "按日";
            default -> "未知";
        });
            vo.setOverdueRate(r.getOverdueRate());
            vo.setStatus(r.getStatus());
            return vo;
        }).toList();
    }

    /**
     * 同公司规则名称唯一校验
     */
    private void checkRuleNameUnique(Long excludeId, String ruleName) {
        Long count = feeRuleMapper.selectCount(new LambdaQueryWrapper<FeeRule>()
                .eq(FeeRule::getRuleName, ruleName)
                .ne(excludeId != null, FeeRule::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("规则名称已存在");
        }
    }

    /**
     * 收费类型必须存在（selectById 受 company_id 拦截器约束，跨公司类型查询不到）
     */
    private FeeItem checkFeeItemExists(Long feeItemId) {
        FeeItem item = feeItemMapper.selectById(feeItemId);
        if (item == null) {
            throw new BizException("收费类型不存在或已删除");
        }
        return item;
    }

    private FeeRule getExists(Long id) {
        FeeRule rule = feeRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException("收费规则不存在或已删除");
        }
        return rule;
    }

    /**
     * 批量加载收费类型名称（跨模块查询，company 自动隔离）
     */
    private Map<Long, FeeItem> loadItemMap(List<FeeRule> rules) {
        List<Long> itemIds = rules.stream()
                .map(FeeRule::getFeeItemId).filter(Objects::nonNull).distinct().toList();
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return feeItemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(FeeItem::getId, i -> i));
    }

    private FeeRuleVO toVO(FeeRule rule, Map<Long, FeeItem> itemMap) {
        FeeItem item = itemMap.get(rule.getFeeItemId());
        FeeRuleVO vo = new FeeRuleVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setFeeItemId(rule.getFeeItemId());
        vo.setFeeItemName(item == null ? null : item.getFeeItemName());
        vo.setCalcMode(rule.getCalcMode());
        vo.setCalcModeText(rule.getCalcMode() == null ? null : switch (rule.getCalcMode()) {
            case 1 -> "定额";
            case 2 -> "按面积";
            default -> "未知";
        });
        vo.setPrice(rule.getPrice());
        vo.setPeriodType(rule.getPeriodType());
        vo.setPeriodTypeText(rule.getPeriodType() == null ? null : switch (rule.getPeriodType()) {
            case 0 -> "不使用周期";
            case 1 -> "按年";
            case 2 -> "按月";
            case 3 -> "按日";
            default -> "未知";
        });
        vo.setOverdueRate(rule.getOverdueRate());
        vo.setStatus(rule.getStatus());
        vo.setRemark(rule.getRemark());
        vo.setCreateTime(rule.getCreateTime());
        return vo;
    }
}