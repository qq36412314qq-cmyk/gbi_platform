<template>
  <div class="g-page-wrap">
    <div class="g-card">
      <div class="g-dash-header">
        <div>
          <div class="g-dash-welcome">你好，{{ userStore.userInfo?.realName }}，欢迎回来</div>
          <div class="g-dash-sub">今天是 {{ today }}，集团业务一体化管控平台为您服务</div>
        </div>
        <el-tag type="primary" effect="plain">集团中台</el-tag>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="g-mt-12">
      <el-col v-for="item in stats" :key="item.label" :span="6">
        <div class="g-card g-stat-card">
          <div class="g-stat-value">{{ item.value }}</div>
          <div class="g-stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <div class="g-card g-mt-12">
      <div class="g-section-title">快捷入口</div>
      <div class="g-quick-list">
        <div
          v-for="entry in quickEntries"
          :key="entry.path"
          class="g-quick-item"
          @click="router.push(entry.path)"
        >
          <el-icon :size="24" color="var(--color-primary)"><component :is="entry.icon" /></el-icon>
          <span>{{ entry.title }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { OfficeBuilding, User, Avatar, Menu, Collection, Tools, Brush, Document, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useAuth } from '@/hooks/useAuth'

const router = useRouter()
const userStore = useUserStore()
const { hasPermission } = useAuth()

const today = dayjs().format('YYYY年MM月DD日')

/** 统计卡片（一期静态占位，二期接入统计接口） */
const stats = computed(() => [
  { label: '组织数量', value: '-' },
  { label: '用户数量', value: '-' },
  { label: '角色数量', value: '-' },
  { label: '待复核权限', value: '-' }
])

/** 快捷入口（按权限过滤） */
const quickEntries = computed(() => {
  const all = [
    { title: '组织管理', path: '/platform/org', icon: OfficeBuilding, permission: 'org:list' },
    { title: '用户管理', path: '/platform/user', icon: User, permission: 'user:list' },
    { title: '角色管理', path: '/platform/role', icon: Avatar, permission: 'role:list' },
    { title: '菜单权限', path: '/platform/menu', icon: Menu, permission: 'menu:list' },
    { title: '字典管理', path: '/platform/dict', icon: Collection, permission: 'dict:list' },
    { title: '参数配置', path: '/platform/config', icon: Tools, permission: 'config:list' },
    { title: 'UI主题', path: '/platform/theme', icon: Brush, permission: 'theme:list' },
    { title: '审计日志', path: '/platform/audit', icon: Document, permission: 'audit:list' },
    { title: '权限复核', path: '/platform/permissionAudit', icon: Key, permission: 'permission:audit:list' }
  ]
  return all.filter((item) => hasPermission(item.permission))
})
</script>

<style scoped>
.g-dash-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.g-dash-welcome {
  font-size: var(--font-size-xxl);
  font-weight: 600;
}

.g-dash-sub {
  margin-top: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.g-stat-card {
  text-align: center;
  padding: 20px 16px;
}

.g-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
}

.g-stat-label {
  margin-top: 6px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.g-section-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  margin-bottom: 12px;
}

.g-quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.g-quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 96px;
  padding: 14px 0;
  border-radius: var(--card-radius);
  cursor: pointer;
  color: var(--color-text-regular);
  font-size: var(--font-size-sm);
}

.g-quick-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-primary);
}
</style>