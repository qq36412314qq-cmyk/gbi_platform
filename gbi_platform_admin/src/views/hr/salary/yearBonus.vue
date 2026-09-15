<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">年终奖/一次性奖金</span>
      <el-button type="primary" @click="openAdd">新增年终奖</el-button>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="年度">
        <el-input-number v-model="query.bonusYear" :min="2020" :max="2099" placeholder="如 2026" controls-position="right" style="width: 120px" />
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="employeeName" label="员工" width="120" show-overflow-tooltip />
        <el-table-column prop="bonusYear" label="年度" width="80" align="center" />
        <el-table-column label="奖金类型" width="110" align="center">
          <template #default="{ row }">{{ bonusTypeText(row.bonusType) }}</template>
        </el-table-column>
        <el-table-column label="奖金金额" width="110" align="right">
          <template #default="{ row }"><span class="g-money">{{ Number(row.bonusAmount).toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column prop="bonusReason" label="发放原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="审批状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="applyStatusType(row.applyStatus)">{{ applyStatusText(row.applyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发放状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.payStatus === 1 ? 'success' : 'warning'">
              {{ row.payStatus === 1 ? '已发放' : '未发放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" :disabled="row.applyStatus === 0" @click="handleSubmitAudit(row)">提交审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="员工ID" prop="employeeId">
          <el-input-number v-model="form.employeeId" :min="1" style="width: 100%" />
          <div class="b-tip">手动输入员工ID，后续可升级为下拉选择</div>
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="form.employeeName" placeholder="员工姓名（可选）" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="奖金年度" prop="bonusYear">
              <el-input-number v-model="form.bonusYear" :min="2020" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖金类型" prop="bonusType">
              <el-select v-model="form.bonusType" style="width: 100%">
                <el-option label="年终奖" :value="1" />
                <el-option label="项目奖" :value="2" />
                <el-option label="评优奖" :value="3" />
                <el-option label="其他一次性奖金" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="奖金金额" prop="bonusAmount">
          <el-input-number v-model="form.bonusAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发放原因">
          <el-input v-model="form.bonusReason" placeholder="如 年度绩效A档发放" />
        </el-form-item>
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
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getYearBonusPageApi, addYearBonusApi, updateYearBonusApi, submitYearBonusAuditApi, type HrYearBonusVO } from '@/api/hrSalary'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData, resetQuery } = useTable<HrYearBonusVO>(getYearBonusPageApi, {
  bonusYear: new Date().getFullYear(), pageNum: 1, pageSize: 10
})
function handleReset() { query.bonusYear = undefined; resetQuery(); loadData() }

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const emptyForm = (): Partial<HrYearBonusVO> => ({
  employeeId: undefined, employeeName: '', bonusYear: new Date().getFullYear(),
  bonusType: 1, bonusAmount: 0, bonusReason: '', remark: ''
})
const form = reactive<Partial<HrYearBonusVO>>(emptyForm())

const rules: FormRules = {
  employeeId: [{ required: true, message: '请输入员工ID', trigger: 'change' }],
  bonusYear: [{ required: true, message: '请选择年度', trigger: 'change' }],
  bonusType: [{ required: true, message: '请选择奖金类型', trigger: 'change' }],
  bonusAmount: [{ required: true, message: '请输入奖金金额', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑年终奖' : '新增年终奖')
function bonusTypeText(v: number) { return ({ 1: '年终奖', 2: '项目奖', 3: '评优奖', 4: '其他' } as Record<number, string>)[v] || '-' }
function applyStatusText(v: number) { return ({ 1: '已生效', 0: '审批中', 2: '已驳回', 3: '已撤回' } as Record<number, string>)[v] || '-' }
function applyStatusType(v: number) { return ({ 1: 'success', 0: 'warning', 2: 'danger', 3: 'info' } as Record<number, string>)[v] || 'info' }

function openAdd() { isEdit.value = false; Object.assign(form, emptyForm()); dialogVisible.value = true }
function openEdit(row: HrYearBonusVO) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateYearBonusApi(form)
        ElMessage.success('修改成功')
      } else {
        await addYearBonusApi(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally { submitting.value = false }
  })
}

async function handleSubmitAudit(row: HrYearBonusVO) {
  await ElMessageBox.confirm(`确定提交员工「${row.employeeName}」的${bonusTypeText(row.bonusType)}去审批吗？审批通过后生效。`, '提交审批', { type: 'info' })
  await submitYearBonusAuditApi(row.id)
  ElMessage.success('已提交审批')
  loadData()
}
</script>

<style scoped>
.g-money { font-weight: 600; color: #e6a23c; }
.b-tip { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px; }
</style>
