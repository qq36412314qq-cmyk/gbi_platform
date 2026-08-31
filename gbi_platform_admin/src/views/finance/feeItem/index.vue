<template>
  <div class="g-page-wrap fee-item-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">收费类型管理</span>
      <div>
        <AuthBtn permission="fee:item:add" type="primary" @click="openDialog()">新增收费类型</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="类型名称">
        <el-input v-model="query.feeItemName" placeholder="输入收费类型名称" clearable style="width: 180px" />
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="feeItemName" label="收费类型" min-width="160" />
        <el-table-column label="收费类别" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="categoryTagType(row.categoryType)" disable-transitions>{{ row.categoryTypeText ?? '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="calcUnit" label="计量单位" width="120" align="center" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="fee:item:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="fee:item:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑收费类型' : '新增收费类型'"
      width="520px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="收费类型" prop="feeItemName">
          <el-input v-model="form.feeItemName" placeholder="如：租金/物业费/水费/电费/押金/其他" maxlength="128" />
        </el-form-item>
        <el-form-item label="收费类别" prop="categoryType">
          <el-select v-model="form.categoryType" placeholder="选择收费类别" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
          <div class="g-tip">类别编码稳定用于账单计费匹配（水费/电费/物业费按类别取摊位绑定规则单价），名称可自定义</div>
        </el-form-item>
        <el-form-item label="计量单位" prop="calcUnit">
          <el-input v-model="form.calcUnit" placeholder="如：元/月、元/平米、元/吨" maxlength="32" />
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
 * 收费类型管理页：租金/物业费/水费/电费/押金/其他等收费类型，子公司可配置
 * 集团账号仅 fee:item:list 只读，无新增/编辑/删除入口（AuthBtn 自动隐藏）
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getFeeItemPageApi,
  addFeeItemApi,
  updateFeeItemApi,
  deleteFeeItemApi,
  type FeeItemDTO,
  type FeeItemVO
} from '@/api/fee'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<FeeItemVO>(getFeeItemPageApi, {
  feeItemName: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<FeeItemDTO>({
  id: 0,
  feeItemName: '',
  categoryType: 1,
  calcUnit: '',
  remark: ''
})

/** 收费类别选项（与后端 CommonConst.FEE_CATEGORY_* 对齐） */
const categoryOptions = [
  { value: 1, label: '租金' },
  { value: 2, label: '物业费' },
  { value: 3, label: '水费' },
  { value: 4, label: '电费' },
  { value: 5, label: '押金' },
  { value: 6, label: '其他' }
]

function categoryTagType(categoryType?: number): string {
  switch (categoryType) {
    case 2:
      return 'warning'
    case 3:
      return 'success'
    case 4:
      return 'danger'
    case 1:
      return 'primary'
    default:
      return 'info'
  }
}

const rules: FormRules = {
  feeItemName: [
    { required: true, message: '请输入收费类型名称', trigger: 'blur' },
    { max: 128, message: '收费类型名称不能超过128字符', trigger: 'blur' }
  ],
  categoryType: [{ required: true, message: '请选择收费类别', trigger: 'change' }],
  calcUnit: [{ max: 32, message: '计量单位不能超过32字符', trigger: 'blur' }]
}

function openDialog(_parentId?: number, row?: FeeItemVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    feeItemName: row?.feeItemName ?? '',
    categoryType: row?.categoryType ?? 1,
    calcUnit: row?.calcUnit ?? '',
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
        await updateFeeItemApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addFeeItemApi({ ...form })
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
async function handleDelete(row: FeeItemVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除收费类型「${row.feeItemName}」吗？被收费规则引用的类型无法删除。`, '提示', {
    type: 'warning'
  })
  await deleteFeeItemApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>