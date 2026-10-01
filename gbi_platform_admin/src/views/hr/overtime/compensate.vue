<!--
  加班补偿页面
  数据表: hr_overtime_compensate
  权限: hr:overtime:compensate:*
-->
<template>
  <div class="overtime-page">
    <SearchBar :model="query" @search="loadData" @reset="handleReset" inline>
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width:140px">
          <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="补偿月份">
        <el-date-picker v-model="query.compensateMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
      </el-form-item>
      <el-form-item label="发放状态">
        <el-select v-model="query.payStatus" placeholder="全部" clearable style="width:100px">
          <el-option label="待发放" :value="0" /><el-option label="已发放" :value="1" /><el-option label="已取消" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <div class="page-actions">
      <AuthBtn permission="hr:overtime:compensate:calculate" type="warning" :loading="calcLoading" @click="openCalcDialog">核算当月补偿</AuthBtn>
      <AuthBtn permission="hr:overtime:compensate:export" link @click="handleExport">导出明细</AuthBtn>
    </div>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe size="small">
        <el-table-column prop="employeeName" label="姓名" width="90" align="center" />
        <el-table-column prop="basicSalary" label="基准月薪" width="100" align="right">
          <template #default="{ row }">{{ row.basicSalary ? `¥${Number(row.basicSalary).toFixed(2)}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="compensateMonth" label="补偿月份" width="100" align="center" sortable />
        <el-table-column prop="totalHours" label="累计时长(h)" width="100" align="center" sortable />
        <el-table-column label="工时明细" width="180" align="center">
          <template #default="{ row }">
            <span class="hour-detail">
              平日<span class="highlight">{{ row.workdayHours ?? 0 }}</span>h
              / 休息<span class="highlight">{{ row.restdayHours ?? 0 }}</span>h
              / 法定<span class="highlight-danger">{{ row.holidayHours ?? 0 }}</span>h
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="usedHours" label="已用时(h)" width="90" align="center" />
        <el-table-column prop="remainHours" label="剩余(h)" width="90" align="center" sortable>
          <template #default="{ row }">
            <span :class="row.remainHours !== null && row.remainHours! < 1 ? 'remain-low' : ''">{{ row.remainHours ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="overtimeAmount" label="加班费(元)" width="100" align="right">
          <template #default="{ row }">{{ row.overtimeAmount ? `¥${Number(row.overtimeAmount).toFixed(2)}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="payStatusText" label="发放状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payStatusTag(row.payStatus)">{{ row.payStatusText ?? payStatusMap[row.payStatus] ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" sortable />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:overtime:compensate:pay" link size="small" type="success" :disabled="row.payStatus !== 0" @click="handlePay(row)">发放</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 核算弹窗 -->
    <CommonDialog v-model="calcVisible" title="核算加班补偿" :loading="calcSaving" @confirm="handleCalcConfirm">
      <el-form label-width="100px" style="padding-right:12px">
        <el-form-item label="补偿月份">
          <el-date-picker v-model="calcMonth" type="month" value-format="YYYY-MM" placeholder="选择核算月份" style="width:200px" />
        </el-form-item>
        <div class="calc-note">
          <el-alert title="核算将基于当前月份所有已确认的加班记录，按配置的加班费率自动计算补偿金额，请确认后执行。" type="info" :closable="false" />
        </div>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import type { EmployeeVO, HrOvertimeCompensateVO, HrOvertimeCompensateQueryDTO } from '@/api/hr'
import SearchBar from '@/components/common/SearchBar.vue'
import TablePage from '@/components/common/TablePage.vue'
import AuthBtn from '@/components/common/AuthBtn.vue'
import CommonDialog from '@/components/common/CommonDialog.vue'

/* ========== 列表 ========== */
const fetchApi = (params: HrOvertimeCompensateQueryDTO) => api.getOvertimeCompensatePageApi(params)
const { query, records, total, loading, loadData } = useTable<HrOvertimeCompensateVO>(fetchApi, { pageNum: 1, pageSize: 20 })

/* ========== 员工下拉 ========== */
const employeeList = ref<EmployeeVO[]>([])
const loadEmployees = async () => {
  try { employeeList.value = await api.getEmployeeListApi() } catch { employeeList.value = [] }
}

/* ========== 核算弹窗 ========== */
const calcVisible = ref(false)
const calcSaving = ref(false)
const calcLoading = ref(false)
const calcMonth = ref<string>('')

const openCalcDialog = () => {
  calcMonth.value = new Date().toISOString().slice(0, 7)
  calcVisible.value = true
}

const handleCalcConfirm = async () => {
  if (!calcMonth.value) { ElMessage.warning('请选择补偿月份'); return }
  calcSaving.value = true
  try {
    await api.calculateOvertimeCompensateApi({ compensateMonth: calcMonth.value })
    ElMessage.success('核算完成')
    calcVisible.value = false
    loadData()
  } finally { calcSaving.value = false }
}

/* ========== 发放 ========== */
const handlePay = async (row: HrOvertimeCompensateVO) => {
  await ElMessageBox.confirm(
    `确定向「${row.employeeName}」发放 ${row.compensateMonth} 加班费 ¥${Number(row.overtimeAmount ?? 0).toFixed(2)} 吗？`,
    '确认发放',
    { type: 'warning' }
  )
  try {
    await api.payOvertimeCompensateApi(row.id)
    ElMessage.success('发放成功')
    loadData()
  } catch {}
}

/* ========== 导出 ========== */
const handleExport = async () => {
  try { await api.exportOvertimeCompensateApi(query as any); ElMessage.success('导出成功') } catch { ElMessage.warning('导出接口暂未就绪') }
}

const handleReset = () => { loadData() }

/* ========== 工具函数 ========== */
const payStatusMap: Record<number, string> = { 0: '待发放', 1: '已发放', 2: '已取消' }
const payStatusTag = (v?: number) => ({ 0: 'warning', 1: 'success', 2: 'info' }[v ?? 0] ?? '')

onMounted(() => { loadEmployees(); loadData() })
</script>

<style scoped>
.overtime-page { padding: 0; }
.page-actions { margin: 12px 0; }
.hour-detail { font-size: 12px; }
.hour-detail .highlight { color: #409eff; font-weight: 600; }
.hour-detail .highlight-danger { color: #f56c6c; font-weight: 600; }
.remain-low { color: #f56c6c; font-weight: 600; }
.calc-note { margin-top: 12px; }
</style>
