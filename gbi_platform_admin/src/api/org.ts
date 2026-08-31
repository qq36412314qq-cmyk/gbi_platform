/**
 * 组织中台接口：组织 / 用户 / 角色 / 菜单（RBAC 功能权限）
 * 与后端 org 模块 Controller 一一对应
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 组织管理 sys_org ------------------------------ */

export interface OrgDTO {
  id?: number
  companyId?: number
  parentId: number
  orgName: string
  orgType: number
  sortOrder: number
  status: number
}

export interface OrgVO extends OrgDTO {
  id: number
  createTime?: string
  children?: OrgVO[]
}

/** 组织树查询 */
export function getOrgTreeApi(): Promise<OrgVO[]> {
  return get<OrgVO[]>('/org/tree')
}

/** 新增组织 */
export function addOrgApi(data: OrgDTO): Promise<null> {
  return post<null>('/org/add', data)
}

/** 编辑组织 */
export function updateOrgApi(data: OrgDTO): Promise<null> {
  return post<null>('/org/update', data)
}

/** 删除组织（逻辑删除） */
export function deleteOrgApi(id: number): Promise<null> {
  return post<null>('/org/delete', { id })
}

/* ------------------------------ 用户管理 sys_user ------------------------------ */

export interface UserQueryDTO {
  pageNum: number
  pageSize: number
  username?: string
  realName?: string
  phone?: string
  status?: number
  companyId?: number
}

export interface UserDTO {
  id?: number
  companyId: number
  username: string
  password?: string
  realName: string
  phone?: string
  email?: string
  avatar?: string
  status: number
  roleIds?: number[]
}

export interface UserVO {
  id: number
  companyId: number
  username: string
  realName: string
  phone?: string
  email?: string
  avatar?: string
  status: number
  roleNames?: string[]
  createTime?: string
}

/** 用户分页查询 */
export function getUserPageApi(params: UserQueryDTO): Promise<PageResult<UserVO>> {
  return get<PageResult<UserVO>>('/org/user/page', params)
}

/** 新增用户 */
export function addUserApi(data: UserDTO): Promise<null> {
  return post<null>('/org/user/add', data)
}

/** 编辑用户 */
export function updateUserApi(data: UserDTO): Promise<null> {
  return post<null>('/org/user/update', data)
}

/** 删除用户（逻辑删除） */
export function deleteUserApi(id: number): Promise<null> {
  return post<null>('/org/user/delete', { id })
}

/** 重置密码 */
export function resetUserPwdApi(id: number, password: string): Promise<null> {
  return post<null>('/org/user/resetPwd', { id, password })
}

/** 启用/禁用账号 */
export function changeUserStatusApi(id: number, status: number): Promise<null> {
  return post<null>('/org/user/changeStatus', { id, status })
}

/* ------------------------------ 角色管理 sys_role ------------------------------ */

export interface RoleDTO {
  id?: number
  companyId: number
  roleName: string
  roleCode: string
  remark?: string
}

export interface RoleVO extends RoleDTO {
  id: number
  createTime?: string
}

/** 角色列表（不分页，供下拉/分配） */
export function getRoleListApi(companyId?: number): Promise<RoleVO[]> {
  return get<RoleVO[]>('/org/role/list', { companyId })
}

/** 角色分页 */
export function getRolePageApi(params: { pageNum: number; pageSize: number; roleName?: string }): Promise<PageResult<RoleVO>> {
  return get<PageResult<RoleVO>>('/org/role/page', params)
}

/** 新增角色 */
export function addRoleApi(data: RoleDTO): Promise<null> {
  return post<null>('/org/role/add', data)
}

/** 编辑角色 */
export function updateRoleApi(data: RoleDTO): Promise<null> {
  return post<null>('/org/role/update', data)
}

/** 删除角色 */
export function deleteRoleApi(id: number): Promise<null> {
  return post<null>('/org/role/delete', { id })
}

/** 查询角色已分配菜单ID */
export function getRoleMenusApi(roleId: number): Promise<number[]> {
  return get<number[]>('/org/role/menus', { roleId })
}

/** 保存角色菜单授权 */
export function saveRoleMenusApi(roleId: number, menuIds: number[]): Promise<null> {
  return post<null>('/org/role/menus/save', { roleId, menuIds })
}

/* ------------------------------ 菜单管理 sys_menu ------------------------------ */

export interface MenuDTO {
  id?: number
  parentId: number
  menuName: string
  permission?: string
  path?: string
  icon?: string
  sortOrder: number
  menuType: number
  visible: number
}

export interface MenuVO extends MenuDTO {
  id: number
  children?: MenuVO[]
}

/** 菜单树查询 */
export function getMenuTreeApi(): Promise<MenuVO[]> {
  return get<MenuVO[]>('/org/menu/tree')
}

/** 新增菜单 */
export function addMenuApi(data: MenuDTO): Promise<null> {
  return post<null>('/org/menu/add', data)
}

/** 编辑菜单 */
export function updateMenuApi(data: MenuDTO): Promise<null> {
  return post<null>('/org/menu/update', data)
}

/** 删除菜单 */
export function deleteMenuApi(id: number): Promise<null> {
  return post<null>('/org/menu/delete', { id })
}