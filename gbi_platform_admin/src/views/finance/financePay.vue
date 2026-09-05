<template>
  <div class="g-page-wrap finance-pay-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">缴费明细单</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="缴费单号">
        <el-input v-model="query.payBillNo" placeholder="模糊查询" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="来源类型">
        <el-select v-model="query.sourceType" placeholder="全部" clearable style="width: 130px">
          <el-option label="物业费" value="fee_bill" />
          <el-option label="水电费" value="water_elec" />
        </el-select>
      </el-form-item>
      <el-form-item label="缴费状态">
        <el-select v-model="query.payStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="待缴" :value="0" />
          <el-option label="已缴" :value="1" />
          <el-option label="部分缴费" :value="2" />
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
              <el-table-column prop="ruleName" label="收费规则" min-width="140" show-overflow-tooltip />
              <el-table-column label="应收金额" width="100" align="right">
                <template #default="{ row: item }">
                  <span class="g-money">{{ formatMoney(item.amount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="优惠抵扣" width="100" align="right">
                <template #default="{ row: item }">
                  <span>{{ formatMoney(item.discountAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="已缴金额" width="100" align="right">
                <template #default="{ row: item }">
                  <span>{{ formatMoney(item.paidAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="未缴金额" width="100" align="right">
                <template #default="{ row: item }">
                  <span :class="Number(item.unpaidAmount) > 0 ? 'g-text-danger' : ''">{{ formatMoney(item.unpaidAmount) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="payBillNo" label="缴费单编号" width="180" show-overflow-tooltip />
        <el-table-column label="来源类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sourceTypeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属公司" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团' : `公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stallId" label="摊位ID" width="80" align="center" />
        <el-table-column prop="merchantId" label="商户ID" width="80" align="center" />
        <el-table-column label="应收总额" width="110" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已缴金额" width="110" align="right">
          <template #default="{ row }">
            <span>{{ formatMoney(row.paidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未缴金额" width="110" align="right">
          <template #default="{ row }">
            <span :class="Number(row.unpaidAmount) > 0 ? 'g-text-danger' : ''">{{ formatMoney(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.payStatus === 1 ? 'success' : row.payStatus === 2 ? 'warning' : 'info'">
              {{ row.payStatusText || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" width="160" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
/**
 * 缴费明细单页：finance_pay_order 主表 + finance_pay_order_item 明细展开
 */
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getFinancePayOrderPageApi, getFinancePayOrderItemsApi, type PayOrderVO, type PayOrderItemVO } from '@/api/finance'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<PayOrderVO>(getFinancePayOrderPageApi, {
  payBillNo: undefined,
  sourceType: undefined,
  payStatus: undefined,
  companyId: undefined,
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

/* ---------------- 展开明细 ---------------- */
/** 展开行数据缓存 */
const expandedItems = ref<Record<number, PayOrderItemVO[]>>({})

async function loadFlowItems(row: PayOrderVO): Promise<void> {
  if (!expandedItems.value[row.id]) {
    try {
      const items = await getFinancePayOrderItemsApi(row.id)
      expandedItems.value[row.id] = items
    } catch {
      expandedItems.value[row.id] = []
    }
  }
}

/* ---------------- 工具函数 ---------------- */
function formatMoney(val: number | string | null | undefined): string {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}
</script>

<style scoped>
.g-money {
  font-weight: 600;
  color: #e6a23c;
}
.g-text-danger {
  color: #f56c6c;
}
</style>