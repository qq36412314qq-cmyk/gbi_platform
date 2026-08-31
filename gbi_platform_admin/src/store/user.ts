/**
 * 用户状态：token / 用户信息 / 权限集合
 * JWT 登录后加载全部权限，权限变更实时生效
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getStorage, removeStorage, setStorage } from '@/utils/storage'
import { loginApi, getInfoApi, logoutApi } from '@/api/base'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  companyId: number
  username: string
  realName: string
  phone?: string
  avatar?: string
  userType?: number
  roles?: string[]
  permissions?: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getStorage<string>('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  /** 登录 */
  async function login(params: LoginParams): Promise<void> {
    const data = await loginApi(params)
    token.value = data.token
    setStorage('token', data.token)
  }

  /** 拉取当前用户信息与权限 */
  async function fetchUserInfo(): Promise<UserInfo> {
    const data = await getInfoApi()
    userInfo.value = data
    permissions.value = data.permissions || []
    return data
  }

  /** 是否有指定权限 */
  function hasPermission(permission?: string): boolean {
    if (!permission) {
      return true
    }
    // 超级管理员放行全部
    if (permissions.value.includes('*:*:*')) {
      return true
    }
    return permissions.value.includes(permission)
  }

  /** 退出登录 */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch {
      // 退出接口失败不阻塞本地清理
    }
    resetState()
  }

  /** 清空本地登录态 */
  function resetState(): void {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    removeStorage('token')
  }

  return { token, userInfo, permissions, login, fetchUserInfo, hasPermission, logout, resetState }
})