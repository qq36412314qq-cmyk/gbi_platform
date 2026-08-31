<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :destroy-on-close="destroyOnClose"
    :close-on-click-modal="false"
    @update:model-value="(val: boolean) => emit('update:modelValue', val)"
    @closed="handleClosed"
  >
    <slot />
    <template #footer>
      <slot name="footer">
        <el-button @click="emit('update:modelValue', false)">取 消</el-button>
        <el-button type="primary" :loading="loading" @click="emit('confirm')">确 定</el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * CommonDialog 统一弹窗组件：关闭自动清空表单与校验提示
 */
withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    width?: string
    loading?: boolean
    destroyOnClose?: boolean
  }>(),
  {
    title: '操作',
    width: '600px',
    loading: false,
    destroyOnClose: true
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'closed'): void
}>()

function handleClosed(): void {
  emit('closed')
}
</script>