/**
 * 财务管理模块接口（对应后端 finance 控制器）
 * 财务流水（双视图）+ 营收汇总 + 冲红/作废/打印
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 财务流水 biz_finance_flow ------------------------------ */

export interface FinanceFlowQueryDTO {
  pageNum: number
  pageSize: number
  businessType?: string
  flowType?: number
  companyId?: number
  startTime?: string
  endTime?: string
  startTimeValue?: string
  endTimeValue?: string
}

export interface FinanceFlowVO {
  id: number
  companyId: number
  businessType: string
  businessTypeText?: string
  billId?: string
  merchantId?: number
  stallId?: number
  // 物业快照
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  categoryName?: string
  merchantName?: string
  // 缴费人快照
  payerName?: string
  payerPhone?: string
  payerCompanyName?: string
  payerType?: number
  payerTypeText?: string
  // 合同快照
  contractNo?: string
  contractId?: number
  // 金额
  originalAmount: number
  discountAmount: number
  realAmount: number
  // 渠道/方向
  payType?: number
  payTypeText?: string
  flowType: number
  flowTypeText?: string
  // 冲红/作废
  flowStatus?: number
  flowStatusText?: string
  redFlushFlowId?: number
  voidReason?: string
  // 其他
  tradeNo?: string
  flowNo?: string
  remark?: string
  createBy: number
  createTime?: string
}

export interface FinanceSummaryQueryDTO {
  businessType?: string
  companyId?: number
  startTime?: string
  endTime?: string
}

export interface FinanceSummaryVO {
  companyId: number
  businessType: string
  businessTypeText?: string
  flowType: number
  flowTypeText?: string
  flowCount: number
  totalAmount: number
}

export interface PayOrderItemVO {
  id: number
  payBillId: number
  billId: number
  bizType: string
  bizTypeText?: string
  ruleName: string
  feeItemType: string
  billMonth: string
  amount: number
  discountAmount: number
  paidAmount: number
  unpaidAmount: number
  createTime?: string
}

export interface PayOrderVO {
  id: number
  payBillNo: string
  companyId: number
  sourceType: string
  sourceTypeText?: string
  sourceId?: number
  stallId?: number
  stallNumber?: string
  stallName?: string
  merchantId?: number
  merchantName?: string
  totalAmount: number
  paidAmount: number
  unpaidAmount: number
  payStatus?: number
  payStatusText?: string
  remark?: string
  payTime?: string
  createTime?: string
}

export interface PayOrderQueryDTO {
  pageNum: number
  pageSize: number
  payBillNo?: string
  sourceType?: string
  payStatus?: number
  companyId?: number
  startTime?: string
  endTime?: string
}
/** 财务流水分页 */
export function getFinanceFlowPageApi(params: FinanceFlowQueryDTO): Promise<PageResult<FinanceFlowVO>> {
  return get<PageResult<FinanceFlowVO>>('/finance/flow/page', params)
}

/** 财务流水导出 CSV */
export function exportFinanceFlowApi(data: FinanceFlowQueryDTO): Promise<string> {
  return post<string>('/finance/flow/export', data)
}

/** 营收汇总 */
export function getFinanceSummaryApi(params: FinanceSummaryQueryDTO): Promise<FinanceSummaryVO[]> {
  return get<FinanceSummaryVO[]>('/finance/summary', params)
}

/** 冲红申请 */
export function redFlushFlowApi(data: { flowId: number; reason: string }): Promise<void> {
  return post<void>('/finance/flow/redFlush', data)
}

/** 冲红审批通过 */
export function approveRedFlushApi(data: { flowId: number; redFlushFlowId: number }): Promise<void> {
  return post<void>('/finance/flow/approveRedFlush', data)
}

/** 作废流水 */
export function voidFlowApi(data: { flowId: number; reason: string }): Promise<void> {
  return post<void>('/finance/flow/void', data)
}

/** 打印收据 HTML */
export function getPrintHtmlApi(flowId: number): Promise<string> {
  return get<string>(`/finance/flow/${flowId}/print`)
}
/** 获取流水关联的缴费单明细 */
export function getFinanceFlowItemsApi(flowId: number): Promise<PayOrderItemVO[]> {
  return get<PayOrderItemVO[]>(`/finance/flow/${flowId}/items`)
}

/** 按缴费单ID查询明细 */
export function getFinancePayOrderItemsApi(payOrderId: number): Promise<PayOrderItemVO[]> {
  return get<PayOrderItemVO[]>(`/finance/payOrder/${payOrderId}/items`)
}

/** 缴费单分页 */
export function getFinancePayOrderPageApi(params: PayOrderQueryDTO): Promise<PageResult<PayOrderVO>> {
  return get<PageResult<PayOrderVO>>("/finance/payOrder/page", params)
}

/** 作废缴费单 */
export function voidPayOrderApi(data: { payOrderId: number; reason: string }): Promise<void> {
  return post<void>('/finance/payOrder/void', data)
}

/** 冲红缴费单 */
export function redFlushPayOrderApi(data: { payOrderId: number; reason: string }): Promise<void> {
  return post<void>('/finance/payOrder/redFlush', data)
}

/** 缴费单打印收据 HTML */
export function getPayOrderPrintHtmlApi(payOrderId: number): Promise<string> {
  return get<string>(`/finance/payOrder/${payOrderId}/print`)
}

/** 缴费单打印数据 */
export interface PayOrderPrintVO {
  payOrder: PayOrderVO
  items: PayOrderItemVO[]
}

export function getPayOrderPrintDataApi(payOrderId: number): Promise<PayOrderPrintVO> {
  return get<PayOrderPrintVO>(`/finance/payOrder/${payOrderId}/printData`)
}
