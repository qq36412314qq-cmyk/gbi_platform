<template>
  <div class="g-page-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">设备授权管理</span>
      <div>
        <AuthBtn permission="device:auth:add" type="primary" @click="openDialog()">新增授权</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="主板SN">
        <el-input v-model="query.motherboardSn" placeholder="请输入主板SN" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="CPU编号">
        <el-input v-model="query.cpuId" placeholder="请输入CPU编号" clearable style="width: 200px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="motherboardSn" label="主板SN" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cpuId" label="CPU编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="diskSn" label="硬盘序列号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="有效期" min-width="160">
          <template #default="{ row }">
            {{ row.expireTime ? formatTime(row.expireTime) : '永久' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="授权时间" min-width="160" />
        <el-table-column prop="updateTime" label="更新时间" min-width="160">
          <template #default="{ row }">
            {{ row.updateTime ? formatTime(row.updateTime) : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="device:auth:add" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="device:auth:toggle" size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </AuthBtn>
            <AuthBtn permission="device:auth:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 弹窗区：新增/编辑 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑授权设备' : '新增授权设备'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="主板SN" prop="motherboardSn">
          <el-input v-model="form.motherboardSn" placeholder="请输入主板SN" maxlength="128" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="CPU编号" prop="cpuId">
          <el-input v-model="form.cpuId" placeholder="请输入CPU编号" maxlength="128" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="硬盘序列号" prop="diskSn">
          <el-input v-model="form.diskSn" placeholder="请输入硬盘序列号（可选）" maxlength="128" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称（备注）" maxlength="128" />
        </el-form-item>
        <el-form-item label="授权有效期" prop="expireTime">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择有效期截止时间（留空表示永久）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="x"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 设备授权管理页：sys_client_device_auth 集团统一授权维护
 */
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getDeviceAuthPageApi,
  addDeviceAuthApi,
  updateDeviceAuthApi,
  deleteDeviceAuthApi,
  toggleDeviceAuthApi,
  type DeviceAuthVO
} from '@/api/deviceAuth'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<DeviceAuthVO>(getDeviceAuthPageApi, {
  motherboardSn: undefined,
  cpuId: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 格式化时间 ---------------- */
function formatTime(timestamp: number | string): string {
  if (!timestamp) return ''
  const date = new Date(Number(timestamp))
  return date.toLocaleString('zh-CN', { hour12: false })
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<Partial<DeviceAuthVO>>({
  motherboardSn: '',
  cpuId: '',
  diskSn: '',
  deviceName: '',
  expireTime: undefined,
  status: 1
})

const rules = computed<FormRules>(() => ({
  motherboardSn: [{ required: true, message: '请输入主板SN', trigger: 'blur' }],
  cpuId: [{ required: true, message: '请输入CPU编号', trigger: 'blur' }]
}))

function openDialog(_parentId?: number, row?: DeviceAuthVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id,
    motherboardSn: row?.motherboardSn ?? '',
    cpuId: row?.cpuId ?? '',
    diskSn: row?.diskSn ?? '',
    deviceName: row?.deviceName ?? '',
    expireTime: row?.expireTime ? new Date(row.expireTime).getTime() : undefined,
    status: row?.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    submitLoading.value = true
    try {
      if (form.id) {
        await updateDeviceAuthApi({ ...form } as DeviceAuthVO)
        ElMessage.success('修改成功')
      } else {
        await addDeviceAuthApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(row: DeviceAuthVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除设备「${row.motherboardSn}」的授权吗？`, '提示', { type: 'warning' })
  await deleteDeviceAuthApi(row.id!)
  ElMessage.success('删除成功')
  loadData()
}

async function handleToggle(row: DeviceAuthVO): Promise<void> {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}设备「${row.motherboardSn}」吗？`, '提示', { type: 'warning' })
  await toggleDeviceAuthApi(row.id!)
  ElMessage.success(`${action}成功`)
  loadData()
}
</script>
