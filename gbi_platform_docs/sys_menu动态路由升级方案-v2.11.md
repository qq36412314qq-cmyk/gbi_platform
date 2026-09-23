# sys_menu 对齐 router 升级方案 v2.11

## 一、现状差异分析

以 `router/index.ts` 静态路由为基准，对比 `sys_menu` 数据库表，发现以下差异：

### 1.1 数据库有多余的菜单（路由中没有）

| ID | 菜单名称 | permission | 问题说明 |
|----|----------|-----------|----------|
| 131~134 | 优惠阈值配置及按钮 | discount:threshold:* | 路由中无此页面，财务管理下不应有此菜单 |

### 1.2 数据库缺失的菜单（路由中有）

| 路由 path | permission | 所属模块 | 说明 |
|-----------|-----------|----------|------|
| /property/lease/stall/canvas | lease:stall:list | 物业管理-租赁-铺位管理 | 铺位画布 |
| /finance/payOrder | finance:payOrder:list | 财务管理 | 缴费明细单 |
| /oa/leave | oa:leave:list | OA办公 | 请假管理 |
| /oa/meetingBooking | oa:meeting:booking:list | OA办公 | 会议室预约 |

### 1.3 层级结构不一致

**中台管理 vs 系统设置**：
- 数据库：`中台管理(id=1)` 和 `系统设置(id=2)` 均为独立顶级目录
- 路由：所有中台和系统管理页面均挂载在 `/platform/*` 下，由「中台管理」统一承载
- 变更：将「系统设置」从顶级目录变为「中台管理」的子目录

**社保参数字典类菜单**：
- 数据库：城市字典等5个挂在「参数配置(id=8)」下，层级混乱
- 变更：新增「社保参数」(id=207) 父级目录，将这5个菜单统一收纳

**水电表管理**：
- 数据库：id=37 为页面类型(menu_type=2)，id=38/196 与其平级挂在36下
- 路由：`/property/meter` 是 Layout 目录，下有 bill 和 pay 两个子页面
- 变更：id=37 改为目录类型，38/196 改为其子菜单

---

## 二、变更清单

### 删除操作（1组）

| ID | 操作 | 原因 |
|----|------|------|
| 131~134 | 删除 优惠阈值配置及按钮 | 路由中无此页面 |

### 保留并调整的操作（2组）

| ID | 变更 | 原因 |
|----|------|------|
| 2 | `parent_id`: 0 → 1 | 系统设置并入中台管理，不再作为独立顶级目录 |
| 160~164 | `parent_id`: 8 → 207 | 统一移入新增的「社保参数」父级目录下 |

### 修改操作（8处）

| ID | 变更 | 原因 |
|----|------|------|
| 37 | `menu_type`: 2 → 1, `permission`: NULL | 水电表管理提升为目录容器，下有 bill/pay 两个子页面 |
| 38 | `parent_id`: 36 → 37 | 水电费账单成为水电表管理的子页面 |
| 196 | `parent_id`: 36 → 37 | 水电缴费成为水电表管理的子页面 |
| 194 | `parent_id`: 193 → 保持不变（已在193下） | 确认层级正确 |
| 199 | `permission`: `property:feePay:add` → `property:feePay:list` | 与路由权限标识对齐 |
| 195 | `icon`: Warning → Wallet | 与路由 meta.icon 保持一致 |
| 54 | `menu_name`: 摊位管理 → 铺位管理 | 与路由 meta.title 保持一致 |
| 207 | 新增「社保参数」目录 (parent_id=1) | 收纳 HR 参数字典类菜单 |

### 新增操作（5条）

| ID | 菜单名称 | permission | parent_id | path |
|----|----------|-----------|-----------|------|
| 203 | 铺位画布 | lease:stall:list | 54(铺位管理) | /property/lease/stall/canvas |
| 204 | 缴费明细单 | finance:payOrder:list | 40(财务管理) | /finance/payOrder |
| 205 | 请假管理 | oa:leave:list | 188(OA办公) | /oa/leave |
| 206 | 会议室预约 | oa:meeting:booking:list | 188(OA办公) | /oa/meetingBooking |
| 207 | 社保参数 | NULL | 1(中台管理) | /platform/socialParam |

---

## 三、变更后的 sys_menu 树形结构（对齐 router）

```
sys_menu 最终结构（menu_type: 1=目录 2=页面）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📁 中台管理 (id=1, /org) [目录]
├── 📁 系统设置 (id=2, /sys) [目录] ← 保留，从顶级并入中台管理
│   ├── 📄 字典管理 (dict:list, /sys/dict)
│   ├── 📄 参数配置 (config:list, /sys/config)
│   │   └── 🔘 参数新增/编辑/删除
│   ├── 📄 UI主题 (theme:list, /sys/theme)
│   ├── 📄 审计日志 (audit:list, /sys/audit)
│   └── 📄 权限复核 (permission:audit:list, /sys/permissionAudit)
├── 📄 组织管理 (org:list, /org)
├── 📄 用户管理 (user:list, /org/user)
├── 📄 角色管理 (role:list, /org/role)
├── 📄 菜单管理 (menu:list, /org/menu)
├── 📁 社保参数 (id=207新增, /platform/socialParam) [目录] ← 新增父级目录
│   ├── 📄 城市字典 (hr:city:list, /platform/city)  ← parent_id 8→207
│   ├── 📄 险种字典 (hr:insurance:list, /platform/insuranceType)  ← parent_id 8→207
│   ├── 📄 行业字典 (hr:industry:list, /platform/industry)  ← parent_id 8→207
│   ├── 📄 社保参数配置 (hr:social:param:list, /platform/socialParam)  ← parent_id 8→207
│   └── 📄 公积金参数配置 (hr:housing:fund:list, /platform/housingFundConfig)  ← parent_id 8→207
├── 📄 设备授权管理 (device:auth:list, /platform/deviceAuth)
└── 📄 字典管理/参数配置/UI主题/审计日志/权限复核 (均挂在系统设置 id=2 下)

📁 物业管理 (id=36, /property) [目录]
├── 📄 市场管理 (market:list, /property/market)
├── 📄 租户管理 (tenant:list, /property/tenant)
├── 📁 水电表管理 (id=37, 改为目录) [目录]
│   ├── 📄 水电费记录 (id=38, parent改为37, waterElec:bill:list, /property/bill)
│   └── 📄 水电缴费 (id=196, parent改为37, waterElec:pay:list, /property/meter/pay)
├── 📁 物业费账单 (id=193) [目录]
│   └── 📄 账单列表 (id=194, property:feeBill:list, /property/feeBill/list)
├── 📄 未支付订单 (id=195, property:unpaidBill:list, /property/unpaidBill) [icon修正]
├── 📄 缴费管理 (id=199, property:feePay:list, /property/feePay) [permission修正]
└── 📁 租赁管理 (id=53, /property/lease) [目录]
    ├── 📄 铺位管理 (id=54, lease:stall:list, /property/lease/stall) [名称修正]
    │   └── 📄 铺位画布 (id=203新增, lease:stall:list, /property/lease/stall/canvas)
    ├── 📄 租赁分类 (id=55, lease:category:list, /property/lease/category)
    └── 📄 合同管理 (id=56, lease:contract:list, /property/lease/contract)

📁 人力资源 (id=107, /hr) [目录]
├── 📄 员工档案 (hr:employee:list)
├── 📄 组织岗位 (hr:org:list)
├── 📄 人事异动 (hr:entry:list)
├── 📄 考勤管理 (hr:attendance:list)
├── 📄 薪酬管理 (hr:salary:month:list)
│   ├── 📄 薪酬级别 (hr:salary:grade:list)
│   ├── 📄 薪资模板 (hr:salary:rule:list)
│   ├── 📄 薪资档案v2 (hr:salary:archive:list)
│   ├── 📄 批量调薪 (hr:salary:batch:list)
│   └── 📄 年终奖管理 (hr:salary:yearBonus:list)
├── 📄 社保公积金 (hr:social:list)
│   ├── 📄 社保核算明细 (hr:social:calc:list)
│   └── 📄 年度基数重算 (hr:recalc:trigger)

📁 财务管理 (id=40, /finance) [目录]
├── 📄 财务流水 (finance:flow:list)
├── 📄 营收统计 (finance:report:list)
├── 📄 收费类型管理 (fee:item:list)
├── 📄 收费规则管理 (fee:rule:list)
├── 📄 应收应付计划 (plan:recvpay:list)
├── 📄 优惠策略 (discount:policy:list)
├── 📄 优惠申请 (discount:apply:list)
└── 📄 缴费明细单 (id=204新增, finance:payOrder:list)
    【已删除】优惠阈值配置 (id=131~134) ❌

📁 审批中心 (id=92, /flow) [目录]
├── 📄 待办处理 (flow:task:list)
├── 📄 我的申请 (flow:apply:list)
├── 📄 流程定义 (flow:def:list)
└── 📄 流程实例 (flow:instance:list)

📁 OA办公 (id=188, /oa) [目录]
├── 📄 请假管理 (id=205新增, oa:leave:list)       ← 新增
├── 📄 公告管理 (oa:announcement:list)
├── 📄 会议室管理 (oa:meeting-room:list)
├── 📄 打卡管理 (oa:clock:list)
├── 📄 工作汇报 (oa:work-report:list)
└── 📄 会议室预约 (id=206新增, oa:meeting:booking:list) ← 新增

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
【保留并调整】📁 系统设置 (id=2) → 并入中台管理(id=1) 下 ✅
【新增】📁 社保参数 (id=207) → 收纳城市/险种/行业/社保参数/公积金参数 ✅
【调整层级】37 提升为目录，38/196 成为其子菜单 ✅
【修正】199 permission: add→list；54 name: 摊位→铺位；195 icon 修正 ✅
【新增】203铺位画布、204缴费明细单、205请假管理、206会议室预约 ✅
【已删除】📁 优惠阈值配置 (id=131~134) ❌
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 四、动态路由方案

### 4.1 核心思路

**自 v2.11 起，业务路由改为从 sys_menu 表动态生成，不再维护 router/index.ts 中的静态路由。**

具体流程：
1. **后端**提供 `/org/menu/authorized-tree` 接口，返回当前用户有权限的菜单树
2. **前端**登录后调用该接口，递归遍历菜单树动态注册路由
3. **router/index.ts** 仅保留 `/login`、`/403`、`/:pathMatch(.*)*` 等固定路由作为兜底

### 4.2 router/index.ts 废弃规范

```typescript
/**
 * ⚠️ 已废弃：静态路由配置（v2.11 起改为动态路由）
 * 
 * 【迁移说明】
 * 自 v2.11 升级后，路由改为从 sys_menu 表动态生成，本文件仅作兼容保留。
 * 新开发请遵循以下规范：
 * 
 * 1. 新增页面：在 sys_menu 表中插入 menu_type=2 的记录，设置正确的 permission 和 path
 * 2. 新增目录：插入 menu_type=1 的记录，parent_id 指向父级目录 id
 * 3. 后台菜单管理界面可直接操作，无需修改前端代码
 * 4. 后端接口：GET /org/menu/authorized-tree 返回当前用户权限菜单树
 * 5. 前端自动根据菜单树递归生成路由并注册到 router
 * 
 * 【静态路由仅保留】
 * - /login、/403、/:pathMatch(.*)* 等固定路由仍需在此声明
 * - dashboard 页面无权限要求，可保留在静态路由中
 * 
 * 【严禁操作】
 * - 禁止在此文件中新增业务路由，一律通过 sys_menu 表配置
 * - 禁止修改此文件的 constantRoutes 来增加页面权限
 * 
 * @deprecated 自 v2.11 起使用动态路由，此文件仅保留静态路由兜底
 */
```

### 4.3 数据模型约定

sys_menu 表需满足以下约定，前端方可正确生成路由：

| 字段 | 约定规则 |
|------|---------|
| `menu_type=1`（目录） | 生成 Layout 包裹的路由节点，`children` 递归展开 |
| `menu_type=2`（页面） | 生成具体页面的路由节点，`component` 由 path 推导 |
| `path` | 必须与路由 path 一致，格式为 `/模块/子路径` |
| `permission` | 非空则加入 `meta.permission`，用于权限校验 |
| `icon` | 对应 Element Plus 图标组件名，放入 `meta.icon` |
| `menu_name` | 对应 `meta.title` |
| `visible=1` | 仅返回 visible=1 的菜单，hidden 的菜单不生成路由 |

### 4.3 component 路径推导规则

```typescript
// path → component 映射规则
// /org → @/views/platform/org/index.vue
// /org/user → @/views/platform/user/index.vue
// /property/meter → @/views/business/waterElec/waterElecMeter.vue
// /hr/salary → @/views/hr/salary/index.vue

function pathToComponent(path: string): string {
  // 1. 去掉首段斜杠
  let p = path.replace(/^\//, '')
  // 2. 路径段转驼峰（kebab → PascalCase）
  const segments = p.split('/').map(seg =>
    seg.replace(/-([a-z])/g, (_, c) => c.toUpperCase())
  )
  // 3. 最后一段加 /index.vue，其余作为目录前缀
  const last = segments.pop()!
  const dir = segments.join('/')
  if (dir) {
    return `@/views/${dir}/${last}.vue`
  }
  return `@/views/${last}/index.vue`
}

// 示例：
// /property/meter        → @/views/business/waterElec/waterElecMeter.vue
// /hr/salary/grade       → @/views/hr/salary/grade.vue
// /platform/socialParam  → @/views/hr/socialParam/index.vue
```

> **注意**：当前项目 views 目录结构与 path 不完全一致，需要建立 path → 物理路径的映射配置文件。

### 4.4 前端动态路由实现伪代码

```typescript
// store/permission.ts
import { dynamicImport } from '@/utils/routerHelper'

export async function generateRoutes(menuTree: MenuVO[]): Promise<RouteRecordRaw[]> {
  const routes: RouteRecordRaw[] = []

  for (const node of menuTree) {
    if (node.menuType === 1) {
      // 目录 → Layout 容器
      const children = node.children && node.children.length > 0
        ? await generateRoutes(node.children)
        : []

      routes.push({
        path: node.path,
        component: () => import('@/components/layout/index.vue'),
        redirect: children[0]?.path,
        meta: { title: node.menuName, icon: node.icon },
        children
      })
    } else if (node.menuType === 2 && node.permission) {
      // 页面 → 具体组件
      routes.push({
        path: node.path,
        name: camelCase(node.menuName),
        component: dynamicImport(pathToComponent(node.path!)),
        meta: {
          title: node.menuName,
          icon: node.icon,
          permission: node.permission,
          isCache: true
        }
      })
    }
  }

  return routes
}

// 在 router.beforeEach 中动态注册
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
    const menuTree = await getMenuTreeApi()  // 调用后端权限菜单树接口
    const dynamicRoutes = await generateRoutes(menuTree)
    dynamicRoutes.forEach(route => router.addRoute(route))
  }
  // 权限校验逻辑不变
  const permission = to.meta?.permission as string | undefined
  if (permission && !userStore.hasPermission(permission)) {
    next('/403')
    return
  }
  next()
})
```

### 4.5 后端接口设计

```java
// MenuController.java
@GetMapping("/menu/authorized-tree")
public Result<List<MenuTreeVO>> getAuthorizedMenuTree() {
    LoginUser loginUser = UserContext.getLoginUser();
    // 根据当前用户的角色，查询其有权访问的菜单树
    // 超级管理员返回全量树，普通用户返回过滤后的子树
    List<MenuTreeVO> tree = menuService.getAuthorizedTree(loginUser);
    return Result.success(tree);
}

// MenuServiceImpl.java
public List<MenuTreeVO> getAuthorizedTree(LoginUser loginUser) {
    if (loginUser.isSuperAdmin()) {
        return tree();  // 返回全量菜单树
    }
    // 1. 查询用户角色的菜单ID集合
    List<Long> menuIds = roleMenuRelMapper.selectList(
        new LambdaQueryWrapper<SysRoleMenuRel>()
            .in(SysRoleMenuRel::getRoleId, loginUser.getRoleIds())
    ).stream().map(SysRoleMenuRel::getMenuId).toList();

    // 2. 过滤出用户有权限的菜单（只保留 menu_type=1 和 2，排除按钮）
    List<SysMenu> menus = menuMapper.selectList(
        new LambdaQueryWrapper<SysMenu>()
            .in(SysMenu::getId, menuIds)
            .in(SysMenu::getMenuType, 1, 2)  // 只返回目录和页面，排除按钮
            .eq(SysMenu::getVisible, 1)      // 只返回可见菜单
            .orderByAsc(SysMenu::getSortOrder)
    );

    // 3. 构建树形结构返回
    return buildMenuTree(menus);
}
```

---

## 五、实施步骤

| 步骤 | 操作 | 风险 |
|------|------|------|
| 1 | 备份 `sys_menu` 和 `sys_role_menu_rel` 表 | — |
| 2 | 执行 `upgrade_v2.11_sync_menu_to_router.sql` | 低，脚本幂等（ON DUPLICATE KEY UPDATE） |
| 3 | 验证菜单树结构（执行SQL中第五步验证语句） | — |
| 4 | 重启后端服务 | 低，仅影响菜单树查询接口 |
| 5 | 用 super_admin 登录，检查菜单是否完整显示 | 中，需人工核对 |
| 6 | 用 test 账号登录，检查各业务页面是否可访问 | 中，需核对权限是否正确 |
| 7 | 开发动态路由前端组件（`router/helper.ts` + `store/permission.ts`） | 高，需充分测试 |
| 8 | 清理 router/index.ts 中的业务静态路由，仅保留固定路由 | 高，旧路由需逐步迁移 |

---

## 六、已完成（v2.11 实施记录）

| 步骤 | 状态 | 说明 |
|------|------|------|
| 1-6 | ✅ 已完成 | SQL 已由用户手动执行，菜单树结构已对齐 |
| 7 | ✅ 已完成 | 新增 `routerHelper.ts`、更新 `permission.ts`、`Sidebar.vue` |
| 8 | ✅ 已完成 | `router/index.ts` 已精简为仅静态路由， Sidebar 优先读动态路由 |
| 后端新增 | ✅ 已完成 | `GET /org/menu/authorized-tree` 接口已实现 |
| 编译验证 | ✅ 通过 | 前后端均编译成功 |

---

## 七、注意事项

1. **删除操作不可逆**：优惠阈值配置(id=131~134)删除后无法恢复，务必先备份
2. **「系统设置」保留并调整**：id=2 从顶级目录并入中台管理(id=1)下，其子菜单(id=7~11)不受影响
3. **「社保参数」新增**：id=207 作为 HR 参数字典类菜单的统一父级目录，便于集中管理
4. **角色权限需重新分配**：删除菜单后，相关角色的 `sys_role_menu_rel` 关联记录已同步清理
5. **component 路径映射**：动态路由方案要求 `path` 与 views 目录严格对应，建议先建立 path → component 映射配置文件
6. **按钮权限独立处理**：动态路由只生成页面级路由，按钮权限通过 `v-permission` 指令从 permissions 数组中判断，不依赖路由
7. **router/index.ts 废弃规范**：
   - 自 v2.11 起，业务路由统一从 sys_menu 表动态生成
   - router/index.ts 仅保留 `/login`、`/403`、`/:pathMatch(.*)*` 等固定路由
   - **严禁**在 router/index.ts 中新增业务路由，新增页面请直接操作后台菜单管理界面
   - 已在文件头部添加废弃注释，请阅读并严格遵守

## 七、v2.11 实施完成记录

| 变更项 | 文件 | 说明 |
|--------|------|------|
| 后端新增接口 | `MenuController.java` | 新增 `GET /org/menu/authorized-tree` |
| 后端新增方法 | `MenuService.java` / `MenuServiceImpl.java` | 新增 `getAuthorizedTree()`，超级管理员返回全量树，普通用户返回授权子树 |
| 前端新增工具 | `src/utils/routerHelper.ts` | `generateRoutes()` 递归生成路由，`pathToComponent()` 路径推导 |
| 前端修改 store | `src/store/permission.ts` | `setupDynamicRoutes()` 登录时动态注册路由 |
| 前端修改路由 | `src/router/index.ts` | 精简为静态兜底路由，beforeEach 调用动态注册 |
| 前端修改侧边栏 | `src/components/layout/Sidebar.vue` | 优先读取 `permissionStore.dynamicRoutes`，无动态路由时兜底静态 |
| 前端新增 API | `src/api/org.ts` | 新增 `getAuthorizedMenuTreeApi()` |
| SQL 脚本 | `upgrade_v2.11_sync_menu_to_router.sql` | 菜单结构调整（已执行） |
