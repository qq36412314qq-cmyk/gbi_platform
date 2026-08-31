<template>
  <template v-for="menu in menus" :key="menu.path">
    <!-- 有可见子菜单 -> 展开目录（支持任意层级递归） -->
    <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
      <template #title>
        <el-icon v-if="menu.meta?.icon"><component :is="menu.meta.icon" /></el-icon>
        <span>{{ menu.meta?.title }}</span>
      </template>
      <SidebarItem :menus="menu.children" />
    </el-sub-menu>
    <!-- 叶子菜单 -> 直接跳转 -->
    <el-menu-item v-else :index="menu.path">
      <el-icon v-if="menu.meta?.icon"><component :is="menu.meta.icon" /></el-icon>
      <span>{{ menu.meta?.title }}</span>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
/**
 * SidebarItem 递归菜单项：支持三级及以上菜单层级
 * 接收已过滤权限、已拼接完整路径的菜单列表
 */
import type { RouteRecordRaw } from 'vue-router'

defineProps<{ menus: RouteRecordRaw[] }>()

defineOptions({ name: 'SidebarItem' })
</script>