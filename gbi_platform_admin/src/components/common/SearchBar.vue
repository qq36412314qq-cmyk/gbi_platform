<template>
  <div class="g-search-bar g-card">
    <el-form :model="model" inline :label-width="labelWidth" @submit.prevent>
      <slot :model="model" />
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
/**
 * SearchBar 搜索栏组件：包裹 el-form inline，默认 slot 渲染搜索项
 * 用法：<SearchBar :model="query" @search="loadData" @reset="resetQuery">
 *         <el-form-item label="名称"><el-input v-model="query.name" /></el-form-item>
 *       </SearchBar>
 */
import { ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    model: Record<string, unknown>
    labelWidth?: string
  }>(),
  { labelWidth: '80px' }
)

const emit = defineEmits<{ (e: 'search'): void; (e: 'reset'): void }>()

const defaultModel = ref<Record<string, unknown>>({ ...props.model })

function handleSearch(): void {
  emit('search')
}

function handleReset(): void {
  // 重置为初始值（保留分页字段）
  Object.keys(props.model).forEach((key) => {
    if (['pageNum', 'pageSize'].includes(key)) {
      return
    }
    props.model[key] = defaultModel.value[key] ?? undefined
  })
  emit('reset')
}
</script>