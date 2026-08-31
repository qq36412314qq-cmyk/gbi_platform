<template>
  <div class="g-page-wrap perm-audit-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">权限复核</span>
      <el-tag type="warning" effect="plain">待办：{{ total }}</el-tag>
    </div>

    <!-- 搜索筛选区 -->
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="申请人">
        <el-input v-model="query.applyUserName" placeholder="请输入申请人" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="权限类型">
        <el-select v-model="query.permissionType" placeholder="全部" clearable style="width: 160px">
          <el-option label="角色权限变更" value="ROLE" />
          <el-option label="用户权限变更" value="USER" />
          <el-option label="权限继承变更" value="INHERIT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="待复核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <!-- 表格展示区 -->
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="applyUserName" label="申请人" min-width="100" />
        <el-table-column prop="permissionType" label="权限类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary" effect="plain">{{ permissionTypeText(row.permissionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="变更对象" min-width="120" show-overflow-tooltip />
        <el-table-column prop="changeDesc" label="变更说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditUserName" label="复核人" min-width="100">
          <template #default="{ row }">
            <span v-if="row.auditUserName">{{ row.auditUserName }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="复核时间" width="170">
          <template #default="{ row }">
            <span v-if="row.auditTime">{{ formatDateTime(row.auditTime) }}</span>
            <span v-else class="g-text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.auditStatus === 0">
              <AuthBtn permission="permission:audit:audit" size="small" type="success" link @click="handleAudit(row, 1)">
                通过
              </AuthBtn>
              <AuthBtn permission="permission:audit:audit" size="small" type="danger" link @click="handleAudit(row, 2)">
                驳回
              </AuthBtn>
            </template>
            <span v-else class="g-text-secondary">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
  </div>
</template>

<script setup lang="ts">
/**
 * 权限复核页：sys_permission_audit 二级复核
 * 仅审计角色可操作通过/驳回，操作后写入 sys_audit_log
 */
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPermissionAuditPageApi,
  auditPermissionApi,
  type PermissionAuditVO
} from '@/api/sys'
import { useTable } from '@/hooks/useTable'
import { formatDateTime } from '@/utils/format'

/* ---------------- 分页查询 ---------------- */
const { query, records, total, loading, loadData, resetQuery } = useTable<PermissionAuditVO>(getPermissionAuditPageApi, {
  applyUserName: undefined,
  permissionType: undefined,
  auditStatus: undefined
})

function handleReset(): void {
  resetQuery()
  loadData()
}

/* ---------------- 通过/驳回 ---------------- */
async function handleAudit(row: PermissionAuditVO, status: 1 | 2): Promise<void> {
  const actionText = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确定${actionText}申请「${row.changeDesc}」吗？该操作将写入审计日志`, '权限复核', {
    type: status === 1 ? 'warning' : 'error',
    confirmButtonText: actionText,
    cancelButtonText: '取消'
  })
  await auditPermissionApi(row.id, status, '')
  ElMessage.success(`${actionText}成功`)
  loadData()
}

/* ---------------- 文案映射 ---------------- */
function permissionTypeText(type: string): string {
  const map: Record<string, string> = { ROLE: '角色权限变更', USER: '用户权限变更', INHERIT: '权限继承变更' }
  return map[type] || type
}

function statusText(status: number): string {
  const map: Record<number, string> = { 0: '待复核', 1: '已通过', 2: '已驳回' }
  return map[status] || '-'
}

function statusTag(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 0) {
    return 'warning'
  }
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'danger'
  }
  return 'info'
}
</script>