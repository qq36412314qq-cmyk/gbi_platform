/**
 * OA 办公模块接口
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ==================== 请假管理 ==================== */
export interface LeaveQueryDTO {
  pageNum: number
  pageSize: number
  leaveType?: number
  applyStatus?: number
  applyUserId?: number
}

export interface LeaveVO {
  id: number
  companyId: number
  applyNo: string
  applyUserId: number
  applyUserName: string
  leaveType: number
  leaveTypeText?: string
  startDate: string
  endDate: string
  leaveDays: number
  reason: string
  flowInstanceId?: number
  applyStatus: number
  applyStatusText?: string
  remark: string
  createTime: string
}

export function getLeavePageApi(params: LeaveQueryDTO): Promise<PageResult<LeaveVO>> {
  return get<PageResult<LeaveVO>>('/oa/leave/page', params)
}

export function getLeaveApi(id: number): Promise<LeaveVO> {
  return get<LeaveVO>(`/oa/leave/${id}`)
}

export interface SubmitLeaveDTO {
  companyId: number
  applyUserId: number
  applyUserName: string
  leaveType: number
  startDate: string
  endDate: string
  leaveDays: number
  reason: string
  remark?: string
}

export function submitLeaveApi(data: SubmitLeaveDTO): Promise<number> {
  return post<number>('/oa/leave/submit', data)
}

export function cancelLeaveApi(id: number): Promise<void> {
  return post<void>(`/oa/leave/cancel/${id}`)
}

/* ==================== 会议室管理 ==================== */
export interface MeetingRoomVO {
  id: number
  companyId: number
  roomName: string
  location: string
  capacity: number
  facilities: string
  status: number
  remark: string
}

export function getMeetingRoomPageApi(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<MeetingRoomVO>> {
  return get<PageResult<MeetingRoomVO>>('/oa/meeting/room/page', params)
}

export function addMeetingRoomApi(data: Partial<MeetingRoomVO>): Promise<void> {
  return post<void>('/oa/meeting/room/add', data)
}

export function updateMeetingRoomApi(data: MeetingRoomVO): Promise<void> {
  return post<void>('/oa/meeting/room/update', data)
}

/* ==================== 会议室预约 ==================== */
export interface MeetingBookingVO {
  id: number
  companyId: number
  roomId: number
  bookerId: number
  bookerName: string
  meetingTitle: string
  meetingDate: string
  startTime: string
  endTime: string
  attendeeCount: number
  bookStatus: number
  bookStatusText?: string
}

export function getMeetingBookingPageApi(params: { pageNum: number; pageSize: number; roomId?: number; bookerId?: number }): Promise<PageResult<MeetingBookingVO>> {
  return get<PageResult<MeetingBookingVO>>('/oa/meeting-booking/page', params)
}

export interface BookMeetingRoomDTO {
  companyId: number
  bookerId: number
  bookerName: string
  roomId: number
  meetingTitle: string
  meetingDate: string
  startTime: string
  endTime: string
  attendeeCount?: number
  attendeeIds?: string
  remark?: string
}

export function bookMeetingRoomApi(data: BookMeetingRoomDTO): Promise<number> {
  return post<number>('/oa/meeting-booking/book', data)
}

export function cancelMeetingBookingApi(id: number): Promise<void> {
  return post<void>(`/oa/meeting-booking/cancel/${id}`)
}

/* ==================== 公告管理 ==================== */
export interface AnnouncementVO {
  id: number
  companyId: number
  title: string
  content: string
  publishType: number
  priority: number
  publisherId: number
  publisherName: string
  publishTime?: string
  publishStatus: number
  publishStatusText?: string
  readCount: number
  createTime: string
}

export interface AnnouncementQueryDTO {
  pageNum: number
  pageSize: number
  publishStatus?: number
  publishType?: number
}

export function getAnnouncementPageApi(params: AnnouncementQueryDTO): Promise<PageResult<AnnouncementVO>> {
  return get<PageResult<AnnouncementVO>>('/oa/announcement/page', params)
}

export function getAnnouncementApi(id: number): Promise<AnnouncementVO> {
  return get<AnnouncementVO>(`/oa/announcement/${id}`)
}

export interface CreateAnnouncementDTO {
  companyId: number
  publisherId: number
  publisherName: string
  title: string
  content: string
  publishType: number
  targetDeptIds?: string
  targetUserIds?: string
  remark?: string
}

export function createAnnouncementApi(data: CreateAnnouncementDTO): Promise<number> {
  return post<number>('/oa/announcement/create', data)
}

export function publishAnnouncementApi(id: number): Promise<void> {
  return post<void>(`/oa/announcement/publish/${id}`)
}

export function recallAnnouncementApi(id: number): Promise<void> {
  return post<void>(`/oa/announcement/recall/${id}`)
}

/* ==================== 打卡管理 ==================== */
export interface ClockRecordVO {
  id: number
  companyId: number
  userId: number
  userName: string
  clockType: number
  clockTime: string
  locationLat?: number
  locationLng?: number
  locationAddr?: string
  deviceType?: number
  status?: number
  remark?: string
}

export interface ClockQueryDTO {
  pageNum: number
  pageSize: number
  userId?: number
  clockType?: number
  startDate?: string
  endDate?: string
}

export function getClockRecordPageApi(params: ClockQueryDTO): Promise<PageResult<ClockRecordVO>> {
  return get<PageResult<ClockRecordVO>>('/oa/clock/page', params)
}

export interface ClockInDTO {
  companyId: number
  userId: number
  userName: string
  clockType: number
  locationLat?: number
  locationLng?: number
  locationAddr?: string
  deviceType?: number
  remark?: string
}

export function clockInApi(data: ClockInDTO): Promise<void> {
  return post<void>('/oa/clock/in', data)
}

/* ==================== 工作汇报 ==================== */
export interface WorkReportVO {
  id: number
  companyId: number
  userId: number
  userName: string
  reportType: number
  reportTypeText?: string
  reportPeriod: string
  reportContent: string
  flowInstanceId?: number
  reportStatus: number
  reportStatusText?: string
  feedback?: string
  createTime: string
}

export interface WorkReportQueryDTO {
  pageNum: number
  pageSize: number
  reportType?: number
  submitUserId?: number
  reportStatus?: number
}

export function getWorkReportPageApi(params: WorkReportQueryDTO): Promise<PageResult<WorkReportVO>> {
  return get<PageResult<WorkReportVO>>('/oa/work-report/page', params)
}

export interface SubmitWorkReportDTO {
  companyId: number
  submitUserId: number
  submitUserName: string
  reportType: number
  reportPeriod: string
  reportContent: string
  remark?: string
}

export function submitWorkReportApi(data: SubmitWorkReportDTO): Promise<number> {
  return post<number>('/oa/work-report/submit', data)
}

export function cancelWorkReportApi(id: number): Promise<void> {
  return post<void>(`/oa/work-report/cancel/${id}`)
}
