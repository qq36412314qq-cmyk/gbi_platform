<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">社保参数配置</span>
      <AuthBtn permission="hr:social:param:add" type="primary" @click="openAdd">新增配置</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="城市">
        <el-select v-model="query.cityCode" placeholder="请选择" clearable style="width:120px">
          <el-option v-for="c in cityOptions" :key="c.cityCode" :label="c.cityName" :value="c.cityCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="险种">
        <el-select v-model="query.insuranceCode" placeholder="请选择" clearable style="width:120px">
          <el-option v-for="i in insuranceOptions" :key="i.insuranceCode" :label="i.insuranceName" :value="i.insuranceCode" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="cityCode" label="城市" width="100" align="center" />
        <el-table-column prop="insuranceCode" label="险种编码" width="100" align="center" />
        <el-table-column prop="insuranceName" label="险种名称" width="120" align="center" />
        <el-table-column prop="periodStart" label="生效起始" width="110" align="center" />
        <el-table-column prop="periodEnd" label="生效截止" width="110" align="center" />
        <el-table-column prop="baseMin" label="基数下限" width="100" align="right" />
        <el-table-column prop="baseMax" label="基数上限" width="100" align="right" />
        <el-table-column prop="personalRate" label="个人比例(%)" width="110" align="right" />
        <el-table-column prop="companyRate" label="单位比例(%)" width="110" align="right" />
        <el-table-column prop="isActive" label="生效状态" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '生效中' : '已停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:social:param:edit" link type="primary" size="small" @click="handleEdit(row)" :disabled="row.isActive === 1">编辑</AuthBtn>
            <AuthBtn permission="hr:social:param:activate" link type="success" size="small" @click="handleActivate(row)" :disabled="row.isActive === 1">激活</AuthBtn>
            <AuthBtn permission="hr:social:param:deactivate" link :type="row.isActive === 1 ? 'warning' : 'success'" size="small" @click="handleToggle(row)">{{ row.isActive === 1 ? '停用' : '启用' }}</AuthBtn>
            <AuthBtn permission="hr:social:param:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑社保参数配置' : '新增社保参数配置'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="城市" prop="cityCode">
          <el-select v-model="form.cityCode" placeholder="请选择" style="width:100%">
            <el-option v-for="c in cityOptions" :key="c.cityCode" :label="c.cityName" :value="c.cityCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="险种" prop="insuranceCode">
          <el-select v-model="form.insuranceCode" placeholder="请选择" style="width:100%">
            <el-option v-for="i in insuranceOptions" :key="i.insuranceCode" :label="i.insuranceName" :value="i.insuranceCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="form.industryCode" placeholder="选填（工伤保险必填）" clearable style="width:100%">
            <el-option v-for="ind in industryOptions" :key="ind.industryCode" :label="ind.industryName" :value="ind.industryCode" />
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
        <el-form-item label="个人比例(%)" prop="personalRate">
          <el-input-number v-model="form.personalRate" :precision="2" :min="0" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位比例(%)" prop="companyRate">
          <el-input-number v-model="form.companyRate" :precision="2" :min="0" :max="100" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ form.id ? '编辑' : '新增' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hrSocialParam'

const query = reactive({ pageNum: 1, pageSize: 20, cityCode: '', insuranceCode: '' })
const { records, total, loading, loadData } = useTable(api.getSocialParamPageApi, query)
const cityOptions = ref<any[]>([])
const insuranceOptions = ref<any[]>([])
const industryOptions = ref<any[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: null as number | null, cityCode: '', insuranceCode: '', industryCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, personalRate: 0, companyRate: 0 })
const rules = { cityCode: [{ required: true }], insuranceCode: [{ required: true }], periodStart: [{ required: true }], baseMin: [{ required: true }], baseMax: [{ required: true }], personalRate: [{ required: true }], companyRate: [{ required: true }] }

const loadDictData = async () => {
  try {
    const [cityRes, insuranceRes, industryRes] = await Promise.all([
      api.getCityPageApi({ pageNum: 1, pageSize: 500 }),
      api.getInsuranceTypePageApi({ pageNum: 1, pageSize: 500 }),
      api.getIndustryPageApi({ pageNum: 1, pageSize: 500 })
    ])
    cityOptions.value = cityRes.records || []
    insuranceOptions.value = insuranceRes.records || []
    industryOptions.value = industryRes.records || []
  } catch {}
}

const openDialog = (data?: any) => {
  if (data) {
    Object.assign(form, data)
  } else {
    Object.assign(form, { id: null, cityCode: '', insuranceCode: '', industryCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, personalRate: 0, companyRate: 0 })
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  openDialog(row)
}

const openAdd = () => openDialog()
const handleActivate = (row: any) => { ElMessageBox.confirm('激活后将停用旧记录，确认?', '提示').then(async () => { await api.activateSocialParamApi(row.id!); ElMessage.success('激活成功'); loadData() }) }
const handleToggle = (row: any) => {
  const action = row.isActive === 1 ? '停用' : '启用'
  ElMessageBox.confirm(`确认${action}?`, '提示').then(async () => {
    await api.toggleSocialParamApi(row.id!)
    ElMessage.success(`${action}成功`)
    loadData()
  })
}
const handleDelete = (row: any) => { ElMessageBox.confirm('确认删除?', '提示').then(async () => { await api.deleteSocialParamApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await api.updateSocialParamApi(form)
      ElMessage.success('编辑成功')
    } else {
      await api.addSocialParamApi(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { query.pageNum = 1; loadData() }

onMounted(() => { loadDictData(); loadData() })
</script>
