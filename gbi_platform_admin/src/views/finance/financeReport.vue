<template>
  <div class="g-page-wrap finance-report-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">营收统计</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadSummary" @reset="handleReset">
      <el-form-item label="业务类型">
        <el-select v-model="query.businessType" placeholder="全部" clearable style="width: 140px">
          <el-option label="租金" value="rent" />
          <el-option label="水电物业" value="water_elec" />
          <el-option label="押金" value="deposit" />
          <el-option label="营销抵扣" value="marketing" />
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

    <!-- 汇总卡片区 -->
    <el-row :gutter="16" class="b-summary-cards">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="b-card-label">收入合计</div>
          <div class="b-card-value g-money">{{ formatMoney(totalIncome) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="b-card-label">支出合计（退费）</div>
          <div class="b-card-value g-money-out">{{ formatMoney(totalExpense) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="b-card-label">净收入</div>
          <div class="b-card-value">{{ formatMoney(totalIncome - totalExpense) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="b-card-label">流水笔数</div>
          <div class="b-card-value">{{ totalCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 汇总明细表 -->
    <el-card shadow="never" class="b-table-card">
      <el-table v-loading="loading" :data="summaryList" border stripe>
        <el-table-column label="所属公司" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团' : `子公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="业务类型" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.businessTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收支方向" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.flowType === 1 ? 'danger' : 'success'">{{ row.flowTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="flowCount" label="笔数" min-width="100" align="center" />
        <el-table-column label="金额合计" min-width="140" align="right">
          <template #default="{ row }">
            <span :class="row.flowType === 1 ? 'g-money' : 'g-money-out'">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 营收统计页：按公司/业务类型/收支方向聚合财务流水
 * 子公司账号仅看本公司，集团账号可看全集团汇总（拦截器自动隔离）
 */
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getFinanceSummaryApi,
  type FinanceSummaryQueryDTO,
  type FinanceSummaryVO
} from '@/api/finance'

const query = reactive<FinanceSummaryQueryDTO>({
  businessType: undefined,
  startTime: undefined,
  endTime: undefined
})

const timeRange = ref<[string, string] | null>(null)
const summaryList = ref<FinanceSummaryVO[]>([])
const loading = ref(false)

function handleTimeChange(): void {
  query.startTime = timeRange.value?.[0]
  query.endTime = timeRange.value?.[1]
}

function handleReset(): void {
  query.businessType = undefined
  query.startTime = undefined
  query.endTime = undefined
  timeRange.value = null
  loadSummary()
}

async function loadSummary(): Promise<void> {
  loading.value = true
  try {
    summaryList.value = await getFinanceSummaryApi({ ...query })
  } finally {
    loading.value = false
  }
}

/** 收入合计 */
const totalIncome = computed(() => {
  return summaryList.value
    .filter((s) => s.flowType === 1)
    .reduce((sum, s) => sum + Number(s.totalAmount || 0), 0)
})

/** 支出合计 */
const totalExpense = computed(() => {
  return summaryList.value
    .filter((s) => s.flowType === 2)
    .reduce((sum, s) => sum + Number(s.totalAmount || 0), 0)
})

/** 流水总笔数 */
const totalCount = computed(() => {
  return summaryList.value.reduce((sum, s) => sum + Number(s.flowCount || 0), 0)
})

function formatMoney(value: number): string {
  return `¥ ${Number(value || 0).toFixed(2)}`
}

onMounted(() => loadSummary())
</script>

<style scoped>
.b-summary-cards {
  margin-bottom: 16px;
}
.b-card-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
.b-card-value {
  font-size: 22px;
  font-weight: 700;
}
.g-money {
  color: var(--el-color-danger);
}
.g-money-out {
  color: var(--el-color-success);
}
.b-table-card {
  border-radius: var(--g-card-radius, 6px);
}
</style>