/**
 * useAuth：权限判断组合逻辑
 * 页面内判断：const { hasPermission } = useAuth()
 */
import { useUserStore } from '@/store/user'

export function useAuth() {
  const userStore = useUserStore()

  function hasPermission(permission?: string): boolean {
    return userStore.hasPermission(permission)
  }

  return { hasPermission }
}