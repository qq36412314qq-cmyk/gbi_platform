<!--
  水电费记录管理页面
  数据表：water_elec_bill
  关联表：water_elec_meter、finance_fee_pay_bill
  权限：waterElec:bill:*
-->
<template>
  <div class="g-page-wrap water-elec-bill-wrap">
    <div class="g-page-header">
      <span class="g-page-title">水电费记录管理</span>
      <div>
        <el-button type="success" @click="handleBatchGenerate" :disabled="selectedRows.length === 0">
          <el-icon><Plus /></el-icon>批量生成账单（{{ selectedRows.length }}）
        </el-button>
        <AuthBtn permission="waterElec:bill:generate" type="primary" @click="openGenerateDialog">生成记录</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="缴费状态">
        <el-select v-model="query.payStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="待缴" :value="0" />
          <el-option label="已缴" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="收费类别">
        <el-select v-model="query.category" placeholder="全部" clearable style="width: 120px">
          <el-option label="水费" :value="3" />
          <el-option label="电费" :value="4" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="billMonth" label="账单月份" width="110" align="center" />
        <el-table-column prop="tenantName" label="租户名称" width="130" show-overflow-tooltip />
        <el-table-column label="收费类别" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.category === 3 ? 'success' : 'warning'">{{ row.categoryText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定铺位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>铺位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="用量" width="90" align="right">
          <template #default="{ row }">
            <span v-if="row.usage != null">{{ Number(row.usage).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="90" align="right">
          <template #default="{ row }">
            <span v-if="row.unitPrice != null" :title="`元，铺位绑定收费规则单价快照，规则修改不回溯`">{{ Number(row.unitPrice).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="费用合计" width="110" align="right">
          <template #default="{ row }">
            <span class="g-money">{{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payStatusType(row.payStatus)">{{ row.payStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span>{{ row.createTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="waterElec:pay:add" size="small" type="primary" link :disabled="row.payStatus === 1 || row.hasFeeBill" @click="handleSync(row)">{{ row.hasFeeBill ? "账单已生成" : "生成账单" }}</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <CommonDialog v-model="generateDialogVisible" title="生成水电费记录" width="640px" :loading="generateLoading" @confirm="handleGenerate">
      <el-alert type="info" :closable="false" show-icon title="按铺位绑定收费规则计算：水费/电费=用量×规则单价；未绑定对应类别规则时该类别金额为0；同铺位同月份已存在记录自动跳过" style="margin-bottom: 12px" />
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="110px">
        <el-form-item label="市场" prop="marketId">
          <el-select v-model="generateForm.marketId" placeholder="全部市场" clearable @change="handleMarketChange" style="width: 150px">
            <el-option v-for="item in marketOptions" :key="item.id" :label="item.marketName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="抄表读数">
          <div>
            <div v-for="(read, index) in generateForm.meterReads" :key="index" class="b-read-row">
              <el-select v-model="read.meterId" placeholder="选择设备" style="width: 180px" @change="(val) => selectMeter(index, val)">
                <el-option v-for="m in meterOptions" :key="m.id" :label="`${m.stallName}(${m.meterNo})`" :value="m.id" />
              </el-select>
              <el-input-number v-model="read.currentRead" :precision="2" :min="0" placeholder="当前读数" style="width: 120px" />
              <el-button type="danger" size="small" @click="removeReadRow(index)">删除</el-button>
            </div>
            <div style="margin-top: 8px">
              <el-button type="primary" plain size="small" :disabled="meterOptions.length === 0" @click="addReadRow">+ 添加设备</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getBillPageApi, generateBillApi, getMeterPageApi, syncWaterElecBillApi, batchSyncWaterElecBillApi, type WaterElecBillVO, type WaterElecMeterVO, type MeterReadItem } from '@/api/waterElec'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { useTable } from '@/hooks/useTable'

const router = useRouter()

const { query, records, total, loading, loadData, resetQuery } = useTable<WaterElecBillVO>(getBillPageApi, {
  payStatus: undefined, category: undefined
})

const selectedRows = ref<WaterElecBillVO[]>([])
function handleSelectionChange(rows: WaterElecBillVO[]): void {
  selectedRows.value = rows
}

function handleReset(): void { resetQuery(); loadData() }

// 缴费状态类型映射（与物业费记录管理一致）
const payStatusType = (status?: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'info', 2: 'success' }
  return map[status ?? 0] || ''
}

// 批量生成账单：将选中记录同步到 finance_fee_pay_bill（统一账单表）
async function handleBatchGenerate(): Promise<void> {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  // 过滤出未同步到 finance_fee_pay_bill 的记录（hasFeeBill=false 且未缴纳）
  const pendingRows = selectedRows.value.filter((r) => !r.hasFeeBill && r.payStatus !== 1)
  if (pendingRows.length === 0) {
    ElMessage.warning('选中的记录已全部生成账单或已缴费，无需重复生成')
    return
  }
  const ids = pendingRows.map((r) => r.id)
  try {
    const count = await batchSyncWaterElecBillApi(ids)
    ElMessage.success('批量生成成功，共写入 ${count} 条未支付订单')
    selectedRows.value = []
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '批量生成失败')
  }
}

async function handleSync(row: WaterElecBillVO): Promise<void> {
  if (row.payStatus === 1) { return }
  try {
    const msg = await syncWaterElecBillApi(row.id)
    ElMessage.success(msg)
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || '同步失败')
  }
}

const generateDialogVisible = ref(false)
const generateLoading = ref(false)
const generateFormRef = ref<FormInstance>()
const marketOptions = ref<MarketVO[]>([])
const meterOptions = ref<WaterElecMeterVO[]>([])

const generateForm = reactive<{ marketId?: number; meterReads: MeterReadItem[] }>({
  marketId: undefined, meterReads: []
})

const generateRules: FormRules = {}

async function loadMeterOptions(): Promise<void> {
  const data = await getMeterPageApi({ pageNum: 1, pageSize: 100, marketId: generateForm.marketId })
  meterOptions.value = data.records || []
}

async function handleMarketChange(): Promise<void> {
  generateForm.meterReads = []
  await loadMeterOptions()
  if (meterOptions.value.length === 0) {
    ElMessage.warning(generateForm.marketId ? '该市场下暂无设备' : '当前无任何设备')
  } else {
    addReadRow()
  }
}

async function openGenerateDialog(): Promise<void> {
  generateForm.marketId = undefined
  generateForm.meterReads = []
  await loadMeterOptions()
  if (meterOptions.value.length === 0) {
    ElMessage.warning('当前无任何设备，请先在水电表管理中绑定设备')
  } else {
    addReadRow()
  }
  generateDialogVisible.value = true
}

function addReadRow(): void {
  generateForm.meterReads.push({ meterId: null as unknown as number, currentRead: 0 })
}

function removeReadRow(index: number): void {
  generateForm.meterReads.splice(index, 1)
}

function selectMeter(index: number, meterId: number): void {
  const meter = meterOptions.value.find((m) => m.id == meterId)
  if (meter) {
    generateForm.meterReads[index].currentRead = meter.currentRead ?? 0
  }
}

async function handleGenerate(): Promise<void> {
  if (!generateFormRef.value) return
  await generateFormRef.value.validate(async (valid) => {
    if (!valid) return
    const validReads = generateForm.meterReads.filter((r) => r.meterId != null && r.currentRead != null)
    if (validReads.length === 0) {
      ElMessage.warning('请至少添加一条抄表读数')
      return
    }
    generateLoading.value = true
    try {
      const count = await generateBillApi({ billMonth: new Date().toISOString().slice(0,7), meterReads: validReads })
      ElMessage.success('记录生成完成，共 ${count} 条记录，已存在记录自动跳过')
      generateDialogVisible.value = false
      selectedRows.value = []
      loadData()
      router.push('/property/unpaidBill')
    } catch (e: any) { ElMessage.error(e?.msg || '生成失败') } finally {
      generateLoading.value = false
    }
  })
}

onMounted(async () => {
  marketOptions.value = await getMarketListApi()
})
</script>

<style scoped>
.b-read-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.g-money { color: #f56c6c; font-weight: 600; }
</style>