<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">公告管理</span>
      <div>
        <AuthBtn permission="oa:announcement:add" type="primary" @click="openAddDialog">发布公告</AuthBtn>
      </div>
    </div>

    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="发布状态">
        <el-select v-model="query.publishStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已撤回" :value="2" />
        </el-select>
      </el-form-item>
    </SearchBar>

    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="100" align="center" />
        <el-table-column label="发布范围" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.publishType === 1 ? '全员' : row.publishType === 2 ? '指定部门' : '指定人员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.publishStatus === 1 ? 'success' : row.publishStatus === 0 ? 'warning' : row.publishStatus === 2 ? 'info' : 'info'">{{ row.publishStatusText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readCount" label="已读人数" width="90" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.publishStatus === 0 || row.publishStatus === 4" type="primary" link size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.publishStatus === 1" type="warning" link size="small" @click="handleRecall(row)">撤回</el-button>
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 编辑/创建弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="发布范围" prop="publishType">
          <el-radio-group v-model="form.publishType">
            <el-radio :value="1">全员</el-radio>
            <el-radio :value="2">指定部门</el-radio>
            <el-radio :value="3">指定人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="公告详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ viewData.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ viewData.publishTime }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap">{{ viewData.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAnnouncementPageApi, createAnnouncementApi, publishAnnouncementApi, recallAnnouncementApi, type AnnouncementVO, type CreateAnnouncementDTO } from '@/api/oa'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const records = ref<AnnouncementVO[]>([])
const dialogVisible = ref(false)
const viewVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const viewData = ref<AnnouncementVO>({} as AnnouncementVO)

const query = reactive({ pageNum: 1, pageSize: 20, publishStatus: undefined as number | undefined })

const form = reactive({ id: undefined as number | undefined, title: '', content: '', publishType: 1 as number, publisherId: 0, publisherName: '' })
const rules = { title: [{ required: true, message: '请输入标题' }], content: [{ required: true, message: '请输入内容' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await getAnnouncementPageApi(query)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleReset() { Object.assign(query, { pageNum: 1, pageSize: 20, publishStatus: undefined }) }
function openAddDialog() { isEdit.value = false; Object.assign(form, { id: undefined, title: '', content: '', publishType: 1, publisherId: userStore.userInfo?.id ?? 0, publisherName: userStore.userInfo?.username ?? '' }); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const dto: CreateAnnouncementDTO = {
      companyId: userStore.userInfo?.companyId ?? 0,
      publisherId: form.publisherId,
      publisherName: form.publisherName,
      title: form.title,
      content: form.content,
      publishType: form.publishType
    }
    const id = await createAnnouncementApi(dto)
    ElMessage.success(isEdit.value ? '公告已更新' : '公告已创建，请发布')
    dialogVisible.value = false
    await loadData()
    } catch (err) {
    ElMessage.error((err as Error).message || '保存失败，请稍后重试')
  } finally { submitLoading.value = false }
}

async function handlePublish(row: AnnouncementVO) { await publishAnnouncementApi(row.id); ElMessage.success('已发布'); loadData() }
async function handleRecall(row: AnnouncementVO) { await recallAnnouncementApi(row.id); ElMessage.success('已撤回'); loadData() }
function handleView(row: AnnouncementVO) { viewData.value = row; viewVisible.value = true }

onMounted(loadData)
</script>

<style scoped>
.g-page-wrap { padding: 20px; }
.g-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.g-page-title { font-size: 18px; font-weight: 600; }
</style>
