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
            <el-table-column prop="workweekConfigName" label="休息日配置" width="120" align="center" show-overflow-tooltip />
            <el-table-column prop="exemptAttendanceText" label="是否参与考勤" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.exemptAttendance === 1 ? 'info' : 'success'">{{ row.exemptAttendanceText }}</el-tag>
              </template>
            </el-table-column>
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
        <el-row :gutter="23">
          <!-- ========== 左侧表单区域 ========== -->
          <el-col :span="14">
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
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="entryForm.gender">
                    <el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="入职日期"><el-date-picker v-model="entryForm.entryDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
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
                <el-form-item label="身份证号" prop="idCardNo">
                  <el-input v-model="entryForm.idCardNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="用工类型" prop="employmentType">
                  <el-select v-model="entryForm.employmentType" placeholder="请选择" style="width:100%">
                    <el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="就职城市" prop="cityId">
                  <el-select v-model="entryForm.cityId" placeholder="请选择就职城市" clearable filterable style="width:100%" :loading="cityLoading">
                    <el-option v-for="city in cityList" :key="city.id" :label="city.cityCode + '、' + city.cityName" :value="city.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row> 
            <el-row :gutter="16">
              <el-col :span="12">
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
              </el-col>
              <el-col :span="12">
                <el-form-item label="目标岗位">
                  <el-select v-model="entryForm.postId" placeholder="请先选择所属组织" clearable filterable style="width:100%" :loading="postLoading">
                    <el-option v-for="post in filteredPostList" :key="post.id" :label="post.postName" :value="post.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row> 
            <el-form-item label="薪资模板">
              <el-select v-model="entryForm.salaryRuleId" placeholder="请选择薪资模板" clearable filterable style="width:100%" :loading="salaryRuleLoading" @change="handleSalaryRuleChange">
                <el-option v-for="rule in salaryRuleList" :key="rule.id" :label="rule.ruleName" :value="rule.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="休息日配置">
              <el-select v-model="entryForm.workweekConfigId" placeholder="可选，默认沿用岗位配置" clearable filterable style="width:100%">
                <el-option v-for="cfg in workweekConfigList" :key="cfg.id" :label="cfg.configName" :value="cfg.id" />
              </el-select>
              <div style="color:#909399;font-size:12px;margin-top:4px">为本次入职员工指定休息日规则；留空则使用岗位或组织默认配置</div>
            </el-form-item>
            <el-form-item label="是否参与考勤">
              <el-radio-group v-model="entryForm.exemptAttendance">
                <el-radio :value="0">参与</el-radio>
                <el-radio :value="1">不参与</el-radio>
              </el-radio-group>
              <div style="color:#909399;font-size:12px;margin-top:4px">不参与考勤的员工，同步打卡时默认为满勤</div>
            </el-form-item>
            <el-form-item label="基本工资">
              <el-input-number v-model="entryForm.basicSalary" :precision="2" :min="0" placeholder="选择模板后自动填充或手动输入" style="width:100%" />
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
          <el-col :span="10">
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
                <el-descriptions-item label="就职城市">{{ viewRecord?.cityName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="薪资模板">{{ viewRecord?.salaryRuleName || '-' }}</el-descriptions-item>
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

    <!-- 转正申请弹窗 -->
    <el-dialog v-model="regularDialogVisible" title="新增转正申请" width="560px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="regularFormRef" :model="regularForm" :rules="regularRules" label-width="100px">
        <el-form-item label="在职员工" prop="employeeId">
          <el-select v-model="regularForm.employeeId" placeholder="请选择员工" clearable filterable style="width:100%" :loading="employeeListLoading">
            <el-option v-for="emp in employeeList" :key="emp.id" :label="emp.employeeNo + '、' + emp.name" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转正日期" prop="regularDate">
          <el-date-picker v-model="regularForm.regularDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="regularForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regularDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="regularSubmitLoading" @click="handleSubmitRegular">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调岗申请弹窗 -->
    <el-dialog v-model="transferDialogVisible" title="新增调岗申请" width="560px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="在职员工" prop="employeeId">
          <el-select v-model="transferForm.employeeId" placeholder="请选择员工" clearable filterable style="width:100%" :loading="employeeListLoading">
            <el-option v-for="emp in employeeList" :key="emp.id" :label="emp.employeeNo + '、' + emp.name" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="新组织" prop="newOrgId">
          <el-tree-select
            v-model="transferForm.newOrgId"
            :data="orgTreeData"
            :props="{ label: 'orgName', value: 'id', children: 'children' }"
            check-strictly node-key="id"
            placeholder="请选择新组织"
            clearable filterable
            style="width:100%" :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item label="新岗位" prop="newPostId">
          <el-select v-model="transferForm.newPostId" placeholder="请先选择新组织" clearable filterable style="width:100%" :loading="postLoading">
            <el-option v-for="post in filteredTransferPostList" :key="post.id" :label="post.postName" :value="post.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调岗日期" prop="transferDate">
          <el-date-picker v-model="transferForm.transferDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="调岗原因">
          <el-input v-model="transferForm.reason" type="textarea" :rows="2" placeholder="请输入调岗原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="transferSubmitLoading" @click="handleSubmitTransfer">确定</el-button>
      </template>
    </el-dialog>

    <!-- 离职申请弹窗 -->
    <el-dialog v-model="resignDialogVisible" title="新增离职申请" width="560px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="resignFormRef" :model="resignForm" :rules="resignRules" label-width="100px">
        <el-form-item label="在职员工" prop="employeeId">
          <el-select v-model="resignForm.employeeId" placeholder="请选择员工" clearable filterable style="width:100%" :loading="employeeListLoading">
            <el-option v-for="emp in employeeList" :key="emp.id" :label="emp.employeeNo + '、' + emp.name" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="离职日期" prop="resignDate">
          <el-date-picker v-model="resignForm.resignDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="离职类型" prop="resignType">
          <el-radio-group v-model="resignForm.resignType">
            <el-radio :value="1">主动辞职</el-radio>
            <el-radio :value="2">合同到期</el-radio>
            <el-radio :value="3">辞退</el-radio>
            <el-radio :value="4">终止合同</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="离职原因">
          <el-input v-model="resignForm.reason" type="textarea" :rows="2" placeholder="请输入离职原因" />
        </el-form-item>
        <el-form-item label="交接备注">
          <el-input v-model="resignForm.handoverRemark" type="textarea" :rows="2" placeholder="请输入工作交接备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resignDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="resignSubmitLoading" @click="handleSubmitResign">确定</el-button>
      </template>
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
import { getCityListApi } from '@/api/hrSocialParam'
import { getSalaryRuleListApi, type HrSalaryRuleVO } from '@/api/hrSalary'
import { getWorkweekConfigListApi } from '@/api/hr'
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
    allPostList.value = result as unknown as api.HrPostVO[]
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
    Object.assign(entryForm, { employeeNo: '', name: '', entryDate: '', employmentType: 1, orgId: undefined, postId: undefined, basicSalary: undefined, cityId: 1, salaryRuleId: undefined, workweekConfigId: undefined, exemptAttendance: 0, remark: '' })
    entryPhotoFile.value = null
    entryPhotoUrl.value = ''
    entryPhotoFileId.value = undefined
    attachmentHtml.value = ''
    workExps.value = []
    eduExps.value = []
    entryDialogVisible.value = true
  } else if (tab === 'regular') {
    Object.assign(regularForm, { employeeId: undefined, regularDate: '', remark: '' })
    // 转正只展示试用期且非派遣/临时工员工
    employeeListLoading.value = true
    api.getEmployeeListApi({ employeeStatus: 2 }).then((list: any[]) => {
      employeeList.value = list.filter(e => e.employmentType !== 3 && e.employmentType !== 4)
      employeeListLoading.value = false
    }).catch(() => { employeeListLoading.value = false })
    regularDialogVisible.value = true
  } else if (tab === 'transfer') {
    Object.assign(transferForm, { employeeId: undefined, newOrgId: undefined, newPostId: undefined, transferDate: '', reason: '' })
    employeeListLoading.value = true
    api.getEmployeeListApi({ employeeStatus: 1 }).then((list: any[]) => {
      employeeList.value = list
      employeeListLoading.value = false
    }).catch(() => { employeeListLoading.value = false })
    transferDialogVisible.value = true
  } else if (tab === 'resign') {
    Object.assign(resignForm, { employeeId: undefined, resignDate: '', resignType: undefined, reason: '', handoverRemark: '' })
    employeeListLoading.value = true
    api.getEmployeeListApi({ employeeStatus: 1 }).then((list: any[]) => {
      employeeList.value = list
      employeeListLoading.value = false
    }).catch(() => { employeeListLoading.value = false })
    resignDialogVisible.value = true
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

/* ---- 转正 / 调岗 / 离职 ---- */
const regularQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: regularRecords, total: regularTotal, loading: regularLoading, loadData: loadRegularData } = useTable(api.getRegularPageApi, regularQuery)

const transferQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: transferRecords, total: transferTotal, loading: transferLoading, loadData: loadTransferData } = useTable(api.getTransferPageApi, transferQuery)

const resignQuery = reactive({ pageNum: 1, pageSize: 20, status: undefined as number | undefined })
const { records: resignRecords, total: resignTotal, loading: resignLoading, loadData: loadResignData } = useTable(api.getResignPageApi, resignQuery)

/* ---- 转正申请 ---- */
const regularDialogVisible = ref(false)
const regularSubmitLoading = ref(false)
const regularFormRef = ref()
const regularForm = reactive<api.RegularApplyDTO>({ employeeId: undefined!, regularDate: '' })
const regularRules = { employeeId: [{ required: true, message: '请选择员工' }], regularDate: [{ required: true, message: '请选择转正日期' }] }
const handleSubmitRegular = async () => {
  await regularFormRef.value.validate()
  regularSubmitLoading.value = true
  try {
    await api.submitRegularApi(regularForm as any)
    ElMessage.success('提交成功')
    regularDialogVisible.value = false
    loadRegularData()
  } finally { regularSubmitLoading.value = false }
}

/* ---- 调岗申请 ---- */
const transferDialogVisible = ref(false)
const transferSubmitLoading = ref(false)
const transferFormRef = ref()
const transferForm = reactive<api.TransferApplyDTO>({ employeeId: undefined!, newOrgId: undefined!, newPostId: undefined!, transferDate: '' })
const transferRules = { employeeId: [{ required: true, message: '请选择员工' }], newOrgId: [{ required: true, message: '请选择新组织' }], newPostId: [{ required: true, message: '请选择新岗位' }], transferDate: [{ required: true, message: '请选择调岗日期' }] }
// 调岗弹窗内的岗位过滤（基于新组织）
const filteredTransferPostList = computed(() => {
  if (!transferForm.newOrgId) return []
  return allPostList.value.filter(p => p.deptId === transferForm.newOrgId)
})
const handleSubmitTransfer = async () => {
  await transferFormRef.value.validate()
  transferSubmitLoading.value = true
  try {
    await api.submitTransferApi(transferForm as any)
    ElMessage.success('提交成功')
    transferDialogVisible.value = false
    loadTransferData()
  } finally { transferSubmitLoading.value = false }
}

/* ---- 离职申请 ---- */
const resignDialogVisible = ref(false)
const resignSubmitLoading = ref(false)
const resignFormRef = ref()
const resignForm = reactive<api.ResignApplyDTO>({ employeeId: undefined!, resignDate: '', resignType: undefined! })
const resignRules = { employeeId: [{ required: true, message: '请选择员工' }], resignDate: [{ required: true, message: '请选择离职日期' }], resignType: [{ required: true, message: '请选择离职类型' }] }
const handleSubmitResign = async () => {
  await resignFormRef.value.validate()
  resignSubmitLoading.value = true
  try {
    await api.submitResignApi(resignForm as any)
    ElMessage.success('提交成功')
    resignDialogVisible.value = false
    loadResignData()
  } finally { resignSubmitLoading.value = false }
}

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

// ---- 就职城市 ----
const cityList = ref<any[]>([])
const cityLoading = ref(false)
const loadCityList = async () => {
  cityLoading.value = true
  try {
    const res = await getCityListApi()
    cityList.value = res || []
    // 默认选中 id=1 的青岛
    if (cityList.value.length > 0) {
      const qd = cityList.value.find((c: any) => c.id === 1)
      if (qd) entryForm.cityId = qd.id
    }
  } catch (e) {
    console.error('[transfer] 加载城市列表失败', e)
  } finally {
    cityLoading.value = false
  }
}

// ---- 薪资模板下拉 ----
const salaryRuleList = ref<HrSalaryRuleVO[]>([])
const salaryRuleLoading = ref(false)
const loadSalaryRuleList = async () => {
  salaryRuleLoading.value = true
  try {
    const res = await getSalaryRuleListApi()
    salaryRuleList.value = res || []
  } catch (e) {
    console.error('[transfer] 加载薪资模板列表失败', e)
  } finally {
    salaryRuleLoading.value = false
  }
}

// 选择薪资模板时自动填充基本工资
function handleSalaryRuleChange(ruleId: number | undefined) {
  if (!ruleId) { entryForm.basicSalary = undefined; return }
  const rule = salaryRuleList.value.find(r => r.id === ruleId)
  if (rule) entryForm.basicSalary = rule.basicSalary
}

// ---- 休息日配置下拉 ----
const workweekConfigList = ref<api.WorkweekConfigVO[]>([])
const loadWorkweekConfigList = async () => {
  try { workweekConfigList.value = await getWorkweekConfigListApi() } catch (e) { console.error('[transfer] 加载休息日配置列表失败', e) }
}

// ---- 员工下拉列表（转正/调岗/离职弹窗用）----
const employeeList = ref<api.EmployeeVO[]>([])
const employeeListLoading = ref(false)
// 根据申请类型加载对应状态的员工列表：转正=试用期(2)，调岗/离职=在职(1)
const loadEmployeeList = async (employeeStatus?: number) => {
  employeeListLoading.value = true
  try {
    const params: any = { employmentType: 1 }
    if (employeeStatus != null) params.employeeStatus = employeeStatus
    const res = await api.getEmployeeListApi(params)
    employeeList.value = res || []
  } catch (e) { console.error('[transfer] 加载员工列表失败', e) }
  finally { employeeListLoading.value = false }
}

onMounted(() => { loadOrgTree(); loadAllPosts(); loadEntryData(); loadCityList(); loadSalaryRuleList(); loadWorkweekConfigList() })
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
