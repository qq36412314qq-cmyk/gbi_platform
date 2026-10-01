# B端企业微信接入技术方案（v3.0）

## 一、方案概述

### 1.1 背景
企业微信（WeCom）是腾讯为企业提供的办公协同平台，支持企业内部沟通、客户管理、应用集成等功能。本方案详细说明如何将集团多业态一体化管控系统与企点微信对接，实现消息推送、审批通知、考勤同步等核心能力。

### 1.2 接入目标
- **基础数据层**：组织架构、员工档案双向同步，确保主数据一致性
- **数据采集层**：企微作为打卡数据采集端，同步到系统考勤模块
- **消息通知层**：审批结果、考勤异常、工资条等实时推送
- **流程联动层**：审批流程与企微互通，员工可通过企微便捷操作

### 1.3 适用场景与对接优先级
| 优先级 | 模块 | 场景 | 对接方式 |
|--------|------|------|----------|
| P0 | 组织架构 | 部门结构同步 | 系统→企微（单向推送） |
| P0 | 员工档案 | 入职同步创建、离职同步禁用 | 系统→企微（单向推送） |
| P1 | 打卡管理 | 打卡数据采集 | 企微→系统（双向同步） |
| P1 | 请假模块 | 申请提交、审批结果同步 | 双向集成 |
| P1 | 审批模块 | 待办提醒、审批结果通知 | 回调接收 |
| P2 | 工作汇报 | 日报/周报审批通过推送 | 单向推送 |
| P2 | 会议室管理 | 预约结果通知 | 单向推送 |
| P2 | 公告管理 | 发布同步到企微 | 单向推送 |
| P3 | 加班管理 | 打卡数据采集、审批回调 | 双向集成 |

### 1.4 不对接模块
| 模块 | 原因 |
|------|------|
| 薪资核算 | 涉及敏感薪酬数据，企微不承载计算逻辑 |
| 社保公积金 | 专业HR系统功能，企微无对应能力 |
| 报表统计 | 内部分析工具，无需对外推送 |

---

## 二、系统对接架构

### 2.1 现有系统模块分析

#### 2.1.1 可对接模块清单
```
┌─────────────────────────────────────────────────────────────┐
│                       可对接模块                             │
├─────────────────────────────────────────────────────────────┤
│  组织架构管理（org/OrgController）                           │
│  ├─ 部门树查询               → 企微通讯录同步               │
│  └─ 部门增删改               → 企微部门变更回调             │
├─────────────────────────────────────────────────────────────┤
│  员工档案管理（hr/employee）                                 │
│  ├─ 入职审批通过             → 企微创建成员                  │
│  ├─ 离职审批通过             → 企微禁用成员                  │
│  └─ 信息变更                 → 企微更新成员信息              │
├─────────────────────────────────────────────────────────────┤
│  打卡管理（oa/clock）                                        │
│  ├─ 打卡数据采集             → 企微API拉取                   │
│  └─ 考勤记录同步             → 企微回调通知                  │
├─────────────────────────────────────────────────────────────┤
│  请假管理（oa/leave）                                        │
│  ├─ 请假申请提交             → 企微表单同步                  │
│  └─ 审批结果回写             → 企微审批回调                  │
├─────────────────────────────────────────────────────────────┤
│  审批流程（flow/）                                           │
│  ├─ 待办任务提醒             → 企微消息推送                  │
│  ├─ 审批通过通知             → 企微消息推送                  │
│  └─ 审批驳回通知             → 企微消息推送                  │
├─────────────────────────────────────────────────────────────┤
│  工作汇报（oa/workReport）                                   │
│  └─ 审批通过通知             → 企微消息推送（可选）          │
├─────────────────────────────────────────────────────────────┤
│  会议室管理（oa/meetingRoom）                                │
│  └─ 预约结果通知             → 企微消息推送（可选）          │
├─────────────────────────────────────────────────────────────┤
│  公告管理（oa/announcement）                                 │
│  └─ 发布同步                 → 企微公告同步（可选）          │
├─────────────────────────────────────────────────────────────┤
│  加班管理（新增模块）                                        │
│  ├─ 打卡数据采集             → 企微API拉取                   │
│  └─ 审批回调                 → 企微审批回调                  │
└─────────────────────────────────────────────────────────────┘
```

#### 2.1.2 数据流向设计
```
┌─────────────────────────────────────────────────────────────────────┐
│                         数据流向设计                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   【主数据推送】系统 → 企微                                         │
│   ┌─────────────────────────────────────────────────────────┐     │
│   │  组织架构 → 企微通讯录      │  员工档案 → 企微成员管理    │     │
│   │  公告发布 → 企微公告        │                             │     │
│   └─────────────────────────────────────────────────────────┘     │
│                                                                     │
│   【数据采集】企微 → 系统                                           │
│   ┌─────────────────────────────────────────────────────────┐     │
│   │  打卡记录 → 考勤表          │  请假申请 → 请假表          │     │
│   │  加班申请 → 加班表          │  审批结果 → 流程实例        │     │
│   └─────────────────────────────────────────────────────────┘     │
│                                                                     │
│   【消息通知】系统 → 企微                                           │
│   ┌─────────────────────────────────────────────────────────┐     │
│   │  审批待办提醒        │  审批结果通知        │             │     │
│   │  考勤异常通知        │  工资条推送          │             │     │
│   │  会议提醒            │  公告同步            │             │     │
│   └─────────────────────────────────────────────────────────┘     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 对接架构图
```
┌─────────────────────────────────────────────────────────────────────┐
│                         企业微信平台                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  应用消息  │  │  通讯录   │  │  打卡管理  │  │  审批流   │           │
│  │  (通知)   │  │  (同步)   │  │  (采集)   │  │  (回调)   │           │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘           │
└───────┼─────────────┼─────────────┼─────────────┼───────────────────┘
        │             │             │             │
        ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    消息网关层 (WecomGateway)                         │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  - 请求签名验证                                               │   │
│  │  - AES加密/解密                                               │   │
│  │  - 消息格式转换                                               │   │
│  │  - 频率限制控制                                               │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      对接服务层 (WecomIntegrationService)            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ 通讯录   │  │ 消息发送  │  │ 打卡同步  │  │ 审批回调  │           │
│  │ 服务     │  │ 服务     │  │ 服务     │  │ 服务     │           │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘           │
└───────┼─────────────┼─────────────┼─────────────┼───────────────────┘
        │             │             │             │
        ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        业务系统层                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ HR服务   │  │ 考勤服务  │  │ 审批服务  │  │ OA服务   │           │
│  │ - 员工   │  │ - 打卡   │  │ - 流程   │  │ - 请假   │           │
│  │ - 组织   │  │ - 异常   │  │ - 任务   │  │ - 汇报   │           │
│  │ - 异动   │  │ - 统计   │  │ - 实例   │  │ - 会议   │           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 三、核心对接场景设计

### 3.1 P0级对接：组织架构与员工档案

#### 3.1.1 组织架构同步
```
触发时机：
- 系统新增/编辑/删除部门时
- 定时全量同步（每日凌晨2点）

同步方向：系统 → 企微
同步内容：
- 部门ID、名称、父部门ID
- 部门负责人
- 部门排序

实现方式：
- 监听 org 模块的增删改事件
- 调用企微通讯录API推送变更
- 记录同步日志到 wecom_contact_sync 表
```

#### 3.1.2 员工档案同步
```
触发时机：
- 入职审批通过时
- 离职审批通过时
- 员工信息变更时
- 定时全量同步（每日凌晨3点）

同步方向：系统 → 企微
同步内容：
- 员工ID、姓名、手机号
- 所属部门
- 职位、邮箱
- 状态（在职/离职）

实现方式：
- 监听 hr_entry/resign 流程回调
- 调用企微成员管理API
- 离职时同步禁用企微账号
```

### 3.2 P1级对接：打卡管理与请假模块

#### 3.2.1 打卡数据采集
```
触发时机：
- 每日凌晨1点定时拉取前一日数据
- 手动触发全量同步

采集方向：企微 → 系统
采集内容：
- 员工打卡时间
- 打卡地点
- 打卡设备

处理逻辑：
- 以企微打卡ID为唯一键（幂等）
- 映射到系统 employee_id
- 写入 hr_attendance_record 表
- 触发加班自动识别（如有）

注意事项：
- 需处理网络超时、数据缺失等情况
- 失败记录写入 wecom_sync_record 表
```

#### 3.2.2 请假模块集成
```
申请提交：
- 员工在系统提交请假申请
- 同步创建企微审批表单
- 审批流程以系统为准

结果回写：
- 企微审批结果回调系统
- 更新请假状态
- 同步考勤记录（请假天数）

实现方式：
- 新增 wecom_leave_sync 表记录同步状态
- 监听请假流程审批回调
- 调用企微审批API获取结果
```

### 3.3 P2级对接：消息通知

#### 3.3.1 审批消息通知
```
触发时机：
- 流程任务创建时（待办提醒）
- 审批通过时（结果通知）
- 审批驳回时（结果通知）

通知对象：
- 待办提醒 → 审批人
- 结果通知 → 申请人

消息模板：
- 待办提醒：包含任务标题、审批人、节点、跳转链接
- 结果通知：包含申请标题、结果、审批意见、跳转链接
```

#### 3.3.2 考勤异常通知
```
触发时机：
- 打卡异常检测时（迟到、早退、缺卡）
- 考勤月度汇总时

通知对象：
- 员工本人
- 部门主管（可选）

消息内容：
- 异常类型、日期、时长
- 处理建议链接
```

#### 3.3.3 工资条推送
```
触发时机：
- 每月10日（薪资核算完成后）

通知对象：
- 员工本人

消息内容：
- 月份、应发金额、实发金额
- 详细构成链接
```

### 3.4 与加班模块的关系

#### 3.4.1 打卡数据采集
加班模块的自动识别依赖企微打卡数据：
```
每日22:00定时任务
    ↓
从企微拉取当日打卡记录
    ↓
过滤请假/出差人员
    ↓
判断下班时间 > 班次结束时间
    ↓
生成待确认加班记录
```

#### 3.4.2 审批回调
加班申请审批结果回写：
```
企微审批回调
    ↓
解析审批结果
    ↓
更新加班申请状态
    ↓
生成加班记录
```

#### 3.4.3 避免重复开发
| 功能 | 企业微信支持 | 本系统开发 |
|------|-------------|-----------|
| 加班申请入口 | ✅ 支持 | ❌ 放弃，改用企微 |
| 打卡数据采集 | ✅ 支持 | ❌ 使用企微数据 |
| 加班时长计算 | ❌ 不支持 | ✅ 必须自研 |
| 调休管理 | ❌ 不支持 | ✅ 必须自研 |
| 加班费计算 | ❌ 不支持 | ✅ 必须自研 |

---

## 四、数据库设计

### 4.1 企业微信消息记录表
```sql
CREATE TABLE `wecom_message_record` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  `msg_id` varchar(64) NOT NULL COMMENT '企业微信消息ID',
  `msg_type` varchar(32) NOT NULL COMMENT '消息类型: text/markdown/news等',
  `from_user` varchar(64) NOT NULL COMMENT '发送者UserID',
  `to_user` varchar(64) NOT NULL COMMENT '接收者UserID',
  `agent_id` int(11) NOT NULL COMMENT '应用ID',
  `content` text COMMENT '消息内容',
  `media_id` varchar(128) DEFAULT NULL COMMENT '媒体文件ID',
  `send_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发送状态 0待发送 1成功 2失败',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_msg_id`(`msg_id` ASC) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_to_user`(`to_user` ASC) USING BTREE,
  INDEX `idx_send_status`(`send_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业微信消息记录表';
```

### 4.2 企业微信同步记录表
```sql
CREATE TABLE `wecom_sync_record` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属公司ID',
  `sync_type` varchar(32) NOT NULL COMMENT '同步类型: contact/attendance/leave等',
  `sync_direction` tinyint NOT NULL COMMENT '同步方向 1系统→企微 2企微→系统',
  `source_id` varchar(64) DEFAULT NULL COMMENT '源数据ID',
  `target_id` varchar(64) DEFAULT NULL COMMENT '目标数据ID',
  `sync_status` tinyint NOT NULL DEFAULT 0 COMMENT '同步状态 0待同步 1成功 2失败',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `sync_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_sync_type`(`sync_type` ASC) USING BTREE,
  INDEX `idx_sync_status`(`sync_status` ASC) USING BTREE,
  INDEX `idx_sync_time`(`sync_time` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业微信同步记录表';
```

### 4.3 sys_config 配置项
```sql
-- 添加到 sys_config 表
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `remark`, `create_by`, `create_time`, `is_delete`) VALUES
('wecom.corp_id', 'ww1234567890abcdef', '企业微信企业ID', '企业微信企业管理后台获取', 1, NOW(), 0),
('wecom.approval.agent_id', '1000001', '审批通知应用ID', '用于审批流程消息推送', 1, NOW(), 0),
('wecom.attendance.agent_id', '1000002', '考勤提醒应用ID', '用于考勤相关消息推送', 1, NOW(), 0),
('wecom.salary.agent_id', '1000003', '薪资通知应用ID', '用于薪资相关消息推送', 1, NOW(), 0),
('wecom.callback.url', 'https://your-domain.com/api/wecom/callback', '消息回调URL', '需公网可访问', 1, NOW(), 0),
('wecom.attendance.auto_sync', 'true', '是否自动同步打卡数据', '开启后每日自动拉取打卡记录', 1, NOW(), 0),
('wecom.contact.auto_sync', 'true', '是否自动同步通讯录', '开启后部门/员工变更自动同步', 1, NOW(), 0),
('wecom.web_url', 'https://your-domain.com', '系统Web地址', '用于消息中的跳转链接', 1, NOW(), 0);
```

---

## 五、安全机制

### 5.1 签名验证
所有来自企业微信的请求都必须验证签名，防止伪造请求：

```java
/**
 * 企业微信请求签名验证过滤器
 */
@Component
public class WecomSignatureFilter implements Filter {
    
    @Resource
    private WecomConfig wecomConfig;
    
    @Resource
    private WecomCryptoUtil cryptoUtil;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // 仅对回调接口进行验证
        if (!req.getRequestURI().contains("/wecom/callback")) {
            chain.doFilter(request, response);
            return;
        }
        
        String msgSignature = req.getParameter("msg_signature");
        String timestamp = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        
        // 验证签名
        boolean isValid = cryptoUtil.checkSignature(
            wecomConfig.getToken(), timestamp, nonce, 
            getEchoString(req), msgSignature);
        
        if (!isValid) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid signature");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

### 5.2 数据加密
敏感数据（如成员手机号、客户信息）传输时进行加密：

```java
/**
 * 数据加密工具类
 */
public class DataEncryptUtil {
    
    /**
     * AES-256加密
     */
    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            DigestUtils.sha256(key).substring(0, 32).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * AES-256解密
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            DigestUtils.sha256(key).substring(0, 32).getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
```

---

## 六、错误处理与重试机制

### 6.1 错误码处理
```java
/**
 * 企业微信错误码处理
 */
@Component
public class WecomErrorHandler {
    
    private static final Map<Integer, String> ERROR_MESSAGES = Map.of(
        0, "成功",
        40001, "获取access_token时AppSecret错误，或者access_token无效",
        40002, "不合法的凭证类型",
        40003, "不合法的OpenID",
        40014, "不合法的access_token",
        42001, "access_token超时",
        45009, "超过消息发送频次限制",
        45010, "菜单创建超出上限",
        99998, "未命中callback匹配，请检查url配置",
        99999, "系统繁忙，此时请开发者稍候再试"
    );
    
    /**
     * 处理企业微信错误
     */
    public void handleErrorCode(int errcode, String errmsg) {
        String message = ERROR_MESSAGES.getOrDefault(errcode, "未知错误: " + errmsg);
        log.error("企业微信接口错误 [{}]: {}", errcode, message);
        
        // 根据错误类型进行不同处理
        switch (errcode) {
            case 42001:
            case 40001:
                // token过期，刷新后重试
                refreshTokenAndRetry();
                break;
            case 40014:
                // token无效，重新获取
                refreshToken();
                break;
            case 45009:
                // 超过消息发送频次限制
                log.warn("消息发送频次限制，请稍后重试");
                break;
            case 99998:
                log.warn("未命中callback匹配，请检查URL配置");
                break;
            case 99999:
                log.warn("系统繁忙，稍候重试");
                break;
            default:
                throw new BizException("企业微信接口错误: " + errcode + " - " + message);
        }
    }
}
```

### 6.2 重试机制
```java
/**
 * 企业微信接口重试模板
 */
@Component
public class WecomRetryTemplate {
    
    @Resource
    private WecomTokenService tokenService;
    
    /**
     * 带重试的API调用
     */
    public <T> T executeWithRetry(Supplier<T> apiCall, int maxRetries) {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= maxRetries) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                retryCount++;
                lastException = e;
                
                if (retryCount > maxRetries) {
                    log.error("接口调用失败，已重试{}次", maxRetries, e);
                    throw e;
                }
                
                // 指数退避重试
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BizException("重试被中断", ie);
                }
            }
        }
        
        throw new BizException("接口调用失败", lastException);
    }
}
```

---

## 六、配置层解耦设计

### 6.1 设计原则

企微对接配置采用**双轨制**管理：

| 配置层级 | 存储位置 | 使用场景 | 示例 |
|----------|----------|----------|------|
| **基础设施层** | `application-{env}.yml` + 环境变量 | 不可动态变更、涉及安全敏感信息 | corpId、token、aesKey、webhookUrl |
| **业务配置层** | `sys_config` 表 | 业务人员可动态调整、支持多租户 | 同步开关、cron表达式、超时重试参数 |

### 6.2 配置分类清单

#### 6.2.1 保留在 application.yml（基础设施层）

```yaml
wecom:
  # 以下为基础设施配置，生产环境通过环境变量覆盖
  # 注意：corpId 已移除（见6.4节），改从 wecom_tenant_config 表动态读取
  token: ${WECOM_TOKEN}               # 消息回调Token，安全敏感
  encoding-aes-key: ${WECOM_AES_KEY}  # 加密密钥，安全敏感
  callback-url: ${WECOM_CALLBACK_URL} # 回调URL，需公网可达
  web-url: ${SYSTEM_WEB_URL}          # 系统访问地址
  
  # API调用参数（固定值，无需动态调整）
  connect-timeout: 5000
  read-timeout: 10000
  max-retry-count: 3
```

**corpId 不保留在 application.yml 的原因**：
- corpId 需要支持多租户（集团+各子公司独立企微），硬编码在 yml 中无法按 company_id 区分
- corpId 属于可管理的业务数据，应存储在数据库中便于维护和历史追溯
- 新增/切换企微实例时，只需更新 `wecom_tenant_config` 表，无需修改配置文件或重启服务

**仍保留在 application.yml 的原因**：
- Token/AES Key 属于安全敏感信息，禁止明文存储于数据库
- callbackUrl/webUrl 在应用启动时即需加载，运行时变更需重启

#### 6.2.2 迁移至 sys_config 表（业务配置层）

| config_key | config_name | 类型 | 说明 |
|------------|-------------|------|------|
| `wecom.sync.attendance.enabled` | 打卡数据同步开关 | Boolean | 是否启用自动同步 |
| `wecom.sync.contact.enabled` | 通讯录同步开关 | Boolean | 是否启用通讯录同步 |
| `wecom.sync.attendance.cron` | 打卡同步Cron | String | 同步触发时机，如 `0 0 1 * * ?` |
| `wecom.sync.contact.cron` | 通讯录同步Cron | String | 同步触发时机，如 `0 0 2 * * ?` |
| `wecom.api.timeout.connect` | API连接超时(ms) | Integer | 可动态调整，无需重启 |
| `wecom.api.timeout.read` | API读取超时(ms) | Integer | 可动态调整 |
| `wecom.api.retry.count` | 重试次数 | Integer | 失败自动重试次数 |
| `wecom.api.retry.delay.seconds` | 重试间隔(秒) | Integer | 指数退避基础间隔 |
| `wecom.agent.approval.id` | 审批应用AgentId | Integer | 审批通知应用ID |
| `wecom.agent.attendance.id` | 考勤应用AgentId | Integer | 考勤提醒应用ID |
| `wecom.agent.salary.id` | 薪资应用AgentId | Integer | 工资条推送应用ID |
| `wecom.message.approval.enabled` | 审批消息通知开关 | Boolean | 审批结果是否推送到企微 |
| `wecom.message.attendance.enabled` | 考勤异常通知开关 | Boolean | 考勤异常是否推送 |
| `wecom.message.salary.enabled` | 薪资通知开关 | Boolean | 工资条生成后是否推送 |

**迁移优势**：
- 业务人员可在前端配置页面动态调整，无需修改配置文件或重启服务
- 支持多租户隔离（`company_id` 字段），子公司可单独配置
- 变更历史可追溯（基于 `BaseEntity` 的审计字段）

### 6.3 配置加载架构

```
┌─────────────────────────────────────────────────────────────┐
│                    配置加载流程                              │
├─────────────────────────────────────────────────────────────┤
│  应用启动                                                   │
│       ↓                                                     │
│  ┌─────────────────────────────────────┐                   │
│  │ Step1: Spring Boot 加载              │                   │
│  │ application.yml → WecomProperties   │                   │
│  │ (基础配置：token/aesKey/callbackUrl等) │                   │
│  └─────────────────────────────────────┘                   │
│       ↓                                                     │
│  ┌─────────────────────────────────────┐                   │
│  │ Step2: WecomConfigService.@PostConstruct │               │
│  │ ① 从 sys_config 表加载业务配置     │                   │
│  │ ② 从 wecom_tenant_config 表加载 corpId │                │
│  │ 缓存至 ConcurrentHashMap / 内存变量  │                   │
│  └─────────────────────────────────────┘                   │
│       ↓                                                     │
│  ┌─────────────────────────────────────┐                   │
│  │ Step3: 定时刷新（可选）              │                   │
│  │ 每5分钟重新加载一次，检测变更        │                   │
│  └─────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    数据源对照                                │
├─────────────────────────────────────────────────────────────┤
│  application.yml + 环境变量        → token / aesKey / callbackUrl / webUrl  │
│  sys_config 表（company_id=0）     → 同步开关 / cron / API参数 / 消息开关    │
│  wecom_tenant_config 表（company_id=0）→ corpId（集团默认）                       │
│  wecom_tenant_config 表（company_id=N）→ corpId（子公司独立企微）                 │
└─────────────────────────────────────────────────────────────┘
```

### 6.4 核心代码结构

#### 6.4.1 配置属性类 `WecomProperties.java`

```java
@Data
@ConfigurationProperties(prefix = "wecom")
public class WecomProperties {
    // 基础设施配置（不可动态变更，通过环境变量覆盖）
    private String token;
    private String encodingAesKey;
    private String callbackUrl;
    private String webUrl;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
    private int maxRetryCount = 3;
    
    // corpId 已从 application.yml 移除，改为从 wecom_tenant_config 表动态读取
    // 通过 WecomTenantConfigService.getCorpId() 获取
}
```

#### 6.4.2 业务配置服务 `WecomConfigService.java`

```java
@Service
public class WecomConfigService {
    
    /** 业务配置缓存，key = configKey */
    private final ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();
    
    /** corpId 缓存（按 company_id 隔离，0=集团默认） */
    private volatile Long cachedCorpId = null;
    private volatile Long cachedCorpIdCompanyId = null; // 对应哪个 company_id
    
    /**
     * 初始化：从 sys_config 表加载所有 wecom 相关业务配置
     */
    @PostConstruct
    public void init() {
        List<SysConfig> configs = sysConfigMapper.selectList(
            new LambdaQueryWrapper<SysConfig>()
                .like(SysConfig::getConfigKey, "wecom.")
        );
        configs.forEach(c -> configCache.put(c.getConfigKey(), c.getConfigValue()));
        log.info("企微业务配置加载完成，共 {} 项", configCache.size());
    }
    
    /**
     * 初始化/刷新 corpId（从 wecom_tenant_config 表读取，支持多租户）
     * 集团级默认 corpId：company_id=0
     */
    @PostConstruct
    public void refreshCorpId() {
        WecomTenantConfig tenant = wecomTenantConfigMapper.selectOne(
            new LambdaQueryWrapper<WecomTenantConfig>()
                .eq(WecomTenantConfig::getCompanyId, 0L)
                .eq(WecomTenantConfig::getStatus, 1)
        );
        if (tenant != null) {
            this.cachedCorpId = tenant.getCorpId();
            this.cachedCorpIdCompanyId = 0L;
            log.info("企微 corpId 已加载（集团级）: corpId={}", tenant.getCorpId());
        } else {
            log.warn("企微 wecom_tenant_config 表中无集团级（company_id=0）配置，corpId 将为 null");
        }
    }
    
    /**
     * 获取当前租户的 corpId（多租户扩展入口）
     * 若指定 company_id 无独立配置，则回退到集团级（company_id=0）
     */
    public Long getCorpId(Long companyId) {
        if (companyId != null && companyId > 0 && !companyId.equals(cachedCorpIdCompanyId)) {
            // 子公司有独立企微配置时查询，否则复用集团默认
            WecomTenantConfig tenant = wecomTenantConfigMapper.selectOne(
                new LambdaQueryWrapper<WecomTenantConfig>()
                    .eq(WecomTenantConfig::getCompanyId, companyId)
                    .eq(WecomTenantConfig::getStatus, 1)
            );
            if (tenant != null && tenant.getCorpId() != null) {
                return tenant.getCorpId();
            }
        }
        return cachedCorpId; // 集团级默认
    }
    
    /**
     * 获取配置值，不存在时返回默认值
     */
    public String get(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return "1".equals(val) || "true".equalsIgnoreCase(val);
    }
    
    public int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try { return Integer.parseInt(val); } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 刷新业务配置（可由管理接口触发）
     */
    public void refresh() {
        configCache.clear();
        init();
    }
    
    /**
     * 刷新 corpId（可由管理接口触发）
     */
    public void refreshCorpId(Long companyId) {
        WecomTenantConfig tenant = wecomTenantConfigMapper.selectOne(
            new LambdaQueryWrapper<WecomTenantConfig>()
                .eq(WecomTenantConfig::getCompanyId, companyId != null ? companyId : 0L)
                .eq(WecomTenantConfig::getStatus, 1)
        );
        if (tenant != null) {
            this.cachedCorpId = tenant.getCorpId();
            this.cachedCorpIdCompanyId = companyId != null ? companyId : 0L;
            log.info("企微 corpId 已刷新（companyId={}）: corpId={}", companyId, tenant.getCorpId());
        }
    }
}
```

#### 6.4.3 租户配置实体 `WecomTenantConfig.java`（新增）

```java
@Data
@TableName("wecom_tenant_config")
public class WecomTenantConfig extends BaseEntity {
    /** 所属公司ID，0=集团默认 */
    private Long companyId;
    /** 企业ID（企微 corpId） */
    private String corpId;
    /** 状态 0禁用 1启用 */
    private Integer status;
    /** 备注 */
    private String remark;
}
```

#### 6.4.4 前端配置管理页面

在「企微配置管理」模块提供可视化配置界面，业务人员可直接在页面编辑 `sys_config` 记录，支持：
- 单个参数编辑
- 批量导入/导出
- 变更历史查看
- 灰度生效（指定子公司）

---

## 七、部署配置

### 7.1 配置分层部署策略

企微配置采用**双层存储**架构，不同层级的配置对应不同的部署方式：

```
┌─────────────────────────────────────────────────────────────┐
│                    配置分层部署图                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────────┐         ┌─────────────────────┐  │
│   │  application.yml     │         │  sys_config 表      │  │
│   │  (基础设施层)         │         │  (业务配置层)        │  │
│   ├─────────────────────┤         ├─────────────────────┤  │
│   │ corpId / token       │         │ sync.attendance.    │  │
│   │ aesKey / callbackUrl │         │   enabled/cron      │  │
│   │ webUrl               │    ↕    │ sync.contact.       │  │
│   │ connectTimeout       │  互补    │   enabled/cron      │  │
│   │ readTimeout          │         │ api.timeout.*       │  │
│   │ maxRetryCount        │         │ api.retry.*         │  │
│   │                      │         │ agent.*.id          │  │
│   │                      │         │ message.*.enabled   │  │
│   └─────────────────────┘         └─────────────────────┘  │
│                                                             │
│   变更方式: 环境变量 / 配置中心       变更方式: 前端页面动态编辑   │
│   生效方式: 重启服务                  生效方式: 实时生效（缓存刷新） │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 application.yml 最小化配置

```yaml
wecom:
# ========== 基础设施层配置（不可动态变更，通过环境变量覆盖）==========
# 注意：corpId 已从 application.yml 移除，改为从 wecom_tenant_config 表动态读取
token: ${WECOM_TOKEN}               # 消息回调Token（安全敏感，禁止硬编码）
encoding-aes-key: ${WECOM_AES_KEY}  # 加密密钥（安全敏感，禁止硬编码）
callback-url: ${WECOM_CALLBACK_URL} # 回调URL（需HTTPS、公网可访问）
web-url: ${SYSTEM_WEB_URL}          # 系统Web地址（用于消息中的跳转链接）

# ========== API调用基础参数（固定值，无需动态调整）==========
connect-timeout: 5000
read-timeout: 10000
max-retry-count: 3

# 定时任务保持启用（用于业务配置刷新）
spring:
task:
scheduling:
enabled: true
```

### 7.3 数据库表初始化

#### 7.3.1 wecom_tenant_config（企微租户配置表）

> 此表是 corpId 多租户化的核心，替代 application.yml 中的固定 corpId，支持集团+多子公司各自独立的企微实例。

```sql
-- 企微租户配置表：支持集团+子公司多企微实例
DROP TABLE IF EXISTS `wecom_tenant_config`;
CREATE TABLE `wecom_tenant_config` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属公司ID，0=集团全局默认',
  `corp_id` varchar(64) NOT NULL COMMENT '企业微信企业ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company` (`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微租户配置表（支持多企微实例）';

-- 初始化集团级默认企微配置（上线时由管理员填写真实 corpId）
INSERT INTO wecom_tenant_config (company_id, corp_id, status, remark, create_by, create_time)
VALUES (0, '', 1, '集团级默认企微配置，上线前请填写真实corpId', 1, NOW());
```

#### 7.3.2 sys_config 表初始配置SQL

```sql
-- 企微业务配置初始化（集团级，company_id=0）
INSERT INTO sys_config (company_id, config_key, config_name, config_value, remark, create_time) VALUES
-- 同步开关配置
(0, 'wecom.sync.attendance.enabled', '打卡数据同步开关', '0', '默认关闭，上线后手动开启', NOW()),
(0, 'wecom.sync.contact.enabled', '通讯录同步开关', '0', '默认关闭，上线后手动开启', NOW()),
-- 同步时机配置
(0, 'wecom.sync.attendance.cron', '打卡同步Cron表达式', '0 0 1 * * ?', '每日凌晨1点执行', NOW()),
(0, 'wecom.sync.contact.cron', '通讯录同步Cron表达式', '0 0 2 * * ?', '每日凌晨2点执行', NOW()),
-- API调用参数（可动态调整）
(0, 'wecom.api.timeout.connect', 'API连接超时(ms)', '5000', '默认5秒', NOW()),
(0, 'wecom.api.timeout.read', 'API读取超时(ms)', '10000', '默认10秒', NOW()),
(0, 'wecom.api.retry.count', '重试次数', '3', '失败自动重试', NOW()),
(0, 'wecom.api.retry.delay.seconds', '重试间隔(秒)', '2', '指数退避基础间隔', NOW()),
-- 应用AgentId配置
(0, 'wecom.agent.approval.id', '审批通知AgentId', '', '在企微管理后台获取', NOW()),
(0, 'wecom.agent.attendance.id', '考勤提醒AgentId', '', '在企微管理后台获取', NOW()),
(0, 'wecom.agent.salary.id', '薪资通知AgentId', '', '在企微管理后台获取', NOW()),
-- 消息推送开关
(0, 'wecom.message.approval.enabled', '审批消息通知开关', '1', '默认开启', NOW()),
(0, 'wecom.message.attendance.enabled', '考勤异常通知开关', '1', '默认开启', NOW()),
(0, 'wecom.message.salary.enabled', '薪资通知开关', '0', '默认关闭', NOW());
```

### 7.4 安全配置要求
1. **HTTPS证书**：回调URL必须使用HTTPS
2. **IP白名单**：在应用详情页配置服务器IP白名单
3. **Token保密**：Token和EncodingAESKey不得硬编码在代码中，使用环境变量管理
4. **访问控制**：回调接口增加签名验证过滤器

---

## 八、测试方案

### 8.1 单元测试
```java
@SpringBootTest
class WecomIntegrationTest {
    
    @Resource
    private WecomMessageService messageService;
    
    @Test
    void testSendTextMessage() {
        WecomResult result = messageService.sendTextMessage(
            "zhangsan", "测试消息", 1000001);
        Assertions.assertEquals(0, result.getErrcode());
    }
    
    @Test
    void testDecryptMessage() throws Exception {
        WecomCryptoUtil cryptoUtil = new WecomCryptoUtil();
        String decrypted = cryptoUtil.decryptMessage(encryptedMsg, aesKey);
        Assertions.assertNotNull(decrypted);
    }
}
```

### 8.2 集成测试
1. 搭建测试环境企业微信应用
2. 配置测试回调URL
3. 发送测试消息验证全流程
4. 验证异常场景处理

---

## 九、上线检查清单

- [ ] 企业微信企业认证完成
- [ ] 审批通知应用创建完成
- [ ] 考勤提醒应用创建完成
- [ ] 薪资通知应用创建完成
- [ ] 回调URL配置正确（HTTPS、公网可访问）
- [ ] Token和EncodingAESKey安全存储（环境变量）
- [ ] IP白名单已配置
- [ ] 数据库表已创建（wecom_message_record、wecom_sync_record等）
- [ ] sys_config 企微业务配置项已初始化
- [ ] wecom_tenant_config 企微租户配置表已初始化（填写集团级 corpId）
- [ ] application.yml 基础设施配置已通过环境变量注入（corpId 已移除）
- [ ] WecomConfigService 初始化正常，corpId 可从 wecom_tenant_config 表读取
- [ ] 企微管理后台菜单权限已初始化（角色授权完成）
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 日志监控已配置
- [ ] 错误告警已配置
- [ ] 备份恢复方案已制定

---

## 十、开发计划

### 10.1 分阶段开发安排

| 阶段 | 内容 | 工期 | 交付物 |
|------|------|------|--------|
| **Phase 1** | 基础层开发 | 5天 | WecomConfig、WecomUtil、WecomTokenService、WecomCryptoUtil |
| **Phase 2** | 消息服务层 | 3天 | WecomMessageService、消息发送、重试机制 |
| **Phase 3** | 回调处理层 | 2天 | WecomCallbackController、签名验证、消息解密 |
| **Phase 4** | 通讯录同步 | 2天 | 组织架构同步、员工档案同步 |
| **Phase 5** | 打卡数据同步 | 2天 | 打卡API对接、数据写入考勤表 |
| **Phase 6** | 审批流程集成 | 2天 | 审批回调、待办提醒、结果通知 |
| **Phase 7** | 前端页面 | 2天 | 企微配置管理页面 |
| **Phase 8** | 联调测试 | 2天 | 整体联调、性能测试 |
| **合计** | | **18天** | |

### 10.2 依赖关系
```
Phase 1 (基础层) → Phase 2 (消息服务) → Phase 3 (回调处理)
                                        ↓
Phase 4 (通讯录同步) ← → Phase 5 (打卡同步) ← → Phase 6 (审批集成)
                                        ↓
                                  Phase 7 (前端页面)
                                        ↓
                                  Phase 8 (联调测试)
```

---

## 十一、企业微信后台操作说明

### 11.1 准备工作

#### 11.1.1 企业认证要求

| 认证项 | 要求 | 说明 |
|--------|------|------|
| 企业主体 | 已完成企业认证 | 未认证企业API接口受限严重 |
| 管理员账号 | 至少1个超管 | 用于创建应用和授权 |
| 公网服务器 | HTTPS域名 | 回调接口必须公网可访问 |
| HTTPS证书 | 有效证书 | 回调URL必须使用HTTPS协议 |

#### 11.1.2 获取企业资质信息

1. 登录 [企业微信管理后台](https://work.weixin.qq.com/wework_admin/frame)
2. 进入「我的企业」→「企业信息」
3. 记录以下信息（用于 application.yml 配置）：
   - **企业ID（corpId）**：首页顶部显示，格式如 `wwxxxxxxxxxxxxxxxx`
   - **管理员账号**：用于获取Secret（仅管理员可查看）

---

### 11.2 创建应用

根据方案对接优先级，需创建以下应用：

#### 11.2.1 审批通知应用（审批流程集成）

1. 进入「应用管理」→「创建应用」
2. 填写应用信息：
   - **应用名称**：`GBI审批通知`
   - **可见范围**：选择所有需要接收审批消息的员工
   - **部门可见范围**：按需配置
3. 记录以下信息：
   - **AgentId**：应用详情页顶部显示（对应 `wecom.agent.approval.id`）
   - **Secret**：点击查看，仅管理员可查看并复制
4. 配置回调URL（如需接收审批回调）：
   - 回调服务URL：`https://你的域名/api/wecom/callback`
   - Token：自定义字符串（对应 application.yml 的 `wecom.token`）
   - EncodingAESKey：点击「随机获取」按钮生成
5. 权限配置：在「接口权限」中申请以下权限：
   - `审批流程读取`（用于接收审批状态）
   - `消息推送`（用于发送审批通知）

#### 11.2.2 考勤提醒应用（打卡数据同步 + 考勤异常通知）

1. 创建应用，名称为 `GBI考勤提醒`
2. 记录 AgentId（对应 `wecom.agent.attendance.id`）
3. 获取 Secret 和 EncodingAESKey
4. 权限申请：
   - `打卡记录读取`（用于同步打卡数据）
   - `部门成员信息读取`（用于同步员工基本信息）
   - `消息推送`（用于发送考勤异常通知）
5. 打卡数据同步方式选择：
   - **API拉取模式**（推荐）：系统定时调用企微API拉取打卡记录
   - **回调推送模式**：企微实时推送打卡变动事件到系统

#### 11.2.3 薪资通知应用（工资条推送）

1. 创建应用，名称为 `GBI薪资通知`
2. 记录 AgentId（对应 `wecom.agent.salary.id`）
3. 权限申请：`消息推送`
4. 仅在薪资模块上线后启用，初期可暂不创建

---

### 11.3 配置回调接口

#### 11.3.1 回调地址格式

```
https://你的域名/api/wecom/callback?agent-id={AgentId}
```

- 示例：`https://hr.gbi.com/api/wecom/callback?agent-id=1000002`
- **必须使用HTTPS协议**
- 确保回调服务器公网可访问，企业微信会验证回调URL的可达性

#### 11.3.2 回调验证流程

企业微信会在添加回调URL时发起验证请求，系统需按以下逻辑响应：

```
企微发送GET请求（含signature、timestamp、nonce、echostr）
    → 系统签名验证（token拼接排序SHA1比对）
    → 验证通过：返回echostr明文
    → 验证失败：返回空或错误信息
```

系统回调接口需处理以下消息类型：
| 消息类型 | 说明 | 对应功能 |
|----------|------|----------|
| `event_change_contact` | 通讯录变更事件 | 组织架构同步触发 |
| `approval_change` | 审批状态变更事件 | 审批结果同步 |
| `attendance_change` | 打卡数据变动事件 | 考勤数据同步触发 |

#### 11.3.3 IP白名单配置

1. 进入应用详情页 →「开发」→「企业可信IP」
2. 添加服务器公网IP地址
3. 如有多台服务器，逐一添加
4. 未在白名单内的IP发起的API请求将被拒绝

---

### 11.4 配置可见范围

各应用的「可见范围」决定了哪些员工可以接收消息：

| 应用 | 可见范围建议 | 说明 |
|------|-------------|------|
| 审批通知 | 全部员工 | 确保所有人都能收到审批结果 |
| 考勤提醒 | 全部员工 | 确保所有人都能收到考勤异常通知 |
| 薪资通知 | 按部门/岗位筛选 | 工资条仅发送给相关人员 |

**多子公司场景**：可在各子公司名下创建独立应用，或通过「消息推送」接口的 `toparty` / `totag` 参数实现差异化推送。

---

### 11.5 测试与验证

#### 11.5.1 回调验证步骤

1. 在企微管理后台填写回调URL后点击「保存」
2. 系统日志应出现：`[WecomCallbackController] 回调验证成功`
3. 如验证失败，检查：
   - URL是否正确（含 `/api/wecom/callback` 路径）
   - HTTPS证书是否有效
   - Token是否与 application.yml 配置一致
   - 服务器防火墙是否放行企微IP段

#### 11.5.2 消息发送测试

1. 在企微管理后台 →「应用」→ 选择对应应用 →「测试号」
2. 输入员工账号，发送测试文本消息
3. 确认员工企业微信收到消息
4. 同时检查系统日志：`[WecomMessageService] 消息发送成功`

#### 11.5.3 数据库验证

同步/推送成功后，检查以下表是否有记录：
```sql
-- 企微消息发送记录
SELECT * FROM wecom_message_record ORDER BY create_time DESC LIMIT 10;
-- 企微同步记录
SELECT * FROM wecom_sync_record ORDER BY create_time DESC LIMIT 10;
```

---

### 11.6 上线前最终确认清单

- [ ] 企业已完成认证（非个人版）
- [ ] 三个应用（审批/考勤/薪资）已全部创建并记录 AgentId
- [ ] Secret 和 EncodingAESKey 已安全存储（环境变量，未提交代码库）
- [ ] 回调URL已配置并通过验证（HTTPS + 公网可达）
- [ ] 服务器IP已加入企业可信IP白名单
- [ ] 各应用可见范围已按需求配置
- [ ] API接口权限已申请并审核通过
- [ ] application.yml 基础设施配置已通过环境变量注入
- [ ] sys_config 业务配置已初始化（同步开关默认关闭）
- [ ] 首次上线时手动开启同步开关，确认数据正常同步后再启用定时任务

---

## 十二、企微管理后台路由与权限菜单方案

### 12.1 设计说明

企微配置管理功能作为系统内部的管理模块，需提供前端页面供管理员查看和管理企微对接状态。本节详细说明对应的菜单、路由和权限设计。

### 12.2 菜单树结构

```
sys_menu（最大ID=232，新菜单从251起）
└── 107 人力资源（目录，已有）
    └── [251] 企微配置管理（菜单页面）← 新增主菜单
        ├── [252] 基础配置查看（子菜单）
        ├── [253] 同步记录查看（子菜单）
        ├── [254] 消息记录查看（子菜单）
        ├── [255] 编辑基础配置（按钮）
        ├── [256] 刷新Token（按钮）
        ├── [257] 手动触发同步（按钮）
        ├── [258] 查看同步详情（按钮）
        └── [259] 导出同步记录（按钮）
```

### 12.3 菜单明细清单

| 菜单ID | 父ID | 菜单名称 | permission | path | menu_type | icon | sort_order |
|--------|------|----------|------------|------|-----------|------|------------|
| 251 | 107 | 企微配置管理 | `wecom:config:list` | `/hr/wecom/config` | 2 | Link | 6 |
| 252 | 251 | 基础配置查看 | `wecom:config:view` | `/hr/wecom/config/basic` | 2 | Setting | 1 |
| 253 | 251 | 同步记录查看 | `wecom:sync:view` | `/hr/wecom/config/sync` | 2 | Document | 2 |
| 254 | 251 | 消息记录查看 | `wecom:message:view` | `/hr/wecom/config/message` | 2 | ChatDotRound | 3 |
| 255 | 252 | 编辑基础配置 | `wecom:config:edit` | NULL | 3 | - | 1 |
| 256 | 252 | 刷新Token | `wecom:token:refresh` | NULL | 3 | - | 2 |
| 257 | 253 | 手动触发同步 | `wecom:sync:trigger` | NULL | 3 | - | 1 |
| 258 | 253 | 查看同步详情 | `wecom:sync:view` | NULL | 3 | - | 2 |
| 259 | 253 | 导出同步记录 | `wecom:sync:export` | NULL | 3 | - | 3 |

> **说明**：菜单ID 251-259 为规划值，实际执行前需 `SELECT MAX(id) FROM sys_menu` 确认无冲突。

### 12.4 SQL初始化脚本

```sql
-- 升级脚本：企微配置管理菜单权限
-- 当前 sys_menu 最大 ID=232，新菜单从251起

-- 1. 主菜单：企微配置管理
INSERT INTO sys_menu (parent_id, menu_name, path, permission, icon, sort_order, menu_type, visible, create_by, create_time, is_delete)
VALUES (107, '企微配置管理', '/hr/wecom/config', 'wecom:config:list', 'Link', 6, 2, 1, 1, NOW(), 0);
SET @wecom_main = LAST_INSERT_ID();

-- 2. 子菜单
INSERT INTO sys_menu (parent_id, menu_name, path, permission, icon, sort_order, menu_type, visible, create_by, create_time, is_delete) VALUES
(@wecom_main, '基础配置查看', '/hr/wecom/config/basic', 'wecom:config:view', 'Setting', 1, 2, 1, 1, NOW(), 0),
(@wecom_main, '同步记录查看', '/hr/wecom/config/sync',  'wecom:sync:view',   'Document', 2, 2, 1, 1, NOW(), 0),
(@wecom_main, '消息记录查看', '/hr/wecom/config/message','wecom:message:view','ChatDotRound', 3, 2, 1, 1, NOW(), 0);
SET @wecom_basic  = LAST_INSERT_ID() - 2;
SET @wecom_sync   = LAST_INSERT_ID() - 1;
SET @wecom_msg    = LAST_INSERT_ID();

-- 3. 按钮权限：基础配置
INSERT INTO sys_menu (parent_id, menu_name, permission, menu_type, sort_order, visible, create_by, create_time, is_delete) VALUES
(@wecom_basic, '编辑基础配置', 'wecom:config:edit',  3, 1, 0, 1, NOW(), 0),
(@wecom_basic, '刷新Token',    'wecom:token:refresh', 3, 2, 0, 1, NOW(), 0);

-- 4. 按钮权限：同步记录
INSERT INTO sys_menu (parent_id, menu_name, permission, menu_type, sort_order, visible, create_by, create_time, is_delete) VALUES
(@wecom_sync,  '手动触发同步', 'wecom:sync:trigger', 3, 1, 0, 1, NOW(), 0),
(@wecom_sync,  '查看同步详情', 'wecom:sync:view',    3, 2, 0, 1, NOW(), 0),
(@wecom_sync,  '导出同步记录', 'wecom:sync:export',  3, 3, 0, 1, NOW(), 0);

-- 5. 按钮权限：消息记录（仅查看）
-- 消息记录页面无操作按钮，所有权限通过子菜单 permission 控制

-- 6. 角色授权
-- 业务管理员（role_id=7）：全部权限
-- 子公司经理（role_id=4）：仅查看权限
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 7, id FROM sys_menu WHERE permission LIKE 'wecom:%' AND is_delete=0;

INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 4, id FROM sys_menu 
WHERE permission LIKE 'wecom:%' 
  AND permission NOT LIKE '%:edit%' 
  AND permission NOT LIKE '%:trigger%' 
  AND permission NOT LIKE '%:refresh%' 
  AND permission NOT LIKE '%:export%';
```

### 12.5 前端路由文件修改

**文件路径**：`gbi_platform_admin/src/utils/routerHelper.ts`

在 `PATH_COMPONENT_OVERRIDE` 中 HR 区块末尾追加：

```typescript
// ========== 企微配置管理（新增）==========
'/hr/wecom/config':      '../views/hr/wecom/config/index.vue',
'/hr/wecom/config/basic':'../views/hr/wecom/config/basic.vue',
'/hr/wecom/config/sync': '../views/hr/wecom/config/sync.vue',
'/hr/wecom/config/message':'../views/hr/wecom/config/message.vue',
```

**物理文件结构**：
```
src/views/hr/wecom/
├── config/
│   ├── index.vue    # 企微配置管理主页（Tab容器）
│   ├── basic.vue    # 基础配置查看/编辑
│   ├── sync.vue     # 同步记录查看
│   └── message.vue  # 消息记录查看
```

### 12.6 后端 PermissionConst.java 新增常量

**文件路径**：`gbi_platform_server/src/main/java/com/gbi/platform/common/constant/PermissionConst.java`

在现有 HR 区块后追加：

```java
/* ------------------------------ 企业微信集成管理 ------------------------------ */
// 企微配置管理
public static final String WECOM_CONFIG_LIST    = "wecom:config:list";
public static final String WECOM_CONFIG_VIEW    = "wecom:config:view";
public static final String WECOM_CONFIG_EDIT    = "wecom:config:edit";
public static final String WECOM_TOKEN_REFRESH  = "wecom:token:refresh";

// 企微同步记录
public static final String WECOM_SYNC_VIEW      = "wecom:sync:view";
public static final String WECOM_SYNC_TRIGGER   = "wecom:sync:trigger";
public static final String WECOM_SYNC_EXPORT    = "wecom:sync:export";

// 企微消息记录
public static final String WECOM_MESSAGE_VIEW   = "wecom:message:view";
```

### 12.7 前端权限控制示例

```vue
<!-- 主页面 Tab 显示控制 -->
<el-tabs v-model="activeTab">
  <el-tab-pane label="基础配置" name="basic" />
  <el-tab-pane label="同步记录" name="sync" />
  <el-tab-pane label="消息记录" name="message" />
</el-tabs>

<!-- 操作按钮权限控制 -->
<AuthBtn permission="wecom:config:edit" @click="openEditDialog">编辑配置</AuthBtn>
<AuthBtn permission="wecom:token:refresh" @click="refreshToken">刷新Token</AuthBtn>
<AuthBtn permission="wecom:sync:trigger" @click="triggerSync">手动触发同步</AuthBtn>
<AuthBtn permission="wecom:sync:export" link @click="handleExport">导出记录</AuthBtn>
```

### 12.8 部署步骤

```
步骤1  执行 12.4 SQL脚本，初始化菜单记录
步骤2  确认菜单ID，按需调整角色授权
步骤3  前端 routerHelper.ts 添加 12.5 路由条目
步骤4  创建 src/views/hr/wecom/config/*.vue 前端组件
步骤5  后端 PermissionConst.java 添加 12.6 常量
步骤6  前后端联调，验证菜单可见性和按钮权限
```

### 12.9 注意事项

1. **权限范围**：企微配置包含安全敏感信息（Token/AES Key），建议仅授予系统管理员可见，不建议开放给子公司经理
2. **corpId 多租户管理**：corpId 已迁移至 `wecom_tenant_config` 表，可在「企微配置管理」页面的「基础配置」Tab中查看和编辑；新接入子公司时，在表中添加对应 company_id 记录即可，无需重启服务
3. **Token刷新**：刷新Token按钮操作会重置企微access_token缓存，建议仅在Token失效时手动触发
4. **手动同步**：手动触发同步用于补漏测试，生产环境建议保持自动定时同步
5. **敏感信息**：基础配置编辑页面需对 AES Key 等敏感字段做脱敏显示（中间掩码），编辑时明文输入
6. **wecom_tenant_config 初始化**：上线前必须填写集团级（company_id=0）的真实 corpId，否则 WecomConfigService 启动时会输出 warn 日志，API调用将失败

---

## 十三、附录

### 13.1 企业微信API文档
- 官方文档：https://developer.work.weixin.qq.com/document
- 消息推送：https://developer.work.weixin.qq.com/document/path/90664
- 通讯录管理：https://developer.work.weixin.qq.com/document/path/90236
- 打卡管理：https://developer.work.weixin.qq.com/document/path/90253
- 审批流：https://developer.work.weixin.qq.com/document/path/91039

### 13.2 常见问题
1. **回调验证失败**：检查Token是否匹配，URL是否公网可访问
2. **token过期**：检查access_token是否定期刷新
3. **消息接收不到**：检查应用可见范围是否包含目标用户
4. **发送失败**：检查权限配置和IP白名单
5. **数据不同步**：检查同步开关配置和日志记录

### 13.3 参考资料
- 企业微信开发文档
- Spring Boot最佳实践
- 微服务架构设计模式
- 集团多业态一体化管控系统文档
- 《第三方集成规范》
