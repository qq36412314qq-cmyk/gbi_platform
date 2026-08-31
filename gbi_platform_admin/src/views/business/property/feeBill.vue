<!-- 
  物业费记录管理页面
  数据表：property_fee_bill
  关联表：biz_fee_bill
  权限：propertyFee:bill:*
-->
<template>
  <!-- 数据表: property_fee_bill -->
  <div class="g-page-wrap property-fee-bill-wrap">
    <div class="g-page-header">
      <span class="g-page-title">物业费记录管理</span>
      <div>
        <el-button type="success" @click="handleBatchGenerate" :disabled="selectedRows.length === 0">
          <el-icon><Plus /></el-icon>批量生成账单（{{ selectedRows.length }}）
        </el-button>
        <el-button type="primary" @click="showSingleGenerateDialog">
          <el-icon><DocumentAdd /></el-icon>单条生成
        </el-button>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="记录月份">
        <el-date-picker v-model="query.billMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 150px" clearable />
      </el-form-item>
      <el-form-item label="缴费状态">
        <el-select v-model="query.payStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="待缴" :value="0" />
          <el-option label="部分缴费" :value="1" />
          <el-option label="已缴" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="计费方式">
        <el-select v-model="query.calcMode" placeholder="全部" clearable style="width: 120px">
          <el-option label="定额" :value="1" />
          <el-option label="按面积" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="billMonth" label="记录月份" width="110" align="center" />
        <el-table-column label="计费方式" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.calcMode === 2 ? 'warning' : 'info'">{{ row.calcModeText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定摊位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>摊位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="用量(㎡)" width="90" align="right">
          <template #default="{ row }">
            <span v-if="row.usage != null">{{ Number(row.usage).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.unitPrice != null">{{ Number(row.unitPrice).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="periodFactor" label="周期系数" width="90" align="right">
          <template #default="{ row }">
            {{ row.periodFactor ?? 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="110" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payStatusType(row.payStatus)">{{ row.payStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :disabled="row.payStatus === 2" @click="handleSync(row)">生成账单</el-button>
            <el-button type="info" link size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 单条生成弹窗 -->
    <el-dialog v-model="singleDialogVisible" title="单条生成物业费账单" width="480px" :close-on-click-modal="false">
      <el-form :model="singleForm" label-width="100px">
        <el-form-item label="账单月份">
          <el-date-picker v-model="singleForm.billMonth" type="month" value-format="YYYY-MM" style="width: 100%" />
        </el-form-item>
        <el-form-item label="市场">
          <el-select v-model="singleForm.marketId" placeholder="选择市场" style="width: 100%" clearable @change="handleSingleMarketChange">
            <el-option v-for="m in marketOptions" :key="m.id" :label="m.marketName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="singleForm.categoryId" placeholder="选择分类" style="width: 100%" clearable @change="handleSingleCategoryChange">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摊位">
          <el-select v-model="singleForm.stallId" filterable placeholder="搜索摊位" style="width: 100%" @change="handleStallSelect">
            <el-option v-for="s in stallOptions" :key="s.id" :label="`${s.stallNumber} ${s.stallName}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <div v-if="singlePreview.amount !== undefined" class="amount-preview">
          计费方式：{{ singlePreview.calcModeText }}　单价：{{ singlePreview.unitPrice }} 元/㎡
          <br>周期系数：{{ singlePreview.periodFactor }}　
          <span class="amount">金额：{{ singlePreview.amount }} 元</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="singleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="singleLoading" @click="handleSingleGenerate">生成记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type ElTable } from 'element-plus'
import {
  getPropertyFeeBillPageApi,
  generatePropertyFeeBillApi,
  generatePropertyFeeBillSingleApi,
  previewPropertyFeeBillApi,
  getPropertyFeeBillDetailApi,
  syncPropertyFeeBillApi,
  type PropertyFeeBillVO
} from '@/api/propertyFee'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { getCategoryListApi, type CategoryVO } from '@/api/lease'
import { getStallOptionsApi, type StallOptionVO } from '@/api/lease'
import { useTable } from '@/hooks/useTable'

const router = useRouter()

// 分页查询列表
const { query, records, total, loading, loadData, resetQuery } = useTable<PropertyFeeBillVO>(getPropertyFeeBillPageApi, {
  billMonth: undefined, payStatus: undefined, calcMode: undefined, marketId: undefined, stallId: undefined
})

// 勾选
const selectedRows = ref<PropertyFeeBillVO[]>([])
function handleSelectionChange(rows: PropertyFeeBillVO[]): void {
  selectedRows.value = rows
}

function handleReset(): void { resetQuery(); loadData() }

// 批量生成
async function handleBatchGenerate(): Promise<void> {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  const month = query.billMonth || selectedRows.value[0].billMonth
  if (!month) {
    ElMessage.warning('请选择账单月份')
    return
  }
  try {
    const count = await generatePropertyFeeBillApi({ billMonth: month, marketId: query.marketId })
    ElMessage.success(`批量生成成功，共 ${count} 条记录`)
    selectedRows.value = []
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '生成失败')
  }
}

// 同步账单到未支付订单
async function handleSync(row: PropertyFeeBillVO): Promise<void> {
  if (row.payStatus === 2) { ElMessage.warning('该记录已缴费，无需生成账单'); return }
  try {
    const msg = await syncPropertyFeeBillApi(row.id)
    ElMessage.success(msg)
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '同步失败')
  }
}

// 缴费状态类型映射
const payStatusType = (status?: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'info', 2: 'success' }
  return map[status ?? 0] || ''
}

// 详情查看
const handleDetail = async (row: PropertyFeeBillVO) => {
  try {
    const detail = await getPropertyFeeBillDetailApi(row.id)
    ElMessage.info(`账单金额: ${Number(detail.amount).toFixed(2)} 元`)
  } catch {
    ElMessage.info('账单ID: ' + row.id)
  }
}

// 单条生成相关
const singleDialogVisible = ref(false)
const singleLoading = ref(false)
const singleForm = reactive({ billMonth: '', marketId: null as number | null, categoryId: null as number | null, stallId: null as number | null })
const marketOptions = ref<MarketVO[]>([])
const categoryOptions = ref<CategoryVO[]>([])
const stallOptions = ref<StallOptionVO[]>([])
const singlePreview = ref<Partial<PropertyFeeBillVO>>({})

// 打开单条生成弹窗
const showSingleGenerateDialog = async () => {
  if (marketOptions.value.length === 0) {
    try {
      marketOptions.value = await getMarketListApi()
    } catch { /* 静默失败 */ }
  }
  if (categoryOptions.value.length === 0) {
    try {
      categoryOptions.value = await getCategoryListApi()
    } catch { /* 静默失败 */ }
  }
  singleForm.billMonth = query.billMonth || ''
  singleForm.marketId = null
  singleForm.categoryId = null
  singleForm.stallId = null
  stallOptions.value = []
  singlePreview.value = {}
  singleDialogVisible.value = true
}

// 市场切换时清空分类和摊位
const handleSingleMarketChange = () => {
  singleForm.categoryId = null
  singleForm.stallId = null
  stallOptions.value = []
  singlePreview.value = {}
}

// 分类切换时清空摊位
const handleSingleCategoryChange = async () => {
  singleForm.stallId = null
  singlePreview.value = {}
  if (singleForm.marketId && singleForm.categoryId) {
    stallOptions.value = await getStallOptionsApi({ marketId: singleForm.marketId, stallCategoryId: singleForm.categoryId })
  }
}

// 选择摊位时预览金额
const handleStallSelect = async (stallId: number) => {
  if (!singleForm.billMonth || !stallId) { singlePreview.value = {}; return }
  try {
    const preview = await previewPropertyFeeBillApi({ billMonth: singleForm.billMonth, stallId })
    singlePreview.value = {
      calcMode: preview.calcMode, calcModeText: preview.calcModeText,
      periodType: preview.periodType, periodTypeText: preview.periodTypeText,
      usage: preview.usage, unitPrice: preview.unitPrice,
      periodFactor: preview.periodFactor, amount: preview.amount
    }
  } catch { singlePreview.value = {} }
}

// 提交单条生成
const handleSingleGenerate = async () => {
  if (!singleForm.billMonth || !singleForm.stallId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  singleLoading.value = true
  try {
    await generatePropertyFeeBillSingleApi({ billMonth: singleForm.billMonth, stallId: singleForm.stallId })
    ElMessage.success('生成成功')
    singleDialogVisible.value = false
    loadData()
    // 生成成功后跳转到物业费记录页面
    router.push('/property/feeBill')
  } catch (e: any) {
    ElMessage.error(e?.msg || '生成失败')
  } finally {
    singleLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.g-money { color: #f56c6c; font-weight: 600; }
.amount-preview { background: #f5f7fa; padding: 12px; border-radius: 4px; margin-top: 12px; }
.amount { color: #f56c6c; font-weight: 600; }
</style>
