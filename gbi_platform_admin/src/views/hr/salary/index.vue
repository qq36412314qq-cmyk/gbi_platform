<template>
  <div class="g-page-wrap">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="薪资档案" name="archive">
        <div class="g-page-header">
          <span class="g-page-title">薪资档案</span>
          <AuthBtn permission="hr:salary:archive:add" type="primary" @click="openAddArchive">新增档案</AuthBtn>
        </div>
        <SearchBar :model="archiveQuery" @search="loadArchiveData" @reset="() => { archiveQuery.pageNum = 1; loadArchiveData() }">
          <el-form-item label="员工">
            <el-input v-model="archiveQuery.employeeId" placeholder="员工ID" clearable style="width:120px" />
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="archiveQuery.pageNum" v-model:page-size="archiveQuery.pageSize" :total="archiveTotal" @refresh="loadArchiveData">
          <el-table v-loading="archiveLoading" :data="archiveRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="basicSalary" label="基本工资" width="110" align="right" />
            <el-table-column prop="performanceSalary" label="绩效工资" width="110" align="right" />
            <el-table-column prop="positionAllowance" label="岗位津贴" width="110" align="right" />
            <el-table-column prop="otherAllowance" label="其他津贴" width="110" align="right" />
            <el-table-column prop="socialSecurityPersonal" label="社保个人" width="110" align="right" />
            <el-table-column prop="housingFundPersonal" label="公积金个人" width="110" align="right" />
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:salary:archive:edit" link type="primary" size="small" @click="openEditArchive(row)">编辑</AuthBtn>
                <AuthBtn permission="hr:salary:archive:delete" link type="danger" size="small" @click="handleDeleteArchive(row)">删除</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="月度薪资" name="month">
        <div class="g-page-header">
          <span class="g-page-title">月度薪资</span>
          <AuthBtn permission="hr:salary:month:generate" type="primary" @click="openGenerate">生成薪资</AuthBtn>
          <AuthBtn permission="hr:salary:month:export" @click="handleExportMonth">导出</AuthBtn>
        </div>
        <SearchBar :model="monthQuery" @search="loadMonthData" @reset="() => { monthQuery.pageNum = 1; loadMonthData() }">
          <el-form-item label="薪资月份">
            <el-date-picker v-model="monthQuery.salaryMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="monthQuery.pageNum" v-model:page-size="monthQuery.pageSize" :total="monthTotal" @refresh="loadMonthData">
          <el-table v-loading="monthLoading" :data="monthRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="salaryMonth" label="薪资月份" width="110" align="center" />
            <el-table-column prop="grossAmount" label="应发金额" width="110" align="right" />
            <el-table-column prop="netAmount" label="实发金额" width="110" align="right" />
            <el-table-column prop="payStatusText" label="发放状态" width="100" align="center">
              <template #default="{ row }"><el-tag size="small" :type="row.payStatus === 1 ? 'success' : 'info'">{{ row.payStatusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:salary:month:pay" link type="primary" size="small" @click="handlePay(row)" :disabled="row.payStatus === 1">发放</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="archiveDialogVisible" :title="archiveForm.id ? '编辑薪资档案' : '新增薪资档案'" width="520px" :close-on-click-modal="false">
      <el-form ref="archiveFormRef" :model="archiveForm" :rules="archiveRules" label-width="110px">
        <el-form-item label="员工ID" prop="employeeId"><el-input-number v-model="archiveForm.employeeId" style="width:100%" /></el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="archiveForm.basicSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="绩效工资"><el-input-number v-model="archiveForm.performanceSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="岗位津贴"><el-input-number v-model="archiveForm.positionAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="其他津贴"><el-input-number v-model="archiveForm.otherAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="社保个人"><el-input-number v-model="archiveForm.socialSecurityPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="公积金个人"><el-input-number v-model="archiveForm.housingFundPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="archiveDialogVisible=false">取消</el-button><el-button type="primary" :loading="archiveSubmitLoading" @click="handleArchiveSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="generateDialogVisible" title="生成月度薪资" width="400px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="薪资月份"><el-date-picker v-model="generateMonth" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="generateDialogVisible=false">取消</el-button><el-button type="primary" :loading="generateLoading" @click="handleGenerate">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const activeTab = ref<'archive' | 'month'>('archive')

/* ---- 薪资档案 ---- */
const archiveQuery = reactive({ pageNum: 1, pageSize: 20, employeeId: undefined as number | undefined })
const { records: archiveRecords, total: archiveTotal, loading: archiveLoading, loadData: loadArchiveData } = useTable(api.getSalaryArchivePageApi, archiveQuery)
const archiveDialogVisible = ref(false)
const archiveSubmitLoading = ref(false)
const archiveFormRef = ref()
const archiveForm = reactive<api.SalaryArchiveDTO>({ employeeId: 0, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0, socialSecurityPersonal: 0, housingFundPersonal: 0 })
const archiveRules = { employeeId: [{ required: true, message: '员工ID不能为空' }] }
const openAddArchive = () => { Object.assign(archiveForm, { id: undefined, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0, socialSecurityPersonal: 0, housingFundPersonal: 0 }); archiveDialogVisible.value = true }
const openEditArchive = (row: api.SalaryArchiveVO) => { Object.assign(archiveForm, { id: row.id, employeeId: row.employeeId, basicSalary: row.basicSalary, performanceSalary: row.performanceSalary, positionAllowance: row.positionAllowance, otherAllowance: row.otherAllowance, socialSecurityPersonal: row.socialSecurityPersonal, housingFundPersonal: row.housingFundPersonal }); archiveDialogVisible.value = true }
const handleDeleteArchive = (row: api.SalaryArchiveVO) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteSalaryArchiveApi(row.id!); ElMessage.success('删除成功'); loadArchiveData() }) }
const handleArchiveSubmit = async () => { await archiveFormRef.value.validate(); archiveSubmitLoading.value = true; try { if (archiveForm.id) { await api.updateSalaryArchiveApi(archiveForm); ElMessage.success('编辑成功') } else { await api.addSalaryArchiveApi(archiveForm); ElMessage.success('新增成功') } archiveDialogVisible.value = false; loadArchiveData() } finally { archiveSubmitLoading.value = false } }

/* ---- 月度薪资 ---- */
const monthQuery = reactive({ pageNum: 1, pageSize: 20, employeeId: undefined as number | undefined, salaryMonth: undefined as string | undefined })
const { records: monthRecords, total: monthTotal, loading: monthLoading, loadData: loadMonthData } = useTable(api.getSalaryMonthPageApi, monthQuery)
const generateDialogVisible = ref(false)
const generateMonth = ref('')
const generateLoading = ref(false)
const openGenerate = () => { generateMonth.value = new Date().toISOString().slice(0, 7); generateDialogVisible.value = true }
const handleGenerate = async () => { generateLoading.value = true; try { await api.generateSalaryMonthApi({ employeeIds: [], salaryMonth: generateMonth.value }); ElMessage.success('生成成功'); generateDialogVisible.value = false; loadMonthData() } finally { generateLoading.value = false } }
const handlePay = (row: api.SalaryMonthVO) => { ElMessageBox.confirm(`确认发放 ${row.employeeName} ${row.salaryMonth} 薪资?`, '提示').then(async () => { await api.paySalaryMonthApi(row.id!); ElMessage.success('发放成功'); loadMonthData() }) }
const handleExportMonth = async () => { try { await api.exportSalaryMonthApi({ employeeId: monthQuery.employeeId, salaryMonth: monthQuery.salaryMonth }) } catch {} }
</script>
