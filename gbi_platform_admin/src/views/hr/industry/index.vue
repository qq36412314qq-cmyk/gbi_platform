<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">行业字典</span>
      <AuthBtn permission="hr:industry:add" type="primary" @click="openAdd">新增行业</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="行业名称">
        <el-input v-model="query.industryName" placeholder="请输入" clearable style="width:150px" />
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="industryCode" label="行业编码" width="120" align="center" />
        <el-table-column prop="industryName" label="行业名称" width="200" align="center" />
        <el-table-column prop="workInjuryRateBase" label="工伤基准费率(%)" width="140" align="right">
          <template #default="{ row }">{{ row.workInjuryRateBase ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:industry:edit" link type="primary" size="small" @click="openEdit(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:industry:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑行业' : '新增行业'" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="行业编码" prop="industryCode">
          <el-input v-model="form.industryCode" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="行业名称" prop="industryName">
          <el-input v-model="form.industryName" />
        </el-form-item>
        <el-form-item label="工伤基准费率(%)">
          <el-input-number v-model="form.workInjuryRateBase" :precision="4" :min="0" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hrSocialParam'

const query = reactive({ pageNum: 1, pageSize: 20, industryName: '' })
const { records, total, loading, loadData } = useTable(api.getIndustryPageApi, query)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: undefined as number | undefined, industryCode: '', industryName: '', workInjuryRateBase: null as number | null, status: 1, remark: '' })
const rules = { industryCode: [{ required: true, message: '行业编码不能为空' }], industryName: [{ required: true, message: '行业名称不能为空' }] }

const openAdd = () => { Object.assign(form, { id: undefined, industryCode: '', industryName: '', workInjuryRateBase: null, status: 1, remark: '' }); dialogVisible.value = true }
const openEdit = (row: any) => { Object.assign(form, { id: row.id, industryCode: row.industryCode, industryName: row.industryName, workInjuryRateBase: row.workInjuryRateBase, status: row.status, remark: row.remark || '' }); dialogVisible.value = true }
const handleDelete = (row: any) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteIndustryApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await api.updateIndustryApi(form); ElMessage.success('编辑成功') }
    else { await api.addIndustryApi(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { query.pageNum = 1; loadData() }
</script>
