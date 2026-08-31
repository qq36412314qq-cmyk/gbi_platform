<template>
  <div class="g-page-wrap fee-rule-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">收费规则管理</span>
      <div>
        <AuthBtn permission="fee:rule:add" type="primary" @click="openDialog()">新增收费规则</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="规则名称">
        <el-input v-model="query.ruleName" placeholder="输入规则名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="收费类型">
        <el-select v-model="query.feeItemId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in feeItemOptions" :key="item.id" :label="item.feeItemName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="feeItemName" label="收费类型" width="120" align="center" />
        <el-table-column label="收费方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.calcMode === 2 ? 'warning' : 'info'">{{ row.calcModeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="right">
          <template #default="{ row }">￥{{ row.price.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="periodTypeText" label="收费周期" width="100" align="center" />
        <el-table-column label="滞纳金" width="100" align="right">
          <template #default="{ row }">{{ Number(row.overdueRate ?? 0).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="fee:rule:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="fee:rule:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑收费规则' : '新增收费规则'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如：月度租金收费规则" maxlength="128" />
        </el-form-item>
        <el-form-item label="收费类型" prop="feeItemId">
          <el-select v-model="form.feeItemId" placeholder="选择收费类型" style="width: 100%">
            <el-option v-for="item in feeItemOptions" :key="item.id" :label="item.feeItemName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费方式" prop="calcMode">
          <el-radio-group v-model="form.calcMode">
            <el-radio :value="1">定额</el-radio>
            <el-radio :value="2">按面积</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
          <span class="b-fee-price-tip">{{ form.calcMode === 2 ? '元/平米' : (form.periodType === 0 ? '元/次' : '元/周期') }}</span>
        </el-form-item>
        <el-form-item label="收费周期" prop="periodType">
          <el-radio-group v-model="form.periodType">
            <el-radio :value="0">不使用周期</el-radio>
            <el-radio :value="1">按年</el-radio>
            <el-radio :value="2">按月</el-radio>
            <el-radio :value="3">按日</el-radio>
          </el-radio-group>
          <div class="g-tip">水费、电费（按用量计费）、押金（一次性）等类型可选择「不使用周期」</div>
        </el-form-item>
        <el-form-item label="滞纳金" prop="overdueRate">
          <el-input-number v-model="form.overdueRate" :min="0" :max="100" :precision="2" :step="0.5" style="width: 200px" />
          <span class="b-fee-price-tip">%（逾期加收比例，0=不收）</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明（可空）" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 收费规则管理页：调用收费类型（feeItemId），收费方式 1定额 2按面积，
 * 收费周期 0不使用 1按年 2按月 3按日（水费/电费/押金等类型可配置不使用周期），滞纳金百分比；子公司可配置，集团账号仅只读
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getFeeItemListApi,
  getFeeRulePageApi,
  addFeeRuleApi,
  updateFeeRuleApi,
  deleteFeeRuleApi,
  type FeeItemVO,
  type FeeRuleDTO,
  type FeeRuleVO
} from '@/api/fee'
import { useTable } from '@/hooks/useTable'

/* ---------------- 收费类型下拉（调用收费类型） ---------------- */
const feeItemOptions = ref<FeeItemVO[]>([])

async function loadFeeItemOptions(): Promise<void> {
  feeItemOptions.value = await getFeeItemListApi()
}

onMounted(() => {
  loadFeeItemOptions()
})

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FeeRuleVO>(getFeeRulePageApi, {
  ruleName: undefined,
  feeItemId: undefined,
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

const form = reactive<FeeRuleDTO>({
  id: 0,
  ruleName: '',
  feeItemId: undefined as unknown as number,
  calcMode: 1,
  price: 0,
  periodType: 2,
  overdueRate: 0,
  status: 1,
  remark: ''
})

const rules: FormRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { max: 128, message: '规则名称不能超过128字符', trigger: 'blur' }
  ],
  feeItemId: [{ required: true, message: '请选择收费类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}

function openDialog(_parentId?: number, row?: FeeRuleVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    ruleName: row?.ruleName ?? '',
    feeItemId: row?.feeItemId ?? undefined,
    calcMode: row?.calcMode ?? 1,
    price: row?.price ?? 0,
    periodType: row?.periodType ?? 2,
    overdueRate: row?.overdueRate ?? 0,
    status: row?.status ?? 1,
    remark: row?.remark ?? ''
  })
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
        await updateFeeRuleApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addFeeRuleApi({ ...form })
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
async function handleDelete(row: FeeRuleVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除收费规则「${row.ruleName}」吗？`, '提示', {
    type: 'warning'
  })
  await deleteFeeRuleApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.b-fee-price-tip {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>