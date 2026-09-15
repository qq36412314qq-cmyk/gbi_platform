<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">城市字典</span>
      <AuthBtn permission="hr:city:add" type="primary" @click="openAdd">新增城市</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="城市名称">
        <el-input v-model="query.cityName" placeholder="请输入" clearable style="width:150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="请选择" clearable style="width:100px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="cityCode" label="城市编码" width="120" align="center" />
        <el-table-column prop="cityName" label="城市名称" width="120" align="center" />
        <el-table-column prop="province" label="省份" width="120" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:city:edit" link type="primary" size="small" @click="openEdit(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:city:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑城市' : '新增城市'" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="城市编码" prop="cityCode">
          <el-input v-model="form.cityCode" placeholder="如 BJ/GZ/SZ" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="城市名称" prop="cityName">
          <el-input v-model="form.cityName" placeholder="如 北京/广州/深圳" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hrSocialParam'

const query = reactive({ pageNum: 1, pageSize: 20, cityName: '', status: undefined as number | undefined })
const { records, total, loading, loadData } = useTable(api.getCityPageApi, query)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: undefined as number | undefined, cityCode: '', cityName: '', province: '', sort: 0, status: 1, remark: '' })
const rules = { cityCode: [{ required: true, message: '城市编码不能为空' }], cityName: [{ required: true, message: '城市名称不能为空' }] }

const openAdd = () => { Object.assign(form, { id: undefined, cityCode: '', cityName: '', province: '', sort: 0, status: 1, remark: '' }); dialogVisible.value = true }
const openEdit = (row: any) => { Object.assign(form, { id: row.id, cityCode: row.cityCode, cityName: row.cityName, province: row.province || '', sort: row.sort || 0, status: row.status, remark: row.remark || '' }); dialogVisible.value = true }
const handleDelete = (row: any) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteCityApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await api.updateCityApi(form); ElMessage.success('编辑成功') }
    else { await api.addCityApi(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { query.pageNum = 1; loadData() }
</script>
