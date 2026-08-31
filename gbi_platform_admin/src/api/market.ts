/**
 * 物业管理-市场管理接口（对应后端 MarketController /property/market）
 * 园区/商圈维度，摊位与市场地图统一关联本表
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface MarketQueryDTO {
  pageNum: number
  pageSize: number
  marketName?: string
  status?: number
}

export interface MarketDTO {
  id?: number
  marketName: string
  marketAddress?: string
  contactPerson?: string
  contactPhone?: string
  status?: number
  remark?: string
}

export interface MarketVO {
  id: number
  companyId: number
  marketName: string
  marketAddress?: string
  contactPerson?: string
  contactPhone?: string
  status: number
  remark?: string
  createTime?: string
}

/** 市场分页 */
export function getMarketPageApi(params: MarketQueryDTO): Promise<PageResult<MarketVO>> {
  return get<PageResult<MarketVO>>('/property/market/page', params)
}

/** 市场全量列表（下拉选择用，仅启用） */
export function getMarketListApi(): Promise<MarketVO[]> {
  return get<MarketVO[]>('/property/market/list')
}

/** 新增市场 */
export function addMarketApi(data: MarketDTO): Promise<null> {
  return post<null>('/property/market/add', data)
}

/** 编辑市场 */
export function updateMarketApi(data: MarketDTO): Promise<null> {
  return post<null>('/property/market/update', data)
}

/** 删除市场（市场下有摊位禁止删除，逻辑删除） */
export function deleteMarketApi(id: number): Promise<null> {
  return post<null>('/property/market/delete', { id })
}