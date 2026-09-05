# Git 版本迭代开发规范

> 适用于：集团多业态一体化管控平台（gbi_platform）
> 最后更新：2026-08-31

---

## 一、分支策略

### 1.1 分支命名规范

| 分支类型 | 命名格式 | 用途 |
|---------|---------|------|
| 主分支 | `main` | 生产环境代码，永久保护 |
| 开发分支 | `feature/{模块名}-{功能名}` | 新功能开发，如 `feature/stall-map` |
| Bug修复 | `fix/{模块名}-{问题描述}` | 修复已知问题，如 `fix/finance-bill-calc` |
| 紧急修复 | `hotfix/{模块名}-{问题描述}` | 线上紧急修复，如 `hotfix/pay-timeout` |
| 发布分支 | `release/{版本号}` | 发布前代码冻结，如 `release/v1.2.0` |

### 1.2 分支保护规则

- `main` 分支**禁止直接 commit**，所有变更必须通过 Merge Request / Pull Request 合并
- 合并前必须通过 CI 检查（编译通过、无敏感文件）
- 禁止 force-push 到 main 分支
- feature/fix 分支合并后可删除

### 1.3 常用分支操作

```powershell
# 从 main 创建新功能分支
git checkout -b feature/stall-map main

# 同步最新 main 代码到当前分支
git rebase main

# 合并分支到 main（在 main 上执行）
git merge feature/stall-map

# 删除已合并的本地分支
git branch -d feature/stall-map

# 删除已合并的远程分支
git push origin --delete feature/stall-map
```

---

## 二、Commit Message 规范

### 2.1 格式要求

严格遵循 **Conventional Commits** 规范：

```
<type>: <subject>

[optional body]

[optional footer]
```

- `type`：变更类型（必填）
- `subject`：简短描述（中文，不超过 50 字）
- `body`：详细说明（可选）
- `footer`：破坏性变更、关联 Issue（可选）

### 2.2 Type 类型清单

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 新增铺位可视化地图模块` |
| `fix` | Bug 修复 | `fix: 修复账单金额计算精度丢失问题` |
| `docs` | 文档变更 | `docs: 更新后端编码规范` |
| `refactor` | 重构（非功能变更） | `refactor: 抽取财务流水公共校验逻辑` |
| `style` | 代码格式（不影响逻辑） | `style: 统一 Controller 层缩进格式` |
| `test` | 测试相关 | `test: 新增收费规则单元测试` |
| `chore` | 构建/工具/依赖 | `chore: 升级 MyBatis-Plus 到 3.5.12` |
| `perf` | 性能优化 | `perf: 优化铺位列表查询 SQL 索引` |
| `ci` | CI/CD 配置 | `ci: 添加 Maven 编译流水线配置` |

### 2.3 完整示例

**普通功能提交：**
```
feat: 新增铺位可视化地图模块

- 支持 Fabric 画布底图上传与编辑
- 铺位状态实时着色（空置/已租/欠费/即将到期）
- 点位点击弹窗联动台账详情

Closes: #23
```

**Bug 修复提交：**
```
fix: 修复水电费账单生成时面积计算精度丢失

账单实收金额保留两位小数，修复浮点运算精度问题。
修复位置：WaterElecBillServiceImpl.java#L156
```

**重构提交：**
```
refactor: 统一财务流水公共校验逻辑

将 FinanceFlowValidator 抽取为公共工具类，
供租赁、水电、物资等多个模块复用。
```

### 2.4 禁止的提交方式

```bash
# ❌ 禁止：无意义描述
git commit -m "update"
git commit -m "fix bug"
git commit -m "修复"

# ❌ 禁止：合并多个无关变更
# 一个 commit 只提交一个逻辑变更

# ❌ 禁止：中文标点混用
git commit -m "feat: 新增铺位地图模块。"  # 句号多余
```

---

## 三、版本号规则（SemVer）

### 3.1 版本号格式

```
主版本.次版本.补丁版本
  1    .   2    .    0
```

| 版本号 | 触发条件 | 示例场景 |
|--------|---------|---------|
| **主版本** (Major) | 不兼容的 API 变更 | 数据库表结构重构、接口协议变更 |
| **次版本** (Minor) | 向后兼容的功能新增 | 新增 OA 办公模块、新增营销运营功能 |
| **补丁版本** (Patch) | 向后兼容的问题修复 | 修复账单计算错误、修复权限校验漏洞 |

### 3.2 当前项目版本

- 当前版本：`1.2.0`
- 开发分支：`v1.3.0-SNAPSHOT`（开发中）
- 下个版本计划：`v1.3.0`（新增收费规则引擎功能）

### 3.3 版本标签规范

```bash
# 打标签（在 main 分支执行）
git tag -a v1.2.0 -m "Release v1.2.0: 新增收费规则引擎"
git push origin v1.2.0

# 查看标签
git tag -l "v*"
```

---

## 四、CHANGELOG 维护规范

### 4.1 文件格式

遵循 [Keep a Changelog](https://keepachangelog.com/) 规范：

```markdown
# 变更日志

## [1.2.0] - 2026-08-31

### 新增
- 铺位可视化地图模块
- 收费规则引擎（6种计费模式）

### 修复
- 修复水电费账单精度问题

### 变更
- 升级 Spring Boot 到 3.5.3
```

### 4.2 手动维护（当前推荐）

每次版本发布前，手动整理 CHANGELOG.md，格式如下：

```markdown
## [v1.3.0] - YYYY-MM-DD

### 新增
- feat: xxx
- feat: xxx

### 修复
- fix: xxx

### 优化
- refactor: xxx
- perf: xxx
```

### 4.3 工具辅助（可选）

使用 [keep-a-changelog](https://github.com/andrewvc/keep-a-changelog) 或 [standard-version](https://github.com/conventional-changelog/standard-version) 自动生成：

```bash
# 安装 standard-version
npm install -g standard-version

# 自动更新 CHANGELOG 并打标签
npx standard-version
```

---

## 五、开发流程

### 5.1 功能开发流程

```
1. 从 main 拉取最新代码
   git pull origin main

2. 创建功能分支
   git checkout -b feature/stall-map main

3. 开发过程中频繁提交（使用规范 commit message）
   git add .
   git commit -m "feat: 完成铺位地图底图上传功能"

4. 定期 rebase 同步 main 最新变更
   git rebase main

5. 功能完成，推送到远程
   git push origin feature/stall-map

6. 创建 Pull Request 到 main，等待 Code Review

7. Review 通过后合并，删除远程分支
   git merge feature/stall-map
   git push origin --delete feature/stall-map
```

### 5.2 Bug 修复流程

```
1. 从 main 拉取最新代码
   git pull origin main

2. 创建修复分支
   git checkout -b fix/bill-calc精度问题 main

3. 修复并测试
   git add .
   git commit -m "fix: 修复账单金额计算精度丢失问题"

4. 推送并创建 MR
   git push origin fix/bill-calc精度问题
```

### 5.3 紧急线上修复流程（Hotfix）

```
1. 从线上版本（tag 或 main）创建 hotfix 分支
   git checkout -b hotfix/pay-timeout main

2. 紧急修复并测试
   git commit -m "hotfix: 修复支付超时问题"

3. 快速合并到 main 和 release 分支
   git checkout main
   git merge hotfix/pay-timeout

4. 打紧急修复版本标签
   git tag -a v1.2.1 -m "Hotfix: 修复支付超时"
   git push origin v1.2.1

5. 删除 hotfix 分支
   git branch -d hotfix/pay-timeout
```

### 5.4 版本发布流程

```
1. 创建发布分支
   git checkout -b release/v1.3.0 main

2. 更新版本号（pom.xml、package.json 等）
   # 在 gbi_platform_server/pom.xml 中修改 <version>
   # 在 gbi_platform_admin/package.json 中修改 "version"

3. 更新 CHANGELOG.md

4. 冻结代码，执行完整回归测试

5. 测试通过后合并到 main
   git checkout main
   git merge release/v1.3.0

6. 打版本标签
   git tag -a v1.3.0 -m "Release v1.3.0: 新增收费规则引擎"
   git push origin v1.3.0

7. 删除发布分支
   git branch -d release/v1.3.0
   git push origin --delete release/v1.3.0
```

---

## 六、代码推送前自检清单

### 6.1 敏感信息检查

推送前必须确认以下文件**不会**被提交：

| 检查项 | 说明 | 检查命令 |
|--------|------|---------|
| 数据库密码 | `application-dev.yml` 中的 password | `grep -r "password:" src/main/resources/` |
| JWT 密钥 | `application.yml` 中的 secret | `grep -r "secret:" src/main/resources/` |
| 个人配置文件 | `.env.local`、`application-local.yml` | `ls .env*` |
| 日志文件 | `*.log`、`logs/` 目录 | `ls logs/` |
| 编译产物 | `target/`、`dist/`、`node_modules/` | `ls target/` |
| 上传文件 | `upload/` 目录下的业务数据 | `ls upload/` |

### 6.2 .gitignore 校验

确认以下文件类型已被正确忽略：

```bash
# 检查是否有文件被意外追踪
git ls-files | grep -E "\\.log$|/target/|/node_modules/|\\.env\\.production|application-dev\\.yml"
```

### 6.3 禁止提交的文件类型

| 文件类型 | 原因 |
|---------|------|
| `*.log` | 日志文件包含敏感信息，体积大 |
| `target/**` | 编译产物，可从源码重建 |
| `node_modules/**` | 依赖包，可从 package.json 重建 |
| `dist/**` | 构建产物，可从源码重建 |
| `upload/**` | 用户上传文件，含业务数据 |
| `.env.production` | 生产环境密钥 |
| `application-prod.yml` | 生产环境配置 |
| `Thumbs.db` | Windows 系统缓存 |
| `.DS_Store` | macOS 系统缓存 |

### 6.4 推送前最终检查

```bash
# 1. 查看将要提交的文件
git status --short

# 2. 检查是否有敏感文件
git diff --cached --name-only | xargs grep -l "password\|secret\|token" 2>/dev/null

# 3. 确认无未追踪的敏感文件
git ls-files --others --exclude-standard | grep -E "env|log|password"

# 4. 查看完整 diff（确认无敏感信息泄露）
git diff HEAD
```

---

## 七、项目专用说明

### 7.1 当前项目结构

```
gbi_platform/
├── gbi_platform_server/     # 后端服务（Spring Boot 3 + Java 21）
├── gbi_platform_admin/      # PC 管理后台（Vue 3 + Element Plus）
├── gbi_platform_miniapp/    # 微信小程序（商户端）
└── gbi_platform_docs/       # 设计文档与 SQL 脚本
```

### 7.2 模块相关分支命名建议

| 模块 | 分支命名示例 |
|------|-------------|
| 铺位租赁 | `feature/stall-map`、`feature/stall-contract` |
| 水电物业 | `feature/water-elec-meter`、`feature/property-fee` |
| 财务收费 | `feature/fee-rule-engine`、`feature/bill-generate` |
| OA 办公 | `feature/oa-leave`、`feature/oa-meeting` |
| 物资管理 | `feature/material-inventory`、`feature/material-transfer` |
| 流程引擎 | `feature/flow-definition`、`feature/flow-instance` |

### 7.3 数据库变更规范

- SQL 升级脚本放在 `gbi_platform_docs/attachments/sql/` 目录
- 脚本命名：`upgrade_v{版本号}.sql`，如 `upgrade_v1.3.0.sql`
- 禁止在生产数据库直接执行 ALTER TABLE，必须通过脚本版本管理

---

## 八、常见问题

### Q: 不小心提交了敏感文件怎么办？

```bash
# 1. 从 git 历史中删除（保留本地文件）
git rm --cached application-dev.yml

# 2. 重新提交
git commit -m "chore: 移除敏感配置文件"

# 3. 强制推送（仅当分支未共享时）
git push --force origin feature/xxx
```

### Q: 如何撤销错误的 commit？

```bash
# 撤销最近一次 commit（保留更改）
git reset --soft HEAD~1

# 撤销最近一次 commit（丢弃更改）
git reset --hard HEAD~1

# 撤销指定 commit
git revert <commit-hash>
```

### Q: 如何查看某个文件的修改历史？

```bash
# 查看文件修改历史
git log --follow gbi_platform_server/src/main/java/com/gbi/platform/service/impl/FinanceServiceImpl.java

# 查看某个提交的具体变更
git show <commit-hash>
```

---

> 📌 本规范由项目核心团队维护，如有变更请通知所有成员同步更新。
> 📌 新人入职请阅读本规范，确保代码风格统一。