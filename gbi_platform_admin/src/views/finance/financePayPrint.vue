<template>
  <div class="print-page">
    <div class="print-btn-bar">
      <el-button type="primary" @click="handlePrint">打印</el-button>
      <el-button @click="handleClose">关闭</el-button>
    </div>

    <div v-if="loading" class="g-loading">加载中...</div>

    <div v-else-if="error" class="g-error">数据加载失败</div>

    <div v-else ref="printContent" class="print-content">
      <h2 class="print-title">缴费单收据</h2>

      <table class="print-info-table">
        <tr>
          <th>缴费单编号</th>
          <td>{{ data.payOrder.payBillNo || '-' }}</td>
          <th>创建时间</th>
          <td>{{ data.payOrder.createTime || '-' }}</td>
        </tr>
        <tr>
          <th>来源类型</th>
          <td>{{ data.payOrder.sourceTypeText || '-' }}</td>
          <th>缴费状态</th>
          <td>{{ data.payOrder.payStatusText || '-' }}</td>
        </tr>
        <tr>
          <th>铺位</th>
          <td>{{ stallDisplay }}</td>
          <th>商户</th>
          <td>{{ merchantDisplay }}</td>
        </tr>
      </table>

      <h3 class="print-subtitle">收费明细</h3>

      <table class="print-detail-table">
        <thead>
          <tr>
            <th>业务类型</th>
            <th>收费项</th>
            <th>账期</th>
            <th>收费规则</th>
            <th class="col-money">应收金额</th>
            <th class="col-money">优惠抵扣</th>
            <th class="col-money">已缴金额</th>
            <th class="col-money">未缴金额</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in data.items" :key="item.id">
            <td>{{ item.bizTypeText || '-' }}</td>
            <td>{{ item.feeItemType || '-' }}</td>
            <td>{{ item.billMonth || '-' }}</td>
            <td>{{ item.ruleName || '-' }}</td>
            <td class="col-money">{{ formatMoney(item.amount) }}</td>
            <td class="col-money">{{ formatMoney(item.discountAmount) }}</td>
            <td class="col-money">{{ formatMoney(item.paidAmount) }}</td>
            <td class="col-money">{{ formatMoney(item.unpaidAmount) }}</td>
          </tr>
        </tbody>
      </table>

      <p class="print-remark" v-if="data.payOrder.remark">
        <strong>备注：</strong>{{ data.payOrder.remark }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 缴费单打印模板页面
 * 独立页面，可通过路由 /finance/payOrderPrint/:id 访问
 * 打印样式使用 @media print 控制，方便调整模板内容、样式、尺寸
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPayOrderPrintDataApi } from '@/api/finance'
import type { PayOrderPrintVO } from '@/api/finance'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref(false)
const data = ref<PayOrderPrintVO>({ payOrder: {}, items: [] })
const printContent = ref<HTMLElement | null>(null)

const stallDisplay = computed(() => {
  const po = data.value.payOrder
  if (po.stallName) return `${po.stallName}（${po.stallNumber || ''}）`
  return `铺位#${po.stallId}`
})

const merchantDisplay = computed(() => {
  const po = data.value.payOrder
  if (po.merchantName) return po.merchantName
  return po.merchantId ? `商户#${po.merchantId}` : '-'
})

onMounted(async () => {
  const payOrderId = Number(route.params.id)
  if (!payOrderId) {
    error.value = true
    loading.value = false
    return
  }
  try {
    const resp = await getPayOrderPrintDataApi(payOrderId)
    data.value = resp
    loading.value = false
  } catch {
    error.value = true
    loading.value = false
  }
})

function formatMoney(val: number | string | null | undefined): string {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

function handlePrint(): void {
  window.print()
}

function handleClose(): void {
  window.close()
}
</script>

<style scoped>
.print-page {
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

.print-btn-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #f5f7fa;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  text-align: center;
}

.print-content {
  max-width: 800px;
  margin: 60px auto 0;
  font-family: SimSun, serif;
  font-size: 14px;
}

.print-title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 20px;
}

.print-subtitle {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
}

.print-info-table {
  width: 100%;
  border-collapse: collapse;
}

.print-info-table th,
.print-info-table td {
  border: 1px solid #000;
  padding: 6px 8px;
  text-align: left;
}

.print-info-table th {
  background: #f0f0f0;
  width: 100px;
}

.print-detail-table {
  width: 100%;
  border-collapse: collapse;
}

.print-detail-table th,
.print-detail-table td {
  border: 1px solid #000;
  padding: 6px 8px;
  text-align: left;
}

.print-detail-table th {
  background: #f0f0f0;
}

.col-money {
  text-align: right;
  font-weight: bold;
}

.print-remark {
  margin-top: 10px;
}

.g-loading,
.g-error {
  text-align: center;
  padding: 100px 0;
  font-size: 16px;
  color: #909399;
}

/* 打印样式 */
@media print {
  .print-btn-bar {
    display: none;
  }

  .print-page {
    padding: 0;
  }

  .print-content {
    margin-top: 0;
    max-width: none;
  }

  @page {
    margin: 20mm 15mm;
  }
}
</style>