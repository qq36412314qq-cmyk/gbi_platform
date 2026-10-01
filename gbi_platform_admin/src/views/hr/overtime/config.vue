<!--
  加班配置页面
  数据表: sys_overtime_config
  权限: hr:overtime:config:*
-->
<template>
  <div class="overtime-page">
    <div class="page-header-row">
      <span class="page-title">加班参数配置</span>
      <AuthBtn permission="hr:overtime:config:save" type="primary" :loading="saving" @click="handleSave">保存配置</AuthBtn>
    </div>

    <el-alert
      v-if="config?.status === 0"
      type="warning"
      :closable="false"
      style="margin-bottom:12px"
      description="当前加班配置已禁用，部分功能可能受限。请在下方启用后再使用。"
    />

    <el-card shadow="never">
      <el-form :model="form" label-width="160px" ref="formRef">
        <el-divider content-position="left">基础参数</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="配置名称">
              <el-input v-model="form.configName" placeholder="如：集团加班通用配置" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小加班时长（小时）">
              <el-input-number v-model="form.overtimeMinHours" :min="0.5" :step="0.5" style="width:120px" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">取整规则</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="取整模式">
              <el-radio-group v-model="form.overtimeRoundMode">
                <el-radio :value="1">向上取整</el-radio>
                <el-radio :value="2">四舍五入</el-radio>
                <el-radio :value="3">向下取整</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每月最大加班时长上限">
              <el-input-number v-model="form.maxOvertimeHours" :min="0" :step="1" placeholder="不限填0" style="width:120px" />
              <span style="margin-left:8px;color:#909399">小时（0 表示不限）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">加班费率（倍率）</el-divider>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="工作日加班倍率">
              <el-input-number v-model="form.workdayRate" :min="0" :step="0.5" :precision="2" style="width:150px" />
              <span style="margin-left:4px;color:#909399">倍（默认1.5）</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="休息日加班倍率">
              <el-input-number v-model="form.restdayRate" :min="0" :step="0.5" :precision="2" style="width:150px" />
              <span style="margin-left:4px;color:#909399">倍（默认2.0）</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="法定节假日倍率">
              <el-input-number v-model="form.holidayRate" :min="0" :step="0.5" :precision="2" style="width:150px" />
              <span style="margin-left:4px;color:#909399">倍（默认3.0）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">补偿与自动化</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="补偿优先级">
              <el-radio-group v-model="form.compensatePriority">
                <el-radio :value="1">调休优先</el-radio>
                <el-radio :value="2">加班费优先</el-radio>
                <el-radio :value="3">混合（按比例）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自动识别">
              <el-switch :model-value="config?.autoDetectEnabled ?? 0" disabled />
              <span style="margin-left:8px;color:#909399">启用后系统自动根据考勤识别加班记录（由全局参数控制）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">状态管理</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="配置状态">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
                @change="handleStatusChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from '@/api/hr'
import type { SysOvertimeConfigVO, SysOvertimeConfigSaveDTO } from '@/api/hr'
import AuthBtn from '@/components/common/AuthBtn.vue'

const config = ref<SysOvertimeConfigVO | null>(null)
const saving = ref(false)

const initForm = (): SysOvertimeConfigSaveDTO => ({
  id: 0, configName: '', overtimeMinHours: 0.5, overtimeRoundMode: 2,
  workdayRate: 1.5, restdayRate: 2.0, holidayRate: 3.0,
  maxOvertimeHours: 0, compensatePriority: 1, status: 1, remark: ''
})

const form = reactive<SysOvertimeConfigSaveDTO>(initForm())

const loadConfig = async () => {
  try {
    const cfg = await api.getOvertimeConfigApi()
    config.value = cfg
    Object.assign(form, {
      id: cfg.id, configName: cfg.configName ?? '',
      overtimeMinHours: cfg.overtimeMinHours ?? 0.5,
      overtimeRoundMode: cfg.overtimeRoundMode ?? 2,
      workdayRate: cfg.workdayRate ?? 1.5,
      restdayRate: cfg.restdayRate ?? 2.0,
      holidayRate: cfg.holidayRate ?? 3.0,
      maxOvertimeHours: cfg.maxOvertimeHours ?? 0,
      compensatePriority: cfg.compensatePriority ?? 1,
      status: cfg.status ?? 1,
      remark: cfg.remark ?? ''
    })
  } catch {
    Object.assign(form, initForm())
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await api.saveOvertimeConfigApi(form)
    ElMessage.success('配置保存成功')
    loadConfig()
  } finally { saving.value = false }
}

const handleStatusChange = async (val: number) => {
  try {
    await api.updateOvertimeConfigStatusApi(form.id, val)
    ElMessage.success(val === 1 ? '配置已启用' : '配置已禁用')
  } catch { form.status = val === 1 ? 0 : 1 }
}

onMounted(() => { loadConfig() })
</script>

<style scoped>
.overtime-page { padding: 0; }
.page-header-row { display: flex; align-items: center; justify-content: space-between; margin: 12px 0; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; }
:deep(.el-divider__text) { font-size: 14px; font-weight: 600; color: #303133; }
</style>
