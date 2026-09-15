<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">薪酬级别管理</span>
      <el-button type="primary" @click="openAdd">新增薪酬级别</el-button>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="级别编码/名称" clearable style="width: 180px" />
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="gradeCode" label="级别编码" width="100" />
        <el-table-column prop="gradeName" label="级别名称" width="120" />
        <el-table-column prop="gradeLevel" label="层级" width="70" align="center" />
        <el-table-column label="带宽下限" width="100" align="right">
          <template #default="{ row }">{{ Number(row.bandMin).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="带宽中位" width="100" align="right">
          <template #default="{ row }">{{ Number(row.bandMid).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="带宽上限" width="100" align="right">
          <template #default="{ row }">{{ Number(row.bandMax).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" :disabled="row.status === 0" @click="handleDisable(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="级别编码" prop="gradeCode">
          <el-input v-model="form.gradeCode" placeholder="如 P1 / P2 / M1" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="级别名称" prop="gradeName">
          <el-input v-model="form.gradeName" placeholder="如 初级专员 / 经理" />
        </el-form-item>
        <el-form-item label="级别层级" prop="gradeLevel">
          <el-input-number v-model="form.gradeLevel" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="带宽下限" prop="bandMin">
          <el-input-number v-model="form.bandMin" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="带宽中位" prop="bandMid">
          <el-input-number v-model="form.bandMid" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="带宽上限" prop="bandMax">
          <el-input-number v-model="form.bandMax" :min="0" :precision="2" />
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
import {
  getSalaryGradePageApi, addSalaryGradeApi, updateSalaryGradeApi, disableSalaryGradeApi,
  type HrSalaryGradeVO
} from '@/api/hrSalary'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData, resetQuery } = useTable<HrSalaryGradeVO>(getSalaryGradePageApi, {
  keyword: '', pageNum: 1, pageSize: 10
})
function handleReset() { resetQuery(); loadData() }

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const emptyForm = (): Partial<HrSalaryGradeVO> => ({
  gradeCode: '', gradeName: '', gradeLevel: 1,
  bandMin: 0, bandMid: 0, bandMax: 0, remark: ''
})
const form = reactive<Partial<HrSalaryGradeVO>>(emptyForm())

const rules: FormRules = {
  gradeCode: [{ required: true, message: '请输入级别编码', trigger: 'blur' }],
  gradeName: [{ required: true, message: '请输入级别名称', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请输入层级', trigger: 'change' }],
  bandMin: [{ required: true, message: '请输入带宽下限', trigger: 'change' }],
  bandMax: [{ required: true, message: '请输入带宽上限', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑薪酬级别' : '新增薪酬级别')

function openAdd() {
  isEdit.value = false
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}
function openEdit(row: HrSalaryGradeVO) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleDisable(row: HrSalaryGradeVO) {
  await ElMessageBox.confirm(`确定停用级别「${row.gradeCode} - ${row.gradeName}」吗？`, '确认停用', { type: 'warning' })
  await disableSalaryGradeApi(row.id)
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
        await updateSalaryGradeApi(form)
        ElMessage.success('修改成功')
      } else {
        await addSalaryGradeApi(form)
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
