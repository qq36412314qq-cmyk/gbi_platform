<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">员工档案</span>
      <div style="display:flex;gap:8px">
        <span v-if="!hasAddPermission" style="font-size:12px;color:#909399;line-height:32px">审批通过的入职申请将自动创建员工档案，无需手动新增</span>
        <AuthBtn v-if="hasAddPermission" permission="hr:employee:add" type="primary" @click="openAddDialog">新增员工</AuthBtn>
        <el-button v-if="hasAddPermission" link @click="openHistoryAdd">历史补录</el-button>
      </div>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="请输入" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="工号">
        <el-input v-model="query.employeeNo" placeholder="请输入" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.employeeStatus" placeholder="全部" clearable style="width:100px">
          <el-option label="在职" :value="1" />
          <el-option label="试用期" :value="2" />
          <el-option label="离职" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="employeeNo" label="工号" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="genderText" label="性别" width="60" align="center">
          <template #default="{ row }">{{ row.genderText || '-' }}</template>
        </el-table-column>
        <el-table-column prop="employmentTypeText" label="用工类型" width="100" align="center" />
        <el-table-column prop="employeeStatusText" label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.employeeStatusText || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" align="center" />
        <el-table-column prop="entryDate" label="入职日期" width="110" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:employee:edit" link type="primary" size="small" @click="openEditDialog(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:employee:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="员工工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender" placeholder="请选择" style="width:100%"><el-option label="男" :value="1" /><el-option label="女" :value="2" /></el-select></el-form-item>
        <el-form-item label="入职日期"><el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="用工类型"><el-select v-model="form.employmentType" placeholder="请选择" style="width:100%"><el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" /></el-select></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入" /></el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="form.basicSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import { useUserStore } from '@/store/user'
import * as api from '@/api/hr'

const userStore = useUserStore()
const hasAddPermission = computed(() => userStore.hasPermission('hr:employee:add'))

const { query, records, total, loading, loadData, resetQuery } = useTable(api.getEmployeePageApi, {
  pageNum: 1, pageSize: 20, name: '', employeeNo: '', employeeStatus: undefined
})

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive<api.EmployeeDTO>({ employeeNo: '', name: '', employmentType: 1 })
const rules = { employeeNo: [{ required: true, message: '工号不能为空' }], name: [{ required: true, message: '姓名不能为空' }] }

const openAddDialog = () => { Object.assign(form, { id: undefined, employeeNo: '', name: '', employmentType: 1 }); dialogVisible.value = true }
const openHistoryAdd = () => { Object.assign(form, { id: undefined, employeeNo: '', name: '', employmentType: 1, remark: '历史补录' }); dialogVisible.value = true }
const openEditDialog = (row: api.EmployeeVO) => { Object.assign(form, { id: row.id, employeeNo: row.employeeNo, name: row.name, gender: row.gender, birthdate: row.birthdate, entryDate: row.entryDate, employmentType: row.employmentType, phone: row.phone, email: row.email, basicSalary: row.basicSalary, remark: row.remark }); dialogVisible.value = true }
const handleDelete = (row: api.EmployeeVO) => { ElMessageBox.confirm('确认删除该员工?', '提示').then(async () => { await api.deleteEmployeeApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => { await formRef.value.validate(); submitLoading.value = true; try { if (form.id) { await api.updateEmployeeApi(form); ElMessage.success('编辑成功') } else { await api.addEmployeeApi(form); ElMessage.success('新增成功') } dialogVisible.value = false; loadData() } finally { submitLoading.value = false } }
const handleReset = () => { resetQuery(); loadData() }
</script>