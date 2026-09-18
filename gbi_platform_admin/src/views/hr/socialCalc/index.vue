<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">社保核算明细</span>
      <AuthBtn permission="hr:social:calc:export" @click="handleExport">导出</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="城市">
        <el-select v-model="query.cityCode" placeholder="请选择" clearable style="width:120px">
          <el-option v-for="c in cityOptions" :key="c.cityCode" :label="c.cityName" :value="c.cityCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="薪资月份">
        <el-date-picker v-model="query.salaryMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="employeeName" label="员工姓名" width="100" align="center" />
        <el-table-column prop="cityCode" label="城市" width="80" align="center" />
        <el-table-column prop="salaryMonth" label="薪资月份" width="110" align="center" />
        <el-table-column prop="socialBase" label="社保基数" width="100" align="right" />
        <el-table-column prop="housingFundBase" label="公积金基数" width="110" align="right" />
        <el-table-column prop="pensionPersonal" label="养老个人" width="90" align="right" />
        <el-table-column prop="pensionCompany" label="养老单位" width="90" align="right" />
        <el-table-column prop="medicalPersonal" label="医疗个人" width="90" align="right" />
        <el-table-column prop="medicalCompany" label="医疗单位" width="90" align="right" />
        <el-table-column prop="unemploymentPersonal" label="失业个人" width="90" align="right" />
        <el-table-column prop="unemploymentCompany" label="失业单位" width="90" align="right" />
        <el-table-column prop="workInjuryCompany" label="工伤单位" width="90" align="right" />
        <el-table-column prop="maternityCompany" label="生育单位" width="90" align="right" />
        <el-table-column prop="longCarePersonal" label="长护险个人" width="100" align="right" />
        <el-table-column prop="longCareCompany" label="长护险单位" width="100" align="right" />
        <el-table-column prop="housingFundPersonal" label="公积金个人" width="100" align="right" />
        <el-table-column prop="housingFundCompany" label="公积金单位" width="100" align="right" />
        <el-table-column prop="createTime" label="生成时间" width="160" align="center" />
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hrSocialParam'

const query = reactive({ pageNum: 1, pageSize: 20, cityCode: '', salaryMonth: '' })
const { records, total, loading, loadData } = useTable(api.getCalcDetailPageApi, query)
const cityOptions = ref<any[]>([])

const loadCityOptions = async () => {
  try { const res = await api.getCityPageApi({ pageNum: 1, pageSize: 500 }); cityOptions.value = res.records || [] } catch {}
}

const handleExport = async () => {
  try { await api.exportCalcDetailApi(query) } catch {}
}
const handleReset = () => { query.pageNum = 1; loadData() }

onMounted(() => { loadCityOptions(); loadData() })
</script>
