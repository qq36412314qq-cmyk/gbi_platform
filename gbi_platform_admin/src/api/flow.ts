/**
 * 统一审批中心接口（对应后端 flow 控制器，对齐规范 6.2 /flow）
 * 待办/申请/实例：全公司共享；流程定义：集团专属配置（flow:def:*）
 */
import { get, post, request } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 查询入参 ------------------------------ */

export interface FlowQueryDTO {
  pageNum: number
  pageSize: number
  bizType?: string
  instanceStatus?: number
  sourceId?: string
}

/* ------------------------------ 审批任务（待办） ------------------------------ */

export interface FlowTaskVO {
  id: number
  instanceId: number
  instanceNo: string
  defName?: string
  bizType?: string
  title?: string
  nodeName?: string
  nodeOrder?: number
  handlerId?: number
  handlerName?: string
  taskStatus?: number
  approveResult?: number
  opinion?: string
  handleTime?: string
  applyUserName?: string
  instanceStatus?: number
  submitTime?: string
  createTime?: string
}

export interface FlowHandleDTO {
  action: 'pass' | 'reject' | 'transfer'
  taskId: number
  approveResult?: number
  opinion?: string
  transferHandlerId?: number
}

/** 我的待办分页 */
export function getMyTodoPageApi(params: FlowQueryDTO): Promise<PageResult<FlowTaskVO>> {
  return get<PageResult<FlowTaskVO>>('/flow/task/page', params)
}

/** 审批处理（pass 通过 / reject 驳回 / transfer 转交） */
export function handleTaskApi(data: FlowHandleDTO): Promise<null> {
  return post<null>('/flow/task/handle', data)
}

/** 催办 */
export function urgeTaskApi(taskId: number): Promise<null> {
  return request<null>({ url: '/flow/task/urge', method: 'post', params: { taskId } })
}

/* ------------------------------ 流程实例（我的申请） ------------------------------ */

export interface FlowInstanceVO {
  id: number
  companyId: number
  instanceNo: string
  defId?: number
  defName?: string
  bizType?: string
  sourceType?: string
  sourceId?: string
  title?: string
  applyUserId?: number
  applyUserName?: string
  instanceStatus?: number
  instanceStatusText?: string
  currentNodeName?: string
  submitTime?: string
  finishTime?: string
  createTime?: string
}

export interface FlowRecordVO {
  id: number
  instanceId: number
  nodeName?: string
  action?: string
  actionText?: string
  handlerId?: number
  handlerName?: string
  comment?: string
  createTime?: string
}

export interface FlowInstanceDetailVO {
  instance: FlowInstanceVO
  tasks: FlowTaskVO[]
  records: FlowRecordVO[]
}

/** 我的申请分页 */
export function getMyApplyPageApi(params: FlowQueryDTO): Promise<PageResult<FlowInstanceVO>> {
  return get<PageResult<FlowInstanceVO>>('/flow/apply/page', params)
}

/** 撤回申请 */
export function revokeApplyApi(instanceId: number): Promise<null> {
  return request<null>({ url: '/flow/apply/revoke', method: 'post', params: { instanceId } })
}

/* ------------------------------ 流程定义（集团专属） ------------------------------ */

export interface FlowDefVO {
  id: number
  companyId: number
  defName: string
  defCode: string
  bizType?: string
  nodeConfigJson?: string
  status?: number
  remark?: string
  createTime?: string
}

export interface FlowDefDTO {
  id?: number
  defName: string
  defCode: string
  bizType: string
  nodeConfigJson?: string
  status?: number
  remark?: string
}

/** 流程定义分页 */
export function getFlowDefPageApi(params: FlowQueryDTO): Promise<PageResult<FlowDefVO>> {
  return get<PageResult<FlowDefVO>>('/flow/def/page', params)
}

/** 新增流程定义 */
export function addFlowDefApi(data: FlowDefDTO): Promise<null> {
  return post<null>('/flow/def/add', data)
}

/** 编辑流程定义 */
export function updateFlowDefApi(data: FlowDefDTO): Promise<null> {
  return post<null>('/flow/def/update', data)
}

/** 删除流程定义（有实例禁止删除，逻辑删除） */
export function deleteFlowDefApi(id: number): Promise<null> {
  return request<null>({ url: '/flow/def/delete', method: 'post', params: { id } })
}

/* ------------------------------ 流程实例（全量查询） ------------------------------ */

/** 流程实例分页 */
export function getFlowInstancePageApi(params: FlowQueryDTO): Promise<PageResult<FlowInstanceVO>> {
  return get<PageResult<FlowInstanceVO>>('/flow/instance/page', params)
}

/** 流程实例详情（含审批任务 + 流转轨迹） */
export function getFlowInstanceDetailApi(instanceId: number): Promise<FlowInstanceDetailVO> {
  return get<FlowInstanceDetailVO>('/flow/instance/detail', { instanceId })
}