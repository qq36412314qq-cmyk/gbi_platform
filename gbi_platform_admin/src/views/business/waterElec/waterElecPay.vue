<!-- 
  缴费管理页面
  数据表：water_elec_pay_record
  权限：waterElec:pay:*
-->
<template>
  <div class="g-page-wrap water-elec-pay-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">缴费管理</span>
      <div>
        <AuthBtn permission="waterElec:pay:add" type="primary" @click="openPayDialog">线下缴费</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="记录类型">
        <el-select v-model="query.recordType" placeholder="全部" clearable style="width: 130px">
          <el-option label="缴费" :value="1" />
          <el-option label="退费" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付渠道">
        <el-select v-model="query.payType" placeholder="全部" clearable style="width: 130px">
          <el-option label="微信" :value="1" />
          <el-option label="支付宝" :value="2" />
          <el-option label="线下现金" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe @expand-change="loadFlowItems">
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <el-descriptions :column="2" border size="small" style="margin: 0 20px">
              <el-descriptions-item label="账单ID">{{ row.billId }}</el-descriptions-item>
              <el-descriptions-item label="流水单号">{{ row.flowNo }}</el-descriptions-item>
              <el-descriptions-item label="缴费金额">
                <span class="g-money">{{ formatMoney(row.payAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付渠道">{{ row.payTypeText }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ row.createTime }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ row.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="流水ID" width="90" align="center" />
        <el-table-column prop="createTime" label="生成时间" min-width="120" align="center" />
        <el-table-column label="所属公司" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团' : '公司' + row.companyId }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.recordType === 1 ? 'success' : 'danger'">{{ row.recordTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收支方向" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.recordType === 1 ? 'danger' : 'success'">{{ row.recordType === 1 ? '收入' : '支出' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="缴费金额" width="110" align="right">
          <template #default="{ row }">
            <span :class="row.recordType === 1 ? 'g-money' : 'g-money-out'">{{ formatMoney(row.payAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付渠道" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.payTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="flowNo" label="流水单号" width="180" align="center" />
        <el-table-column label="缴费人" min-width="120" align="center" v-if="showPayerColumn">
          <template #default="{ row }">
            <span>{{ row.merchantName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="绑定铺位" min-width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>铺位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <template v-if="row.recordType === 1">
              <el-tag size="small" :type="row.refundStatus === 1 ? 'info' : 'primary'">
                {{ row.refundStatus === 1 ? '已退费' : '正常' }}
              </el-tag>
            </template>
            <span v-else>正常</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.recordType === 1 && row.refundStatus === 0"
              link type="danger" size="small"
              @click="handleRefund(row)"
            >退费</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 线下缴费弹窗 -->
    <el-dialog v-model="payDialogVisible" title="线下缴费" width="520px" :close-on-click-modal="false">
      <el-form ref="payFormRef" :model="payForm" :rules="payRules" label-width="100px">
        <el-form-item label="账单ID" prop="billId">
          <el-input-number v-model="payForm.billId" :min="1" :precision="0" style="width: 100%" @change="loadBillDetail" />
        </el-form-item>
        <el-form-item label="账单信息" v-if="billDetail">
          <div class="b-bill-info">
            <div>月份：{{ billDetail.billMonth }}　铺位：{{ billDetail.stallName ? (billDetail.stallMarketName || '未知市场') + ' / ' + (billDetail.categoryName || '未分类') + ' / ' + billDetail.stallName + '（' + billDetail.stallNumber + '）' : ('铺位#' + billDetail.stallId) }}　状态：{{ billDetail.payStatusText }}</div>
            <div>
              应收合计：<span class="g-money">{{ formatMoney(billDetail.amount || billDetail.totalAmount || 0) }} 元</span>
              <el-tag v-if="billDetail.payStatus === 1" size="small" type="warning" style="margin-left: 8px">已缴清，不可重复缴费</el-tag>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="支付渠道" prop="payType">
          <el-radio-group v-model="payForm.payType">
            <el-radio :value="1">微信</el-radio>
            <el-radio :value="2">支付宝</el-radio>
            <el-radio :value="3">线下现金</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="幂等请求ID">
          <el-input :model-value="payForm.requestId" disabled />
          <div class="b-tip">自动生成，防止重复提交</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="payForm.remark" type="textarea" :rows="2" placeholder="缴费备注（可空）" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 缴费管理页：线下缴费登记（requestId 幂等）+ 退费（写支出流水）
 * 支持物业费和水电费两种账单类型
 */
import { reactive, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getPayPageApi,
  payBillApi,
  refundPayApi,
  getBillDetailApi,
  type WaterElecPayRecordVO,
  type WaterElecBillVO
} from '@/api/waterElec'
import { getFeeBillDetailApi, payPropertyFeeApi, type PropertyFeeBillVO } from '@/api/propertyFee'
import { useTable } from '@/hooks/useTable'

const showPayerColumn = computed(() => records.value.some(r => !!r.merchantName))

const route = useRoute()

/** 生成幂等请求ID（满足后端 8-64 位字母/数字/横线规则） */
function buildRequestId(): string {
  return 'PAY' + Date.now() + '-' + Math.random().toString(36).slice(2, 10)
}

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<WaterElecPayRecordVO>(getPayPageApi, {
  recordType: undefined,
  payType: undefined,
  billId: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 展开明细 ---------------- */
const expandedItems = ref<Record<number, unknown[]>>({})

async function loadFlowItems(_row: WaterElecPayRecordVO): Promise<void> {
  // 水电缴费流水暂无子明细，保留接口结构供后续扩展
}

/* ---------------- 线下缴费 ---------------- */
const payDialogVisible = ref(false)
const payLoading = ref(false)
const payFormRef = ref<FormInstance>()
const billDetail = ref<WaterElecBillVO | PropertyFeeBillVO | null>(null)
const billType = ref<'water_elec' | 'property_fee'>('water_elec')
const payForm = reactive<{ billId?: number; payType: number; requestId: string; remark?: string }>({
  billId: undefined,
  payType: 3,
  requestId: buildRequestId(),
  remark: ''
})

const payRules: FormRules = {
  billId: [{ required: true, message: '请输入账单ID', trigger: 'blur' }],
  payType: [{ required: true, message: '请选择支付渠道', trigger: 'change' }]
}

function openPayDialog(): void {
  payFormRef.value?.clearValidate()
  Object.assign(payForm, { billId: undefined, payType: 3, requestId: buildRequestId(), remark: '' })
  billDetail.value = null
  billType.value = 'water_elec'
  payDialogVisible.value = true
}

/** 输入账单ID后核对账单金额 */
async function loadBillDetail(): Promise<void> {
  if (!payForm.billId) {
    billDetail.value = null
    return
  }
  try {
    const id = payForm.billId!
    // 先尝试水电费账单详情
    try {
      const waterBill = await getBillDetailApi(id)
      billDetail.value = waterBill
      billType.value = 'water_elec'
      return
    } catch {
      // 不是水电费账单，尝试物业费账单
    }
    try {
      const feeBill = await getFeeBillDetailApi(id)
      billDetail.value = feeBill
      billType.value = 'property_fee'
    } catch {
      billDetail.value = null
    }
  } catch {
    billDetail.value = null
  }
}

async function handlePay(): Promise<void> {
  if (!payFormRef.value) {
    return
  }
  await payFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    if (!billDetail.value) {
      ElMessage.warning('请先查询账单信息')
      return
    }
    const status = billDetail.value.payStatus
    if (status === 1 || status === 2) {
      ElMessage.warning('该账单已缴清，无需重复缴费')
      return
    }
    payLoading.value = true
    try {
      if (billType.value === 'water_elec') {
        await payBillApi({ billId: payForm.billId!, payType: payForm.payType, requestId: payForm.requestId, remark: payForm.remark })
      } else {
        await payPropertyFeeApi({ billId: payForm.billId!, payType: payForm.payType, requestId: payForm.requestId, remark: payForm.remark })
      }
      ElMessage.success('缴费成功')
      payDialogVisible.value = false
      loadData()
    } catch (e) {
      // 拦截器已显示 ElMessage.error，此处仅阻止 Uncaught
      console.warn('[缴费] 后端提示：', e?.message || e)
    } finally {
      payLoading.value = false
    }
  })
}

/* ---------------- 退费 ---------------- */
async function handleRefund(row: WaterElecPayRecordVO): Promise<void> {
  await ElMessageBox.confirm(
    '确定对缴费记录（账单 ' + row.billId + '，金额 ' + formatMoney(row.payAmount) + ' 元）发起退费吗？退费将同步生成支出流水。',
    '退费确认',
    { type: 'warning' }
  )
  await refundPayApi({ payRecordId: row.id, requestId: buildRequestId(), remark: '线下退费' })
  ElMessage.success('退费成功')
  loadData()
}

/* ---------------- 接收账单页跳转定位 ---------------- */
watch(
  () => route.query.billId,
  (billId) => {
    if (billId) {
      const id = Number(billId)
      if (id > 0) {
        query.billId = id
        loadData()
        // 打开缴费弹窗并预填账单
        openPayDialog()
        payForm.billId = id
        loadBillDetail()
      }
    }
  },
  { immediate: true }
)

/* ---------------- 工具函数 ---------------- */
function formatMoney(val: number | string | null | undefined): string {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}
</script>

<style scoped>
.b-bill-info {
  width: 100%;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.8;
}
.b-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
.g-money {
  font-weight: 600;
  color: var(--el-color-danger);
}
.g-money-out {
  font-weight: 600;
  color: var(--el-color-success);
}
</style>