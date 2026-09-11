<template>
  <div class="g-page-wrap role-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">角色管理</span>
      <div>
        <AuthBtn permission="role:add" type="primary" @click="openDialog()">新增角色</AuthBtn>
      </div>
    </div>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="companyId" label="所属公司" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.companyId === 0 ? 'primary' : 'success'">
              {{ row.companyId === 0 ? '集团全局' : `子公司${row.companyId}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="role:edit" size="small" type="primary" link @click="openMenuDialog(row)">
              菜单授权
            </AuthBtn>
            <AuthBtn permission="role:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="role:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 弹窗区：新增/编辑角色 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑角色' : '新增角色'"
      width="520px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="如 super_admin，全小写下划线" maxlength="64" />
        </el-form-item>
        <el-form-item label="所属公司" prop="companyId">
          <el-select v-model="form.companyId" style="width: 100%">
            <el-option label="集团全局角色模板" :value="0" />
            <el-option label="子公司角色" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="角色说明" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 弹窗区：菜单授权 -->
    <CommonDialog
      v-model="menuDialogVisible"
      :title="`菜单授权 - ${currentRole?.roleName || ''}`"
      width="420px"
      :loading="menuSubmitLoading"
      @confirm="handleSaveMenus"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 角色管理页：sys_role + sys_role_menu_rel 菜单授权（RBAC 核心）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import {
  getRolePageApi,
  addRoleApi,
  updateRoleApi,
  deleteRoleApi,
  getRoleMenusApi,
  saveRoleMenusApi,
  getMenuTreeApi,
  type RoleDTO,
  type RoleVO,
  type MenuVO
} from '@/api/org'
import { useTable } from '@/hooks/useTable'
import { formatDateTime } from '@/utils/format'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData } = useTable<RoleVO>(getRolePageApi)

/* ---------------- 新增/编辑角色 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<RoleDTO>({
  companyId: 0,
  roleName: '',
  roleCode: '',
  remark: ''
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '角色编码须为小写字母/数字/下划线', trigger: 'blur' }
  ]
}

function openDialog(_parentId?: number, row?: RoleVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id,
    companyId: row?.companyId ?? 0,
    roleName: row?.roleName ?? '',
    roleCode: row?.roleCode ?? '',
    remark: row?.remark ?? ''
  })
  dialogVisible.value = true
}

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
        await updateRoleApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addRoleApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(row: RoleVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await deleteRoleApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/* ---------------- 菜单授权 ---------------- */
const menuDialogVisible = ref(false)
const menuSubmitLoading = ref(false)
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const menuTree = ref<MenuVO[]>([])
const currentRole = ref<RoleVO | null>(null)

async function openMenuDialog(row: RoleVO): Promise<void> {
  currentRole.value = row
  menuDialogVisible.value = true
  // 加载菜单树（无缓存时拉取）
  if (menuTree.value.length === 0) {
    menuTree.value = await getMenuTreeApi()
  }
  // 回显已授权菜单（过滤掉父级文件夹节点，防止子节点全选）
  const checkedIds = (await getRoleMenusApi(row.id)).filter(
    (id) => !menuTree.value.some((n) => n.id === id && n.children && n.children.length > 0)
  )
  menuTreeRef.value?.setCheckedKeys(checkedIds)
}
async function handleSaveMenus(): Promise<void> {
  if (!currentRole.value || !menuTreeRef.value) {
    return
  }
  menuSubmitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys(false) as number[]
    // 只授权叶节点（有 permission 的实际菜单），过滤掉父级分组节点
    const leafNodeIds = new Set(menuTree.value.flatMap((node) => node.children ? node.children.map((c) => c.id) : [node.id]))
    const filteredCheckedKeys = checkedKeys.filter((id) => leafNodeIds.has(id))
    // 过滤掉 halfCheckedKeys 中的父节点（folder类型），只保留叶节点，避免再次写入脏数据
    const halfCheckedIdSet = new Set(menuTree.value.flatMap((node) => node.children ? node.children.map((c) => c.id) : [node.id]))
    const halfCheckedKeys = (menuTreeRef.value.getHalfCheckedKeys() as number[]).filter((id) => halfCheckedIdSet.has(id))
    // 已过滤父节点，仅提交叶节点
    await saveRoleMenusApi(currentRole.value.id, [...filteredCheckedKeys, ...halfCheckedKeys])
    ElMessage.success('授权成功')
    menuDialogVisible.value = false
  } finally {
    menuSubmitLoading.value = false
  }
}

onMounted(loadData)
</script>
