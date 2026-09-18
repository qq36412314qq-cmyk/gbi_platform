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

    <!-- 入职申请弹窗 -->
    <el-dialog v-model="entryDialogVisible" title="新增入职申请" width="800px" :close-on-click-modal="false">
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
            <el-form-item label="入职日期" prop="entryDate"><el-date-picker v-model="entryForm.entryDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用工类型" prop="employmentType">
              <el-select v-model="entryForm.employmentType" placeholder="请选择" style="width:100%">
                <el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属组织" prop="orgId">
          <el-tree-select
            v-model="entryForm.orgId"
            :data="orgTreeData"
            :props="{ label: 'orgName', value: 'id', children: 'children' }"
            check-strictly
            node-key="id"
            placeholder="请选择部门"
            clearable
            filterable
            style="width:100%"
            :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item label="目标岗位" prop="postId">
          <el-select v-model="entryForm.postId" placeholder="请先选择所属组织" clearable filterable style="width:100%" :loading="postLoading">
            <el-option v-for="post in filteredPostList" :key="post.id" :label="post.postName" :value="post.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="entryForm.basicSalary" :precision="2" :min="0" placeholder="请输入" style="width:100%" /></el-form-item>
        
        <!-- 工作经历 -->
        <el-form-item label="工作经历">
          <div style="width:100%">
            <el-table :data="workExps" border size="small" style="width:100%">
              <el-table-column prop="companyName" label="公司名称" min-width="120">
                <template #default="{ row, $index }">
                  <el-input v-model="row.companyName" placeholder="公司名称" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="position" label="职位" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.position" placeholder="职位" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="department" label="部门" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.department" placeholder="部门" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="startDate" label="开始时间" width="100">
                <template #default="{ row }">
                  <el-date-picker v-model="row.startDate" type="date" placeholder="开始时间" value-format="YYYY-MM" size="small" style="width:100%" />
                </template>
              </el-table-column>
              <el-table-column prop="endDate" label="结束时间" width="100">
                <template #default="{ row }">
                  <el-date-picker v-model="row.endDate" type="date" placeholder="结束时间" value-format="YYYY-MM" size="small" style="width:100%" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeWorkExpRow($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" link size="small" @click="addWorkExpRow" style="margin-top:8px">+ 添加工作经历</el-button>
          </div>
        </el-form-item>
        
        <!-- 学业经历 -->
        <el-form-item label="学业经历">
          <div style="width:100%">
            <el-table :data="eduExps" border size="small" style="width:100%">
              <el-table-column prop="schoolName" label="学校名称" min-width="120">
                <template #default="{ row, $index }">
                  <el-input v-model="row.schoolName" placeholder="学校名称" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="degree" label="学位" width="80">
                <template #default="{ row }">
                  <el-input v-model="row.degree" placeholder="学位" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="major" label="专业" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.major" placeholder="专业" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="startDate" label="入学时间" width="100">
                <template #default="{ row }">
                  <el-date-picker v-model="row.startDate" type="date" placeholder="入学时间" value-format="YYYY-MM" size="small" style="width:100%" />
                </template>
              </el-table-column>
              <el-table-column prop="graduationDate" label="毕业时间" width="100">
                <template #default="{ row }">
                  <el-date-picker v-model="row.graduationDate" type="date" placeholder="毕业时间" value-format="YYYY-MM" size="small" style="width:100%" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeEduExpRow($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" link size="small" @click="addEduExpRow" style="margin-top:8px">+ 添加学业经历</el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="备注"><el-input v-model="entryForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="entrySubmitLoading" @click="handleSubmitEntry">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看入职申请弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="入职申请详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工号">{{ viewRecord?.employeeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewRecord?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ viewRecord?.entryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用工类型">{{ employmentTypeText(viewRecord?.employmentType) }}</el-descriptions-item>
        <el-descriptions-item label="目标组织">{{ orgNameMap[viewRecord?.orgId ?? 0] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标岗位">{{ postNameMap[viewRecord?.postId ?? 0] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ viewRecord?.basicSalary ? '¥' + viewRecord.basicSalary : '-' }}</el-descriptions-item>
        <el-descriptions-item label="工资卡号">{{ viewRecord?.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewRecord?.statusText }}</el-descriptions-item>
        <el-descriptions-item label="流程实例ID">{{ viewRecord?.flowInstanceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewRecord?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import { getOrgTreeApi } from '@/api/org'

const activeTab = ref('entry')

/* ---- 组织树数据 ---- */
const orgTreeData = ref<any[]>([])
const orgNameMap = ref<Record<number, string>>({})

const loadOrgTree = async () => {
  try {
    const list = await getOrgTreeApi()
    orgTreeData.value = markDisabledNodes(list)
    // 构建 orgId -> orgName 映射
    const flatList: any[] = []
    const collect = (nodes: any[]) => {
      for (const n of nodes) {
        flatList.push(n)
        if (n.children) collect(n.children)
      }
    }
    collect(list)
    orgNameMap.value = Object.fromEntries(flatList.map((o: any) => [o.id, o.orgName]))
  } catch (e) {
    console.error('[transfer] 加载组织树失败', e)
  }
}

/** 为每个节点添加 disabled 字段：仅 orgType=3（部门）可选 */
function markDisabledNodes(nodes: any[]): any[] {
  return nodes.map(node => ({
    ...node,
    disabled: node.orgType !== 3,
    children: node.children ? markDisabledNodes(node.children) : undefined
  }))
}

/* ---- 岗位列表数据 ---- */
const allPostList = ref<api.HrPostVO[]>([])
const postLoading = ref(false)

const loadAllPosts = async () => {
  postLoading.value = true
  try {
    const result = await api.getPostFlatListApi()
    allPostList.value = result
  } finally {
    postLoading.value = false
  }
}

// 根据选定部门过滤岗位列表
const filteredPostList = computed(() => {
  if (!entryForm.orgId) return []
  return allPostList.value.filter(p => p.deptId === entryForm.orgId)
})

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
    Object.assign(entryForm, { employeeNo: '', name: '', entryDate: '', employmentType: 1, orgId: undefined, postId: undefined, basicSalary: undefined, remark: '' })
    entryDialogVisible.value = true
  }
}

// 工作经历/学业经历数据（临时存储，提交时序列化为experienceData）
const workExps = ref<Array<{ companyName: string; position?: string; department?: string; startDate: string; endDate?: string; isCurrent?: number; reasonForLeaving?: string; remark?: string }>>([])
const eduExps = ref<Array<{ schoolName: string; degree?: string; major?: string; educationLevel?: string; startDate: string; graduationDate?: string; isGraduated?: number; certificateNo?: string; remark?: string }>>([])

// 工作经历行操作
const addWorkExpRow = () => { workExps.value.push({ companyName: '', startDate: '' }) }
const removeWorkExpRow = (index: number) => { workExps.value.splice(index, 1) }

// 学业经历行操作
const addEduExpRow = () => { eduExps.value.push({ schoolName: '', startDate: '' }) }
const removeEduExpRow = (index: number) => { eduExps.value.splice(index, 1) }

const handleSubmitEntry = async () => {
  await entryFormRef.value.validate()
  const submitData = {
    ...entryForm,
    experienceData: JSON.stringify({ workExps: workExps.value, eduExps: eduExps.value })
  } as any
  entrySubmitLoading.value = true
  try {
    await api.submitEntryApi(submitData)
    ElMessage.success('提交成功')
    entryDialogVisible.value = false
    workExps.value = []
    eduExps.value = []
    loadEntryData()
  } finally { entrySubmitLoading.value = false }
}

const handleRevokeEntry = (row: api.HrEntryApplyVO) => {
  ElMessageBox.confirm('确认撤销该入职申请?', '提示').then(async () => {
    await api.revokeEntryApi(row.id!)
    ElMessage.success('撤销成功')
    loadEntryData()
  })
}

/* ---- 查看入职申请 ---- */
const viewDialogVisible = ref(false)
const viewRecord = ref<api.HrEntryApplyVO | null>(null)
const handleView = (row: api.HrEntryApplyVO) => { viewRecord.value = row; viewDialogVisible.value = true }

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

const handleRevokeRegular = (row: api.HrRegularApplyVO) => {
  ElMessageBox.confirm('确认撤销该转正申请?', '提示').then(async () => {
    await api.revokeRegularApi(row.id!)
    ElMessage.success('撤销成功')
    loadRegularData()
  })
}

const handleRevokeTransfer = (row: api.HrTransferApplyVO) => {
  ElMessageBox.confirm('确认撤销该调岗申请?', '提示').then(async () => {
    await api.revokeTransferApi(row.id!)
    ElMessage.success('撤销成功')
    loadTransferData()
  })
}

const handleRevokeResign = (row: api.HrResignApplyVO) => {
  ElMessageBox.confirm('确认撤销该离职申请?', '提示').then(async () => {
    await api.revokeResignApi(row.id!)
    ElMessage.success('撤销成功')
    loadResignData()
  })
}

onMounted(() => { loadOrgTree(); loadAllPosts(); loadEntryData() })
</script>
