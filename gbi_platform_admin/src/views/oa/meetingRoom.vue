<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">会议室管理</span>
      <div>
        <AuthBtn permission="oa:meeting-room:add" type="primary" @click="openAddDialog">添加会议室</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="roomName" label="会议室名称" min-width="150" />
        <el-table-column prop="location" label="位置" width="120" />
        <el-table-column prop="facilities" label="设施" width="150" show-overflow-tooltip />
        <el-table-column prop="capacity" label="容纳人数" width="100" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleToggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <el-dialog v-model="dialogVisible" title="添加会议室" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如：A栋3楼" />
        </el-form-item>
        <el-form-item label="设施配置" prop="facilities">
          <el-input v-model="form.facilities" placeholder="如：投影仪,视频会议,电话" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMeetingRoomPageApi, addMeetingRoomApi, updateMeetingRoomApi, type MeetingRoomVO } from '@/api/oa'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const records = ref<MeetingRoomVO[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const form = reactive({ id: undefined as number | undefined, roomName: '', location: '', capacity: 10, facilities: '', status: 1 })
const rules = { roomName: [{ required: true, message: '请输入会议室名称' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await getMeetingRoomPageApi(query)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleReset() { Object.assign(query, { pageNum: 1, pageSize: 20, status: undefined }) }
function openAddDialog() { Object.assign(form, { id: undefined, roomName: '', location: '', capacity: 10, facilities: '', status: 1 }); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateMeetingRoomApi(form)
    } else {
      await addMeetingRoomApi({ companyId: userStore.userInfo?.companyId ?? 0, ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleToggle(row: MeetingRoomVO) {
  await updateMeetingRoomApi({ ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.g-page-wrap { padding: 20px; }
.g-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.g-page-title { font-size: 18px; font-weight: 600; }
</style>
