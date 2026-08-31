<template>
  <div class="g-page-wrap flow-def-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">流程定义</span>
      <div>
        <AuthBtn permission="flow:def:add" type="primary" @click="openDialog()">新增流程定义</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="流程名称">
        <el-input v-model="query.defName" placeholder="输入流程名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="defName" label="流程名称" width="180" show-overflow-tooltip />
        <el-table-column prop="defCode" label="流程编码" width="160" />
        <el-table-column prop="bizType" label="业务类型" width="130" />
        <el-table-column label="节点数" width="90" align="center">
          <template #default="{ row }">{{ parseNodeCount(row.nodeConfigJson) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="flow:def:edit" size="small" type="primary" link @click="openDialog(row)">编辑</AuthBtn>
            <AuthBtn permission="flow:def:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑流程定义' : '新增流程定义'"
      width="600px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="流程名称" prop="defName">
          <el-input v-model="form.defName" placeholder="如：租赁合同审批" maxlength="128" />
        </el-form-item>
        <el-form-item label="流程编码" prop="defCode">
          <el-input v-model="form.defCode" placeholder="如：contract / contract_discount" maxlength="64" />
          <div class="g-tip">集团全局唯一，与业务接入编码一致</div>
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-input v-model="form.bizType" placeholder="如：contract / contract_discount / plan_adjust" maxlength="32" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="节点配置" prop="nodeConfigJson">
          <el-input
            v-model="form.nodeConfigJson"
            type="textarea"
            :rows="8"
            placeholder='[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]'
          />
          <div class="g-tip">节点 JSON：nodeName 节点名称 / nodeMode single|multi / handlerType role|user|submitter / handlerValue 角色编码或用户ID / copyTo 抄送数组</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明（可空）" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 流程定义页（集团专属配置）：表驱动审批引擎的流程模板，节点配置 JSON 表单生成，
 * 后端 FlowConfigUtil 过滤脚本后入库；子公司只读复用集团模板
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getFlowDefPageApi,
  addFlowDefApi,
  updateFlowDefApi,
  deleteFlowDefApi,
  type FlowDefDTO,
  type FlowDefVO
} from '@/api/flow'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FlowDefVO>(getFlowDefPageApi, {
  defName: undefined,
  status: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

function parseNodeCount(json?: string): number {
  if (!json) {
    return 0
  }
  try {
    const parsed = JSON.parse(json)
    return Array.isArray(parsed.nodes) ? parsed.nodes.length : 0
  } catch {
    return 0
  }
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<FlowDefDTO>({
  id: 0,
  defName: '',
  defCode: '',
  bizType: '',
  nodeConfigJson: '',
  status: 1,
  remark: ''
})

const rules: FormRules = {
  defName: [
    { required: true, message: '请输入流程名称', trigger: 'blur' },
    { max: 128, message: '流程名称不能超过128字符', trigger: 'blur' }
  ],
  defCode: [
    { required: true, message: '请输入流程编码', trigger: 'blur' },
    { max: 64, message: '流程编码不能超过64字符', trigger: 'blur' }
  ],
  bizType: [{ required: true, message: '请输入业务类型', trigger: 'blur' }],
  nodeConfigJson: [
    { required: true, message: '请输入节点配置 JSON', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          return callback()
        }
        try {
          const parsed = JSON.parse(value)
          if (!Array.isArray(parsed.nodes)) {
            return callback(new Error('节点配置必须是 {"nodes": [...]} 结构'))
          }
          callback()
        } catch {
          callback(new Error('节点配置 JSON 格式错误'))
        }
      },
      trigger: 'blur'
    }
  ]
}

function openDialog(row?: FlowDefVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    defName: row?.defName ?? '',
    defCode: row?.defCode ?? '',
    bizType: row?.bizType ?? '',
    nodeConfigJson: row?.nodeConfigJson ?? '',
    status: row?.status ?? 1,
    remark: row?.remark ?? ''
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
        await updateFlowDefApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addFlowDefApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 删除 ---------------- */
async function handleDelete(row: FlowDefVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除流程定义「${row.defName}」吗？已有流程实例的禁止删除`, '提示', {
    type: 'warning'
  })
  await deleteFlowDefApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>