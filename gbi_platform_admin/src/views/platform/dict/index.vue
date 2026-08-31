<template>
  <div class="g-page-wrap dict-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">字典管理</span>
      <div>
        <AuthBtn permission="dict:add" type="primary" @click="openTypeDialog()">新增字典类型</AuthBtn>
      </div>
    </div>

    <!-- 双栏布局：左侧类型 / 右侧数据 -->
    <el-row :gutter="16">
      <!-- 左侧：字典类型列表 -->
      <el-col :span="10">
        <div class="g-table-card">
          <el-table v-loading="typeLoading" :data="typeRecords" highlight-current-row @current-change="handleTypeChange">
            <el-table-column prop="dictName" label="字典名称" min-width="120" />
            <el-table-column prop="dictCode" label="编码" min-width="140" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <AuthBtn permission="dict:edit" size="small" type="primary" link @click="openTypeDialog(undefined, row)">
                  编辑
                </AuthBtn>
                <AuthBtn permission="dict:delete" size="small" type="danger" link @click="handleDeleteType(row)">
                  删除
                </AuthBtn>
              </template>
            </el-table-column>
          </el-table>
          <div class="g-pagination">
            <el-pagination
              v-model:current-page="typeQuery.pageNum"
              v-model:page-size="typeQuery.pageSize"
              :total="typeTotal"
              :page-sizes="[10, 20]"
              layout="total, sizes, prev, pager, next"
              background
              small
              @size-change="loadTypes"
              @current-change="loadTypes"
            />
          </div>
        </div>
      </el-col>

      <!-- 右侧：当前类型的数据项 -->
      <el-col :span="14">
        <div class="g-table-card">
          <div class="g-dict-data-header">
            <span class="g-dict-data-title">
              字典数据{{ currentType ? ` - ${currentType.dictName}` : '' }}
            </span>
            <AuthBtn permission="dict:add" size="small" type="primary" :disabled="!currentType" @click="openDataDialog()">
              新增数据
            </AuthBtn>
          </div>
          <el-table v-loading="dataLoading" :data="dataRecords" border stripe>
            <el-table-column prop="dictKey" label="存储值" min-width="100" />
            <el-table-column prop="dictValue" label="显示文本" min-width="120" />
            <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <AuthBtn permission="dict:edit" size="small" type="primary" link @click="openDataDialog(undefined, row)">
                  编辑
                </AuthBtn>
                <AuthBtn permission="dict:delete" size="small" type="danger" link @click="handleDeleteData(row)">
                  删除
                </AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 弹窗：字典类型 -->
    <CommonDialog
      v-model="typeDialogVisible"
      :title="typeForm.id ? '编辑字典类型' : '新增字典类型'"
      width="520px"
      :loading="submitLoading"
      @confirm="handleSubmitType"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如 摊位状态" maxlength="128" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input
            v-model="typeForm.dictCode"
            placeholder="如 stall_status，小写下划线"
            :disabled="!!typeForm.id"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
    </CommonDialog>

    <!-- 弹窗：字典数据 -->
    <CommonDialog
      v-model="dataDialogVisible"
      :title="dataForm.id ? '编辑字典数据' : '新增字典数据'"
      width="480px"
      :loading="submitLoading"
      @confirm="handleSubmitData"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="存储值" prop="dictKey">
          <el-input v-model="dataForm.dictKey" placeholder="如 0" maxlength="128" />
        </el-form-item>
        <el-form-item label="显示文本" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如 空置" maxlength="128" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dataForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
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
 * 字典管理页：sys_dict_type（集团全局）+ sys_dict_data 双栏维护
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getDictTypePageApi,
  addDictTypeApi,
  updateDictTypeApi,
  deleteDictTypeApi,
  getDictDataListApi,
  addDictDataApi,
  updateDictDataApi,
  deleteDictDataApi,
  type DictTypeDTO,
  type DictTypeVO,
  type DictDataDTO
} from '@/api/base'
import type { DictItemVO } from '@/api/base'
import { useDictStore } from '@/store/dict'

const dictStore = useDictStore()

/* ---------------- 字典类型 ---------------- */
const typeLoading = ref(false)
const typeRecords = ref<DictTypeVO[]>([])
const typeTotal = ref(0)
const typeQuery = reactive({ pageNum: 1, pageSize: 10 })
const currentType = ref<DictTypeVO | null>(null)

async function loadTypes(): Promise<void> {
  typeLoading.value = true
  try {
    const data = await getDictTypePageApi({ ...typeQuery })
    typeRecords.value = data.records || []
    typeTotal.value = data.total || 0
    // 默认选中第一行
    if (!currentType.value && typeRecords.value.length > 0) {
      currentType.value = typeRecords.value[0]
      loadDataList()
    }
  } finally {
    typeLoading.value = false
  }
}

/** 切换类型：加载对应数据 */
function handleTypeChange(row: DictTypeVO | null): void {
  currentType.value = row
  if (row) {
    loadDataList()
  } else {
    dataRecords.value = []
  }
}

/* ---------------- 字典数据 ---------------- */
const dataLoading = ref(false)
const dataRecords = ref<DictItemVO[]>([])

async function loadDataList(): Promise<void> {
  if (!currentType.value) {
    return
  }
  dataLoading.value = true
  try {
    dataRecords.value = await getDictDataListApi(currentType.value.id)
  } finally {
    dataLoading.value = false
  }
}

/* ---------------- 类型新增/编辑 ---------------- */
const typeDialogVisible = ref(false)
const submitLoading = ref(false)
const typeFormRef = ref<FormInstance>()

const typeForm = reactive<DictTypeDTO>({
  dictCode: '',
  dictName: '',
  status: 1,
  remark: ''
})

const typeRules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '编码须为小写字母/数字/下划线', trigger: 'blur' }
  ]
}

function openTypeDialog(_parentId?: number, row?: DictTypeVO): void {
  typeFormRef.value?.clearValidate()
  Object.assign(typeForm, {
    id: row?.id,
    dictCode: row?.dictCode ?? '',
    dictName: row?.dictName ?? '',
    status: row?.status ?? 1,
    remark: row?.remark ?? ''
  })
  typeDialogVisible.value = true
}

async function handleSubmitType(): Promise<void> {
  if (!typeFormRef.value) {
    return
  }
  await typeFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    submitLoading.value = true
    try {
      if (typeForm.id) {
        await updateDictTypeApi({ ...typeForm })
        ElMessage.success('修改成功')
      } else {
        await addDictTypeApi({ ...typeForm })
        ElMessage.success('新增成功')
      }
      typeDialogVisible.value = false
      dictStore.clearDict(typeForm.dictCode)
      loadTypes()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDeleteType(row: DictTypeVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除字典「${row.dictName}」吗？其下数据一并删除`, '提示', { type: 'warning' })
  await deleteDictTypeApi(row.id)
  ElMessage.success('删除成功')
  dictStore.clearDict(row.dictCode)
  if (currentType.value?.id === row.id) {
    currentType.value = null
    dataRecords.value = []
  }
  loadTypes()
}

/* ---------------- 数据新增/编辑 ---------------- */
const dataDialogVisible = ref(false)
const dataFormRef = ref<FormInstance>()

const dataForm = reactive<DictDataDTO>({
  dictTypeId: 0,
  dictKey: '',
  dictValue: '',
  sortOrder: 0,
  status: 1
})

const dataRules: FormRules = {
  dictKey: [{ required: true, message: '请输入存储值', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入显示文本', trigger: 'blur' }]
}

function openDataDialog(_parentId?: number, row?: DictItemVO): void {
  if (!currentType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  dataFormRef.value?.clearValidate()
  Object.assign(dataForm, {
    id: row?.id,
    dictTypeId: currentType.value.id,
    dictKey: row?.dictKey ?? '',
    dictValue: row?.dictValue ?? '',
    sortOrder: row?.sortOrder ?? 0,
    status: row?.status ?? 1
  })
  dataDialogVisible.value = true
}

async function handleSubmitData(): Promise<void> {
  if (!dataFormRef.value) {
    return
  }
  await dataFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    submitLoading.value = true
    try {
      if (dataForm.id) {
        await updateDictDataApi({ ...dataForm })
        ElMessage.success('修改成功')
      } else {
        await addDictDataApi({ ...dataForm })
        ElMessage.success('新增成功')
      }
      dataDialogVisible.value = false
      if (currentType.value) {
        dictStore.clearDict(currentType.value.dictCode)
      }
      loadDataList()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDeleteData(row: DictItemVO): Promise<void> {
  await ElMessageBox.confirm(`确定删除字典数据「${row.dictValue}」吗？`, '提示', { type: 'warning' })
  await deleteDictDataApi(row.id)
  ElMessage.success('删除成功')
  if (currentType.value) {
    dictStore.clearDict(currentType.value.dictCode)
  }
  loadDataList()
}

onMounted(loadTypes)
</script>

<style scoped>
.g-dict-data-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.g-dict-data-title {
  font-size: var(--font-size-base);
  font-weight: 600;
}
</style>