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


/* ==================== 下拉选项接口 ==================== */
export interface OrgFlatOption { id: number; orgName: string }
export interface PostFlatOption { id: number; postName: string; postCode: string }
export function getOrgFlatListApi(): Promise<OrgFlatOption[]> {
  return get<OrgFlatOption[]>(`/org/tree`)
}
export function getPostFlatListApi(): Promise<PostFlatOption[]> {
  return get<PageResult<PostVO>>(`/hr/org/post/page`, { pageNum: 1, pageSize: 500, status: 1 }).then(r => r.records)
}

/* ==================== 组织岗位 ==================== */
export interface PostQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface PostVO {
  id: number
  companyId: number
  postName: string
  postCode: string
  postLevel?: string
  deptId?: number
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface PostDTO {
  id?: number
  postName: string
  postCode: string
  postLevel?: string
  deptId?: number
  status?: number
  remark?: string
}

export function getPostPageApi(params: PostQueryDTO): Promise<PageResult<PostVO>> {
  return get<PageResult<PostVO>>('/hr/org/post/page', params)
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

/* ==================== 入职申请 ==================== */
export interface EntryApplyQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface EntryApplyVO {
  id: number
  companyId: number
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
  flowInstanceId?: number
  status: number
  statusText?: string
  remark?: string
  employmentTypeText?: string
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
}

export function getEntryPageApi(params: EntryApplyQueryDTO): Promise<PageResult<EntryApplyVO>> {
  return get<PageResult<EntryApplyVO>>('/hr/transfer/entry/page', params)
}

export function submitEntryApi(data: EntryApplyDTO): Promise<number> {
  return post<number>('/hr/transfer/entry/submit', data)
}

export function revokeEntryApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/entry/revoke/${id}`)
}

export function getEntryDetailApi(id: number): Promise<EntryApplyVO> {
}

/* =================== 转正申请 ==================== */
export interface RegularApplyQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface RegularApplyVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  regularDate: string
  remark?: string
  flowInstanceId?: number
  status: number
  statusText?: string
  createTime: string
}

export interface RegularApplyDTO {
  employeeId: number
  regularDate: string
  remark?: string
}

export function getRegularPageApi(params: RegularApplyQueryDTO): Promise<PageResult<RegularApplyVO>> {
  return get<PageResult<RegularApplyVO>>('/hr/transfer/regular/page', params)
}

export function submitRegularApi(data: RegularApplyDTO): Promise<number> {
  return post<number>('/hr/transfer/regular/submit', data)
}

export function revokeRegularApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/regular/revoke/${id}`)
}

/* ==================== 调岗申请 ==================== */
export interface TransferApplyQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface TransferApplyVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  oldOrgId?: number
  oldPostId?: number
  newOrgId: number
  newPostId: number
  newPostLevel?: string
  transferDate: string
  reason?: string
  flowInstanceId?: number
  status: number
  statusText?: string
}

export interface TransferApplyDTO {
  employeeId: number
  newOrgId: number
  newPostId: number
  transferDate: string
  reason?: string
}

export function getTransferPageApi(params: TransferApplyQueryDTO): Promise<PageResult<TransferApplyVO>> {
  return get<PageResult<TransferApplyVO>>('/hr/transfer/transfer/page', params)
}

export function submitTransferApi(data: TransferApplyDTO): Promise<number> {
  return post<number>('/hr/transfer/transfer/submit', data)
}

export function revokeTransferApi(id: number): Promise<void> {
  return post<void>(`/hr/transfer/transfer/revoke/${id}`)
}

/* ==================== 离职申请 ==================== */
export interface ResignApplyQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
}

export interface ResignApplyVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  resignDate: string
  resignType?: number
  resignTypeText?: string
  reason?: string
  handoverRemark?: string
  flowInstanceId?: number
  status: number
  statusText?: string
  createTime: string
}

export interface ResignApplyDTO {
  employeeId: number
  resignDate: string
  resignType: number
  reason?: string
  handoverRemark?: string
}

export function getResignPageApi(params: ResignApplyQueryDTO): Promise<PageResult<ResignApplyVO>> {
  return get<PageResult<ResignApplyVO>>('/hr/transfer/resign/page', params)
}

export function submitResignApi(data: ResignApplyDTO): Promise<number> {
  return post<number>('/hr/transfer/resign/submit', data)
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
  return post<AttendanceVO[]>('/hr/attendance/export', null, { params })
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
  basicSalary: number
  performanceSalary: number
  positionAllowance: number
  otherAllowance: number
  socialSecurityPersonal: number
  housingFundPersonal: number
  remark?: string
  createTime: string
}

export interface SalaryArchiveDTO {
  id?: number
  employeeId: number
  basicSalary?: number
  performanceSalary?: number
  positionAllowance?: number
  otherAllowance?: number
  socialSecurityPersonal?: number
  housingFundPersonal?: number
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
