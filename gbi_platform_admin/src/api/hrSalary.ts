/**
 * 薪酬体系管理前端 API
 * 覆盖：薪酬级别、薪资模板、薪资档案、批量调薪、年终奖
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ==================== 薪酬级别 hr_salary_grade ==================== */
export interface HrSalaryGradeVO {
  id: number
  companyId: number
  gradeCode: string
  gradeName: string
  gradeLevel: number
  bandMin: number
  bandMid: number
  bandMax: number
  status: number
  remark?: string
  createTime?: string
}

export function getSalaryGradePageApi(params: {
  keyword?: string
  pageNum?: number
  pageSize?: number
}) {
  return get<PageResult<HrSalaryGradeVO>>('/hr/salary/grade/page', params)
}

export function getSalaryGradeListApi() {
  return get<HrSalaryGradeVO[]>('/hr/salary/grade/list')
}

export function addSalaryGradeApi(data: Partial<HrSalaryGradeVO>) {
  return post<number>('/hr/salary/grade/add', data)
}

export function updateSalaryGradeApi(data: Partial<HrSalaryGradeVO>) {
  return post<void>('/hr/salary/grade/update', data)
}

export function disableSalaryGradeApi(id: number) {
  return post<void>('/hr/salary/grade/disable/' + id)
}

/* ==================== 薪资模板 hr_salary_rule ==================== */
export interface HrSalaryRuleVO {
  id: number
  companyId: number
  ruleName: string
  bindType: number
  postId?: number
  gradeCode?: string
  basicSalary: number
  performanceBase: number
  positionAllowance: number
  otherAllowance: number
  fixedMonthBonus: number
  socialSecurityRate: number
  housingFundRate: number
  remark?: string
  flowInstanceId?: number
  applyStatus: number
  status: number
  createTime?: string
}

export function getSalaryRulePageApi(params: {
  bindType?: number
  postId?: number
  gradeCode?: string
  pageNum?: number
  pageSize?: number
}) {
  return get<PageResult<HrSalaryRuleVO>>('/hr/salary/rule/page', params)
}

export function addSalaryRuleApi(data: Partial<HrSalaryRuleVO>) {
  return post<number>('/hr/salary/rule/add', data)
}

export function updateSalaryRuleApi(data: Partial<HrSalaryRuleVO>) {
  return post<void>('/hr/salary/rule/update', data)
}

export function disableSalaryRuleApi(id: number) {
  return post<void>('/hr/salary/rule/disable/' + id)
}

export function submitSalaryRuleAuditApi(id: number) {
  return post<void>('/hr/salary/rule/submitAudit/' + id)
}

/* ==================== 薪资档案 hr_salary_archive ==================== */
export function submitSalaryArchiveAuditApi(id: number) {
  return post<void>('/hr/salary/archive/submitAudit/' + id)
}

/* ==================== 批量调薪 hr_salary_batch_adjust ==================== */
export interface HrSalaryBatchAdjustVO {
  id: number
  companyId: number
  adjustName: string
  adjustMode: number
  adjustValue: number
  targetGradeCode?: string
  effectiveDate?: string
  totalCount: number
  successCount: number
  failCount: number
  status: number
  flowInstanceId?: number
  remark?: string
  createTime?: string
}

export function getBatchAdjustPageApi(params: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<HrSalaryBatchAdjustVO>>('/hr/salary/batchAdjust/page', params)
}

export function createBatchAdjustApi(data: Partial<HrSalaryBatchAdjustVO>) {
  return post<number>('/hr/salary/batchAdjust/createByFilter', data)
}

export function submitBatchAdjustAuditApi(id: number) {
  return post<void>('/hr/salary/batchAdjust/submitAudit/' + id)
}

/* ==================== 年终奖 hr_year_bonus ==================== */
export interface HrYearBonusVO {
  id: number
  companyId: number
  employeeId: number
  employeeName?: string
  bonusYear: number
  bonusType: number
  bonusAmount: number
  bonusReason?: string
  payStatus: number
  flowInstanceId?: number
  applyStatus: number
  status: number
  remark?: string
  createTime?: string
}

export function getYearBonusPageApi(params: {
  bonusYear?: number
  employeeId?: number
  pageNum?: number
  pageSize?: number
}) {
  return get<PageResult<HrYearBonusVO>>('/hr/salary/yearBonus/page', params)
}

export function addYearBonusApi(data: Partial<HrYearBonusVO>) {
  return post<number>('/hr/salary/yearBonus/add', data)
}

export function updateYearBonusApi(data: Partial<HrYearBonusVO>) {
  return post<void>('/hr/salary/yearBonus/update', data)
}

export function submitYearBonusAuditApi(id: number) {
  return post<void>('/hr/salary/yearBonus/submitAudit/' + id)
}
