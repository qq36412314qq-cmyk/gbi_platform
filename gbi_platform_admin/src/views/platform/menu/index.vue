<template>
  <div class="g-page-wrap menu-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">菜单权限</span>
      <div>
        <AuthBtn permission="menu:add" type="primary" @click="openDialog()">新增菜单</AuthBtn>
      </div>
    </div>

    <!-- 表格区：菜单树 -->
    <div class="g-table-card">
      <el-table v-loading="loading" :data="treeData" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="220" />
        <el-table-column prop="menuType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" size="small">
              {{ menuTypeText(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permission" label="权限标识" min-width="180">
          <template #default="{ row }">
            <span v-if="row.permission">{{ row.permission }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" min-width="160">
          <template #default="{ row }">
            <span v-if="row.path">{{ row.path }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="visible" label="显示" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="menu:add" size="small" type="primary" link @click="openDialog(row.id)">
              新增下级
            </AuthBtn>
            <AuthBtn permission="menu:edit" size="small" type="primary" link @click="openDialog(undefined, row)">
              编辑
            </AuthBtn>
            <AuthBtn permission="menu:delete" size="small" type="danger" link @click="handleDelete(row)">
              删除
            </AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 弹窗区：新增/编辑 -->
    <CommonDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑菜单' : '新增菜单'"
      width="560px"
      :loading="submitLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'menuName', value: 'id' }"
            check-strictly
            default-expand-all
            placeholder="请选择上级菜单（0 为顶级）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单页面</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如 user:list（模块:操作）" maxlength="128" />
        </el-form-item>
        <el-form-item label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="如 /platform/user" maxlength="256" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="Element Plus 图标组件名，如 User" maxlength="128" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </CommonDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 菜单权限页：sys_menu 树形维护（集团全局，权限标识 模块:操作）
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getMenuTreeApi,
  addMenuApi,
  updateMenuApi,
  deleteMenuApi,
  type MenuDTO,
  type MenuVO
} from '@/api/org'

const loading = ref(false)
const treeData = ref<MenuVO[]>([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<MenuDTO>({
  parentId: 0,
  menuName: '',
  permission: '',
  path: '',
  icon: '',
  sortOrder: 0,
  menuType: 1,
  visible: 1
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

async function loadTree(): Promise<void> {
  loading.value = true
  try {
    treeData.value = await getMenuTreeApi()
  } finally {
    loading.value = false
  }
}

function openDialog(parentId?: number, row?: MenuVO): void {
  formRef.value?.clearValidate()
  Object.assign(form, {
    id: undefined,
    parentId: parentId ?? row?.parentId ?? 0,
    menuName: row?.menuName ?? '',
    permission: row?.permission ?? '',
    path: row?.path ?? '',
    icon: row?.icon ?? '',
    sortOrder: row?.sortOrder ?? 0,
    menuType: row?.menuType ?? 1,
    visible: row?.visible ?? 1
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
        await updateMenuApi({ ...form })
        ElMessage.success('修改成功')
      } else {
        await addMenuApi({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadTree()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(row: MenuVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？删除后其子菜单一并失效`, '提示', {
    type: 'warning'
  })
  await deleteMenuApi(row.id)
  ElMessage.success('删除成功')
  loadTree()
}

function menuTypeText(type: number): string {
  const map: Record<number, string> = { 1: '目录', 2: '菜单', 3: '按钮' }
  return map[type] || '-'
}

function menuTypeTag(type: number): 'primary' | 'success' | 'info' {
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