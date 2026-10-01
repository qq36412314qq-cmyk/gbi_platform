<template>
  <div class="g-page-wrap">
    <div class="g-page-header">
      <span class="g-page-title">员工档案</span>
      <div style="display:flex;gap:8px">
        <span v-if="!hasAddPermission" style="font-size:12px;color:#909399;line-height:32px">审批通过的入职申请将自动创建员工档案，无需手动新增</span>
        <AuthBtn v-if="hasAddPermission" permission="hr:employee:add" type="primary" @click="openAddDialog">新增员工</AuthBtn>
      </div>
    </div>
    <SearchBar :model="query" @search="loadData" @reset="handleReset">
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="请输入" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="工号">
        <el-input v-model="query.employeeNo" placeholder="请输入" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.employeeStatus" placeholder="全部" clearable style="width:100px">
          <el-option label="在职" :value="1" /><el-option label="试用期" :value="2" /><el-option label="离职" :value="3" />
        </el-select>
      </el-form-item>
    </SearchBar>
    <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
      <el-table v-loading="loading" :data="records" border stripe @expand-change="onExpandChange">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding:10px 20px">
              <el-tabs v-model="activeTab[row.id]">
                <el-tab-pane label="工作经历">
                  <div style="margin-bottom:10px">
                    <AuthBtn v-if="hasEditPermission" permission="hr:workExp:add" type="primary" size="small" @click="openWorkDialog(row)">+ 新增工作经历</AuthBtn>
                  </div>
                  <el-table :data="workExpMap[row.id] || []" border size="small" empty-text="暂无工作经历">
                    <el-table-column prop="companyName" label="公司名称" min-width="150" />
                    <el-table-column prop="position" label="职位" width="120" />
                    <el-table-column prop="department" label="部门" width="120" />
                    <el-table-column prop="startDate" label="入职时间" width="110" />
                    <el-table-column prop="endDate" label="离职时间" width="110" />
                    <el-table-column prop="isCurrentText" label="状态" width="80" align="center">
                      <template #default="{ row: item }"><el-tag size="small" :type="item.isCurrent === 1 ? 'success' : 'info'">{{ item.isCurrentText }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="reasonForLeaving" label="离职原因" min-width="120" show-overflow-tooltip />
                    <el-table-column label="操作" width="120" align="center" v-if="hasEditPermission">
                      <template #default="{ row: item }">
                        <AuthBtn permission="hr:workExp:edit" link type="primary" size="small" @click="editWorkExp(item, row)">编辑</AuthBtn>
                        <AuthBtn permission="hr:workExp:delete" link type="danger" size="small" @click="delWorkExp(item)">删除</AuthBtn>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="学业经历">
                  <div style="margin-bottom:10px">
                    <AuthBtn v-if="hasEditPermission" permission="hr:eduExp:add" type="primary" size="small" @click="openEduDialog(row)">+ 新增学业经历</AuthBtn>
                  </div>
                  <el-table :data="eduExpMap[row.id] || []" border size="small" empty-text="暂无学业经历">
                    <el-table-column prop="schoolName" label="学校名称" min-width="150" />
                    <el-table-column prop="degree" label="学历" width="100" />
                    <el-table-column prop="major" label="专业" width="120" />
                    <el-table-column prop="startDate" label="入学时间" width="110" />
                    <el-table-column prop="graduationDate" label="毕业时间" width="110" />
                    <el-table-column prop="isGraduatedText" label="状态" width="80" align="center">
                      <template #default="{ row: item }"><el-tag size="small" :type="item.isGraduated === 1 ? 'success' : 'warning'">{{ item.isGraduatedText }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="certificateNo" label="证书编号" width="120" />
                    <el-table-column label="操作" width="120" align="center" v-if="hasEditPermission">
                      <template #default="{ row: item }">
                        <AuthBtn permission="hr:eduExp:edit" link type="primary" size="small" @click="editEduExp(item, row)">编辑</AuthBtn>
                        <AuthBtn permission="hr:eduExp:delete" link type="danger" size="small" @click="delEduExp(item)">删除</AuthBtn>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="employeeNo" label="工号" width="110" align="center" />
        <el-table-column prop="name" label="姓名" width="90" align="center" />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{ row }">{{ row.genderText || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" align="center" />
        <el-table-column prop="orgName" label="所属部门" width="130" align="center" show-overflow-tooltip />
        <el-table-column prop="postName" label="岗位名称" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="employmentTypeText" label="用工类型" width="90" align="center" />
        <el-table-column prop="entryDate" label="入职日期" width="110" align="center" />
        <el-table-column prop="exemptAttendanceText" label="是否参与考勤" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.exemptAttendance === 1 ? 'info' : 'success'">{{ row.exemptAttendanceText }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column prop="employeeStatusText" label="状态" width="85" align="center">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.employeeStatus)">{{ row.employeeStatusText }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right" v-if="hasEditPermission">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <AuthBtn permission="hr:employee:delete" link type="danger" size="small" @click="handleDelete(row)">删除</AuthBtn>
          </template>
        </el-table-column>
      </el-table>
    </TablePage>

    <!-- 新增/编辑员工弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="1200px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <!-- ========== 左侧表单区域 ========== -->
          <el-col :span="14">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="员工工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-radio-group v-model="form.gender">
                    <el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="入职日期"><el-date-picker v-model="form.entryDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="免冠照片">
                  <el-upload ref="photoUploadRef" action="" :auto-upload="false" :on-change="handlePhotoChange" :show-file-list="false" accept="image/jpeg,image/png,image/jpg">
                    <el-image v-if="photoUrl" :src="photoUrl" fit="cover" style="width:120px;height:160px;border-radius:4px;border:1px solid #dcdfe6" />
                    <el-button v-else type="primary" size="small"><el-icon><Plus /></el-icon> 上传照片</el-button>
                  </el-upload>
                  <div style="color:#909399;font-size:12px;margin-top:4px">支持 JPG/PNG，不超过 5MB</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCardNo"><el-input v-model="form.idCardNo" placeholder="请输入" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="用工类型">
                  <el-select v-model="form.employmentType" placeholder="请选择" style="width:100%">
                    <el-option label="正式" :value="1" /><el-option label="试用期" :value="2" /><el-option label="劳务派遣" :value="3" /><el-option label="临时工" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="就职城市" prop="cityId">
                  <el-select v-model="form.cityId" placeholder="请选择就职城市" clearable filterable style="width:100%" :loading="cityLoading">
                    <el-option v-for="city in cityList" :key="city.id" :label="city.cityCode + '、' + city.cityName" :value="city.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="所属组织" prop="orgId">
                  <el-tree-select
                    v-model="form.orgId"
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
                <el-form-item label="目标岗位" prop="postId">
                  <el-select v-model="form.postId" placeholder="请选择目标岗位" clearable filterable style="width:100%" :loading="postLoading">
                    <el-option v-for="post in filteredPostList" :key="post.id" :label="post.postName" :value="post.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="薪资模板">
              <el-select v-model="form.salaryRuleId" placeholder="请选择薪资模板" clearable filterable style="width:100%" :loading="salaryRuleLoading" @change="handleSalaryRuleChange">
                <el-option v-for="rule in salaryRuleList" :key="rule.id" :label="rule.ruleName" :value="rule.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="休息日配置">
              <el-select v-model="form.workweekConfigId" placeholder="请选择休息日配置" clearable filterable style="width:100%">
                <el-option v-for="cfg in workweekConfigList" :key="cfg.id" :label="cfg.configName" :value="cfg.id" />
              </el-select>
              <div style="color:#909399;font-size:12px;margin-top:4px">
                说明：配置员工出勤的休息日规则（单休/双休/做五休二等）。
                优先级：员工个人配置 &gt; 岗位默认配置 &gt; 组织默认配置 &gt; 集团全局默认。
                子公司可在「组织岗位」页面为岗位单独指定休息日配置；也可在「员工档案」中为单个员工单独设置，覆盖岗位默认值。
              </div>
            </el-form-item>
            <el-form-item label="是否参与考勤">
              <el-radio-group v-model="form.exemptAttendance">
                <el-radio :value="0">参与</el-radio>
                <el-radio :value="1">不参与</el-radio>
              </el-radio-group>
              <div style="color:#909399;font-size:12px;margin-top:4px">不参与考勤的员工，同步打卡时默认为满勤</div>
            </el-form-item>
            <el-form-item label="基本工资"><el-input-number v-model="form.basicSalary" :precision="2" :min="0" placeholder="请输入" style="width:100%" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>

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
          </el-col>

          <!-- ========== 右侧：富文本附件内容 ========== -->
          <el-col :span="10">
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
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- HTML 源码编辑弹窗 -->
    <el-dialog v-model="htmlDialogVisible" title="编辑 HTML 源码" width="720px" :close-on-click-modal="false" destroy-on-close>
      <el-input v-model="htmlContent" type="textarea" :rows="16" placeholder="在此编辑 HTML 源码…" spellcheck="false" resize="vertical" style="font-family:Consolas,Monaco,'Courier New',monospace;font-size:13px" />
      <template #footer>
        <el-button @click="htmlDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="applyHtmlEdit">应用到编辑器</el-button>
      </template>
    </el-dialog>

    <!-- 查看员工详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="员工档案详情" width="780px" :close-on-click-modal="false">
      <el-tabs v-model="viewActiveTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="员工工号">{{ viewRecord?.employeeNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ viewRecord?.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ viewRecord?.genderText || '—' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ viewRecord?.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="入职日期">{{ viewRecord?.entryDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用工类型">{{ viewRecord?.employmentTypeText || '—' }}</el-descriptions-item>
            <el-descriptions-item label="所属组织">{{ viewRecord?.orgName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="状态"><el-tag size="small" :type="statusType(viewRecord?.employeeStatus)">{{ viewRecord?.employeeStatusText }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ viewRecord?.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="附件内容" name="attachment">
          <div v-if="viewRecord?.attachmentContent" v-html="viewRecord.attachmentContent" style="padding:8px;min-height:80px;border:1px solid #ebeef5;border-radius:4px" />
          <el-empty v-else description="无附件内容" :image-size="60" />
        </el-tab-pane>
        <el-tab-pane label="工作经历" name="workExp">
          <el-table :data="viewWorkExps" border size="small" empty-text="暂无工作经历">
            <el-table-column prop="companyName" label="公司名称" min-width="150" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="startDate" label="入职时间" width="110" />
            <el-table-column prop="endDate" label="离职时间" width="110" />
            <el-table-column prop="isCurrentText" label="状态" width="80" align="center">
              <template #default="{ row }"><el-tag size="small" :type="row.isCurrent === 1 ? 'success' : 'info'">{{ row.isCurrentText }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="学业经历" name="eduExp">
          <el-table :data="viewEduExps" border size="small" empty-text="暂无学业经历">
            <el-table-column prop="schoolName" label="学校名称" min-width="150" />
            <el-table-column prop="degree" label="学历" width="100" />
            <el-table-column prop="major" label="专业" width="120" />
            <el-table-column prop="startDate" label="入学时间" width="110" />
            <el-table-column prop="graduationDate" label="毕业时间" width="110" />
            <el-table-column prop="isGraduatedText" label="状态" width="80" align="center">
              <template #default="{ row }"><el-tag size="small" :type="row.isGraduated === 1 ? 'success' : 'warning'">{{ row.isGraduatedText }}</el-tag></template>
            </el-table-column>
          </el-table>
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
import { getCityListApi } from '@/api/hrSocialParam'
import { getSalaryRuleListApi, type HrSalaryRuleVO } from '@/api/hrSalary'
import { getWorkweekConfigListApi } from '@/api/hr'
import { getStorage } from '@/utils/storage'

const hasAddPermission = computed(() => true)
const hasEditPermission = computed(() => true)
const activeTab = ref<Record<number, string>>({})
const query = reactive({ pageNum: 1, pageSize: 20, name: '', employeeNo: '', employeeStatus: undefined as number | undefined })
const { records, total, loading, loadData } = useTable(api.getEmployeePageApi, query)

/* ---- 组织树 ---- */
const orgTreeData = ref<any[]>([])
onMounted(async () => {
  try { orgTreeData.value = await getOrgTreeApi() } catch { /* ignore */ }
  loadData()
  await loadAllPosts()
  await loadCityList()
  await loadSalaryRuleList()
  await loadWorkweekConfigList()
})

// ---- 就职城市 ----
const cityList = ref<any[]>([])
const cityLoading = ref(false)
const loadCityList = async () => {
  cityLoading.value = true
  try {
    const res = await getCityListApi()
    cityList.value = res || []
  } catch (e) { console.error('[employee] 加载城市列表失败', e) }
  finally { cityLoading.value = false }
}

// ---- 薪资模板下拉 ----
const salaryRuleList = ref<HrSalaryRuleVO[]>([])
const salaryRuleLoading = ref(false)
const loadSalaryRuleList = async () => {
  salaryRuleLoading.value = true
  try {
    const res = await getSalaryRuleListApi()
    salaryRuleList.value = res || []
  } catch (e) { console.error('[employee] 加载薪资模板列表失败', e) }
  finally { salaryRuleLoading.value = false }
}

// ---- 岗位列表（按所属组织联动过滤）----
const allPostList = ref<api.HrPostVO[]>([])
const postLoading = ref(false)
const filteredPostList = computed(() => {
  if (!form.orgId) return []
  return allPostList.value.filter(p => p.deptId === form.orgId)
})
const loadAllPosts = async () => {
  postLoading.value = true
  try {
    const result = await api.getPostFlatListApi()
    allPostList.value = result as unknown as api.HrPostVO[]
  } finally {
    postLoading.value = false
  }
}

// 选择薪资模板时自动填充基本工资
function handleSalaryRuleChange(ruleId: number | undefined) {
  if (!ruleId) { form.basicSalary = undefined; return }
  const rule = salaryRuleList.value.find(r => r.id === ruleId)
  if (rule) form.basicSalary = rule.basicSalary
}

// ---- 休息日配置下拉 ----
const workweekConfigList = ref<api.WorkweekConfigVO[]>([])
const loadWorkweekConfigList = async () => {
  try {
    const res = await getWorkweekConfigListApi()
    workweekConfigList.value = res || []
  } catch (e) { console.error('[employee] 加载休息日配置列表失败', e) }
}
// 筛选当前公司可用的休息日配置：全局( companyId=0) + 本公司配置
const visibleWorkweekConfigs = computed(() => {
  // 先从岗位获取默认配置，显示时带名称
  return workweekConfigList.value
})

/* ---- 照片上传 ---- */
const photoUploadRef = ref()
const photoFile = ref<File | null>(null)
const photoUrl = ref<string>('')
const photoFileId = ref<number | undefined>(undefined)

function handlePhotoChange(uploadFile: any) {
  const file = uploadFile.raw
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('照片大小不能超过 5MB'); return }
  if (!['image/jpeg', 'image/png'].includes(file.type)) { ElMessage.warning('仅支持 JPG/PNG 格式'); return }
  photoFile.value = file
  photoUrl.value = URL.createObjectURL(file)
  photoFileId.value = undefined
}

/* ---- 富文本附件 ---- */
const editorInstance = shallowRef<any>(null)
const editorReady = ref(false)
const attachmentHtml = ref('')
const editorConfig = {
  placeholder: '请输入附件内容，支持文字编辑与图片上传…',
  MENU_CONF: {
    uploadImage: {
      customUpload(file: File, insertFn: (url: string) => void) {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('bizType', 'hr_employee')
        const token = getStorage('token')
        fetch('/api/base/file/upload', {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
          body: formData
        }).then(resp => resp.json()).then(res => {
          if (res.code === 200 || res.code === 0) insertFn(res.data?.previewUrl)
          else ElMessage.error(res.msg || '图片上传失败')
        }).catch(() => ElMessage.error('图片上传失败，请重试'))
      }
    }
  }
}
const handleEditorCreated = (inst: any) => { editorInstance.value = inst; editorReady.value = true }
const handleEditorDestroyed = () => { editorInstance.value = null; editorReady.value = false }

// HTML 源码编辑弹窗
const htmlDialogVisible = ref(false)
const htmlContent = ref('')
const openHtmlEditor = () => { if (!editorInstance.value) return; htmlContent.value = editorInstance.value.getHtml(); htmlDialogVisible.value = true }
const applyHtmlEdit = () => {
  if (!editorInstance.value) return
  editorInstance.value.setHtml(htmlContent.value)
  attachmentHtml.value = htmlContent.value
  htmlDialogVisible.value = false
  ElMessage.success('HTML 源码已应用')
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

/* ---- 新增/编辑 ---- */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive<api.EmployeeDTO>({ employeeNo: '', name: '', employmentType: 1 })
const rules = { employeeNo: [{ required: true, message: '工号不能为空' }], name: [{ required: true, message: '姓名不能为空' }] }

const openAddDialog = () => {
  Object.assign(form, { id: undefined, employeeNo: '', name: '', employmentType: 1, cityId: 1, salaryRuleId: undefined, workweekConfigId: undefined, exemptAttendance: 0, orgId: undefined, postId: undefined, remark: '' })
  resetEditor()
  workExps.value = []
  eduExps.value = []
  dialogVisible.value = true
}
const openHistoryAdd = () => {
  Object.assign(form, { id: undefined, employeeNo: '', name: '', employmentType: 1, orgId: undefined, postId: undefined, remark: '历史补录' })
  resetEditor()
  workExps.value = []
  eduExps.value = []
  dialogVisible.value = true
}
const openEditDialog = (row: api.EmployeeVO) => {
  Object.assign(form, { id: row.id, employeeNo: row.employeeNo, name: row.name, gender: row.gender, birthdate: row.birthdate, entryDate: row.entryDate, employmentType: row.employmentType, phone: row.phone, email: row.email, basicSalary: row.basicSalary, cityId: row.cityId, salaryRuleId: row.salaryRuleId, workweekConfigId: row.workweekConfigId, exemptAttendance: row.exemptAttendance, orgId: row.orgId, postId: row.postId, remark: row.remark, photoFileId: row.photoFileId, attachmentContent: row.attachmentContent })
  photoFile.value = null
  photoFileId.value = row.photoFileId
  photoUrl.value = row.photoPreviewUrl || ''
  attachmentHtml.value = row.attachmentContent || ''
  workExps.value = []
  eduExps.value = []
  dialogVisible.value = true
}
const resetEditor = () => {
  photoFile.value = null
  photoUrl.value = ''
  photoFileId.value = undefined
  attachmentHtml.value = ''
  htmlContent.value = ''
  htmlDialogVisible.value = false
  workExps.value = []
  eduExps.value = []
  form.cityId = 1
  form.salaryRuleId = undefined
  form.workweekConfigId = undefined
  form.exemptAttendance = 0
}
const handleDelete = (row: api.EmployeeVO) => {
  ElMessageBox.confirm('确认删除该员工?', '提示').then(async () => { await api.deleteEmployeeApi(row.id!); ElMessage.success('删除成功'); loadData() })
}
const handleSubmit = async () => {
  await formRef.value.validate()
  let uploadedPhotoFileId = photoFileId.value
  if (photoFile.value) {
    try {
      const result = await fileApi.uploadFileApi(photoFile.value, 'hr_employee')
      uploadedPhotoFileId = result.fileId
    } catch (e: any) { ElMessage.error('照片上传失败：' + (e?.message || '请稍后重试')); return }
  }
  submitLoading.value = true
  try {
    const workExpsData = workExps.value.filter(r => r.companyName)
    const eduExpsData = eduExps.value.filter(r => r.schoolName)
    const submitData = { ...form, photoFileId: uploadedPhotoFileId, attachmentContent: attachmentHtml.value, experienceData: JSON.stringify({ workExps: workExpsData, eduExps: eduExpsData }) } as any
    if (form.id) { await api.updateEmployeeApi(submitData); ElMessage.success('编辑成功') }
    else { await api.addEmployeeApi(submitData); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}
const handleReset = () => { resetQuery(); loadData() }
const resetQuery = () => { query.pageNum = 1; query.name = ''; query.employeeNo = ''; query.employeeStatus = undefined }

/* ---- 查看详情 ---- */
const viewDialogVisible = ref(false)
const viewRecord = ref<api.EmployeeVO | null>(null)
const viewActiveTab = ref('basic')
const viewWorkExps = ref<any[]>([])
const viewEduExps = ref<any[]>([])
const onExpandChange = async (row: api.EmployeeVO) => {
  if (!row) return
  if (!workExpMap.value[row.id]) { api.getWorkExpsApi(row.id).then(list => { workExpMap.value[row.id] = list }) }
  if (!eduExpMap.value[row.id]) { api.getEduExpsApi(row.id).then(list => { eduExpMap.value[row.id] = list }) }
}
const handleView = (row: api.EmployeeVO) => {
  viewRecord.value = row
  viewActiveTab.value = 'basic'
  viewWorkExps.value = []
  viewEduExps.value = []
  viewDialogVisible.value = true
  api.getWorkExpsApi(row.id!).then(list => { viewWorkExps.value = list })
  api.getEduExpsApi(row.id!).then(list => { viewEduExps.value = list })
}
const workExpMap = ref<Record<number, api.WorkExpVO[]>>({})
const eduExpMap = ref<Record<number, api.EduExpVO[]>>({})
const statusType = (status?: number) => { const m: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }; return m[status ?? 0] || '' }

/* ---- 工作经历 ---- */
const workDialogVisible = ref(false)
const workSubmitLoading = ref(false)
const workFormRef = ref()
const currentWorkEmployeeId = ref<number>()
const workForm = reactive<api.WorkExpDTO>({ employeeId: 0, companyName: '', startDate: '' })
const workRules = { companyName: [{ required: true, message: '公司名称不能为空' }], startDate: [{ required: true, message: '入职时间不能为空' }] }
const openWorkDialog = (row: api.EmployeeVO) => { Object.assign(workForm, { id: undefined, employeeId: row.id!, companyName: '', position: '', department: '', startDate: '', endDate: '', isCurrent: 0, reasonForLeaving: '', remark: '' }); currentWorkEmployeeId.value = row.id!; workDialogVisible.value = true }
const editWorkExp = (item: api.WorkExpVO, row: api.EmployeeVO) => { Object.assign(workForm, { id: item.id, employeeId: row.id!, companyName: item.companyName, position: item.position, department: item.department, startDate: item.startDate, endDate: item.endDate, isCurrent: item.isCurrent, reasonForLeaving: item.reasonForLeaving, remark: item.remark }); currentWorkEmployeeId.value = row.id!; workDialogVisible.value = true }
const handleWorkSubmit = async () => { await workFormRef.value.validate(); workSubmitLoading.value = true; try { if (workForm.id) { await api.updateWorkExpApi(workForm); ElMessage.success('编辑成功') } else { await api.addWorkExpApi(workForm); ElMessage.success('新增成功') } workDialogVisible.value = false; if (currentWorkEmployeeId.value) api.getWorkExpsApi(currentWorkEmployeeId.value).then(list => { workExpMap.value[currentWorkEmployeeId.value!] = list }) } finally { workSubmitLoading.value = false } }
const delWorkExp = (item: api.WorkExpVO) => { ElMessageBox.confirm('确认删除该工作经历?', '提示').then(async () => { await api.deleteWorkExpApi(item.id!); ElMessage.success('删除成功'); if (currentWorkEmployeeId.value) api.getWorkExpsApi(currentWorkEmployeeId.value).then(list => { workExpMap.value[currentWorkEmployeeId.value!] = list }) }) }

/* ---- 学业经历 ---- */
const eduDialogVisible = ref(false)
const eduSubmitLoading = ref(false)
const eduFormRef = ref()
const currentEduEmployeeId = ref<number>()
const eduForm = reactive<api.EduExpDTO>({ employeeId: 0, schoolName: '', startDate: '' })
const eduRules = { schoolName: [{ required: true, message: '学校名称不能为空' }], startDate: [{ required: true, message: '入学时间不能为空' }] }
const openEduDialog = (row: api.EmployeeVO) => { Object.assign(eduForm, { id: undefined, employeeId: row.id!, schoolName: '', degree: '', major: '', educationLevel: '', startDate: '', graduationDate: '', isGraduated: 0, certificateNo: '', remark: '' }); currentEduEmployeeId.value = row.id!; eduDialogVisible.value = true }
const editEduExp = (item: api.EduExpVO, row: api.EmployeeVO) => { Object.assign(eduForm, { id: item.id, employeeId: row.id!, schoolName: item.schoolName, degree: item.degree, major: item.major, educationLevel: item.educationLevel, startDate: item.startDate, graduationDate: item.graduationDate, isGraduated: item.isGraduated, certificateNo: item.certificateNo, remark: item.remark }); currentEduEmployeeId.value = row.id!; eduDialogVisible.value = true }
const handleEduSubmit = async () => { await eduFormRef.value.validate(); eduSubmitLoading.value = true; try { if (eduForm.id) { await api.updateEduExpApi(eduForm); ElMessage.success('编辑成功') } else { await api.addEduExpApi(eduForm); ElMessage.success('新增成功') } eduDialogVisible.value = false; if (currentEduEmployeeId.value) api.getEduExpsApi(currentEduEmployeeId.value).then(list => { eduExpMap.value[currentEduEmployeeId.value!] = list }) } finally { eduSubmitLoading.value = false } }
const delEduExp = (item: api.EduExpVO) => { ElMessageBox.confirm('确认删除该学业经历?', '提示').then(async () => { await api.deleteEduExpApi(item.id!); ElMessage.success('删除成功'); if (currentEduEmployeeId.value) api.getEduExpsApi(currentEduEmployeeId.value).then(list => { eduExpMap.value[currentEduEmployeeId.value!] = list }) }) }

// Editor 实例由 destroy-on-close 自动管理，无需手动销毁
</script>

<style scoped>
.wang-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.wang-editor-wrapper :deep(.w-e-toolbar) {
  border-bottom: 1px solid #dcdfe6;
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
