/**
 * 物业管理-租赁管理接口（对应后端 LeaseCategory/Stall/Contract 三个控制器）
 * 分类管理 / 摊位管理 / 合同管理
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 租赁分类 stall_category ------------------------------ */

export interface CategoryQueryDTO {
  pageNum: number
  pageSize: number
  categoryName?: string
  status?: number
}

export interface CategoryDTO {
  id?: number
  categoryName: string
  sortOrder?: number
  status?: number
  remark?: string
}

export interface CategoryVO {
  id: number
  categoryName: string
  sortOrder: number
  status: number
  remark?: string
  createTime?: string
}

/** 分类全量列表（下拉选择用，启用优先） */
export function getCategoryListApi(): Promise<CategoryVO[]> {
  return get<CategoryVO[]>('/property/lease/category/list')
}

/** 分类分页 */
export function getCategoryPageApi(params: CategoryQueryDTO): Promise<PageResult<CategoryVO>> {
  return get<PageResult<CategoryVO>>('/property/lease/category/page', params)
}

/** 新增分类 */
export function addCategoryApi(data: CategoryDTO): Promise<null> {
  return post<null>('/property/lease/category/add', data)
}

/** 编辑分类 */
export function updateCategoryApi(data: CategoryDTO): Promise<null> {
  return post<null>('/property/lease/category/update', data)
}

/** 删除分类（被摊位引用禁止删除，逻辑删除） */
export function deleteCategoryApi(id: number): Promise<null> {
  return post<null>('/property/lease/category/delete', { id })
}

/* ------------------------------ 租赁摊位 stall_info ------------------------------ */

export interface StallQueryDTO {
  pageNum: number
  pageSize: number
  stallNumber?: string
  stallCategoryId?: number
  status?: number
  marketId?: number
}

export interface StallAddDTO {
  marketId: number
  stallCategoryId?: number
  stallNumber: string
  stallName?: string
  stallArea?: number
  remark?: string
  /** 绑定收费规则ID集合（同一收费类型限选一条，可空=不绑定） */
  ruleIds?: number[]
}

export interface StallUpdateDTO extends StallAddDTO {
  id: number
  status?: number
}

export interface StallVO {
  id: number
  companyId: number
  marketId: number
  stallCategoryId?: number
  categoryName?: string
  stallNumber: string
  stallName?: string
  stallArea?: number
  status: number
  statusText?: string
  remark?: string
  createTime?: string
  /** 已绑定收费规则（列表展示，空=未绑定） */
  feeRules?: StallRuleRel[]
}

/** 摊位分页（含分类名称） */
export function getStallPageApi(params: StallQueryDTO): Promise<PageResult<StallVO>> {
  return get<PageResult<StallVO>>('/property/lease/stall/page', params)
}

/** 新增摊位（初始状态空置） */
export function addStallApi(data: StallAddDTO): Promise<null> {
  return post<null>('/property/lease/stall/add', data)
}

/** 编辑摊位 */
export function updateStallApi(data: StallUpdateDTO): Promise<null> {
  return post<null>('/property/lease/stall/update', data)
}

/** 删除摊位（有合同禁止删除，逻辑删除） */
export function deleteStallApi(id: number): Promise<null> {
  return post<null>('/property/lease/stall/delete', { id })
}

/* ------------------------------ 摊位收费规则绑定 ------------------------------ */

/** 收费规则下拉选项（仅启用，按收费类型分组展示） */
export interface FeeRuleOption {
  id: number
  ruleName: string
  feeItemId: number
  feeItemName?: string
  /** 收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他 */
  categoryType?: number
  calcMode?: number
  calcModeText?: string
  price?: number
  periodType?: number
  periodTypeText?: string
  overdueRate?: number
  status?: number
}

/** 摊位已绑定收费规则（编辑回显） */
export interface StallRuleRel {
  relId: number
  ruleId: number
  ruleName?: string
  feeItemId?: number
  feeItemName?: string
  /** 收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他（合同租金/押金自动带出按此匹配） */
  categoryType?: number
  calcMode?: number
  calcModeText?: string
  price?: number
  periodType?: number
  periodTypeText?: string
  overdueRate?: number
}

/** 启用中的收费规则选项（摊位绑定下拉选择） */
export function getStallRuleOptionsApi(): Promise<FeeRuleOption[]> {
  return get<FeeRuleOption[]>('/property/lease/stall/ruleOptions')
}

/** 摊位已绑定收费规则（编辑回显） */
export function getStallRuleRelListApi(stallId: number): Promise<StallRuleRel[]> {
  return get<StallRuleRel[]>('/property/lease/stall/ruleRel/list', { stallId })
}

/* ------------------------------ 摊位联动下拉（水电表绑定等复用） ------------------------------ */

export interface StallOptionVO {
  id: number
  stallNumber: string
  stallName?: string
  marketId?: number
  marketName?: string
  stallCategoryId?: number
  categoryName?: string
  /** 摊位面积(平方米，按面积收费规则计算用) */
  stallArea?: number
}

/** 摊位联动下拉选项（市场/租赁分类过滤，company 后端自动隔离） */
export function getStallOptionsApi(params: {
  marketId?: number
  stallCategoryId?: number
  status?: number
}): Promise<StallOptionVO[]> {
  return get<StallOptionVO[]>('/property/lease/stall/options', params)
}

/* ------------------------------ 租赁合同 stall_contract ------------------------------ */

export interface ContractQueryDTO {
  pageNum: number
  pageSize: number
  contractNo?: string
  tenantId?: number
  stallId?: number
  contractStatus?: number
}

export interface ContractAddDTO {
  tenantId: number
  stallId: number
  rentAmount: number
  depositAmount: number
  startTime: string
  endTime: string
  attachmentUrl?: string
  remark?: string
  /** 优惠策略ID（可选，随合同提交自动生成优惠申请） */
  policyId?: number
  /** 免租期月数（可选） */
  waiveMonths?: number
  /** 折扣率%（100=无折扣） */
  discountRate?: number
  /** 减免金额（元） */
  deductAmount?: number
  /** 优惠备注 */
  discountRemark?: string
}

export interface ContractTerminateDTO {
  contractId: number
  remark?: string
}

export interface ContractVO {
  id: number
  contractNo: string
  tenantId?: number
  tenantName?: string
  stallId: number
  stallNumber?: string
  stallName?: string
  categoryName?: string
  rentAmount: number
  depositAmount: number
  startTime?: string
  endTime?: string
  contractStatus: number
  contractStatusText?: string
  attachmentUrl?: string
  remark?: string
  createTime?: string
}

/** 合同分页（含租户/摊位/分类名称） */
export function getContractPageApi(params: ContractQueryDTO): Promise<PageResult<ContractVO>> {
  return get<PageResult<ContractVO>>('/property/lease/contract/page', params)
}

/** 新增合同（摊位置为已租，押金写收入流水） */
export function addContractApi(data: ContractAddDTO): Promise<null> {
  return post<null>('/property/lease/contract/add', data)
}

/** 退租（高危：摊位置空、押金退费支出流水、强制审计） */
export function terminateContractApi(data: ContractTerminateDTO): Promise<null> {
  return post<null>('/property/lease/contract/terminate', data)
}