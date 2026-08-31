/**
 * 财务管理-自定义收费模块接口（对应后端 finance 控制器：feeItem / feeRule）
 * 收费类型管理（租金/物业费/水费/电费/押金/其他等）+ 收费规则管理（调用收费类型）
 * 权限标识 fee:item:* / fee:rule:*（子公司可配置，集团账号仅只读）
 */
import { get, post, request } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 收费类型 biz_fee_item ------------------------------ */

export interface FeeItemQueryDTO {
  pageNum?: number
  pageSize?: number
  feeItemName?: string
}

export interface FeeItemDTO {
  id?: number
  feeItemName: string
  /** 收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他 */
  categoryType: number
  calcUnit?: string
  remark?: string
}

export interface FeeItemVO {
  id: number
  feeItemName: string
  /** 收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他 */
  categoryType?: number
  categoryTypeText?: string
  calcUnit?: string
  remark?: string
  createTime?: string
}

/** 收费类型全量列表（供收费规则下拉选择，company_id 自动隔离） */
export function getFeeItemListApi(params?: FeeItemQueryDTO): Promise<FeeItemVO[]> {
  return get<FeeItemVO[]>('/finance/feeItem/list', params)
}

/** 收费类型分页 */
export function getFeeItemPageApi(params: FeeItemQueryDTO): Promise<PageResult<FeeItemVO>> {
  return get<PageResult<FeeItemVO>>('/finance/feeItem/page', params)
}

/** 新增收费类型 */
export function addFeeItemApi(data: FeeItemDTO): Promise<null> {
  return post<null>('/finance/feeItem/add', data)
}

/** 编辑收费类型 */
export function updateFeeItemApi(data: FeeItemDTO): Promise<null> {
  return post<null>('/finance/feeItem/update', data)
}

/** 删除收费类型（被收费规则引用禁止删除；id 走 query 参数对齐后端 @RequestParam） */
export function deleteFeeItemApi(id: number): Promise<null> {
  return request<null>({ url: '/finance/feeItem/delete', method: 'post', params: { id } })
}

/* ------------------------------ 收费规则 biz_fee_rule ------------------------------ */

export interface FeeRuleQueryDTO {
  pageNum: number
  pageSize: number
  ruleName?: string
  feeItemId?: number
  status?: number
}

export interface FeeRuleDTO {
  id?: number
  ruleName: string
  feeItemId: number
  calcMode: number
  price: number
  periodType: number
  overdueRate?: number
  status: number
  remark?: string
}

export interface FeeRuleVO extends FeeRuleDTO {
  id: number
  feeItemName?: string
  calcModeText?: string
  periodTypeText?: string
  createTime?: string
}

/** 收费规则分页 */
export function getFeeRulePageApi(params: FeeRuleQueryDTO): Promise<PageResult<FeeRuleVO>> {
  return get<PageResult<FeeRuleVO>>('/finance/feeRule/page', params)
}

/** 新增收费规则 */
export function addFeeRuleApi(data: FeeRuleDTO): Promise<null> {
  return post<null>('/finance/feeRule/add', data)
}

/** 编辑收费规则 */
export function updateFeeRuleApi(data: FeeRuleDTO): Promise<null> {
  return post<null>('/finance/feeRule/update', data)
}

/** 删除收费规则（id 走 query 参数对齐后端 @RequestParam） */
export function deleteFeeRuleApi(id: number): Promise<null> {
  return request<null>({ url: '/finance/feeRule/delete', method: 'post', params: { id } })
}