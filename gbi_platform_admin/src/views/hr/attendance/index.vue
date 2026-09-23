<template>
  <div class="g-page-wrap">
    <!-- 月度统计卡片 -->
    <el-row :gutter="12" class="summary-row" v-if="selectedMonth">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">应出勤天数</div>
          <div class="summary-value">{{ summaryData.workDays ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">实际出勤天数</div>
          <div class="summary-value">{{ summaryData.actualDays ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">迟到总次数</div>
          <div class="summary-value late">{{ summaryData.lateCount ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">迟到总分钟</div>
          <div class="summary-value late">{{ summaryData.totalLateMinutes ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="g-page-header">
      <span class="g-page-title">考勤管理</span>
      <div class="header-actions">
        <AuthBtn permission="hr:attendance:export" @click="handleExport" :loading="exportLoading">导出Excel</AuthBtn>
        <AuthBtn permission="hr:attendance:sync" type="primary" @click="handleSync" :loading="syncLoading">同步打卡</AuthBtn>
      </div>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="考勤月份">
        <el-date-picker v-model="query.attendanceMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
      </el-form-item>
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width:140px">
          <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="打卡状态">
        <el-select v-model="query.clockType" placeholder="全部" clearable style="width:100px">
          <el-option label="正常" :value="1" /><el-option label="迟到" :value="2" />
          <el-option label="早退" :value="3" /><el-option label="缺卡" :value="4" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe size="small">
        <el-table-column prop="attendanceDay" label="日期" width="110" align="center" sortable />
        <el-table-column prop="employeeName" label="姓名" width="90" align="center" />
        <el-table-column prop="attendanceMonth" label="月份" width="100" align="center" />
        <el-table-column prop="clockInTime" label="上班打卡" width="155" align="center">
          <template #default="{ row }">
            <span :class="clockInClass(row)">{{ formatTime(row.clockInTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="clockOutTime" label="下班打卡" width="155" align="center">
          <template #default="{ row }">{{ formatTime(row.clockOutTime) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="clockTypeText" label="打卡状态" width="85" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="clockTypeTag(row.clockType)">{{ row.clockTypeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lateMinutes" label="迟到(分)" width="85" align="center">
          <template #default="{ row }"><span v-if="row.lateMinutes" class="late-text">{{ row.lateMinutes }}</span><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="earlyMinutes" label="早退(分)" width="85" align="center">
          <template #default="{ row }"><span v-if="row.earlyMinutes" class="late-text">{{ row.earlyMinutes }}</span><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="workDays" label="应出勤" width="80" align="center" />
        <el-table-column prop="actualDays" label="实际出勤" width="85" align="center" />
        <el-table-column prop="leaveDays" label="请假天数" width="85" align="center">
          <template #default="{ row }">{{ row.leaveDays ? Number(row.leaveDays).toFixed(1) : '-' }}</template>
        </el-table-column>
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import type { EmployeeVO } from '@/api/hr'

/* ---- 月份快捷选择 ---- */
const selectedMonth = computed(() => query.attendanceMonth)

/* ---- 员工下拉列表 ---- */
const employeeList = ref<EmployeeVO[]>([])
const loadEmployees = async () => {
  try {
    const result = await api.getEmployeePageApi({ pageNum: 1, pageSize: 500 })
    employeeList.value = result.records.filter(e => e.employeeStatus === 1)
  } catch {}
}

/* ---- 月度汇总 ---- */
const summaryData = ref<{ workDays?: number; actualDays?: number; lateCount?: number; totalLateMinutes?: number }>({})
const loadSummary = async (month: string) => {
  if (!month) { summaryData.value = {}; return }
  try {
    const list = await api.exportAttendanceApi({ attendanceMonth: month })
    summaryData.value = {
      workDays: list.length,
      actualDays: list.filter(r => r.clockType === 1).length,
      lateCount: list.filter(r => r.clockType === 2).length,
      totalLateMinutes: list.reduce((sum, r) => sum + (r.lateMinutes ?? 0), 0)
    }
  } catch { summaryData.value = {} }
}

/* ---- 表格数据 ---- */
const query = reactive({ pageNum: 1, pageSize: 20, attendanceMonth: undefined as string | undefined, employeeId: undefined as number | undefined, clockType: undefined as number | undefined })
const { records, total, loading, loadData } = useTable(api.getAttendancePageApi, query)

/* ---- 同步 ---- */
const syncLoading = ref(false)
const handleSync = async () => {
  if (!query.attendanceMonth) { ElMessage.warning('请先选择考勤月份'); return }
  syncLoading.value = true
  try {
    const count = await api.syncAttendanceApi(query.attendanceMonth)
    ElMessage.success(`同步完成，新增 ${count} 条记录`)
    loadData()
    loadSummary(query.attendanceMonth)
  } finally { syncLoading.value = false }
}

/* ---- 导出 ---- */
const exportLoading = ref(false)
const handleExport = async () => {
  if (!query.attendanceMonth) { ElMessage.warning('请先选择考勤月份'); return }
  exportLoading.value = true
  try {
    const list = await api.exportAttendanceApi({ employeeId: query.employeeId, attendanceMonth: query.attendanceMonth })
    // 生成CSV
    const headers = ['日期', '姓名', '上班打卡', '下班打卡', '打卡状态', '迟到(分)', '早退(分)', '应出勤', '实际出勤', '请假天数']
    const rows = list.map(r => [
      r.attendanceDay ?? '',
      r.employeeName ?? '',
      r.clockInTime ? formatDateTime(r.clockInTime) : '',
      r.clockOutTime ? formatDateTime(r.clockOutTime) : '',
      r.clockTypeText ?? '',
      r.lateMinutes ?? '',
      r.earlyMinutes ?? '',
      r.workDays ?? '',
      r.actualDays ?? '',
      r.leaveDays ? Number(r.leaveDays).toFixed(1) : ''
    ])
    const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(r => r.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `考勤_${query.attendanceMonth}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error('导出失败：' + (e?.message || ''))
  } finally { exportLoading.value = false }
}

/* ---- 重置 ---- */
const handleReset = () => {
  query.pageNum = 1
  query.attendanceMonth = undefined
  query.employeeId = undefined
  query.clockType = undefined
  summaryData.value = {}
  loadData()
}

/* ---- 工具函数 ---- */
const formatTime = (t?: string) => t ? t.substring(11, 16) : ''
const formatDateTime = (t?: string) => t ? t.substring(0, 16) : ''

const clockTypeMap: Record<number, string> = { 1: '正常', 2: '迟到', 3: '早退', 4: '缺卡' }
const clockTypeTagMap: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'warning', 4: 'danger' }
const clockTypeTag = (v?: number) => v != null ? (clockTypeTagMap[v] ?? '') : ''
const clockInClass = (row: api.AttendanceVO) => row.clockType === 2 ? 'clock-late' : ''

onMounted(() => { loadEmployees(); loadData() })
</script>

<style scoped>
.summary-row { margin-bottom: 12px; }
.summary-card { text-align: center; cursor: default; }
.summary-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.summary-value { font-size: 22px; font-weight: 700; color: #303133; }
.summary-value.late { color: #e6a23c; }
.clock-late { color: #e6a23c; font-weight: 600; }
.late-text { color: #e6a23c; font-weight: 600; }
.header-actions { display: flex; gap: 8px; }
</style>
