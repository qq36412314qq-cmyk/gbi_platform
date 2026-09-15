<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">薪资规则模板</span>
      <el-button type="primary" @click="openAdd">新增模板</el-button>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="绑定类型">
        <el-select v-model="query.bindType" placeholder="全部" clearable style="width: 130px">
          <el-option label="岗位" :value="1" />
          <el-option label="薪酬级别" :value="2" />
          <el-option label="岗位+级别" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="ruleName" label="模板名称" width="160" show-overflow-tooltip />
        <el-table-column label="绑定类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ bindTypeText(row.bindType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gradeCode" label="薪酬级别" width="100" />
        <el-table-column label="基本工资" width="100" align="right">
          <template #default="{ row }">{{ Number(row.basicSalary).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="绩效基数" width="100" align="right">
          <template #default="{ row }">{{ Number(row.performanceBase).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="岗位津贴" width="100" align="right">
          <template #default="{ row }">{{ Number(row.positionAllowance).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="审批状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="applyStatusType(row.applyStatus)">{{ applyStatusText(row.applyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" :disabled="row.applyStatus === 0" @click="handleSubmitAudit(row)">提交审批</el-button>
            <el-button link type="danger" size="small" :disabled="row.status === 0" @click="handleDisable(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="模板名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如 P2岗位通用薪资模板" />
        </el-form-item>
        <el-form-item label="绑定类型" prop="bindType">
          <el-radio-group v-model="form.bindType">
            <el-radio :value="1">岗位</el-radio>
            <el-radio :value="2">薪酬级别</el-radio>
            <el-radio :value="3">岗位+薪酬级别</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.bindType === 2 || form.bindType === 3" label="薪酬级别" prop="gradeCode">
          <el-select v-model="form.gradeCode" placeholder="请选择薪酬级别" style="width: 100%">
            <el-option v-for="g in gradeOptions" :key="g.gradeCode" :label="g.gradeCode + ' ' + g.gradeName" :value="g.gradeCode" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基本工资" prop="basicSalary">
              <el-input-number v-model="form.basicSalary" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绩效基数" prop="performanceBase">
              <el-input-number v-model="form.performanceBase" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="岗位津贴" prop="positionAllowance">
              <el-input-number v-model="form.positionAllowance" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="其他津贴">
              <el-input-number v-model="form.otherAllowance" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="社保个人比例">
              <el-input-number v-model="form.socialSecurityRate" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公积金比例">
              <el-input-number v-model="form.housingFundRate" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getSalaryRulePageApi, addSalaryRuleApi, updateSalaryRuleApi, disableSalaryRuleApi,
  submitSalaryRuleAuditApi, getSalaryGradeListApi,
  type HrSalaryRuleVO, type HrSalaryGradeVO
} from '@/api/hrSalary'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData, resetQuery } = useTable<HrSalaryRuleVO>(getSalaryRulePageApi, {
  bindType: undefined, pageNum: 1, pageSize: 10
})
function handleReset() { resetQuery(); loadData() }

const gradeOptions = ref<HrSalaryGradeVO[]>([])
onMounted(async () => {
  try { gradeOptions.value = await getSalaryGradeListApi() } catch {}
})

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const emptyForm = (): Partial<HrSalaryRuleVO> => ({
  ruleName: '', bindType: 2, gradeCode: '',
  basicSalary: 0, performanceBase: 0, positionAllowance: 0, otherAllowance: 0,
  socialSecurityRate: 0, housingFundRate: 0, remark: ''
})
const form = reactive<Partial<HrSalaryRuleVO>>(emptyForm())

const rules: FormRules = {
  ruleName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  bindType: [{ required: true, message: '请选择绑定类型', trigger: 'change' }],
  basicSalary: [{ required: true, message: '请输入基本工资', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑薪资模板' : '新增薪资模板')

function bindTypeText(v: number) { return ({ 1: '岗位', 2: '薪酬级别', 3: '岗位+级别' } as Record<number, string>)[v] || '-' }
function applyStatusText(v: number) { return ({ 1: '已生效', 0: '审批中', 2: '已驳回', 3: '已撤回' } as Record<number, string>)[v] || '-' }
function applyStatusType(v: number) { return ({ 1: 'success', 0: 'warning', 2: 'danger', 3: 'info' } as Record<number, string>)[v] || 'info' }

function openAdd() {
  isEdit.value = false
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}
function openEdit(row: HrSalaryRuleVO) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSubmitAudit(row: HrSalaryRuleVO) {
  await ElMessageBox.confirm(`确定提交模板「${row.ruleName}」去审批吗？审批通过后生效。`, '提交审批', { type: 'info' })
  await submitSalaryRuleAuditApi(row.id)
  ElMessage.success('已提交审批')
  loadData()
}
async function handleDisable(row: HrSalaryRuleVO) {
  await ElMessageBox.confirm(`确定停用模板「${row.ruleName}」吗？`, '确认停用', { type: 'warning' })
  await disableSalaryRuleApi(row.id)
  ElMessage.success('已停用')
  loadData()
}
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateSalaryRuleApi(form)
        ElMessage.success('修改成功')
      } else {
        await addSalaryRuleApi(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}
</script>
