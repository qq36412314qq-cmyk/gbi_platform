<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">年度基数重算</span>
    </div>
    <el-card>
      <el-descriptions title="操作说明" :column="1" border>
        <el-descriptions-item label="功能描述">每年7月执行上年度工资月均重算，自动更新员工社保/公积金申报基数</el-descriptions-item>
        <el-descriptions-item label="重算逻辑">取上年1-12月全部税前收入总和 / 实际发薪月份数，经城市上下限截断后锁定为新基数</el-descriptions-item>
        <el-descriptions-item label="注意事项">重算结果为幂等操作，已处理过的年度员工将自动跳过</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <el-form :model="form" label-width="120px" style="max-width:400px">
        <el-form-item label="重算年度">
          <el-input-number v-model="form.recalcYear" :min="2020" :max="2030" style="width:120px" />
          <span style="margin-left:8px;color:#999">（将重算该年度的基数，生效周期为当年7月至次年6月）</span>
        </el-form-item>
        <el-form-item label="执行公司">
          <el-select v-model="form.companyId" placeholder="全部公司" clearable style="width:200px">
            <el-option label="集团全局" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRecalc">执行重算</el-button>
        </el-form-item>
      </el-form>
      <el-divider v-if="result" />
      <el-alert v-if="result" :title="`重算完成：总数 ${result.totalCount}，成功 ${result.successCount}，失败 ${result.failCount}`" type="success" :closable="false" show-icon />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from '@/api/hrSocialParam'

const form = reactive({ recalcYear: new Date().getFullYear() - 1, companyId: undefined as number | undefined } as any)
const loading = ref(false)
const result = ref<any>(null)

const handleRecalc = async () => {
  loading.value = true
  try {
    const res = await api.triggerAnnualRecalcApi(form)
    result.value = res.data
    ElMessage.success('重算任务执行完成')
  } catch (e: any) {
    ElMessage.error(e?.msg || '执行失败')
  } finally { loading.value = false }
}
</script>
