<template>
  <div class="g-page-wrap flow-instance-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">流程实例</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="业务类型">
        <el-input v-model="query.bizType" placeholder="如 contract / contract_discount" clearable style="width: 220px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.instanceStatus" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(text, value) in instanceStatusMap" :key="value" :label="text" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源单据">
        <el-input v-model="query.sourceId" placeholder="来源单据ID" clearable style="width: 140px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="instanceNo" label="实例编号" width="180" />
        <el-table-column prop="defName" label="流程名称" width="160" show-overflow-tooltip />
        <el-table-column prop="title" label="审批标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="applyUserName" label="申请人" width="100" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.instanceStatus ?? 0)">{{ row.instanceStatusText || instanceStatusMap[row.instanceStatus ?? 0] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="150" />
        <el-table-column prop="finishTime" label="完成时间" width="150" />
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 详情弹窗（共用组件） -->
    <InstanceDetailDialog v-model="detailVisible" :instance-id="detailInstanceId" />
  </div>
</template>

<script setup lang="ts">
/**
 * 流程实例页：全量流程实例查询（集团看全量，子公司仅本公司），查看审批任务与流转轨迹
 */
import { ref } from 'vue'
import { getFlowInstancePageApi, type FlowInstanceVO } from '@/api/flow'
import { useTable } from '@/hooks/useTable'
import InstanceDetailDialog from './instanceDetailDialog.vue'

const instanceStatusMap: Record<number, string> = { 0: '审批中', 1: '通过', 2: '驳回', 3: '撤回', 4: '终止' }

function statusTagType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2 || status === 4) return 'danger'
  return 'info'
}

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FlowInstanceVO>(getFlowInstancePageApi, {
  bizType: undefined,
  instanceStatus: undefined,
  sourceId: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const detailInstanceId = ref(0)

function openDetail(instanceId: number): void {
  detailInstanceId.value = instanceId
  detailVisible.value = true
}
</script>