<template>
  <router-view v-slot="{ Component, route }">
    <!-- 登录/错误/打印页面不需要 Layout 包裹 -->
    <template v-if="isPublicRoute(route.path)">
      <component :is="Component" />
    </template>
    <!-- 其他所有页面都用 Layout 包裹（侧边栏 + header + 内容区） -->
    <template v-else>
      <Layout>
        <component :is="Component" />
      </Layout>
    </template>
  </router-view>
</template>

<script setup lang="ts">
import Layout from '@/components/layout/index.vue'

function isPublicRoute(path: string): boolean {
  return (
    path === '/login' ||
    path === '/403' ||
    path.startsWith('/finance/payOrderPrint')
  )
}
</script>
