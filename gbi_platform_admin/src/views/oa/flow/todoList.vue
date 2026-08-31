<template>
  <div class="g-page-wrap flow-todo-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">待办处理</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="业务类型">
        <el-input v-model="query.bizType" placeholder="如 contract / contract_discount" clearable style="width: 220px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="defName" label="流程名称" width="150" show-overflow-tooltip />
        <el-table-column prop="title" label="审批标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="当前节点" width="130" />
        <el-table-column prop="applyUserName" label="申请人" width="100" align="center" />
        <el-table-column label="任务状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.taskStatus === 0 ? 'warning' : 'info'">{{ taskStatusMap[row.taskStatus ?? 0] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实例状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.instanceStatus === 0 ? 'warning' : 'info'">{{ instanceStatusMap[row.instanceStatus ?? 0] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="150" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn v-if="row.taskStatus === 0" permission="flow:task:handle" size="small" type="primary" link @click="openHandleDialog(row)">处理</AuthBtn>
            <AuthBtn permission="flow:task:urge" size="small" link @click="handleUrge(row)">催办</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 审批处理弹窗 -->
    <CommonDialog v-model="handleVisible" title="审批处理" width="560px" :loading="submitLoading" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="流程标题">
          <span class="g-text-strong">{{ currentTask?.title }}</span>
        </el-form-item>
        <el-form-item label="当前节点">
          <span>{{ currentTask?.nodeName }}</span>
        </el-form-item>
        <el-form-item label="审批动作" prop="action">
          <el-radio-group v-model="form.action">
            <el-radio value="pass">通过</el-radio>
            <el-radio value="reject">驳回</el-radio>
            <el-radio value="transfer">转交</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.action === 'transfer'" label="转交目标" prop="transferHandlerId">
          <el-select
            v-model="form.transferHandlerId"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userLoading"
            placeholder="输入姓名/账号搜索用户"
            style="width: 100%"
          >
            <el-option v-for="item in userOptions" :key="item.id" :label="`${item.realName}（${item.username}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批意见" prop="opinion">
          <el-input v-model="form.opinion" type="textarea" :rows="3" placeholder="审批意见（可空）" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 待办处理页：聚合当前登录人全部审批待办（单据审批 + 敏感审批），支持通过/驳回/转交/催办
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getMyTodoPageApi,
  handleTaskApi,
  urgeTaskApi,
  type FlowHandleDTO,
  type FlowTaskVO
} from '@/api/flow'
import { getUserPageApi, type UserVO } from '@/api/org'
import { useTable } from '@/hooks/useTable'

const instanceStatusMap: Record<number, string> = { 0: '审批中', 1: '通过', 2: '驳回', 3: '撤回', 4: '终止' }
const taskStatusMap: Record<number, string> = { 0: '待办', 1: '已办', 2: '已转交', 3: '已作废' }

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FlowTaskVO>(getMyTodoPageApi, {
  bizType: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 审批处理 ---------------- */
const handleVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentTask = ref<FlowTaskVO | null>(null)

const form = reactive<FlowHandleDTO>({
  action: 'pass',
  taskId: 0,
  approveResult: 1,
  opinion: '',
  transferHandlerId: undefined
})

const rules: FormRules = {
  action: [{ required: true, message: '请选择审批动作', trigger: 'change' }],
  transferHandlerId: [{ required: true, message: '请选择转交目标用户', trigger: 'change' }]
}

function openHandleDialog(row: FlowTaskVO): void {
  currentTask.value = row
  formRef.value?.clearValidate()
  Object.assign(form, { action: 'pass', taskId: row.id, approveResult: 1, opinion: '', transferHandlerId: undefined })
  handleVisible.value = true
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
      await handleTaskApi({
        action: form.action,
        taskId: form.taskId,
        approveResult: form.action === 'pass' ? 1 : form.action === 'reject' ? 0 : undefined,
        opinion: form.opinion,
        transferHandlerId: form.action === 'transfer' ? form.transferHandlerId : undefined
      })
      ElMessage.success(form.action === 'pass' ? '审批通过' : form.action === 'reject' ? '已驳回' : '已转交')
      handleVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 转交目标用户搜索 ---------------- */
const userOptions = ref<UserVO[]>([])
const userLoading = ref(false)

async function searchUsers(keyword: string): Promise<void> {
  userLoading.value = true
  try {
    const data = await getUserPageApi({ pageNum: 1, pageSize: 20, realName: keyword || undefined, username: keyword || undefined })
    userOptions.value = data.records || []
  } finally {
    userLoading.value = false
  }
}

/* ---------------- 催办 ---------------- */
async function handleUrge(row: FlowTaskVO): Promise<void> {
  await ElMessageBox.confirm(`确定催办流程「${row.defName}」吗？将向审批人推送站内信提醒`, '提示', { type: 'warning' })
  await urgeTaskApi(row.id)
  ElMessage.success('催办成功')
}
</script>