<template>
  <div class="g-page-wrap market-list-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">市场管理</span>
      <div>
        <AuthBtn permission="market:add" type="primary" @click="openDialog()">新增市场</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="市场名称">
        <el-input v-model="query.marketName" placeholder="输入市场名称" clearable style="width: 180px" />
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
        <el-table-column prop="marketName" label="市场名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="marketAddress" label="市场地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="110" align="center" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" align="center" />
        <el-table-column label="归属" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团模板' : '本公司' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="market:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="market:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑市场' : '新增市场'"
      width="580px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="市场名称" prop="marketName">
          <el-input v-model="form.marketName" placeholder="如：汽车城A区市场" maxlength="128" />
        </el-form-item>
        <el-form-item label="市场地址" prop="marketAddress">
          <el-input v-model="form.marketAddress" placeholder="输入市场地址" maxlength="500" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="输入市场联系人" maxlength="64" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="输入联系电话" maxlength="32" />
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
 * 市场管理页：园区/商圈维度维护，摊位与市场地图统一关联
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getMarketPageApi,
  addMarketApi,
  updateMarketApi,
  deleteMarketApi,
  type MarketDTO,
  type MarketVO
} from '@/api/market'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<MarketVO>(getMarketPageApi, {
  marketName: undefined,
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

const form = reactive<MarketDTO>({
  id: 0,
  marketName: '',
  marketAddress: '',
  contactPerson: '',
  contactPhone: '',
  status: 1,
  remark: ''
})

const rules: FormRules = {
  marketName: [
    { required: true, message: '请输入市场名称', trigger: 'blur' },
    { max: 128, message: '市场名称不能超过128字符', trigger: 'blur' }
  ]
}

function openDialog(_parentId?: number, row?: MarketVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    marketName: row?.marketName ?? '',
    marketAddress: row?.marketAddress ?? '',
    contactPerson: row?.contactPerson ?? '',
    contactPhone: row?.contactPhone ?? '',
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
        await updateMarketApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addMarketApi({ ...form })
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
async function handleDelete(row: MarketVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除市场「${row.marketName}」吗？市场下存在摊位的无法删除。`, '提示', {
    type: 'warning'
  })
  await deleteMarketApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>