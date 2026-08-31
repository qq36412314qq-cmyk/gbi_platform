<!-- 
  水电费记录管理页面
  数据表：water_elec_bill
  关联表：water_elec_meter、biz_fee_bill
  权限：waterElec:bill:*
-->
<template>
  <!-- 数据表: water_elec_bill -->
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
      <el-form-item label="记录月份">
        <el-date-picker v-model="query.billMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 150px" clearable />
      </el-form-item>
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
        <el-table-column prop="billMonth" label="记录月份" width="110" align="center" />
        <el-table-column label="收费类别" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.category === 3 ? 'success' : 'warning'">{{ row.categoryText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定摊位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>摊位#{{ row.stallId }}</span>
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
            <span v-if="row.unitPrice != null" :title="`元，摊位绑定收费规则单价快照，规则修改不回溯`">{{ Number(row.unitPrice).toFixed(2) }}</span>
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
            <el-tag size="small" :type="row.payStatus === 1 ? 'success' : 'warning'">{{ row.payStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="waterElec:pay:add" size="small" type="primary" link :disabled="row.payStatus === 1" @click="handleSync(row)">生成账单</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <CommonDialog v-model="generateDialogVisible" title="生成水电费账单" width="640px" :loading="generateLoading" @confirm="handleGenerate">
      <el-alert type="info" :closable="false" show-icon title="按摊位绑定收费规则计算：水费/电费=用量×规则单价；未绑定对应类别规则时该类别金额为0；同摊位同月份已存在记录自动跳过" style="margin-bottom: 12px" />
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="110px">
        <el-form-item label="记录月份" prop="billMonth">
          <el-date-picker v-model="generateForm.billMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" />
        </el-form-item>
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
            <el-button type="primary" plain size="small" :disabled="meterOptions.length === 0" @click="addReadRow">+ 添加设备</el-button>
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
import { getBillPageApi, generateBillApi, getMeterPageApi, syncWaterElecBillApi, type WaterElecBillVO, type WaterElecMeterVO, type MeterReadItem } from '@/api/waterElec'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { useTable } from '@/hooks/useTable'

const router = useRouter()

// 分页查询列表
const { query, records, total, loading, loadData, resetQuery } = useTable<WaterElecBillVO>(getBillPageApi, {
  billMonth: undefined, payStatus: undefined, category: undefined
})

// 勾选
const selectedRows = ref<WaterElecBillVO[]>([])
function handleSelectionChange(rows: WaterElecBillVO[]): void {
  selectedRows.value = rows
}

function handleReset(): void { resetQuery(); loadData() }

// 批量生成：打开抄表弹窗，预设第一条记录的月份
async function handleBatchGenerate(): Promise<void> {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  // 预设月份为选中记录的第一条月份
  generateForm.billMonth = selectedRows.value[0].billMonth
  generateForm.marketId = undefined
  generateForm.meterReads = []
  await loadMeterOptions()
  if (meterOptions.value.length === 0) {
    ElMessage.warning('当前无任何设备，请先在水电表管理中绑定设备')
    return
  }
  addReadRow()
  generateDialogVisible.value = true
}

// 同步账单到未支付订单，处理重复生成情况
async function handleSync(row: WaterElecBillVO): Promise<void> {
  if (row.payStatus === 1) { return }
  try {
    const msg = await syncWaterElecBillApi(row.id)
    ElMessage.success(msg)
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.msg || "同步失败")
  }
}

const generateDialogVisible = ref(false)
const generateLoading = ref(false)
const generateFormRef = ref<FormInstance>()
const marketOptions = ref<MarketVO[]>([])
const meterOptions = ref<WaterElecMeterVO[]>([])

const generateForm = reactive<{ billMonth: string; marketId?: number; meterReads: MeterReadItem[] }>({
  billMonth: '', marketId: undefined, meterReads: []
})

const generateRules: FormRules = {
  billMonth: [{ required: true, message: '请选择记录月份', trigger: 'change' }]
}

// 加载设备列表（按市场筛选）
async function loadMeterOptions(): Promise<void> {
  const data = await getMeterPageApi({ pageNum: 1, pageSize: 100, marketId: generateForm.marketId })
  meterOptions.value = data.records || []
}

// 市场切换时清空抄表记录并重新加载设备
async function handleMarketChange(): Promise<void> {
  generateForm.meterReads = []
  await loadMeterOptions()
  if (meterOptions.value.length === 0) {
    ElMessage.warning(generateForm.marketId ? '该市场下暂无设备' : '当前无任何设备')
  } else {
    addReadRow()
  }
}

// 打开生成弹窗
async function openGenerateDialog(): Promise<void> {
  generateForm.billMonth = ''
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

// 添加抄表行
function addReadRow(): void {
  generateForm.meterReads.push({ meterId: null as unknown as number, currentRead: 0 })
}

// 删除抄表行
function removeReadRow(index: number): void {
  generateForm.meterReads.splice(index, 1)
}

// 选择设备时预填当前读数
function selectMeter(index: number, meterId: number): void {
  const meter = meterOptions.value.find((m) => m.id == meterId)
  if (meter) {
    generateForm.meterReads[index].currentRead = meter.currentRead ?? 0
  }
}

// 提交生成账单
async function handleGenerate(): Promise<void> {
  if (!generateFormRef.value) return
  await generateFormRef.value.validate(async (valid) => {
    if (!valid) return
    // 过滤有效抄表记录
    const validReads = generateForm.meterReads.filter((r) => r.meterId != null && r.currentRead != null)
    if (validReads.length === 0) {
      ElMessage.warning('请至少添加一条抄表读数')
      return
    }
    generateLoading.value = true
    try {
      const count = await generateBillApi({ billMonth: generateForm.billMonth, meterReads: validReads })
      ElMessage.success(`记录生成完成，共 ${count} 条记录，已存在记录自动跳过`)
      generateDialogVisible.value = false
      selectedRows.value = []
      loadData()
      // 生成成功后跳转到未支付订单页面
      router.push('/property/unpaidBill')
    } catch (e: any) { ElMessage.error(e?.msg || "生成失败") } finally {
      generateLoading.value = false
    }
  })
}

// 初始化加载市场列表
onMounted(async () => {
  marketOptions.value = await getMarketListApi()
})
</script>

<style scoped>
.b-read-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.g-money { color: #f56c6c; font-weight: 600; }
</style>

