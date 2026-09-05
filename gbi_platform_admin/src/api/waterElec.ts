/**
 * 水电物业模块接口（对应后端 waterElec 三个控制器）
 * 设备管理 / 账单管理 / 缴费管理
 */
import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/* ------------------------------ 水电表设备 water_elec_meter ------------------------------ */

export interface WaterElecMeterQueryDTO {
  pageNum: number
  pageSize: number
  meterNo?: string
  meterType?: number
  status?: number
  marketId?: number
}

export interface WaterElecMeterDTO {
  id?: number
  stallId: number
  meterNo: string
  meterType: number
  gatewayCode?: string
  currentRead?: number
  balanceAmount?: number
}

export interface WaterElecMeterVO {
  id: number
  companyId: number
  stallId: number
  stallNumber?: string
  stallName?: string
  stallMarketId?: number
  stallMarketName?: string
  stallCategoryId?: number
  categoryName?: string
  meterNo: string
  meterType: number
  meterTypeText?: string
  gatewayCode?: string
  currentRead?: number
  balanceAmount?: number
  status: number
  statusText?: string
  createTime?: string
}

export interface WaterElecReadDTO {
  meterId: number | null
  currentRead: number
}

export interface WaterElecSwitchDTO {
  meterId: number | null
  status: number
}

export function getMeterPageApi(params: WaterElecMeterQueryDTO): Promise<PageResult<WaterElecMeterVO>> {
  return get<PageResult<WaterElecMeterVO>>('/waterElec/meter/page', params)
}
export function addMeterApi(data: WaterElecMeterDTO): Promise<null> {
  return post<null>('/waterElec/meter/add', data)
}
export function updateMeterApi(data: WaterElecMeterDTO): Promise<null> {
  return post<null>('/waterElec/meter/update', data)
}
export function deleteMeterApi(id: number): Promise<null> {
  return post<null>('/waterElec/meter/delete', { id })
}
export function readMeterApi(data: WaterElecReadDTO): Promise<null> {
  return post<null>('/waterElec/meter/read', data)
}
export function switchMeterApi(data: WaterElecSwitchDTO): Promise<null> {
  return post<null>('/waterElec/meter/switch', data)
}

/* ------------------------------ 月度账单 water_elec_bill ------------------------------ */

export interface WaterElecBillQueryDTO {
  pageNum: number
  pageSize: number
  billMonth?: string
  stallId?: number
  payStatus?: number
  category?: number
}

export interface WaterElecBillVO {
  id: number
  companyId: number
  stallId: number
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  categoryName?: string
  merchantId?: number
  tenantName?: string
  hasFeeBill?: boolean
  billMonth: string
  category?: number
  categoryText?: string
  usage?: number
  unitPrice?: number
  amount?: number
  totalAmount: number
  payStatus: number
  payStatusText?: string
  payTime?: string
  createTime?: string
}

export interface MeterReadItem {
  meterId: number | null
  currentRead: number
}

export interface WaterElecBillGenerateDTO {
  billMonth: string
  meterReads: MeterReadItem[]
}

export function getBillPageApi(params: WaterElecBillQueryDTO): Promise<PageResult<WaterElecBillVO>> {
  return get<PageResult<WaterElecBillVO>>('/waterElec/bill/page', params)
}
export function getBillDetailApi(id: number): Promise<WaterElecBillVO> {
  return get<WaterElecBillVO>('/waterElec/bill/detail', { id })
}
export function generateBillApi(data: WaterElecBillGenerateDTO): Promise<number> {
  return post<number>('/waterElec/bill/generate', data)
}
export function syncWaterElecBillApi(id: number): Promise<string> {
  return post<string>('/waterElec/bill/sync/' + id, null)
}
export function batchSyncWaterElecBillApi(ids: number[]): Promise<number> {
  return post<number>('/waterElec/bill/batchSync', ids)
}

/* ------------------------------ 缴费记录 water_elec_pay_record ------------------------------ */

export interface WaterElecPayQueryDTO {
  pageNum: number
  pageSize: number
  recordType?: number
  payType?: number
  billId?: number
}

export interface WaterElecPayRecordVO {
  id: number
  companyId: number
  billId: number
  stallId: number
  merchantId?: number
  payAmount: number
  payType: number
  payTypeText?: string
  recordType: number
  recordTypeText?: string
  refundStatus: number
  refundTime?: string
  refundRecordId?: number
  remark?: string
  flowNo?: string
  createByName?: string
  createTime?: string
}

export interface WaterElecPayDTO {
  billId: number
  payType: number
  requestId: string
  remark?: string
}

export interface WaterElecRefundDTO {
  payRecordId: number
  requestId: string
  remark?: string
}

export function getPayPageApi(params: WaterElecPayQueryDTO): Promise<PageResult<WaterElecPayRecordVO>> {
  return get<PageResult<WaterElecPayRecordVO>>('/waterElec/pay/page', params)
}
export function payBillApi(data: WaterElecPayDTO): Promise<null> {
  return post<null>('/waterElec/pay/pay', data)
}
export function refundPayApi(data: WaterElecRefundDTO): Promise<null> {
  return post<null>('/waterElec/pay/refund', data)
}