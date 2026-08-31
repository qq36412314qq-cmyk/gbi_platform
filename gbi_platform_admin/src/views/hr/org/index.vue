<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">组织岗位</span>
      <AuthBtn permission="hr:org:post:add" type="primary" @click="openAddPost">新增岗位</AuthBtn>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
          <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="postCode" label="岗位编码" width="130" align="center" />
        <el-table-column prop="postName" label="岗位名称" width="150" align="center" />
        <el-table-column prop="postLevel" label="岗位职级" width="120" align="center" />
        <el-table-column prop="statusText" label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.statusText || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <AuthBtn permission="hr:org:post:edit" link type="primary" size="small" @click="openEditPost(row)">编辑</AuthBtn>
            <AuthBtn permission="hr:org:post:delete" link type="danger" size="small" @click="handleDeletePost(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>
    <el-dialog v-model="postDialogVisible" :title="postForm.id ? '编辑岗位' : '新增岗位'" width="480px" :close-on-click-modal="false">
      <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-width="90px">
        <el-form-item label="岗位编码" prop="postCode"><el-input v-model="postForm.postCode" placeholder="请输入" /></el-form-item>
        <el-form-item label="岗位名称" prop="postName"><el-input v-model="postForm.postName" placeholder="请输入" /></el-form-item>
        <el-form-item label="岗位职级"><el-input v-model="postForm.postLevel" placeholder="如：P3/M2" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="postForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="postForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="postDialogVisible=false">取消</el-button><el-button type="primary" :loading="postSubmitLoading" @click="handlePostSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'

const { query, records, total, loading, loadData } = useTable(api.getPostPageApi, { pageNum: 1, pageSize: 20, status: undefined })
const postDialogVisible = ref(false)
const postSubmitLoading = ref(false)
const postFormRef = ref()
const postForm = reactive<api.PostDTO>({ postCode: '', postName: '', status: 1 })
const postRules = { postCode: [{ required: true, message: '岗位编码不能为空' }], postName: [{ required: true, message: '岗位名称不能为空' }] }

const openAddPost = () => { Object.assign(postForm, { id: undefined, postCode: '', postName: '', postLevel: '', status: 1, remark: '' }); postDialogVisible.value = true }
const openEditPost = (row: api.PostVO) => { Object.assign(postForm, { id: row.id, postCode: row.postCode, postName: row.postName, postLevel: row.postLevel, status: row.status, remark: row.remark }); postDialogVisible.value = true }
const handleDeletePost = (row: api.PostVO) => { ElMessageBox.confirm('确认删除该岗位?', '提示').then(async () => { await api.deletePostApi(row.id!); ElMessage.success('删除成功'); loadData() }) }
const handlePostSubmit = async () => { await postFormRef.value.validate(); postSubmitLoading.value = true; try { if (postForm.id) { await api.updatePostApi(postForm); ElMessage.success('编辑成功') } else { await api.addPostApi(postForm); ElMessage.success('新增成功') } postDialogVisible.value = false; loadData() } finally { postSubmitLoading.value = false } }
const handleReset = () => { loadData() }
</script>
