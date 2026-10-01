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
            <el-table-column prop="effectiveDate" label="生效日期" width="105" align="center" />
            <el-table-column label="来源" width="95" align="center">
              <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="是否生效" width="85" align="center">
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
        <el-collapse v-model="salaryHelpActive" accordion style="margin-bottom:12px">
          <el-collapse-item title="📖 薪资计算说明" name="help">
            <template #title>
              <span style="font-size:13px;color:#606266">薪资计算说明</span>
              <el-tag size="small" type="info" style="margin-left:8px">点击展开</el-tag>
            </template>
            <div style="font-size:13px;line-height:1.8;padding:8px">
              <ul style="margin:0;padding-left:20px">
                <li><b>数据来源</b>：从「薪资档案」读取员工基本工资、绩效工资、岗位津贴、其他津贴</li>
                <li><b>社保计算</b>：根据员工城市代码，从「社保参数配置」读取个人/公司缴纳比例，基数按工资总额上下限 Clamp</li>
                <li><b>公积金计算</b>：从「公积金配置」读取缴存比例，基数同样受上下限约束</li>
                <li><b>个税计算</b>：采用累计预扣法，扣除社保个人部分+公积金个人部分+5000元起征点</li>
                <li><b>考勤扣款</b>：勾选「同步考勤扣款」后，根据「考勤记录」计算迟到/早退/缺卡扣款（关联薪资模板的skipAttendance、latePenaltyRate等参数）</li>
                <li><b>加班补偿</b>：勾选「同步加班费」后，汇总当月已核算的加班补偿台账（hr_overtime_compensate），将加班费金额加入实发合计</li>
                <li><b>最低保护</b>：实发金额不得低于当地最低工资标准，超出部分自动调整扣款</li>
                <li><b>前置条件</b>：生成前请确保「薪资档案」已维护，「社保参数」和「公积金配置」已初始化</li>
              </ul>
            </div>
          </el-collapse-item>
        </el-collapse>
        <SearchBar :model="monthQuery" @search="loadMonthData" @reset="() => { monthQuery.pageNum = 1; loadMonthData() }">
          <el-form-item label="薪资月份">
            <el-date-picker v-model="monthQuery.salaryMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="monthQuery.pageNum" v-model:page-size="monthQuery.pageSize" :total="monthTotal" @refresh="loadMonthData">
          <el-table v-loading="monthLoading" :data="monthRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" fixed="left" />
            <el-table-column prop="salaryMonth" label="薪资月份" width="110" align="center" fixed="left" />
            <el-table-column label="基本工资" width="100" align="right">
              <template #default="{ row }">{{ Number(row.basicSalary ?? 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="绩效工资" width="100" align="right">
              <template #default="{ row }">{{ Number(row.performanceSalary ?? 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="补贴合计" width="100" align="right">
              <template #default="{ row }">{{ Number(row.allowanceAmount ?? 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="应发金额" width="110" align="right">
              <template #default="{ row }"><b>{{ Number(row.grossAmount ?? 0).toFixed(2) }}</b></template>
            </el-table-column>
            <el-table-column label="社保扣款" width="100" align="right">
              <template #default="{ row }"><span style="color:#f56c6c">{{ Number(row.socialSecurity ?? 0).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="公积金扣款" width="110" align="right">
              <template #default="{ row }"><span style="color:#f56c6c">{{ Number(row.housingFund ?? 0).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="个税" width="90" align="right">
              <template #default="{ row }"><span style="color:#f56c6c">{{ Number(row.taxAmount ?? 0).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="考勤扣款" width="110" align="right" show-overflow-tooltip>
              <template #default="{ row }">
                <div style="line-height:1.3">
                  <div><span style="color:#f56c6c;font-weight:600">{{ Number(row.attendanceDeduction ?? 0).toFixed(2) }}</span></div>
                  <div style="color:#909399;font-size:11px;line-height:1.4">
                    旷工{{ Number(row.absentDeduction ?? 0).toFixed(2) }} / 迟到{{ Number(row.lateDeduction ?? 0).toFixed(2) }}
                  </div>
                  <div style="color:#909399;font-size:11px;line-height:1.4">
                    早退{{ Number(row.earlyDeduction ?? 0).toFixed(2) }} / 事假{{ Number(row.unpaidLeaveDeduction ?? 0).toFixed(2) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="最低工资保护" width="110" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.minWageProtected === 1" size="small" type="warning">已触发</el-tag>
                <span v-else style="color:#c0c4cc">-</span>
              </template>
            </el-table-column>
            <el-table-column label="实发金额" width="120" align="right">
              <template #default="{ row }"><span style="color:#e6a23c;font-weight:700;font-size:14px">{{ Number(row.netAmount ?? 0).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column prop="payStatusText" label="发放状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.payStatus === 1 ? 'success' : row.payStatus === 2 ? 'danger' : 'info'">{{ row.payStatusText || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="生成时间" width="155" align="center" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:salary:month:view" link type="primary" size="small" @click="openDetail(row)">明细</AuthBtn>
                <AuthBtn permission="hr:salary:month:pay" link type="success" size="small" :disabled="row.payStatus !== 0" @click="handlePay(row)">发放</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
    </el-tabs>

    <!-- 薪资档案弹窗 -->
    <el-dialog v-model="archiveDialogVisible" :title="archiveForm.id ? '编辑薪资档案' : '新增薪资档案'" width="560px" :close-on-click-modal="false">
      <el-form ref="archiveFormRef" :model="archiveForm" :rules="archiveRules" label-width="110px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="archiveForm.employeeId" placeholder="请选择员工" filterable style="width:100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资模板">
          <el-select v-model="archiveForm.ruleId" placeholder="请选择" filterable style="width:100%" @change="handleRuleChange">
            <el-option v-for="r in ruleOptions" :key="r.id" :label="r.ruleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="archiveForm.basicSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="绩效工资"><el-input-number v-model="archiveForm.performanceSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="岗位津贴"><el-input-number v-model="archiveForm.positionAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="其他津贴"><el-input-number v-model="archiveForm.otherAllowance" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="生效日期"><el-date-picker v-model="archiveForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="archiveForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="archiveDialogVisible=false">取消</el-button><el-button type="primary" :loading="archiveSubmitLoading" @click="handleArchiveSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 生成月度薪资弹窗 -->
    <el-dialog v-model="generateDialogVisible" title="生成月度薪资" width="400px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="核算月份"><el-date-picker v-model="generateMonth" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item>
        <el-form-item label="同步考勤扣款"><el-switch v-model="syncAttendance" /></el-form-item>
        <el-form-item label="同步加班费"><el-switch v-model="syncOvertime" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="generateDialogVisible=false">取消</el-button><el-button type="primary" :loading="generateLoading" @click="handleGenerate">确定</el-button></template>
    </el-dialog>

    <!-- 月度薪资明细弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="薪资明细" width="720px">
      <template v-if="detailRow">
        <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="员工">{{ detailRow.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="薪资月份">{{ detailRow.salaryMonth }}</el-descriptions-item>
          <el-descriptions-item label="发放状态">
            <el-tag size="small" :type="detailRow.payStatus === 1 ? 'success' : detailRow.payStatus === 2 ? 'danger' : 'info'">{{ detailRow.payStatusText || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="基本工资">¥{{ Number(detailRow.basicSalary ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="绩效工资">¥{{ Number(detailRow.performanceSalary ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="补贴合计">¥{{ Number(detailRow.allowanceAmount ?? 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">应发合计</el-divider>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="应发金额">
            <span style="color:#e6a23c;font-weight:700;font-size:16px">¥{{ Number(detailRow.grossAmount ?? 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="实发金额" :span="2">
            <span style="color:#67c23a;font-weight:700;font-size:16px">¥{{ Number(detailRow.netAmount ?? 0).toFixed(2) }}</span>
            <span v-if="detailRow.minWageProtected === 1" style="margin-left:8px"><el-tag size="small" type="warning">触发最低工资保护</el-tag></span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">扣款明细</el-divider>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="社保扣款">¥{{ Number(detailRow.socialSecurity ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="公积金扣款">¥{{ Number(detailRow.housingFund ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="个税">¥{{ Number(detailRow.taxAmount ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="考勤扣款">¥{{ Number(detailRow.attendanceDeduction ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="旷工扣款">¥{{ Number(detailRow.absentDeduction ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="迟到扣款">¥{{ Number(detailRow.lateDeduction ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="早退扣款">¥{{ Number(detailRow.earlyDeduction ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="无薪事假扣款">¥{{ Number(detailRow.unpaidLeaveDeduction ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="其他扣款">¥{{ Number(detailRow.deductionAmount ?? 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">发放信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="生成时间">{{ detailRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际发放时间">{{ detailRow.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer><el-button @click="detailDialogVisible=false">关闭</el-button></template>
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
const salaryHelpActive = ref<string[]>([]) // 默认收起

/* ---- 薪资档案 ---- */
const { query: archiveQuery, records: archiveRecords, total: archiveTotal, loading: archiveLoading, loadData: loadArchiveData } = useTable(api.getSalaryArchivePageApi, {
  pageSize: 20,
  keyword: '',
  gradeCode: undefined as string | undefined,
})
const archiveDialogVisible = ref(false)
const archiveSubmitLoading = ref(false)
const archiveFormRef = ref()
const archiveForm = reactive<api.SalaryArchiveDTO>({ employeeId: 0, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0 })
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
  }
}
const openAddArchive = () => { loadEmployeeOptions(); loadRuleOptions(); const today = new Date().toISOString().slice(0, 10); Object.assign(archiveForm, { id: undefined, basicSalary: 0, performanceSalary: 0, positionAllowance: 0, otherAllowance: 0, effectiveDate: today }); archiveDialogVisible.value = true }
const openEditArchive = (row: api.SalaryArchiveVO) => { loadEmployeeOptions(); loadRuleOptions(); Object.assign(archiveForm, { id: row.id, employeeId: row.employeeId, ruleId: row.ruleId, basicSalary: row.basicSalary, performanceSalary: row.performanceSalary, positionAllowance: row.positionAllowance, otherAllowance: row.otherAllowance, effectiveDate: row.effectiveDate, gradeCode: row.gradeCode, gradeName: row.gradeName }); archiveDialogVisible.value = true }
const handleDeleteArchive = (row: api.SalaryArchiveVO) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteSalaryArchiveApi(row.id!); ElMessage.success('删除成功'); loadArchiveData() }) }
const handleSubmitArchiveAudit = (row: api.SalaryArchiveVO) => { ElMessageBox.confirm('确认提交该档案变更审批?', '提示').then(async () => { await salaryApi.submitSalaryArchiveAuditApi(row.id!); ElMessage.success('已提交审批'); loadArchiveData() }) }
const handleArchiveSubmit = async () => { await archiveFormRef.value.validate(); archiveSubmitLoading.value = true; try { if (archiveForm.id) { await api.updateSalaryArchiveApi(archiveForm); ElMessage.success('编辑成功') } else { await api.addSalaryArchiveApi(archiveForm); ElMessage.success('新增成功') } archiveDialogVisible.value = false; loadArchiveData() } finally { archiveSubmitLoading.value = false } }

/* ---- 月度薪资 ---- */
const { query: monthQuery, records: monthRecords, total: monthTotal, loading: monthLoading, loadData: loadMonthData } = useTable(api.getSalaryMonthPageApi, {
  pageSize: 20,
  employeeId: undefined as number | undefined,
  salaryMonth: undefined as string | undefined,
})
const generateDialogVisible = ref(false)
const generateMonth = ref('')
const syncAttendance = ref(true)
const syncOvertime = ref(false)
const generateLoading = ref(false)
const detailDialogVisible = ref(false)
const detailRow = ref<api.SalaryMonthVO | null>(null)
const openGenerate = () => { generateMonth.value = new Date().toISOString().slice(0, 7); generateDialogVisible.value = true }
const handleGenerate = async () => {
  const payload = {
    employeeIds: [] as number[],
    salaryMonth: generateMonth.value,
    syncAttendance: (syncAttendance.value ? 1 : 0) as number,
    syncOvertime: (syncOvertime.value ? 1 : 0) as number,
  }
  generateLoading.value = true
  try {
    await api.generateSalaryMonthApi(payload)
    ElMessage.success('生成成功')
    generateDialogVisible.value = false
    loadMonthData()
  } finally { generateLoading.value = false }
}
const handlePay = (row: api.SalaryMonthVO) => { ElMessageBox.confirm(`确认发放 ${row.employeeName} ${row.salaryMonth} 薪资?`, '提示').then(async () => { await api.paySalaryMonthApi(row.id!); ElMessage.success('发放成功'); loadMonthData() }) }
const handleExportMonth = async () => {
  const params = { employeeId: monthQuery.employeeId as number | undefined, salaryMonth: monthQuery.salaryMonth as string | undefined }
  try { await api.exportSalaryMonthApi(params) } catch {}
}
const openDetail = (row: api.SalaryMonthVO) => { detailRow.value = row; detailDialogVisible.value = true }
</script>
