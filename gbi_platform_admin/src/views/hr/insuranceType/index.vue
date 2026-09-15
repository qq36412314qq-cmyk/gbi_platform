<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">险种字典</span>
      <AuthBtn permission="hr:insurance:add" type="primary" @click="openAdd">新增险种</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="险种名称">
        <el-input v-model="query.insuranceName" placeholder="请输入" clearable style="width:150px" />
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="insuranceCode" label="险种编码" width="150" align="center" />
        <el-table-column prop="insuranceName" label="险种名称" width="120" align="center" />
        <el-table-column prop="insuranceType" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.insuranceType === 1 ? 'success' : row.insuranceType === 2 ? 'warning' : 'info'" size="small">
              {{ row.insuranceType === 1 ? '法定五险' : row.insuranceType === 2 ? '补充福利' : '试点险种' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="personalShare" label="个人缴纳" width="100" align="center">
          <template #default="{ row }"><el-tag :type="row.personalShare === 1 ? '' : 'info'" size="small">{{ row.personalShare === 1 ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="companyShare" label="单位缴纳" width="100" align="center">
          <template #default="{ row }"><el-tag :type="row.companyShare === 1 ? '' : 'info'" size="small">{{ row.companyShare === 1 ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:insurance:edit" link type="primary" size="small" @click="openEdit(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:insurance:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑险种' : '新增险种'" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="险种编码" prop="insuranceCode">
          <el-input v-model="form.insuranceCode" placeholder="如 PENSION/MEDICAL" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="险种名称" prop="insuranceName">
          <el-input v-model="form.insuranceName" placeholder="如 养老保险" />
        </el-form-item>
        <el-form-item label="分类" prop="insuranceType">
          <el-select v-model="form.insuranceType" style="width:100%">
            <el-option label="法定五险" :value="1" />
            <el-option label="补充福利" :value="2" />
            <el-option label="试点险种" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="个人缴纳">
          <el-radio-group v-model="form.personalShare">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单位缴纳">
          <el-radio-group v-model="form.companyShare">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
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

const query = reactive({ pageNum: 1, pageSize: 20, insuranceName: '' })
const { records, total, loading, loadData } = useTable(api.getInsuranceTypePageApi, query)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: undefined as number | undefined, insuranceCode: '', insuranceName: '', insuranceType: 1, personalShare: 1, companyShare: 1, status: 1, remark: '' })
const rules = { insuranceCode: [{ required: true, message: '险种编码不能为空' }], insuranceName: [{ required: true, message: '险种名称不能为空' }] }

const openAdd = () => { Object.assign(form, { id: undefined, insuranceCode: '', insuranceName: '', insuranceType: 1, personalShare: 1, companyShare: 1, status: 1, remark: '' }); dialogVisible.value = true }
const openEdit = (row: any) => { Object.assign(form, { id: row.id, insuranceCode: row.insuranceCode, insuranceName: row.insuranceName, insuranceType: row.insuranceType, personalShare: row.personalShare, companyShare: row.companyShare, status: row.status, remark: row.remark || '' }); dialogVisible.value = true }
const handleDelete = (row: any) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteInsuranceTypeApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await api.updateInsuranceTypeApi(form); ElMessage.success('编辑成功') }
    else { await api.addInsuranceTypeApi(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { query.pageNum = 1; loadData() }
</script>
