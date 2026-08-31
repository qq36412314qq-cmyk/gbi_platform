/**
 * 物业费账单管理接口
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface PropertyFeeBillQueryDTO {
  pageNum: number
  pageSize: number
  billMonth?: string
  stallId?: number
  marketId?: number
  payStatus?: number
  calcMode?: number
}

export interface PropertyFeeBillVO {
  id: number
  companyId: number
  stallId: number
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  categoryName?: string
  merchantId?: number
  billMonth: string
  ruleId?: number
  ruleName?: string
  feeItemId?: number
  calcMode?: number
  calcModeText?: string
  periodType?: number
  periodTypeText?: string
  usage?: number
  unitPrice?: number
  periodFactor?: number
  amount: number
  payStatus: number
  payStatusText?: string
  payTime?: string
  planId?: number
  createTime?: string
}

export interface PropertyFeeBillGenerateDTO {
  billMonth: string
  marketId?: number
  stallId?: number
}


export interface PropertyFeeBillPreviewVO {
  stallId?: number
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  calcMode?: number
  calcModeText?: string
  periodType?: number
  periodTypeText?: string
  usage?: number
  unitPrice?: number
  periodFactor?: number
  amount?: number
  hasExisting?: boolean
}

/** 预览物业费账单金额（不创建账单） */
export function previewPropertyFeeBillApi(data: PropertyFeeBillGenerateDTO): Promise<PropertyFeeBillPreviewVO> {
  return post<PropertyFeeBillPreviewVO>('/property/feeBill/preview', data)
}
/** 物业费账单分页 */
export function getPropertyFeeBillPageApi(params: PropertyFeeBillQueryDTO): Promise<PageResult<PropertyFeeBillVO>> {
  return get<PageResult<PropertyFeeBillVO>>('/property/feeBill/page', params)
}

/** 物业费账单详情 */
export function getPropertyFeeBillDetailApi(id: number): Promise<PropertyFeeBillVO> {
  return get<PropertyFeeBillVO>('/property/feeBill/detail', { id })
}

/** 批量生成物业费账单 */
export function generatePropertyFeeBillApi(data: PropertyFeeBillGenerateDTO): Promise<number> {
  return post<number>('/property/feeBill/generate', data)
}

/** 单条生成物业费账单 */
export function generatePropertyFeeBillSingleApi(data: PropertyFeeBillGenerateDTO): Promise<number> {
  return post<number>('/property/feeBill/generateSingle', data)
}

/* ------------------------------ 未支付订单聚合 ------------------------------ */

export interface UnpaidBillQueryDTO {
  pageNum: number
  pageSize: number
  billMonth?: string
  stallId?: number
  marketId?: number
}

export interface UnpaidBillVO {
  id: number
  companyId: number
  sourceBillId?: number
  businessType: string
  businessTypeText?: string
  billMonth: string
  stallId: number
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  categoryName?: string
  amount: number
  payStatus: number
  payStatusText?: string
  payTime?: string
  createTime?: string
}

/** 未支付订单分页（聚合物业费 + 水电费） */
export function getUnpaidBillPageApi(params: UnpaidBillQueryDTO): Promise<PageResult<UnpaidBillVO>> {
  return get<PageResult<UnpaidBillVO>>('/property/unpaidBill/page', params)
}
/** 同步已有物业费记录到未支付订单 */
export function syncPropertyFeeBillApi(id: number): Promise<string> {
  return post<null>('/property/feeBill/sync/' + id, null)
}

/** 物业费线下缴费 */
export interface PropertyFeePayDTO {
  billId: number
  payType: number
  requestId: string
  remark?: string
}

export function payPropertyFeeApi(data: PropertyFeePayDTO): Promise<null> {
  return post<null>('/property/feePay/pay', data)
}
/** 物业费账单详情（别名，供缴费页调用） */
export const getFeeBillDetailApi = getPropertyFeeBillDetailApi
/** 统一缴费（物业费/水电费共用入口） */
export interface UnifiedPayDTO {
  billId: number
  billType: string
  payType: number
  requestId: string
  remark?: string
}

export function unifiedPayApi(data: UnifiedPayDTO): Promise<null> {
  return post<null>('/property/unifiedPay/pay', data)
}