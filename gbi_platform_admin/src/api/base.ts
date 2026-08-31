/**
 * 公共接口：登录、退出、当前用户、字典、文件上传
 * 与后端 base 模块 Controller 一一对应
 */
import { get, post, upload } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/** 登录入参 */
export interface LoginDTO {
  username: string
  password: string
}

/** 登录返回 */
export interface LoginVO {
  token: string
}

/** 当前用户信息 */
export interface UserInfoVO {
  id: number
  companyId: number
  username: string
  realName: string
  phone?: string
  avatar?: string
  userType?: number
  permissions?: string[]
}

/** 登录 */
export function loginApi(data: LoginDTO): Promise<LoginVO> {
  console.log(data)
  return post<LoginVO>('/base/login', data)
}

/** 退出登录 */
export function logoutApi(): Promise<null> {
  return post<null>('/base/logout')
}

/** 获取当前登录用户信息与权限集合 */
export function getInfoApi(): Promise<UserInfoVO> {
  return get<UserInfoVO>('/base/info')
}

/** 按字典编码查询字典数据 */
export function getDictDataApi(dictCode: string): Promise<DictItemVO[]> {
  return get<DictItemVO[]>('/base/dict/data', { dictCode })
}

/** 字典数据项 */
export interface DictItemVO {
  id: number
  dictTypeId: number
  dictValue: string
  dictKey: string
  sortOrder: number
  status: number
}

/** 文件上传（OSS） */
export function uploadFileApi(file: File): Promise<{ url: string }> {
  return upload<{ url: string }>('/base/upload', file)
}

/* ------------------------------ 字典管理 sys_dict_type / sys_dict_data ------------------------------ */

/** 字典类型 */
export interface DictTypeDTO {
  id?: number
  dictCode: string
  dictName: string
  status: number
  remark?: string
}

export interface DictTypeVO extends DictTypeDTO {
  id: number
  createTime?: string
}

/** 字典数据项 */
export interface DictDataDTO {
  id?: number
  dictTypeId: number
  dictValue: string
  dictKey: string
  sortOrder: number
  status: number
}

/** 字典类型分页 */
export function getDictTypePageApi(params: {
  pageNum: number
  pageSize: number
  dictName?: string
  dictCode?: string
}): Promise<PageResult<DictTypeVO>> {
  return get<PageResult<DictTypeVO>>('/base/dictType/page', params)
}

/** 新增字典类型 */
export function addDictTypeApi(data: DictTypeDTO): Promise<null> {
  return post<null>('/base/dictType/add', data)
}

/** 编辑字典类型 */
export function updateDictTypeApi(data: DictTypeDTO): Promise<null> {
  return post<null>('/base/dictType/update', data)
}

/** 删除字典类型 */
export function deleteDictTypeApi(id: number): Promise<null> {
  return post<null>('/base/dictType/delete', { id })
}

/** 按类型ID查询字典数据 */
export function getDictDataListApi(dictTypeId: number): Promise<DictItemVO[]> {
  return get<DictItemVO[]>('/base/dictData/list', { dictTypeId })
}

/** 新增字典数据 */
export function addDictDataApi(data: DictDataDTO): Promise<null> {
  return post<null>('/base/dictData/add', data)
}

/** 编辑字典数据 */
export function updateDictDataApi(data: DictDataDTO): Promise<null> {
  return post<null>('/base/dictData/update', data)
}

/** 删除字典数据 */
export function deleteDictDataApi(id: number): Promise<null> {
  return post<null>('/base/dictData/delete', { id })
}