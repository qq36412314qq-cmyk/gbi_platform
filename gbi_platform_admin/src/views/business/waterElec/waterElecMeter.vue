<template>
  <div class="g-page-wrap water-elec-meter-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">水电表管理</span>
      <div>
        <AuthBtn permission="waterElec:add" type="primary" @click="openDialog()">新增设备</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="设备编号">
        <el-input v-model="query.meterNo" placeholder="输入设备编号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="表类型">
        <el-select v-model="query.meterType" placeholder="全部" clearable style="width: 120px">
          <el-option label="水表" :value="1" />
          <el-option label="电表" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="通电" :value="1" />
          <el-option label="断电" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="meterNo" label="设备编号" min-width="140" />
        <el-table-column label="表类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.meterType === 1 ? 'primary' : 'warning'">{{ row.meterTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定铺位" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.stallName">{{ row.stallMarketName || '未知市场' }} / {{ row.categoryName || '未分类' }} / {{ row.stallName }}（{{ row.stallNumber }}）</span>
            <span v-else>铺位#{{ row.stallId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gatewayCode" label="网关编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="currentRead" label="当前读数" width="110" align="right" />
        <el-table-column prop="balanceAmount" label="账户余额" width="110" align="right" />
        <el-table-column label="设备状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="waterElec:read" size="small" type="primary" link @click="openReadDialog(row)">
              抄表
            </AuthBtn>
            <AuthBtn permission="waterElec:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="waterElec:switch" size="small" :type="row.status === 1 ? 'danger' : 'success'" link @click="handleSwitch(row)">
              {{ row.status === 1 ? '断电' : '合闸' }}
            </AuthBtn>
            <AuthBtn permission="waterElec:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑设备' : '新增设备'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 绑定铺位：市场 → 租赁分类 → 铺位 三级联动 -->
        <el-form-item label="绑定市场" prop="formMarketId">
          <el-select v-model="form.formMarketId" placeholder="选择市场" clearable style="width: 100%" @change="handleMarketChange">
            <el-option v-for="m in marketOptions" :key="m.id" :label="m.marketName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="租赁分类" prop="formCategoryId">
          <el-select v-model="form.formCategoryId" placeholder="选择租赁分类" clearable style="width: 100%" @change="handleCategoryChange">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定铺位" prop="stallId">
          <el-select v-model="form.stallId" placeholder="请先选择市场/分类" clearable filterable style="width: 100%" :disabled="!form.formMarketId && !form.formCategoryId">
            <el-option v-for="s in stallOptions" :key="s.id" :label="s.stallNumber + (s.stallName ? ` - ${s.stallName}` : '')" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备编号" prop="meterNo">
          <el-input v-model="form.meterNo" placeholder="输入智能表设备编号" maxlength="64" />
        </el-form-item>
        <el-form-item label="表类型" prop="meterType">
          <el-radio-group v-model="form.meterType">
            <el-radio :value="1">水表</el-radio>
            <el-radio :value="2">电表</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="网关编码" prop="gatewayCode">
          <el-input v-model="form.gatewayCode" placeholder="物联网网关编码（可空）" maxlength="64" />
        </el-form-item>
        <el-form-item label="当前读数" prop="currentRead">
          <el-input-number v-model="form.currentRead" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="账户余额" prop="balanceAmount">
          <el-input-number v-model="form.balanceAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 抄表弹窗 -->
    <CommonDialog v-model="readDialogVisible" title="远程抄表" width="480px" :loading="readLoading" @confirm="handleRead">
      <el-form ref="readFormRef" :model="readForm" :rules="readRules" label-width="100px">
        <el-form-item label="设备编号">
          <el-input :model-value="readForm.meterNo" disabled />
        </el-form-item>
        <el-form-item label="当前读数">
          <el-input :model-value="String(readForm.oldRead ?? 0)" disabled />
        </el-form-item>
        <el-form-item label="本次读数" prop="currentRead">
          <el-input-number v-model="readForm.currentRead" :min="readForm.oldRead ?? 0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 水电表管理页：设备台账 + 远程抄表 + 合闸断电（合闸断电属于高危操作，二次确认）
 * 绑定铺位采用「市场 → 租赁分类 → 铺位」三级联动下拉（数据源 market_info / stall_category / stall_info）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getMeterPageApi,
  addMeterApi,
  updateMeterApi,
  deleteMeterApi,
  readMeterApi,
  switchMeterApi,
  type WaterElecMeterDTO,
  type WaterElecMeterVO
} from '@/api/waterElec'
import { getCategoryListApi, getStallOptionsApi, type CategoryVO, type StallOptionVO } from '@/api/lease'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { useTable } from '@/hooks/useTable'

/* ---------------- 三级联动下拉数据源（市场/分类公司级数据，打开页面加载一次） ---------------- */
const marketOptions = ref<MarketVO[]>([])
const categoryOptions = ref<CategoryVO[]>([])
const stallOptions = ref<StallOptionVO[]>([])
onMounted(async () => {
  try {
    marketOptions.value = await getMarketListApi()
  } catch {
    marketOptions.value = []
  }
  try {
    categoryOptions.value = await getCategoryListApi()
  } catch {
    categoryOptions.value = []
  }
})

/* 市场/分类变化 → 清空已选铺位并重新加载铺位选项 */
async function handleMarketChange(): Promise<void> {
  form.stallId = undefined
  await loadStallOptions()
}

async function handleCategoryChange(): Promise<void> {
  form.stallId = undefined
  await loadStallOptions()
}

async function loadStallOptions(): Promise<void> {
  try {
    stallOptions.value = await getStallOptionsApi({
      marketId: form.formMarketId,
      stallCategoryId: form.formCategoryId
    })
  } catch {
    stallOptions.value = []
  }
}

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<WaterElecMeterVO>(getMeterPageApi, {
  meterNo: undefined,
  meterType: undefined,
  status: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<
  Omit<WaterElecMeterDTO, 'stallId'> & { stallId?: number; formMarketId?: number; formCategoryId?: number }
>({
  stallId: undefined as unknown as number,
  formMarketId: undefined,
  formCategoryId: undefined,
  meterNo: '',
  meterType: 1,
  gatewayCode: '',
  currentRead: 0,
  balanceAmount: 0
})

const rules: FormRules = {
  stallId: [{ required: true, message: '请选择绑定铺位', trigger: 'change' }],
  meterNo: [
    { required: true, message: '请输入设备编号', trigger: 'blur' },
    { max: 64, message: '设备编号不能超过64字符', trigger: 'blur' }
  ],
  meterType: [{ required: true, message: '请选择表类型', trigger: 'change' }]
}

function openDialog(_parentId?: number, row?: WaterElecMeterVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id,
    stallId: row?.stallId,
    // 编辑回显：用设备 VO 带回的市场/分类先回填，再加载该范围铺位选项匹配选中
    formMarketId: row?.stallMarketId ?? undefined,
    formCategoryId: row?.stallCategoryId ?? undefined,
    meterNo: row?.meterNo ?? '',
    meterType: row?.meterType ?? 1,
    gatewayCode: row?.gatewayCode ?? '',
    currentRead: row?.currentRead ?? 0,
    balanceAmount: row?.balanceAmount ?? 0
  })
  void loadStallOptions()
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    submitLoading.value = true
    try {
      // 只提交业务字段，剥离联动中间值 formMarketId/formCategoryId；校验已保证 stallId 必填
      const { formMarketId: _m, formCategoryId: _c, ...rest } = form
      const submit: WaterElecMeterDTO = { ...rest, stallId: form.stallId! }
      if (form.id) {
        await updateMeterApi(submit)
        ElMessage.success('修改成功')
      } else {
        await addMeterApi(submit)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 抄表 ---------------- */
const readDialogVisible = ref(false)
const readLoading = ref(false)
const readFormRef = ref<FormInstance>()
const readForm = reactive<{ meterId: number; meterNo: string; oldRead?: number; currentRead?: number }>({
  meterId: 0,
  meterNo: '',
  oldRead: 0,
  currentRead: 0
})

const readRules: FormRules = {
  currentRead: [{ required: true, message: '请输入本次读数', trigger: 'blur' }]
}

function openReadDialog(row: WaterElecMeterVO): void {
  Object.assign(readForm, {
    meterId: row.id,
    meterNo: row.meterNo,
    oldRead: row.currentRead ?? 0,
    currentRead: row.currentRead ?? 0
  })
  readDialogVisible.value = true
}

async function handleRead(): Promise<void> {
  if (!readFormRef.value) {
    return
  }
  await readFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    readLoading.value = true
    try {
      await readMeterApi({ meterId: readForm.meterId, currentRead: readForm.currentRead! })
      ElMessage.success('抄表成功')
      readDialogVisible.value = false
      loadData()
    } finally {
      readLoading.value = false
    }
  })
}

/* ---------------- 合闸/断电（高危操作二次确认） ---------------- */
async function handleSwitch(row: WaterElecMeterVO): Promise<void> {
  const target = row.status === 1 ? 0 : 1
  const action = target === 0 ? '断电' : '合闸'
  await ElMessageBox.confirm(`确定对设备「${row.meterNo}」执行${action}操作吗？此操作将联动智能网关，请确认！`, '高危操作确认', {
    type: 'warning'
  })
  await switchMeterApi({ meterId: row.id, status: target })
  ElMessage.success(`${action}成功`)
  loadData()
}

/* ---------------- 删除 ---------------- */
async function handleDelete(row: WaterElecMeterVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除设备「${row.meterNo}」吗？`, '提示', { type: 'warning' })
  await deleteMeterApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>