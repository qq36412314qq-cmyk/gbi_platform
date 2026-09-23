/**
 * 权限状态：动态路由生成 + 权限管理
 * 自 v2.11 起：登录时拉取授权菜单树，动态注册路由
 * 一期兼容：保留静态路由兜底
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuVO } from '@/api/org'
import { generateRoutes } from '@/utils/routerHelper'
import { getAuthorizedMenuTreeApi } from '@/api/org'

export interface MenuRoute extends MenuVO {
  children?: MenuRoute[]
}

export const usePermissionStore = defineStore('permission', () => {
  // 动态路由缓存（登录成功后生成）
  const dynamicRoutes = ref<RouteRecordRaw[]>([])
  // 原始菜单树（用于侧边栏渲染）
  const menuTree = ref<MenuRoute[]>([])
  // 路由是否已注册到 Vue Router（避免重复注册，也用于守卫判断）
  const routesRegistered = ref(false)

  /**
   * 根据菜单树生成路由并注册到 router
   * 由 router/index.ts 的 beforeEach 调用
   * 幂等：如果 routesRegistered=true 则跳过
   *       如果 menuTree 有数据但 routesRegistered=false，直接用已有菜单树重新注册
   */
  async function setupDynamicRoutes(router: any): Promise<void> {
    // 拉菜单树：已有则复用，没有则从后端拉
    let menuList = menuTree.value as MenuVO[]
    if (!menuList || menuList.length === 0) {
      menuList = await getAuthorizedMenuTreeApi()
    }

    if (!menuList || menuList.length === 0) {
      console.warn('[permission] 无菜单数据，跳过动态路由注册')
      return
    }

    // 缓存菜单树（侧边栏用）
    if (menuTree.value.length === 0) {
      menuTree.value = menuList as MenuRoute[]
    }

    // 生成路由对象
    const routes = generateRoutes(menuList)
    dynamicRoutes.value = routes

    // 扁平化路由直接注册为顶级路由（App.vue 里手动包裹 Layout）
    // 每个路由 path 都是完整绝对路径如 '/org'，component 就是页面组件
    // 同 name 的路由会被 Vue Router 自动替换，不会重复
    for (const pageRoute of routes) {
      router.addRoute(pageRoute)
    }

    console.log('[permission] 动态路由注册完成，共', routes.length, '条页面路由')
  }

  /** 清空动态路由缓存（登出时调用） */
  function clearRoutes(): void {
    dynamicRoutes.value = []
    menuTree.value = []
    routesRegistered.value = false
  }

  return { dynamicRoutes, menuTree, routesRegistered, setupDynamicRoutes, clearRoutes }
})
