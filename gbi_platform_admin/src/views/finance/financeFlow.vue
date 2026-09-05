<template>
  <div class="g-page-wrap finance-flow-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">财务流水</span>
      <div>
        <AuthBtn permission="finance:flow:export" type="primary" @click="handleExport">导出流水</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="业务类型">
        <el-select v-model="query.businessType" placeholder="全部" clearable style="width: 140px">
          <el-option label="租金" value="rent" />
          <el-option label="水电物业" value="water_elec" />
          <el-option label="押金" value="deposit" />
          <el-option label="营销抵扣" value="marketing" />
        </el-select>
      </el-form-item>
      <el-form-item label="收支方向">
        <el-select v-model="query.flowType" placeholder="全部" clearable style="width: 130px">
          <el-option label="收入" :value="1" />
          <el-option label="支出退费" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 340px"
          clearable
          @change="handleTimeChange"
        />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe @expand="loadFlowItems">
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <el-table :data="expandedItems[row.id] || []" border stripe size="small" style="margin: 0 20px">
              <el-table-column prop="bizTypeText" label="业务类型" width="100" align="center" />
              <el-table-column prop="feeItemType" label="收费项" width="120" align="center" />
              <el-table-column prop="billMonth" label="账期" width="100" align="center" />
              <el-table-column prop="amount" label="应收金额" width="100" align="right">
                <template #default="{ row: item }">
                  <span class="g-money">{{ Number(item.amount).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="discountAmount" label="优惠抵扣" width="100" align="right" />
              <el-table-column prop="paidAmount" label="已缴金额" width="100" align="right" />
              <el-table-column prop="unpaidAmount" label="未缴金额" width="100" align="right" />
              <el-table-column prop="ruleName" label="收费规则" min-width="120" show-overflow-tooltip />
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="流水ID" width="90" align="center" />
        <el-table-column prop="createTime" label="生成时间" min-width="120" align="center" />
        <el-table-column label="所属公司" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团' : `公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.businessTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收支方向" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.flowType === 1 ? 'danger' : 'success'">{{ row.flowTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="originalAmount" label="应收原价" width="100" align="right" />
        <el-table-column prop="discountAmount" label="优惠抵扣" width="100" align="right" />
        <el-table-column prop="realAmount" label="实收金额" width="100" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ Number(row.realAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付渠道" width="100" align="center">
          <template #default="{ row }">
            {{ row.payTypeText || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="flowNo" label="流水单号" width="180" align="center" />
        <el-table-column label="缴费人" min-width="120" align="center" v-if="showPayerColumn">
          <template #default="{ row }">
            <span>{{ row.payerName || row.merchantName || '-' }}</span>
            <span v-if="row.payerCompanyName" class="payer-company">（{{ row.payerCompanyName }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="合同编号" width="130" align="center" v-if="showContractColumn">
          <template #default="{ row }">
            {{ row.contractNo || row.billId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="绑定摊位" min-width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>摊位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="flowStatusType(row.flowStatus)">
              {{ row.flowStatusText || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handlePrint(row)">打印</el-button>
            <el-button
              v-if="canRedFlush(row)"
              link type="warning" size="small"
              @click="handleRedFlush(row)"
            >冲红</el-button>
            <el-button
              v-if="canVoid(row)"
              link type="danger" size="small"
              @click="handleVoid(row)"
            >作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 冲红弹窗 -->
    <el-dialog v-model="redFlushDialogVisible" title="申请冲红" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="流水单号">
          <span>{{ redFlushTarget?.flowNo }}</span>
        </el-form-item>
        <el-form-item label="冲红原因">
          <el-input v-model="redFlushReason" type="textarea" :rows="3" placeholder="请输入冲红原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="redFlushDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="redFlushLoading" @click="confirmRedFlush">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 作废弹窗 -->
    <el-dialog v-model="voidDialogVisible" title="作废流水" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="流水单号">
          <span>{{ voidTarget?.flowNo }}</span>
        </el-form-item>
        <el-form-item label="作废原因">
          <el-input v-model="voidReason" type="textarea" :rows="3" placeholder="请输入作废原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="voidDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="voidLoading" @click="confirmVoid">确认作废</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 财务流水页：全域统一资金台账
 * 支持冲红（需审批）、作废（仅草稿/未记账）、打印收据
 */
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFinanceFlowPageApi,
  exportFinanceFlowApi,
  redFlushFlowApi,
  voidFlowApi,
  getPrintHtmlApi,
  type FinanceFlowVO,
  type PayOrderItemVO,
  getFinanceFlowItemsApi
} from '@/api/finance'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FinanceFlowVO>(getFinanceFlowPageApi, {
  businessType: undefined,
  flowType: undefined,
  startTime: undefined,
  endTime: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

const timeRange = ref<[string, string] | null>(null)

function handleTimeChange(val: [string, string] | null): void {
  if (val) {
    query.startTime = val[0]
    query.endTime = val[1]
  } else {
    query.startTime = undefined
    query.endTime = undefined
  }
  handleQuery()
}

function handleQuery(): void {
  query.pageNum = 1
  loadData()
}

/* ---------------- 条件列显示 ---------------- */
// 显示缴费人列：仅当列表中有非水电物业的业务类型
const showPayerColumn = computed(() =>
  records.value.some(r => !['water_elec', null, undefined].includes(r.businessType))
)

// 显示合同编号列：仅当列表中有非水电物业的业务类型
const showContractColumn = computed(() =>
  records.value.some(r => !['water_elec', null, undefined].includes(r.businessType))
)

/* ---------------- 导出 ---------------- */
async function handleExport(): Promise<void> {
  try {
    const fileUrl = await exportFinanceFlowApi({ ...query })
    const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/api\/?$/, '')
    window.open(`${baseUrl}${fileUrl}`, '_blank')
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败，请稍后重试')
  }
}

/* ---------------- 打印 ---------------- */
/** 展开行数据缓存 */
const expandedItems = ref<Record<number, PayOrderItemVO[]>>({})

async function loadFlowItems(row: FinanceFlowVO): Promise<void> {
  if (!expandedItems.value[row.id]) {
    try {
      expandedItems.value[row.id] = await getFinanceFlowItemsApi(row.id)
    } catch {
      expandedItems.value[row.id] = []
    }
  }
}

async function handlePrint(row: FinanceFlowVO): Promise<void> {
  try {
    const html = await getPrintHtmlApi(row.id)
    const printWindow = window.open('', '_blank')
    if (printWindow) {
      printWindow.document.write(html)
      printWindow.document.close()
      printWindow.focus()
      printWindow.print()
      printWindow.close()
    }
  } catch {
    ElMessage.error('打印失败，请稍后重试')
  }
}

/* ---------------- 冲红 ---------------- */
const redFlushDialogVisible = ref(false)
const redFlushLoading = ref(false)
const redFlushTarget = ref<FinanceFlowVO | null>(null)
const redFlushReason = ref('')

function canRedFlush(row: FinanceFlowVO): boolean {
  return row.flowStatus === 1 // 仅正常状态的流水可冲红
}

function handleRedFlush(row: FinanceFlowVO): void {
  redFlushTarget.value = row
  redFlushReason.value = ''
  redFlushDialogVisible.value = true
}

async function confirmRedFlush(): Promise<void> {
  if (!redFlushTarget.value || !redFlushReason.value.trim()) {
    ElMessage.warning('请填写冲红原因')
    return
  }
  redFlushLoading.value = true
  try {
    await redFlushFlowApi({ flowId: redFlushTarget.value.id, reason: redFlushReason.value.trim() })
    ElMessage.success('冲红申请已提交，等待审批')
    redFlushDialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '冲红申请失败')
  } finally {
    redFlushLoading.value = false
  }
}

/* ---------------- 作废 ---------------- */
const voidDialogVisible = ref(false)
const voidLoading = ref(false)
const voidTarget = ref<FinanceFlowVO | null>(null)
const voidReason = ref('')

function canVoid(row: FinanceFlowVO): boolean {
  return row.flowStatus === 1 // 仅正常状态且未关联计划的可作废（后端进一步校验）
}

function handleVoid(row: FinanceFlowVO): void {
  voidTarget.value = row
  voidReason.value = ''
  voidDialogVisible.value = true
}

async function confirmVoid(): Promise<void> {
  if (!voidTarget.value || !voidReason.value.trim()) {
    ElMessage.warning('请填写作废原因')
    return
  }
  voidLoading.value = true
  try {
    await voidFlowApi({ flowId: voidTarget.value.id, reason: voidReason.value.trim() })
    ElMessage.success('流水已作废')
    voidDialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '作废失败')
  } finally {
    voidLoading.value = false
  }
}

/* ---------------- 状态标签颜色 ---------------- */
function flowStatusType(status: number | undefined): string {
  switch (status) {
    case 2: return 'warning'  // 冲红中
    case 3: return 'info'    // 已冲红
    case 4: return 'info'    // 已作废
    default: return ''
  }
}
</script>

<style scoped>
.g-money {
  font-weight: 600;
  color: #e6a23c;
}
.payer-company {
  color: #909399;
  font-size: 12px;
}
</style>
