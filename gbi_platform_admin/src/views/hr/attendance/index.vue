<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">考勤管理</span>
      <AuthBtn permission="hr:attendance:sync" type="primary" @click="handleSync" :loading="syncLoading">同步考勤</AuthBtn>
      <AuthBtn permission="hr:attendance:export" @click="handleExport">导出</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="考勤月份">
        <el-date-picker v-model="query.attendanceMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
        <el-table-column prop="attendanceMonth" label="考勤月份" width="110" align="center" />
        <el-table-column prop="attendanceDay" label="考勤日期" width="110" align="center" />
        <el-table-column prop="clockInTime" label="上班打卡" width="160" align="center" />
        <el-table-column prop="clockOutTime" label="下班打卡" width="160" align="center" />
        <el-table-column prop="clockTypeText" label="打卡状态" width="90" align="center">
          <template #default="{ row }"><el-tag size="small" :type="clockTypeTag(row.clockType)">{{ row.clockTypeText || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="workDays" label="出勤天数" width="90" align="center" />
        <el-table-column prop="absent" label="旷工天数" width="90" align="center" />
        <el-table-column prop="leaveDays" label="请假天数" width="90" align="center" />
        <el-table-column prop="lateMinutes" label="迟到分钟" width="100" align="center" />
        <el-table-column prop="earlyMinutes" label="早退分钟" width="100" align="center" />
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const clockTypeMap: Record<number, string> = { 1: '正常', 2: '迟到', 3: '早退', 4: '缺卡' }
const clockTypeTagMap: Record<number, string> = { 1: '', 2: 'warning', 3: 'warning', 4: 'danger' }
const clockTypeTag = (v?: number) => v != null ? (clockTypeTagMap[v] ?? '') : ''

const { query, records, total, loading, loadData } = useTable(api.getAttendancePageApi, { pageNum: 1, pageSize: 20, attendanceMonth: undefined })
const syncLoading = ref(false)

const handleSync = async () => {
  syncLoading.value = true
  try { const count = await api.syncAttendanceApi(query.attendanceMonth); ElMessage.success(`同步完成，共${count}条`); loadData() } finally { syncLoading.value = false }
}
const handleExport = async () => {
  try { const list = await api.exportAttendanceApi({ employeeId: query.employeeId, attendanceMonth: query.attendanceMonth }); console.log('export', list.length) } catch {}
}
const handleReset = () => { loadData() }
</script>
