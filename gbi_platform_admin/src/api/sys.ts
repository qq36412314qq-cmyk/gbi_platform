/**
 * 系统配置中台接口：参数配置 / UI主题 / 审计日志 / 权限二级复核
 * 与后端 sys 模块 Controller 一一对应
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 参数配置 sys_config ------------------------------ */
export interface ConfigDTO {
  id?: number
  companyId?: number
  configKey: string
  configValue: string
  configName: string
  remark?: string
}

export interface ConfigVO extends ConfigDTO {
  id: number
  createTime?: string
}

/** 参数分页 */
export function getConfigPageApi(params: {
  pageNum: number
  pageSize: number
  configName?: string
  configKey?: string
}): Promise<PageResult<ConfigVO>> {
  return get<PageResult<ConfigVO>>('/sys/config/page', params)
}

/** 新增参数 */
export function addConfigApi(data: ConfigDTO): Promise<null> {
  return post<null>('/sys/config/add', data)
}

/** 编辑参数 */
export function updateConfigApi(data: ConfigDTO): Promise<null> {
  return post<null>('/sys/config/update', data)
}

/** 删除参数 */
export function deleteConfigApi(id: number): Promise<null> {
  return post<null>('/sys/config/delete', { id })
}

/** 批量读取集团参数值 */
export function getConfigValuesApi(keys: string[]): Promise<Record<string, string>> {
  return get<Record<string, string>>('/sys/config/values', { keys })
}

/* ------------------------------ UI主题 sys_ui_theme ------------------------------ */

export interface UiThemeDTO {
  id?: number
  companyId?: number
  primaryColor: string
  layoutMode: 'side' | 'top'
  cardRadius: number
  darkMode: 0 | 1
}

/** 查询当前公司主题 */
export function getThemeApi(): Promise<UiThemeDTO> {
  return get<UiThemeDTO>('/sys/theme/get')
}

/** 保存主题 */
export function saveThemeApi(data: UiThemeDTO): Promise<null> {
  return post<null>('/sys/theme/save', data)
}

/* ------------------------------ 审计日志 sys_audit_log ------------------------------ */

export interface AuditLogQueryDTO {
  pageNum: number
  pageSize: number
  operModule?: string
  operType?: string
  operUserName?: string
  startTime?: string
  endTime?: string
}

export interface AuditLogVO {
  id: number
  companyId: number
  operUserId: number
  operUserName: string
  operIp?: string
  operModule: string
  operType: string
  bizId?: string
  beforeJson?: string
  afterJson?: string
  auditOperId?: number
  createTime: string
}

/** 审计日志分页（只读） */
export function getAuditLogPageApi(params: AuditLogQueryDTO): Promise<PageResult<AuditLogVO>> {
  return get<PageResult<AuditLogVO>>('/sys/audit/page', params)
}

/** 审计日志导出 */
export function exportAuditLogApi(params: AuditLogQueryDTO): Promise<null> {
  return post<null>('/sys/audit/export', params)
}

/* ------------------------------ 权限二级复核 sys_permission_audit ------------------------------ */

export interface PermissionAuditDTO {
  id?: number
  companyId?: number
  targetUserId: number
  applyUserId?: number
  auditUserId?: number
  permissionList: string
  applyReason?: string
  auditStatus: number
  auditComment?: string
  applyTime?: string
  auditTime?: string
}

export interface PermissionAuditVO extends PermissionAuditDTO {
  id: number
  applyUserName?: string
  targetUserName?: string
  /** 权限类型：ROLE / USER / INHERIT */
  permissionType?: string
  /** 变更对象展示名 */
  targetName?: string
  /** 变更说明 */
  changeDesc?: string
  /** 复核人姓名 */
  auditUserName?: string
}

/** 权限复核分页 */
export function getPermissionAuditPageApi(params: {
  pageNum: number
  pageSize: number
  auditStatus?: number
  applyUserName?: string
  permissionType?: string
}): Promise<PageResult<PermissionAuditVO>> {
  return get<PageResult<PermissionAuditVO>>('/sys/permissionAudit/page', params)
}

/** 复核处理：通过 / 驳回 */
export function auditPermissionApi(id: number, auditStatus: number, auditComment?: string): Promise<null> {
  return post<null>('/sys/permissionAudit/audit', { id, auditStatus, auditComment })
}