/**
 * ⚠️ 已废弃：静态路由配置（v2.11 起改为动态路由）
 *
 * 【迁移说明】
 * 自 v2.11 升级后，路由改为从 sys_menu 表动态生成，本文件仅作兼容保留。
 * 新开发请遵循以下规范：
 *
 * 1. 新增页面：在 sys_menu 表中插入 menu_type=2 的记录，设置正确的 permission 和 path
 * 2. 新增目录：插入 menu_type=1 的记录，parent_id 指向父级目录 id
 * 3. 后台菜单管理界面可直接操作，无需修改前端代码
 * 4. 后端接口：GET /org/menu/authorized-tree 返回当前用户权限菜单树
 * 5. 前端自动根据菜单树递归生成路由并注册到 router
 *
 * 【静态路由仅保留】
 * - /login、/403、/:pathMatch(.*)* 等固定路由仍需在此声明
 * - dashboard 页面无权限要求，可保留在静态路由中
 *
 * 【严禁操作】
 * - 禁止在此文件中新增业务路由，一律通过 sys_menu 表配置
 * - 禁止修改此文件的 constantRoutes 来增加页面权限
 *
 * @deprecated 自 v2.11 起使用动态路由，此文件仅保留静态路由兜底
 */
/**
 * 路由配置：与后台菜单、权限标识一一对应
 * meta 固定三元组：title / icon / permission
 *
 * ⚠️ 注意：业务路由已从静态配置迁移至 sys_menu 表动态生成
 * 详见上方废弃说明
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { useThemeStore } from '@/store/theme'
import { getThemeApi } from '@/api/sys'

/** 静态路由（仅保留固定路由，业务路由由动态生成） */
const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '工作台', icon: 'HomeFilled', permission: '', isCache: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '403' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/finance/payOrderPrint/:id',
    name: 'FinancePayOrderPrint',
    component: () => import('@/views/finance/financePayPrint.vue'),
    meta: { title: '缴费单打印' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 暴露静态路由（供 Sidebar 等组件在动态路由未就绪时使用兜底）
export const staticRoutesList = staticRoutes

// 白名单：无需登录
const WHITE_LIST = ['/login', '/403', '/finance/payOrderPrint']

/** 全局前置守卫：无 token 跳登录 / 无权限跳 403 / 动态注册路由 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  // 设置页面标题
  document.title = `${to.meta?.title ? to.meta.title + ' - ' : ''}集团业务一体化管控平台`

  const hasToken = !!userStore.token
  if (!hasToken) {
    if (WHITE_LIST.some(p => to.path.startsWith(p))) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  // 已登录访问登录页 -> 跳首页
  if (to.path === '/login') {
    next('/dashboard')
    return
  }

  // 确保用户信息已加载
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
      // 拉取主题配置
      try {
        const themeConfig = await getThemeApi()
        useThemeStore().applyTheme(themeConfig as never)
      } catch {
        useThemeStore().applyTheme()
      }
    } catch (err) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 401) {
        userStore.resetState()
        permissionStore.clearRoutes()
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        return
      }
      console.warn('[Router] fetchUserInfo 失败:', err)
    }
  }

  // 确保动态路由已注册（每次导航都尝试，幂等安全）
  let routesChanged = false
  try {
    const beforeCount = router.getRoutes().length
    await permissionStore.setupDynamicRoutes(router)
    const afterCount = router.getRoutes().length
    routesChanged = afterCount > beforeCount
  } catch (err) {
    console.warn('[Router] setupDynamicRoutes 失败:', err)
  }

  // 如果注册了新路由，必须重新触发一次导航让 Vue Router 重新匹配
  // 因为当前导航的路由匹配已经完成，addRoute 注册的新路由不会被当前导航使用
  if (routesChanged) {
    next({ path: to.fullPath, replace: true })
    return
  }

  // 页面权限校验（meta.permission 为空视为公共页面）
  const permission = to.meta?.permission as string | undefined
  if (permission && !userStore.hasPermission(permission)) {
    next('/403')
    return
  }
  next()
})

export default router
