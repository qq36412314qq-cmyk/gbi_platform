<template>
  <div class="g-page-wrap lease-category-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">租赁分类</span>
      <div>
        <AuthBtn permission="lease:category:add" type="primary" @click="openDialog()">新增分类</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="分类名称">
        <el-input v-model="query.categoryName" placeholder="输入分类名称" clearable style="width: 180px" />
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
        <el-table-column prop="categoryName" label="分类名称" min-width="180" />
        <el-table-column prop="sortOrder" label="排序号" width="100" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="lease:category:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="lease:category:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑分类' : '新增分类'"
      width="520px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="如：商铺/仓库/车位" maxlength="64" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :precision="0" style="width: 100%" />
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
 * 租赁分类页：商铺/仓库/车位等租赁标的分类，子公司可自定义
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getCategoryPageApi,
  addCategoryApi,
  updateCategoryApi,
  deleteCategoryApi,
  type CategoryDTO,
  type CategoryVO
} from '@/api/lease'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<CategoryVO>(getCategoryPageApi, {
  categoryName: undefined,
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

const form = reactive<CategoryDTO>({
  id: 0,
  categoryName: '',
  sortOrder: 0,
  status: 1,
  remark: ''
})

const rules: FormRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 64, message: '分类名称不能超过64字符', trigger: 'blur' }
  ]
}

function openDialog(_parentId?: number, row?: CategoryVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    categoryName: row?.categoryName ?? '',
    sortOrder: row?.sortOrder ?? 0,
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
        await updateCategoryApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addCategoryApi({ ...form })
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
async function handleDelete(row: CategoryVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？分类下存在摊位的无法删除。`, '提示', {
    type: 'warning'
  })
  await deleteCategoryApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>