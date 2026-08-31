# gbi_platform 前后端编译运行规范

> 适用环境：Windows PowerShell；JDK21；Maven 3.9；Node.js 20+；MySQL 8.0；服务端口：8080(后端) / 5173(前端)
> 数据库连接：root / 21145211 @ group_rent_db（phpStudy）

---

## 1、后端编译运行（gbi_platform_server）

### 1.1 路径与工具

| 项目 | 路径 / 值 |
|---|---|
| 源码根目录 | `D:\Office\Project\Java\gbi_platform\gbi_platform_server` |
| **Fat JAR 产物** | **`target\out\gbi_platform_server.jar`**（53MB，含全部依赖） |
| 中间产物（勿用） | `target\gbi_platform_server.jar`（仅 918KB，spring-boot-maven-plugin 替换后的占位文件） |
| Maven 命令 | `mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp"` |
| JDK | `C:\Users\Admin\jdk-21.0.12+8\bin\java.exe` |
| 日志输出 | `gbi_platform_server\server_run.log` / `server_err.log` |
| 配置文件 | `src\main\resources\application-dev.yml` |

> **关键**：pom.xml 中 `spring-boot-maven-plugin` 的 `outputDirectory` 配置为 `target/out/`，fat JAR 输出在 `target/out/gbi_platform_server.jar`，**务必从此路径启动**。

### 1.2 完整执行流程（固定顺序）

```powershell
# 1. 停止旧 Java 进程（释放 8080 端口）
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. Maven 打包（跳过测试，使用独立仓库绕过 .m2 文件锁定）
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp" 2>&1 | Select-Object -Last 10

# 3. 启动后端（前台运行，可实时看到启动日志）
java -jar target\out\gbi_platform_server.jar --spring.profiles.active=dev

# 4. 等待启动并验证（看到 Started 后）
Start-Sleep -Seconds 15
(Invoke-WebRequest -Uri "http://localhost:8080/base/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' -UseBasicParsing -TimeoutSec 5).StatusCode
# 返回 200 表示启动成功
```

> **后台启动（不推荐）**：`Start-Process java -ArgumentList ...` 在部分环境下会导致进程立即退出且无日志，优先使用前台运行。

### 1.3 故障判断

| 现象 | 原因 | 排查 |
|---|---|---|
| `BUILD FAILURE` | 编译错误（Java 源码） | 查看 `mvn` 输出中的 `[ERROR]` 行 |
| `端口 8080 被占用` | 旧进程未释放 | 先执行 `Get-Process java \| Stop-Process -Force` |
| `端口未就绪` | Spring 启动失败 | 查看启动日志末尾 30 行 |
| `SQLSyntaxErrorException: ... usage ...` | MySQL 保留字未加反引号 | 实体类 `@TableField` 加反引号，见 §5 |
| `AccessDeniedException: spring-jcl-6.2.8.jar` | Maven 本地仓库文件被锁定 | 使用 `-Dmaven.repo.local=D:\...\m2_repo_tmp` 独立仓库 |
| `系统繁忙，请稍后重试` | 业务异常被兜底捕获 | 查看启动日志中的 `ERROR` 或 `WARN` 行 |
| 启动后 JAR 立即退出 | 使用了 918KB 占位 jar | 改用 `target/out/gbi_platform_server.jar`（53MB） |

---

## 2、前端开发运行（gbi_platform_admin）

### 2.1 路径与工具

| 项目 | 路径 |
|---|---|
| 源码根目录 | `D:\Office\Project\Java\gbi_platform\gbi_platform_admin` |
| Node | `C:\Program Files\nodejs\node.exe` |
| npm | `C:\Program Files\nodejs\npm.cmd` |
| Vite 开发服务 | 端口 5173 |
| API 代理 | 本地 `/api` → `http://127.0.0.1:8080` |

### 2.2 启动命令

```powershell
# 方式一：npm run dev（推荐，自动打开终端，前台运行）
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_admin"
npm run dev

# 方式二：npx vite（前台运行，可直接看到编译日志）
npx vite --host 0.0.0.0 --port 5173
```

> **重要**：前端 Vite 服务**必须在前台运行**，使用 `Start-Process -WindowStyle Hidden` 或后台模式会导致进程立即退出。推荐在独立终端窗口中运行 `npm run dev`。

### 2.3 故障判断

| 现象 | 原因 | 排查 |
|---|---|---|
| `端口 5173 被占用` | 旧 Vite 进程残留 | `netstat -ano \| Select-String ":5173"` 找 PID 后 `Stop-Process -Force` |
| `VITE build failed` | TypeScript/Vue 语法错误 | 终端报错信息定位到具体文件 |
| `请求失败 / 系统繁忙` | 后端未启动或接口异常 | 检查 8080 端口是否监听 |
| 前端进程启动后立刻退出 | 后台启动方式不当 | 改用前台运行 `npm run dev` |

---

## 3、数据库（MySQL 8.0 via phpStudy）

| 项目 | 值 |
|---|---|
| 路径 | `D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe` |
| 数据库 | `group_rent_db` |
| 用户名 | `root` |
| 密码 | `21145211` |
| SQL 目录 | `gbi_platform_docs\attachments\sql\` |

### 3.1 常用命令

```powershell
# 登录数据库
& "D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe" -uroot -p21145211 group_rent_db

# 查看表结构
DESCRIBE water_elec_bill;
DESCRIBE property_bill;

# 查询数据
SELECT * FROM water_elec_bill WHERE is_delete=0;
SELECT * FROM biz_recv_pay_plan WHERE is_delete=0 ORDER BY id DESC LIMIT 10;
```

### 3.2 SQL 升级脚本执行顺序

```
install.sql                              → 初始建表+数据
upgrade_v1.1 ~ v1.12                    → 各阶段结构变更
upgrade_v2.0 ~ v2.1                     → 应收应付+审批流程
fix_v1.x ~ v2.2                         → 修复类补丁（按文件名顺序）
```

---

## 4、一键启停脚本

### 4.1 完整重启（后端+前端）

```powershell
# ========= 后端：停止+编译+启动 =========
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp" 2>&1 | Select-Object -Last 5
if ($LASTEXITCODE -ne 0) { Write-Host "编译失败，请检查错误"; exit 1 }
Write-Host "后端启动中..."
java -jar target\out\gbi_platform_server.jar --spring.profiles.active=dev
```

### 4.2 验证所有服务

```powershell
# 后端验证
$login = Invoke-RestMethod -Uri "http://localhost:8080/base/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
Write-Host "后端登录: code=$($login.code)"

# 前端验证
$fe = Invoke-WebRequest -Uri "http://localhost:5173/" -UseBasicParsing -TimeoutSec 5
Write-Host "前端首页: StatusCode=$($fe.StatusCode)"
```

---

## 5、MySQL 保留字注解规范（重要！）

**问题**：MySQL 8.0 中 `usage`、`unit_price`、`period_factor` 等是保留字或易冲突标识符。MyBatis-Plus 在生成 SQL 时，若 `@TableField` 中未加反引号，会直接输出裸字面量导致 `SQLSyntaxErrorException`。

**规则**：以下字段在实体类中**必须**使用反引号包裹：

| 实体类 | 需加反引号的字段 |
|---|---|
| `WaterElecBill.java` | `usage`、`unit_price` |
| `PropertyFeeBill.java` | `usage`、`unit_price`、`period_factor` |
| 其他实体 | 涉及 MySQL 保留字的字段同样处理 |

**正确写法**：
```java
@TableField("`usage`")
private BigDecimal usage;

@TableField("`unit_price`")
private BigDecimal unitPrice;
```

**错误写法**（会引发 SQL 语法错误）：
```java
@TableField("usage")  // 错误：usage 是 MySQL 保留字
private BigDecimal usage;
```

---

## 6、SecurityConfig 白名单路径规则（重要！）

**关键规则**：Vite proxy 会自动去掉 `/api` 前缀再转发到后端。
- 前端请求: `POST /api/base/login` → Vite proxy rewrite → 后端收到: `POST /base/login`
- 因此 `SecurityConfig.java` 的 `PERMIT_ALL` 白名单**必须使用无 `/api` 前缀的路径**
- 错误示例: `/api/base/login`（多加了 `/api` 前缀，导致登录接口被 JWT 过滤器拦截返回 401）

---

## 7、快照字段规范（审计合规）

涉及关联查询的表，关键字段写入时固化快照值，禁止运行时关联：
```sql
stall_number varchar(64) COMMENT '摊位编号快照（写入时固化，禁止修改）'
stall_name varchar(128) COMMENT '摊位名称快照'
stall_market_name varchar(128) COMMENT '所属市场名称快照'
merchant_name varchar(128) COMMENT '商户名称快照'
```

---

## 8、前后端接口对接检查清单

每次新增或修改接口时检查：
- [ ] 后端 `@RestController` 的 `@RequestMapping` 路径
- [ ] 前端 `api/*.ts` 中的请求路径与后端一致
- [ ] `SecurityConfig.java` 白名单包含新接口路径（如为免登录接口）
- [ ] 数据库表结构变更已写入 `install.sql` 和 `upgrade_xxx.sql`
- [ ] 前端 `router/index.ts` 新增路由配置
- [ ] 实体类字段与数据库列名通过驼峰映射对齐
- [ ] 实体类涉及 MySQL 保留字的字段已加反引号 `@TableField("\`列名\`")`
- [ ] VO/DTO 类包含前端需要的所有字段

---

## v2.7 更新内容（2026-08-27 补充）

### H. Fat JAR 输出路径（关键！）

**现象**：启动 `target/gbi_platform_server.jar`（918KB）后进程立即退出，或启动后接口全部返回 500。
**原因**：`spring-boot-maven-plugin` 的 `outputDirectory` 配置为 `target/out/`，fat JAR 输出在 `target/out/gbi_platform_server.jar`（53MB），`target/` 下的 jar 是 repackage 后留下的占位文件。
**解决方案**：启动时务必使用 `target/out/gbi_platform_server.jar`。

### I. Maven 本地仓库文件锁定

**现象**：`AccessDeniedException: spring-jcl-6.2.8.jar`，编译失败。
**原因**：其他进程（IDEA/Code/杀毒软件）锁定了 `.m2` 仓库中的 jar 文件。
**解决方案**：Maven 命令追加 `-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp`，使用独立临时仓库编译。

### J. Vite 前端后台启动失败

**现象**：使用 `Start-Process -WindowStyle Hidden` 启动 Vite 后进程立即退出，端口 5173 无监听。
**原因**：Node.js 在无 TTY 环境下部分 npm 脚本行为异常，后台模式不可靠。
**解决方案**：前端 Vite 必须在前台终端运行（`npm run dev`），不可使用后台启动方式。

### K. 水电账单 SQL 语法错误

**现象**：查询 `water_elec_bill` 表返回 `SQLSyntaxErrorException: ... usage ...`。
**原因**：`usage`、`unit_price` 是 MySQL 保留字/易冲突标识符，实体类 `@TableField` 缺少反引号。
**解决方案**：见 §5 保留字注解规范，已在 `WaterElecBill.java` 和 `PropertyFeeBill.java` 中修复。
---

## L. 编译失败排查：Lombok + Java 21 注解处理器循环

**现象**：mvn clean compile 失败，报错 Compilation failure，具体错误信息乱码（实际为「无法关闭注释处理程序源」）。

**原因**：Java 21 的 javac 对注解处理器循环检测更严格，Lombok 与 MyBatis-Plus 的注解处理器可能发生冲突。

**解决方案**：
1. 项目已提供预编译 JAR：gbi_platform_server_new.jar（53MB，含全部依赖），可直接运行
2. 若需重新编译，尝试以下方法：
   - 确保 Lombok 版本为 1.18.38（已在 pom.xml 中指定）
   - 确保 BizFeeBill.java 等实体类**无 UTF-8 BOM**（BOM 会导致编译失败）
   - 若仍失败，检查是否有其他注解处理器冲突

**当前状态**：后端使用预编译 JAR 运行于 8080 端口，前端 Vite 运行于 5174 端口。
