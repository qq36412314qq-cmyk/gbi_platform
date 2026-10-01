<!--
  考勤配置管理页面
  数据表: sys_workweek_config, sys_holiday_config
  权限: hr:config:workweek, hr:config:holiday
-->
<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">考勤配置管理</span>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 休息日配置 -->
      <el-tab-pane label="休息日配置" name="workweek">
        <div class="g-page-header" style="margin-bottom:12px">
          <span></span>
          <div class="header-actions">
            <AuthBtn permission="hr:config:workweek" type="primary" @click="openWorkweekDialog()">新增配置</AuthBtn>
          </div>
        </div>
        <el-table v-loading="workweekLoading" :data="workweekList" border stripe size="small">
          <el-table-column prop="configName" label="配置名称" width="150" align="center" />
          <el-table-column prop="workweekTypeText" label="休息日类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.workweekTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="restDayPattern" label="休息日模式" width="150" align="center" show-overflow-tooltip />
          <el-table-column prop="statusText" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <AuthBtn permission="hr:config:workweek" link @click="openWorkweekDialog(row)">编辑</AuthBtn>
              <AuthBtn permission="hr:config:workweek" link @click="handleToggleWorkweekStatus(row)">启用/禁用</AuthBtn>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 节假日配置 -->
      <el-tab-pane label="节假日配置" name="holiday">
        <div class="g-page-header" style="margin-bottom:12px">
          <span></span>
          <div class="header-actions">
            <el-date-picker v-model="holidayYear" type="year" value-format="YYYY" placeholder="选择年份" style="width:120px;margin-right:8px" />
            <AuthBtn permission="hr:config:holiday" type="primary" @click="openHolidayDialog">新增节假日</AuthBtn>
            <AuthBtn permission="hr:config:holiday" @click="loadHolidays">刷新</AuthBtn>
          </div>
        </div>
        <el-table v-loading="holidayLoading" :data="holidayList" border stripe size="small">
          <el-table-column prop="holidayDate" label="日期" width="120" align="center" sortable />
          <el-table-column prop="holidayName" label="节假日名称" width="150" align="center" />
          <el-table-column prop="holidayTypeText" label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="holidayTypeTag(row.holidayType)" size="small">{{ row.holidayTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isWorkdayText" label="是否工作日" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isWorkday === 1 ? 'success' : 'info'" size="small">{{ row.isWorkdayText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="statusText" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <AuthBtn permission="hr:config:holiday" link @click="openHolidayDialog(row)">编辑</AuthBtn>
              <AuthBtn permission="hr:config:holiday" link @click="handleToggleHolidayStatus(row)">启用/禁用</AuthBtn>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 休息日配置弹窗 -->
    <el-dialog v-model="workweekDialogVisible" :title="workweekForm.id ? '编辑休息日配置' : '新增休息日配置'" width="480px" :close-on-click-modal="false">
      <el-form ref="workweekFormRef" :model="workweekForm" :rules="workweekRules" label-width="110px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="workweekForm.configName" placeholder="如：双休、单休" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="休息日类型" prop="workweekType">
          <el-select v-model="workweekForm.workweekType" placeholder="请选择" style="width:100%">
            <el-option label="单休" :value="1" />
            <el-option label="双休" :value="2" />
            <el-option label="做五休二" :value="3" />
            <el-option label="做六休一" :value="4" />
            <el-option label="综合工时" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="休息日模式" prop="restDayPattern">
          <el-input v-model="workweekForm.restDayPattern" placeholder="如：周六、周日" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="workweekForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workweekDialogVisible = false">取消</el-button>
        <AuthBtn permission="hr:config:workweek" type="primary" @click="confirmWorkweek" :loading="workweekSubmitting">确定</AuthBtn>
      </template>
    </el-dialog>

    <!-- 节假日配置弹窗 -->
    <el-dialog v-model="holidayDialogVisible" :title="holidayForm.id ? '编辑节假日' : '新增节假日'" width="480px" :close-on-click-modal="false">
      <el-form ref="holidayFormRef" :model="holidayForm" :rules="holidayRules" label-width="100px">
        <el-form-item label="节假日日期" prop="holidayDate">
          <el-date-picker v-model="holidayForm.holidayDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="节假日名称" prop="holidayName">
          <el-input v-model="holidayForm.holidayName" placeholder="如：国庆节" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="holidayType">
          <el-select v-model="holidayForm.holidayType" placeholder="请选择" style="width:100%">
            <el-option label="法定假日" :value="1" />
            <el-option label="调休日" :value="2" />
            <el-option label="补班日" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否工作日">
          <el-switch v-model="holidayForm.isWorkday" :active-value="1" :inactive-value="0" />
          <span style="margin-left:8px;color:#909399;font-size:12px">补班日需设为工作日</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="holidayForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="holidayDialogVisible = false">取消</el-button>
        <AuthBtn permission="hr:config:holiday" type="primary" @click="confirmHoliday" :loading="holidaySubmitting">确定</AuthBtn>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as api from '@/api/hr'

/* ---- Tab切换 ---- */
const activeTab = ref('workweek')

/* ---- 休息日配置 ---- */
const workweekLoading = ref(false)
const workweekList = ref<api.SysWorkweekConfigVO[]>([])
const workweekDialogVisible = ref(false)
const workweekSubmitting = ref(false)
const workweekFormRef = ref<FormInstance>()

const workweekForm = reactive<api.SysWorkweekConfigDTO>({
  id: undefined,
  configName: '',
  workweekType: 2,
  restDayPattern: '',
  remark: '',
  status: 1,
})

const workweekRules = computed<FormRules>(() => ({
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  workweekType: [{ required: true, message: '请选择休息日类型', trigger: 'change' }],
}))

const loadWorkweekList = async () => {
  workweekLoading.value = true
  try {
    workweekList.value = await api.getWorkweekConfigListApi()
  } finally {
    workweekLoading.value = false
  }
}

const openWorkweekDialog = (row?: api.SysWorkweekConfigVO) => {
  if (row) {
    workweekForm.id = row.id
    workweekForm.configName = row.configName
    workweekForm.workweekType = row.workweekType
    workweekForm.restDayPattern = row.restDayPattern ?? ''
    workweekForm.remark = row.remark ?? ''
    workweekForm.status = row.status
  } else {
    workweekForm.id = undefined
    workweekForm.configName = ''
    workweekForm.workweekType = 2
    workweekForm.restDayPattern = ''
    workweekForm.remark = ''
    workweekForm.status = 1
  }
  workweekDialogVisible.value = true
}

const confirmWorkweek = async () => {
  if (!workweekFormRef.value) return
  await workweekFormRef.value.validate(async (valid) => {
    if (!valid) return
    workweekSubmitting.value = true
    try {
      if (workweekForm.id) {
        await api.updateWorkweekConfigApi(workweekForm.id, workweekForm)
        ElMessage.success('更新成功')
      } else {
        await api.createWorkweekConfigApi(workweekForm)
        ElMessage.success('新增成功')
      }
      workweekDialogVisible.value = false
      loadWorkweekList()
    } finally {
      workweekSubmitting.value = false
    }
  })
}

const handleToggleWorkweekStatus = async (row: api.SysWorkweekConfigVO) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await api.updateWorkweekConfigApi(row.id, { ...row, status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadWorkweekList()
  } catch (e: any) {
    ElMessage.error('操作失败：' + (e?.message || ''))
  }
}

/* ---- 节假日配置 ---- */
const holidayLoading = ref(false)
const holidayList = ref<api.SysHolidayConfigVO[]>([])
const holidayYear = ref<string>(new Date().getFullYear().toString())
const holidayDialogVisible = ref(false)
const holidaySubmitting = ref(false)
const holidayFormRef = ref<FormInstance>()

const holidayForm = reactive<api.SysHolidayConfigDTO>({
  id: undefined,
  holidayDate: '',
  holidayName: '',
  holidayType: 1,
  isWorkday: 0,
  remark: '',
  status: 1,
})

const holidayRules = computed<FormRules>(() => ({
  holidayDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  holidayName: [{ required: true, message: '请输入节假日名称', trigger: 'blur' }],
  holidayType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}))

const loadHolidays = async () => {
  holidayLoading.value = true
  try {
    holidayList.value = await api.getHolidayConfigListApi({ year: holidayYear.value })
  } finally {
    holidayLoading.value = false
  }
}

const openHolidayDialog = (row?: api.SysHolidayConfigVO) => {
  if (row) {
    holidayForm.id = row.id
    holidayForm.holidayDate = row.holidayDate
    holidayForm.holidayName = row.holidayName
    holidayForm.holidayType = row.holidayType
    holidayForm.isWorkday = row.isWorkday
    holidayForm.remark = row.remark ?? ''
    holidayForm.status = row.status
  } else {
    holidayForm.id = undefined
    holidayForm.holidayDate = ''
    holidayForm.holidayName = ''
    holidayForm.holidayType = 1
    holidayForm.isWorkday = 0
    holidayForm.remark = ''
    holidayForm.status = 1
  }
  holidayDialogVisible.value = true
}

const confirmHoliday = async () => {
  if (!holidayFormRef.value) return
  await holidayFormRef.value.validate(async (valid) => {
    if (!valid) return
    holidaySubmitting.value = true
    try {
      if (holidayForm.id) {
        await api.updateHolidayConfigApi(holidayForm.id, holidayForm)
        ElMessage.success('更新成功')
      } else {
        await api.createHolidayConfigApi(holidayForm)
        ElMessage.success('新增成功')
      }
      holidayDialogVisible.value = false
      loadHolidays()
    } finally {
      holidaySubmitting.value = false
    }
  })
}

const handleToggleHolidayStatus = async (row: api.SysHolidayConfigVO) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await api.updateHolidayConfigApi(row.id, { ...row, status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadHolidays()
  } catch (e: any) {
    ElMessage.error('操作失败：' + (e?.message || ''))
  }
}

/* ---- 工具函数 ---- */
const holidayTypeTag = (v?: number) => {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'info' }
  return map[v ?? 0] ?? ''
}

onMounted(() => {
  loadWorkweekList()
  loadHolidays()
})
</script>

<style scoped>
.header-actions { display: flex; gap: 8px; align-items: center; }
</style>
