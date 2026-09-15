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
            <el-input v-model="archiveQuery.keyword" placeholder="姓名/工号" clearable style="width:140px" />
          </el-form-item>
          <el-form-item label="薪酬级别">
            <el-select v-model="archiveQuery.gradeCode" placeholder="全部" clearable style="width:120px">
              <el-option v-for="g in gradeOptions" :key="g.gradeCode" :label="g.gradeCode" :value="g.gradeCode" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="archiveQuery.pageNum" v-model:page-size="archiveQuery.pageSize" :total="archiveTotal" @refresh="loadArchiveData">
          <el-table v-loading="archiveLoading" :data="archiveRecords" border stripe>
            <el-table-column prop="employeeName" label="员工" width="110" show-overflow-tooltip />
            <el-table-column prop="versionNo" label="版本" width="60" align="center" />
            <el-table-column label="薪酬级别" width="100" align="center">
              <template #default="{ row }"><el-tag size="small" v-if="row.gradeCode">{{ row.gradeCode }}</el-tag><span v-else>-</span></template>
            </el-table-column>
            <el-table-column label="基本工资" width="90" align="right"><template #default="{ row }">{{ Number(row.basicSalary).toFixed(2) }}</template></el-table-column>
            <el-table-column label="绩效工资" width="90" align="right"><template #default="{ row }">{{ Number(row.performanceSalary).toFixed(2) }}</template></el-table-column>
            <el-table-column label="岗位津贴" width="90" align="right"><template #default="{ row }">{{ Number(row.positionAllowance).toFixed(2) }}</template></el-table-column>
            <el-table-column label="其他津贴" width="90" align="right"><template #default="{ row }">{{ Number(row.otherAllowance).toFixed(2) }}</template></el-table-column>
            <el-table-column label="社保个人" width="90" align="right"><template #default="{ row }">{{ Number(row.socialSecurityPersonal).toFixed(2) }}</template></el-table-column>
            <el-table-column label="公积金个人" width="90" align="right"><template #default="{ row }">{{ Number(row.housingFundPersonal).toFixed(2) }}</template></el-table-column>
            <el-table-column prop="effectiveDate" label="生效日期" width="105" align="center" />
            <el-table-column label="来源" width="95" align="center">
              <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="当前版本" width="85" align="center">
              <template #default="{ row }"><el-tag size="small" :type="row.isCurrent === 1 ? 'success' : 'info'">{{ row.isCurrent === 1 ? '是' : '历史' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="155" align="center" />
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:salary:archive:edit" link type="primary" size="small" @click="openEditArchive(row)">编辑</AuthBtn>
                <AuthBtn permission="hr:salary:archive:edit" link type="warning" size="small" :disabled="row.isCurrent !== 1" @click="handleSubmitArchiveAudit(row)">提交审批</AuthBtn>
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
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="archiveForm.employeeId" filterable placeholder="请输入员工姓名搜索" style="width:100%">
            <el-option v-for="item in employeeOptions" :key="item.id" :label="`${item.name} (${item.employeeNo})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资模板" prop="ruleId">
          <el-select v-model="archiveForm.ruleId" placeholder="请选择薪资模板" clearable style="width:100%" @change="handleRuleChange">
            <el-option v-for="item in ruleOptions" :key="item.id" :label="item.ruleName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="archiveForm.basicSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="绩效工资"><el-input-number v-model="archiveForm.performanceSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="岗位津贴"><el-input-number v-model="archiveForm.positionAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="其他津贴"><el-input-number v-model="archiveForm.otherAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="社保个人"><el-input-number v-model="archiveForm.socialSecurityPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="公积金个人"><el-input-number v-model="archiveForm.housingFundPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="archiveForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="请选择生效日期" />
        </el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import * as salaryApi from '@/api/hrSalary'

const activeTab = ref<'archive' | 'month'>('archive')

/* ---- 薪资档案 ---- */
const archiveQuery = reactive({ pageNum: 1, pageSize: 20, keyword: '', gradeCode: undefined as string | undefined })
const { records: archiveRecords, total: archiveTotal, loading: archiveLoading, loadData: loadArchiveData } = useTable(api.getSalaryArchivePageApi, archiveQuery)
const archiveDialogVisible = ref(false)
const archiveSubmitLoading = ref(false)
const archiveFormRef = ref()
const archiveForm = reactive<api.SalaryArchiveDTO>({ employeeId: 0, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0, socialSecurityPersonal: 0, housingFundPersonal: 0 })
const archiveRules = { employeeId: [{ required: true, message: '员工不能为空' }] }
const employeeOptions = ref<api.EmployeeVO[]>([])
const ruleOptions = ref<salaryApi.HrSalaryRuleVO[]>([])
const gradeOptions = ref<salaryApi.HrSalaryGradeVO[]>([])
const loadEmployeeOptions = async () => {
  try { const res = await api.getEmployeePageApi({ pageNum: 1, pageSize: 500, name: '' }); employeeOptions.value = res.records } catch {}
}
const loadRuleOptions = async () => {
  try { const res = await salaryApi.getSalaryRulePageApi({ pageNum: 1, pageSize: 500 }); ruleOptions.value = res.records } catch {}
}
onMounted(async () => { loadEmployeeOptions(); loadRuleOptions(); try { gradeOptions.value = await salaryApi.getSalaryGradeListApi() } catch {} })
const sourceTypeText = (v?: number) => ({ 1: '模板生成', 2: '人工录入', 3: '批量调薪', 4: '晋升调级' } as Record<number, string>)[v ?? 0] || '-'
const handleRuleChange = (ruleId: number) => {
  const rule = ruleOptions.value.find(r => r.id === ruleId)
  if (rule) {
    archiveForm.basicSalary = rule.basicSalary || 0
    archiveForm.performanceSalary = rule.performanceBase || 0
    archiveForm.positionAllowance = rule.positionAllowance || 0
    archiveForm.otherAllowance = rule.otherAllowance || 0
    const salaryBase = (rule.basicSalary || 0) + (rule.performanceBase || 0) + (rule.positionAllowance || 0) + (rule.otherAllowance || 0)
    archiveForm.socialSecurityPersonal = Math.round(salaryBase * (rule.socialSecurityRate || 0) / 100 * 100) / 100
    archiveForm.housingFundPersonal = Math.round((rule.basicSalary || 0) * (rule.housingFundRate || 0) / 100 * 100) / 100
  }
}
const openAddArchive = () => { loadEmployeeOptions(); loadRuleOptions(); const today = new Date().toISOString().slice(0, 10); Object.assign(archiveForm, { id: undefined, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0, socialSecurityPersonal: 0, housingFundPersonal: 0, effectiveDate: today }); archiveDialogVisible.value = true }
const openEditArchive = (row: api.SalaryArchiveVO) => { loadEmployeeOptions(); loadRuleOptions(); Object.assign(archiveForm, { id: row.id, employeeId: row.employeeId, ruleId: row.ruleId, basicSalary: row.basicSalary, performanceSalary: row.performanceSalary, positionAllowance: row.positionAllowance, otherAllowance: row.otherAllowance, socialSecurityPersonal: row.socialSecurityPersonal, housingFundPersonal: row.housingFundPersonal, effectiveDate: row.effectiveDate, gradeCode: row.gradeCode, gradeName: row.gradeName }); archiveDialogVisible.value = true }
const handleDeleteArchive = (row: api.SalaryArchiveVO) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteSalaryArchiveApi(row.id!); ElMessage.success('删除成功'); loadArchiveData() }) }
const handleSubmitArchiveAudit = (row: api.SalaryArchiveVO) => { ElMessageBox.confirm('确认提交该档案变更审批?', '提示').then(async () => { await salaryApi.submitSalaryArchiveAuditApi(row.id!); ElMessage.success('已提交审批'); loadArchiveData() }) }
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
