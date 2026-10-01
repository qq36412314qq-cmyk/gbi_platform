/**
 * 人力资源模块接口
 */
import { get, post, request } from '@/utils/request'
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
  photoFileId?: number
  photoPreviewUrl?: string
  attachmentContent?: string
  cityId?: number
  cityName?: string
  salaryRuleId?: number
  salaryRuleName?: string
  workweekConfigId?: number
  workweekConfigName?: string
  /** 是否免考勤 0参与 1不参与 */
  exemptAttendance?: number
  /** 是否参与考勤文本 */
  exemptAttendanceText?: string
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
  cityId?: number
  salaryRuleId?: number
  workweekConfigId?: number
  /** 是否免考勤 0参与 1不参与 */
  exemptAttendance?: number
  remark?: string
  photoFileId?: number
  attachmentContent?: string
}

export function getEmployeePageApi(params: EmployeeQueryDTO): Promise<PageResult<EmployeeVO>> {
  return get<PageResult<EmployeeVO>>('/hr/employee/page', params)
}

export function getEmployeeListApi(params?: { name?: string; employeeNo?: string; employeeStatus?: number; employmentType?: number }): Promise<EmployeeVO[]> {
  return get<EmployeeVO[]>('/hr/employee/list', params ?? {})
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
export interface PostFlatOption { id: number; postName: string; postCode: string; deptId?: number; orgName?: string; workweekConfigId?: number; shiftType?: number }
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
  workweekConfigId?: number
  workweekConfigName?: string
  shiftType?: number
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
  workweekConfigId?: number
  shiftType?: number
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
  companyId?: number
  employeeNo: string
  name: string
  idCardNo?: string
  phone?: string
  gender?: number
  birthdate?: string
  entryDate: string
  employmentType?: number
  employmentTypeText?: string
  orgId?: number
  postId?: number
  orgName?: string
  postName?: string
  basicSalary?: number
  bankAccount?: string
  autoCreateUser?: number
  status: number
  statusText?: string
  flowInstanceId?: number
  remark?: string
  createTime: string
  experienceData?: string
  /** 免冠照片对应的 sys_file.id */
  photoFileId?: number
  /** 免冠照片可直访预览URL */
  photoPreviewUrl?: string
  /** 附件内容（富文本HTML） */
  attachmentContent?: string
  /** 就职城市ID */
  cityId?: number
  /** 就职城市名称 */
  cityName?: string
  /** 薪资模板ID */
  salaryRuleId?: number
  /** 薪资模板名称 */
  salaryRuleName?: string
  /** 休息日配置ID */
  workweekConfigId?: number
  /** 休息日配置名称 */
  workweekConfigName?: string
  /** 是否免考勤 0参与 1不参与 */
  exemptAttendance?: number
  /** 是否参与考勤文本 */
  exemptAttendanceText?: string
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
  /** 免冠照片对应的 sys_file.id */
  photoFileId?: number
  /** 附件内容（富文本HTML） */
  attachmentContent?: string
  /** 就职城市ID（关联sys_city.id） */
  cityId?: number
  /** 薪资模板ID（关联hr_salary_rule.id） */
  salaryRuleId?: number
  /** 休息日配置ID（关联sys_workweek_config.id） */
  workweekConfigId?: number
  /** 是否免考勤 0参与 1不参与 */
  exemptAttendance?: number
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
  overtimeAmount?: number
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
  syncOvertime?: number
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
/* ==================== 考勤高级同步 ==================== */
export interface AttendancePreviewDTO {
  month: string
  employeeScope: number  // 0=全部 1=指定
  employeeIds?: number[]
  syncType?: number  // 1=首次同步 2=重新同步
}

export interface AttendanceSyncPreviewVO {
  workDays: number
  clockRecords: number
  willCreate: number
  fullAttendance: number
  normal: number
  late: number
  absent: number
  employeeCount: number
}

export interface AttendanceExceptionVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  exceptionType: number
  exceptionTypeText?: string
  exceptionDate: string
  detailCount?: number
  detailJson?: string
  status: number
  statusText?: string
  handleBy?: number
  handleTime?: string
  handleRemark?: string
  createTime: string
}

export interface WorkweekConfigVO {
  id: number
  companyId: number
  configName: string
  workweekType: number
  workweekTypeText?: string
  restDayPattern?: string
  status: number
  statusText?: string
  remark?: string
  createTime: string
}

export interface HolidayConfigVO {
  id: number
  companyId: number
  holidayDate: string
  holidayName: string
  holidayType: number
  holidayTypeText?: string
  isWorkday: number
  status: number
  remark?: string
  createTime: string
}

export interface ShiftConfigVO {
  shiftType: number
  shiftTypeText?: string
  shiftStartTime?: string
  shiftEndTime?: string
}

export interface EmployeeShiftVO {
  id: number
  companyId: number
  employeeId: number
  employeeName?: string
  shiftType: number
  shiftTypeText?: string
  shiftStartTime?: string
  shiftEndTime?: string
  startDate: string
  endDate?: string
  status: number
  remark?: string
  createTime: string
}

export function syncAttendancePreviewApi(data: AttendancePreviewDTO): Promise<AttendanceSyncPreviewVO> {
  return post<AttendanceSyncPreviewVO>('/hr/attendance/sync/preview', data)
}

export function syncAttendanceAdvancedApi(data: AttendancePreviewDTO): Promise<number> {
  return post<number>('/hr/attendance/sync/advanced', data)
}

export function getAttendanceExceptionsApi(params: { pageNum: number; pageSize: number; employeeId?: number; exceptionType?: number; status?: number }): Promise<PageResult<AttendanceExceptionVO>> {
  return get<PageResult<AttendanceExceptionVO>>('/hr/attendance/exception/page', params)
}

export function handleAttendanceExceptionApi(id: number, handleType: number, handleRemark?: string, handlerId?: number): Promise<void> {
  return post<void>('/hr/attendance/exception/handle', null, { params: { id, handleType, handleRemark, handlerId } })
}

export function detectAttendanceExceptionsApi(month: string): Promise<void> {
  return get<void>('/hr/attendance/exception/detect', { month })
}

export function getAttendanceConfigsApi(): Promise<Record<string, string>> {
  return get<Record<string, string>>('/hr/attendance/config/params')
}

export function updateAttendanceConfigsApi(configs: Record<string, string>): Promise<void> {
  return put<void>('/hr/attendance/config/params', configs)
}

export function getWorkweekConfigListApi(): Promise<WorkweekConfigVO[]> {
  return get<WorkweekConfigVO[]>('/hr/attendance/workweek/list')
}

export function createWorkweekConfigApi(data: Partial<WorkweekConfigVO>): Promise<number> {
  return post<number>('/hr/attendance/workweek/add', data)
}

export function addWorkweekConfigApi(data: Partial<WorkweekConfigVO>): Promise<number> {
  return post<number>('/hr/attendance/workweek/add', data)
}

export function updateWorkweekConfigApi(data: Partial<WorkweekConfigVO>): Promise<void> {
  return put<void>('/hr/attendance/workweek/update', data)
}

export function deleteWorkweekConfigApi(id: number): Promise<void> {
  return post<void>(`/hr/attendance/workweek/delete/${id}`)
}

export function setDefaultWorkweekConfigApi(id: number): Promise<void> {
  return post<void>(`/hr/attendance/workweek/default/${id}`)
}

export function getHolidayConfigListApi(params: { year?: number }): Promise<HolidayConfigVO[]> {
  return get<HolidayConfigVO[]>('/hr/attendance/holiday/list', params)
}

export function createHolidayConfigApi(data: Partial<HolidayConfigVO>): Promise<number> {
  return post<number>('/hr/attendance/holiday/add', data)
}

export function updateHolidayConfigApi(data: Partial<HolidayConfigVO>): Promise<void> {
  return put<void>('/hr/attendance/holiday/update', data)
}

export function deleteHolidayConfigApi(id: number): Promise<void> {
  return post<void>(`/hr/attendance/holiday/delete/${id}`)
}

export function getEmployeeShiftApi(employeeId: number, date: string): Promise<ShiftConfigVO> {
  return get<ShiftConfigVO>(`/hr/attendance/shift/${employeeId}`, { params: { date } })
}

export function listEmployeeShiftsApi(employeeId: number): Promise<EmployeeShiftVO[]> {
  return get<EmployeeShiftVO[]>(`/hr/attendance/shift/list/${employeeId}`)
}

export function addEmployeeShiftApi(data: Partial<EmployeeShiftVO>): Promise<void> {
  return post<void>('/hr/attendance/shift/add', data)
}

export function updateEmployeeShiftApi(data: Partial<EmployeeShiftVO>): Promise<void> {
  return put<void>('/hr/attendance/shift/update', data)
}

export function deleteEmployeeShiftApi(id: number): Promise<void> {
  return post<void>(`/hr/attendance/shift/delete/${id}`)
}

export function batchHandleAttendanceExceptionsApi(ids: number[], handleType: number, handleRemark?: string): Promise<void> {
  return post<void>('/hr/attendance/exception/batch-handle', null, { params: { ids: ids.join(','), handleType, handleRemark } })
}

/* ==================== 加班管理 ==================== */
/** PUT 请求辅助（request.ts 未导出 put，此处内联） */
function put<T = unknown>(url: string, data?: object): Promise<T> {
  return request<T>({ url, method: 'put', data })
}

/* --- 类型定义 --- */
export interface HrOvertimeApplyVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  overtimeDate: string
  startTime: string
  endTime: string
  expectedHours: number
  overtimeType: number          // 1工作日 2休息日 3法定节假日
  overtimeTypeText?: string
  reason: string
  status: number                // 0待审批 1已通过 2已驳回 3已撤回 4已取消
  statusText?: string
  flowInstanceId?: number
  createTime: string
}

export interface HrOvertimeApplyQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  employeeName?: string
  overtimeDateStart?: string
  overtimeDateEnd?: string
  status?: number
}

export interface HrOvertimeApplyAddDTO {
  employeeId: number
  employeeName: string
  overtimeDate: string
  startTime: string
  endTime: string
  expectedHours: number
  overtimeType?: number
  reason: string
}

export interface HrOvertimeRecordVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  overtimeDate: string
  startTime: string
  endTime: string
  overtimeHours: number
  overtimeType: number          // 1工作日 2休息日 3法定节假日
  overtimeTypeText?: string
  sourceType: number            // 1手动申请 2自动识别
  sourceTypeText?: string
  applyId?: number
  attendRecordId?: number
  confirmStatus: number         // 0待确认 1已确认 2已驳回
  confirmStatusText?: string
  confirmTime?: string
  status: number                // 1有效 2已抵扣 3已作废
  statusText?: string
  compensateStatus: number      // 0未补偿 1已调休 2已发放加班费
  compensateStatusText?: string
  createTime: string
}

export interface HrOvertimeRecordQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  employeeName?: string
  overtimeDateStart?: string
  overtimeDateEnd?: string
  sourceType?: number
  confirmStatus?: number
}

export interface HrOvertimeRecordConfirmDTO {
  id: number
  confirmStatus: number         // 1确认 2驳回
  remark?: string
}

export interface HrOvertimeAutoDetectVO {
  totalRecords: number
  newRecords: number
  updatedRecords: number
  skippedRecords: number
}

export interface HrOvertimeCompensateVO {
  id: number
  companyId: number
  employeeId: number
  employeeName: string
  basicSalary: number
  compensateMonth: string
  totalHours: number
  workdayHours: number
  restdayHours: number
  holidayHours: number
  usedHours: number
  remainHours: number
  compensateType: number        // 1调休 2加班费 3混合
  compensateTypeText?: string
  overtimeAmount: number
  payStatus: number             // 0待发放 1已发放 2已取消
  payStatusText?: string
  payTime?: string
  remark?: string
  createTime: string
}

export interface HrOvertimeCompensateQueryDTO {
  pageNum: number
  pageSize: number
  employeeId?: number
  employeeName?: string
  compensateMonth?: string
  payStatus?: number
}

export interface HrOvertimeCompensateCalculateDTO {
  compensateMonth: string
}

export interface SysOvertimeConfigVO {
  id: number
  companyId: number
  configName: string
  overtimeMinHours: number
  overtimeRoundMode: number     // 1向上 2四舍五入 3向下
  overtimeRoundModeText?: string
  workdayRate: number
  restdayRate: number
  holidayRate: number
  maxOvertimeHours?: number
  compensatePriority: number    // 1调休优先 2加班费优先
  compensatePriorityText?: string
  autoDetectEnabled: number
  autoDetectCron?: string
  status: number                // 0禁用 1启用
  statusText?: string
  remark?: string
}

export interface SysOvertimeConfigSaveDTO {
  id: number
  configName: string
  overtimeMinHours: number
  overtimeRoundMode: number
  workdayRate: number
  restdayRate: number
  holidayRate: number
  maxOvertimeHours?: number
  compensatePriority: number
  status: number
  remark?: string
}

/* --- API 函数 --- */
export function getOvertimeApplyPageApi(params: HrOvertimeApplyQueryDTO): Promise<PageResult<HrOvertimeApplyVO>> {
  return get<PageResult<HrOvertimeApplyVO>>('/hr/overtime/apply/page', params)
}

export function addOvertimeApplyApi(data: HrOvertimeApplyAddDTO): Promise<number> {
  return post<number>('/hr/overtime/apply/add', data)
}

export function editOvertimeApplyApi(data: HrOvertimeApplyAddDTO & { id: number }): Promise<void> {
  return put<void>('/hr/overtime/apply/edit', data)
}

export function deleteOvertimeApplyApi(id: number): Promise<void> {
  return post<void>(`/hr/overtime/apply/delete/${id}`)
}

export function revokeOvertimeApplyApi(id: number): Promise<void> {
  return post<void>(`/hr/overtime/apply/revoke/${id}`)
}

export function getOvertimeRecordPageApi(params: HrOvertimeRecordQueryDTO): Promise<PageResult<HrOvertimeRecordVO>> {
  return get<PageResult<HrOvertimeRecordVO>>('/hr/overtime/record/page', params)
}

export function confirmOvertimeRecordApi(data: HrOvertimeRecordConfirmDTO): Promise<void> {
  return post<void>('/hr/overtime/record/confirm', data)
}

export function autoDetectOvertimeApi(date?: string): Promise<HrOvertimeAutoDetectVO> {
  if (date) {
    return get<HrOvertimeAutoDetectVO>('/hr/overtime/record/detect/byDate', { params: { date } })
  }
  return post<HrOvertimeAutoDetectVO>('/hr/overtime/record/detect')
}

export function exportOvertimeRecordApi(params: HrOvertimeRecordQueryDTO): Promise<void> {
  return get<void>('/hr/overtime/record/export', params)
}

export function getOvertimeCompensatePageApi(params: HrOvertimeCompensateQueryDTO): Promise<PageResult<HrOvertimeCompensateVO>> {
  return get<PageResult<HrOvertimeCompensateVO>>('/hr/overtime/compensate/page', params)
}

export function calculateOvertimeCompensateApi(data: HrOvertimeCompensateCalculateDTO): Promise<void> {
  return post<void>('/hr/overtime/compensate/calculate', data)
}

export function payOvertimeCompensateApi(id: number): Promise<void> {
  return post<void>(`/hr/overtime/compensate/pay/${id}`)
}

export function exportOvertimeCompensateApi(params: HrOvertimeCompensateQueryDTO): Promise<void> {
  return get<void>('/hr/overtime/compensate/export', params)
}

export function getOvertimeConfigApi(): Promise<SysOvertimeConfigVO> {
  return get<SysOvertimeConfigVO>('/hr/overtime/config')
}

export function saveOvertimeConfigApi(data: SysOvertimeConfigSaveDTO): Promise<void> {
  return put<void>('/hr/overtime/config/save', data)
}

export function updateOvertimeConfigStatusApi(id: number, status: number): Promise<void> {
  return request<void>({ url: '/hr/overtime/config/status', method: 'put', params: { id, status } })
}
