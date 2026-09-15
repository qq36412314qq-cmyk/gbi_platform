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
      <el-table v-loading="loading" :data="records" border stripe @expand-change="onExpandChange">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 10px 20px">
              <el-tabs v-model="activeTab[row.id]">
                <!-- 工作经历Tab -->
                <el-tab-pane label="工作经历">
                  <div style="margin-bottom: 10px">
                    <AuthBtn v-if="hasEditPermission" permission="hr:workExp:add" type="primary" size="small" @click="openWorkDialog(row)">+ 新增工作经历</AuthBtn>
                  </div>
                  <el-table :data="workExpMap[row.id] || []" border size="small" empty-text="暂无工作经历">
                    <el-table-column prop="companyName" label="公司名称" min-width="150" />
                    <el-table-column prop="position" label="职位" width="120" />
                    <el-table-column prop="department" label="部门" width="120" />
                    <el-table-column prop="startDate" label="入职时间" width="110" />
                    <el-table-column prop="endDate" label="离职时间" width="110" />
                    <el-table-column prop="isCurrentText" label="状态" width="80" align="center">
                      <template #default="{ row: item }">
                        <el-tag size="small" :type="item.isCurrent === 1 ? 'success' : 'info'">{{ item.isCurrentText }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="reasonForLeaving" label="离职原因" min-width="120" show-overflow-tooltip />
                    <el-table-column label="操作" width="120" align="center" v-if="hasEditPermission">
                      <template #default="{ row: item }">
                        <AuthBtn permission="hr:workExp:edit" link type="primary" size="small" @click="editWorkExp(item, row)">编辑</AuthBtn>
                        <AuthBtn permission="hr:workExp:delete" link type="danger" size="small" @click="delWorkExp(item)">删除</AuthBtn>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <!-- 学业经历Tab -->
                <el-tab-pane label="学业经历">
                  <div style="margin-bottom: 10px">
                    <AuthBtn v-if="hasEditPermission" permission="hr:eduExp:add" type="primary" size="small" @click="openEduDialog(row)">+ 新增学业经历</AuthBtn>
                  </div>
                  <el-table :data="eduExpMap[row.id] || []" border size="small" empty-text="暂无学业经历">
                    <el-table-column prop="schoolName" label="学校名称" min-width="150" />
                    <el-table-column prop="degree" label="学历" width="100" />
                    <el-table-column prop="major" label="专业" width="120" />
                    <el-table-column prop="educationLevel" label="教育形式" width="100" />
                    <el-table-column prop="startDate" label="入学时间" width="110" />
                    <el-table-column prop="graduationDate" label="毕业时间" width="110" />
                    <el-table-column prop="isGraduatedText" label="状态" width="80" align="center">
                      <template #default="{ row: item }">
                        <el-tag size="small" :type="item.isGraduated === 1 ? 'success' : 'warning'">{{ item.isGraduatedText }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="certificateNo" label="证书编号" width="120" />
                    <el-table-column label="操作" width="120" align="center" v-if="hasEditPermission">
                      <template #default="{ row: item }">
                        <AuthBtn permission="hr:eduExp:edit" link type="primary" size="small" @click="editEduExp(item, row)">编辑</AuthBtn>
                        <AuthBtn permission="hr:eduExp:delete" link type="danger" size="small" @click="delEduExp(item)">删除</AuthBtn>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </template>
        </el-table-column>
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

    <!-- 员工新增/编辑弹窗 -->
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

    <!-- 工作经历弹窗 -->
    <el-dialog v-model="workDialogVisible" :title="workForm.id ? '编辑工作经历' : '新增工作经历'" width="500px" :close-on-click-modal="false">
      <el-form ref="workFormRef" :model="workForm" :rules="workRules" label-width="100px">
        <el-form-item label="公司名称" prop="companyName"><el-input v-model="workForm.companyName" placeholder="请输入公司名称" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="workForm.position" placeholder="请输入职位" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="workForm.department" placeholder="请输入部门" /></el-form-item>
        <el-form-item label="入职时间" prop="startDate"><el-date-picker v-model="workForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="离职时间"><el-date-picker v-model="workForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled="workForm.isCurrent === 1" /></el-form-item>
        <el-form-item label="是否在职">
          <el-radio-group v-model="workForm.isCurrent">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">已离职</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="离职原因" v-if="workForm.isCurrent === 0"><el-input v-model="workForm.reasonForLeaving" type="textarea" :rows="2" placeholder="请输入离职原因" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="workForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="workDialogVisible=false">取消</el-button><el-button type="primary" :loading="workSubmitLoading" @click="handleWorkSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 学业经历弹窗 -->
    <el-dialog v-model="eduDialogVisible" :title="eduForm.id ? '编辑学业经历' : '新增学业经历'" width="500px" :close-on-click-modal="false">
      <el-form ref="eduFormRef" :model="eduForm" :rules="eduRules" label-width="100px">
        <el-form-item label="学校名称" prop="schoolName"><el-input v-model="eduForm.schoolName" placeholder="请输入学校名称" /></el-form-item>
        <el-form-item label="学历"><el-input v-model="eduForm.degree" placeholder="如：本科/硕士/博士" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="eduForm.major" placeholder="请输入专业" /></el-form-item>
        <el-form-item label="教育形式"><el-input v-model="eduForm.educationLevel" placeholder="如：全日制/在职/自考" /></el-form-item>
        <el-form-item label="入学时间" prop="startDate"><el-date-picker v-model="eduForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="毕业时间"><el-date-picker v-model="eduForm.graduationDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled="eduForm.isGraduated === 1" /></el-form-item>
        <el-form-item label="是否毕业">
          <el-radio-group v-model="eduForm.isGraduated">
            <el-radio :value="1">已毕业</el-radio>
            <el-radio :value="0">在读</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="证书编号" v-if="eduForm.isGraduated === 1"><el-input v-model="eduForm.certificateNo" placeholder="请输入学位证书号" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="eduForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="eduDialogVisible=false">取消</el-button><el-button type="primary" :loading="eduSubmitLoading" @click="handleEduSubmit">确定</el-button></template>
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
const hasEditPermission = computed(() => userStore.hasPermission('hr:employee:edit') || userStore.hasPermission('hr:workExp:edit') || userStore.hasPermission('hr:eduExp:edit'))

const { query, records, total, loading, loadData, resetQuery } = useTable(api.getEmployeePageApi, {
  pageNum: 1, pageSize: 20, name: '', employeeNo: '', employeeStatus: undefined
})

// 展开相关状态
const workExpMap = reactive<Record<number, api.WorkExpVO[]>>({})
const eduExpMap = reactive<Record<number, api.EduExpVO[]>>({})
const activeTab = reactive<Record<number, string>>({})

const onExpandChange = (row: api.EmployeeVO, expandedRows: any[]) => {
  const id = row.id!
  if (!workExpMap[id]) {
    api.getWorkExpsApi(id).then(list => { workExpMap[id] = list })
  }
  if (!eduExpMap[id]) {
    api.getEduExpsApi(id).then(list => { eduExpMap[id] = list })
  }
  if (!activeTab[id]) {
    activeTab[id] = 'work'
  }
}

// 员工表单
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

// 工作经历弹窗
const workDialogVisible = ref(false)
const workSubmitLoading = ref(false)
const workFormRef = ref()
const currentWorkEmployeeId = ref<number>()
const workForm = reactive<api.WorkExpDTO>({ employeeId: 0, companyName: '', startDate: '' })
const workRules = { companyName: [{ required: true, message: '公司名称不能为空' }], startDate: [{ required: true, message: '入职时间不能为空' }] }

const openWorkDialog = (row: api.EmployeeVO) => {
  Object.assign(workForm, { id: undefined, employeeId: row.id!, companyName: '', position: '', department: '', startDate: '', endDate: '', isCurrent: 0, reasonForLeaving: '', remark: '' })
  currentWorkEmployeeId.value = row.id!
  workDialogVisible.value = true
}
const editWorkExp = (item: api.WorkExpVO, row: api.EmployeeVO) => {
  Object.assign(workForm, { id: item.id, employeeId: row.id!, companyName: item.companyName, position: item.position, department: item.department, startDate: item.startDate, endDate: item.endDate, isCurrent: item.isCurrent, reasonForLeaving: item.reasonForLeaving, remark: item.remark })
  currentWorkEmployeeId.value = row.id!
  workDialogVisible.value = true
}
const handleWorkSubmit = async () => {
  await workFormRef.value.validate()
  workSubmitLoading.value = true
  try {
    if (workForm.id) {
      await api.updateWorkExpApi(workForm)
      ElMessage.success('编辑成功')
    } else {
      await api.addWorkExpApi(workForm)
      ElMessage.success('新增成功')
    }
    workDialogVisible.value = false
    if (currentWorkEmployeeId.value) {
      api.getWorkExpsApi(currentWorkEmployeeId.value).then(list => { workExpMap[currentWorkEmployeeId.value!] = list })
    }
  } finally { workSubmitLoading.value = false }
}
const delWorkExp = (item: api.WorkExpVO) => {
  ElMessageBox.confirm('确认删除该工作经历?', '提示').then(async () => {
    await api.deleteWorkExpApi(item.id!)
    ElMessage.success('删除成功')
    if (currentWorkEmployeeId.value) {
      api.getWorkExpsApi(currentWorkEmployeeId.value).then(list => { workExpMap[currentWorkEmployeeId.value!] = list })
    }
  })
}

// 学业经历弹窗
const eduDialogVisible = ref(false)
const eduSubmitLoading = ref(false)
const eduFormRef = ref()
const currentEduEmployeeId = ref<number>()
const eduForm = reactive<api.EduExpDTO>({ employeeId: 0, schoolName: '', startDate: '' })
const eduRules = { schoolName: [{ required: true, message: '学校名称不能为空' }], startDate: [{ required: true, message: '入学时间不能为空' }] }

const openEduDialog = (row: api.EmployeeVO) => {
  Object.assign(eduForm, { id: undefined, employeeId: row.id!, schoolName: '', degree: '', major: '', educationLevel: '', startDate: '', graduationDate: '', isGraduated: 0, certificateNo: '', remark: '' })
  currentEduEmployeeId.value = row.id!
  eduDialogVisible.value = true
}
const editEduExp = (item: api.EduExpVO, row: api.EmployeeVO) => {
  Object.assign(eduForm, { id: item.id, employeeId: row.id!, schoolName: item.schoolName, degree: item.degree, major: item.major, educationLevel: item.educationLevel, startDate: item.startDate, graduationDate: item.graduationDate, isGraduated: item.isGraduated, certificateNo: item.certificateNo, remark: item.remark })
  currentEduEmployeeId.value = row.id!
  eduDialogVisible.value = true
}
const handleEduSubmit = async () => {
  await eduFormRef.value.validate()
  eduSubmitLoading.value = true
  try {
    if (eduForm.id) {
      await api.updateEduExpApi(eduForm)
      ElMessage.success('编辑成功')
    } else {
      await api.addEduExpApi(eduForm)
      ElMessage.success('新增成功')
    }
    eduDialogVisible.value = false
    if (currentEduEmployeeId.value) {
      api.getEduExpsApi(currentEduEmployeeId.value).then(list => { eduExpMap[currentEduEmployeeId.value!] = list })
    }
  } finally { eduSubmitLoading.value = false }
}
const delEduExp = (item: api.EduExpVO) => {
  ElMessageBox.confirm('确认删除该学业经历?', '提示').then(async () => {
    await api.deleteEduExpApi(item.id!)
    ElMessage.success('删除成功')
    if (currentEduEmployeeId.value) {
      api.getEduExpsApi(currentEduEmployeeId.value).then(list => { eduExpMap[currentEduEmployeeId.value!] = list })
    }
  })
}
</script>
