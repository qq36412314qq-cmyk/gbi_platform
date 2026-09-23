<template>
  <div class="g-page-wrap">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="入职申请" name="entry">
        <div class="g-page-header">
          <span class="g-page-title">入职申请</span>
          <AuthBtn permission="hr:entry:add" type="primary" @click="openAdd('entry')">新增入职</AuthBtn>
        </div>
        <SearchBar :model="entryQuery" @search="loadEntryData" @reset="() => { entryQuery.pageNum = 1; loadEntryData() }">
          <el-form-item label="状态">
            <el-select v-model="entryQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="entryQuery.pageNum" v-model:page-size="entryQuery.pageSize" :total="entryTotal" @refresh="loadEntryData">
          <el-table v-loading="entryLoading" :data="entryRecords" border stripe>
            <el-table-column prop="photoFileId" label="免冠照片" width="80" align="center">
              <template #default="{ row }">
                <el-image
                  v-if="row.photoPreviewUrl"
                  :src="row.photoPreviewUrl" fit="cover"
                  style="width:44px;height:56px;border-radius:3px"
                  :preview-src-list="[row.photoPreviewUrl]"
                  preview-teleported
                />
                <span v-else style="color:#c0c4cc;font-size:12px">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="employeeNo" label="工号" width="110" align="center" />
            <el-table-column prop="name" label="姓名" width="90" align="center" />
            <el-table-column label="性别" width="70" align="center">
              <template #default="{ row }"><span>{{ genderText(row.gender) }}</span></template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="120" align="center" />
            <el-table-column prop="orgName" label="入职组织" width="130" align="center" show-overflow-tooltip />
            <el-table-column prop="postName" label="入职岗位" width="120" align="center" show-overflow-tooltip />
            <el-table-column prop="entryDate" label="入职日期" width="110" align="center" />
            <el-table-column prop="employmentTypeText" label="用工类型" width="90" align="center" />
            <el-table-column prop="basicSalary" label="基本工资" width="100" align="right">
              <template #default="{ row }">{{ row.basicSalary ? '¥' + row.basicSalary.toLocaleString() : '—' }}</template>
            </el-table-column>
            <el-table-column prop="statusText" label="状态" width="85" align="center">
              <template #default="{ row }"><el-tag size="small" :type="entryStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
                <AuthBtn permission="hr:entry:revoke" link type="warning" size="small" @click="handleRevokeEntry(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="转正申请" name="regular">
        <div class="g-page-header">
          <span class="g-page-title">转正申请</span>
          <AuthBtn permission="hr:regular:add" type="primary" @click="openAdd('regular')">新增转正</AuthBtn>
        </div>
        <SearchBar :model="regularQuery" @search="loadRegularData" @reset="() => { regularQuery.pageNum = 1; loadRegularData() }">
          <el-form-item label="状态">
            <el-select v-model="regularQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="regularQuery.pageNum" v-model:page-size="regularQuery.pageSize" :total="regularTotal" @refresh="loadRegularData">
          <el-table v-loading="regularLoading" :data="regularRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="regularDate" label="转正日期" width="110" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:regular:revoke" link type="warning" size="small" @click="handleRevokeRegular(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="调岗申请" name="transfer">
        <div class="g-page-header">
          <span class="g-page-title">调岗申请</span>
          <AuthBtn permission="hr:transfer:add" type="primary" @click="openAdd('transfer')">新增调岗</AuthBtn>
        </div>
        <SearchBar :model="transferQuery" @search="loadTransferData" @reset="() => { transferQuery.pageNum = 1; loadTransferData() }">
          <el-form-item label="状态">
            <el-select v-model="transferQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="transferQuery.pageNum" v-model:page-size="transferQuery.pageSize" :total="transferTotal" @refresh="loadTransferData">
          <el-table v-loading="transferLoading" :data="transferRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="transferDate" label="调岗日期" width="110" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:transfer:revoke" link type="warning" size="small" @click="handleRevokeTransfer(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
      <el-tab-pane label="离职申请" name="resign">
        <div class="g-page-header">
          <span class="g-page-title">离职申请</span>
          <AuthBtn permission="hr:resign:add" type="primary" @click="openAdd('resign')">新增离职</AuthBtn>
        </div>
        <SearchBar :model="resignQuery" @search="loadResignData" @reset="() => { resignQuery.pageNum = 1; loadResignData() }">
          <el-form-item label="状态">
            <el-select v-model="resignQuery.status" placeholder="全部" clearable style="width:100px">
              <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="已撤回" :value="4" />
            </el-select>
          </el-form-item>
        </SearchBar>
        <TablePage v-model:page-num="resignQuery.pageNum" v-model:page-size="resignQuery.pageSize" :total="resignTotal" @refresh="loadResignData">
          <el-table v-loading="resignLoading" :data="resignRecords" border stripe>
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="resignDate" label="离职日期" width="110" align="center" />
            <el-table-column prop="statusText" label="状态" width="90" align="center">
              <template #default="{ row }"><el-tag size="small" :type="transferStatusType(row.status)">{{ row.statusText || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <AuthBtn permission="hr:resign:revoke" link type="warning" size="small" @click="handleRevokeResign(row)" :disabled="row.status !== 0 && row.status !== 1">撤销</AuthBtn>
              </template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>
    </el-tabs>

    <!-- 入职申请弹窗 -->
    <el-dialog v-model="entryDialogVisible" title="新增入职申请" width="1200px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="entryFormRef" :model="entryForm" :rules="entryRules" label-width="100px">
        <el-row :gutter="20">
          <!-- ========== 左侧表单区域 ========== -->
          <el-col :span="12">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="员工工号" prop="employeeNo">
                  <el-input v-model="entryForm.employeeNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="entryForm.name" placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="免冠照片" prop="photoFileId">
                  <el-upload
                    ref="photoUploadRef"
                    action=""
                    :auto-upload="false"
                    :on-change="handlePhotoChange"
                    :show-file-list="false"
                    accept="image/jpeg,image/png,image/jpg"
                  >
                    <el-image
                      v-if="entryPhotoUrl"
                      :src="entryPhotoUrl"
                      fit="cover"
                      style="width:120px;height:160px;border-radius:4px;border:1px solid #dcdfe6"
                    />
                    <el-button v-else type="primary" size="small">
                      <el-icon><Plus /></el-icon> 上传照片
                    </el-button>
                  </el-upload>
                  <div style="color:#909399;font-size:12px;margin-top:4px">支持 JPG/PNG，不超过 5MB</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="入职日期" prop="entryDate">
                  <el-date-picker v-model="entryForm.entryDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="所属组织" prop="orgId">
              <el-tree-select
                v-model="entryForm.orgId"
                :data="orgTreeData"
                :props="{ label: 'orgName', value: 'id', children: 'children' }"
                check-strictly
                node-key="id"
                placeholder="请选择部门"
                clearable
                filterable
                style="width:100%"
                :render-after-expand="false"
              />
            </el-form-item>
            <el-form-item label="目标岗位" prop="postId">
              <el-select v-model="entryForm.postId" placeholder="请先选择所属组织" clearable filterable style="width:100%" :loading="postLoading">
                <el-option v-for="post in filteredPostList" :key="post.id" :label="post.postName" :value="post.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="基本工资">
              <el-input-number v-model="entryForm.basicSalary" :precision="2" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>

            <!-- 工作经历 -->
            <el-form-item label="工作经历">
              <div style="width:100%">
                <el-table :data="workExps" border size="small" style="width:100%">
                  <el-table-column prop="companyName" label="公司名称" min-width="120">
                    <template #default="{ row, $index }">
                      <el-input v-model="row.companyName" placeholder="公司名称" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="position" label="职位" width="100">
                    <template #default="{ row }">
                      <el-input v-model="row.position" placeholder="职位" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="department" label="部门" width="100">
                    <template #default="{ row }">
                      <el-input v-model="row.department" placeholder="部门" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="startDate" label="开始时间" width="100">
                    <template #default="{ row }">
                      <el-date-picker v-model="row.startDate" type="date" placeholder="开始时间" value-format="YYYY-MM" size="small" style="width:100%" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="endDate" label="结束时间" width="100">
                    <template #default="{ row }">
                      <el-date-picker v-model="row.endDate" type="date" placeholder="结束时间" value-format="YYYY-MM" size="small" style="width:100%" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="60" align="center">
                    <template #default="{ $index }">
                      <el-button type="danger" link size="small" @click="removeWorkExpRow($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button type="primary" link size="small" @click="addWorkExpRow" style="margin-top:8px">+ 添加工作经历</el-button>
              </div>
            </el-form-item>

            <!-- 学业经历 -->
            <el-form-item label="学业经历">
              <div style="width:100%">
                <el-table :data="eduExps" border size="small" style="width:100%">
                  <el-table-column prop="schoolName" label="学校名称" min-width="120">
                    <template #default="{ row, $index }">
                      <el-input v-model="row.schoolName" placeholder="学校名称" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="degree" label="学位" width="80">
                    <template #default="{ row }">
                      <el-input v-model="row.degree" placeholder="学位" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="major" label="专业" width="100">
                    <template #default="{ row }">
                      <el-input v-model="row.major" placeholder="专业" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="startDate" label="入学时间" width="100">
                    <template #default="{ row }">
                      <el-date-picker v-model="row.startDate" type="date" placeholder="入学时间" value-format="YYYY-MM" size="small" style="width:100%" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="graduationDate" label="毕业时间" width="100">
                    <template #default="{ row }">
                      <el-date-picker v-model="row.graduationDate" type="date" placeholder="毕业时间" value-format="YYYY-MM" size="small" style="width:100%" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="60" align="center">
                    <template #default="{ $index }">
                      <el-button type="danger" link size="small" @click="removeEduExpRow($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button type="primary" link size="small" @click="addEduExpRow" style="margin-top:8px">+ 添加学业经历</el-button>
              </div>
            </el-form-item>

            <el-form-item label="备注">
              <el-input v-model="entryForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>

          <!-- ========== 右侧：富文本附件内容（红色框区域） ========== -->
          <el-col :span="12">
            <!-- label-position="top" 标签放到顶部，标题在上，编辑器在下 -->
            <el-form-item label="附件内容" label-position="top" style="height:100%;margin-bottom:0;">
              <div class="wang-editor-wrapper">
                <div class="wang-editor-toolbar-row">
                  <Toolbar v-if="editorReady" :editor="editorInstance" />
                  <el-button v-if="editorReady" size="small" @click="openHtmlEditor" title="编辑 HTML 源码" style="margin-left:auto;border-radius:0;border-bottom:1px solid #ccc">
                    <el-icon><EditPen /></el-icon> HTML
                  </el-button>
                </div>
                <Editor
                  v-model="attachmentHtml"
                  :defaultConfig="editorConfig"
                  @onCreated="handleEditorCreated"
                  @onDestroyed="handleEditorDestroyed"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="entryDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="entrySubmitLoading" @click="handleSubmitEntry">确定</el-button>
      </template>
    </el-dialog>

    <!-- HTML 源码编辑弹窗 -->
    <el-dialog v-model="htmlDialogVisible" title="编辑 HTML 源码" width="720px" :close-on-click-modal="false" destroy-on-close>
      <el-input
        v-model="htmlContent"
        type="textarea"
        :rows="16"
        placeholder="在此编辑 HTML 源码…"
        spellcheck="false"
        resize="vertical"
        style="font-family: Consolas, Monaco, 'Courier New', monospace;font-size:13px"
      />
      <template #footer>
        <el-button @click="htmlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyHtmlEdit">应用到编辑器</el-button>
      </template>
    </el-dialog>

    <!-- 查看入职申请弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="入职申请详情" width="780px" :close-on-click-modal="false">
      <el-tabs v-model="viewActiveTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-row :gutter="16">
            <!-- 左侧：照片 -->
            <el-col :span="6" class="view-photo-col">
              <div class="view-photo-wrap">
                <el-image
                  v-if="viewRecord?.photoPreviewUrl"
                  :src="viewRecord.photoPreviewUrl"
                  fit="cover"
                  style="width:100%;height:140px;border-radius:6px"
                  preview-teleported
                  :preview-src-list="[viewRecord.photoPreviewUrl]"
                />
                <div v-else class="view-photo-placeholder">无照片</div>
              </div>
              <div class="view-photo-label">免冠照片</div>
            </el-col>
            <!-- 右侧：个人信息 -->
            <el-col :span="18">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="工号">{{ viewRecord?.employeeNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="姓名">{{ viewRecord?.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ genderText(viewRecord?.gender) }}</el-descriptions-item>
                <el-descriptions-item label="出生日期">{{ viewRecord?.birthdate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="身份证号" :span="2">{{ viewRecord?.idCardNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="手机号" :span="2">{{ viewRecord?.phone || '-' }}</el-descriptions-item>
              </el-descriptions>
              <el-divider content-position="left" style="margin:12px 0 8px">入职信息</el-divider>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="入职日期">{{ viewRecord?.entryDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="用工类型">{{ employmentTypeText(viewRecord?.employmentType) }}</el-descriptions-item>
                <el-descriptions-item label="入职组织">{{ viewRecord?.orgName || orgNameMap[viewRecord?.orgId ?? 0] || '-' }}</el-descriptions-item>
                <el-descriptions-item label="入职岗位">{{ viewRecord?.postName || getPostName(viewRecord?.postId) || '-' }}</el-descriptions-item>
                <el-descriptions-item label="基本工资">{{ viewRecord?.basicSalary ? '¥' + viewRecord.basicSalary.toLocaleString() : '-' }}</el-descriptions-item>
                <el-descriptions-item label="工资卡号">{{ viewRecord?.bankAccount || '-' }}</el-descriptions-item>
                <el-descriptions-item label="自动创建账号">{{ viewRecord?.autoCreateUser === 1 ? '是' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag size="small" :type="entryStatusType(viewRecord?.status)">{{ viewRecord?.statusText }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ viewRecord?.remark || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 经历信息 -->
        <el-tab-pane label="经历信息" name="experience">
          <template v-if="viewWorkExps.length > 0 || viewEduExps.length > 0">
            <div class="view-exp-section" v-if="viewWorkExps.length > 0">
              <div class="view-exp-title">工作经历</div>
              <el-table :data="viewWorkExps" border stripe size="small" class="view-exp-table">
                <el-table-column prop="companyName" label="公司名称" min-width="140" />
                <el-table-column prop="position" label="职位" width="100" />
                <el-table-column prop="department" label="部门" width="100" />
                <el-table-column prop="startDate" label="开始时间" width="100" />
                <el-table-column prop="endDate" label="结束时间" width="100" />
                <el-table-column prop="isCurrentText" label="当前" width="60" align="center">
                  <template #default="{ row }">{{ row.isCurrent ? '是' : '否' }}</template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
              </el-table>
            </div>
            <div class="view-exp-section" v-if="viewEduExps.length > 0">
              <div class="view-exp-title">学业经历</div>
              <el-table :data="viewEduExps" border stripe size="small" class="view-exp-table">
                <el-table-column prop="schoolName" label="学校名称" min-width="140" />
                <el-table-column prop="degree" label="学位" width="80" />
                <el-table-column prop="major" label="专业" width="120" />
                <el-table-column prop="educationLevel" label="学历" width="80" />
                <el-table-column prop="startDate" label="入学时间" width="100" />
                <el-table-column prop="graduationDate" label="毕业时间" width="100" />
                <el-table-column prop="isGraduatedText" label="已毕业" width="70" align="center">
                  <template #default="{ row }">{{ row.isGraduated ? '是' : '否' }}</template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
              </el-table>
            </div>
          </template>
          <el-empty v-else description="未填写经历信息" :image-size="60" />
        </el-tab-pane>

        <!-- 系统信息 -->
        <el-tab-pane label="系统信息" name="system">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="申请单ID">{{ viewRecord?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="流程实例ID">{{ viewRecord?.flowInstanceId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ viewRecord?.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="记录状态">
              <el-tag size="small" :type="entryStatusType(viewRecord?.status)">{{ viewRecord?.statusText }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 附件内容 -->
        <el-tab-pane label="附件内容" name="attachment">
          <div v-if="viewRecord?.attachmentContent" v-html="viewRecord.attachmentContent" style="padding:8px;min-height:80px;border:1px solid #ebeef5;border-radius:4px;" />
          <el-empty v-else description="无附件内容" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, shallowRef, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, EditPen } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { createEditor } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import * as fileApi from '@/api/file'
import { getOrgTreeApi } from '@/api/org'
import { getStorage } from '@/utils/storage'

const activeTab = ref('entry')

/* ---- 组织树数据 ---- */
const orgTreeData = ref<any[]>([])
const orgNameMap = ref<Record<number, string>>({})

const loadOrgTree = async () => {
  try {
    const list = await getOrgTreeApi()
    orgTreeData.value = markDisabledNodes(list)
    // 构建 orgId -> orgName 映射
    const flatList: any[] = []
    const collect = (nodes: any[]) => {
      for (const n of nodes) {
        flatList.push(n)
        if (n.children) collect(n.children)
      }
    }
    collect(list)
    orgNameMap.value = Object.fromEntries(flatList.map((o: any) => [o.id, o.orgName]))
  } catch (e) {
    console.error('[transfer] 加载组织树失败', e)
  }
}

/** 为每个节点添加 disabled 字段：仅 orgType=3（部门）可选 */
function markDisabledNodes(nodes: any[]): any[] {
  return nodes.map(node => ({
    ...node,
    disabled: node.orgType !== 3,
    children: node.children ? markDisabledNodes(node.children) : undefined
  }))
}

/* ---- 岗位列表数据 ---- */
const allPostList = ref<api.HrPostVO[]>([])
const postLoading = ref(false)

const loadAllPosts = async () => {
  postLoading.value = true
  try {
    const result = await api.getPostFlatListApi()
    allPostList.value = result
  } finally {
    postLoading.value = false
  }
}

// 根据选定部门过滤岗位列表
const filteredPostList = computed(() => {
  if (!entryForm.orgId) return []
  return allPostList.value.filter(p => p.deptId === entryForm.orgId)
})

/* ---- 入职申请 ---- */
const entryQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: entryRecords, total: entryTotal, loading: entryLoading, loadData: loadEntryData } = useTable(api.getEntryPageApi, entryQuery)

const entryDialogVisible = ref(false)
const entrySubmitLoading = ref(false)
const entryFormRef = ref()
const entryForm = reactive<api.EntryApplyDTO>({ employeeNo: '', name: '', entryDate: '', employmentType: 1 })
const entryPhotoFile = ref<File | null>(null)
const entryPhotoUrl = ref<string>('')
const entryPhotoFileId = ref<number | undefined>(undefined)
const photoUploadRef = ref()

function handlePhotoChange(uploadFile: any) {
  const file = uploadFile.raw
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('照片大小不能超过 5MB')
    return
  }
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG 格式')
    return
  }
  entryPhotoFile.value = file
  entryPhotoUrl.value = URL.createObjectURL(file)
  entryPhotoFileId.value = undefined
}
const entryRules = {
  employeeNo: [{ required: true, message: '工号不能为空' }],
  name: [{ required: true, message: '姓名不能为空' }],
  entryDate: [{ required: true, message: '入职日期不能为空' }]
}

// /** 打开新增入职弹窗：先创建编辑器实例，再打开弹窗，保证 Editor 挂载时实例已就绪 */
// const openEntryDialog = () => {
//   editorInstance.value = createEditor(editorConfig)
//   attachmentHtml.value = ''
// }

const openAdd = (tab: string) => {
  if (tab === 'entry') {
    Object.assign(entryForm, { employeeNo: '', name: '', entryDate: '', employmentType: 1, orgId: undefined, postId: undefined, basicSalary: undefined, remark: '' })
    entryPhotoFile.value = null
    entryPhotoUrl.value = ''
    entryPhotoFileId.value = undefined
    // 清空富文本内容，不手动创建editor实例
    attachmentHtml.value = ''
    workExps.value = []
    eduExps.value = []
    entryDialogVisible.value = true
  }
}


// 工作经历/学业经历数据（临时存储，提交时序列化为experienceData）
const workExps = ref<Array<{ companyName: string; position?: string; department?: string; startDate: string; endDate?: string; isCurrent?: number; reasonForLeaving?: string; remark?: string }>>([])
const eduExps = ref<Array<{ schoolName: string; degree?: string; major?: string; educationLevel?: string; startDate: string; graduationDate?: string; isGraduated?: number; certificateNo?: string; remark?: string }>>([])

// 工作经历行操作
const addWorkExpRow = () => { workExps.value.push({ companyName: '', startDate: '' }) }
const removeWorkExpRow = (index: number) => { workExps.value.splice(index, 1) }

// 学业经历行操作
const addEduExpRow = () => { eduExps.value.push({ schoolName: '', startDate: '' }) }
const removeEduExpRow = (index: number) => { eduExps.value.splice(index, 1) }

// wangEditor 富文本附件
const editorInstance = shallowRef<any>(null)
const editorReady = ref(false)
const attachmentHtml = ref('')
const editorConfig = {
  placeholder: '请输入附件内容，支持文字编辑与图片上传…',
  MENU_CONF: {
    uploadImage: {
      // 使用 customUpload 确保上传后图片能正确插入编辑器
      customUpload(file: File, insertFn: (url: string) => void) {
        console.log('[wangEditor] 开始上传图片:', file.name, file.size)
        const formData = new FormData()
        formData.append('file', file)
        formData.append('bizType', 'hr_entry')
        const token = getStorage('token')
        console.log('[wangEditor] token:', token ? '存在' : '不存在')
        
        fetch('/api/base/file/upload', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          body: formData
        })
        .then(resp => resp.json())
        .then(res => {
          console.log('[wangEditor] 上传响应:', res)
          if (res.code === 200 || res.code === 0) {
            // 使用后端返回的 previewUrl（本地模式为 /upload/{fileKey}，OSS模式为完整CDN URL）
            const imageUrl = res.data?.previewUrl
            console.log('[wangEditor] 图片URL:', imageUrl)
            insertFn(imageUrl)  // 插入图片到编辑器
          } else {
            ElMessage.error(res.msg || '图片上传失败')
          }
        })
        .catch(err => {
          console.error('[wangEditor] 上传错误:', err)
          ElMessage.error('图片上传失败，请重试')
        })
      }
    }
  }
}

const handleEditorCreated = (inst: any) => {
  editorInstance.value = inst
  editorReady.value = true
}
const handleEditorDestroyed = () => {
  editorInstance.value = null
  editorReady.value = false
}

// HTML 源码编辑
const htmlDialogVisible = ref(false)
const htmlContent = ref('')

const openHtmlEditor = () => {
  if (!editorInstance.value) return
  htmlContent.value = editorInstance.value.getHtml()
  htmlDialogVisible.value = true
}

const applyHtmlEdit = () => {
  if (!editorInstance.value) return
  editorInstance.value.setHtml(htmlContent.value)
  attachmentHtml.value = htmlContent.value
  htmlDialogVisible.value = false
  ElMessage.success('HTML 源码已应用')
}

const handleSubmitEntry = async () => {
  await entryFormRef.value.validate()
  // 先上传图片（若有）
  let photoFileId: number | undefined = entryPhotoFileId.value
  if (entryPhotoFile.value) {
    try {
      const uploadResult = await fileApi.uploadFileApi(entryPhotoFile.value, 'hr_entry')
      photoFileId = uploadResult.fileId
      entryPhotoFileId.value = photoFileId
    } catch (e: any) {
      ElMessage.error('照片上传失败：' + (e?.message || '请稍后重试'))
      entrySubmitLoading.value = false
      return
    }
  }
  const submitData = {
    ...entryForm,
    photoFileId,
    experienceData: JSON.stringify({ workExps: workExps.value, eduExps: eduExps.value }),
    attachmentContent: attachmentHtml.value
  } as any
  entrySubmitLoading.value = true
  try {
    await api.submitEntryApi(submitData)
    ElMessage.success('提交成功')
    entryDialogVisible.value = false
    workExps.value = []
    eduExps.value = []
    loadEntryData()
  } finally { entrySubmitLoading.value = false }
}

const handleRevokeEntry = (row: api.HrEntryApplyVO) => {
  ElMessageBox.confirm('确认撤销该入职申请?', '提示').then(async () => {
    await api.revokeEntryApi(row.id!)
    ElMessage.success('撤销成功')
    loadEntryData()
  })
}

/* ---- 查看入职申请 ---- */
const viewDialogVisible = ref(false)
const viewRecord = ref<api.HrEntryApplyVO | null>(null)
const viewActiveTab = ref('basic')
const viewWorkExps = ref<Array<{ companyName: string; position?: string; department?: string; startDate: string; endDate?: string; isCurrent?: number; reasonForLeaving?: string; remark?: string }>>([])
const viewEduExps = ref<Array<{ schoolName: string; degree?: string; major?: string; educationLevel?: string; startDate: string; graduationDate?: string; isGraduated?: number; certificateNo?: string; remark?: string }>>([])

/** 从岗位列表中查找岗位名称 */
const getPostName = (postId?: number) => {
  if (!postId) return ''
  const post = allPostList.value.find(p => p.id === postId)
  return post?.postName || ''
}

/** 解析经历数据 JSON */
const parseExperienceData = (data?: string) => {
  if (!data) { viewWorkExps.value = []; viewEduExps.value = []; return }
  try {
    const parsed = JSON.parse(data)
    viewWorkExps.value = parsed.workExps || []
    viewEduExps.value = parsed.eduExps || []
  } catch {
    viewWorkExps.value = []
    viewEduExps.value = []
  }
}

const handleView = (row: api.HrEntryApplyVO) => {
  viewRecord.value = row
  viewActiveTab.value = 'basic'
  parseExperienceData(row.experienceData)
  viewDialogVisible.value = true
}

const employmentTypeText = (type?: number) => {
  const map: Record<number, string> = { 1: '正式', 2: '试用期', 3: '劳务派遣', 4: '临时工' }
  return type != null ? map[type] : '-'
}

const genderText = (gender?: number) => {
  const map: Record<number, string> = { 1: '男', 2: '女' }
  return gender != null ? map[gender] : '-'
}

const entryStatusType = (status?: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return status != null ? map[status] : ''
}

const transferStatusType = (status?: number) => entryStatusType(status)

/* ---- 转正 / 调岗 / 离职（占位） ---- */
const regularQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: regularRecords, total: regularTotal, loading: regularLoading, loadData: loadRegularData } = useTable(api.getRegularPageApi, regularQuery)

const transferQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: transferRecords, total: transferTotal, loading: transferLoading, loadData: loadTransferData } = useTable(api.getTransferPageApi, transferQuery)

const resignQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: resignRecords, total: resignTotal, loading: resignLoading, loadData: loadResignData } = useTable(api.getResignPageApi, resignQuery)

const handleRevokeRegular = (row: api.HrRegularApplyVO) => {
  ElMessageBox.confirm('确认撤销该转正申请?', '提示').then(async () => {
    await api.revokeRegularApi(row.id!)
    ElMessage.success('撤销成功')
    loadRegularData()
  })
}

const handleRevokeTransfer = (row: api.HrTransferApplyVO) => {
  ElMessageBox.confirm('确认撤销该调岗申请?', '提示').then(async () => {
    await api.revokeTransferApi(row.id!)
    ElMessage.success('撤销成功')
    loadTransferData()
  })
}

const handleRevokeResign = (row: api.HrResignApplyVO) => {
  ElMessageBox.confirm('确认撤销该离职申请?', '提示').then(async () => {
    await api.revokeResignApi(row.id!)
    ElMessage.success('撤销成功')
    loadResignData()
  })
}

onMounted(() => { loadOrgTree(); loadAllPosts(); loadEntryData() })
// Editor 实例由 destroy-on-close 自动管理，无需手动销毁
</script>

<style scoped>
.view-photo-col { display: flex; flex-direction: column; align-items: center; }
.view-photo-wrap { width: 100%; }
.view-photo-placeholder {
  width: 100%; height: 140px; border-radius: 6px; border: 1px dashed #dcdfe6;
  display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 13px;
}
.view-photo-label { margin-top: 6px; color: #606266; font-size: 12px; }
.view-exp-section { margin-bottom: 16px; }
.view-exp-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 8px; padding-left: 8px; border-left: 3px solid #409eff; }
.view-exp-table { width: 100%; }
/* wangEditor 富文本编辑器样式 */
.wang-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.wang-editor-wrapper :deep(.w-e-toolbar) {
  border-bottom:1px solid #dcdfe6;
}
.wang-editor-wrapper :deep(.w-e-text-container) {
  flex: 1;
  min-height: 420px;
}
.wang-editor-wrapper .w-e-toolbar {
  border-bottom: 1px solid #dcdfe6 !important;
}
.wang-editor-wrapper .w-e-text-container {
  height: 320px !important;
  overflow-y: auto;
}
.wang-editor-toolbar-row {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #dcdfe6;
}
.wang-editor-toolbar-row :deep(.w-e-toolbar) {
  border-bottom: none !important;
  flex: 1;
}
</style>
