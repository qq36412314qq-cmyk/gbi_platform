package com.gbi.platform.service;

import com.gbi.platform.dto.FinanceFlowQueryDTO;
import com.gbi.platform.dto.FinanceSummaryQueryDTO;
import com.gbi.platform.dto.PayOrderQueryDTO;
import com.gbi.platform.vo.FinanceSummaryVO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.FinanceFlowVO;
import com.gbi.platform.vo.PayOrderItemVO;
import com.gbi.platform.vo.PayOrderVO;

import java.util.List;

/**
 * 财务流水服务：全域统一资金台账（子公司视图自动过滤，集团视图全量）
 * 快照字段直接读本表；支持冲红/作废/打印
 *
 * @author gbi
 */
public interface FinanceService {

    /**
     * 流水分页（biz_finance_flow 由多租户拦截器自动双视图隔离）
     */
    PageVO<FinanceFlowVO> page(FinanceFlowQueryDTO dto);

    /**
     * 营收汇总：按公司/业务类型/收支方向聚合
     */
    List<FinanceSummaryVO> summary(FinanceSummaryQueryDTO dto);

    /**
     * 流水导出 CSV（高危操作，写入审计日志）
     *
     * @return 导出文件访问地址
     */
    String exportCsv(FinanceFlowQueryDTO dto);

    /**
     * 冲红申请：标记原流水为冲红中，创建反向流水待审批
     *
     * @param flowId  原流水ID
     * @param reason  冲红原因
     */
    void redFlush(Long flowId, String reason);

    /**
     * 冲红审批通过：激活原流水反向状态，激活反向流水
     *
     * @param flowId         原流水ID
     * @param redFlushFlowId 反向流水ID（审批通过时由引擎传入）
     */
    void approveRedFlush(Long flowId, Long redFlushFlowId);

    /**
     * 冲红审批驳回/撤回：恢复原流水状态，删除冲红反向流水
     *
     * @param flowId 原流水ID
     */
    void rejectRedFlush(Long flowId);

    /**
     * 作废流水：仅允许草稿/未关联计划的流水
     *
     * @param flowId  流水ID
     * @param reason  作废原因
     */
    void voidFlow(Long flowId, String reason);

    /**
     * 生成打印收据 HTML
     *
     * @param flowId 流水ID
     * @return HTML 字符串
     */
    String getPrintHtml(Long flowId);

    /**
     * 获取流水关联的缴费单明细列表
     *
     * @param flowId 流水ID（通过 bill_id 关联 finance_pay_order_item.bill_id）
     * @return 缴费单明细列表
     */
    List<PayOrderItemVO> getPayOrderItems(Long flowId);

    /**
     * 缴费单分页列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageVO<PayOrderVO> pagePayOrders(PayOrderQueryDTO dto);

    /**
     * 按缴费单ID查询明细列表
     */
    List<PayOrderItemVO> getPayOrderItemsById(Long payOrderId);
}
