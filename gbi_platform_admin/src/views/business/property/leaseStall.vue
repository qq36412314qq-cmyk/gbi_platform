<template>
  <div class="g-page-wrap lease-stall-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">铺位管理</span>
        <router-link to="/property/lease/canvas" class="canvas-link" title="铺位布局画布">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          铺位画布
        </router-link>
        <div>
          <AuthBtn permission="lease:stall:add" type="primary" @click="openDialog()">新增铺位</AuthBtn>
        </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="铺位编号">
        <el-input v-model="query.stallNumber" placeholder="输入铺位编号" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="租赁分类">
        <el-select v-model="query.stallCategoryId" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属市场">
        <el-select v-model="query.marketId" placeholder="全部" clearable style="width: 150px">
          <el-option v-for="m in marketOptions" :key="m.id" :label="m.marketName" :value="m.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="铺位状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="空置" :value="0" />
          <el-option label="已租" :value="1" />
          <el-option label="欠费" :value="2" />
          <el-option label="即将到期" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="stallNumber" label="铺位编号" min-width="120" />
        <el-table-column prop="marketName" label="所属市场" width="130" align="center" />
        <el-table-column prop="categoryName" label="租赁分类" width="100" align="center" />
        <el-table-column prop="stallName" label="铺位名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="stallArea" label="面积(㎡)" width="100" align="right" />
        <el-table-column label="收费规则" min-width="200">
          <template #default="{ row }">
            <template v-if="row.feeRules && row.feeRules.length > 0">
              <el-tag
                v-for="rule in row.feeRules"
                :key="rule.ruleId"
                size="small"
                type="info"
                effect="plain"
                class="b-stall-rule-tag"
              >
                {{ rule.ruleName }}<span v-if="rule.price !== undefined">（{{ rule.price }}元/{{ rule.calcMode === 2 ? '平米' : rule.periodTypeText === '不使用周期' ? '次' : (rule.periodTypeText ?? '周期') }}）</span>
              </el-tag>
            </template>
            <span v-else class="b-stall-rule-empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="stallStatusTag(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="lease:stall:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="lease:stall:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑铺位' : '新增铺位'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属市场" prop="marketId">
          <el-select v-model="form.marketId" placeholder="选择市场" style="width: 100%">
            <el-option v-for="m in marketOptions" :key="m.id" :label="m.marketName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="租赁分类" prop="stallCategoryId">
          <el-select v-model="form.stallCategoryId" placeholder="选择租赁分类" clearable style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="铺位编号" prop="stallNumber">
          <el-input v-model="form.stallNumber" placeholder="输入铺位编号（同公司唯一）" maxlength="64" />
        </el-form-item>
        <el-form-item label="铺位名称" prop="stallName">
          <el-input v-model="form.stallName" placeholder="输入铺位名称（可空）" maxlength="128" />
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="stallArea">
          <el-input-number v-model="form.stallArea" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.id" label="铺位状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="空置" :value="0" />
            <el-option label="已租" :value="1" />
            <el-option label="欠费" :value="2" />
            <el-option label="即将到期" :value="3" />
          </el-select>
        </el-form-item>
<el-form-item label="备注" prop="remark">
    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明，可空" maxlength="500" />
  </el-form-item>
  <el-form-item label="收费规则" prop="ruleIds">
    <el-select
      v-model="form.ruleIds"
      multiple
      collapse-tags
      clearable
      placeholder="选择收费规则（同一收费类型限选一条）"
      style="width: 100%"
    >
      <el-option-group v-for="group in groupedRuleOptions" :key="group.feeItemId" :label="group.feeItemName">
        <el-option
          v-for="opt in group.options"
          :key="opt.id"
          :label="`${opt.ruleName}（${opt.price ?? 0}元/${opt.calcMode === 2 ? '平米' : periodText(opt.periodType)}${opt.overdueRate ? '，滞纳金' + opt.overdueRate + '%' : ''}）`"
          :value="opt.id"
          :disabled="isRuleOptionDisabled(opt)"
        />
      </el-option-group>
    </el-select>
    <div class="g-tip">同一收费类型（租金/物业费/水费/电费等）只能选择一条收费规则</div>
  </el-form-item>
</el-form>
</CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 租赁铺位页：商铺/仓库/车位等租赁标的，分类下拉取自租赁分类
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed } from 'vue'
import {
  getStallPageApi,
  addStallApi,
  updateStallApi,
  deleteStallApi,
  getCategoryListApi,
  getStallRuleOptionsApi,
  getStallRuleRelListApi,
  type StallAddDTO,
  type StallUpdateDTO,
  type StallVO,
  type CategoryVO,
  type FeeRuleOption
} from '@/api/lease'
import { getMarketListApi, type MarketVO } from '@/api/market'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分类下拉（启用优先） ---------------- */
const categoryOptions = ref<CategoryVO[]>([])
/* ---------------- 市场下拉（启用优先） ---------------- */
const marketOptions = ref<MarketVO[]>([])
onMounted(async () => {
  try {
    categoryOptions.value = await getCategoryListApi()
  } catch {
    categoryOptions.value = []
  }
  try {
    marketOptions.value = await getMarketListApi()
  } catch {
    marketOptions.value = []
  }
})

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<StallVO>(getStallPageApi, {
  stallNumber: undefined,
  stallCategoryId: undefined,
  marketId: undefined,
  status: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

function stallStatusTag(status: number): 'success' | 'primary' | 'danger' | 'warning' {
  switch (status) {
    case 0:
      return 'success'
    case 1:
      return 'primary'
    case 2:
      return 'danger'
    default:
      return 'warning'
  }
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<StallUpdateDTO>({
  id: 0,
  marketId: undefined as unknown as number,
  stallCategoryId: undefined,
  stallNumber: '',
  stallName: '',
  stallArea: 0,
  status: 0,
  remark: '',
  ruleIds: []
})

/* ---------------- 收费规则选项（同收费类型限选一条） ---------------- */
const ruleOptions = ref<FeeRuleOption[]>([])

/** 按收费类型分组（下拉分组展示） */
const groupedRuleOptions = computed(() => {
  const map = new Map<number, { feeItemId: number; feeItemName: string; options: FeeRuleOption[] }>()
  for (const opt of ruleOptions.value) {
    if (!map.has(opt.feeItemId)) {
      map.set(opt.feeItemId, { feeItemId: opt.feeItemId, feeItemName: opt.feeItemName ?? '未分类', options: [] })
    }
    map.get(opt.feeItemId)!.options.push(opt)
  }
  return Array.from(map.values())
})

/** 同一收费类型已选其他规则 → 禁用（核心互斥约束） */
function isRuleOptionDisabled(opt: FeeRuleOption): boolean {
  const selected = form.ruleIds ?? []
  if (selected.length === 0 || selected.includes(opt.id)) {
    return false
  }
  return ruleOptions.value.some((o) => selected.includes(o.id) && o.feeItemId === opt.feeItemId)
}

/** 周期文本（用于选项标题展示；0=不使用周期） */
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

const rules: FormRules = {
  marketId: [{ required: true, message: '请选择市场', trigger: 'change' }],
  stallNumber: [
    { required: true, message: '请输入铺位编号', trigger: 'blur' },
    { max: 64, message: '铺位编号不能超过64字符', trigger: 'blur' }
  ]
}

async function openDialog(_parentId?: number, row?: StallVO): Promise<void> {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    marketId: row?.marketId ?? undefined,
    stallCategoryId: row?.stallCategoryId ?? undefined,
    stallNumber: row?.stallNumber ?? '',
    stallName: row?.stallName ?? '',
    stallArea: row?.stallArea ?? 0,
    status: row?.status ?? 0,
    remark: row?.remark ?? '',
    ruleIds: []
  })
  // 每次打开刷新规则选项（保证新增/启停规则即时生效）
  try {
    ruleOptions.value = await getStallRuleOptionsApi()
  } catch {
    ruleOptions.value = []
  }
  // 编辑回显已绑定收费规则
  if (row?.id) {
    try {
      const rels = await getStallRuleRelListApi(row.id)
      form.ruleIds = rels.map((r) => r.ruleId)
    } catch {
      form.ruleIds = []
    }
  }
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
      if (form.id) {
        await updateStallApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addStallApi({ ...form } as StallAddDTO)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 删除 ---------------- */
async function handleDelete(row: StallVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除铺位「${row.stallNumber}」吗？存在租赁合同的铺位无法删除。`, '提示', {
    type: 'warning'
  })
  await deleteStallApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.b-stall-rule-tag {
  margin: 2px 6px 2px 0;
}
.b-stall-rule-empty {
  color: var(--el-text-color-placeholder);
}

/* 铺位画布快捷入口 */
.canvas-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  margin-right: 8px;
  background: #0d1f35;
  border: 1px solid #2d6a9f;
  color: #7fa8cc;
  border-radius: 4px;
  font-size: 12px;
  text-decoration: none;
  transition: all 0.15s;
  font-family: monospace;
}
.canvas-link:hover {
  background: #1a3a5f;
  color: #e2e8f0;
  border-color: #4a90d9;
}</style>