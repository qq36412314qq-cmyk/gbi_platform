<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">请假管理</span>
      <div>
        <AuthBtn permission="oa:leave:add" type="primary" @click="openAddDialog">申请请假</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="请假类型">
        <el-select v-model="query.leaveType" placeholder="全部" clearable style="width: 120px">
          <el-option label="年假" :value="1" />
          <el-option label="事假" :value="2" />
          <el-option label="病假" :value="3" />
          <el-option label="调休" :value="4" />
          <el-option label="产假" :value="5" />
          <el-option label="其他" :value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.applyStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="草稿" :value="0" />
          <el-option label="审批中" :value="1" />
          <el-option label="通过" :value="2" />
          <el-option label="驳回" :value="3" />
          <el-option label="作废" :value="4" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="applyNo" label="申请单号" width="140" align="center" />
        <el-table-column prop="applyUserName" label="申请人" width="100" align="center" />
        <el-table-column label="请假类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.leaveTypeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" align="center" />
        <el-table-column prop="endDate" label="结束日期" width="110" align="center" />
        <el-table-column prop="leaveDays" label="天数" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.applyStatus)">{{ row.applyStatusText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假事由" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.applyStatus === 0 || row.applyStatus === 1" type="primary" link size="small" @click="handleCancel(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 申请弹窗 -->
    <el-dialog v-model="dialogVisible" title="申请请假" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择" style="width: 100%">
            <el-option label="年假" :value="1" />
            <el-option label="事假" :value="2" />
            <el-option label="病假" :value="3" />
            <el-option label="调休" :value="4" />
            <el-option label="产假" :value="5" />
            <el-option label="其他" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假事由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLeavePageApi, submitLeaveApi, cancelLeaveApi, type LeaveVO, type SubmitLeaveDTO } from '@/api/oa'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const records = ref<LeaveVO[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({ pageNum: 1, pageSize: 20, leaveType: undefined as number | undefined, applyStatus: undefined as number | undefined })

const form = reactive({ leaveType: undefined as number | undefined, startDate: '', endDate: '', reason: '' })
const rules = { leaveType: [{ required: true, message: '请选择请假类型' }], startDate: [{ required: true, message: '请选择开始日期' }], endDate: [{ required: true, message: '请选择结束日期' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await getLeavePageApi(query)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleReset() { Object.assign(query, { pageNum: 1, pageSize: 20, leaveType: undefined, applyStatus: undefined }) }
function openAddDialog() { Object.assign(form, { leaveType: undefined, startDate: '', endDate: '', reason: '' }); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const dto: SubmitLeaveDTO = {
      companyId: userStore.userInfo?.companyId ?? 0,
      applyUserId: userStore.userInfo?.id ?? 0,
      applyUserName: userStore.userInfo?.username ?? '',
      leaveType: form.leaveType!,
      startDate: form.startDate,
      endDate: form.endDate,
      leaveDays: 1,
      reason: form.reason
    }
    await submitLeaveApi(dto)
    ElMessage.success('请假申请已提交')
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleCancel(row: LeaveVO) {
  await cancelLeaveApi(row.id)
  ElMessage.success('已撤销')
  loadData()
}

function statusType(s: number) { const m: Record<number,string> = {0:'info',1:'warning',2:'success',3:'danger',4:'info'}; return m[s] ?? '' }

onMounted(loadData)
</script>

<style scoped>
.g-page-wrap { padding: 20px; }
.g-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.g-page-title { font-size: 18px; font-weight: 600; }
</style>
