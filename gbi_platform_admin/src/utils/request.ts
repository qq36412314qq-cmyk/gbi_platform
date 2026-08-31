/**
 * axios 统一请求拦截器
 * - 请求头自动携带 token
 * - 统一返回 JSON 结构 { code, msg, data }
 * - 全局错误拦截：401 自动登出、403 无权限提示、500 统一提示
 */
import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getStorage, removeStorage } from './storage'

/** 统一返回体结构（与后端 Result 对齐） */
export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
}

/** 分页返回结构（与后端分页 VO 对齐） */
export interface PageResult<T = unknown> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 请求拦截：携带 token
service.interceptors.request.use(
  (config) => {
    const token = getStorage<string>('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：统一处理业务码
service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult
    // 兼容后端直接返回非标准结构（如二进制流）
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    if (res.code === 200) {
      return response
    }
    // 401 未登录：清空登录态并跳转登录页
    if (res.code === 401) {
      removeStorage('token')
      ElMessage.error(res.msg || '登录已失效，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new Error(res.msg || '未登录'))
    }
    // 403 无权限
    if (res.code === 403) {
      ElMessage.error(res.msg || '无操作权限')
      return Promise.reject(new Error(res.msg || '无权限'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeStorage('token')
      window.location.href = '/login'
    }
    // 无 response：网络层错误（后端未启动 / 代理断开 / 超时），给出明确提示而非无限等待
    if (!error.response) {
      const isTimeout = error.code === 'ECONNABORTED' || /timeout/i.test(error.message || '')
      ElMessage.error(isTimeout ? '请求超时，请稍后重试' : '无法连接后端服务，请确认服务已启动')
      return Promise.reject(error)
    }
    ElMessage.error(error.response.data?.msg || '请求失败，请稍后重试')
    return Promise.reject(error)
  }
)

/**
 * 统一请求方法：返回 data 数据体
 */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request<ApiResult<T>>(config).then((res) => res.data.data as T)
}

/** GET 请求 */
export function get<T = unknown>(url: string, params?: object): Promise<T> {
  return request<T>({ url, method: 'get', params })
}

/** POST 请求（新增/编辑/删除/缴费等统一 POST） */
export function post<T = unknown>(url: string, data?: object): Promise<T> {
  return request<T>({ url, method: 'post', data })
}

/** 文件上传（multipart/form-data） */
export function upload<T = unknown>(url: string, file: File, extra?: Record<string, unknown>): Promise<T> {
  const formData = new FormData()
  formData.append('file', file)
  if (extra) {
    Object.entries(extra).forEach(([key, value]) => formData.append(key, String(value)))
  }
  return request<T>({ url, method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

export default service