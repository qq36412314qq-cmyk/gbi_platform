<template>
  <div class="g-page-wrap finance-flow-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">财务流水</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="来源类型">
        <el-select v-model="query.sourceType" placeholder="全部" clearable style="width: 140px">
          <el-option label="物业费" value="fee_bill" />
          <el-option label="水电费" value="water_elec" />
        </el-select>
      </el-form-item>
      <el-form-item label="缴费状态">
        <el-select v-model="query.payStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="待缴" :value="0" />
          <el-option label="部分缴费" :value="1" />
          <el-option label="已缴" :value="2" />
          <el-option label="已退费" :value="3" />
          <el-option label="已冲红" :value="4" />
          <el-option label="已作废" :value="5" />
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
      <el-table v-loading="loading" :data="records" border stripe @expand-change="loadFlowItems">
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
        <el-table-column prop="id" label="缴费单ID" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="140" align="center" />
        <el-table-column label="所属公司" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团' : `公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sourceTypeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payBillNo" label="缴费单编号" width="180" align="center" />
        <el-table-column label="应收总额" width="110" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已缴金额" width="100" align="right">
          <template #default="{ row }">
            <span>{{ Number(row.paidAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未缴金额" width="100" align="right">
          <template #default="{ row }">
            <span :class="Number(row.unpaidAmount) > 0 ? 'g-text-danger' : ''">{{ Number(row.unpaidAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="绑定铺位" min-width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>铺位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payStatusType(row.payStatus)">
              {{ row.payStatusText || '待缴' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
/**
 * 财务流水页：finance_pay_order 缴费单主表（只读视图）
 * 主数据表：finance_pay_order（聚合支付载体）
 * 展开明细表：finance_pay_order_item（通过 payBillId 直接关联）
 * 功能：分页查询、条件筛选、展开明细
 */
import { ref, reactive } from 'vue'
import {
  getFinancePayOrderPageApi,
  getFinancePayOrderItemsApi,
  type PayOrderVO,
  type PayOrderItemVO
} from '@/api/finance'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<PayOrderVO>(getFinancePayOrderPageApi, {
  sourceType: undefined,
  payStatus: undefined,
  startTime: undefined,
  endTime: undefined
})

const timeRange = ref<[string, string] | null>(null)

function handleReset(): void {
  resetQuery()
  timeRange.value = null
  loadData()
}

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

/* ---------------- 展开明细 ---------------- */
const expandedItems = reactive<Record<number, PayOrderItemVO[]>>({})

/**
 * Element Plus v2.9+ el-table 展开/折叠统一走 expand-change
 * 回调签名：(row, expandedRows: Row[]) —— 展开时 expandedRows 包含当前行，折叠时不包含
 * 注意：用 reactive 而非 ref，保证新增 key 时 template 中 expandedItems[row.id] 能触发重渲染
 * 改造说明：原通过 flowId 间接查询（finance_pay_flow.pay_bill_id → finance_pay_order_item）
 *           现直接通过 payOrderId 查询（finance_pay_order.id = finance_pay_order_item.pay_bill_id）
 */
async function loadFlowItems(row: PayOrderVO, expandedRows: PayOrderVO[]): Promise<void> {
  const isExpanding = expandedRows.some((r) => r.id === row.id)
  if (!isExpanding) return
  if (expandedItems[row.id]) return

  try {
    const data = await getFinancePayOrderItemsApi(row.id)
    expandedItems[row.id] = data
  } catch (err) {
    console.error('加载缴费单明细失败 rowId=', row.id, err)
    expandedItems[row.id] = []
  }
}

/* ---------------- 状态标签颜色 ---------------- */
function payStatusType(status: number | undefined | null): 'primary' | 'success' | 'info' | 'warning' | 'danger' | undefined {
  switch (status) {
    case 0: return 'warning'   // 待缴
    case 1: return 'warning'   // 部分缴费
    case 2: return 'success'   // 已缴
    case 3: return 'info'      // 已退费
    case 4: return 'info'      // 已冲红
    case 5: return 'info'      // 已作废
    default: return undefined
  }
}
</script>

<style scoped>
.g-money {
  font-weight: 600;
  color: #e6a23c;
}
.g-text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
