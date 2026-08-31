<template>
  <div class="g-page-wrap discount-apply-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">优惠申请</span>
      <span class="g-tip">随合同提交自动生成，超集团阈值自动发起审批，审批通过后合同优惠生效</span>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="合同编号">
        <el-input v-model="query.contractNo" placeholder="合同编号" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="申请状态">
        <el-select v-model="query.applyStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="(text, value) in applyStatusMap" :key="value" :label="text" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否需审批">
        <el-select v-model="query.needAudit" placeholder="全部" clearable style="width: 120px">
          <el-option label="需审批" :value="1" />
          <el-option label="无需审批" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="applyNo" label="申请编号" width="170" show-overflow-tooltip />
        <el-table-column prop="policyName" label="优惠策略" width="140" show-overflow-tooltip />
        <el-table-column prop="contractNo" label="合同编号" width="140" show-overflow-tooltip />
        <el-table-column prop="waiveMonths" label="免租月数" width="90" align="center">
          <template #default="{ row }">{{ row.waiveMonths ?? 0 }} 个月</template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="90" align="center">
          <template #default="{ row }">{{ Number(row.discountRate ?? 100).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="deductAmount" label="减免金额" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.deductAmount) }}</template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠总额" width="110" align="right">
          <template #default="{ row }">
            <span class="g-text-danger">-￥{{ formatMoney(row.discountAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="needAuditText" label="是否需审批" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.needAudit === 1 ? 'warning' : 'info'">{{ row.needAuditText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyStatusText" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="applyStatusTagType(row.applyStatus ?? 0)">{{ row.applyStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyUserName" label="申请人" width="100" align="center" />
        <el-table-column prop="createTime" label="申请时间" width="150" />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetail(row.id)">详情</el-button>
            <AuthBtn
              permission="discount:apply:cancel"
              size="small"
              type="danger"
              link
              :disabled="row.applyStatus !== 0 && row.applyStatus !== 1"
              @click="handleCancel(row)"
            >撤销</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="优惠申请详情" width="640px" destroy-on-close>
      <div v-if="detail" v-loading="detailLoading">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="申请编号">{{ detail.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="优惠策略">{{ detail.policyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同编号">{{ detail.contractNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applyUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="免租月数">{{ detail.waiveMonths ?? 0 }} 个月</el-descriptions-item>
          <el-descriptions-item label="折扣率">{{ Number(detail.discountRate ?? 100).toFixed(2) }}%</el-descriptions-item>
          <el-descriptions-item label="减免金额">￥{{ formatMoney(detail.deductAmount) }}</el-descriptions-item>
          <el-descriptions-item label="优惠总额">-￥{{ formatMoney(detail.discountAmount) }}</el-descriptions-item>
          <el-descriptions-item label="是否需审批">{{ detail.needAuditText || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.applyStatusText || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批完成时间">{{ detail.auditTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 优惠申请页：随合同提交自动生成（草稿→阈值判定→审批），
 * 超集团阈值（免租/折扣/减免/占比）自动发起 contract_discount 审批
 */
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDiscountApplyPageApi,
  getDiscountApplyDetailApi,
  cancelDiscountApplyApi,
  type DiscountApplyVO
} from '@/api/discount'
import { useTable } from '@/hooks/useTable'
import { formatMoney } from '@/utils/format'

const applyStatusMap: Record<number, string> = { 0: '草稿', 1: '审批中', 2: '通过', 3: '驳回', 4: '作废' }

function applyStatusTagType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3 || status === 4) return 'danger'
  return 'info'
}

const { query, records, total, loading, loadData, resetQuery } = useTable<DiscountApplyVO>(getDiscountApplyPageApi, {
  contractNo: undefined,
  applyStatus: undefined,
  needAudit: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 详情 ---------------- */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<DiscountApplyVO | null>(null)

async function openDetail(id: number): Promise<void> {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await getDiscountApplyDetailApi(id)
  } finally {
    detailLoading.value = false
  }
}

/* ---------------- 撤销 ---------------- */
async function handleCancel(row: DiscountApplyVO): Promise<void> {
  await ElMessageBox.confirm(`确定撤销优惠申请「${row.applyNo}」吗？`, '撤销确认', { type: 'warning' })
  await cancelDiscountApplyApi(row.id)
  ElMessage.success('撤销成功')
  loadData()
}
</script>