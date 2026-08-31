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
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
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
            <el-table-column prop="newPostLevel" label="新岗位职级" width="120" align="center" />
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

    <!-- 入职弹窗 -->
    <el-dialog v-model="entryDialogVisible" title="新增入职申请" width="560px" :close-on-click-modal="false">
      <el-form ref="entryFormRef" :model="entryForm" :rules="entryRules" label-width="100px">
        <el-form-item label="员工工号" prop="employeeNo"><el-input v-model="entryForm.employeeNo" placeholder="请输入" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="entryForm.name" placeholder="请输入" /></el-form-item>
        <el-form-item label="入职日期" prop="entryDate"><el-date-picker v-model="entryForm.entryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="用工类型"><el-select v-model="entryForm.employmentType" style="width:100%"><el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" /></el-select></el-form-item>
        <el-form-item label="所属组织ID"><el-input-number v-model="entryForm.orgId" style="width:100%" /></el-form-item>
        <el-form-item label="岗位ID"><el-input-number v-model="entryForm.postId" style="width:100%" /></el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="entryForm.basicSalary" :precision="2" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="entryDialogVisible=false">取消</el-button><el-button type="primary" :loading="entrySubmitLoading" @click="handleSubmitEntry">提交审批</el-button></template>
    </el-dialog>

    <!-- 转正弹窗 -->
    <el-dialog v-model="regularDialogVisible" title="新增转正申请" width="480px" :close-on-click-modal="false">
      <el-form ref="regularFormRef" :model="regularForm" :rules="regularRules" label-width="100px">
        <el-form-item label="员工ID" prop="employeeId"><el-input-number v-model="regularForm.employeeId" style="width:100%" /></el-form-item>
        <el-form-item label="转正日期" prop="regularDate"><el-date-picker v-model="regularForm.regularDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="regularForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="regularDialogVisible=false">取消</el-button><el-button type="primary" :loading="regularSubmitLoading" @click="handleSubmitRegular">提交审批</el-button></template>
    </el-dialog>

    <!-- 调岗弹窗 -->
    <el-dialog v-model="transferDialogVisible" title="新增调岗申请" width="480px" :close-on-click-modal="false">
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="员工ID" prop="employeeId"><el-input-number v-model="transferForm.employeeId" style="width:100%" /></el-form-item>
        <el-form-item label="新组织ID" prop="newOrgId"><el-input-number v-model="transferForm.newOrgId" style="width:100%" /></el-form-item>
        <el-form-item label="新岗位ID" prop="newPostId"><el-input-number v-model="transferForm.newPostId" style="width:100%" /></el-form-item>
        <el-form-item label="调岗日期" prop="transferDate"><el-date-picker v-model="transferForm.transferDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="transferForm.reason" type="textarea" :rows="2" placeholder="请输入调岗原因" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="transferDialogVisible=false">取消</el-button><el-button type="primary" :loading="transferSubmitLoading" @click="handleSubmitTransfer">提交审批</el-button></template>
    </el-dialog>

    <!-- 离职弹窗 -->
    <el-dialog v-model="resignDialogVisible" title="新增离职申请" width="480px" :close-on-click-modal="false">
      <el-form ref="resignFormRef" :model="resignForm" :rules="resignRules" label-width="100px">
        <el-form-item label="员工ID" prop="employeeId"><el-input-number v-model="resignForm.employeeId" style="width:100%" /></el-form-item>
        <el-form-item label="离职日期" prop="resignDate"><el-date-picker v-model="resignForm.resignDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="离职类型" prop="resignType"><el-select v-model="resignForm.resignType" style="width:100%"><el-option label="主动辞职" :value="1" /><el-option label="合同到期" :value="2" /><el-option label="辞退" :value="3" /><el-option label="终止合同" :value="4" /></el-select></el-form-item>
        <el-form-item label="原因"><el-input v-model="resignForm.reason" type="textarea" :rows="2" placeholder="请输入离职原因" /></el-form-item>
        <el-form-item label="工作交接"><el-input v-model="resignForm.handoverRemark" type="textarea" :rows="2" placeholder="请输入交接说明" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="resignDialogVisible=false">取消</el-button><el-button type="primary" :loading="resignSubmitLoading" @click="handleSubmitResign">提交审批</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const activeTab = ref('entry')

const statusMap: Record<number, string> = { 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已撤回' }
const statusTypeMap: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
const entryStatusType = (v?: number) => v != null ? (statusTypeMap[v] ?? '') : ''
const transferStatusType = entryStatusType

/* ---- 入职 ---- */
const entryQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: entryRecords, total: entryTotal, loading: entryLoading, loadData: loadEntryData } = useTable(api.getEntryPageApi, entryQuery)
const entryDialogVisible = ref(false), entrySubmitLoading = ref(false), entryFormRef = ref()
const entryForm = reactive<api.EntryApplyDTO>({ employeeNo: '', name: '', entryDate: '', employmentType: 1 })
const entryRules = { employeeNo: [{ required: true, message: '工号不能为空' }], name: [{ required: true, message: '姓名不能为空' }], entryDate: [{ required: true, message: '入职日期不能为空' }] }
const openAdd = (tab: string) => {
  if (tab === 'entry') { Object.assign(entryForm, { employeeNo: '', name: '', entryDate: '', employmentType: 1 }); entryDialogVisible.value = true }
  else if (tab === 'regular') { Object.assign(regularForm, { employeeId: 0, regularDate: '', remark: '' }); regularDialogVisible.value = true }
  else if (tab === 'transfer') { Object.assign(transferForm, { employeeId: 0, newOrgId: 0, newPostId: 0, transferDate: '', reason: '' }); transferDialogVisible.value = true }
  else if (tab === 'resign') { Object.assign(resignForm, { employeeId: 0, resignDate: '', resignType: 1, reason: '', handoverRemark: '' }); resignDialogVisible.value = true }
}
const handleSubmitEntry = async () => { await entryFormRef.value.validate(); entrySubmitLoading.value = true; try { await api.submitEntryApi(entryForm); ElMessage.success('提交成功'); entryDialogVisible.value = false; loadEntryData() } finally { entrySubmitLoading.value = false } }
const handleRevokeEntry = (row: api.EntryApplyVO) => { ElMessageBox.confirm('确认撤销?', '提示').then(async () => { await api.revokeEntryApi(row.id!); ElMessage.success('撤销成功'); loadEntryData() }) }

/* ---- 转正 ---- */
const regularQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: regularRecords, total: regularTotal, loading: regularLoading, loadData: loadRegularData } = useTable(api.getRegularPageApi, regularQuery)
const regularDialogVisible = ref(false), regularSubmitLoading = ref(false), regularFormRef = ref()
const regularForm = reactive<api.RegularApplyDTO>({ employeeId: 0, regularDate: '', remark: '' })
const regularRules = { employeeId: [{ required: true, message: '员工ID不能为空' }], regularDate: [{ required: true, message: '转正日期不能为空' }] }
const handleSubmitRegular = async () => { await regularFormRef.value.validate(); regularSubmitLoading.value = true; try { await api.submitRegularApi(regularForm); ElMessage.success('提交成功'); regularDialogVisible.value = false; loadRegularData() } finally { regularSubmitLoading.value = false } }
const handleRevokeRegular = (row: api.RegularApplyVO) => { ElMessageBox.confirm('确认撤销?', '提示').then(async () => { await api.revokeRegularApi(row.id!); ElMessage.success('撤销成功'); loadRegularData() }) }

/* ---- 调岗 ---- */
const transferQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: transferRecords, total: transferTotal, loading: transferLoading, loadData: loadTransferData } = useTable(api.getTransferPageApi, transferQuery)
const transferDialogVisible = ref(false), transferSubmitLoading = ref(false), transferFormRef = ref()
const transferForm = reactive<api.TransferApplyDTO>({ employeeId: 0, newOrgId: 0, newPostId: 0, transferDate: '', reason: '' })
const transferRules = { employeeId: [{ required: true, message: '员工ID不能为空' }], newOrgId: [{ required: true, message: '新组织ID不能为空' }], newPostId: [{ required: true, message: '新岗位ID不能为空' }], transferDate: [{ required: true, message: '调岗日期不能为空' }] }
const handleSubmitTransfer = async () => { await transferFormRef.value.validate(); transferSubmitLoading.value = true; try { await api.submitTransferApi(transferForm); ElMessage.success('提交成功'); transferDialogVisible.value = false; loadTransferData() } finally { transferSubmitLoading.value = false } }
const handleRevokeTransfer = (row: api.TransferApplyVO) => { ElMessageBox.confirm('确认撤销?', '提示').then(async () => { await api.revokeTransferApi(row.id!); ElMessage.success('撤销成功'); loadTransferData() }) }

/* ---- 离职 ---- */
const resignQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: resignRecords, total: resignTotal, loading: resignLoading, loadData: loadResignData } = useTable(api.getResignPageApi, resignQuery)
const resignDialogVisible = ref(false), resignSubmitLoading = ref(false), resignFormRef = ref()
const resignForm = reactive<api.ResignApplyDTO>({ employeeId: 0, resignDate: '', resignType: 1, reason: '', handoverRemark: '' })
const resignRules = { employeeId: [{ required: true, message: '员工ID不能为空' }], resignDate: [{ required: true, message: '离职日期不能为空' }], resignType: [{ required: true, message: '离职类型不能为空' }] }
const handleSubmitResign = async () => { await resignFormRef.value.validate(); resignSubmitLoading.value = true; try { await api.submitResignApi(resignForm); ElMessage.success('提交成功'); resignDialogVisible.value = false; loadResignData() } finally { resignSubmitLoading.value = false } }
const handleRevokeResign = (row: api.ResignApplyVO) => { ElMessageBox.confirm('确认撤销?', '提示').then(async () => { await api.revokeResignApi(row.id!); ElMessage.success('撤销成功'); loadResignData() }) }
</script>
