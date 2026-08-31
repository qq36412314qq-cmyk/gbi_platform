<template>
  <div class="g-page-wrap tenant-list-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">租户管理</span>
      <div>
        <AuthBtn permission="tenant:add" type="primary" @click="openDialog()">新增租户</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="租户名称">
        <el-input v-model="query.tenantName" placeholder="输入租户名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="租户类型">
        <el-select v-model="query.tenantType" placeholder="全部" clearable style="width: 130px">
          <el-option label="个体工商户" :value="1" />
          <el-option label="企业" :value="2" />
          <el-option label="个人" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="query.contactPhone" placeholder="输入联系电话" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="tenantName" label="租户名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="租户类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.tenantType === 2 ? 'primary' : 'info'">{{ row.tenantTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" align="center" />
        <el-table-column prop="contactPhone" label="联系电话（脱敏）" width="140" align="center" />
        <el-table-column prop="socialCreditCode" label="统一社会信用代码" min-width="170" show-overflow-tooltip />
        <el-table-column prop="bankAccount" label="银行账号" width="130" align="center" />
        <el-table-column prop="address" label="联系地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="tenant:edit" size="small" type="primary" link @click="openDialog(undefined, row)">编辑</AuthBtn>
            <AuthBtn permission="tenant:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑租户' : '新增租户'"
      width="620px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="输入租户名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="租户类型" prop="tenantType">
          <el-radio-group v-model="form.tenantType">
            <el-radio :value="1">个体工商户</el-radio>
            <el-radio :value="2">企业</el-radio>
            <el-radio :value="3">个人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="输入联系人" maxlength="64" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="输入联系电话" maxlength="32" />
        </el-form-item>
        <el-form-item label="身份证号码" prop="idCardNo">
          <el-input v-model="form.idCardNo" placeholder="输入身份证号码（加密存储）" maxlength="64" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="socialCreditCode">
          <el-input v-model="form.socialCreditCode" placeholder="输入统一社会信用代码" maxlength="64" />
        </el-form-item>
        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="form.bankAccount" placeholder="输入银行账号（加密存储）" maxlength="64" />
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="输入联系地址" maxlength="500" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
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
 * 租户管理页：商户/租户入驻建档（敏感字段后端脱敏返回）
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getTenantPageApi,
  addTenantApi,
  updateTenantApi,
  deleteTenantApi,
  type TenantAddDTO,
  type TenantUpdateDTO,
  type TenantVO
} from '@/api/tenant'
import { useTable } from '@/hooks/useTable'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<TenantVO>(getTenantPageApi, {
  tenantName: undefined,
  tenantType: undefined,
  contactPhone: undefined,
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

const form = reactive<TenantUpdateDTO>({
  id: 0,
  tenantName: '',
  tenantType: 1,
  contactPerson: '',
  contactPhone: '',
  idCardNo: '',
  socialCreditCode: '',
  bankAccount: '',
  address: '',
  status: 1,
  remark: ''
})

const rules: FormRules = {
  tenantName: [
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { max: 128, message: '租户名称不能超过128字符', trigger: 'blur' }
  ],
  tenantType: [{ required: true, message: '请选择租户类型', trigger: 'change' }]
}

function openDialog(_parentId?: number, row?: TenantVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? 0,
    tenantName: row?.tenantName ?? '',
    tenantType: row?.tenantType ?? 1,
    contactPerson: row?.contactPerson ?? '',
    contactPhone: row?.contactPhone ?? '',
    idCardNo: row?.idCardNo ?? '',
    socialCreditCode: row?.socialCreditCode ?? '',
    bankAccount: row?.bankAccount ?? '',
    address: row?.address ?? '',
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
        await updateTenantApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addTenantApi({ ...form } as TenantAddDTO)
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
async function handleDelete(row: TenantVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除租户「${row.tenantName}」吗？存在生效合同的租户无法删除。`, '提示', {
    type: 'warning'
  })
  await deleteTenantApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>