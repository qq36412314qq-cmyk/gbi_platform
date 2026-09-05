package com.gbi.platform.service;

import com.gbi.platform.dto.FeeRuleDTO;
import com.gbi.platform.dto.FeeRuleQueryDTO;
import com.gbi.platform.vo.FeeRuleOptionVO;
import com.gbi.platform.vo.FeeRuleVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;

/**
 * 自定义收费规则服务（调用收费类型，收费方式/周期/滞纳金可配置）
 *
 * @author gbi
 */
public interface FeeRuleService {

    /** 收费规则分页（含收费类型名称） */
    PageVO<FeeRuleVO> page(FeeRuleQueryDTO dto);

    /** 新增收费规则（同公司规则名唯一，收费类型必须存在） */
    void add(FeeRuleDTO dto);

    /** 编辑收费规则 */
    void update(FeeRuleDTO dto);

    /** 删除收费规则（逻辑删除） */
    void delete(Long id);

    /** 统计指定收费类型被多少规则引用（跨模块供收费类型删除前置校验） */
    long countByFeeItemId(Long feeItemId);

    /**
     * 启用中的收费规则选项（供铺位绑定下拉选择，company_id 自动隔离）
     *
     * @return 仅 status=1 的规则选项
     */
    List<FeeRuleOptionVO> listRuleOptions();

    /**
     * 按规则ID批量取选项（跨模块供绑定校验/回显组装，company_id 自动隔离）
     *
     * @param ruleIds 规则ID集合
     * @return 规则选项集合（不存在/跨公司的规则不会出现在结果中）
     */
    List<FeeRuleOptionVO> listOptionsByIds(List<Long> ruleIds);
}