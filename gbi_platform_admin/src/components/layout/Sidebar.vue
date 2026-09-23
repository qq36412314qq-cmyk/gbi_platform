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
 * Sidebar 侧边栏：基于 permissionStore.menuTree 渲染（完整树形结构）
 * v2.11 动态路由扁平化后，dynamicRoutes 没有 children，
 *      所以侧边栏必须从 menuTree（原始 MenuVO 树）转换渲染
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { usePermissionStore } from '@/store/permission'
import { useUserStore } from '@/store/user'
import type { MenuVO } from '@/api/org'
import SidebarItem from './SidebarItem.vue'

defineProps<{ isCollapse: boolean }>()

const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const activeMenu = computed(() => route.path)

/**
 * 把后端 MenuVO 树 转成 SidebarItem 能消费的 RouteRecordRaw 树
 * 过滤规则：
 *   - menuType=3（按钮）：跳过，不渲染在侧边栏
 *   - visible=0：隐藏，跳过
 *   - 无权限（permission 存在且 userStore 没有）：跳过
 *   - menuType=2（页面）但 path 为空：跳过（没法跳转）
 *   - menuType=1（目录）子节点全被过滤：整个目录跳过
 */
function convertMenuTree(nodes: MenuVO[]): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []
  for (const n of nodes) {
    // 按钮权限不渲染
    if (n.menuType === 3) continue
    // 隐藏菜单不渲染
    if (n.visible === 0) continue
    // 权限校验
    if (n.permission && !userStore.hasPermission(n.permission)) continue
    // 页面菜单必须有 path
    if (n.menuType === 2 && (!n.path || !n.path.startsWith('/'))) continue

    // 递归处理子节点
    const children = n.children ? convertMenuTree(n.children) : []

    // 目录节点：如果子节点全被过滤，整个目录也跳过
    if (n.menuType === 1 && children.length === 0) continue

    const route: RouteRecordRaw = {
      path: n.path || '',
      meta: {
        title: n.menuName,
        icon: n.icon || undefined,
        permission: n.permission || undefined,
      },
    }
    if (children.length > 0) {
      route.children = children
    }
    result.push(route)
  }
  return result
}

/**
 * 从 permissionStore.menuTree 读取原始树形菜单
 * 如果 menuTree 还没加载（极端场景），返回空数组（Layout 仍正常渲染，只是菜单为空）
 */
const visibleMenus = computed(() => {
  const tree = permissionStore.menuTree
  if (!tree || tree.length === 0) return []
  return convertMenuTree(tree)
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