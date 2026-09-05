package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.entity.FeeRuleStallRel;
import com.gbi.platform.mapper.FeeRuleStallRelMapper;
import com.gbi.platform.service.FeeRuleService;
import com.gbi.platform.service.FeeRuleStallRelService;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.FeeRuleOptionVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.StallRuleRelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 收费规则-铺位绑定服务实现
 * 同一收费类型限选一条（按 fee_item_id 去重校验）；绑定前校验铺位与规则归属公司（自动隔离）；
 * 全量替换绑定（先逻辑删旧再插入新）；变更强制审计（oper_module=fee_rule，类型=绑定）
 *
 * @author gbi
 */
@Slf4j
@Service
public class FeeRuleStallRelServiceImpl implements FeeRuleStallRelService {

    private final FeeRuleStallRelMapper relMapper;

    /** 跨模块调用收费规则 Service 接口：选项查询与绑定校验 */
    private final FeeRuleService feeRuleService;

    /** 跨模块调用租赁铺位 Service 接口：校验铺位存在（company 自动隔离）；
     *  @Lazy 打在构造器参数上，打破 LeaseStallServiceImpl ↔ FeeRuleStallRelServiceImpl 循环依赖 */
    private final LeaseStallService leaseStallService;

    private final AuditLogUtil auditLogUtil;

    public FeeRuleStallRelServiceImpl(FeeRuleStallRelMapper relMapper,
                                      FeeRuleService feeRuleService,
                                      @Lazy LeaseStallService leaseStallService,
                                      AuditLogUtil auditLogUtil) {
        this.relMapper = relMapper;
        this.feeRuleService = feeRuleService;
        this.leaseStallService = leaseStallService;
        this.auditLogUtil = auditLogUtil;
    }

    @Override
    public List<FeeRuleOptionVO> listRuleOptions() {
        // 仅返回启用规则（转发收费规则 Service，company 自动隔离）
        return feeRuleService.listRuleOptions();
    }

    @Override
    public List<StallRuleRelVO> listByStallId(Long stallId) {
        List<StallRuleRelVO> list = listByStallIds(Collections.singletonList(stallId)).get(stallId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public Map<Long, List<StallRuleRelVO>> listByStallIds(Collection<Long> stallIds) {
        if (stallIds == null || stallIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<FeeRuleStallRel> rels = relMapper.selectList(new LambdaQueryWrapper<FeeRuleStallRel>()
                .in(FeeRuleStallRel::getStallId, stallIds)
                .orderByAsc(FeeRuleStallRel::getId));
        if (rels.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ruleIds = rels.stream().map(FeeRuleStallRel::getRuleId).distinct().toList();
        Map<Long, FeeRuleOptionVO> optionMap = feeRuleService.listOptionsByIds(ruleIds).stream()
                .collect(Collectors.toMap(FeeRuleOptionVO::getId, o -> o));
        // 按铺位分组并组装规则信息（顺序按 rel.id 升序，与绑定顺序一致）
        Map<Long, List<StallRuleRelVO>> result = new HashMap<>();
        for (FeeRuleStallRel rel : rels) {
            FeeRuleOptionVO opt = optionMap.get(rel.getRuleId());
            StallRuleRelVO vo = new StallRuleRelVO();
            vo.setRelId(rel.getId());
            vo.setRuleId(rel.getRuleId());
            if (opt != null) {
                vo.setRuleName(opt.getRuleName());
                vo.setFeeItemId(opt.getFeeItemId());
                vo.setFeeItemName(opt.getFeeItemName());
                vo.setCategoryType(opt.getCategoryType());
                vo.setCalcMode(opt.getCalcMode());
                vo.setCalcModeText(opt.getCalcModeText());
                vo.setPrice(opt.getPrice());
                vo.setPeriodType(opt.getPeriodType());
                vo.setPeriodTypeText(opt.getPeriodTypeText());
                vo.setOverdueRate(opt.getOverdueRate());
            }
            result.computeIfAbsent(rel.getStallId(), k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindings(Long stallId, List<Long> ruleIds) {
        if (stallId == null) {
            throw new BizException("铺位ID不能为空");
        }
        if (ruleIds == null) {
            // 未传规则集合：保持现状（兼容旧调用）
            return;
        }
        // 铺位必须存在（getOptionsByIds 受 company_id 拦截器约束，跨公司铺位查不到）
        Map<Long, StallOptionVO> stallMap = leaseStallService.getOptionsByIds(Collections.singletonList(stallId));
        if (stallMap.isEmpty()) {
            throw new BizException("铺位不存在或已删除");
        }

        LoginUser loginUser = UserContext.getLoginUser();
        List<Long> distinctRuleIds = ruleIds.stream().distinct().toList();

        // 旧绑定快照（审计用）
        List<StallRuleRelVO> oldRels = listByStallId(stallId);

        // 校验规则：存在/启用/同收费类型唯一（单次赋值保证 effectively final）
        List<FeeRuleOptionVO> options = distinctRuleIds.isEmpty()
                ? Collections.emptyList()
                : feeRuleService.listOptionsByIds(distinctRuleIds);
        if (!distinctRuleIds.isEmpty()) {
            // 数量不一致 = 存在无效/停用/跨公司规则，拒绝绑定
            if (options.size() != distinctRuleIds.size()) {
                throw new BizException("存在无效或已停用的收费规则，请刷新后重试");
            }
            // 同一收费类型只能绑定一条规则（核心业务约束）
            Map<Long, Long> feeItemCount = options.stream()
                    .collect(Collectors.groupingBy(FeeRuleOptionVO::getFeeItemId, Collectors.counting()));
            feeItemCount.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .findFirst()
                    .ifPresent(e -> {
                        String feeItemName = options.stream()
                                .filter(o -> Objects.equals(o.getFeeItemId(), e.getKey()))
                                .map(FeeRuleOptionVO::getFeeItemName).findFirst().orElse("该类型");
                        throw new BizException("同一收费类型只能选择一条收费规则：" + feeItemName);
                    });
            // 仅启用规则可绑定
            options.stream().filter(o -> !Objects.equals(o.getStatus(), CommonConst.STATUS_ENABLED))
                    .findFirst()
                    .ifPresent(o -> {
                        throw new BizException("收费规则「" + o.getRuleName() + "」已停用，禁止绑定");
                    });
        }

        // 全量替换：先物理清空旧绑定（含历史软删行，规避 uk_rule_stall 含 is_delete 的软删冲突），再插入新绑定（幂等：重复提交结果一致）
        relMapper.deleteByStallIdPhysical(stallId);
        for (FeeRuleOptionVO opt : options) {
            FeeRuleStallRel rel = new FeeRuleStallRel();
            rel.setCompanyId(loginUser.getCompanyId());
            rel.setRuleId(opt.getId());
            rel.setStallId(stallId);
            rel.setOverrideFlag(CommonConst.OVERRIDE_FLAG_NO);
            relMapper.insert(rel);
        }

        // 绑定变更审计（oper_module=fee_rule，类型=绑定）
        auditLogUtil.record(CommonConst.MODULE_FEE_RULE, CommonConst.OPER_TYPE_BIND,
                String.valueOf(stallId), oldRels, options);
    }
}