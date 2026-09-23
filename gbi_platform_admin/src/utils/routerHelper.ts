// 动态路由工具：根据菜单树生成 Vue Router 路由对象
// 自 v2.11 起使用，替代 router/index.ts 中的静态业务路由
//
// 核心机制：
// 1. import.meta.glob 预扫描 views 目录所有 .vue 文件（Vite 编译时静态收集）
// 2. pathToComponentGlobKey 把菜单 path 映射到 glob 返回的 key（必须是同一个字符串）
// 3. generateRoutes 直接把 glob 的 value（懒加载函数）挂到 route.component 上
//
// 约定：
// - menu_type=1（目录）→ Layout 容器，children 递归展开
// - menu_type=2（页面）→ 具体组件，path 映射到 glob key
//
// 路径格式统一：glob 模式用相对路径 ../views/ 下的所有 .vue 文件，
// 所以所有 glob key 都是 ../views/xxx.vue 格式。
// override 表的 value、自动推导的候选路径、fallback 路径全部必须是这个格式。
// 不能混用 @/views/xxx.vue，glob 找不到。
import type { RouteRecordRaw } from 'vue-router'
import type { MenuVO } from '@/api/org'

// 预扫描 views 目录所有 .vue 文件
// glob 模式不支持 Vite alias（@/），必须用相对路径
// 返回示例：key='../views/platform/org/index.vue', value=() => Promise<Component>
const VIEWS_GLOB = import.meta.glob('../views/**/*.vue', { eager: false }) as Record<
  string,
  () => Promise<unknown>
>

/**
 * path → glob key 手动 override 映射表
 *
 * key: sys_menu.path 字段值（完整路径，camelCase）
 * value: glob 返回的 key 格式（'../views/xxx.vue'，相对路径，不能用 @/）
 *
 * 用途：
 * - 处理非标准目录结构（如 business/property 这种多一层 business 的）
 * - 处理文件名非 index.vue 的页面（如 waterElecBill.vue）
 * - 处理 path 与物理目录完全不匹配的历史遗留
 *
 * 维护规则：新增页面时检查此表是否需要补充
 */
const PATH_COMPONENT_OVERRIDE: Record<string, string> = {
  // ========== 中台管理 ==========
  '/org': '../views/platform/org/index.vue',
  '/org/menu': '../views/platform/menu/index.vue',
  '/org/role': '../views/platform/role/index.vue',
  '/org/user': '../views/platform/user/index.vue',

  // ========== 系统设置 ==========
  '/sys/dict': '../views/platform/dict/index.vue',
  '/sys/config': '../views/platform/config/index.vue',
  '/sys/theme': '../views/platform/theme/index.vue',
  '/sys/audit': '../views/platform/audit/index.vue',
  '/sys/permissionAudit': '../views/platform/permissionAudit/index.vue',

  // ========== 社保参数（path 挂在 /platform 下但文件在 hr/ 目录）==========
  '/platform/city': '../views/hr/city/index.vue',
  '/platform/industry': '../views/hr/industry/index.vue',
  '/platform/insuranceType': '../views/hr/insuranceType/index.vue',
  '/platform/socialParam': '../views/hr/socialParam/index.vue',
  '/platform/housingFundConfig': '../views/hr/housingFundConfig/index.vue',
  '/platform/deviceAuth': '../views/platform/deviceAuth/index.vue',

  // ========== 物业管理 ==========
  '/property/tenant': '../views/business/property/tenantList.vue',
  '/property/market': '../views/business/property/marketList.vue',
  '/property/bill': '../views/business/waterElec/waterElecBill.vue',
  '/property/meter/pay': '../views/business/waterElec/waterElecPay.vue',
  '/property/feeBill': '../views/business/property/feeBill.vue',
  '/property/feeBill/list': '../views/business/property/feeBill.vue',
  '/property/unpaidBill': '../views/business/property/unpaidBill.vue',
  '/property/feePay': '../views/business/property/propertyFeePay.vue',

  // 租赁子模块
  '/property/lease/stall': '../views/business/property/leaseStall.vue',
  '/property/lease/category': '../views/business/property/leaseCategory.vue',
  '/property/lease/contract': '../views/business/property/leaseContract.vue',
  '/property/lease/stall/canvas': '../views/business/property/stallCanvas.vue',

  // ========== 财务管理 ==========
  '/finance/flow': '../views/finance/financeFlow.vue',
  '/finance/report': '../views/finance/financeReport.vue',
  '/finance/payOrder': '../views/finance/financePay.vue',
  '/finance/feeItem': '../views/finance/feeItem/index.vue',
  '/finance/feeRule': '../views/finance/feeRule/index.vue',
  '/finance/discountPolicy': '../views/finance/discountPolicy/index.vue',
  '/finance/discountApply': '../views/finance/discountApply/index.vue',
  '/finance/recvPayPlan': '../views/finance/recvPayPlan/index.vue',

  // ========== 审批中心（path=/flow 但文件在 oa/flow/ 下）==========
  '/flow/task': '../views/oa/flow/todoList.vue',
  '/flow/apply': '../views/oa/flow/applyList.vue',
  '/flow/definition': '../views/oa/flow/defList.vue',
  '/flow/instance': '../views/oa/flow/instanceList.vue',

  // ========== HR 管理 ==========
  '/hr/org': '../views/hr/org/index.vue',
  '/hr/employee': '../views/hr/employee/index.vue',
  '/hr/transfer': '../views/hr/transfer/index.vue',
  '/hr/attendance': '../views/hr/attendance/index.vue',
  '/hr/salary': '../views/hr/salary/index.vue',
  '/hr/salary/grade': '../views/hr/salary/grade.vue',
  '/hr/salary/rule': '../views/hr/salary/rule.vue',
  '/hr/salary/archive': '../views/hr/salary/archive.vue',
  '/hr/salary/batchAdjust': '../views/hr/salary/batchAdjust.vue',
  '/hr/salary/yearBonus': '../views/hr/salary/yearBonus.vue',
  '/hr/social': '../views/hr/social/index.vue',
  '/hr/socialCalc': '../views/hr/socialCalc/index.vue',
  '/hr/annualRecalc': '../views/hr/annualRecalc/index.vue',

  // ========== OA 办公 ==========
  '/oa/announcement': '../views/oa/announcement.vue',
  '/oa/meetingRoom': '../views/oa/meetingRoom.vue',
  '/oa/meetingBooking': '../views/oa/meetingRoom.vue',
  '/oa/leave': '../views/oa/leave.vue',
  '/oa/clock': '../views/oa/clock.vue',
  '/oa/workReport': '../views/oa/workReport.vue',
}

/**
 * 自动推导 path → glob key 的候选路径
 * 所有返回值必须是 '../views/xxx.vue' 格式（与 glob key 格式一致）
 */
function buildCandidates(path: string): string[] {
  const candidates: string[] = []
  let p = path.replace(/^\//, '')
  const segments = p.split('/').filter(Boolean)
  if (segments.length === 0) {
    candidates.push('../views/dashboard/index.vue')
    return candidates
  }

  const lastRaw = segments[segments.length - 1]
  const dir = segments.slice(0, -1).join('/')

  if (dir) {
    candidates.push(`../views/${dir}/${lastRaw}.vue`)
    candidates.push(`../views/${dir}/${lastRaw}/index.vue`)
  } else {
    candidates.push(`../views/${lastRaw}/index.vue`)
    candidates.push(`../views/${lastRaw}.vue`)
  }

  return candidates
}

/**
 * path → glob key 查找
 * 返回值一定是 glob 能查到的 key（'../views/xxx.vue'），
 * 实在找不到兜底到 '../views/error/404.vue'
 */
function pathToComponentGlobKey(path: string): string {
  if (!path) return '../views/error/404.vue'

  // 1. 手动 override 表（优先级最高）
  if (PATH_COMPONENT_OVERRIDE[path]) {
    const key = PATH_COMPONENT_OVERRIDE[path]
    if (VIEWS_GLOB[key]) return key
    console.warn('[routerHelper] override 指向的文件不存在，降级自动推导:', path, '→', key)
  }

  // 2. 自动推导
  const candidates = buildCandidates(path)
  for (const key of candidates) {
    if (VIEWS_GLOB[key]) return key
  }

  // 3. 兜底 404
  console.warn('[routerHelper] path 未匹配到任何 .vue 文件:', path, '候选:', candidates)
  return '../views/error/404.vue'
}

/**
 * 驼峰转换（用于路由 name）
 */
function toCamelCase(str: string): string {
  return str
    .replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')
    .replace(/_([a-z])/g, (_, c) => c.toUpperCase())
    .replace(/^_/, '')
}

/**
 * 扁平化递归收集所有页面路由
 *
 * 设计说明：
 * - 外层 permission.ts 已经注册了顶级 Layout（path='/'），所有页面路由作为它的 children
 * - 目录（menu_type=1）仅用于侧边栏分组，不生成路由
 * - 只有页面（menu_type=2）才生成路由对象，path 是完整绝对路径
 * - 页面可以出现在菜单树的任意层级（顶级目录、二级目录...），都被递归收集
 * - 最终返回的数组直接作为 Layout 的 children，每个元素 path 如 '/org/user'
 */
export function generateRoutes(menuTree: MenuVO[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  function walk(nodes: MenuVO[]) {
    for (const node of nodes) {
      if (node.menuType === 2 && node.path && node.permission) {
        // 页面节点 → 生成路由
        const globKey = pathToComponentGlobKey(node.path)
        const loader = VIEWS_GLOB[globKey]
        if (!loader) {
          console.error('[routerHelper] glob key 查找失败:', node.path, '→', globKey)
          continue
        }
        routes.push({
          path: node.path,
          name: toCamelCase(node.menuName || node.path),
          component: loader as RouteRecordRaw['component'],
          meta: {
            title: node.menuName,
            icon: node.icon,
            permission: node.permission,
            isCache: true,
          },
        })
      }
      // 递归处理子节点（无论目录还是页面都可能有子节点）
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }

  walk(menuTree)
  return routes
}
