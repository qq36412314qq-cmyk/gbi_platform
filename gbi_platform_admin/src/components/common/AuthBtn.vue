<template>
  <el-button
    v-if="userStore.hasPermission(permission)"
    v-bind="$attrs"
    :type="type"
    :size="size"
    :icon="icon"
    :loading="loading"
    :disabled="disabled"
    @click="handleClick"
  >
    <slot />
  </el-button>
</template>

<script setup lang="ts">
/**
 * AuthBtn 权限按钮组件（强制统一，禁止 v-if 手动判断权限）
 * 用法：<AuthBtn permission="user:add" type="primary" @click="openDialog">新增用户</AuthBtn>
 */
import { useUserStore } from '@/store/user'

withDefaults(
  defineProps<{
    permission?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default'
    size?: 'large' | 'default' | 'small'
    icon?: string
    loading?: boolean
    disabled?: boolean
  }>(),
  {
    type: 'default',
    size: 'default',
    loading: false,
    disabled: false
  }
)

const emit = defineEmits<{ (e: 'click', event: MouseEvent): void }>()

const userStore = useUserStore()

function handleClick(event: MouseEvent): void {
  emit('click', event)
}
</script>

<script lang="ts">
export default {
  name: 'AuthBtn',
  inheritAttrs: false
}
</script>