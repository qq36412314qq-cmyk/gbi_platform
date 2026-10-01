<!--
  加班申请页面
  数据表: hr_overtime_apply
  权限: hr:overtime:apply:*
-->
<template>
  <div class="overtime-page">
    <SearchBar :model="query" @search="loadData" @reset="handleReset" inline>
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width:140px">
          <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="加班日期">
        <el-date-picker v-model="query.overtimeDateStart" type="date" value-format="YYYY-MM-DD" placeholder="开始" style="width:130px" />
        <span style="margin:0 4px">~</span>
        <el-date-picker v-model="query.overtimeDateEnd" type="date" value-format="YYYY-MM-DD" placeholder="结束" style="width:130px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
          <el-option label="待审批" :value="0" /><el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" /><el-option label="已撤回" :value="3" /><el-option label="已取消" :value="4" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <div class="page-actions">
      <AuthBtn permission="hr:overtime:apply:add" type="primary" @click="openAddDialog">新增加班申请</AuthBtn>
    </div>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe size="small">
        <el-table-column prop="employeeName" label="姓名" width="90" align="center" />
        <el-table-column prop="overtimeDate" label="加班日期" width="110" align="center" sortable />
        <el-table-column label="时段" width="140" align="center">
          <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="expectedHours" label="预计时长(h)" width="100" align="center" sortable />
        <el-table-column prop="overtimeTypeText" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="overtimeTypeTag(row.overtimeType)">{{ row.overtimeTypeText ?? overtimeTypeMap[row.overtimeType] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="加班事由" min-width="160" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="85" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ row.statusText ?? statusMap[row.status] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" align="center" sortable />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:overtime:apply:edit" link size="small" @click="openEditDialog(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:overtime:apply:revoke" link size="small" :disabled="row.status !== 0" @click="handleRevoke(row)">撤回</AuthBtn>
            <AuthBtn permission="hr:overtime:apply:delete" link size="small" type="danger" :disabled="row.status !== 0" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog v-model="formVisible" :title="formId ? '编辑加班申请' : '新增加班申请'" :loading="saving" @confirm="handleSave">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="padding-right:12px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="form.employeeId" placeholder="请选择员工" filterable style="width:100%" @change="onSelectEmployee">
            <el-option v-for="e in employeeList" :key="e.id" :label="`${e.name}（${e.employeeNo}）`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="加班日期" prop="overtimeDate">
          <el-date-picker v-model="form.overtimeDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段" required>
          <el-col :span="11">
            <el-form-item prop="startTime"><el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="开始" style="width:100%" /></el-form-item></el-col>
          <el-col :span="2" style="text-align:center;line-height:32px">-</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime"><el-time-picker v-model="form.endTime" value-format="HH:mm:ss" placeholder="结束" style="width:100%" /></el-form-item></el-col>
        </el-form-item>
        <el-form-item label="预计时长" prop="expectedHours">
          <el-input-number v-model="form.expectedHours" :min="0.5" :step="0.5" style="width:120px" />
          <span style="margin-left:8px;color:#909399">小时</span>
        </el-form-item>
        <el-form-item label="加班类型">
          <el-radio-group v-model="form.overtimeType">
            <el-radio :value="1">工作日（1.5倍）</el-radio>
            <el-radio :value="2">休息日（2倍）</el-radio>
            <el-radio :value="3">法定节假日（3倍）</el-radio>
          </el-radio-group>
          <span style="margin-left:12px;color:#909399;font-size:12px">留空按日期自动判断</span>
        </el-form-item>
        <el-form-item label="加班事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入加班事由" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import type { EmployeeVO, HrOvertimeApplyVO, HrOvertimeApplyQueryDTO, HrOvertimeApplyAddDTO } from '@/api/hr'
import SearchBar from '@/components/common/SearchBar.vue'
import TablePage from '@/components/common/TablePage.vue'
import AuthBtn from '@/components/common/AuthBtn.vue'
import CommonDialog from '@/components/common/CommonDialog.vue'

/* ========== 列表 ========== */
const fetchApi = (params: HrOvertimeApplyQueryDTO) => api.getOvertimeApplyPageApi(params)
const { query, records, total, loading, loadData } = useTable<HrOvertimeApplyVO>(fetchApi, { pageNum: 1, pageSize: 20 })

/* ========== 员工下拉 ========== */
const employeeList = ref<EmployeeVO[]>([])
const loadEmployees = async () => {
  try { employeeList.value = await api.getEmployeeListApi() } catch { employeeList.value = [] }
}

/* ========== 新增/编辑 ========== */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const formId = ref<number | undefined>()

const initForm = (): HrOvertimeApplyAddDTO => ({
  employeeId: undefined as any, employeeName: '', overtimeDate: '',
  startTime: '', endTime: '', expectedHours: 0, overtimeType: undefined, reason: ''
})
const form = reactive(initForm())

const rules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  overtimeDate: [{ required: true, message: '请选择加班日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请输入开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请输入结束时间', trigger: 'blur' }],
  expectedHours: [{ required: true, message: '请输入预计时长', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入加班事由', trigger: 'blur' }]
}

const openAddDialog = () => {
  formId.value = undefined
  Object.assign(form, initForm())
  formVisible.value = true
}

const openEditDialog = (row: HrOvertimeApplyVO) => {
  formId.value = row.id
  Object.assign(form, {
    employeeId: row.employeeId, employeeName: row.employeeName,
    overtimeDate: row.overtimeDate?.toString().slice(0, 10) ?? '',
    startTime: row.startTime?.slice(11, 19) ?? '',
    endTime: row.endTime?.slice(11, 19) ?? '',
    expectedHours: Number(row.expectedHours),
    overtimeType: row.overtimeType, reason: row.reason
  })
  formVisible.value = true
}

const onSelectEmployee = (id: number) => {
  const emp = employeeList.value.find(e => e.id === id)
  if (emp) form.employeeName = emp.name
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (formId.value) {
        await api.editOvertimeApplyApi({ ...form, id: formId.value })
        ElMessage.success('编辑成功')
      } else {
        await api.addOvertimeApplyApi(form)
        ElMessage.success('申请提交成功，等待审批')
      }
      formVisible.value = false
      loadData()
    } finally { saving.value = false }
  })
}

/* ========== 撤回/删除 ========== */
const handleRevoke = async (row: HrOvertimeApplyVO) => {
  await ElMessageBox.confirm(`确定撤回「${row.overtimeDate}」的加班申请吗？`, '提示', { type: 'warning' })
  try { await api.revokeOvertimeApplyApi(row.id); ElMessage.success('撤回成功'); loadData() } catch {}
}

const handleDelete = async (row: HrOvertimeApplyVO) => {
  await ElMessageBox.confirm('确定删除该加班申请吗？此操作不可恢复。', '提示', { type: 'warning' })
  try { await api.deleteOvertimeApplyApi(row.id); ElMessage.success('删除成功'); loadData() } catch {}
}

const handleReset = () => { loadData() }

/* ========== 工具函数 ========== */
const overtimeTypeMap: Record<number, string> = { 1: '工作日', 2: '休息日', 3: '法定节假日' }
const overtimeTypeTag = (v?: number) => (v === 2 || v === 3) ? 'warning' : 'info'
const statusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回', 3: '已撤回', 4: '已取消' }
const statusTag = (v?: number) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'info' }[v ?? 0] ?? '')

onMounted(() => { loadEmployees(); loadData() })
</script>

<style scoped>
.overtime-page { padding: 0; }
.page-actions { margin: 12px 0; }
</style>
