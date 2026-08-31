<template>
  <div class="g-navbar">
    <div class="g-navbar-left">
      <el-icon class="g-collapse-btn" @click="emit('toggleCollapse')">
        <Expand v-if="isCollapse" />
        <Fold v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="g-navbar-right">
      <!-- 暗黑模式切换 -->
      <el-tooltip content="深浅主题切换" placement="bottom">
        <el-icon class="g-navbar-icon" @click="toggleDark">
          <Moon v-if="!isDark" />
          <Sunny v-else />
        </el-icon>
      </el-tooltip>
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="g-user-info">
          <el-avatar :size="30" :src="userStore.userInfo?.avatar">{{ avatarText }}</el-avatar>
          <span class="g-user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="theme">主题配置</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Navbar 顶部导航：面包屑 / 主题切换 / 用户信息下拉
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Expand, Fold, Moon, Sunny, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'

defineProps<{ isCollapse: boolean }>()
const emit = defineEmits<{ (e: 'toggleCollapse'): void }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isDark = computed(() => themeStore.theme.darkMode === 1)
const avatarText = computed(() => (userStore.userInfo?.realName || '用').slice(0, 1))

const breadcrumbs = computed(() => {
  return route.matched
    .filter((item) => item.meta?.title)
    .map((item) => ({ path: item.path, title: item.meta?.title as string }))
})

/** 深浅主题切换 */
function toggleDark(): void {
  themeStore.applyTheme({ ...themeStore.theme, darkMode: isDark.value ? 0 : 1 })
}

/** 下拉命令 */
async function handleCommand(command: string): Promise<void> {
  if (command === 'theme') {
    router.push('/platform/theme')
    return
  }
  if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    router.replace('/login')
  }
}
</script>

<style scoped>
.g-navbar {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border-light);
}

.g-navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.g-collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-regular);
}

.g-navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.g-navbar-icon {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-regular);
}

.g-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--color-text-primary);
  outline: none;
}

.g-user-name {
  font-size: var(--font-size-base);
}
</style>