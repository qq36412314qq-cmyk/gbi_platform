<template>
  <div class="g-page-wrap audit-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">审计日志</span>
      <div>
        <AuthBtn permission="audit:export" type="primary" @click="handleExport">导出日志</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="操作人">
        <el-input v-model="query.operUserName" placeholder="请输入操作人" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="操作模块">
        <el-input v-model="query.operModule" placeholder="如 org/finance" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="query.operType" placeholder="全部" clearable style="width: 130px">
          <el-option label="新增" value="新增" />
          <el-option label="编辑" value="编辑" />
          <el-option label="删除" value="删除" />
          <el-option label="导出" value="导出" />
          <el-option label="审核" value="审核" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 340px"
        />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区（只读） -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="operUserName" label="操作人" min-width="100" />
        <el-table-column prop="operModule" label="操作模块" min-width="110">
          <template #default="{ row }">
            <el-tag size="small" type="primary" effect="plain">{{ row.operModule }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operType" label="操作类型" width="90" align="center" />
        <el-table-column prop="operIp" label="IP地址" min-width="120" />
        <el-table-column prop="bizId" label="业务单据ID" min-width="110">
          <template #default="{ row }">
            <span v-if="row.bizId">{{ row.bizId }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditOperId" label="复核人ID" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.auditOperId">{{ row.auditOperId }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 详情弹窗：变更前后 JSON 快照 -->
    <CommonDialog v-model="detailVisible" title="日志详情" width="720px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="操作人">{{ currentLog?.operUserName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog?.operModule }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog?.operType }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog?.operIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务单据ID">{{ currentLog?.bizId || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">变更前快照</el-divider>
      <pre class="g-audit-json">{{ currentLog?.beforeJson || '无' }}</pre>
      <el-divider content-position="left">变更后快照</el-divider>
      <pre class="g-audit-json">{{ currentLog?.afterJson || '无' }}</pre>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 审计日志页：sys_audit_log 只读查询 + 导出（永久归档，不可删除）
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAuditLogPageApi,
  exportAuditLogApi,
  type AuditLogVO,
  type AuditLogQueryDTO
} from '@/api/sys'
import { useTable } from '@/hooks/useTable'
import { formatDateTime } from '@/utils/format'

/* ---------------- 分页查询 ---------------- */
const timeRange = ref<[string, string] | null>(null)

const { query, records, total, loading, loadData, resetQuery } = useTable<AuditLogVO>(getAuditLogPageApi, {
  operUserName: undefined,
  operModule: undefined,
  operType: undefined,
  startTime: undefined,
  endTime: undefined
})

function handleReset(): void {
  timeRange.value = null
  resetQuery()
  loadData()
}

/* ---------------- 导出（同一筛选条件） ---------------- */
async function handleExport(): Promise<void> {
  const params: AuditLogQueryDTO = {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    operUserName: query.operUserName as string | undefined,
    operModule: query.operModule as string | undefined,
    operType: query.operType as string | undefined,
    startTime: timeRange.value?.[0],
    endTime: timeRange.value?.[1]
  }
  await exportAuditLogApi(params)
  ElMessage.success('导出任务已提交，请稍后下载')
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const currentLog = ref<AuditLogVO | null>(null)

function openDetail(row: AuditLogVO): void {
  currentLog.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.g-audit-json {
  max-height: 240px;
  overflow: auto;
  padding: 8px;
  background: var(--color-bg-page);
  border-radius: var(--card-radius);
  font-size: var(--font-size-xs);
  color: var(--color-text-regular);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>