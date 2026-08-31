package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FeeRuleStallRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收费规则-摊位绑定 Mapper（biz_fee_rule_stall_rel，company_id 自动隔离）
 *
 * @author gbi
 */
@Mapper
public interface FeeRuleStallRelMapper extends BaseMapper<FeeRuleStallRel> {

    /**
     * 物理删除摊位全部绑定（含历史逻辑删除行）
     * <p>说明：唯一键 uk_rule_stall(rule_id, stall_id, is_delete) 与逻辑删除天然冲突——
     * 软删行 (rule,stall,1) 会与下次逻辑删旧目标撞唯一键导致 DuplicateKeyException；
     * 本表为纯关系表，无独立业务/审计价值（绑定历史已由 sys_audit_log 永久归档），
     * 故绕过全局逻辑删除做物理清理，保证全量替换幂等；财务/审计/地图表不受影响。
     *
     * @param stallId 摊位ID
     */
    @Delete("DELETE FROM biz_fee_rule_stall_rel WHERE stall_id = #{stallId}")
    int deleteByStallIdPhysical(@Param("stallId") Long stallId);
}