<template>
  <div class="g-page-wrap org-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">组织管理</span>
      <div>
        <AuthBtn permission="org:add" type="primary" @click="openDialog()">新增组织</AuthBtn>
      </div>
    </div>

    <!-- 表格区：组织树 -->
    <div class="g-table-card">
      <el-table
        v-loading="loading"
        :data="treeData"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="orgName" label="组织名称" min-width="220" />
        <el-table-column prop="orgType" label="组织类型" width="120">
          <template #default="{ row }">
            <el-tag :type="orgTypeTag(row.orgType)" size="small">{{ orgTypeText(row.orgType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <AuthBtn permission="org:add" size="small" type="primary" link @click="openDialog(row.id)">
              新增下级
            </AuthBtn>
            <AuthBtn permission="org:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="org:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 弹窗区：新增/编辑 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑组织' : '新增组织'"
      width="520px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级组织" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'orgName', value: 'id' }"
            check-strictly
            default-expand-all
            placeholder="请选择上级组织（0 为顶级）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" style="width: 100%">
            <el-option label="集团" :value="1" />
            <el-option label="子公司" :value="2" />
            <el-option label="部门" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 组织管理页：sys_org 树形维护（集团全局，子公司只读）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getOrgTreeApi,
  addOrgApi,
  updateOrgApi,
  deleteOrgApi,
  type OrgDTO,
  type OrgVO
} from '@/api/org'

const loading = ref(false)
const treeData = ref<OrgVO[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<OrgDTO>({
  parentId: 0,
  orgName: '',
  orgType: 1,
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  orgType: [{ required: true, message: '请选择组织类型', trigger: 'change' }]
}

/** 加载组织树 */
async function loadTree(): Promise<void> {
  loading.value = true
  try {
    treeData.value = await getOrgTreeApi()
  } finally {
    loading.value = false
  }
}

/** 打开弹窗：parentId 新增下级 / row 编辑 */
function openDialog(parentId?: number, row?: OrgVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id ?? undefined,
    parentId: parentId ?? row?.parentId ?? (treeData.value[0]?.id ?? 0),
    orgName: row?.orgName ?? '',
    orgType: row?.orgType ?? 1,
    sortOrder: row?.sortOrder ?? 0,
    status: row?.status ?? 1
  })
  dialogVisible.value = true
}

/** 提交新增/编辑 */
async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    submitLoading.value = true
    try {
      if (form.id) {
        await updateOrgApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addOrgApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadTree()
    } finally {
      submitLoading.value = false
    }
  })
}

/** 删除组织（逻辑删除） */
async function handleDelete(row: OrgVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除组织「${row.orgName}」吗？`, '提示', { type: 'warning' })
  await deleteOrgApi(row.id)
  ElMessage.success('删除成功')
  loadTree()
}

/** 组织类型文案 */
function orgTypeText(type: number): string {
  const map: Record<number, string> = { 1: '集团', 2: '子公司', 3: '部门' }
  return map[type] || '-'
}

/** 组织类型标签色 */
function orgTypeTag(type: number): 'primary' | 'success' | 'info' {
  if (type === 1) {
    return 'primary'
  }
  if (type === 2) {
    return 'success'
  }
  return 'info'
}

onMounted(loadTree)
</script>
