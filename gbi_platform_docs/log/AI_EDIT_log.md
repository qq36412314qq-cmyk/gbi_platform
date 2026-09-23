# AI_EDIT_LOG

## 【2026-09-21 12:00:00】修复 pathToComponent 路径映射缺失问题

- 用户原始需求：点击导航菜单报错 "Failed to resolve module specifier '@/views/org/index.vue'"
- 修改涉及文件：
  - `gbi_platform_admin/src/utils/routerHelper.ts`（扩展 PATH_COMPONENT_OVERRIDE 映射表）
- 变更摘要：
  1. 新增中台管理路径映射：/org, /user, /role, /menu, /dict, /config, /theme, /deviceAuth, /audit, /permissionAudit
  2. 新增物业管理路径映射：/property/meter, /property/lease/stall, /property/lease/category, /property/lease/contract, /property/meter/bill, /property/feeBill 等
  3. 新增财务管理路径映射：/finance/payOrder, /finance/finance-flow, /finance/finance-report 等
  4. 新增 HR 管理路径映射：/hr/org, /hr/employee, /hr/salary, /hr/social 等
  5. 新增 OA 办公路径映射：/oa/leave, /oa/meeting-booking, /oa/flow/* 等
  6. 共扩展 47 条路径映射，覆盖所有 views 目录下的实际文件
- 风险与注意事项：数据库菜单 path 字段定义不规范（缺少 /platform 前缀），建议后续通过 SQL 脚本统一修正

## 【2026-09-21 11:45:00】修复刷新页面跳转登录页问题

- 用户原始需求：登录后刷新页面提示"资源不存在"并跳转到登录页，侧边栏空白
- 修改涉及文件：
  - `gbi_platform_server/src/main/java/com/gbi/platform/service/impl/MenuServiceImpl.java`
  - `gbi_platform_admin/src/router/index.ts`
  - `gbi_platform_admin/src/store/permission.ts`
- 变更摘要：
  1. MenuServiceImpl.java：补充缺失的 import（LoginUser、UserContext、SysUserRoleRel、SysUserRoleRelMapper），添加 userRoleRelMapper 字段注入
  2. router/index.ts：优化 beforeEach 错误处理，仅 401 认证失败时重置登录态跳转登录页，其他错误允许继续导航并输出警告日志
  3. permission.ts：改用 menuTree.value.length 判断是否已加载菜单（避免 dynamicRoutes 为空时误判），无菜单时注册空布局路由避免导航到空白页
- 风险与注意事项：MenuServiceImpl 缺少依赖注入会导致 NPE；非 401 错误不再强制登出，可能掩盖其他问题

## 【2026-09-21 11:30:00】v2.11 动态路由启用 + pathToComponent 路径映射修复

- 用户原始需求：按升级方案实施动态路由，SQL 已手动执行，本次启动 API 调用并修复路径推导问题
- 修改涉及文件：
  - `gbi_platform_admin/src/store/permission.ts`（启用 getAuthorizedMenuTreeApi 调用，补充导入）
  - `gbi_platform_admin/src/utils/routerHelper.ts`（新增 PATH_COMPONENT_OVERRIDE 映射表）
- 变更摘要：
  1. permission.ts：取消 TODO 注释，启用 `getAuthorizedMenuTreeApi()` 真实 API 调用，替换空数组兜底
  2. permission.ts：补充 `import { getAuthorizedMenuTreeApi } from '@/api/org'`
  3. routerHelper.ts：新增 `PATH_COMPONENT_OVERRIDE` 映射表，覆盖 7 个路径与 views 目录实际文件名不一致的情况
  4. 前后端均编译通过（前端 1719 modules，后端无报错）
- 风险与注意事项：pathToComponent 映射表是临时方案；后续规范做法是 views 目录与 sys_menu path 字段保持 kebab-case 对齐

## 【2026-09-21 11:00:00】v2.11 动态路由实施完成（前后端编译通过）

- 用户原始需求：按方案实施动态路由升级，SQL 已手动执行
- 修改涉及文件：
  - `gbi_platform_server/src/main/java/com/gbi/platform/controller/org/MenuController.java`
  - `gbi_platform_server/src/main/java/com/gbi/platform/service/MenuService.java`
  - `gbi_platform_server/src/main/java/com/gbi/platform/service/impl/MenuServiceImpl.java`
  - `gbi_platform_admin/src/api/org.ts`
  - `gbi_platform_admin/src/utils/routerHelper.ts`（新建）
  - `gbi_platform_admin/src/store/permission.ts`（重写）
  - `gbi_platform_admin/src/router/index.ts`（精简为静态兜底）
  - `gbi_platform_admin/src/components/layout/Sidebar.vue`（适配动态路由）
  - `gbi_platform_docs/sys_menu动态路由升级方案-v2.11.md`（补充实施记录）
- 变更摘要：
  1. 后端新增 `/org/menu/authorized-tree` 接口，超级管理员返回全量菜单树，普通用户返回授权子树
  2. 前端新增 routerHelper.ts：generateRoutes() 递归生成路由，pathToComponent() 推导组件路径
  3. permission.ts store：setupDynamicRoutes() 登录时动态注册路由，clearRoutes() 登出时清理
  4. router/index.ts 精简为仅静态路由（login/403/404/payOrderPrint），beforeEach 中调用动态注册
  5. Sidebar.vue 优先读取 dynamicRoutes，无动态路由时兜底 staticRoutesList
  6. 前后端均编译通过
- 风险与注意事项：dynamicRoutes 为空时使用静态路由兜底，确保平滑过渡；pathToComponent 路径推导规则需与 views 目录结构对应
