<template>
  <div class="g-page-wrap user-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">用户管理</span>
      <div>
        <AuthBtn permission="user:add" type="primary" @click="openDialog()">新增用户</AuthBtn>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="登录账号">
        <el-input v-model="query.username" placeholder="请输入登录账号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.realName" placeholder="请输入姓名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="登录账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">{{ formatPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="roleNames" label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames || []" :key="role" size="small" class="b-user-role-tag">
              {{ role }}
            </el-tag>
            <span v-if="!row.roleNames || row.roleNames.length === 0" class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="user:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="user:edit" size="small" type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </AuthBtn>
            <AuthBtn permission="user:edit" size="small" type="primary" link @click="handleResetPwd(row)">
              重置密码
            </AuthBtn>
            <AuthBtn permission="user:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 弹窗区：新增/编辑 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" :disabled="!!form.id" maxlength="64" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password maxlength="32" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="32" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="128" />
        </el-form-item>
        <el-form-item label="所属角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 用户管理页：sys_user 账号维护（集团/子公司账号分层）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getUserPageApi,
  addUserApi,
  updateUserApi,
  deleteUserApi,
  resetUserPwdApi,
  changeUserStatusApi,
  getRoleListApi,
  type UserDTO,
  type UserVO,
  type RoleVO
} from '@/api/org'
import { useTable } from '@/hooks/useTable'
import { formatPhone, formatDateTime } from '@/utils/format'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<UserVO>(getUserPageApi, {
  username: undefined,
  realName: undefined,
  status: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 角色下拉 ---------------- */
const roleList = ref<RoleVO[]>([])

async function loadRoles(): Promise<void> {
  roleList.value = await getRoleListApi()
}

/* ---------------- 新增/编辑 ---------------- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<UserDTO>({
  companyId: 0,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 2, max: 64, message: '账号长度 2-64 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{validator: (rule, value, callback) => { if (value && !/^1[3-9]\\d{9}$/.test(value)) callback('手机号格式不正确'); else callback(); }, trigger: 'blur'}],
  email: [{validator: (rule, value, callback) => { if (value && !/^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$/.test(value)) callback('邮箱格式不正确'); else callback(); }, trigger: 'blur'}]
}

function openDialog(_parentId?: number, row?: UserVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: row?.id,
    companyId: row?.companyId ?? 0,
    username: row?.username ?? '',
    password: '',
    realName: row?.realName ?? '',
    phone: row?.phone ?? '',
    email: row?.email ?? '',
    status: row?.status ?? 1,
    roleIds: []
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
        await updateUserApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addUserApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

/* ---------------- 启停/重置密码/删除 ---------------- */
async function handleToggleStatus(row: UserVO): Promise<void> {
  const nextStatus = row.status === 1 ? 0 : 1
  await changeUserStatusApi(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

async function handleResetPwd(row: UserVO): Promise<void> {
  const { value } = await ElMessageBox.prompt(`请输入「${row.username}」的新密码`, '重置密码', {
    inputType: 'password',
    inputPattern: /^.{6,32}$/,
    inputErrorMessage: '密码长度 6-32 位',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await resetUserPwdApi(row.id, value)
  ElMessage.success('密码已重置')
}

async function handleDelete(row: UserVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteUserApi(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadRoles)
</script>

<style scoped>
.b-user-role-tag {
  margin-right: 4px;
}
</style>
