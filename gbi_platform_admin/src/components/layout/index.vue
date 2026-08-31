<template>
  <el-container class="g-layout">
    <!-- 侧边栏布局 -->
    <el-aside v-if="layoutMode === 'side'" :width="isCollapse ? '64px' : '220px'" class="g-layout-aside">
      <div class="g-layout-logo">
        <el-icon :size="22" color="#fff"><Platform /></el-icon>
        <span v-show="!isCollapse" class="g-layout-logo-text">集团管控平台</span>
      </div>
      <el-scrollbar>
        <Sidebar :is-collapse="isCollapse" />
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="g-layout-header" height="auto">
        <!-- 顶部布局：导航条上方展示 logo + 菜单 -->
        <div v-if="layoutMode === 'top'" class="g-layout-top">
          <div class="g-layout-logo g-layout-logo-top">
            <el-icon :size="22" color="#fff"><Platform /></el-icon>
            <span class="g-layout-logo-text">集团管控平台</span>
          </div>
          <el-scrollbar class="g-layout-top-menu">
            <Sidebar :is-collapse="false" />
          </el-scrollbar>
        </div>
        <Navbar :is-collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
      </el-header>

      <el-main class="g-layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
/**
 * 主布局：侧边 / 顶部双模式（跟随 sys_ui_theme.layout_mode）
 */
import { computed, ref } from 'vue'
import { Platform } from '@element-plus/icons-vue'
import { useThemeStore } from '@/store/theme'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

const themeStore = useThemeStore()
const isCollapse = ref(false)
const layoutMode = computed(() => themeStore.theme.layoutMode)
</script>

<style scoped>
.g-layout {
  height: 100%;
}

.g-layout-aside {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border-light);
  transition: width 0.2s;
  overflow: hidden;
}

.g-layout-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--color-primary);
  flex-shrink: 0;
}

.g-layout-logo-text {
  color: #fff;
  font-size: var(--font-size-xl);
  font-weight: 600;
  white-space: nowrap;
}

.g-layout-header {
  padding: 0;
  background: var(--color-bg-card);
}

.g-layout-top {
  display: flex;
  align-items: center;
  background: var(--color-primary);
}

.g-layout-logo-top {
  background: transparent;
  padding: 0 24px;
  flex-shrink: 0;
}

.g-layout-top-menu {
  flex: 1;
}

.g-layout-top-menu :deep(.el-menu) {
  --el-menu-text-color: #fff;
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.12);
  --el-menu-bg-color: transparent;
}

.g-layout-main {
  padding: 0;
  background: var(--color-bg-page);
  overflow-y: auto;
}
</style>