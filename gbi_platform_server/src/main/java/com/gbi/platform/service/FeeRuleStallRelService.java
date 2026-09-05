package com.gbi.platform.service;

import com.gbi.platform.vo.FeeRuleOptionVO;
import com.gbi.platform.vo.StallRuleRelVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 收费规则-铺位绑定服务（biz_fee_rule_stall_rel）
 * 铺位多选收费规则，同一收费类型（biz_fee_item）限选一条
 *
 * @author gbi
 */
public interface FeeRuleStallRelService {

    /**
     * 启用中的收费规则选项（供铺位绑定下拉选择，company_id 自动隔离）
     *
     * @return 仅 status=1 的规则选项（含收费类型名称）
     */
    List<FeeRuleOptionVO> listRuleOptions();

    /**
     * 查询铺位已绑定收费规则（回显用，company_id 自动隔离）
     *
     * @param stallId 铺位ID
     * @return 已绑定规则列表（含规则/收费类型名称）
     */
    List<StallRuleRelVO> listByStallId(Long stallId);

    /**
     * 批量查询多个铺位已绑定收费规则（分页列表组装用，一次查询避免 N+1，company_id 自动隔离）
     *
     * @param stallIds 铺位ID集合
     * @return stallId → 已绑定规则列表（无绑定的铺位不包含在 map 中）
     */
    Map<Long, List<StallRuleRelVO>> listByStallIds(Collection<Long> stallIds);

    /**
     * 全量替换铺位收费规则绑定
     * <ul>
     *   <li>ruleIds 为 null：不处理（保持现状，兼容旧调用）</li>
     *   <li>ruleIds 为空数组：清空绑定</li>
     *   <li>非空：校验后先逻辑删旧再插入新绑定</li>
     * </ul>
     * 校验规则：铺位必须存在（company 自动隔离）；每条规则必须存在且启用（停用/跨公司拦截）；
     * 同一收费类型只能绑定一条规则（按 fee_item_id 去重校验，违者抛 BizException）
     *
     * @param stallId 铺位ID
     * @param ruleIds 收费规则ID集合
     */
    void saveBindings(Long stallId, List<Long> ruleIds);
}