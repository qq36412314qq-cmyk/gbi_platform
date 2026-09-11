<template>
  <div class="g-page-wrap">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="入职申请" name="entry">
        <div class="g-page-header">
          <span class="g-page-title">入职申请</span>
          <AuthBtn permission="hr:entry:add" type="primary" @click="openAdd('entry')">新增入职</AuthBtn>
        </div>
        <SearchBar :model="entryQuery" @search="loadEntryData" @reset="() => { entryQuery.pageNum = 1; loadEntryData() }">
          <el-form-item label="状态">
            <el-select v-model="entryQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="entryQuery.pageNum" v-model:page-size="entryQuery.pageSize" :total="entryTotal" @refresh="loadEntryData">
          <el-table v-loading="entryLoading" :data="entryRecords" border stripe>
            <el-table-column prop="employeeNo" label="工号" width="120" align="center" />
            <el-table-column prop="name" label="姓名" width="100" align="center" />
            <el-table-column prop="entryDate" label="入职日期" width="110" align="center" />
            <el-table-column prop="employmentTypeText" label="用工类型" width="100" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="entryStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
                <AuthBtn permission="hr:entry:revoke" link type="warning" size="small" @click="handleRevokeEntry(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="转正申请" name="regular">
        <div class="g-page-header">
          <span class="g-page-title">转正申请</span>
          <AuthBtn permission="hr:regular:add" type="primary" @click="openAdd('regular')">新增转正</AuthBtn>
        </div>
        <SearchBar :model="regularQuery" @search="loadRegularData" @reset="() => { regularQuery.pageNum = 1; loadRegularData() }">
          <el-form-item label="状态">
            <el-select v-model="regularQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="regularQuery.pageNum" v-model:page-size="regularQuery.pageSize" :total="regularTotal" @refresh="loadRegularData">
          <el-table v-loading="regularLoading" :data="regularRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="regularDate" label="转正日期" width="110" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:regular:revoke" link type="warning" size="small" @click="handleRevokeRegular(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="调岗申请" name="transfer">
        <div class="g-page-header">
          <span class="g-page-title">调岗申请</span>
          <AuthBtn permission="hr:transfer:add" type="primary" @click="openAdd('transfer')">新增调岗</AuthBtn>
        </div>
        <SearchBar :model="transferQuery" @search="loadTransferData" @reset="() => { transferQuery.pageNum = 1; loadTransferData() }">
          <el-form-item label="状态">
            <el-select v-model="transferQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="transferQuery.pageNum" v-model:page-size="transferQuery.pageSize" :total="transferTotal" @refresh="loadTransferData">
          <el-table v-loading="transferLoading" :data="transferRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="transferDate" label="调岗日期" width="110" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:transfer:revoke" link type="warning" size="small" @click="handleRevokeTransfer(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="离职申请" name="resign">
        <div class="g-page-header">
          <span class="g-page-title">离职申请</span>
          <AuthBtn permission="hr:resign:add" type="primary" @click="openAdd('resign')">新增离职</AuthBtn>
        </div>
        <SearchBar :model="resignQuery" @search="loadResignData" @reset="() => { resignQuery.pageNum = 1; loadResignData() }">
          <el-form-item label="状态">
            <el-select v-model="resignQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="resignQuery.pageNum" v-model:page-size="resignQuery.pageSize" :total="resignTotal" @refresh="loadResignData">
          <el-table v-loading="resignLoading" :data="resignRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="resignDate" label="离职日期" width="110" align="center" />
            <el-table-column prop="resignTypeText" label="离职类型" width="100" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:resign:revoke" link type="warning" size="small" @click="handleRevokeResign(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
    </el-tabs>

    <!-- 入职申请对话框 -->
    <el-dialog v-model="entryDialogVisible" title="入职申请" width="650px" :close-on-click-modal="false">
      <el-form ref="entryFormRef" :model="entryForm" :rules="entryRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工工号" prop="employeeNo"><el-input v-model="entryForm.employeeNo" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="entryForm.name" placeholder="请输入" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate"><el-date-picker v-model="entryForm.entryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="请选择" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用工类型" prop="employmentType">
              <el-select v-model="entryForm.employmentType" style="width:100%">
                <el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别"><el-select v-model="entryForm.gender" placeholder="请选择" style="width:100%"><el-option label="男" :value="1" /><el-option label="女" :value="2" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期"><el-date-picker v-model="entryForm.birthdate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="请选择" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号"><el-input v-model="entryForm.phone" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号"><el-input v-model="entryForm.idCardNo" placeholder="请输入" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属组织" prop="orgId">
              <el-select v-model="entryForm.orgId" placeholder="请选择组织" filterable style="width:100%" :loading="orgLoading">
                <el-option v-for="o in orgList" :key="o.id" :label="o.orgName" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="postId">
              <el-select v-model="entryForm.postId" placeholder="请选择岗位" filterable style="width:100%" :loading="postLoading">
                <el-option v-for="p in postList" :key="p.id" :label="p.postName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基本工资"><el-input-number v-model="entryForm.basicSalary" :precision="2" :min="0" style="width:100%" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工资卡号"><el-input v-model="entryForm.bankAccount" placeholder="请输入" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="entryForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="entrySubmitLoading" @click="handleSubmitEntry">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看入职申请对话框 -->
    <el-dialog v-model="viewDialogVisible" title="入职申请详情" width="600px">
      <el-descriptions :column="2" border v-if="viewRecord">
        <el-descriptions-item label="工号">{{ viewRecord.employeeNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewRecord.name }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ viewRecord.entryDate }}</el-descriptions-item>
        <el-descriptions-item label="用工类型">{{ employmentTypeText(viewRecord.employmentType) }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ viewRecord.gender === 1 ? '男' : viewRecord.gender === 2 ? '女' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ viewRecord.birthdate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewRecord.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ viewRecord.idCardNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ orgNameMap[viewRecord.orgId] || viewRecord.orgId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ postNameMap[viewRecord.postId] || viewRecord.postId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ viewRecord.basicSalary ? '¥' + viewRecord.basicSalary : '-' }}</el-descriptions-item>
        <el-descriptions-item label="工资卡号">{{ viewRecord.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewRecord.statusText }}</el-descriptions-item>
        <el-descriptions-item label="流程实例ID">{{ viewRecord.flowInstanceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewRecord.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const activeTab = ref('entry')

/* ---- 下拉选项数据 ---- */
const orgList = ref<api.OrgFlatOption[]>([])
const postList = ref<api.PostFlatOption[]>([])
const orgLoading = ref(false)
const postLoading = ref(false)
const orgNameMap = ref<Record<number, string>>({})
const postNameMap = ref<Record<number, string>>({})

const loadOptions = async () => {
  orgLoading.value = true
  try {
    const list = await api.getOrgFlatListApi()
    orgList.value = list
    orgNameMap.value = Object.fromEntries(list.map(o => [o.id, o.orgName]))
  } finally { orgLoading.value = false }
  postLoading.value = true
  try {
    const result = await api.getPostFlatListApi()
    postList.value = result
    postNameMap.value = Object.fromEntries(result.map(p => [p.id, p.postName]))
  } finally { postLoading.value = false }
}

/* ---- 入职申请 ---- */
const entryQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: entryRecords, total: entryTotal, loading: entryLoading, loadData: loadEntryData } = useTable(api.getEntryPageApi, entryQuery)

const entryDialogVisible = ref(false)
const entrySubmitLoading = ref(false)
const entryFormRef = ref()
const entryForm = reactive<api.EntryApplyDTO>({ employeeNo: '', name: '', entryDate: '', employmentType: 1 })
const entryRules = {
  employeeNo: [{ required: true, message: '工号不能为空' }],
  name: [{ required: true, message: '姓名不能为空' }],
  entryDate: [{ required: true, message: '入职日期不能为空' }]
}

const openAdd = (tab: string) => {
  if (tab === 'entry') {
    Object.assign(entryForm, { employeeNo: '', name: '', entryDate: '', employmentType: 1 })
    entryDialogVisible.value = true
  }
}

const handleSubmitEntry = async () => {
  await entryFormRef.value.validate()
  entrySubmitLoading.value = true
  try {
    await api.submitEntryApi(entryForm)
    ElMessage.success('提交成功')
    entryDialogVisible.value = false
    loadEntryData()
  } finally { entrySubmitLoading.value = false }
}

const handleRevokeEntry = (row: api.EntryApplyVO) => {
  ElMessageBox.confirm('确认撤销该入职申请?', '提示').then(async () => {
    await api.revokeEntryApi(row.id!)
    ElMessage.success('撤销成功')
    loadEntryData()
  })
}

/* ---- 查看入职申请 ---- */
const viewDialogVisible = ref(false)
const viewRecord = ref<api.EntryApplyVO | null>(null)
const handleView = (row: api.EntryApplyVO) => { viewRecord.value = row; viewDialogVisible.value = true }
const employmentTypeText = (type?: number) => {
  const map: Record<number, string> = { 1: '正式', 2: '试用期', 3: '劳务派遣', 4: '临时工' }
  return type != null ? map[type] : '-'
}
const entryStatusType = (status?: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return status != null ? map[status] : ''
}
const transferStatusType = (status?: number) => entryStatusType(status)

/* ---- 转正 / 调岗 / 离职（占位） ---- */
const regularQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: regularRecords, total: regularTotal, loading: regularLoading, loadData: loadRegularData } = useTable(api.getRegularPageApi, regularQuery)

const transferQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: transferRecords, total: transferTotal, loading: transferLoading, loadData: loadTransferData } = useTable(api.getTransferPageApi, transferQuery)

const resignQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: resignRecords, total: resignTotal, loading: resignLoading, loadData: loadResignData } = useTable(api.getResignPageApi, resignQuery)

onMounted(() => { loadOptions(); loadEntryData() })
</script>