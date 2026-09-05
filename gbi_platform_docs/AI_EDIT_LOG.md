# AI 编辑日志

## 2026-08-20 系统配置参数驱动的合同字段约束

### 问题背景
在实现收费规则逻辑时，需要新增系统配置参数控制租赁合同中租金/押金和优惠参数的可写性。

### 根因分析
项目目录名 `gbi_platform` / `gbi_platform‑server` / `gbi_platform‑admin` 中混入了 Unicode 连字符（U+2011，en-dash），与 Node.js/npm/Maven 不兼容，导致：
- `npm run build` 报错：`Expected ";" but found "@"`（路径解析异常）
- `mvn` 命令找不到 jar 文件
- 前端 dev server 启动在 5174 而非配置的 5173

### 修复步骤

#### 1. 目录重命名（修复 Unicode 路径问题）
使用 `robocopy /MOVE` 将三个含 en-dash 的目录迁移至正常路径：
- `gbi_platform` → `gbi_platform`
- `gbi_platform_server` → `gbi_platform_server`
- `gbi_platform‑admin` → `gbi_platform_admin`
- `gbi_platform‑miniapp` → `gbi_platform_miniapp`

旧目录因 VS Code 持有句柄被锁定，新目录完全可用。

#### 2. 后端修改

**`ContractAddDTO.java`**
- 移除 `rentAmount` 字段的 `@NotNull` 和 `requiredMode = REQUIRED`，使其成为可选字段（由 Service 层根据配置做条件校验）

**`LeaseContractServiceImpl.java`**
- 注入 `ConfigService`，在 `add()` 方法步骤 3.5 新增条件校验：
  - `contract.rent_editable=0` → 要求 `rentAmount`/`depositAmount` 非空
  - `contract.discount_editable=0` → 要求优惠参数均为空

#### 3. 前端修改

**`sys.ts`** — 修复重复注释，保留 `getConfigValuesApi` 接口

**`lease.ts`** — 移除误添加的 `getConfigValuesApi`（已在 sys.ts 中定义）

**`leaseContract.vue`** — 核心改动：
- 导入 `getConfigValuesApi`
- 新增 `rentEditable`/`discountEditable` 响应式状态
- `onMounted` 读取配置初始化两个开关
- 租金/押金 `<el-input-number>` 绑定 `:disabled="!rentEditable"`
- 优惠三个字段绑定 `:disabled="!discountEditable"`
- 提示文案根据开关状态动态切换
- `rules` 改为 `computed` 根据配置动态控制必填
- `handlePolicyChange` 在 `discount_editable=0` 时自动清空策略

### 配置参数
| 参数 key | 默认值 | 含义 |
|---------|--------|------|
| `contract.rent_editable` | `1` | 1=租金/押金可手工填写；0=只读（按收费规则自动带出） |
| `contract.discount_editable` | `1` | 1=优惠参数可手工调整；0=只读（随策略自动带出） |

### 构建验证
- 前端：`npm run build` ✓（1786 modules，5.03s）
- 后端：`mvn package -DskipTests` ✓（gbi_platform_server.jar 51MB）
- 后端运行：`java -jar gbi_platform_server.jar --spring.profiles.active=prod` → 端口 8080 ✓
- 前端运行：`vite --port 5173` → 端口 5173 ✓

### 遗留事项
- 旧目录 `D:\Office\Project\Java\gbi_platform`（含 en-dash）被 VS Code 锁定，需重启后手动删除

## 【2026-08-26 07:38:00】权限常量补全+后端重启
- 用户原始需求：修复property/tenant页面500错误
- 问题根因：PermissionConst缺少TENANT_LIST/LEASE_STALL_LIST/FINANCE_REPORT_LIST等16个常量
- 修复内容：在PermissionConst.java中新增租户/铺位/收费类型/收费规则/财务汇总共24个权限常量
- 涉及修改文件：gbi_platform_server/src/main/java/com/gbi/platform/common/constant/PermissionConst.java
- 验证结果：登录+17个接口全部返回200
- 风险与注意事项：无特殊风险