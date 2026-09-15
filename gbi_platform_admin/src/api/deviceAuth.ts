/**
 * 客户端设备校验相关接口
 */
import { get, post } from '@/utils/request'

/** 硬件信息 */
export interface HardwareInfo {
  motherboardSn: string
  cpuId: string
  diskSn?: string
  timestamp: number
  nonce: string
}

/** 授权设备 */
export interface DeviceAuthVO {
  id: number
  motherboardSn: string
  cpuId: string
  diskSn?: string
  deviceName?: string
  authorizedBy?: number
  expireTime?: string
  updateTime?: string
  status: number
  createTime?: string
}

/** 获取本机硬件信息（直连Go YOIREI Device Verification，绕过后端代理） */
export function getHardwareInfoApi(): Promise<HardwareInfo> {
  console.log('获取硬件信息')
  return fetch('http://127.0.0.1:8765/api/hardware/info')
    .then(res => {
      console.log(res)
      if (!res.ok) throw new Error('Go YOIREI Device Verification未响应')
      return res.json()
    })
}

/** 硬件信息签名（由后端签名） */
export function signHardwareApi(data: HardwareInfo): Promise<{ signature: string }> {
  return post<{ signature: string }>('/sys/device/hardware/sign', data)
}

/* ------------------------------ 授权设备管理 ------------------------------ */

/** 授权设备分页 */
export function getDeviceAuthPageApi(params: {
  pageNum: number
  pageSize: number
  motherboardSn?: string
  cpuId?: string
}): Promise<any> {
  return get<any>('/sys/device/auth/page', params)
}

/** 新增授权设备 */
export function addDeviceAuthApi(data: Partial<DeviceAuthVO>): Promise<null> {
  return post<null>('/sys/device/auth/add', data)
}

/** 编辑授权设备 */
export function updateDeviceAuthApi(data: DeviceAuthVO): Promise<null> {
  return post<null>('/sys/device/auth/update', data)
}

/** 删除授权设备 */
export function deleteDeviceAuthApi(id: number): Promise<null> {
  return post<null>('/sys/device/auth/delete', { id })
}

/** 切换设备启用/禁用状态 */
export function toggleDeviceAuthApi(id: number): Promise<null> {
  return post<null>(`/sys/device/auth/toggle/${id}`)
}
