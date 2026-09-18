/**
 * 人力资源模块接口
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ==================== 员工档案 ==================== */
export interface EmployeeQueryDTO {
  pageNum: number
  pageSize: number
  name?: string
  employeeNo?: string
  employeeStatus?: number
}

export interface EmployeeVO {
  id: number
  companyId: number
  userId?: number
  employeeNo: string
  name: string
  idCardNo?: string
  phone?: string
  email?: string
  gender?: number
  genderText?: string
  birthdate?: string
  entryDate?: string
  regularDate?: string
  resignDate?: string
  employmentType?: number
  employmentTypeText?: string
  employeeStatus: number
  employeeStatusText?: string
  orgId?: number
  postId?: number
  postLevel?: string
  orgName?: string
  supervisorId?: number
  bankAccount?: string
  socialSecurityBase?: number
  basicSalary?: number
  remark?: string
  createTime: string
}

export interface EmployeeDTO {
  id?: number
  employeeNo: string
  name: string
  idCardNo?: string
  phone?: string
  email?: string
  gender?: number
  birthdate?: string
  entryDate?: string
  employmentType?: number
  orgId?: number
  postId?: number
  bankAccount?: string
  socialSecurityBase?: number
  basicSalary?: number
  remark?: string
}

export function getEmployeePageApi(params: EmployeeQueryDTO): Promise<PageResult<EmployeeVO>> {
  return get<PageResult<EmployeeVO>>('/hr/employee/page', params)
}

export function getEmployeeApi(id: number): Promise<EmployeeVO> {
  return get<EmployeeVO>(`/hr/employee/${id}`)
}

export function addEmployeeApi(data: EmployeeDTO): Promise<number> {
  return post<number>('/hr/employee/add', data)
}

export function updateEmployeeApi(data: EmployeeDTO): Promise<void> {
  return post<void>('/hr/employee/update', data)
}

export function deleteEmployeeApi(id: number): Promise<void> {
  return post<void>(`/hr/employee/delete/${id}`)
}

export function exportEmployeeApi(ids: number[]): Promise<EmployeeVO[]> {
  return post<EmployeeVO[]>('/hr/employee/export', ids)
}

/* ==================== 工作经历 ==================== */
export interface WorkExpVO {
  id: number
  companyId: number
  employeeId: number
  companyName: string
  position?: string
  department?: string
  startDate: string
  endDate?: string
  isCurrent: number
  isCurrentText?: string
  reasonForLeaving?: string
  remark?: string
  createTime: string
}

export interface WorkExpDTO {
  id?: number
  employeeId?: number
  companyName: string
  position?: string
  department?: string
  startDate: string
  endDate?: string
  isCurrent?: number
  reasonForLeaving?: string
  remark?: string
}

export function getWorkExpsApi(employeeId: number): Promise<WorkExpVO[]> {
  return get<WorkExpVO[]>(`/hr/employee/${employeeId}/workExps`)
}

export function addWorkExpApi(data: WorkExpDTO): Promise<void> {
  return post<void>('/hr/employee/workExp/add', data)
}

export function updateWorkExpApi(data: WorkExpDTO): Promise<void> {
  return post<void>('/hr/employee/workExp/update', data)
}

export function deleteWorkExpApi(id: number): Promise<void> {
  return post<void>(`/hr/employee/workExp/delete/${id}`)
}

/* ==================== 学业经历 ==================== */
export interface EduExpVO {
  id: number
  companyId: number
  employeeId: number
  schoolName: string
  degree?: string
  major?: string
  educationLevel?: string
  startDate: string
  graduationDate?: string
  isGraduated: number
  isGraduatedText?: string
  certificateNo?: string
  remark?: string
  createTime: string
}

export interface EduExpDTO {
  id?: number
  employeeId?: number
  schoolName: string
  degree?: string
  major?: string
  educationLevel?: string
  startDate: string
  graduationDate?: string
  isGraduated?: number
  certificateNo?: string
  remark?: string
}

export function getEduExpsApi(employeeId: number): Promise<EduExpVO[]> {
  return get<EduExpVO[]>(`/hr/employee/${employeeId}/eduExps`)
}

export function addEduExpApi(data: EduExpDTO): Promise<void> {
  return post<void>('/hr/employee/eduExp/add', data)
}

export function updateEduExpApi(data: EduExpDTO): Promise<void> {
  return post<void>('/hr/employee/eduExp/update', data)
}

export function deleteEduExpApi(id: number): Promise<void> {
  return post<void>(`/hr/employee/eduExp/delete/${id}`)
}

/* ==================== 下拉选项接口 ==================== */
export interface OrgFlatOption { id: number; orgName: string; orgType?: number }
export interface PostFlatOption { id: number; postName: string; postCode: string; deptId?: number; orgName?: string }
export function getOrgFlatListApi(): Promise<OrgFlatOption[]> {
  return get<OrgFlatOption[]>(`/org/tree`).then(list => flattenOrgTree(list))
}

function flattenOrgTree(nodes: any[]): OrgFlatOption[] {
  const result: OrgFlatOption[] = []
  function walk(list: any[]) {
    for (const node of list) {
      result.push({ id: node.id, orgName: node.orgName, orgType: node.orgType })
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(nodes)
  return result
}
export function getPostFlatListApi(): Promise<PostFlatOption[]> {
  return get<PageResult<HrPostVO>>(`/hr/org/post/page`, { pageNum: 1, pageSize: 500, status: 1 }).then(r => r.records)
}

/* ==================== 组织岗位 ==================== */
export interface HrPostQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface HrPostVO {
  id: number
  companyId: number
  postName: string
  postCode: string
  postLevel?: string
  deptId?: number
  orgName?: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface PostDTO {
  id?: number
  companyId: number
  postName: string
  postCode: string
  postLevel?: string
  deptId?: number
  status?: number
  remark?: string
}

export function getPostPageApi(params: HrPostQueryDTO): Promise<PageResult<HrPostVO>> {
  return get<PageResult<HrPostVO>>('/hr/org/post/page', params)
}

export function addPostApi(data: PostDTO): Promise<void> {
  return post<void>('/hr/org/post/add', data)
}

export function updatePostApi(data: PostDTO): Promise<void> {
  return post<void>('/hr/org/post/update', data)
}

export function deletePostApi(id: number): Promise<void> {
  return post<void>(`/hr/org/post/delete/${id}`)
}

export function getPostByDeptApi(deptId: number): Promise<HrPostVO[]> {
  return get<HrPostVO[]>(`/hr/org/post/byDept/${deptId}`)
}

/* ==================== 人事异动 ==================== */
export interface HrEntryApplyVO {
  id?: number
  employeeNo: string
  name: string
  entryDate: string
  employmentType?: number
  employmentTypeText?: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface HrRegularApplyVO {
  id?: number
  employeeNo: string
  employeeName: string
  regularDate: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface HrTransferApplyVO {
  id?: number
  employeeNo: string
  employeeName: string
  transferDate: string
  targetPostName?: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface HrResignApplyVO {
  id?: number
  employeeNo: string
  employeeName: string
  resignDate: string
  status: number
  statusText?: string
  reason?: string
  remark?: string
  createTime: string
}

export interface EntryApplyDTO {
  employeeNo: string
  name: string
  idCardNo?: string
  phone?: string
  gender?: number
  birthdate?: string
  entryDate: string
  employmentType?: number
  orgId?: number
  postId?: number
  basicSalary?: number
  bankAccount?: string
  autoCreateUser?: number
  remark?: string
  experienceData?: string
}

export interface RegularApplyDTO {
  employeeId: number
  regularDate: string
  remark?: string
}

export interface TransferApplyDTO {
  employeeId: number
  newOrgId: number
  newPostId: number
  transferDate: string
  reason?: string
}

export interface ResignApplyDTO {
  employeeId: number
  resignDate: string
  resignType: number
  reason?: string
  handoverRemark?: string
}

export function getEntryPageApi(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<HrEntryApplyVO>> {
  return get<PageResult<HrEntryApplyVO>>('/hr/transfer/entry/page', params)
}

export function getRegularPageApi(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<HrRegularApplyVO>> {
  return get<PageResult<HrRegularApplyVO>>('/hr/transfer/regular/page', params)
}

export function getTransferPageApi(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<HrTransferApplyVO>> {
  return get<PageResult<HrTransferApplyVO>>('/hr/transfer/transfer/page', params)
}

export function getResignPageApi(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<HrResignApplyVO>> {
  return get<PageResult<HrResignApplyVO>>('/hr/transfer/resign/page', params)
}

export function submitEntryApi(data: EntryApplyDTO): Promise<void> {
  return post<void>('/hr/transfer/entry/submit', data)
}

export function submitRegularApi(data: RegularApplyDTO): Promise<void> {
  return post<void>('/hr/transfer/regular/submit', data)
}

export function submitTransferApi(data: TransferApplyDTO): Promise<void> {
  return post<void>('/hr/transfer/transfer/submit', data)
}

export function submitResignApi(data: ResignApplyDTO): Promise<void> {
  return post<void>('/hr/transfer/resign/submit', data)
}

export function revokeEntryApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/entry/revoke/${id}`)
}

export function revokeRegularApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/regular/revoke/${id}`)
}

export function revokeTransferApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/transfer/revoke/${id}`)
}

export function revokeResignApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/resign/revoke/${id}`)
}

/* ==================== 考勤管理 ==================== */
export interface AttendanceQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  attendanceMonth?: string
}

export interface AttendanceVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  attendanceMonth: string
  attendanceDay: string
  clockInTime?: string
  clockOutTime?: string
  clockType?: number
  clockTypeText?: string
  lateMinutes?: number
  earlyMinutes?: number
  absent?: number
  leaveDays?: number
  workDays?: number
  actualDays?: number
  remark?: string
  createTime: string
}

export function getAttendancePageApi(params: AttendanceQueryDTO): Promise<PageResult<AttendanceVO>> {
  return get<PageResult<AttendanceVO>>('/hr/attendance/page', params)
}

export function syncAttendanceApi(attendanceMonth?: string): Promise<number> {
  return post<number>('/hr/attendance/sync', null, { params: { attendanceMonth } })
}

export function exportAttendanceApi(params: { employeeId?: number; attendanceMonth?: string }): Promise<AttendanceVO[]> {
  return get<AttendanceVO[]>('/hr/attendance/export', { params })
}

/* ==================== 薪资档案 ==================== */
export interface SalaryArchiveQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
}

export interface SalaryArchiveVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  gradeCode?: string
  gradeName?: string
  ruleId?: number
  ruleName?: string
  versionNo?: number
  sourceType?: number
  effectiveDate?: string
  isCurrent?: number
  basicSalary: number
  performanceSalary: number
  positionAllowance: number
  otherAllowance: number
  remark?: string
  createTime: string
}

export interface SalaryArchiveDTO {
  id?: number
  employeeId: number
  ruleId?: number
  basicSalary?: number
  performanceSalary?: number
  positionAllowance?: number
  otherAllowance?: number
  effectiveDate?: string
  remark?: string
}

export function getSalaryArchivePageApi(params: SalaryArchiveQueryDTO): Promise<PageResult<SalaryArchiveVO>> {
  return get<PageResult<SalaryArchiveVO>>('/hr/salary/archive/page', params)
}

export function addSalaryArchiveApi(data: SalaryArchiveDTO): Promise<void> {
  return post<void>('/hr/salary/archive/add', data)
}

export function updateSalaryArchiveApi(data: SalaryArchiveDTO): Promise<void> {
  return post<void>('/hr/salary/archive/update', data)
}

export function deleteSalaryArchiveApi(id: number): Promise<void> {
  return post<void>(`/hr/salary/archive/delete/${id}`)
}

/* ==================== 月度薪资 ==================== */
export interface SalaryMonthQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  salaryMonth?: string
}

export interface SalaryMonthVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  salaryMonth: string
  basicSalary: number
  performanceSalary: number
  allowanceAmount: number
  socialSecurity: number
  housingFund: number
  taxAmount: number
  deductionAmount: number
  attendanceDeduction?: number
  absentDeduction?: number
  lateDeduction?: number
  earlyDeduction?: number
  unpaidLeaveDeduction?: number
  minWageProtected?: number
  grossAmount: number
  netAmount: number
  payStatus: number
  payStatusText?: string
  payTime?: string
  flowInstanceId?: number
  planId?: number
  remark?: string
  createTime: string
}

export interface SalaryMonthDTO {
  employeeIds: number[]
  salaryMonth: string
  syncAttendance?: number
}

export function getSalaryMonthPageApi(params: SalaryMonthQueryDTO): Promise<PageResult<SalaryMonthVO>> {
  return get<PageResult<SalaryMonthVO>>('/hr/salary/month/page', params)
}

export function generateSalaryMonthApi(data: SalaryMonthDTO): Promise<void> {
  return post<void>('/hr/salary/month/generate', data)
}

export function paySalaryMonthApi(id: number): Promise<void> {
  return post<void>(`/hr/salary/month/pay/${id}`)
}

export function exportSalaryMonthApi(params: { employeeId?: number; salaryMonth?: string }): Promise<SalaryMonthVO[]> {
  return post<SalaryMonthVO[]>('/hr/salary/month/export', null, { params })
}

/* ==================== 社保公积金 ==================== */
export interface SocialQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  status?: number
}

export interface SocialVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  socialSecurityBase: number
  housingFundBase: number
  socialSecurityCompany: number
  socialSecurityPersonal: number
  housingFundCompany: number
  housingFundPersonal: number
  startMonth: string
  endMonth?: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface SocialDTO {
  id?: number
  employeeId: number
  socialSecurityBase: number
  housingFundBase: number
  socialSecurityCompany?: number
  socialSecurityPersonal?: number
  housingFundCompany?: number
  housingFundPersonal?: number
  startMonth: string
  endMonth?: string
  status?: number
  remark?: string
}

export function getSocialPageApi(params: SocialQueryDTO): Promise<PageResult<SocialVO>> {
  return get<PageResult<SocialVO>>('/hr/social/page', params)
}

export function addSocialApi(data: SocialDTO): Promise<void> {
  return post<void>('/hr/social/add', data)
}

export function updateSocialApi(data: SocialDTO): Promise<void> {
  return post<void>('/hr/social/update', data)
}

export function deleteSocialApi(id: number): Promise<void> {
  return post<void>(`/hr/social/delete/${id}`)
}
