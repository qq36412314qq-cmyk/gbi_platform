<template>
  <div class="g-table-card">
    <!-- 默认 slot：el-table 列定义 -->
    <slot />
    <div class="g-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * TablePage 表格分页容器：自带分页，slot 放置 el-table
 * 用法：<TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize"
 *                  :total="total" @refresh="loadData">
 *         <el-table :data="records">...</el-table>
 *       </TablePage>
 */
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    pageNum: number
    pageSize: number
    total: number
  }>(),
  { total: 0 }
)

const emit = defineEmits<{
  (e: 'update:pageNum', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'refresh'): void
}>()

const currentPage = computed({
  get: () => props.pageNum,
  set: (value) => emit('update:pageNum', value)
})

const pageSize = computed({
  get: () => props.pageSize,
  set: (value) => emit('update:pageSize', value)
})

function handleSizeChange(): void {
  // 切换每页条数后回到第一页
  emit('update:pageNum', 1)
  emit('refresh')
}

function handleCurrentChange(): void {
  emit('refresh')
}
</script>