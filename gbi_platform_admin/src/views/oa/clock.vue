<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">打卡管理</span>
      <div>
        <el-button type="primary" @click="handleClockIn">上班打卡</el-button>
      <el-button type="info" @click="handleClockOut">下班打卡</el-button>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="打卡人">
        <el-input v-model="query.userName" placeholder="姓名" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="打卡类型">
        <el-select v-model="query.clockType" placeholder="全部" clearable style="width: 120px">
          <el-option label="上班" :value="1" />
          <el-option label="下班" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="userName" label="打卡人" width="100" align="center" />
        <el-table-column label="打卡类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.clockType === 1 ? 'primary' : 'success'">{{ row.clockType === 1 ? '上班' : '下班' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clockTime" label="打卡时间" width="160" align="center" />
        <el-table-column prop="locationAddr" label="打卡地点" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isLate === 1" size="small" type="warning">迟到</el-tag>
            <el-tag v-else-if="row.isEarly === 1" size="small" type="danger">早退</el-tag>
            <el-tag v-else-if="row.isAbsent === 1" size="small" type="info">缺卡</el-tag>
            <el-tag v-else size="small" type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
      </el-table>
    </TablePage>

    <el-dialog v-model="clockDialogVisible" title="手动打卡" width="400px">
      <el-form label-width="80px">
        <el-form-item label="打卡时间">
          <el-date-picker v-model="clockTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="打卡地址">
          <el-input v-model="clockAddr" placeholder="自动获取或手动输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="clockLoading" @click="handleConfirmClock">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getClockRecordPageApi, clockInApi, type ClockRecordVO, type ClockInDTO } from '@/api/oa'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const records = ref<ClockRecordVO[]>([])
const clockDialogVisible = ref(false)
const clockLoading = ref(false)
const clockType = ref(1)
const clockTime = ref('')
const clockAddr = ref('')

const query = reactive({ pageNum: 1, pageSize: 20, userName: '', clockType: undefined as number | undefined, startDate: '', endDate: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getClockRecordPageApi(query)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleReset() { Object.assign(query, { pageNum: 1, pageSize: 20, userName: '', clockType: undefined, startDate: '', endDate: '' }) }
function handleClockIn() { clockType.value = 1; clockTime.value = new Date().toLocaleString('sv-SE').replace(' ', 'T'); clockAddr.value = ''; clockDialogVisible.value = true }
function handleClockOut() { clockType.value = 2; clockTime.value = new Date().toLocaleString('sv-SE').replace(' ', 'T'); clockAddr.value = ''; clockDialogVisible.value = true }

async function handleConfirmClock() {
  clockLoading.value = true
  try {
    const dto: ClockInDTO = {
      companyId: userStore.userInfo?.companyId ?? 0,
      userId: userStore.userInfo?.id ?? 0,
      userName: userStore.userInfo?.username ?? '',
      clockType: clockType.value,
      locationAddr: clockAddr.value
    }
    await clockInApi(dto)
    ElMessage.success('打卡成功')
    clockDialogVisible.value = false
    loadData()
  } finally { clockLoading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.g-page-wrap { padding: 20px; }
.g-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.g-page-title { font-size: 18px; font-weight: 600; }
</style>