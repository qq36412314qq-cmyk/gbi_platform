# 集团多业态一体化管控平台

**基于 Spring Boot 3 + MyBatis-Plus + Vue 3 的企业级多租户业务管理平台**

---

## 项目解决什么问题

集团型企业同时运营多种业态（汽车城、商业街、物业园区等），传统管理存在以下痛点：

- **数据孤岛**：租赁、水电、物资、财务各自为政，无法统一对账
- **管控缺失**：集团无法实时掌握各子公司经营状况
- **人工成本高**：合同到期、欠费提醒、巡检任务依赖人工跟进
- **审批流程不规范**：请假、采购、报销等依赖纸质或分散的 OA 工具

本平台通过**统一中台 + 多业务模块**架构，实现集团-子公司两级隔离、数据同源闭环、流程标准化，帮助集团型企业实现业务数字化管控。

---

## 主要功能

### 统一中台能力

| 能力 | 说明 |
|---|---|
| 组织与权限 | 集团-子公司-部门-员工多级组织架构；RBAC 功能权限 + company_id 数据权限；敏感操作强制二级双人复核 |
| 流程引擎 | Flowable 驱动，支持条件分支、会签、抄送、催办；覆盖合同审批、调拨、采购、报销、请假等场景 |
| 文件与消息 | 文件统一 OSS 存储并按公司隔离；站内信、小程序模板消息、短信多渠道通知 |
| 审计日志 | 登录退出、权限变更、财务操作、地图编辑等高危操作永久归档，不可删除 |

### 业务运营模块

| 模块 | 核心功能 |
|---|---|
| 铺位租赁管理 | 铺位台账、商户入驻、合同管理（租金/押金/租期）、到期预警、续租退租；含 Fabric 可视化地图，实时显示空置/已租/欠费/即将到期状态 |
| 水电物业收费 | 智能仪表远程抄表、合并账单生成、阶梯电价配置、欠费远程断电联动、缴费记录管理 |
| 物资管理 | 集团总库与子公司分库两级管理、采购申请、出入库、调拨审批、库存预警 |
| 安全巡检 | 巡检计划配置、现场打卡拍照、隐患上报与整改闭环、台账导出 |
| 收费规则引擎 | 6 种计费模式（固定金额/按面积/仪表计量/租金百分比/逾期违约金/公摊分摊），规则级折扣与免租期，账单金额快照防篡改 |
| 营销运营 | 优惠模板配置、活动审批发布、小程序展示与核销、老带新奖励、商户分层标签推送 |

### OA 办公模块

公文收发文、通用审批（请假/出差/采购/报销）、会议室预约、公告发布与已读统计、定位打卡考勤、工作汇报（日报/周报/月报）、个人云盘。

### 财务与数据分析

全业务收支统一归集 `biz_finance_flow` 流水，支持子公司对账视图与集团汇总视图，合同-账单-缴费-凭证完整对账链路。

---

## 技术栈

| 层次 | 技术选型 |
|---|---|
| 后端 | Spring Boot 3.5 + Java 21 + MyBatis-Plus 3.5 + Flowable 7.x |
| 数据库 | MySQL 8.0（库名 `group_rent_db`，utf8mb4 字符集） |
| 缓存 | Redis 6+（字典缓存、幂等控制、会话管理） |
| 前端管理后台 | Vue 3 + Vite + Element Plus + Pinia + Axios |
| 小程序 | 微信小程序原生开发 |
| 文件存储 | 本地目录 / OSS（可配置） |
| 接口文档 | SpringDoc OpenAPI（/doc.html） |

---

## 安装方法

### 环境要求

| 组件 | 版本要求 |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0 |
| Redis | 6+ |
| Nginx | 1.20+（生产环境） |

### 数据库初始化

```powershell
# 创建数据库
mysql -uroot -p -e "CREATE DATABASE group_rent_db DEFAULT CHARSET utf8mb4;"

# 导入基础数据（位于 gbi_platform_docs/attachments/sql/）
mysql -uroot -p你的密码 --default-character-set=utf8mb4 group_rent_db < install.sql
```

### 后端编译与运行

```powershell
# 1. 停止旧进程（如有）
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. 编译打包
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" `
  "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp"

# 3. 启动服务（默认 8080 端口）
java -jar target\out\gbi_platform_server.jar --spring.profiles.active=dev
```

> 生产环境通过环境变量注入配置：`MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PASSWORD`、`JWT_SECRET`、`UPLOAD_DIR`。

### 前端编译与运行

```powershell
# 开发模式
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_admin"
npm install
npm run dev   # 访问 http://localhost:5173

# 生产构建
npm run build  # 产物输出至 dist/
```

---

## 使用方法

### 典型工作流程

1. **首次登录**：使用默认管理员账号 `admin` / `123456` 登录管理后台
2. **组织配置**：在「组织管理」中创建子公司、部门，分配角色权限
3. **合同管理**：录入铺位信息 → 创建租赁合同 → 提交审批 → 审批通过后生效
4. **账单生成**：按周期（月/季/年）批量生成收费账单，自动关联合同金额
5. **缴费核销**：商户通过小程序在线缴费或管理员线下登记，自动生成财务流水
6. **数据查看**：集团管理员可查看全公司汇总数据；子公司仅查看本公司数据

### 权限控制说明

- 所有接口自动附加 `company_id` 过滤，跨公司数据不可见
- 敏感权限（地图编辑、全局配置、财务全域查看）需二级双人复核后方可开通
- 所有权限变更、账号启停、密码修改写入审计日志

### 接口文档

服务启动后访问：`http://localhost:8080/doc.html`

---

## 输入输出示例

### 登录接口

**请求**

```http
POST /api/base/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "realName": "系统管理员",
      "companyId": 1,
      "companyName": "集团总公司"
    }
  }
}
```

### 铺位列表查询

**请求**

```http
POST /api/stall/list
Content-Type: application/json
Authorization: Bearer {token}

{
  "pageNum": 1,
  "pageSize": 20,
  "marketId": 1,
  "status": "rent"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "total": 156,
    "list": [
      {
        "id": 1001,
        "stallNo": "A-001",
        "area": 50.5,
        "status": "rent",
        "tenantName": "张三",
        "tenantPhone": "138****1234",
        "contractEndDate": "2026-12-31",
        "marketName": "汽车城一期"
      }
    ]
  }
}
```

### 账单生成

**请求**

```http
POST /api/finance/bill/generate
Content-Type: application/json
Authorization: Bearer {token}

{
  "billingMonth": "2026-09",
  "marketId": 1
}
```

**响应**

```json
{
  "code": 200,
  "message": "成功生成账单 48 条",
  "data": {
    "generated": 48,
    "skipped": 3,
    "totalAmount": 256800.00
  }
}
```

### 审批流提交

**请求**

```http
POST /api/flow/instance/submit
Content-Type: application/json
Authorization: Bearer {token}

{
  "flowDefId": 1,
  "bizType": "contract",
  "bizId": 1001,
  "formData": {
    "tenantName": "李四",
    "stallId": 1005,
    "startDate": "2026-09-01",
    "endDate": "2027-08-31",
    "monthlyRent": 3500.00,
    "deposit": 7000.00
  }
}
```

**响应**

```json
{
  "code": 200,
  "message": "提交成功，当前审批节点：部门经理审批",
  "data": {
    "flowInstanceId": 5001,
    "currentNode": "部门经理审批",
    "currentAssignee": "王经理"
  }
}
```

---

## 项目结构

```
gbi_platform/
├── gbi_platform_server/       # 后端服务（Spring Boot 3）
│   └── src/main/java/com/gbi/platform/
│       ├── config/            # 全局配置、拦截器、定时任务
│       ├── controller/        # REST 接口（按模块分包）
│       ├── service/impl/      # 业务逻辑实现
│       ├── mapper/            # MyBatis-Plus 数据层
│       ├── entity/            # 数据库实体（44 张表）
│       ├── dto/               # 入参对象
│       ├── vo/                # 返回视图对象
│       ├── common/            # 统一返回体、异常、常量、安全工具
│       └── util/              # 第三方工具（OSS、钉钉、支付等）
├── gbi_platform_admin/        # PC 管理后台（Vue 3 + Element Plus）
├── gbi_platform_miniapp/      # 微信小程序（商户端）
└── gbi_platform_docs/         # 设计文档与 SQL 脚本
```

---

## 许可
---

## 版本迭代规范

本项目遵循 **GitFlow + Conventional Commits** 开发规范，详细规则请查阅：

📄 [`GIT_CONVENTION.md`](./GIT_CONVENTION.md)

包含内容：
- 分支策略（main / feature / fix / hotfix）
- Commit Message 规范（Conventional Commits）
- 语义化版本号规则（SemVer）
- CHANGELOG 维护规范
- 功能开发 / Bug修复 / 版本发布流程
- 推送前自检清单


内部项目，仅供集团业务使用。