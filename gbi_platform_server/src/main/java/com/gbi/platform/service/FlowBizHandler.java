package com.gbi.platform.service;

/**
 * 审批引擎业务回调接口：审批动作通过 FlowBizHandler 回调业务 Service
 * 引擎不感知具体业务，业务接入层只需实现本接口并注册（bizType 匹配）
 * 对齐《应收应付计划+统一审批引擎+优惠管理模块设计规范》3.5
 *
 * @author gbi
 */
public interface FlowBizHandler {

    /**
     * 支持的业务类型（对齐 flow_definition.biz_type / 计划 biz_type）
     * 如 contract / contract_discount / contract_terminate / plan_adjust
     *
     * @return 业务类型编码
     */
    String bizType();

    /**
     * 提交前业务校验（可选，如单据存在性、金额阈值）
     */
    default void onSubmit(Long sourceId) {
    }

    /**
     * 审批通过回调（执行业务动作，如合同生效、优惠生效、计划生成、红冲链）
     *
     * @param sourceId   来源单据ID
     * @param instanceId 流程实例ID
     */
    void onPass(Long sourceId, Long instanceId);

    /**
     * 审批驳回回调（业务回滚草稿态）
     */
    default void onReject(Long sourceId, Long instanceId) {
    }

    /**
     * 撤回/终止回调
     */
    default void onCancel(Long sourceId, Long instanceId) {
    }
}