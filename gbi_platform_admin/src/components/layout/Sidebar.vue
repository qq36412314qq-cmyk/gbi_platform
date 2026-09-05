<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    :unique-opened="true"
    router
    background-color="transparent"
    class="g-sidebar-menu"
  >
    <SidebarItem :menus="visibleMenus" />
  </el-menu>
</template>

<script setup lang="ts">
/**
 * Sidebar 侧边栏：基于 constantRoutes 完整目录渲染，支持多一级模块切换
 * 目录级：中台管理 / 物业管理 / 财务管理；子菜单按权限递归过滤，无可见子菜单的目录自动隐藏
 * 支持三级菜单（如 物业管理 -> 租赁管理 -> 铺位管理），由 SidebarItem 递归渲染
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { constantRoutes } from '@/router'
import { useUserStore } from '@/store/user'
import SidebarItem from './SidebarItem.vue'

defineProps<{ isCollapse: boolean }>()

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

/**
 * 递归过滤无权限菜单并拼接完整路径
 * @param routes 当前层级路由
 * @param parentPath 父级完整路径（子路由为相对路径时拼接）
 */
function filterMenus(routes: RouteRecordRaw[], parentPath: string): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []
  for (const r of routes) {
    const perm = r.meta?.permission as string | undefined
    if (perm && !userStore.hasPermission(perm)) {
      continue
    }
    const fullPath = r.path.startsWith('/') ? r.path : `${parentPath}/${r.path}`
    const children = r.children ? filterMenus(r.children, fullPath) : []
    result.push({ ...r, path: fullPath, children })
  }
  return result
}

/**
 * 一级目录渲染全部可见模块（排除根路径/登录/错误页），子级递归过滤
 */
const visibleMenus = computed(() => {
  return constantRoutes
    .filter((r) => {
      return r.path.startsWith('/') && r.path !== '/' && r.path !== '/login' && r.path !== '/403' && r.meta?.title
    })
    .map((r) => ({ ...r, children: filterMenus(r.children || [], r.path) }))
    .filter((r) => r.children.length > 0) as RouteRecordRaw[]
})
</script>

<style scoped>
.g-sidebar-menu {
  border-right: none;
}

.g-sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}
</style>