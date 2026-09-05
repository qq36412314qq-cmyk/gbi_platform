<!--
  未支付订单管理页面
  数据表：biz_fee_bill
  关联表：stall_info（铺位）
  权限：property:fee:*
  功能：多选合并缴费、单条缴费、缴费人显示
-->
<template>
  <div class="g-page-wrap unpaid-bill-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">未支付订单</span>
      <el-button
        type="primary"
        :disabled="selectedIds.length === 0"
        @click="openMergePayDialog"
      >
        合并缴费（{{ selectedIds.length }}）
      </el-button>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="账单月份">
        <el-date-picker
          v-model="query.billMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          clearable
          style="width: 150px"
        />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getBizTypeTag(row.businessType)">
              {{ row.businessTypeText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billMonth" label="账单月份" width="110" align="center" />
        <el-table-column label="绑定铺位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">
              {{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）
            </span>
            <span v-else>铺位 #{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payerName" label="缴费人" width="100" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.payerName || '未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额（元）" width="110" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.payStatus === 0 ? 'warning' : (row.payStatus === 2 ? 'success' : 'info')">
              {{ row.payStatusText || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openPayDialog(row)">缴费</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 单条缴费弹窗 -->
    <CommonDialog
      v-model="payDialogVisible"
      title="线下缴费"
      width="520px"
      :loading="payLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="payFormRef" :model="payForm" :rules="payRules" label-width="100px">
        <!-- 账单信息（只读，自动带入） -->
        <el-form-item label="业务类型">
          <el-tag size="small" :type="getBizTypeTag(payForm.businessType)">
            {{ payForm.businessTypeText }}
          </el-tag>
        </el-form-item>
        <el-form-item label="账单月份">
          <span>{{ payForm.billMonth }}</span>
        </el-form-item>
        <el-form-item label="绑定铺位">
          <span>{{ getStallDisplay() }}</span>
        </el-form-item>
        <el-form-item label="缴费金额">
          <span class="g-money">{{ Number(payForm.amount).toFixed(2) }} 元</span>
        </el-form-item>
        <!-- 支付渠道 -->
        <el-form-item label="支付渠道" prop="payType">
          <el-radio-group v-model="payForm.payType">
            <el-radio :value="1">微信</el-radio>
            <el-radio :value="2">支付宝</el-radio>
            <el-radio :value="3">线下现金</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 幂等请求ID -->
        <el-form-item label="幂等请求ID">
          <el-input v-model="payForm.requestId" disabled />
          <div class="b-tip">自动生成，防止重复提交</div>
        </el-form-item>
        <!-- 备注 -->
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="payForm.remark"
            type="textarea"
            :rows="2"
            placeholder="缴费备注（可空）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 合并缴费弹窗 -->
    <CommonDialog
      v-model="mergePayDialogVisible"
      title="合并缴费"
      width="640px"
      :loading="mergePayLoading"
      @confirm="handleSubmitMerge"
    >
      <el-alert
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      >
        已选择 {{ selectedIds.length }} 条订单，总金额 <span class="g-money">{{ mergeTotalAmount.toFixed(2) }}</span> 元
      </el-alert>

      <el-form ref="mergePayFormRef" :model="mergePayForm" :rules="mergePayRules" label-width="100px">
        <!-- 缴费人信息 -->
        <el-form-item label="缴费人姓名" prop="payerName">
          <el-input v-model="mergePayForm.payerName" placeholder="请输入缴费人姓名" maxlength="64" />
        </el-form-item>
        <el-form-item label="缴费人手机" prop="payerPhone">
          <el-input v-model="mergePayForm.payerPhone" placeholder="请输入手机号" maxlength="32" />
        </el-form-item>
        <!-- 支付渠道 -->
        <el-form-item label="支付渠道" prop="payType">
          <el-radio-group v-model="mergePayForm.payType">
            <el-radio :value="1">微信</el-radio>
            <el-radio :value="2">支付宝</el-radio>
            <el-radio :value="3">线下现金</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 幂等请求ID -->
        <el-form-item label="幂等请求ID">
          <el-input v-model="mergePayForm.requestId" disabled />
          <div class="b-tip">自动生成，防止重复提交</div>
        </el-form-item>
        <!-- 备注 -->
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="mergePayForm.remark"
            type="textarea"
            :rows="2"
            placeholder="缴费备注（可空）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <!-- 明细列表 -->
      <div class="merge-detail-list">
        <div class="merge-detail-header">
          <span>业务类型</span>
          <span>账单月份</span>
          <span>铺位</span>
          <span class="merge-amount">金额</span>
        </div>
        <div v-for="item in selectedBills" :key="item.id" class="merge-item">
          <el-tag size="small" :type="getBizTypeTag(item.businessType)">{{ item.businessTypeText }}</el-tag>
          <span>{{ item.billMonth }}</span>
          <span class="merge-stall">{{ item.stallName || '铺位' + item.stallId }}</span>
          <span class="merge-amount g-money">{{ Number(item.amount).toFixed(2) }}</span>
        </div>
      </div>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 未支付订单页：聚合物业费账单 + 水电费账单 + 租赁费 + 押金
 * 支持单条缴费和合并缴费
 */
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getUnpaidBillPageApi,
  unifiedPayApi,
  createPayBillApi,
  payPayBillApi,
  type UnpaidBillVO,
  type PayBillCreateDTO,
  type PayBillPayDTO
} from '@/api/propertyFee'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData, resetQuery } = useTable<UnpaidBillVO>(getUnpaidBillPageApi, {
  billMonth: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/** 生成幂等请求ID */
function buildRequestId(): string {
  return `PAY${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

/** 获取业务类型标签颜色 */
function getBizTypeTag(bizType: string): string {
  const map: Record<string, string> = {
    property_fee: 'primary',
    water_elec: 'success',
    rent: 'warning',
    deposit: 'info',
    kindergarten: 'danger'
  }
  return map[bizType] || 'info'
}

/* ---------------- 多选状态 ---------------- */
const selectedIds = ref<number[]>([])
const selectedBills = ref<UnpaidBillVO[]>([])

function handleSelectionChange(rows: UnpaidBillVO[]): void {
  selectedIds.value = rows.map(r => r.id)
  selectedBills.value = rows
}

/** 合并缴费总金额 */
const mergeTotalAmount = ref(0)

/* ---------------- 单条缴费弹窗 ---------------- */
const payDialogVisible = ref(false)
const payLoading = ref(false)
const payFormRef = ref<FormInstance>()

const payForm = reactive<{
  billId?: number
  billType: string
  businessTypeText?: string
  billMonth?: string
  stallId?: number
  stallNumber?: string
  stallName?: string
  stallMarketName?: string
  categoryName?: string
  amount: number
  payType: number
  requestId: string
  remark?: string
}>({
  billType: '',
  payType: 3,
  amount: 0,
  requestId: buildRequestId(),
  remark: ''
})

const payRules: FormRules = {
  payType: [{ required: true, message: '请选择支付渠道', trigger: 'change' }]
}

function openPayDialog(row: UnpaidBillVO): void {
  payFormRef.value?.clearValidate()
  Object.assign(payForm, {
    billId: row.id,
    billType: row.businessType,
    businessTypeText: row.businessTypeText,
    billMonth: row.billMonth,
    stallId: row.stallId,
    stallNumber: row.stallNumber,
    stallName: row.stallName,
    stallMarketName: row.stallMarketName,
    categoryName: row.categoryName,
    amount: Number(row.amount) || 0,
    payType: 3,
    requestId: buildRequestId(),
    remark: ''
  })
  payDialogVisible.value = true
}

function getStallDisplay(): string {
  if (payForm.stallName) {
    return `${payForm.stallMarketName || '未知市场'} / ${payForm.categoryName || '未分类'} / ${payForm.stallName}（${payForm.stallNumber || ''}）`
  }
  return `铺位 #${payForm.stallId}`
}

async function handleSubmit(): Promise<void> {
  if (!payFormRef.value) return
  await payFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!payForm.billId) {
      ElMessage.warning('账单ID缺失，请重试')
      return
    }
    payLoading.value = true
    try {
      await unifiedPayApi({
        billId: payForm.billId,
        billType: payForm.billType,
        payType: payForm.payType,
        requestId: payForm.requestId,
        remark: payForm.remark
      })
      ElMessage.success('缴费成功')
      payDialogVisible.value = false
      loadData()
    } catch (e: any) {
      console.warn('[缴费] 后端提示：', e?.message || e)
    } finally {
      payLoading.value = false
    }
  })
}

/* ---------------- 合并缴费弹窗 ---------------- */
const mergePayDialogVisible = ref(false)
const mergePayLoading = ref(false)
const mergePayFormRef = ref<FormInstance>()

const mergePayForm = reactive<{
  payerName?: string
  payerPhone?: string
  payType: number
  requestId: string
  remark?: string
}>({
  payType: 3,
  requestId: buildRequestId(),
  remark: ''
})

const mergePayRules: FormRules = {
  payType: [{ required: true, message: '请选择支付渠道', trigger: 'change' }]
}

function openMergePayDialog(): void {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要缴费的订单')
    return
  }
  mergeTotalAmount.value = selectedBills.value.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
  Object.assign(mergePayForm, {
    payerName: '',
    payerPhone: '',
    payType: 3,
    requestId: buildRequestId(),
    remark: ''
  })
  mergePayDialogVisible.value = true
}

async function handleSubmitMerge(): Promise<void> {
  if (!mergePayFormRef.value) return
  await mergePayFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 1. 创建缴费单
    mergePayLoading.value = true
    try {
      const createDto: PayBillCreateDTO = {
        bizFeeBillIds: selectedIds.value,
        remark: mergePayForm.remark
      }
      const payBillId = await createPayBillApi(createDto)

      // 2. 缴费
      const payDto: PayBillPayDTO = {
        payBillId,
        payType: mergePayForm.payType,
        requestId: mergePayForm.requestId
      }
      await payPayBillApi(payDto)

      ElMessage.success('合并缴费成功')
      mergePayDialogVisible.value = false
      selectedIds.value = []
      selectedBills.value = []
      loadData()
    } catch (e: any) {
      console.warn('[合并缴费] 后端提示：', e?.message || e)
    } finally {
      mergePayLoading.value = false
    }
  })
}
</script>

<style scoped>
.g-money {
  color: #f56c6c;
  font-weight: 600;
}
.b-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
.merge-detail-list {
  margin-top: 16px;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 8px;
}
.merge-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}
.merge-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
}
.merge-item:last-child {
  border-bottom: none;
}
.merge-stall {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.merge-amount {
  flex-shrink: 0;
  font-weight: 600;
}
</style>

