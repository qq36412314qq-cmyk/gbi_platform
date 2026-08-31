/**
 * 权限状态：菜单路由生成（后端菜单动态生成路由，预留）
 * 一期采用静态路由 + meta.permission 拦截，二期切换动态路由
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface MenuRoute {
  id: number
  parentId: number
  menuName: string
  permission?: string
  path?: string
  icon?: string
  sortOrder: number
  menuType: number
  visible: number
  children?: MenuRoute[]
}

export const usePermissionStore = defineStore('permission', () => {
  const menus = ref<MenuRoute[]>([])

  function setMenus(list: MenuRoute[]): void {
    menus.value = list
  }

  function clearMenus(): void {
    menus.value = []
  }

  return { menus, setMenus, clearMenus }
})