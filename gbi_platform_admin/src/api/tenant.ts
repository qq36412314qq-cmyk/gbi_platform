/**
 * 物业管理-租户管理接口（对应后端 TenantController /property/tenant）
 * 敏感字段（手机/身份证/银行账号）后端 VO 已脱敏返回
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface TenantQueryDTO {
  pageNum: number
  pageSize: number
  tenantName?: string
  tenantType?: number
  contactPhone?: string
  status?: number
}

export interface TenantAddDTO {
  tenantName: string
  /** 租户类型 1个体工商户 2企业 3个人 */
  tenantType: number
  contactPerson?: string
  contactPhone?: string
  idCardNo?: string
  socialCreditCode?: string
  bankAccount?: string
  address?: string
  miniOpenid?: string
  status?: number
  remark?: string
}

export interface TenantUpdateDTO extends TenantAddDTO {
  id: number
}

export interface TenantVO {
  id: number
  companyId: number
  tenantName: string
  tenantType: number
  tenantTypeText?: string
  contactPerson?: string
  contactPhone?: string
  idCardNo?: string
  socialCreditCode?: string
  bankAccount?: string
  address?: string
  status: number
  remark?: string
  createTime?: string
}

/** 租户分页 */
export function getTenantPageApi(params: TenantQueryDTO): Promise<PageResult<TenantVO>> {
  return get<PageResult<TenantVO>>('/property/tenant/page', params)
}

/** 新增租户 */
export function addTenantApi(data: TenantAddDTO): Promise<null> {
  return post<null>('/property/tenant/add', data)
}

/** 编辑租户 */
export function updateTenantApi(data: TenantUpdateDTO): Promise<null> {
  return post<null>('/property/tenant/update', data)
}

/** 删除租户（有生效合同禁止删除，逻辑删除） */
export function deleteTenantApi(id: number): Promise<null> {
  return post<null>('/property/tenant/delete', { id })
}