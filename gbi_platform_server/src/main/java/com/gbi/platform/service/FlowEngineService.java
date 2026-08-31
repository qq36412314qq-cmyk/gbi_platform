package com.gbi.platform.service;

import com.gbi.platform.dto.FlowDefDTO;
import com.gbi.platform.dto.FlowHandleDTO;
import com.gbi.platform.dto.FlowQueryDTO;
import com.gbi.platform.dto.FlowSubmitDTO;
import com.gbi.platform.vo.FlowDefVO;
import com.gbi.platform.vo.FlowInstanceDetailVO;
import com.gbi.platform.vo.FlowInstanceVO;
import com.gbi.platform.vo.FlowTaskVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;

/**
 * 统一审批引擎服务（自研轻量表驱动，一期业务可平滑替换 Flowable）
 * 承载：业务单据自身审批（合同）+ 敏感高危审批（大额优惠/合同作废终止/计划大额调账）
 * 对齐《应收应付计划+统一审批引擎+优惠管理模块设计规范》第三章
 *
 * @author gbi
 */
public interface FlowEngineService {

    /**
     * 提交审批（创建流程实例 + 首节点任务）
     *
     * @return 流程实例ID
     */
    Long submit(FlowSubmitDTO dto);

    /**
     * 提交审批（便捷重载：业务侧一键发起，实际业务类型以流程定义为准）
     *
     * @return 流程实例ID
     */
    Long submit(String defCode, String sourceType, String sourceId, String title);

    /**
     * 审批处理（pass通过 / reject驳回 / transfer转交）
     */
    void handle(FlowHandleDTO dto);

    /**
     * 催办（写记录 + 站内信，不改变流转）
     */
    void urge(Long taskId);

    /**
     * 撤回申请（仅申请人，审批中可撤回）
     */
    void revoke(Long instanceId);

    /**
     * 业务侧强制终止（如合同直接到期）
     */
    void terminate(Long instanceId, String remark);

    /**
     * 我的待办分页
     */
    PageVO<FlowTaskVO> pageMyTodo(FlowQueryDTO dto);

    /**
     * 我的申请分页
     */
    PageVO<FlowInstanceVO> pageMyApply(FlowQueryDTO dto);

    /**
     * 流程实例分页（集团全量，子公司仅本公司）
     */
    PageVO<FlowInstanceVO> pageInstance(FlowQueryDTO dto);

    /**
     * 流程详情（含流转轨迹 flow_record 全展示）
     */
    FlowInstanceDetailVO instanceDetail(Long instanceId);

    /**
     * 流程定义分页（集团专属配置）
     */
    PageVO<FlowDefVO> pageDefinition(FlowQueryDTO dto);

    /**
     * 新增流程定义
     */
    void addDefinition(FlowDefDTO dto);

    /**
     * 编辑流程定义
     */
    void updateDefinition(FlowDefDTO dto);

    /**
     * 删除流程定义（有实例禁止删除，逻辑删除）
     */
    void deleteDefinition(Long id);

    /**
     * 按角色编码解析审批人用户ID集合（供业务/节点展开复用）
     *
     * @param roleCode 角色编码
     * @return 用户ID集合（含集团全局角色与本公司角色）
     */
    List<Long> resolveRoleUserIds(String roleCode);
}