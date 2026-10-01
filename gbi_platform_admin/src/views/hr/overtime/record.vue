<!--
  加班记录页面
  数据表: hr_overtime_record
  权限: hr:overtime:record:*
-->
<template>
  <div class="overtime-page">
    <SearchBar :model="query" @search="loadData" @reset="handleReset" inline>
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width:140px">
          <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="加班日期">
        <el-date-picker v-model="query.overtimeDateStart" type="date" value-format="YYYY-MM-DD" placeholder="开始" style="width:130px" />
        <span style="margin:0 4px">~</span>
        <el-date-picker v-model="query.overtimeDateEnd" type="date" value-format="YYYY-MM-DD" placeholder="结束" style="width:130px" />
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="query.sourceType" placeholder="全部" clearable style="width:100px">
          <el-option label="手动申请" :value="1" /><el-option label="自动识别" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="确认状态">
        <el-select v-model="query.confirmStatus" placeholder="全部" clearable style="width:100px">
          <el-option label="待确认" :value="0" /><el-option label="已确认" :value="1" /><el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <div class="page-actions">
      <AuthBtn permission="hr:overtime:record:detect" type="primary" :loading="detectLoading" @click="handleDetect">触发自动识别（昨日）</AuthBtn>
      <AuthBtn permission="hr:overtime:record:export" link @click="handleExport">导出</AuthBtn>
    </div>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe size="small">
        <el-table-column prop="employeeName" label="姓名" width="90" align="center" />
        <el-table-column prop="overtimeDate" label="加班日期" width="110" align="center" sortable />
        <el-table-column label="时段" width="140" align="center">
          <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="overtimeHours" label="时长(h)" width="85" align="center" sortable />
        <el-table-column prop="overtimeTypeText" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="overtimeTypeTag(row.overtimeType)">{{ row.overtimeTypeText ?? overtimeTypeMap[row.overtimeType] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceTypeText" label="来源" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.sourceType === 2 ? 'success' : 'info'">{{ row.sourceTypeText ?? sourceTypeMap[row.sourceType] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confirmStatusText" label="确认状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="confirmStatusTag(row.confirmStatus)">{{ row.confirmStatusText ?? confirmStatusMap[row.confirmStatus] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="compensateStatusText" label="补偿状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="compStatusTag(row.compensateStatus)">{{ row.compensateStatusText ?? compStatusMap[row.compensateStatus] ?? '未补偿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" sortable />
        <el-table-column label="操作" width="140" align="center" fixed="right" v-if="hasConfirmActions">
          <template #default="{ row }">
            <AuthBtn permission="hr:overtime:record:confirm" link size="small" @click="handleConfirm(row, 1)">确认</AuthBtn>
            <AuthBtn permission="hr:overtime:record:reject" link size="small" type="danger" @click="openRejectDialog(row)">驳回</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 驳回弹窗 -->
    <CommonDialog v-model="rejectVisible" title="驳回加班记录" :loading="rejectSaving" @confirm="handleRejectConfirm">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.remark" type="textarea" :rows="3" placeholder="请输入驳回原因" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import type { EmployeeVO, HrOvertimeRecordVO, HrOvertimeRecordQueryDTO } from '@/api/hr'
import SearchBar from '@/components/common/SearchBar.vue'
import TablePage from '@/components/common/TablePage.vue'
import AuthBtn from '@/components/common/AuthBtn.vue'
import CommonDialog from '@/components/common/CommonDialog.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasConfirmActions = computed(() => userStore.hasPermission('hr:overtime:record:confirm') || userStore.hasPermission('hr:overtime:record:reject'))

/* ========== 列表 ========== */
const fetchApi = (params: HrOvertimeRecordQueryDTO) => api.getOvertimeRecordPageApi(params)
const { query, records, total, loading, loadData } = useTable<HrOvertimeRecordVO>(fetchApi, { pageNum: 1, pageSize: 20 })

/* ========== 员工下拉 ========== */
const employeeList = ref<EmployeeVO[]>([])
const loadEmployees = async () => {
  try { employeeList.value = await api.getEmployeeListApi() } catch { employeeList.value = [] }
}

/* ========== 自动识别 ========== */
const detectLoading = ref(false)
const handleDetect = async () => {
  detectLoading.value = true
  try {
    const result = await api.autoDetectOvertimeApi()
    ElMessage.success(
      `识别完成：共${result.totalRecords}条记录，新增${result.newRecords}条，更新${result.updatedRecords}条，跳过${result.skippedRecords}条`
    )
    loadData()
  } catch { /* ignored */ } finally { detectLoading.value = false }
}

/* ========== 导出 ========== */
const handleExport = async () => {
  try { await api.exportOvertimeRecordApi(query as any); ElMessage.success('导出成功') } catch { ElMessage.warning('导出接口暂未就绪') }
}

/* ========== 确认/驳回 ========== */
const currentRow = ref<HrOvertimeRecordVO | null>(null)
const rejectVisible = ref(false)
const rejectSaving = ref(false)
const rejectForm = reactive({ remark: '' })
const rejectId = ref<number | undefined>()

const handleConfirm = async (row: HrOvertimeRecordVO, confirmStatus: number) => {
  const action = confirmStatus === 1 ? '确认' : '驳回'
  await ElMessageBox.confirm(`确定${action}「${row.overtimeDate}」的加班记录吗？`, '提示', { type: confirmStatus === 1 ? 'info' : 'warning' })
  try {
    await api.confirmOvertimeRecordApi({ id: row.id, confirmStatus })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {}
}

const openRejectDialog = (row: HrOvertimeRecordVO) => {
  currentRow.value = row
  rejectId.value = row.id
  rejectForm.remark = ''
  rejectVisible.value = true
}

const handleRejectConfirm = async () => {
  if (!rejectId.value) return
  rejectSaving.value = true
  try {
    await api.confirmOvertimeRecordApi({ id: rejectId.value, confirmStatus: 2, remark: rejectForm.remark })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadData()
  } finally { rejectSaving.value = false }
}

const handleReset = () => { loadData() }

/* ========== 工具函数 ========== */
const overtimeTypeMap: Record<number, string> = { 1: '工作日', 2: '休息日', 3: '法定节假日' }
const overtimeTypeTag = (v?: number) => (v === 2 || v === 3) ? 'warning' : 'info'
const sourceTypeMap: Record<number, string> = { 1: '手动', 2: '自动' }
const confirmStatusMap: Record<number, string> = { 0: '待确认', 1: '已确认', 2: '已驳回' }
const confirmStatusTag = (v?: number) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[v ?? 0] ?? '')
const compStatusMap: Record<number, string> = { 0: '未补偿', 1: '已调休', 2: '已发放' }
const compStatusTag = (v?: number) => ({ 0: 'info', 1: 'success', 2: 'warning' }[v ?? 0] ?? '')

onMounted(() => { loadEmployees(); loadData() })
</script>

<style scoped>
.overtime-page { padding: 0; }
.page-actions { margin: 12px 0; }
</style>
