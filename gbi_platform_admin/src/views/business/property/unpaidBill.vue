<!--
  未支付订单管理页面
  数据表：biz_fee_bill
  关联表：stall_info（摊位）
  权限：property:fee:*
-->
<template>
  <div class="g-page-wrap unpaid-bill-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">未支付订单</span>
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
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.businessType === 'property_fee' ? 'primary' : 'success'">
              {{ row.businessTypeText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billMonth" label="账单月份" width="110" align="center" />
        <el-table-column label="绑定摊位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">
              {{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）
            </span>
            <span v-else>摊位 #{{ row.stallId }}</span>
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

    <!-- 线下缴费弹窗 -->
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
          <el-tag size="small" :type="payForm.billType === 'property_fee' ? 'primary' : 'success'">
            {{ payForm.businessTypeText }}
          </el-tag>
        </el-form-item>
        <el-form-item label="账单月份">
          <span>{{ payForm.billMonth }}</span>
        </el-form-item>
        <el-form-item label="绑定摊位">
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
  </div>
</template>

<script setup lang="ts">
/**
 * 未支付订单页：聚合物业费账单 + 水电费账单
 * 点击"缴费"直接在当前页弹出线下缴费弹窗，无需跳转
 *
 * 缴费逻辑：
 * 1. 生成幂等请求ID（requestId），防止重复提交
 * 2. 调用 /property/unifiedPay/pay 统一缴费接口
 * 3. 缴费成功后刷新列表
 */
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getUnpaidBillPageApi, type UnpaidBillVO, unifiedPayApi } from '@/api/propertyFee'
import { useTable } from '@/hooks/useTable'

const { query, records, total, loading, loadData, resetQuery } = useTable<UnpaidBillVO>(getUnpaidBillPageApi, {
  billMonth: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/** 生成幂等请求ID（满足后端 8-64 位字母/数字/横线规则） */
function buildRequestId(): string {
  return `PAY${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

/* ---------------- 缴费弹窗 ---------------- */
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

/** 打开缴费弹窗，自动带入当前行数据 */
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

/** 拼接摊位显示文本 */
function getStallDisplay(): string {
  if (payForm.stallName) {
    return `${payForm.stallMarketName || '未知市场'} / ${payForm.categoryName || '未分类'} / ${payForm.stallName}（${payForm.stallNumber || ''}）`
  }
  return `摊位 #${payForm.stallId}`
}

/** 提交缴费 */
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
    } catch (e) {
      // 拦截器已显示 ElMessage.error，此处仅阻止 Uncaught 爆日志
      console.warn('[缴费] 后端提示：', e?.message || e)
    } finally {
      payLoading.value = false
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
</style>
