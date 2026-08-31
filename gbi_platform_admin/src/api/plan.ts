/**
 * 应收应付计划接口（对应后端 RecvPayPlanController，对齐规范 6.1）
 * 计划为全系统唯一应收应付台账：生成幂等、调账/作废超阈值走审批、自动对账、导出限流审计
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 查询入参 ------------------------------ */

export interface RecvPayPlanQueryDTO {
  pageNum: number
  pageSize: number
  direction?: number
  bizType?: string
  planStatus?: number
  redFlag?: number
  stallId?: number
  periodNo?: string
}

export interface ReconcileQueryDTO {
  pageNum: number
  pageSize: number
  stallId?: number
  periodNo?: string
}

/* ------------------------------ 视图对象 ------------------------------ */

export interface RecvPayPlanVO {
  id: number
  companyId: number
  planNo: string
  direction?: number
  directionText?: string
  bizType?: string
  bizTypeText?: string
  sourceType?: string
  sourceId?: string
  marketId?: number
  stallId?: number
  stallNumber?: string
  tenantId?: number
  tenantName?: string
  merchantId?: number
  periodNo?: string
  periodType?: number
  dueDate?: string
  originalAmount?: number
  discountAmount?: number
  adjustAmount?: number
  planAmount?: number
  paidAmount?: number
  unpaidAmount?: number
  planStatus?: number
  planStatusText?: string
  redFlag?: number
  redFlagText?: string
  origPlanId?: number
  overdueDays?: number
  flowInstanceId?: number
  discountApplyId?: number
  remark?: string
  createTime?: string
}

export interface BillPlanRelVO {
  id: number
  billType?: string
  billId?: number
  planId?: number
  splitAmount?: number
}

export interface WriteoffVO {
  id: number
  financeFlowId?: number
  planId?: number
  billType?: string
  billId?: number
  writeoffAmount?: number
  writeoffType?: number
  writeoffTypeText?: string
  remark?: string
  createByName?: string
  createTime?: string
}

export interface RecvPayPlanDetailVO {
  plan: RecvPayPlanVO
  billPlanRels: BillPlanRelVO[]
  writeoffs: WriteoffVO[]
}

export interface ReconcileDiffVO {
  planId: number
  planNo: string
  stallId?: number
  periodNo?: string
  planAmount?: number
  paidAmount?: number
  writeoffSum?: number
  unpaidAmount?: number
  computedRemaining?: number
  diff?: number
}

/* ------------------------------ 入参对象 ------------------------------ */

export interface PlanGenerateDTO {
  contractId: number
}

export interface PlanAdjustDTO {
  planId: number
  adjustAmount: number
  remark?: string
}

export interface PlanTerminateDTO {
  planId: number
  remark?: string
}

/* ------------------------------ 接口 ------------------------------ */

/** 计划分页 */
export function getRecvPayPlanPageApi(params: RecvPayPlanQueryDTO): Promise<PageResult<RecvPayPlanVO>> {
  return get<PageResult<RecvPayPlanVO>>('/finance/recvPayPlan/page', params)
}

/** 计划详情（含关联账单/核销分摊） */
export function getRecvPayPlanDetailApi(planId: number): Promise<RecvPayPlanDetailVO> {
  return get<RecvPayPlanDetailVO>('/finance/recvPayPlan/detail', { planId })
}

/** 按合同补生成计划（幂等：同来源同周期已存在跳过） */
export function generateRecvPayPlanApi(data: PlanGenerateDTO): Promise<null> {
  return post<null>('/finance/recvPayPlan/generate', data)
}

/** 计划人工调账（敏感：二次确认 + 审计，超阈值走审批） */
export function adjustRecvPayPlanApi(data: PlanAdjustDTO): Promise<null> {
  return post<null>('/finance/recvPayPlan/adjust', data)
}

/** 计划作废/终止（高危：审批 + 红冲链） */
export function terminateRecvPayPlanApi(data: PlanTerminateDTO): Promise<null> {
  return post<null>('/finance/recvPayPlan/terminate', data)
}

/** 自动对账（以计划为权威源输出差异） */
export function getReconcileDiffPageApi(params: ReconcileQueryDTO): Promise<PageResult<ReconcileDiffVO>> {
  return get<PageResult<ReconcileDiffVO>>('/finance/recvPayPlan/reconcile', params)
}

/** 计划导出 CSV（返回下载地址） */
export function exportRecvPayPlanApi(params: RecvPayPlanQueryDTO): Promise<{ url: string }> {
  return post<{ url: string }>('/finance/recvPayPlan/export', params)
}