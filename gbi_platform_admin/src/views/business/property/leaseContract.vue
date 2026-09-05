<!--
  租赁合同管理页面
  数据表：stall_contract
  关联表：stall_info（铺位）、stall_tenant（租户）、stall_category（分类）、market_info（市场）
  权限：lease:contract:*
-->
<template>
  <div class="g-page-wrap lease-contract-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">合同管理</span>
      <div>
        <AuthBtn permission="lease:contract:add" type="primary" @click="openDialog()">新增合同</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="合同编号">
        <el-input v-model="query.contractNo" placeholder="输入合同编号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="合同状态">
        <el-select v-model="query.contractStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="签约中" :value="0" />
          <el-option label="签约中" :value="0" />`r`n          <el-option label="生效中" :value="1" />
          <el-option label="已退租" :value="2" />
          <el-option label="已到期" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="sortedRecords" border stripe>
        <el-table-column prop="contractNo" label="合同编号" min-width="180" />
        <el-table-column prop="tenantName" label="租户" min-width="130" show-overflow-tooltip />
        <el-table-column prop="marketName" label="所属市场" width="120" align="center" />
        <el-table-column prop="categoryName" label="租赁分类" width="100" align="center" />
        <el-table-column prop="stallNumber" label="铺位编号" width="110" align="center" />
        <el-table-column label="月租金(元)" width="110" align="right">
          <template #default="{ row }"><span class="rent-value">{{ getRentDisplay(row).value }}</span><span v-if="getRentDisplay(row).tag" class="rent-tag rent-tag--{{ getRentDisplay(row).tagClass }}">{{ getRentDisplay(row).tag }}</span></template>
        </el-table-column>
        <el-table-column prop="depositAmount" label="押金(元)" width="100" align="right" />
        <el-table-column prop="startTime" label="开始日期" width="110" align="center" />
        <el-table-column width="110" align="center">
          <template #header>
            <span class="g-sort-header" @click="handleEndTimeSort">
              到期日期
              <el-icon class="sort-arrow asc-arrow" :class="{ active: endTimeSort === 'asc' }"><ArrowUp /></el-icon>
              <el-icon class="sort-arrow desc-arrow" :class="{ active: endTimeSort === 'desc' }"><ArrowDown /></el-icon>
            </span>
          </template>
          <template #default="{ row }"> {{ row.endTime }} </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="contractStatusTag(row.contractStatus)">{{ row.contractStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn
              v-if="row.contractStatus === 1"
              permission="lease:contract:terminate"
              size="small"
              type="danger"
              link
              @click="handleTerminate(row)"
            >
              退租
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增合同弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      title="新增租赁合同"
      width="620px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="租户" prop="tenantId">
          <el-select
            v-model="form.tenantId"
            filterable
            placeholder="选择租户（可搜索）"
            style="width: 100%"
            :loading="tenantLoading"
            @visible-change="loadTenants"
          >
            <el-option v-for="t in tenantOptions" :key="t.id" :label="t.tenantName" :value="t.id" />
          </el-select>
        </el-form-item>
        <!-- 铺位三级联动：市场 → 租赁分类 → 空置铺位（数据源 market_info / stall_category / stall_info，仅空置 status=0） -->
        <el-form-item label="市场" prop="formMarketId">
          <el-select v-model="form.formMarketId" placeholder="选择市场" clearable style="width: 100%" @change="handleMarketChange">
            <el-option v-for="m in marketOptions" :key="m.id" :label="m.marketName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="租赁分类" prop="formCategoryId">
          <el-select v-model="form.formCategoryId" placeholder="选择租赁分类" clearable style="width: 100%" @change="handleCategoryChange">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="铺位" prop="stallId">
          <el-select
            v-model="form.stallId"
            filterable
            clearable
            placeholder="请先选择市场/分类"
            style="width: 100%"
            :disabled="!form.formMarketId && !form.formCategoryId"
            @change="handleStallChange"
          >
            <el-option
              v-for="s in stallOptions"
              :key="s.id"
              :label="`${s.stallNumber}（${s.categoryName ?? '未分类'}）`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="rentLabel" prop="rentAmount">
          <el-input-number v-model="form.rentAmount" :min="0" :precision="2" :disabled="!rentEditable" style="width: 100%" />
          <div v-if="rentRuleText" class="g-tip">已按铺位收费规则自动带出：{{ rentRuleText }}
            <span v-if="!rentEditable" class="g-tip">（仅展示，禁止手动修改）</span>
            <span v-else class="g-tip">可手动修改</span>
          </div>
          <div v-else class="g-tip">该铺位未绑定租金收费规则（定额/按面积），请手动填写</div>
        </el-form-item>
        <el-form-item :label="depositLabel" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :disabled="!rentEditable" style="width: 100%" />
          <div v-if="depositRuleText" class="g-tip">已按铺位收费规则自动带出：{{ depositRuleText }}
            <span v-if="!rentEditable" class="g-tip">（仅展示，禁止手动修改）</span>
            <span v-else class="g-tip">可手动修改</span>
          </div>
          <div v-else class="g-tip">该铺位未绑定押金收费规则（定额/按面积），请手动填写</div>
        </el-form-item>
        <div style="display: flex; justify-content: center; align-items: center; gap: 8px; margin-bottom: 8px;">
          <el-button size="small" @click="handleAddMonth">加1月</el-button>
          <el-button size="small" @click="handleAddYear">加1年</el-button>
          <el-button size="small" @click="handleResetDate">重置</el-button>
        </div>
        <el-form-item label="开始日期" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择到期日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明（可空）" maxlength="500" />
        </el-form-item>
        <!-- 优惠区块：随合同提交自动生成优惠申请，超集团阈值自动发起 contract_discount 审批，审批通过后合同优惠生效 -->
        <el-divider content-position="left">优惠（可选）</el-divider>
        <el-form-item label="优惠策略">
          <div class="g-tip">未选择策略：合同直接生成收款计划；超集团阈值（免租>3月 / 折扣<80% / 减免>5000元 / 优惠占合同总租金>10%）将自动发起审批，通过后优惠生效</div>
          <div v-if="!discountEditable" class="g-tip">优惠参数已由策略自动带出，禁止手动修改</div>
          <el-select
            v-model="form.policyId"
            filterable
            clearable
            placeholder="选择优惠策略（集团模板+本公司自建）"
            style="width: 100%"
            :loading="policyLoading"
            @change="handlePolicyChange"
          >
            <el-option v-for="p in policyOptions" :key="p.id" :label="policyLabel(p)" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="免租期(月)">
          <el-input-number v-model="form.waiveMonths" :min="0" :max="12" :precision="0" placeholder="0 表示不免租" :disabled="!discountEditable" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣率(%)">
          <el-input-number v-model="form.discountRate" :min="0" :max="100" :precision="2" placeholder="100 表示无折扣" :disabled="!discountEditable" style="width: 100%" />
        </el-form-item>
        <el-form-item label="减免金额(元)">
          <el-input-number v-model="form.deductAmount" :min="0" :precision="2" placeholder="0 表示无减免" :disabled="!discountEditable" style="width: 100%" />
        </el-form-item>
        <el-form-item label="优惠备注">
          <el-input v-model="form.discountRemark" type="textarea" :rows="2" placeholder="优惠事由说明（可空）" maxlength="200" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 租赁合同页：租户+铺位签订合同，生效后铺位自动置为已租、押金写入财务流水；退租为高危操作二次确认
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import {
  getContractPageApi,
  addContractApi,
  terminateContractApi,
  getCategoryListApi,
  getStallOptionsApi,
  getStallRuleRelListApi,
  type ContractAddDTO,
  type ContractVO,
  type CategoryVO,
  type StallOptionVO,
  type StallRuleRel
} from '@/api/lease'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { getTenantPageApi, type TenantVO } from '@/api/tenant'
import { getDiscountPolicyPageApi, type DiscountPolicyVO } from '@/api/discount'
import { getConfigValuesApi } from '@/api/sys'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<ContractVO>(getContractPageApi, {
  contractNo: undefined,
  contractStatus: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

function contractStatusTag(status: number): 'success' | 'danger' | 'info' {
  switch (status) {
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
}

/* ---------------- 到期日期排序 ---------------- */
const endTimeSort = ref<"asc" | "desc" | "">("")

const sortedRecords = computed(() => {
  if (!endTimeSort.value) return records.value
  const sorted = [...records.value].sort((a, b) => {
    const va = a.endTime ?? ""
    const vb = b.endTime ?? ""
    return endTimeSort.value === "asc" ? va.localeCompare(vb) : vb.localeCompare(va)
  })
  return sorted
})

function handleEndTimeSort(): void {
  if (endTimeSort.value === "") {
    endTimeSort.value = "asc"
  } else if (endTimeSort.value === "asc") {
    endTimeSort.value = "desc"
  } else {
    endTimeSort.value = ""
  }
}

/* ---------------- 租户下拉 ---------------- */
const tenantLoading = ref(false)
const tenantOptions = ref<TenantVO[]>([])
let tenantLoaded = false
async function loadTenants(visible: boolean): Promise<void> {
  if (!visible || tenantLoaded) {
    return
  }
  tenantLoading.value = true
  try {
    const res = await getTenantPageApi({ pageNum: 1, pageSize: 100, status: 1 })
    tenantOptions.value = res.records
    tenantLoaded = true
  } finally {
    tenantLoading.value = false
  }
}

/* ---------------- 铺位三级联动（市场/分类公司级数据打开加载一次；铺位按条件动态加载且仅空置） ---------------- */
const marketOptions = ref<MarketVO[]>([])
const categoryOptions = ref<CategoryVO[]>([])
const stallOptions = ref<StallOptionVO[]>([])

/* ---------------- 集团配置开关（租金/押金、优惠参数可写性） ---------------- */
const rentEditable = ref(true)
const discountEditable = ref(true)

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
  // 读取集团配置参数（租金押金/优惠参数可写开关）
  try {
    const cfg = await getConfigValuesApi(['contract.rent_editable', 'contract.discount_editable'])
    rentEditable.value = cfg['contract.rent_editable'] !== '0'
    discountEditable.value = cfg['contract.discount_editable'] !== '0'
  } catch {
    rentEditable.value = true
    discountEditable.value = true
  }
  try {
    policyLoading.value = true
    const res = await getDiscountPolicyPageApi({ pageNum: 1, pageSize: 100, status: 1 })
    policyOptions.value = res.records
  } catch {
    policyOptions.value = []
  } finally {
    policyLoading.value = false
  }
})

/* 市场/分类变化 → 清空已选铺位并重新加载空置铺位选项（status=0，后端过滤） ---------------- */
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
      stallCategoryId: form.formCategoryId,
      status: 0
    })
  } catch {
    stallOptions.value = []
  }
}

/* ---------------- 租金/押金按铺位收费规则自动带出（收费类别：1租金 5押金；定额=单价，按面积=单价×面积） ---------------- */
const rentRuleText = ref('')
const depositRuleText = ref('')

/* 周期类型跟随铺位绑定收费规则 period_type（0不使用周期 1按年 2按月 3按日；undefined=未绑定规则走默认） ---------------- */
const rentPeriodType = ref<number | undefined>(undefined)
const depositPeriodType = ref<number | undefined>(undefined)

/** 租金表单标签单位（随收费规则周期动态显示，默认按月） ---------------- */
const rentLabel = computed(() => {
  if (rentPeriodType.value === 0) {
    return '租金(元)'
  }
  if (rentPeriodType.value === 1) {
    return '年租金(元)'
  }
  if (rentPeriodType.value === 3) {
    return '日租金(元)'
  }
  return '月租金(元)'
})

/** 押金表单标签单位（押金通常不使用周期，默认无周期前缀） ---------------- */
const depositLabel = computed(() => {
  if (depositPeriodType.value === 1) {
    return '年押金(元)'
  }
  if (depositPeriodType.value === 3) {
    return '日押金(元)'
  }
  return '押金(元)'
})

/** 收费规则金额计算：定额取单价，按面积取 单价×铺位面积（两位小数） ---------------- */
function calcRuleAmount(rel: StallRuleRel, area?: number): number {
  const price = rel.price ?? 0
  if (rel.calcMode === 2) {
    return Math.round(price * (area ?? 0) * 100) / 100
  }
  return Math.round(price * 100) / 100
}

/** 选中铺位 → 读取该铺位已绑定收费规则，按类别自动带出租金/押金（无对应规则保留手动填写） ---------------- */
async function handleStallChange(): Promise<void> {
  rentRuleText.value = ''
  depositRuleText.value = ''
  rentPeriodType.value = undefined
  depositPeriodType.value = undefined
  if (!form.stallId) {
    return
  }
  const stall = stallOptions.value.find((s) => s.id === form.stallId)
  try {
    const rels = await getStallRuleRelListApi(form.stallId)
    const rentRel = rels.find((r) => r.categoryType === 1)
    const depositRel = rels.find((r) => r.categoryType === 5)
    if (rentRel) {
      form.rentAmount = calcRuleAmount(rentRel, stall?.stallArea)
      rentPeriodType.value = rentRel.periodType
      rentRuleText.value = `${rentRel.ruleName}（${rentRel.calcModeText ?? '定额'} ${rentRel.price ?? 0}元${rentRel.calcMode === 2 ? '/平米' : periodText(rentRel.periodType)}）`
    }
    if (depositRel) {
      form.depositAmount = calcRuleAmount(depositRel, stall?.stallArea)
      depositPeriodType.value = depositRel.periodType
      depositRuleText.value = `${depositRel.ruleName}（${depositRel.calcModeText ?? '定额'} ${depositRel.price ?? 0}元${depositRel.calcMode === 2 ? '/平米' : periodText(depositRel.periodType)}）`
    }
  } catch {
    // 规则读取失败不阻塞签约，保留手动填写
  }
}

/** 周期文本（用于带出提示；0=不使用周期） ---------------- */
function periodText(periodType?: number): string {
  if (periodType === 0) {
    return '次'
  }
  if (periodType === 1) {
    return '年'
  }
  if (periodType === 3) {
    return '日'
  }
  return '月'
}

/** 租金列展示：返回换算后数值 + 周期标签（年/日/No） ---------------- */
function getRentDisplay(row: { rentAmount?: number; rentPeriodType?: number }): { value: string; tag: string; tagClass: string } {
  if (row.rentAmount == null) return { value: '-', tag: '', tagClass: 'month' }
  const num = Number(row.rentAmount)
  switch (row.rentPeriodType) {
    case 1: return { value: (num / 12).toFixed(2), tag: '年', tagClass: 'year' }
    case 3: return { value: (num * 30).toFixed(2), tag: '日', tagClass: 'day' }
    default: return { value: num.toFixed(2), tag: 'No', tagClass: 'month' }
  }
}


/* ---------------- 优惠策略（可选）：随合同提交自动生成优惠申请，超阈值自动发起审批 ---------------- */
const policyLoading = ref(false)
const policyOptions = ref<DiscountPolicyVO[]>([])

/** 策略下拉文案：名称 + 类型 + 主要参数 ---------------- */
function policyLabel(p: DiscountPolicyVO): string {
  const parts: string[] = [p.policyName]
  if (p.discountTypeText) {
    parts.push(p.discountTypeText)
  }
  if (p.waiveMonths != null && p.waiveMonths > 0) {
    parts.push('免租' + p.waiveMonths + '月')
  }
  if (p.discountRate != null && p.discountRate < 100) {
    parts.push(p.discountRate + '%折')
  }
  if (p.deductAmount != null && p.deductAmount > 0) {
    parts.push('减' + p.deductAmount + '元')
  }
  return parts.join('（') + (parts.length > 1 ? '）' : '')
}

/** 选中策略 → 按策略配置回填免租期/折扣率/减免金额（可再手动修改） ---------------- */
function handlePolicyChange(): void {
  const p = policyOptions.value.find((x) => x.id === form.policyId)
  if (!p) {
    form.waiveMonths = undefined
    form.discountRate = undefined
    form.deductAmount = undefined
    return
  }
  form.waiveMonths = p.waiveMonths ?? 0
  form.discountRate = p.discountRate ?? 100
  form.deductAmount = p.deductAmount ?? 0
}

/* ---------------- 新增合同 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<
  Omit<ContractAddDTO, 'stallId'> & { stallId?: number; formMarketId?: number; formCategoryId?: number }
>({
  tenantId: undefined as unknown as number,
  stallId: undefined as unknown as number,
  formMarketId: undefined,
  formCategoryId: undefined,
  rentAmount: 0,
  depositAmount: 0,
  startTime: '',
  endTime: '',
  remark: '',
  policyId: undefined,
  waiveMonths: undefined,
  discountRate: undefined,
  deductAmount: undefined,
  discountRemark: ''
})

const rules: FormRules = computed(() => ({
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  stallId: [{ required: true, message: '请选择空置铺位', trigger: 'change' }],
  rentAmount: [{ required: rentEditable.value, message: '请输入月租金（可先选择铺位自动带出）', trigger: 'change' }],
  depositAmount: [{ required: rentEditable.value, message: '请输入押金（可先选择铺位自动带出）', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}))

function openDialog(): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    tenantId: undefined,
    stallId: undefined,
    formMarketId: undefined,
    formCategoryId: undefined,
    rentAmount: 0,
    depositAmount: 0,
    startTime: formatToday(), // 默认当天日期
    endTime: '',
    remark: ''
  })
  stallOptions.value = []
  rentRuleText.value = ''
  depositRuleText.value = ''
  rentPeriodType.value = undefined
  depositPeriodType.value = undefined
  if (!discountEditable.value) {
    form.waiveMonths = undefined
    form.discountRate = undefined
    form.deductAmount = undefined
    form.policyId = undefined
  }
  dialogVisible.value = true
}

/** 获取当天日期字符串 YYYY-MM-DD ---------------- */
function formatToday(): string {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 加1月：在到期日期基础上加1个月填入到期日期，为空则以开始日期计算 ---------------- */
function handleAddMonth(): void {
  const base = form.endTime || form.startTime
  if (!base) { return }
  const d = new Date(base)
  d.setMonth(d.getMonth() + 1)
  form.endTime = formatFrom(d)
}

/** 加1年：在到期日期基础上加1年填入到期日期，为空则以开始日期计算 ---------------- */
function handleAddYear(): void {
  const base = form.endTime || form.startTime
  if (!base) { return }
  const d = new Date(base)
  d.setFullYear(d.getFullYear() + 1)
  form.endTime = formatFrom(d)
}

/** 重置：开始日期填入当天，到期日期清空 ---------------- */
function handleResetDate(): void {
  form.startTime = formatToday()
  form.endTime = ''
}

/** 日期对象转 YYYY-MM-DD ---------------- */
function formatFrom(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
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
      // 剥离联动中间字段（formMarketId/formCategoryId），只提交业务字段
      const { formMarketId: _m, formCategoryId: _c, ...rest } = form
      await addContractApi(rest as ContractAddDTO)
      ElMessage.success('合同签订成功，铺位已置为已租')
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 退租（高危操作二次确认） ---------------- */
async function handleTerminate(row: ContractVO): Promise<void> {
  await ElMessageBox.confirm(
    `确定对合同「${row.contractNo}」（租户：${row.tenantName}）执行退租吗？退租后铺位置为空置，押金将按原金额生成退费支出流水，该操作不可撤销！`,
    '高危操作确认',
    { type: 'warning', confirmButtonText: '确认退租' }
  )
  await terminateContractApi({ contractId: row.id })
  ElMessage.success('退租成功')
  loadData()
}
</script>
<style scoped>
.g-sort-header {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  user-select: none;
}
.sort-arrow {
  font-size: 12px;
  color: #c0c4cc;
  transition: color 0.2s;
}
.sort-arrow.active {
  color: #409eff;
  font-weight: 700;
}

/* 租金列周期标签 */
.rent-value { font-weight: 600; }
.rent-tag { margin-left: 4px; font-size: 10px; padding: 0 4px; border-radius: 3px; line-height: 16px; vertical-align: middle; }
.rent-tag--year { color: #e6a23c; background: #fdf6ec; }
.rent-tag--day  { color: #f56c6c; background: #fef0f0; }
.rent-tag--month { color: #909399; background: #f4f4f5; }
</style>



