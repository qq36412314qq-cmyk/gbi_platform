package com.gbi.platform.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.FlowDefDTO;
import com.gbi.platform.dto.FlowHandleDTO;
import com.gbi.platform.dto.FlowQueryDTO;
import com.gbi.platform.dto.FlowSubmitDTO;
import com.gbi.platform.entity.FlowDefinition;
import com.gbi.platform.entity.FlowInstance;
import com.gbi.platform.entity.FlowRecord;
import com.gbi.platform.entity.FlowTask;
import com.gbi.platform.entity.SysRole;
import com.gbi.platform.entity.SysUserRoleRel;
import com.gbi.platform.mapper.FlowDefinitionMapper;
import com.gbi.platform.mapper.FlowInstanceMapper;
import com.gbi.platform.mapper.FlowRecordMapper;
import com.gbi.platform.mapper.FlowTaskMapper;
import com.gbi.platform.mapper.SysRoleMapper;
import com.gbi.platform.mapper.SysUserMapper;
import com.gbi.platform.mapper.SysUserRoleRelMapper;
import com.gbi.platform.service.FlowBizHandler;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.UserService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FlowConfigUtil;
import com.gbi.platform.vo.FlowDefVO;
import com.gbi.platform.vo.FlowInstanceDetailVO;
import com.gbi.platform.vo.FlowInstanceVO;
import com.gbi.platform.vo.FlowRecordVO;
import com.gbi.platform.vo.FlowTaskVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统一审批引擎服务实现（轻量表驱动状态机）
 * 状态机：提交→审批中→通过/驳回/撤回/终止；支持转交/催办/抄送/会签（全部通过才流转）
 * 安全：审批动作校验操作人归属（handler_id），无权限待办返回业务异常；全动作审计留痕
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowEngineServiceImpl implements FlowEngineService {

    private final FlowDefinitionMapper definitionMapper;

    private final FlowInstanceMapper instanceMapper;

    private final FlowTaskMapper taskMapper;

    private final FlowRecordMapper recordMapper;

    private final SysRoleMapper roleMapper;

    private final SysUserRoleRelMapper userRoleRelMapper;

    private final SysUserMapper userMapper;

    private final UserService userService;

    /** 业务回调处理器：ObjectProvider 延迟解析，打破 审批引擎→业务Handler→计划服务→审批引擎 构造环 */
    private final ObjectProvider<FlowBizHandler> bizHandlersProvider;

    private final AuditLogUtil auditLogUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(FlowSubmitDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        // 1. 流程定义校验（集团全局模板，子公司只读复用）
        FlowDefinition def = definitionMapper.selectOne(new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getDefCode, dto.getDefCode())
                .last("LIMIT 1"));
        if (def == null) {
            throw new BizException("流程定义不存在：" + dto.getDefCode());
        }
        if (!Objects.equals(def.getStatus(), CommonConst.STATUS_ENABLED)) {
            throw new BizException("流程已停用：" + def.getDefName());
        }
        List<FlowConfigUtil.NodeSpec> nodes = FlowConfigUtil.parseNodes(def.getNodeConfigJson());

        // 2. 创建流程实例
        FlowInstance instance = new FlowInstance();
        instance.setCompanyId(companyId);
        instance.setInstanceNo("FL" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + RandomUtil.randomNumbers(4));
        instance.setDefId(def.getId());
        instance.setDefName(def.getDefName());
        instance.setBizType(def.getBizType());
        instance.setSourceType(dto.getSourceType());
        instance.setSourceId(dto.getSourceId());
        instance.setTitle(dto.getTitle());
        instance.setApplyUserId(loginUser.getUserId());
        instance.setApplyUserName(loginUser.getRealName() == null ? loginUser.getUsername() : loginUser.getRealName());
        instance.setInstanceStatus(CommonConst.FLOW_STATUS_RUNNING);
        instance.setSubmitTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        // 3. 展开首节点任务
        List<Long> firstNodeHandlers = resolveNodeHandlers(nodes.get(0), companyId, loginUser.getUserId());
        createNodeTasks(instance, nodes.get(0), firstNodeHandlers, 1);
        instance.setCurrentNodeName(nodes.get(0).getNodeName());
        instance.setCurrentHandlers(toJsonArray(firstNodeHandlers));
        instanceMapper.updateById(instance);

        // 4. 抄送留痕
        writeCopyRecords(instance, nodes.get(0), companyId);

        // 5. 流转记录 + 审计
        writeRecord(instance, null, CommonConst.FLOW_ACTION_SUBMIT, dto.getTitle());
        auditLogUtil.record(CommonConst.MODULE_FLOW, CommonConst.OPER_TYPE_SUBMIT,
                String.valueOf(instance.getId()), null, instance);

        // 6. 业务提交前校验回调
        invokeHandler(def.getBizType(), h -> h.onSubmit(Long.valueOf(dto.getSourceId())));
        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(String defCode, String sourceType, String sourceId, String title) {
        FlowSubmitDTO dto = new FlowSubmitDTO();
        dto.setDefCode(defCode);
        dto.setSourceType(sourceType);
        dto.setSourceId(sourceId);
        dto.setTitle(title);
        return submit(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(FlowHandleDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();

        // 1. 任务校验：待办 + 当前操作人归属
        FlowTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null || !Objects.equals(task.getTaskStatus(), CommonConst.TASK_STATUS_PENDING)) {
            throw new BizException("审批任务不存在或已处理");
        }
        if (!Objects.equals(task.getHandlerId(), loginUser.getUserId())) {
            throw new BizException("无权处理他人审批任务");
        }
        FlowInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            throw new BizException("流程实例不存在");
        }

        String action = dto.getAction();
        if (CommonConst.FLOW_ACTION_REJECT.equals(action)) {
            reject(instance, task, dto);
        } else if (CommonConst.FLOW_ACTION_TRANSFER.equals(action)) {
            transfer(instance, task, dto);
        } else if (CommonConst.FLOW_ACTION_PASS.equals(action)) {
            pass(instance, task, dto);
        } else {
            throw new BizException("不支持的审批动作：" + action);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urge(Long taskId) {
        FlowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("审批任务不存在");
        }
        FlowInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null || !Objects.equals(instance.getInstanceStatus(), CommonConst.FLOW_STATUS_RUNNING)) {
            throw new BizException("流程未在审批中");
        }
        writeRecord(instance, null, CommonConst.FLOW_ACTION_URGE, "催办");
        auditLogUtil.record(CommonConst.MODULE_FLOW, "催办", String.valueOf(instance.getId()), null, task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long instanceId) {
        LoginUser loginUser = UserContext.getLoginUser();
        FlowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException("流程实例不存在");
        }
        if (!Objects.equals(instance.getApplyUserId(), loginUser.getUserId())) {
            throw new BizException("仅申请人可撤回申请");
        }
        if (!Objects.equals(instance.getInstanceStatus(), CommonConst.FLOW_STATUS_RUNNING)) {
            throw new BizException("当前状态不可撤回");
        }
        finishInstance(instance, CommonConst.FLOW_STATUS_REVOKE, CommonConst.FLOW_ACTION_REVOKE, "申请人撤回");
        invokeHandler(instance.getBizType(), h -> h.onCancel(Long.valueOf(instance.getSourceId()), instanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long instanceId, String remark) {
        FlowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException("流程实例不存在");
        }
        if (!Objects.equals(instance.getInstanceStatus(), CommonConst.FLOW_STATUS_RUNNING)) {
            throw new BizException("当前状态不可终止");
        }
        finishInstance(instance, CommonConst.FLOW_STATUS_TERMINATE, CommonConst.FLOW_ACTION_TERMINATE,
                StringUtils.hasText(remark) ? remark : "业务侧终止");
        invokeHandler(instance.getBizType(), h -> h.onCancel(Long.valueOf(instance.getSourceId()), instanceId));
    }

    @Override
    public PageVO<FlowTaskVO> pageMyTodo(FlowQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<FlowTask> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FlowTask> wrapper = new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getHandlerId, loginUser.getUserId())
                .eq(FlowTask::getTaskStatus, CommonConst.TASK_STATUS_PENDING)
                .orderByAsc(FlowTask::getId);
        Page<FlowTask> result = taskMapper.selectPage(page, wrapper);
        return new PageVO<>(toTaskVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public PageVO<FlowInstanceVO> pageMyApply(FlowQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<FlowInstance> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FlowInstance> wrapper = new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getApplyUserId, loginUser.getUserId())
                .eq(StringUtils.hasText(dto.getBizType()), FlowInstance::getBizType, dto.getBizType())
                .eq(dto.getInstanceStatus() != null, FlowInstance::getInstanceStatus, dto.getInstanceStatus())
                .orderByDesc(FlowInstance::getId);
        Page<FlowInstance> result = instanceMapper.selectPage(page, wrapper);
        List<FlowInstanceVO> voList = result.getRecords().stream().map(this::toInstanceVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public PageVO<FlowInstanceVO> pageInstance(FlowQueryDTO dto) {
        Page<FlowInstance> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FlowInstance> wrapper = new LambdaQueryWrapper<FlowInstance>()
                .eq(StringUtils.hasText(dto.getBizType()), FlowInstance::getBizType, dto.getBizType())
                .eq(dto.getInstanceStatus() != null, FlowInstance::getInstanceStatus, dto.getInstanceStatus())
                .eq(StringUtils.hasText(dto.getSourceId()), FlowInstance::getSourceId, dto.getSourceId())
                .orderByDesc(FlowInstance::getId);
        Page<FlowInstance> result = instanceMapper.selectPage(page, wrapper);
        List<FlowInstanceVO> voList = result.getRecords().stream().map(this::toInstanceVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public FlowInstanceDetailVO instanceDetail(Long instanceId) {
        FlowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException("流程实例不存在");
        }
        List<FlowTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getInstanceId, instanceId).orderByAsc(FlowTask::getNodeOrder));
        List<FlowRecord> records = recordMapper.selectList(new LambdaQueryWrapper<FlowRecord>()
                .eq(FlowRecord::getInstanceId, instanceId).orderByAsc(FlowRecord::getId));

        FlowInstanceDetailVO detail = new FlowInstanceDetailVO();
        detail.setInstance(toInstanceVO(instance));
        detail.setTasks(toTaskVOList(tasks));
        detail.setRecords(records.stream().map(r -> {
            FlowRecordVO vo = new FlowRecordVO();
            vo.setId(r.getId());
            vo.setInstanceId(r.getInstanceId());
            vo.setNodeName(r.getNodeName());
            vo.setAction(r.getAction());
            vo.setActionText(actionText(r.getAction()));
            vo.setHandlerId(r.getHandlerId());
            vo.setHandlerName(r.getHandlerName());
            vo.setComment(r.getComment());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).toList());
        return detail;
    }

    @Override
    public PageVO<FlowDefVO> pageDefinition(FlowQueryDTO dto) {
        Page<FlowDefinition> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<FlowDefinition> wrapper = new LambdaQueryWrapper<FlowDefinition>()
                .eq(StringUtils.hasText(dto.getBizType()), FlowDefinition::getBizType, dto.getBizType())
                .orderByAsc(FlowDefinition::getId);
        Page<FlowDefinition> result = definitionMapper.selectPage(page, wrapper);
        List<FlowDefVO> voList = result.getRecords().stream().map(def -> {
            FlowDefVO vo = new FlowDefVO();
            vo.setId(def.getId());
            vo.setCompanyId(def.getCompanyId());
            vo.setDefName(def.getDefName());
            vo.setDefCode(def.getDefCode());
            vo.setBizType(def.getBizType());
            vo.setNodeConfigJson(def.getNodeConfigJson());
            vo.setStatus(def.getStatus());
            vo.setRemark(def.getRemark());
            vo.setCreateTime(def.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDefinition(FlowDefDTO dto) {
        // 节点配置校验（非法 JSON/脚本直接拦截，不入库）
        List<FlowConfigUtil.NodeSpec> nodes = FlowConfigUtil.parseNodes(dto.getNodeConfigJson());
        checkDefCodeUnique(dto.getDefCode(), null);

        FlowDefinition def = new FlowDefinition();
        def.setCompanyId(CommonConst.COMPANY_ROOT);
        def.setDefName(dto.getDefName());
        def.setDefCode(dto.getDefCode());
        def.setBizType(dto.getBizType());
        def.setNodeConfigJson(FlowConfigUtil.sanitize(dto.getNodeConfigJson()));
        def.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        def.setRemark(dto.getRemark());
        definitionMapper.insert(def);
        auditLogUtil.record(CommonConst.MODULE_FLOW, CommonConst.OPER_TYPE_ADD,
                String.valueOf(def.getId()), null, def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefinition(FlowDefDTO dto) {
        FlowDefinition def = definitionMapper.selectById(dto.getId());
        if (def == null) {
            throw new BizException("流程定义不存在");
        }
        FlowConfigUtil.parseNodes(dto.getNodeConfigJson());
        checkDefCodeUnique(dto.getDefCode(), dto.getId());

        FlowDefinition before = new FlowDefinition();
        cn.hutool.core.bean.BeanUtil.copyProperties(def, before);
        def.setDefName(dto.getDefName());
        def.setDefCode(dto.getDefCode());
        def.setBizType(dto.getBizType());
        def.setNodeConfigJson(FlowConfigUtil.sanitize(dto.getNodeConfigJson()));
        def.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        def.setRemark(dto.getRemark());
        definitionMapper.updateById(def);
        auditLogUtil.record(CommonConst.MODULE_FLOW, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(def.getId()), before, def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefinition(Long id) {
        FlowDefinition def = definitionMapper.selectById(id);
        if (def == null) {
            throw new BizException("流程定义不存在");
        }
        Long count = instanceMapper.selectCount(new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getDefId, id));
        if (count != null && count > 0) {
            throw new BizException("该流程已产生审批实例，禁止删除");
        }
        definitionMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_FLOW, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), def, null);
    }

    @Override
    public List<Long> resolveRoleUserIds(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return Collections.emptyList();
        }
        LoginUser loginUser = UserContext.getLoginUser();
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        // 仅取集团全局角色（company_id=0）或本公司角色
        List<Long> roleIds = roles.stream()
                .filter(r -> Objects.equals(r.getCompanyId(), CommonConst.COMPANY_ROOT)
                        || Objects.equals(r.getCompanyId(), loginUser.getCompanyId()))
                .map(SysRole::getId).toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userRoleRelMapper.selectList(new LambdaQueryWrapper<SysUserRoleRel>()
                        .in(SysUserRoleRel::getRoleId, roleIds))
                .stream().map(SysUserRoleRel::getUserId).distinct().toList();
    }

    /* ============================== 私有方法 ============================== */

    /**
     * 通过节点流转：全部任务通过才进入下一节点；最后节点通过则实例完成
     */
    private void pass(FlowInstance instance, FlowTask task, FlowHandleDTO dto) {
        markTaskDone(task, CommonConst.TASK_STATUS_DONE, 1, dto.getOpinion());
        writeRecord(instance, task, CommonConst.FLOW_ACTION_PASS, dto.getOpinion());

        // 会签未完成：同节点仍有待办任务则等待
        Long remaining = taskMapper.selectCount(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getInstanceId, instance.getId())
                .eq(FlowTask::getNodeOrder, task.getNodeOrder())
                .eq(FlowTask::getTaskStatus, CommonConst.TASK_STATUS_PENDING));
        if (remaining != null && remaining > 0) {
            return;
        }

        // 进入下一节点或完成
        List<FlowConfigUtil.NodeSpec> nodes = FlowConfigUtil.parseNodes(
                definitionMapper.selectById(instance.getDefId()).getNodeConfigJson());
        if (task.getNodeOrder() < nodes.size()) {
            FlowConfigUtil.NodeSpec next = nodes.get(task.getNodeOrder());
            List<Long> nextHandlers = resolveNodeHandlers(next, instance.getCompanyId(), instance.getApplyUserId());
            createNodeTasks(instance, next, nextHandlers, task.getNodeOrder() + 1);
            instance.setCurrentNodeName(next.getNodeName());
            instance.setCurrentHandlers(toJsonArray(nextHandlers));
            instanceMapper.updateById(instance);
            writeCopyRecords(instance, next, instance.getCompanyId());
        } else {
            finishInstance(instance, CommonConst.FLOW_STATUS_PASS, null, "全部节点审批通过");
            invokeHandler(instance.getBizType(), h -> h.onPass(Long.valueOf(instance.getSourceId()), instance.getId()));
        }
    }

    /**
     * 驳回：实例驳回，作废剩余待办，回调业务
     */
    private void reject(FlowInstance instance, FlowTask task, FlowHandleDTO dto) {
        markTaskDone(task, CommonConst.TASK_STATUS_DONE, 0, dto.getOpinion());
        finishInstance(instance, CommonConst.FLOW_STATUS_REJECT, CommonConst.FLOW_ACTION_REJECT, dto.getOpinion());
        invokeHandler(instance.getBizType(), h -> h.onReject(Long.valueOf(instance.getSourceId()), instance.getId()));
    }

    /**
     * 转交：原任务作废转交，新任务复制节点信息
     */
    private void transfer(FlowInstance instance, FlowTask task, FlowHandleDTO dto) {
        if (dto.getTransferHandlerId() == null) {
            throw new BizException("转交目标用户不能为空");
        }
        task.setTaskStatus(CommonConst.TASK_STATUS_TRANSFERRED);
        task.setHandleTime(LocalDateTime.now());
        taskMapper.updateById(task);

        String targetName = userService.mapRealNameByIds(List.of(dto.getTransferHandlerId()))
                .getOrDefault(dto.getTransferHandlerId(), "用户" + dto.getTransferHandlerId());
        FlowTask newTask = new FlowTask();
        newTask.setCompanyId(instance.getCompanyId());
        newTask.setInstanceId(instance.getId());
        newTask.setNodeName(task.getNodeName());
        newTask.setNodeOrder(task.getNodeOrder());
        newTask.setHandlerId(dto.getTransferHandlerId());
        newTask.setHandlerName(targetName);
        newTask.setTaskStatus(CommonConst.TASK_STATUS_PENDING);
        newTask.setParentTaskId(task.getId());
        taskMapper.insert(newTask);
        writeRecord(instance, task, CommonConst.FLOW_ACTION_TRANSFER,
                "转交至" + targetName);
        auditLogUtil.record(CommonConst.MODULE_FLOW, "转交", String.valueOf(instance.getId()), task, newTask);
    }

    /**
     * 完成实例（终态：通过/驳回/撤回/终止），作废剩余待办
     */
    private void finishInstance(FlowInstance instance, int status, String action, String comment) {
        instance.setInstanceStatus(status);
        instance.setFinishTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        taskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<FlowTask>()
                .eq(FlowTask::getInstanceId, instance.getId())
                .eq(FlowTask::getTaskStatus, CommonConst.TASK_STATUS_PENDING)
                .set(FlowTask::getTaskStatus, CommonConst.TASK_STATUS_CANCELED));
        if (action != null) {
            writeRecord(instance, null, action, comment);
        }
        auditLogUtil.record(CommonConst.MODULE_FLOW, "完成", String.valueOf(instance.getId()), null, instance);
    }

    /**
     * 标记任务已办
     */
    private void markTaskDone(FlowTask task, int taskStatus, int approveResult, String opinion) {
        task.setTaskStatus(taskStatus);
        task.setApproveResult(approveResult);
        task.setOpinion(opinion);
        task.setHandleTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    /**
     * 展开节点审批人（role/user/submitter），并写抄送留痕
     */
    private List<Long> resolveNodeHandlers(FlowConfigUtil.NodeSpec node, Long companyId, Long submitterId) {
        List<Long> handlerIds;
        if ("user".equals(node.getHandlerType())) {
            handlerIds = List.of(Long.valueOf(node.getHandlerValue()));
        } else if ("submitter".equals(node.getHandlerType())) {
            handlerIds = List.of(submitterId);
        } else {
            handlerIds = resolveRoleUserIds(node.getHandlerValue());
        }
        if (handlerIds.isEmpty()) {
            throw new BizException("节点[" + node.getNodeName() + "]未匹配到审批人，请检查角色配置");
        }
        return handlerIds;
    }

    /**
     * 创建某节点审批任务
     */
    private void createNodeTasks(FlowInstance instance, FlowConfigUtil.NodeSpec node, List<Long> handlerIds, int nodeOrder) {
        Map<Long, String> nameMap = userService.mapRealNameByIds(handlerIds);
        for (Long handlerId : handlerIds) {
            FlowTask task = new FlowTask();
            task.setCompanyId(instance.getCompanyId());
            task.setInstanceId(instance.getId());
            task.setNodeName(node.getNodeName());
            task.setNodeOrder(nodeOrder);
            task.setHandlerId(handlerId);
            task.setHandlerName(nameMap.getOrDefault(handlerId, "用户" + handlerId));
            task.setTaskStatus(CommonConst.TASK_STATUS_PENDING);
            taskMapper.insert(task);
        }
    }

    /**
     * 抄送留痕（不阻塞流转，仅写 flow_record）
     */
    private void writeCopyRecords(FlowInstance instance, FlowConfigUtil.NodeSpec node, Long companyId) {
        if (node.getCopyTo() == null) {
            return;
        }
        for (String copy : node.getCopyTo()) {
            String roleCode = copy.startsWith("role:") ? copy.substring(5) : copy;
            List<Long> userIds = resolveRoleUserIds(roleCode);
            if (!userIds.isEmpty()) {
                writeRecord(instance, null, CommonConst.FLOW_ACTION_CC, "抄送角色：" + roleCode);
            }
        }
    }

    /**
     * 写流转记录（操作人默认取当前登录用户）
     */
    private void writeRecord(FlowInstance instance, FlowTask task, String action, String comment) {
        LoginUser loginUser = UserContext.getLoginUserOrNull();
        FlowRecord record = new FlowRecord();
        record.setCompanyId(instance.getCompanyId());
        record.setInstanceId(instance.getId());
        record.setNodeName(task == null ? (instance.getCurrentNodeName() == null ? instance.getDefName() : instance.getCurrentNodeName()) : task.getNodeName());
        record.setAction(action);
        if (loginUser != null) {
            record.setHandlerId(loginUser.getUserId());
            record.setHandlerName(loginUser.getRealName() == null ? loginUser.getUsername() : loginUser.getRealName());
        }
        record.setComment(comment);
        recordMapper.insert(record);
    }

    /**
     * 回调业务处理（按 bizType 匹配，未注册的处理器忽略）
     */
    private void invokeHandler(String bizType, HandlerConsumer consumer) {
        bizHandlersProvider.orderedStream().forEach(handler -> {
            if (handler.bizType().equals(bizType)) {
                consumer.accept(handler);
            }
        });
    }

    @FunctionalInterface
    private interface HandlerConsumer {
        void accept(FlowBizHandler handler);
    }

    private void checkDefCodeUnique(String defCode, Long excludeId) {
        LambdaQueryWrapper<FlowDefinition> wrapper = new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getDefCode, defCode);
        if (excludeId != null) {
            wrapper.ne(FlowDefinition::getId, excludeId);
        }
        Long count = definitionMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException("流程编码已存在：" + defCode);
        }
    }

    /**
     * 任务列表转 VO（组装实例摘要）
     */
    private List<FlowTaskVO> toTaskVOList(List<FlowTask> tasks) {
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> instanceIds = tasks.stream().map(FlowTask::getInstanceId).distinct().toList();
        Map<Long, FlowInstance> instanceMap = instanceMapper.selectBatchIds(instanceIds).stream()
                .collect(Collectors.toMap(FlowInstance::getId, i -> i));
        return tasks.stream().map(t -> {
            FlowTaskVO vo = new FlowTaskVO();
            vo.setId(t.getId());
            vo.setInstanceId(t.getInstanceId());
            vo.setNodeName(t.getNodeName());
            vo.setNodeOrder(t.getNodeOrder());
            vo.setHandlerId(t.getHandlerId());
            vo.setHandlerName(t.getHandlerName());
            vo.setTaskStatus(t.getTaskStatus());
            vo.setApproveResult(t.getApproveResult());
            vo.setOpinion(t.getOpinion());
            vo.setHandleTime(t.getHandleTime());
            vo.setCreateTime(t.getCreateTime());
            FlowInstance instance = instanceMap.get(t.getInstanceId());
            if (instance != null) {
                vo.setInstanceNo(instance.getInstanceNo());
                vo.setDefName(instance.getDefName());
                vo.setBizType(instance.getBizType());
                vo.setTitle(instance.getTitle());
                vo.setApplyUserName(instance.getApplyUserName());
                vo.setInstanceStatus(instance.getInstanceStatus());
                vo.setSubmitTime(instance.getSubmitTime());
            }
            return vo;
        }).toList();
    }

    private FlowInstanceVO toInstanceVO(FlowInstance instance) {
        FlowInstanceVO vo = new FlowInstanceVO();
        vo.setId(instance.getId());
        vo.setCompanyId(instance.getCompanyId());
        vo.setInstanceNo(instance.getInstanceNo());
        vo.setDefId(instance.getDefId());
        vo.setDefName(instance.getDefName());
        vo.setBizType(instance.getBizType());
        vo.setSourceType(instance.getSourceType());
        vo.setSourceId(instance.getSourceId());
        vo.setTitle(instance.getTitle());
        vo.setApplyUserId(instance.getApplyUserId());
        vo.setApplyUserName(instance.getApplyUserName());
        vo.setInstanceStatus(instance.getInstanceStatus());
        vo.setInstanceStatusText(instanceStatusText(instance.getInstanceStatus()));
        vo.setCurrentNodeName(instance.getCurrentNodeName());
        vo.setSubmitTime(instance.getSubmitTime());
        vo.setFinishTime(instance.getFinishTime());
        vo.setCreateTime(instance.getCreateTime());
        return vo;
    }

    private String toJsonArray(List<Long> ids) {
        return "[" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }

    private String instanceStatusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CommonConst.FLOW_STATUS_RUNNING -> "审批中";
            case CommonConst.FLOW_STATUS_PASS -> "通过";
            case CommonConst.FLOW_STATUS_REJECT -> "驳回";
            case CommonConst.FLOW_STATUS_REVOKE -> "撤回";
            case CommonConst.FLOW_STATUS_TERMINATE -> "终止";
            default -> "未知";
        };
    }

    private String actionText(String action) {
        if (action == null) {
            return null;
        }
        return switch (action) {
            case CommonConst.FLOW_ACTION_SUBMIT -> "提交";
            case CommonConst.FLOW_ACTION_PASS -> "通过";
            case CommonConst.FLOW_ACTION_REJECT -> "驳回";
            case CommonConst.FLOW_ACTION_REVOKE -> "撤回";
            case CommonConst.FLOW_ACTION_TRANSFER -> "转交";
            case CommonConst.FLOW_ACTION_URGE -> "催办";
            case CommonConst.FLOW_ACTION_CC -> "抄送";
            case CommonConst.FLOW_ACTION_TERMINATE -> "终止";
            default -> action;
        };
    }
}