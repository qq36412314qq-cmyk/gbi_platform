/**
 * 优惠管理接口（对应后端 DiscountController，对齐规范 6.3 /finance/discount）
 * 优惠策略：集团模板 + 子公司自建；优惠申请：随合同提交，超集团阈值自动发起审批
 */
import { get, post, request } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 优惠策略 ------------------------------ */

export interface DiscountPolicyQueryDTO {
  pageNum: number
  pageSize: number
  policyName?: string
  discountType?: number
  status?: number
}

export interface DiscountPolicyVO {
  id: number
  companyId: number
  policyName: string
  discountType?: number
  discountTypeText?: string
  waiveMonths?: number
  discountRate?: number
  deductAmount?: number
  scopeType?: number
  startTime?: string
  endTime?: string
  status?: number
  statusText?: string
  remark?: string
}

export interface DiscountPolicyDTO {
  id?: number
  policyName: string
  discountType: number
  waiveMonths?: number
  discountRate?: number
  deductAmount?: number
  scopeType?: number
  startTime?: string
  endTime?: string
  status?: number
  remark?: string
}

/** 优惠策略分页 */
export function getDiscountPolicyPageApi(params: DiscountPolicyQueryDTO): Promise<PageResult<DiscountPolicyVO>> {
  return get<PageResult<DiscountPolicyVO>>('/finance/discount/policy/page', params)
}

/** 新增优惠策略 */
export function addDiscountPolicyApi(data: DiscountPolicyDTO): Promise<null> {
  return post<null>('/finance/discount/policy/add', data)
}

/** 编辑优惠策略 */
export function updateDiscountPolicyApi(data: DiscountPolicyDTO): Promise<null> {
  return post<null>('/finance/discount/policy/update', data)
}

/** 删除优惠策略（被申请引用禁止删除） */
export function deleteDiscountPolicyApi(id: number): Promise<null> {
  return request<null>({ url: '/finance/discount/policy/delete', method: 'post', params: { id } })
}

/* ------------------------------ 优惠申请 ------------------------------ */

export interface DiscountApplyQueryDTO {
  pageNum: number
  pageSize: number
  contractNo?: string
  applyStatus?: number
  needAudit?: number
}

export interface DiscountApplyVO {
  id: number
  companyId: number
  applyNo: string
  policyId?: number
  policyName?: string
  contractNo?: string
  stallId?: number
  tenantId?: number
  waiveMonths?: number
  discountRate?: number
  deductAmount?: number
  discountAmount?: number
  needAudit?: number
  needAuditText?: string
  flowInstanceId?: number
  applyStatus?: number
  applyStatusText?: string
  applyUserName?: string
  remark?: string
  auditTime?: string
  createTime?: string
}

/** 优惠申请分页 */
export function getDiscountApplyPageApi(params: DiscountApplyQueryDTO): Promise<PageResult<DiscountApplyVO>> {
  return get<PageResult<DiscountApplyVO>>('/finance/discount/apply/page', params)
}

/** 优惠申请详情 */
export function getDiscountApplyDetailApi(id: number): Promise<DiscountApplyVO> {
  return get<DiscountApplyVO>('/finance/discount/apply/detail', { id })
}

/** 撤销优惠申请（仅草稿/审批中） */
export function cancelDiscountApplyApi(id: number): Promise<null> {
  return request<null>({ url: '/finance/discount/apply/cancel', method: 'post', params: { id } })
}