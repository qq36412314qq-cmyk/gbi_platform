<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">批量调薪</span>
      <el-button type="primary" @click="openCreate">创建批量调薪任务</el-button>
    </div>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="adjustName" label="任务名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="调整方式" width="110" align="center">
          <template #default="{ row }">{{ row.adjustMode === 1 ? '统一比例' : '统一固定金额' }}</template>
        </el-table-column>
        <el-table-column label="调整值" width="100" align="right">
          <template #default="{ row }">
            {{ row.adjustMode === 1 ? (Number(row.adjustValue) * 100).toFixed(1) + '%' : Number(row.adjustValue).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="110" align="center" />
        <el-table-column label="总人数" width="80" align="center" prop="totalCount" />
        <el-table-column label="成功/失败" width="110" align="center">
          <template #default="{ row }">
            <span class="g-text-success">{{ row.successCount }}</span> /
            <span class="g-text-danger">{{ row.failCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" size="small" :disabled="row.status !== 0" @click="handleSubmit(row)">提交审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 创建弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建批量调薪任务（页面筛选方式，Excel导入二期）" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="adjustName">
          <el-input v-model="form.adjustName" placeholder="如 2026年度全员调薪" />
        </el-form-item>
        <el-form-item label="调整方式" prop="adjustMode">
          <el-radio-group v-model="form.adjustMode">
            <el-radio :value="1">统一比例（%）</el-radio>
            <el-radio :value="2">统一固定金额</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整值" prop="adjustValue">
          <el-input-number v-model="form.adjustValue" :min="0" :precision="4" :step="form.adjustMode === 1 ? 0.01 : 100" style="width: 100%" />
          <div class="b-tip" v-if="form.adjustMode === 1">比例输入示例：0.08 表示 8%</div>
        </el-form-item>
        <el-form-item label="目标薪酬级别">
          <el-select v-model="form.targetGradeCode" placeholder="不限制" clearable style="width: 100%">
            <el-option v-for="g in gradeOptions" :key="g.gradeCode" :label="g.gradeCode + ' ' + g.gradeName" :value="g.gradeCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreateSubmit">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getBatchAdjustPageApi, createBatchAdjustApi, submitBatchAdjustAuditApi,
  getSalaryGradeListApi,
  type HrSalaryBatchAdjustVO, type HrSalaryGradeVO
} from '@/api/hrSalary'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData } = useTable<HrSalaryBatchAdjustVO>(getBatchAdjustPageApi, { pageNum: 1, pageSize: 10 })

const gradeOptions = ref<HrSalaryGradeVO[]>([])
onMounted(async () => { try { gradeOptions.value = await getSalaryGradeListApi() } catch {} })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const emptyForm = (): Partial<HrSalaryBatchAdjustVO> => ({
  adjustName: '', adjustMode: 1, adjustValue: 0, targetGradeCode: '',
  effectiveDate: undefined, remark: ''
})
const form = reactive<Partial<HrSalaryBatchAdjustVO>>(emptyForm())

const rules: FormRules = {
  adjustName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  adjustMode: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
  adjustValue: [{ required: true, message: '请输入调整值', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

function statusText(v: number) { return ({ 0: '草稿', 1: '审批中', 2: '已执行', 3: '已驳回', 4: '已通过待执行', 5: '已撤回' } as Record<number, string>)[v] || '-' }
function statusType(v: number) { return ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: '', 5: 'info' } as Record<number, string>)[v] || 'info' }

function openCreate() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function handleCreateSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await createBatchAdjustApi({ ...form, totalCount: 0, successCount: 0, failCount: 0, status: 0 })
      ElMessage.success('创建成功，请提交审批后执行')
      dialogVisible.value = false
      loadData()
    } finally { submitting.value = false }
  })
}

async function handleSubmit(row: HrSalaryBatchAdjustVO) {
  await ElMessageBox.confirm(`确定提交任务「${row.adjustName}」去审批吗？审批通过后可以执行批量调薪。`, '提交审批', { type: 'info' })
  await submitBatchAdjustAuditApi(row.id)
  ElMessage.success('已提交审批')
  loadData()
}
</script>
