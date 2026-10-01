<!--
  考勤管理页面（Tab 整合版）
  数据表: hr_attendance_record, hr_attendance_exception, sys_workweek_config, sys_holiday_config, hr_employee_shift
  权限: hr:attendance:*
  侧边栏唯一入口: /hr/attendance
-->
<template>
  <div class="g-page-wrap">
    <!-- 页面头部 -->
    <div class="g-page-header">
      <span class="g-page-title">考勤管理</span>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="attendance-tabs" stretch>
      <!-- ========== Tab1: 考勤记录 ========== -->
      <el-tab-pane label="考勤记录" name="record">
        <!-- 月度统计卡片 -->
        <el-row :gutter="12" class="summary-row" v-if="selectedMonth">
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card">
              <div class="summary-label">应出勤天数</div>
              <div class="summary-value">{{ summaryData.workDays ?? '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card">
              <div class="summary-label">实际出勤天数</div>
              <div class="summary-value">{{ summaryData.actualDays ?? '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card">
              <div class="summary-label">迟到总次数</div>
              <div class="summary-value late">{{ summaryData.lateCount ?? '-' }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card">
              <div class="summary-label">迟到总分钟</div>
              <div class="summary-value late">{{ summaryData.totalLateMinutes ?? '-' }}</div>
            </el-card>
          </el-col>
        </el-row>

        <div class="tab-toolbar">
          <SearchBar :model="query" @search="loadData" @reset="handleReset" inline>
            <el-form-item label="考勤月份">
              <el-date-picker v-model="query.attendanceMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
            </el-form-item>
            <el-form-item label="员工">
              <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width:140px">
                <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="打卡状态">
              <el-select v-model="query.clockType" placeholder="全部" clearable style="width:100px">
                <el-option label="正常" :value="1" /><el-option label="迟到" :value="2" />
                <el-option label="早退" :value="3" /><el-option label="缺卡" :value="4" /><el-option label="旷工" :value="5" />
              </el-select>
            </el-form-item>
          </SearchBar>
          <div class="tab-actions">
            <AuthBtn permission="hr:attendance:export" @click="handleExport" :loading="exportLoading">导出</AuthBtn>
            <AuthBtn permission="hr:attendance:sync" type="primary" @click="syncVisible=true" :loading="syncLoading">同步打卡</AuthBtn>
          </div>
        </div>

        <TablePage v-model:page-num="query.pageNum" v-model:page-size="query.pageSize" :total="total" @refresh="loadData">
          <el-table v-loading="loading" :data="records" border stripe size="small">
            <el-table-column prop="attendanceDay" label="日期" width="110" align="center" sortable />
            <el-table-column prop="employeeName" label="姓名" width="90" align="center" />
            <el-table-column prop="attendanceMonth" label="月份" width="100" align="center" />
            <el-table-column prop="clockInTime" label="上班打卡" width="155" align="center">
              <template #default="{ row }">
                <span :class="clockInClass(row)">{{ formatTime(row.clockInTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="clockOutTime" label="下班打卡" width="155" align="center">
              <template #default="{ row }">{{ formatTime(row.clockOutTime) || '-' }}</template>
            </el-table-column>
            <el-table-column prop="clockTypeText" label="打卡状态" width="85" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="clockTypeTag(row.clockType)">{{ row.clockTypeText || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lateMinutes" label="迟到(分)" width="85" align="center">
              <template #default="{ row }"><span v-if="row.lateMinutes" class="late-text">{{ row.lateMinutes }}</span><span v-else>-</span></template>
            </el-table-column>
            <el-table-column prop="earlyMinutes" label="早退(分)" width="85" align="center">
              <template #default="{ row }"><span v-if="row.earlyMinutes" class="late-text">{{ row.earlyMinutes }}</span><span v-else>-</span></template>
            </el-table-column>
            <el-table-column prop="workDays" label="应出勤" width="80" align="center" />
            <el-table-column prop="actualDays" label="实际出勤" width="85" align="center" />
            <el-table-column prop="leaveDays" label="请假天数" width="85" align="center">
              <template #default="{ row }">{{ row.leaveDays ? Number(row.leaveDays).toFixed(1) : '-' }}</template>
            </el-table-column>
          </el-table>
        </TablePage>
      </el-tab-pane>

      <!-- ========== Tab2: 考勤异常 ========== -->
      <el-tab-pane label="考勤异常" name="exception">
        <div class="tab-toolbar">
          <SearchBar :model="exceptionQuery" @search="loadExceptions" @reset="handleExceptionReset" inline>
            <el-form-item label="异常类型">
              <el-select v-model="exceptionQuery.exceptionType" placeholder="全部" clearable style="width:120px">
                <el-option label="连续缺卡" :value="1" /><el-option label="月度迟到频繁" :value="2" />
                <el-option label="旷工" :value="3" /><el-option label="早退频繁" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="处理状态">
              <el-select v-model="exceptionQuery.status" placeholder="全部" clearable style="width:100px">
                <el-option label="待处理" :value="0" /><el-option label="已确认" :value="1" />
                <el-option label="已豁免" :value="2" /><el-option label="已忽略" :value="3" />
              </el-select>
            </el-form-item>
          </SearchBar>
          <div class="tab-actions">
            <AuthBtn permission="hr:attendance:sync" @click="detectExceptions" :loading="detectLoading">重新检测</AuthBtn>
          </div>
        </div>

        <el-table v-loading="exceptionLoading" :data="exceptionRecords" border stripe size="small">
          <el-table-column prop="employeeName" label="员工姓名" width="100" align="center" />
          <el-table-column prop="exceptionTypeText" label="异常类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="exceptionTypeTag(row.exceptionType)">{{ row.exceptionTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="exceptionDate" label="异常日期" width="110" align="center" />
          <el-table-column prop="detailCount" label="详情数量" width="90" align="center" />
          <el-table-column prop="statusText" label="处理状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="exceptionStatusTag(row.status)">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handleRemark" label="处理备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" link @click="openHandleDialog(row)">处理</el-button>
              <span v-else size="small" style="color:#909399">已处理</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="exceptionTotal > 0" style="margin-top:12px;text-align:right"
          :current-page="exceptionQuery.pageNum" :page-size="exceptionQuery.pageSize" :total="exceptionTotal"
          layout="total, prev, pager, next" @current-change="(v: number) => { exceptionQuery.pageNum = v; loadExceptions() }" />
      </el-tab-pane>

      <!-- ========== Tab3: 休息日配置 ========== -->
      <el-tab-pane label="休息日配置" name="workweek">
        <!-- 说明提示 -->
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom:12px"
        >
          <template #title>
            <div style="font-size:13px;line-height:1.8">
              <b>配置说明：</b>休息日配置用于定义不同员工的考勤规则，系统将根据此配置自动计算每月应出勤天数。
              <br><b>配置方法：</b>选择"双休"等基础规则，或在"休息规则"列填写具体休息日（如"周六、周日"）。支持单休/双休/做五休二/做六休一/综合工时等多种类型。
              <br><b>子公司调用：</b>子公司可在「组织岗位」页面设置本公司的默认休息日配置ID；也可在「员工档案」中为单个员工单独指定休息日配置，覆盖岗位默认值。
            </div>
          </template>
        </el-alert>
        <div class="tab-toolbar">
          <SearchBar :model="workweekQuery" @search="loadWorkweeks" @reset="handleWorkweekReset" inline>
            <el-form-item label="配置名称">
              <el-input v-model="workweekQuery.configName" placeholder="搜索" clearable style="width:140px" />
            </el-form-item>
          </SearchBar>
          <div class="tab-actions">
            <AuthBtn @click="openWorkweekDialog()">新增配置</AuthBtn>
          </div>
        </div>
        <el-table v-loading="workweekLoading" :data="workweekRecords" border stripe size="small">
          <el-table-column prop="configName" label="配置名称" width="160" />
          <el-table-column prop="workweekTypeText" label="休息日类型" width="120" align="center" />
          <el-table-column prop="restDayPattern" label="休息规则" min-width="140" />
          <el-table-column prop="statusText" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status !== 1" size="small" link type="primary" @click="setDefaultWorkweek(row)">设为默认</el-button>
              <el-button size="small" link type="primary" @click="openWorkweekDialog(row)">编辑</el-button>
              <el-button size="small" link type="danger" @click="handleDeleteWorkweek(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="workweekTotal > 0" style="margin-top:12px;text-align:right"
          :current-page="workweekQuery.pageNum" :page-size="workweekQuery.pageSize" :total="workweekTotal"
          layout="total, prev, pager, next" @current-change="(v: number) => { workweekQuery.pageNum = v; loadWorkweeks() }" />
      </el-tab-pane>

      <!-- ========== Tab4: 节假日配置 ========== -->
      <el-tab-pane label="节假日配置" name="holiday">
        <div class="tab-toolbar">
          <SearchBar :model="holidayQuery" @search="loadHolidays" @reset="handleHolidayReset" inline>
            <el-form-item label="年份">
              <el-date-picker v-model="holidayQuery.year" type="year" value-format="YYYY" placeholder="选择年份" style="width:120px" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="holidayQuery.holidayType" placeholder="全部" clearable style="width:120px">
                <el-option label="法定假日" :value="1" /><el-option label="调休日" :value="2" /><el-option label="补班日" :value="3" />
              </el-select>
            </el-form-item>
          </SearchBar>
          <div class="tab-actions">
            <AuthBtn @click="openHolidayDialog()">新增节假日</AuthBtn>
          </div>
        </div>
        <el-table v-loading="holidayLoading" :data="holidayRecords" border stripe size="small">
          <el-table-column prop="holidayDate" label="日期" width="120" align="center" />
          <el-table-column prop="holidayName" label="节假日名称" width="160" />
          <el-table-column prop="holidayTypeText" label="类型" width="100" align="center" />
          <el-table-column label="是否工作日" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.isWorkday === 1 ? 'success' : 'info'">{{ row.isWorkday === 1 ? '补班' : '休息' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" link type="danger" @click="handleDeleteHoliday(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="holidayTotal > 0" style="margin-top:12px;text-align:right"
          :current-page="holidayQuery.pageNum" :page-size="holidayQuery.pageSize" :total="holidayTotal"
          layout="total, prev, pager, next" @current-change="(v: number) => { holidayQuery.pageNum = v; loadHolidays() }" />
      </el-tab-pane>

      <!-- ========== Tab5: 班次管理 ========== -->
      <el-tab-pane label="员工班次" name="shift">
        <div class="tab-toolbar">
          <SearchBar :model="shiftQuery" @search="loadShifts" @reset="handleShiftReset" inline>
            <el-form-item label="员工">
              <el-select v-model="shiftQuery.employeeId" placeholder="全部" clearable filterable style="width:160px">
                <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
          </SearchBar>
          <div class="tab-actions">
            <AuthBtn @click="openShiftDialog()">新增班次</AuthBtn>
          </div>
        </div>
        <el-table v-loading="shiftLoading" :data="shiftRecords" border stripe size="small">
          <el-table-column prop="employeeName" label="员工" width="100" align="center" />
          <el-table-column prop="shiftTypeText" label="班次类型" width="100" align="center" />
          <el-table-column prop="shiftStartTime" label="上班时间" width="100" align="center" />
          <el-table-column prop="shiftEndTime" label="下班时间" width="100" align="center" />
          <el-table-column prop="startDate" label="生效日期" width="110" align="center" />
          <el-table-column prop="endDate" label="结束日期" width="110" align="center" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" link type="danger" @click="handleDeleteShift(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== Tab6: 参数配置 ========== -->
      <el-tab-pane label="参数配置" name="config">
        <div class="config-panel">
          <div class="config-title">考勤阈值参数（sys_config）</div>
          <div class="config-desc">以下参数控制考勤同步和异常检测逻辑，修改后立即生效。</div>
          <el-table :data="configList" border stripe size="small">
            <el-table-column prop="key" label="参数键" width="260" />
            <el-table-column label="当前值" min-width="200">
              <template #default="{ row }">
                <el-input v-if="editingKey === row.key" v-model="editForm[row.key]" size="small" />
                <span v-else>{{ configMap[row.key] ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="说明" min-width="200" />
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <template v-if="editingKey === row.key">
                  <el-button size="small" link type="primary" @click="saveConfigRow(row)">保存</el-button>
                  <el-button size="small" link @click="editingKey = null">取消</el-button>
                </template>
                <template v-else>
                  <el-button size="small" link type="primary" @click="startEditConfig(row)">编辑</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top:12px">
            <AuthBtn permission="hr:attendance:edit" type="primary" @click="batchSaveConfig" :loading="configSaving">批量保存全部修改</AuthBtn>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 同步打卡弹窗 ========== -->
    <el-dialog v-model="syncVisible" title="考勤同步" width="560px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="考勤月份">
          <el-date-picker v-model="syncForm.month" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:100%" />
        </el-form-item>
        <el-form-item label="员工范围">
          <el-radio-group v-model="syncForm.employeeScope">
            <el-radio :value="0">全部在职员工</el-radio>
            <el-radio :value="1">指定员工</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="syncForm.employeeScope === 1" label="选择员工">
          <el-select v-model="syncForm.employeeIds" multiple filterable placeholder="请选择员工" style="width:100%">
            <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-radio-group v-model="syncForm.syncType">
            <el-radio :value="1">首次同步</el-radio>
            <el-radio :value="2">重新同步（覆盖已有记录）</el-radio>
          </el-radio-group>
          <div style="font-size:12px;color:#909399;margin-top:4px">重新同步将清除该月所有考勤记录后重新生成</div>
        </el-form-item>
        <el-form-item label="预览结果" v-if="syncPreview">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="工作日总数">{{ syncPreview.workDays }}天</el-descriptions-item>
            <el-descriptions-item label="员工数">{{ syncPreview.employeeCount }}人</el-descriptions-item>
            <el-descriptions-item label="预计生成">{{ syncPreview.willCreate }}条</el-descriptions-item>
            <el-descriptions-item label="OA打卡记录">{{ syncPreview.clockRecords }}条</el-descriptions-item>
            <el-descriptions-item label="满勤（免考勤）">{{ syncPreview.fullAttendance }}条</el-descriptions-item>
            <el-descriptions-item label="正常打卡">{{ syncPreview.normal }}条</el-descriptions-item>
            <el-descriptions-item label="迟到">{{ syncPreview.late }}条</el-descriptions-item>
            <el-descriptions-item label="缺卡/旷工">{{ syncPreview.absent }}条</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="syncVisible=false">取消</el-button>
        <el-button type="primary" :loading="syncLoading" @click="confirmSync">确认同步</el-button>
      </template>
    </el-dialog>

    <!-- ========== 异常处理弹窗 ========== -->
    <el-dialog v-model="handleVisible" title="处理考勤异常" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="异常类型">
          <el-tag>{{ currentException?.exceptionTypeText }}</el-tag>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.handleType">
            <el-radio :value="1">已确认</el-radio>
            <el-radio :value="2">已豁免</el-radio>
            <el-radio :value="3">已忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible=false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="confirmHandle">确认</el-button>
      </template>
    </el-dialog>

    <!-- ========== 休息日配置弹窗 ========== -->
    <el-dialog v-model="workweekVisible" :title="workweekForm.id ? '编辑休息日配置' : '新增休息日配置'" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="配置名称">
          <el-input v-model="workweekForm.configName" placeholder="如：做五休二" />
        </el-form-item>
        <el-form-item label="休息日类型">
          <el-select v-model="workweekForm.workweekType" style="width:100%">
            <el-option label="单休" :value="1" /><el-option label="双休" :value="2" />
            <el-option label="做五休二" :value="3" /><el-option label="做六休一" :value="4" /><el-option label="综合工时" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="休息规则">
          <el-input v-model="workweekForm.restDayPattern" placeholder="如：周六,周日 或 Sunday,Saturday" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="workweekForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="workweekForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workweekVisible=false">取消</el-button>
        <el-button type="primary" :loading="workweekSaving" @click="saveWorkweek">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 节假日配置弹窗 ========== -->
    <el-dialog v-model="holidayVisible" title="新增节假日配置" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="日期">
          <el-date-picker v-model="holidayForm.holidayDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="节假日名称">
          <el-input v-model="holidayForm.holidayName" placeholder="如：元旦、春节调休" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="holidayForm.holidayType" style="width:100%">
            <el-option label="法定假日" :value="1" /><el-option label="调休日" :value="2" /><el-option label="补班日" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否工作日">
          <el-radio-group v-model="holidayForm.isWorkday">
            <el-radio :value="0">休息日</el-radio>
            <el-radio :value="1">补班日</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="holidayForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="holidayVisible=false">取消</el-button>
        <el-button type="primary" :loading="holidaySaving" @click="saveHoliday">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 班次配置弹窗 ========== -->
    <el-dialog v-model="shiftVisible" title="新增员工班次" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="员工">
          <el-select v-model="shiftForm.employeeId" filterable placeholder="请选择员工" style="width:100%">
            <el-option v-for="e in employeeList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班次类型">
          <el-select v-model="shiftForm.shiftType" style="width:100%">
            <el-option label="标准工时" :value="1" /><el-option label="早班" :value="2" />
            <el-option label="晚班" :value="3" /><el-option label="夜班" :value="4" /><el-option label="综合工时" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="上班时间">
          <el-time-picker v-model="shiftForm.shiftStartTime" value-format="HH:mm" placeholder="如 08:30" style="width:100%" />
        </el-form-item>
        <el-form-item label="下班时间">
          <el-time-picker v-model="shiftForm.shiftEndTime" value-format="HH:mm" placeholder="如 18:30" style="width:100%" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="shiftForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="shiftForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shiftVisible=false">取消</el-button>
        <el-button type="primary" :loading="shiftSaving" @click="saveShift">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/useTable'
import * as api from '@/api/hr'
import type { EmployeeVO } from '@/api/hr'
import { useUserStore } from '@/store/user'
import type { AttendanceExceptionVO, AttendanceSyncPreviewVO } from '@/api/hr'

/* ========== Tab 切换 ========== */
const activeTab = ref('record')

watch(activeTab, (val) => {
  if (val === 'exception') loadExceptions()
  if (val === 'workweek') loadWorkweeks()
  if (val === 'holiday') loadHolidays()
  if (val === 'shift') { loadEmployees(); loadShifts() }  // 补充loadEmployees
  if (val === 'config') loadConfig()
})

/* ========== 员工下拉（多处复用） ========== */
const employeeList = ref<EmployeeVO[]>([])
const loadEmployees = async () => {
  try {
    const result = await api.getEmployeePageApi({ pageNum: 1, pageSize: 500 })
    employeeList.value = result.records.filter(e => e.employeeStatus === 1 || e.employeeStatus === 2)
  } catch {}
}

/* ========== 月度汇总 ========== */
const selectedMonth = computed(() => query.attendanceMonth)
const summaryData = ref<{ workDays?: number; actualDays?: number; lateCount?: number; totalLateMinutes?: number }>({})
const loadSummary = async (month: string) => {
  if (!month) { summaryData.value = {}; return }
  try {
    const list = await api.exportAttendanceApi({ attendanceMonth: month })
    summaryData.value = {
      workDays: list.length,
      actualDays: list.filter(r => r.clockType === 1).length,
      lateCount: list.filter(r => r.clockType === 2).length,
      totalLateMinutes: list.reduce((sum, r) => sum + (r.lateMinutes ?? 0), 0)
    }
  } catch { summaryData.value = {} }
}

/* ========== Tab1: 考勤记录 ========== */
const { query, records, total, loading, loadData } = useTable(api.getAttendancePageApi, {
  pageSize: 20,
  attendanceMonth: undefined as string | undefined,
  employeeId: undefined as number | undefined,
  clockType: undefined as number | undefined,
})

/* ---- 同步打卡 ---- */
const syncVisible = ref(false)
const syncLoading = ref(false)
const syncPreview = ref<AttendanceSyncPreviewVO | null>(null)
const syncForm = reactive({ month: '', employeeScope: 0 as number, employeeIds: [] as number[], syncType: 1 as number })

watch(syncVisible, async (val) => {
  if (!val) { syncPreview.value = null; return }
  syncForm.month = query.attendanceMonth || new Date().toISOString().slice(0, 7)
  syncForm.employeeScope = 0; syncForm.employeeIds = []; syncForm.syncType = 1
  if (syncForm.month) {
    try { syncPreview.value = await api.syncAttendancePreviewApi({ month: syncForm.month, employeeScope: 0, syncType: 1 }) } catch {}
  }
})

const confirmSync = async () => {
  if (!syncForm.month) { ElMessage.warning('请选择考勤月份'); return }
  syncLoading.value = true
  try {
    const count = await api.syncAttendanceAdvancedApi({ month: syncForm.month, employeeScope: syncForm.employeeScope, employeeIds: syncForm.employeeIds, syncType: syncForm.syncType })
    ElMessage.success(`同步完成，共生成 ${count} 条考勤记录`)
    syncVisible.value = false
    loadData()
    loadSummary(syncForm.month)
    if (activeTab.value === 'exception') loadExceptions()
  } finally { syncLoading.value = false }
}

/* ---- 导出 ---- */
const exportLoading = ref(false)
const handleExport = async () => {
  if (!query.attendanceMonth) { ElMessage.warning('请先选择考勤月份'); return }
  exportLoading.value = true
  try {
    const list = await api.exportAttendanceApi({ employeeId: query.employeeId, attendanceMonth: query.attendanceMonth })
    const headers = ['日期', '姓名', '上班打卡', '下班打卡', '打卡状态', '迟到(分)', '早退(分)', '应出勤', '实际出勤', '请假天数']
    const rows = list.map(r => [
      r.attendanceDay ?? '', r.employeeName ?? '',
      r.clockInTime ? formatDateTime(r.clockInTime) : '', r.clockOutTime ? formatDateTime(r.clockOutTime) : '',
      r.clockTypeText ?? '', r.lateMinutes ?? '', r.earlyMinutes ?? '',
      r.workDays ?? '', r.actualDays ?? '', r.leaveDays ? Number(r.leaveDays).toFixed(1) : ''
    ])
    const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(r => r.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = `考勤_${query.attendanceMonth}.csv`; a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) { ElMessage.error('导出失败：' + (e?.message || '')) } finally { exportLoading.value = false }
}

const handleReset = () => {
  query.pageNum = 1; query.attendanceMonth = undefined; query.employeeId = undefined; query.clockType = undefined
  summaryData.value = {}; loadData()
}

/* ========== Tab2: 考勤异常 ========== */
const exceptionLoading = ref(false)
const exceptionRecords = ref<AttendanceExceptionVO[]>([])
const exceptionTotal = ref(0)
const exceptionQuery = reactive({ pageNum: 1, pageSize: 20, exceptionType: undefined as number | undefined, status: undefined as number | undefined })

const loadExceptions = async () => {
  exceptionLoading.value = true
  try {
    const res = await api.getAttendanceExceptionsApi({ pageNum: exceptionQuery.pageNum, pageSize: exceptionQuery.pageSize, exceptionType: exceptionQuery.exceptionType, status: exceptionQuery.status })
    exceptionRecords.value = res.records; exceptionTotal.value = res.total
  } finally { exceptionLoading.value = false }
}
const handleExceptionReset = () => { exceptionQuery.pageNum = 1; exceptionQuery.exceptionType = undefined; exceptionQuery.status = undefined; loadExceptions() }

const detectLoading = ref(false)
const detectExceptions = async () => {
  detectLoading.value = true
  try {
    const month = query.attendanceMonth || new Date().toISOString().slice(0, 7)
    await api.detectAttendanceExceptionsApi(month)
    ElMessage.success('异常检测完成')
    loadExceptions()
  } finally { detectLoading.value = false }
}

/* ---- 异常处理弹窗 ---- */
const handleVisible = ref(false); const handleLoading = ref(false)
const currentException = ref<AttendanceExceptionVO | null>(null)
const handleForm = reactive({ handleType: 1 as number, handleRemark: '' })

const openHandleDialog = (row: AttendanceExceptionVO) => {
  currentException.value = row; handleForm.handleType = 1; handleForm.handleRemark = ''; handleVisible.value = true
}
const confirmHandle = async () => {
  if (!currentException.value) return
  handleLoading.value = true
  try {
    const userStore = useUserStore()
    await api.handleAttendanceExceptionApi(currentException.value.id, handleForm.handleType, handleForm.handleRemark, userStore.userInfo?.id)
    ElMessage.success('处理成功'); handleVisible.value = false; loadExceptions()
  } finally { handleLoading.value = false }
}

/* ========== Tab3: 休息日配置 ========== */
const workweekLoading = ref(false)
const workweekRecords = ref<any[]>([]); const workweekTotal = ref(0)
const workweekQuery = reactive({ pageNum: 1, pageSize: 10, configName: '' })

const loadWorkweeks = async () => {
  workweekLoading.value = true
  try {
    const res = await api.getWorkweekConfigListApi()
    workweekRecords.value = res ?? []
    workweekTotal.value = res?.length ?? 0
  } finally { workweekLoading.value = false }
}
const handleWorkweekReset = () => { workweekQuery.pageNum = 1; workweekQuery.configName = ''; loadWorkweeks() }

const workweekVisible = ref(false); const workweekSaving = ref(false)
const workweekForm = reactive({ id: undefined as number | undefined, configName: '', workweekType: 1, restDayPattern: '', status: 1, remark: '' })

const openWorkweekDialog = (row?: any) => {
  Object.assign(workweekForm, { id: row?.id, configName: row?.configName ?? '', workweekType: row?.workweekType ?? 1, restDayPattern: row?.restDayPattern ?? '', status: row?.status ?? 1, remark: row?.remark ?? '' })
  workweekVisible.value = true
}
const saveWorkweek = async () => {
  if (!workweekForm.configName) { ElMessage.warning('请输入配置名称'); return }
  workweekSaving.value = true
  try {
    if (workweekForm.id) { await api.updateWorkweekConfigApi(workweekForm); ElMessage.success('更新成功') }
    else { await api.createWorkweekConfigApi(workweekForm); ElMessage.success('新增成功') }
    workweekVisible.value = false; loadWorkweeks()
  } finally { workweekSaving.value = false }
}
const setDefaultWorkweek = async (row: any) => {
  await api.setDefaultWorkweekConfigApi(row.id); ElMessage.success('已设为默认'); loadWorkweeks()
}
const handleDeleteWorkweek = async (row: any) => {
  await ElMessageBox.confirm(`确定删除休息日配置「${row.configName}」吗？`, '提示', { type: 'warning' })
  await api.deleteWorkweekConfigApi(row.id); ElMessage.success('删除成功'); loadWorkweeks()
}

/* ========== Tab4: 节假日配置 ========== */
const holidayLoading = ref(false)
const holidayRecords = ref<any[]>([]); const holidayTotal = ref(0)
const holidayQuery = reactive({ pageNum: 1, pageSize: 10, year: undefined as string | undefined, holidayType: undefined as number | undefined })

const loadHolidays = async () => {
  holidayLoading.value = true
  try {
    const res = await api.getHolidayConfigListApi({ year: holidayQuery.year ? Number(holidayQuery.year) : undefined })
    holidayRecords.value = res ?? []
    holidayTotal.value = res?.length ?? 0
  } finally { holidayLoading.value = false }
}
const handleHolidayReset = () => { holidayQuery.pageNum = 1; holidayQuery.year = undefined; holidayQuery.holidayType = undefined; loadHolidays() }

const holidayVisible = ref(false); const holidaySaving = ref(false)
const holidayForm = reactive({ holidayDate: '', holidayName: '', holidayType: 1, isWorkday: 0, remark: '' })

const openHolidayDialog = () => { Object.assign(holidayForm, { holidayDate: '', holidayName: '', holidayType: 1, isWorkday: 0, remark: '' }); holidayVisible.value = true }
const saveHoliday = async () => {
  if (!holidayForm.holidayDate || !holidayForm.holidayName) { ElMessage.warning('请填写日期和名称'); return }
  holidaySaving.value = true
  try {
    await api.createHolidayConfigApi(holidayForm); ElMessage.success('新增成功'); holidayVisible.value = false; loadHolidays()
  } finally { holidaySaving.value = false }
}
const handleDeleteHoliday = async (row: any) => {
  await ElMessageBox.confirm(`确定删除节假日「${row.holidayName}」吗？`, '提示', { type: 'warning' })
  try {
    await api.deleteHolidayConfigApi(row.id)
    ElMessage.success('删除成功'); loadHolidays()
  } catch { ElMessage.warning('节假日删除接口暂未开放，请联系管理员') }
}

/* ========== Tab5: 员工班次 ========== */
const shiftLoading = ref(false)
const shiftRecords = ref<any[]>([])
const shiftQuery = reactive({ pageNum: 1, pageSize: 20, employeeId: undefined as number | undefined })

const loadShifts = async () => {
  shiftLoading.value = true
  try {
    const res = await api.listEmployeeShiftsApi(shiftQuery.employeeId ?? 0)
    shiftRecords.value = res ?? []
  } catch { shiftRecords.value = [] }
  finally { shiftLoading.value = false }
}
const handleShiftReset = () => { shiftQuery.pageNum = 1; shiftQuery.employeeId = undefined; loadShifts() }

const shiftVisible = ref(false); const shiftSaving = ref(false)
const shiftForm = reactive({ employeeId: undefined as number | undefined, shiftType: 1, shiftStartTime: '', shiftEndTime: '', startDate: '', endDate: '' })

const openShiftDialog = () => { Object.assign(shiftForm, { employeeId: undefined, shiftType: 1, shiftStartTime: '', shiftEndTime: '', startDate: '', endDate: '' }); shiftVisible.value = true }
const saveShift = async () => {
  if (!shiftForm.employeeId) { ElMessage.warning('请选择员工'); return }
  shiftSaving.value = true
  try {
    await api.addEmployeeShiftApi(shiftForm); ElMessage.success('新增成功'); shiftVisible.value = false; loadShifts()
  } finally { shiftSaving.value = false }
}
const handleDeleteShift = async (row: any) => {
  await ElMessageBox.confirm('确定删除该班次配置吗？', '提示', { type: 'warning' })
  await api.deleteEmployeeShiftApi(row.id); ElMessage.success('删除成功'); loadShifts()
}

/* ========== Tab6: 参数配置 ========== */
const configList = [
  { key: 'attendance.late_threshold', desc: '迟到阈值（分钟）' },
  { key: 'attendance.absent_threshold', desc: '旷工阈值（分钟）' },
  { key: 'attendance.early_threshold', desc: '早退阈值（分钟）' },
  { key: 'attendance.normal_clockin_start', desc: '标准上班时间' },
  { key: 'attendance.normal_clockout_end', desc: '标准下班时间' },
  { key: 'attendance.absent_reminder_threshold', desc: '连续缺卡提醒阈值（次）' },
  { key: 'attendance.late_frequent_threshold', desc: '月度迟到频繁阈值（次）' },
  { key: 'attendance.reminder_enabled', desc: '是否启用自动提醒 1/0' },
  { key: 'attendance.reminder_method', desc: '提醒方式（钉钉/系统）' },
]
const configMap = ref<Record<string, string>>({})
const editingKey = ref<string | null>(null)
const editForm = reactive<Record<string, string>>({})
const configSaving = ref(false)

const loadConfig = async () => {
  try {
    const res = await api.getAttendanceConfigsApi()
    configMap.value = res ?? {}
  } catch { configMap.value = {} }
}
const startEditConfig = (row: { key: string }) => {
  editForm[row.key] = configMap.value[row.key] ?? ''
  editingKey.value = row.key
}
const saveConfigRow = async (row: { key: string }) => {
  configSaving.value = true
  try {
    await api.updateAttendanceConfigsApi({ [row.key]: editForm[row.key] })
    configMap.value[row.key] = editForm[row.key]
    editingKey.value = null
    ElMessage.success('已保存')
  } finally { configSaving.value = false }
}
const batchSaveConfig = async () => {
  const toSave: Record<string, string> = {}
  for (const c of configList) {
    if (editForm[c.key] !== undefined) toSave[c.key] = editForm[c.key]
    else if (configMap.value[c.key] !== undefined) toSave[c.key] = configMap.value[c.key]
  }
  configSaving.value = true
  try {
    await api.updateAttendanceConfigsApi(toSave)
    ElMessage.success('批量保存成功')
    loadConfig()
  } finally { configSaving.value = false }
}

/* ========== 工具函数 ========== */
const formatTime = (t?: string) => t ? t.substring(11, 16) : ''
const formatDateTime = (t?: string) => t ? t.substring(0, 16) : ''
const clockTypeMap: Record<number, string> = { 1: '正常', 2: '迟到', 3: '早退', 4: '缺卡', 5: '旷工' }
const clockTypeTagMap: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'warning', 4: 'danger', 5: 'danger' }
const clockTypeTag = (v?: number) => v != null ? (clockTypeTagMap[v] ?? '') : ''
const clockInClass = (row: api.AttendanceVO) => row.clockType === 2 ? 'clock-late' : ''
const exceptionTypeTag = (v?: number) => { const map: Record<number, string> = { 1: 'danger', 2: 'warning', 3: 'danger', 4: 'warning' }; return map[v ?? 0] ?? '' }
const exceptionStatusTag = (v?: number) => { const map: Record<number, string> = { 0: 'danger', 1: 'success', 2: 'info', 3: 'info' }; return map[v ?? 0] ?? '' }

onMounted(() => { loadEmployees(); loadData() })
</script>

<style scoped>
.summary-row { margin-bottom: 12px; }
.summary-card { text-align: center; cursor: default; }
.summary-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.summary-value { font-size: 22px; font-weight: 700; color: #303133; }
.summary-value.late { color: #e6a23c; }
.clock-late { color: #e6a23c; font-weight: 600; }
.late-text { color: #e6a23c; font-weight: 600; }

.attendance-tabs { margin-top: 12px; }
.attendance-tabs :deep(.el-tabs__item) { font-size: 14px; font-weight: 500; }
.tab-toolbar { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 12px; }
.tab-toolbar :deep(.g-search-bar) { flex: 1; }
.tab-actions { display: flex; gap: 8px; padding-top: 4px; flex-shrink: 0; }

.config-panel { padding: 12px; background: #fafafa; border-radius: 4px; }
.config-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.config-desc { font-size: 12px; color: #909399; margin-bottom: 12px; }
</style>
