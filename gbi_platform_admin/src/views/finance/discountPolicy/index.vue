<template>
  <div class="g-page-wrap discount-policy-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">优惠策略</span>
      <div>
        <AuthBtn permission="discount:policy:add" type="primary" @click="openAdd()">新增策略</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="策略名称">
        <el-input v-model="query.policyName" placeholder="策略名称模糊" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="优惠类型">
        <el-select v-model="query.discountType" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(text, value) in discountTypeMap" :key="value" :label="text" :value="Number(value)" />
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
        <el-table-column prop="policyName" label="策略名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="discountTypeText" label="优惠类型" width="100" align="center" />
        <el-table-column prop="waiveMonths" label="免租月数" width="90" align="center">
          <template #default="{ row }">{{ row.waiveMonths ?? 0 }} 个月</template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="90" align="center">
          <template #default="{ row }">{{ Number(row.discountRate ?? 100).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="deductAmount" label="减免金额" width="100" align="right">
          <template #default="{ row }">￥{{ formatMoney(row.deductAmount) }}</template>
        </el-table-column>
        <el-table-column label="适用范围" width="90" align="center">
          <template #default="{ row }">{{ row.scopeType === 2 ? '按铺位' : '按合同' }}</template>
        </el-table-column>
        <el-table-column label="生效时间" width="110">
          <template #default="{ row }">{{ row.startTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="失效时间" width="110">
          <template #default="{ row }">{{ row.endTime || '永久' }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="discount:policy:edit" size="small" type="primary" link @click="openEdit(row)">编辑</AuthBtn>
            <AuthBtn permission="discount:policy:delete" size="small" type="danger" link @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑弹窗 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑优惠策略' : '新增优惠策略'"
      width="600px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="策略名称" prop="policyName">
          <el-input v-model="form.policyName" placeholder="如：开业免租1个月" maxlength="128" />
        </el-form-item>
        <el-form-item label="优惠类型" prop="discountType">
          <el-select v-model="form.discountType" style="width: 100%">
            <el-option v-for="(text, value) in discountTypeMap" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.discountType === 1 || form.discountType === 4" label="免租月数" prop="waiveMonths">
          <el-input-number v-model="form.waiveMonths" :min="0" :max="36" style="width: 200px" />
          <span class="g-tip">租赁期前 N 个月不生成租金计划</span>
        </el-form-item>
        <el-form-item v-if="form.discountType === 2 || form.discountType === 4" label="折扣率" prop="discountRate">
          <el-input-number v-model="form.discountRate" :min="0" :max="100" :precision="2" style="width: 200px" />
          <span class="g-tip">%（100=无折扣，低于集团下限需审批）</span>
        </el-form-item>
        <el-form-item v-if="form.discountType === 3 || form.discountType === 4" label="减免金额" prop="deductAmount">
          <el-input-number v-model="form.deductAmount" :min="0" :max="99999999.99" :precision="2" style="width: 200px" />
          <span class="g-tip">按租赁期月数均摊冲减，超集团上限需审批</span>
        </el-form-item>
        <el-form-item label="适用范围" prop="scopeType">
          <el-radio-group v-model="form.scopeType">
            <el-radio :label="1">按合同</el-radio>
            <el-radio :label="2">按铺位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="form.startTime" type="date" value-format="YYYY-MM-DD" placeholder="不填立即生效" style="width: 200px" />
        </el-form-item>
        <el-form-item label="失效时间">
          <el-date-picker v-model="form.endTime" type="date" value-format="YYYY-MM-DD" placeholder="不填永久有效" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 优惠策略页：集团模板（company_id=0）+ 子公司自建；免租期/折扣/减免/组合四类，
 * 被优惠申请引用后禁止删除；变更写入审计日志
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getDiscountPolicyPageApi,
  addDiscountPolicyApi,
  updateDiscountPolicyApi,
  deleteDiscountPolicyApi,
  type DiscountPolicyVO,
  type DiscountPolicyDTO
} from '@/api/discount'
import { useTable } from '@/hooks/useTable'
import { formatMoney } from '@/utils/format'

const discountTypeMap: Record<number, string> = { 1: '免租期', 2: '折扣率', 3: '减免金额', 4: '组合' }

const { query, records, total, loading, loadData, resetQuery } = useTable<DiscountPolicyVO>(getDiscountPolicyPageApi, {
  policyName: undefined,
  discountType: undefined,
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

const form = reactive<DiscountPolicyDTO>({
  id: undefined,
  policyName: '',
  discountType: 1,
  waiveMonths: 0,
  discountRate: 100,
  deductAmount: 0,
  scopeType: 1,
  startTime: undefined,
  endTime: undefined,
  status: 1,
  remark: ''
})

const rules: FormRules = {
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  discountType: [{ required: true, message: '请选择优惠类型', trigger: 'change' }],
  waiveMonths: [{ required: true, message: '请输入免租月数', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  deductAmount: [{ required: true, message: '请输入减免金额', trigger: 'blur' }]
}

function openAdd(): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: undefined,
    policyName: '',
    discountType: 1,
    waiveMonths: 0,
    discountRate: 100,
    deductAmount: 0,
    scopeType: 1,
    startTime: undefined,
    endTime: undefined,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

function openEdit(row: DiscountPolicyVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row.id,
    policyName: row.policyName,
    discountType: row.discountType,
    waiveMonths: row.waiveMonths ?? 0,
    discountRate: row.discountRate ?? 100,
    deductAmount: row.deductAmount ?? 0,
    scopeType: row.scopeType ?? 1,
    startTime: row.startTime,
    endTime: row.endTime,
    status: row.status ?? 1,
    remark: row.remark
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
        await updateDiscountPolicyApi({ ...form })
        ElMessage.success('编辑成功')
      } else {
        await addDiscountPolicyApi({ ...form })
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
async function handleDelete(row: DiscountPolicyVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除优惠策略「${row.policyName}」吗？被优惠申请引用的策略禁止删除`, '删除确认', {
    type: 'warning'
  })
  await deleteDiscountPolicyApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>