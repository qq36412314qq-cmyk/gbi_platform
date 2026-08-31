<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">工作汇报</span>
      <div>
        <AuthBtn permission="oa:work-report:add" type="primary" @click="openAddDialog">提交汇报</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="报告类型">
        <el-select v-model="query.reportType" placeholder="全部" clearable style="width: 120px">
          <el-option label="日报" :value="1" />
          <el-option label="周报" :value="2" />
          <el-option label="月报" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.reportStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="草稿" :value="0" />
          <el-option label="审批中" :value="1" />
          <el-option label="通过" :value="2" />
          <el-option label="驳回" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="userName" label="提交人" width="100" align="center" />
        <el-table-column label="报告类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.reportTypeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportPeriod" label="周期" width="120" align="center" />
        <el-table-column prop="reportContent" label="内容摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.reportStatus === 2 ? 'success' : row.reportStatus === 1 ? 'warning' : row.reportStatus === 3 ? 'danger' : 'info'">{{ row.reportStatusText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.reportStatus === 0 || row.reportStatus === 1" type="primary" link size="small" @click="handleCancel(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <el-dialog v-model="dialogVisible" title="提交工作汇报" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="报告类型" prop="reportType">
          <el-radio-group v-model="form.reportType">
            <el-radio :value="1">日报</el-radio>
            <el-radio :value="2">周报</el-radio>
            <el-radio :value="3">月报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报告周期" prop="reportPeriod">
          <el-input v-model="form.reportPeriod" placeholder="如：2026-08-24" />
        </el-form-item>
        <el-form-item label="报告内容" prop="reportContent">
          <el-input v-model="form.reportContent" type="textarea" :rows="6" placeholder="请输入汇报内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getWorkReportPageApi, submitWorkReportApi, cancelWorkReportApi, type WorkReportVO, type SubmitWorkReportDTO } from '@/api/oa'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const records = ref<WorkReportVO[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({ pageNum: 1, pageSize: 20, reportType: undefined as number | undefined, reportStatus: undefined as number | undefined })
const form = reactive({ reportType: 1 as number, reportPeriod: '', reportContent: '' })
const rules = { reportType: [{ required: true }], reportPeriod: [{ required: true, message: '请输入报告周期' }], reportContent: [{ required: true, message: '请输入报告内容' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkReportPageApi(query)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleReset() { Object.assign(query, { pageNum: 1, pageSize: 20, reportType: undefined, reportStatus: undefined }) }
function openAddDialog() { Object.assign(form, { reportType: 1, reportPeriod: new Date().toISOString().slice(0, 10), reportContent: '' }); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const dto: SubmitWorkReportDTO = {
      companyId: userStore.userInfo?.companyId ?? 0,
      submitUserId: userStore.userInfo?.id ?? 0,
      submitUserName: userStore.userInfo?.username ?? '',
      reportType: form.reportType,
      reportPeriod: form.reportPeriod,
      reportContent: form.reportContent
    }
    await submitWorkReportApi(dto)
    ElMessage.success('汇报已提交')
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleCancel(row: WorkReportVO) {
  await cancelWorkReportApi(row.id)
  ElMessage.success('已撤销')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.g-page-wrap { padding: 20px; }
.g-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.g-page-title { font-size: 18px; font-weight: 600; }
</style>
