<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">公积金参数配置</span>
      <AuthBtn permission="hr:housing:fund:add" type="primary" @click="openAdd">新增配置</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="城市">
        <el-select v-model="query.cityCode" placeholder="请选择" clearable style="width:120px">
          <el-option v-for="c in cityOptions" :key="c.cityCode" :label="c.cityName" :value="c.cityCode" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="cityCode" label="城市" width="100" align="center" />
        <el-table-column prop="periodStart" label="生效起始" width="110" align="center" />
        <el-table-column prop="periodEnd" label="生效截止" width="110" align="center" />
        <el-table-column prop="baseMin" label="基数下限" width="100" align="right" />
        <el-table-column prop="baseMax" label="基数上限" width="100" align="right" />
        <el-table-column prop="employeeRate" label="个人比例(%)" width="110" align="right" />
        <el-table-column prop="companyRate" label="单位比例(%)" width="110" align="right" />
        <el-table-column prop="isActive" label="状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '生效中' : '已停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:housing:fund:activate" link type="success" size="small" @click="handleActivate(row)" :disabled="row.isActive === 1">激活</AuthBtn>
            <AuthBtn permission="hr:housing:fund:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" title="新增公积金参数配置" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="城市" prop="cityCode">
          <el-select v-model="form.cityCode" placeholder="请选择" style="width:100%">
            <el-option v-for="c in cityOptions" :key="c.cityCode" :label="c.cityName" :value="c.cityCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效起始" prop="periodStart">
          <el-date-picker v-model="form.periodStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="生效截止">
          <el-date-picker v-model="form.periodEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="基数下限" prop="baseMin">
          <el-input-number v-model="form.baseMin" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="基数上限" prop="baseMax">
          <el-input-number v-model="form.baseMax" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="个人比例(%)" prop="employeeRate">
          <el-input-number v-model="form.employeeRate" :precision="2" :min="0" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位比例(%)" prop="companyRate">
          <el-input-number v-model="form.companyRate" :precision="2" :min="0" :max="100" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hrSocialParam'

const query = reactive({ pageNum: 1, pageSize: 20, cityCode: '' })
const { records, total, loading, loadData } = useTable(api.getHousingFundPageApi, query)
const cityOptions = ref<any[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ cityCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, employeeRate: 0, companyRate: 0 })
const rules = { cityCode: [{ required: true }], periodStart: [{ required: true }], baseMin: [{ required: true }], baseMax: [{ required: true }], employeeRate: [{ required: true }], companyRate: [{ required: true }] }

const loadCityOptions = async () => {
  try { const res = await api.getCityPageApi({ pageNum: 1, pageSize: 500 }); cityOptions.value = res.records || [] } catch {}
}

const openAdd = () => { Object.assign(form, { cityCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, employeeRate: 0, companyRate: 0 }); dialogVisible.value = true }
const handleActivate = (row: any) => { ElMessageBox.confirm('激活后将停用旧记录，确认?', '提示').then(async () => { await api.activateHousingFundApi(row.id!); ElMessage.success('激活成功'); loadData() }) }
const handleDelete = (row: any) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteHousingFundApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await api.addHousingFundApi(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { query.pageNum = 1; loadData() }

onMounted(() => { loadCityOptions(); loadData() })
</script>
