<template>
  <div class="g-page-wrap config-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">参数配置</span>
      <div>
        <AuthBtn permission="config:add" type="primary" @click="openDialog()">新增参数</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="参数名称">
        <el-input v-model="query.configName" placeholder="请输入参数名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="参数Key">
        <el-input v-model="query.configKey" placeholder="请输入参数Key" clearable style="width: 180px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="configName" label="参数名称" min-width="140" />
        <el-table-column prop="configKey" label="参数Key" min-width="160" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="companyId" label="所属公司" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团全局' : `子公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="config:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="config:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 弹窗区：新增/编辑 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑参数' : '新增参数'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="参数Key" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 rent_expire_warn_days" :disabled="!!form.id" maxlength="128" />
        </el-form-item>
        <el-form-item label="参数值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="请输入参数值" maxlength="1000" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="参数说明" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 参数配置页：sys_config 集团全局参数维护（修改走审计）
 */
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getConfigPageApi,
  addConfigApi,
  updateConfigApi,
  deleteConfigApi,
  type ConfigDTO,
  type ConfigVO
} from '@/api/sys'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<ConfigVO>(getConfigPageApi, {
  configName: undefined,
  configKey: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<ConfigDTO>({
  companyId: 0,
  configKey: '',
  configValue: '',
  configName: '',
  remark: ''
})

/**
 * 参数Key校验规则：编辑态 configKey 只读不可修改，跳过校验，避免
 * 历史数据（如 water_elec.water_price 含点号）编辑时误报格式错误
 */
const rules = computed<FormRules>(() => {
  const base: FormRules = {
    configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
    configValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }]
  }
  if (!form.id) {
    base.configKey = [
      { required: true, message: '请输入参数Key', trigger: 'blur' },
      { pattern: /^[a-z][a-z0-9_.]*$/, message: 'Key 须为小写字母/数字/下划线/点', trigger: 'blur' }
    ]
  }
  return base
})

function openDialog(_parentId?: number, row?: ConfigVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id,
    companyId: row?.companyId ?? 0,
    configKey: row?.configKey ?? '',
    configValue: row?.configValue ?? '',
    configName: row?.configName ?? '',
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
        await updateConfigApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addConfigApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(row: ConfigVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除参数「${row.configName}」吗？`, '提示', { type: 'warning' })
  await deleteConfigApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>