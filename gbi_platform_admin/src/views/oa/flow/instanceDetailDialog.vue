<template>
  <el-dialog v-model="visible" title="流程实例详情" width="720px" destroy-on-close>
    <div v-if="detail" v-loading="loading" class="flow-detail-wrap">
      <!-- 实例信息 -->
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="实例编号">{{ detail.instance.instanceNo }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ detail.instance.defName }}</el-descriptions-item>
        <el-descriptions-item label="审批标题" :span="2">{{ detail.instance.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.instance.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagType(detail.instance.instanceStatus ?? 0)">{{ detail.instance.instanceStatusText || instanceStatusMap[detail.instance.instanceStatus ?? 0] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detail.instance.currentNodeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.instance.submitTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审批任务 -->
      <div class="flow-section-title">审批任务</div>
      <el-table :data="detail.tasks" border size="small">
        <el-table-column prop="nodeName" label="节点" min-width="110" />
        <el-table-column prop="handlerName" label="审批人" width="100" />
        <el-table-column label="任务状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.taskStatus === 0 ? 'warning' : 'info'">{{ taskStatusMap[row.taskStatus ?? 0] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批结果" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.approveResult === 1" class="g-text-success">通过</span>
            <span v-else-if="row.approveResult === 0" class="g-text-danger">驳回</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="opinion" label="审批意见" min-width="140" show-overflow-tooltip />
        <el-table-column prop="handleTime" label="处理时间" width="150" />
      </el-table>

      <!-- 流转轨迹 -->
      <div class="flow-section-title">流转轨迹</div>
      <el-timeline v-if="detail.records.length" class="flow-timeline">
        <el-timeline-item
          v-for="record in detail.records"
          :key="record.id"
          :timestamp="record.createTime || ''"
          :type="record.action === 'reject' ? 'danger' : 'primary'"
        >
          <span class="g-text-strong">{{ record.actionText || record.action }}</span>
          <span v-if="record.nodeName">（{{ record.nodeName }}）</span>
          <span v-if="record.handlerName"> - {{ record.handlerName }}</span>
          <span v-if="record.comment" class="flow-record-comment">：{{ record.comment }}</span>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无流转记录" :image-size="60" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 流程实例详情弹窗：实例信息 + 审批任务 + 流转轨迹（共用组件，我的申请/流程实例页复用）
 */
import { ref, watch } from 'vue'
import { getFlowInstanceDetailApi, type FlowInstanceDetailVO } from '@/api/flow'

const props = defineProps<{ modelValue: boolean; instanceId: number }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: boolean): void }>()

const visible = ref(false)
const loading = ref(false)
const detail = ref<FlowInstanceDetailVO | null>(null)

const instanceStatusMap: Record<number, string> = { 0: '审批中', 1: '通过', 2: '驳回', 3: '撤回', 4: '终止' }
const taskStatusMap: Record<number, string> = { 0: '待办', 1: '已办', 2: '已转交', 3: '已作废' }

function statusTagType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2 || status === 4) return 'danger'
  return 'info'
}

watch(
  () => props.modelValue,
  async (val) => {
    visible.value = val
    if (val && props.instanceId) {
      loading.value = true
      try {
        detail.value = await getFlowInstanceDetailApi(props.instanceId)
      } finally {
        loading.value = false
      }
    }
  }
)

watch(visible, (val) => emit('update:modelValue', val))
</script>

<style scoped>
.flow-section-title {
  margin: 14px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.flow-timeline {
  padding-left: 4px;
}

.flow-record-comment {
  color: var(--el-text-color-secondary);
}
</style>