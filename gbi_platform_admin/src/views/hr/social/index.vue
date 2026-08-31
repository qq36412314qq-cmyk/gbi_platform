<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">社保公积金</span>
      <AuthBtn permission="hr:social:add" type="primary" @click="openAdd">新增台账</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
          <el-option label="参保" :value="1" /><el-option label="停保" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
        <el-table-column prop="socialSecurityBase" label="社保基数" width="110" align="right" />
        <el-table-column prop="housingFundBase" label="公积金基数" width="110" align="right" />
        <el-table-column prop="socialSecurityCompany" label="公司社保" width="110" align="right" />
        <el-table-column prop="socialSecurityPersonal" label="个人社保" width="110" align="right" />
        <el-table-column prop="housingFundCompany" label="公司公积金" width="110" align="right" />
        <el-table-column prop="housingFundPersonal" label="个人公积金" width="110" align="right" />
        <el-table-column prop="startMonth" label="起始月份" width="100" align="center" />
        <el-table-column prop="statusText" label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.statusText || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:social:edit" link type="primary" size="small" @click="openEdit(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:social:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑社保台账' : '新增社保台账'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="员工ID" prop="employeeId"><el-input-number v-model="form.employeeId" style="width:100%" /></el-form-item>
        <el-form-item label="社保基数"><el-input-number v-model="form.socialSecurityBase" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="公积金基数"><el-input-number v-model="form.housingFundBase" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="公司社保"><el-input-number v-model="form.socialSecurityCompany" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="个人社保"><el-input-number v-model="form.socialSecurityPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="公司公积金"><el-input-number v-model="form.housingFundCompany" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="个人公积金"><el-input-number v-model="form.housingFundPersonal" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="起始月份" prop="startMonth"><el-date-picker v-model="form.startMonth" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">参保</el-radio><el-radio :value="0">停保</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const { query, records, total, loading, loadData } = useTable(api.getSocialPageApi, { pageNum: 1, pageSize: 20, employeeId: undefined, status: undefined })
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive<api.SocialDTO>({ employeeId: 0, socialSecurityBase: 0, housingFundBase: 0, status: 1, startMonth: '' })
const rules = { employeeId: [{ required: true, message: '员工ID不能为空' }], startMonth: [{ required: true, message: '起始月份不能为空' }] }

const openAdd = () => { Object.assign(form, { id: undefined, socialSecurityBase: 0, housingFundBase: 0, socialSecurityCompany: 0, socialSecurityPersonal: 0, housingFundCompany: 0, housingFundPersonal: 0, status: 1, startMonth: '' }); dialogVisible.value = true }
const openEdit = (row: api.SocialVO) => { Object.assign(form, { id: row.id, employeeId: row.employeeId, socialSecurityBase: row.socialSecurityBase, housingFundBase: row.housingFundBase, socialSecurityCompany: row.socialSecurityCompany, socialSecurityPersonal: row.socialSecurityPersonal, housingFundCompany: row.housingFundCompany, housingFundPersonal: row.housingFundPersonal, startMonth: row.startMonth, endMonth: row.endMonth, status: row.status, remark: row.remark }); dialogVisible.value = true }
const handleDelete = (row: api.SocialVO) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteSocialApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => { await formRef.value.validate(); submitLoading.value = true; try { if (form.id) { await api.updateSocialApi(form); ElMessage.success('编辑成功') } else { await api.addSocialApi(form); ElMessage.success('新增成功') } dialogVisible.value = false; loadData() } finally { submitLoading.value = false } }
const handleReset = () => { loadData() }
</script>
