<template>
  <div class="g-page-wrap recv-pay-plan-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">应收应付计划</span>
      <div>
        <AuthBtn permission="plan:recvpay:reconcile" type="primary" plain @click="openReconcile()">自动对账</AuthBtn>
        <AuthBtn permission="plan:recvpay:export" type="success" plain @click="handleExport()">导出</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="方向">
        <el-select v-model="query.direction" placeholder="全部" clearable style="width: 110px">
          <el-option label="应收" :value="1" />
          <el-option label="应付" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型">
        <el-select v-model="query.bizType" placeholder="全部" clearable style="width: 140px">
          <el-option label="租金" value="rent" />
          <el-option label="押金" value="deposit" />
          <el-option label="物业费" value="property" />
          <el-option label="收费规则账单" value="fee_bill" />
        </el-select>
      </el-form-item>
      <el-form-item label="计划状态">
        <el-select v-model="query.planStatus" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(text, value) in planStatusMap" :key="value" :label="text" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="期次">
        <el-input v-model="query.periodNo" placeholder="yyyy-MM / once" clearable style="width: 130px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="planNo" label="计划编号" width="170" show-overflow-tooltip />
        <el-table-column prop="directionText" label="方向" width="70" align="center" />
        <el-table-column prop="bizTypeText" label="业务类型" width="110" />
        <el-table-column prop="stallNumber" label="摊位" width="90" align="center">
          <template #default="{ row }">{{ row.stallNumber || '-' }}</template>
        </el-table-column>
        <el-table-column prop="periodNo" label="期次" width="90" align="center" />
        <el-table-column prop="dueDate" label="应收日期" width="100" />
        <el-table-column label="原价" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.originalAmount) }}</template>
        </el-table-column>
        <el-table-column label="优惠" width="90" align="right">
          <template #default="{ row }">-￥{{ formatMoney(row.discountAmount) }}</template>
        </el-table-column>
        <el-table-column label="调账" width="90" align="right">
          <template #default="{ row }">
            <span :class="Number(row.adjustAmount) !== 0 ? 'g-text-danger' : ''">{{ formatMoney(row.adjustAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划金额" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.planAmount) }}</template>
        </el-table-column>
        <el-table-column label="已收" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="未收" width="100" align="right">
          <template #default="{ row }">
            <span :class="Number(row.unpaidAmount) > 0 ? 'g-text-danger' : ''">￥{{ formatMoney(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.planStatus ?? 0)">{{ row.planStatusText || planStatusMap[row.planStatus ?? 0] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="红冲" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.redFlag === 1" size="small" type="danger">红冲</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetail(row.id)">详情</el-button>
            <AuthBtn permission="plan:recvpay:adjust" size="small" type="warning" link @click="openAdjust(row)">调账</AuthBtn>
            <AuthBtn permission="plan:recvpay:terminate" size="small" type="danger" link @click="openTerminate(row)">作废</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="计划详情" width="760px" destroy-on-close>
      <div v-if="detail" v-loading="detailLoading" class="plan-detail-wrap">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="计划编号">{{ detail.plan.planNo }}</el-descriptions-item>
          <el-descriptions-item label="方向">{{ detail.plan.directionText }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ detail.plan.bizTypeText }}</el-descriptions-item>
          <el-descriptions-item label="摊位">{{ detail.plan.stallNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="期次">{{ detail.plan.periodNo }}</el-descriptions-item>
          <el-descriptions-item label="应收日期">{{ detail.plan.dueDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划金额">￥{{ formatMoney(detail.plan.planAmount) }}</el-descriptions-item>
          <el-descriptions-item label="已收/未收">￥{{ formatMoney(detail.plan.paidAmount) }} / ￥{{ formatMoney(detail.plan.unpaidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.plan.planStatusText }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detail.plan.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="plan-section-title">关联账单（bill_plan_rel）</div>
        <el-table v-if="detail.billPlanRels.length" :data="detail.billPlanRels" border size="small">
          <el-table-column prop="billType" label="账单类型" width="120" />
          <el-table-column prop="billId" label="账单ID" width="100" />
          <el-table-column label="分摊金额" width="120" align="right">
            <template #default="{ row }">￥{{ formatMoney(row.splitAmount) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="无关联账单" :image-size="50" />

        <div class="plan-section-title">核销分摊明细</div>
        <el-table v-if="detail.writeoffs.length" :data="detail.writeoffs" border size="small">
          <el-table-column prop="writeoffTypeText" label="类型" width="100" />
          <el-table-column label="分摊金额" width="110" align="right">
            <template #default="{ row }">
              <span :class="Number(row.writeoffAmount) < 0 ? 'g-text-danger' : ''">￥{{ formatMoney(row.writeoffAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="financeFlowId" label="资金流水ID" width="100" />
          <el-table-column prop="createByName" label="操作人" width="100" />
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="150" />
        </el-table>
        <el-empty v-else description="无核销记录" :image-size="50" />
      </div>
    </el-dialog>

    <!-- 调账弹窗（敏感操作） -->
    <CommonDialog
      v-model="adjustVisible"
      title="计划人工调账"
      width="520px"
      :loading="submitLoading"
      @confirm="handleAdjust"
    >
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="计划编号">
          <span class="g-text-strong">{{ adjustForm.planNo }}</span>
        </el-form-item>
        <el-form-item label="调账金额" prop="adjustAmount">
          <el-input-number v-model="adjustForm.adjustAmount" :precision="2" :step="100" :min="-99999999.99" :max="99999999.99" style="width: 100%" />
          <div class="g-tip">正数=加收，负数=减免；超集团阈值（plan.adjust_amount_limit）将自动发起审批</div>
        </el-form-item>
        <el-form-item label="调账原因" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="3" placeholder="调账原因（必填，写入审计日志）" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 作废/终止弹窗（高危操作） -->
    <CommonDialog
      v-model="terminateVisible"
      title="计划作废/终止"
      width="520px"
      :loading="submitLoading"
      @confirm="handleTerminate"
    >
      <el-form ref="terminateFormRef" :model="terminateForm" :rules="terminateRules" label-width="100px">
        <el-form-item label="计划编号">
          <span class="g-text-strong">{{ terminateForm.planNo }}</span>
        </el-form-item>
        <el-form-item label="作废原因" prop="remark">
          <el-input v-model="terminateForm.remark" type="textarea" :rows="3" placeholder="作废/终止原因（必填）" maxlength="500" />
        </el-form-item>
        <el-form-item>
          <div class="g-tip g-text-danger">高危操作：将触发审批流程，通过后执行红冲链（冲销计划/账单/核销/流水），全程审计可追溯</div>
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 自动对账弹窗 -->
    <el-dialog v-model="reconcileVisible" title="自动对账（以计划为权威源）" width="900px" destroy-on-close>
      <div class="reconcile-filter">
        <el-input v-model="reconcileQuery.periodNo" placeholder="期次 yyyy-MM" clearable style="width: 150px" @change="loadReconcile()" />
        <el-button type="primary" plain @click="loadReconcile()">查询</el-button>
        <span class="g-tip">差异 = 计算剩余 − 账面未收，非 0 表示对账异常（仅告警展示，不可直接改流水）</span>
      </div>
      <el-table v-loading="reconcileLoading" :data="reconcileRecords" border stripe size="small">
        <el-table-column prop="planNo" label="计划编号" width="170" show-overflow-tooltip />
        <el-table-column prop="periodNo" label="期次" width="90" align="center" />
        <el-table-column label="计划金额" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.planAmount) }}</template>
        </el-table-column>
        <el-table-column label="账面未收" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.unpaidAmount) }}</template>
        </el-table-column>
        <el-table-column label="核销合计" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.writeoffSum) }}</template>
        </el-table-column>
        <el-table-column label="计算剩余" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.computedRemaining) }}</template>
        </el-table-column>
        <el-table-column label="差异" width="110" align="right">
          <template #default="{ row }">
            <el-tag :type="Number(row.diff) === 0 ? 'success' : 'danger'" size="small">￥{{ formatMoney(row.diff) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="reconcile-pager">
        <el-pagination
          v-model:current-page="reconcileQuery.pageNum"
          v-model:page-size="reconcileQuery.pageSize"
          :total="reconcileTotal"
          layout="total, prev, pager, next"
          @current-change="loadReconcile()"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 应收应付计划页：全系统唯一应收应付台账（押金/租金/物业费/收费规则账单），
 * 生成幂等、调账/作废超阈值走审批、自动对账引擎、导出限流审计
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getRecvPayPlanPageApi,
  getRecvPayPlanDetailApi,
  adjustRecvPayPlanApi,
  terminateRecvPayPlanApi,
  getReconcileDiffPageApi,
  exportRecvPayPlanApi,
  type RecvPayPlanVO,
  type RecvPayPlanDetailVO,
  type ReconcileDiffVO
} from '@/api/plan'
import { useTable } from '@/hooks/useTable'
import { formatMoney } from '@/utils/format'

const planStatusMap: Record<number, string> = { 0: '待执行', 1: '部分核销', 2: '完成', 3: '逾期', 4: '作废', 5: '终止' }

function statusTagType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 3 || status === 4 || status === 5) return 'danger'
  if (status === 2) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<RecvPayPlanVO>(getRecvPayPlanPageApi, {
  direction: undefined,
  bizType: undefined,
  planStatus: undefined,
  periodNo: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<RecvPayPlanDetailVO | null>(null)

async function openDetail(planId: number): Promise<void> {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await getRecvPayPlanDetailApi(planId)
  } finally {
    detailLoading.value = false
  }
}

/* ---------------- 调账（敏感：二次确认 + 审计） ---------------- */
const adjustVisible = ref(false)
const submitLoading = ref(false)
const adjustFormRef = ref<FormInstance>()

const adjustForm = reactive<{ planId: number; planNo: string; adjustAmount: number; remark: string }>({
  planId: 0,
  planNo: '',
  adjustAmount: 0,
  remark: ''
})

const adjustRules: FormRules = {
  adjustAmount: [{ required: true, message: '请输入调账金额', trigger: 'blur' }],
  remark: [{ required: true, message: '请输入调账原因', trigger: 'blur' }]
}

function openAdjust(row: RecvPayPlanVO): void {
  adjustFormRef.value?.clearValidate()
  Object.assign(adjustForm, { planId: row.id, planNo: row.planNo, adjustAmount: 0, remark: '' })
  adjustVisible.value = true
}

async function handleAdjust(): Promise<void> {
  if (!adjustFormRef.value) {
    return
  }
  await adjustFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    await ElMessageBox.confirm(`确定对计划「${adjustForm.planNo}」调账 ${formatMoney(adjustForm.adjustAmount)} 元吗？调账记录将写入审计日志`, '二次确认', {
      type: 'warning'
    })
    submitLoading.value = true
    try {
      await adjustRecvPayPlanApi({ planId: adjustForm.planId, adjustAmount: adjustForm.adjustAmount, remark: adjustForm.remark })
      ElMessage.success('调账成功')
      adjustVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 作废/终止（高危：审批 + 红冲链） ---------------- */
const terminateVisible = ref(false)
const terminateFormRef = ref<FormInstance>()

const terminateForm = reactive<{ planId: number; planNo: string; remark: string }>({
  planId: 0,
  planNo: '',
  remark: ''
})

const terminateRules: FormRules = {
  remark: [{ required: true, message: '请输入作废原因', trigger: 'blur' }]
}

function openTerminate(row: RecvPayPlanVO): void {
  terminateFormRef.value?.clearValidate()
  Object.assign(terminateForm, { planId: row.id, planNo: row.planNo, remark: '' })
  terminateVisible.value = true
}

async function handleTerminate(): Promise<void> {
  if (!terminateFormRef.value) {
    return
  }
  await terminateFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    await ElMessageBox.confirm(`确定作废/终止计划「${terminateForm.planNo}」吗？该操作将触发审批与红冲链，不可撤销`, '高危二次确认', {
      type: 'warning',
      confirmButtonText: '确定作废'
    })
    submitLoading.value = true
    try {
      await terminateRecvPayPlanApi({ planId: terminateForm.planId, remark: terminateForm.remark })
      ElMessage.success('已提交作废处理')
      terminateVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 自动对账 ---------------- */
const reconcileVisible = ref(false)
const reconcileLoading = ref(false)
const reconcileRecords = ref<ReconcileDiffVO[]>([])
const reconcileTotal = ref(0)

const reconcileQuery = reactive({ pageNum: 1, pageSize: 10, periodNo: undefined as string | undefined })

function openReconcile(): void {
  reconcileVisible.value = true
  loadReconcile()
}

async function loadReconcile(): Promise<void> {
  reconcileLoading.value = true
  try {
    const data = await getReconcileDiffPageApi({ ...reconcileQuery })
    reconcileRecords.value = data.records || []
    reconcileTotal.value = data.total || 0
  } finally {
    reconcileLoading.value = false
  }
}

/* ---------------- 导出 ---------------- */
async function handleExport(): Promise<void> {
  await ElMessageBox.confirm('确定导出当前筛选条件下的计划数据吗？导出操作将记录审计日志', '导出确认', { type: 'info' })
  const res = await exportRecvPayPlanApi({ ...query })
  if (res.url) {
    window.open(res.url, '_blank')
    ElMessage.success('导出成功')
  }
}
</script>

<style scoped>
.plan-section-title {
  margin: 14px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.reconcile-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.reconcile-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>