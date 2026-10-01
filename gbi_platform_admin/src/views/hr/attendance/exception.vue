<!--
  考勤异常管理页面
  数据表: hr_attendance_exception
  权限: hr:attendance:exception:view
-->
<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">考勤异常管理</span>
      <div class="header-actions">
        <AuthBtn permission="hr:attendance:exception:handle" type="primary" @click="handleBatchConfirm" :loading="batchLoading">批量确认</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="异常类型">
        <el-select v-model="query.exceptionType" placeholder="全部" clearable style="width:140px">
          <el-option label="连续缺卡" :value="1" />
          <el-option label="月度迟到频繁" :value="2" />
          <el-option label="旷工" :value="3" />
          <el-option label="早退频繁" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
          <el-option label="待处理" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已豁免" :value="2" />
          <el-option label="已忽略" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe size="small" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" align="center" />
        <el-table-column prop="exceptionDate" label="异常日期" width="110" align="center" sortable />
        <el-table-column prop="exceptionTypeText" label="异常类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="exceptionTypeTag(row.exceptionType)" size="small">{{ row.exceptionTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detailCount" label="详情数量" width="90" align="center" />
        <el-table-column prop="statusText" label="处理状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="exceptionStatusTag(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleRemark" label="处理备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" sortable />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:attendance:exception:handle" link @click="openHandleDialog(row)">处理</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理考勤异常" width="480px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="异常类型">
          <el-tag :type="exceptionTypeTag(currentException?.exceptionType)" size="small">{{ currentException?.exceptionTypeText }}</el-tag>
        </el-form-item>
        <el-form-item label="异常日期">
          <span>{{ currentException?.exceptionDate }}</span>
        </el-form-item>
        <el-form-item label="详情数量">
          <span>{{ currentException?.detailCount }} 条</span>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.handleType">
            <el-radio :value="1">确认正常</el-radio>
            <el-radio :value="2">豁免处理</el-radio>
            <el-radio :value="3">标记忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <AuthBtn permission="hr:attendance:exception:handle" type="primary" @click="confirmHandle" :loading="handleLoading">确定</AuthBtn>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import * as api from '@/api/hr'

/* ---- 数据定义 ---- */
const loading = ref(false)
const records = ref<api.AttendanceExceptionVO[]>([])
const total = ref(0)
const selectedRows = ref<api.AttendanceExceptionVO[]>([])
const batchLoading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 20,
  exceptionType: undefined as number | undefined,
  status: undefined as number | undefined,
})

/* ---- 加载数据 ---- */
const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getAttendanceExceptionsApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      exceptionType: query.exceptionType,
      status: query.status,
    })
    records.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.pageNum = 1
  query.exceptionType = undefined
  query.status = undefined
  loadData()
}

/* ---- 选择 ---- */
const handleSelectionChange = (rows: api.AttendanceExceptionVO[]) => {
  selectedRows.value = rows
}

const handleBatchConfirm = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要处理的记录')
    return
  }
  batchLoading.value = true
  try {
    const ids = selectedRows.value.map(r => r.id)
    await api.batchHandleAttendanceExceptionsApi(ids, 1, '')
    ElMessage.success('批量确认成功')
    selectedRows.value = []
    loadData()
  } catch (e: any) {
    ElMessage.error('批量确认失败：' + (e?.message || ''))
  } finally {
    batchLoading.value = false
  }
}

/* ---- 处理弹窗 ---- */
const handleVisible = ref(false)
const handleLoading = ref(false)
const currentException = ref<api.AttendanceExceptionVO | null>(null)
const handleForm = reactive({ handleType: 1 as number, handleRemark: '' })

const openHandleDialog = (row: api.AttendanceExceptionVO) => {
  currentException.value = row
  handleForm.handleType = 1
  handleForm.handleRemark = ''
  handleVisible.value = true
}

const confirmHandle = async () => {
  if (!currentException.value) return
  handleLoading.value = true
  try {
    const userStore = useUserStore()
    await api.handleAttendanceExceptionApi(
      currentException.value.id,
      handleForm.handleType,
      handleForm.handleRemark,
      userStore.userInfo?.id
    )
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadData()
  } finally {
    handleLoading.value = false
  }
}

/* ---- 工具函数 ---- */
const exceptionTypeTag = (v?: number) => {
  const map: Record<number, string> = { 1: 'danger', 2: 'warning', 3: 'danger', 4: 'warning' }
  return map[v ?? 0] ?? ''
}

const exceptionStatusTag = (v?: number) => {
  const map: Record<number, string> = { 0: 'danger', 1: 'success', 2: 'info', 3: 'info' }
  return map[v ?? 0] ?? ''
}

onMounted(() => { loadData() })
</script>

<style scoped>
.header-actions { display: flex; gap: 8px; }
</style>
