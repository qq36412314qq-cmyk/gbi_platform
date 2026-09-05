/**
 * 路由配置：与后台菜单、权限标识一一对应
 * meta 固定三元组：title / icon / permission
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'
import { getThemeApi } from '@/api/sys'

/** 静态路由（一期固定，二期切换后端菜单动态生成） */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'HomeFilled', permission: '', isCache: true }
      }
    ]
  },
  {
    path: '/platform',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/platform/org',
    meta: { title: '集团中台', icon: 'Setting' },
    children: [
      {
        path: 'org',
        name: 'PlatformOrg',
        component: () => import('@/views/platform/org/index.vue'),
        meta: { title: '组织管理', icon: 'OfficeBuilding', permission: 'org:list', isCache: true }
      },
      {
        path: 'user',
        name: 'PlatformUser',
        component: () => import('@/views/platform/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'user:list', isCache: true }
      },
      {
        path: 'role',
        name: 'PlatformRole',
        component: () => import('@/views/platform/role/index.vue'),
        meta: { title: '角色管理', icon: 'Avatar', permission: 'role:list', isCache: true }
      },
      {
        path: 'menu',
        name: 'PlatformMenu',
        component: () => import('@/views/platform/menu/index.vue'),
        meta: { title: '菜单权限', icon: 'Menu', permission: 'menu:list', isCache: true }
      },
      {
        path: 'dict',
        name: 'PlatformDict',
        component: () => import('@/views/platform/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection', permission: 'dict:list', isCache: true }
      },
      {
        path: 'config',
        name: 'PlatformConfig',
        component: () => import('@/views/platform/config/index.vue'),
        meta: { title: '参数配置', icon: 'Tools', permission: 'config:list', isCache: true }
      },
      {
        path: 'theme',
        name: 'PlatformTheme',
        component: () => import('@/views/platform/theme/index.vue'),
        meta: { title: 'UI主题', icon: 'Brush', permission: 'theme:list', isCache: true }
      },
      {
        path: 'audit',
        name: 'PlatformAudit',
        component: () => import('@/views/platform/audit/index.vue'),
        meta: { title: '审计日志', icon: 'Document', permission: 'audit:list', isCache: true }
      },
      {
        path: 'permissionAudit',
        name: 'PlatformPermissionAudit',
        component: () => import('@/views/platform/permissionAudit/index.vue'),
        meta: { title: '权限复核', icon: 'Key', permission: 'permission:audit:list', isCache: true }
      }
    ]
  },
  {
    path: '/property',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/property/meter',
    meta: { title: '物业管理', icon: 'Odometer' },
    children: [
      {
        path: 'market',
        name: 'PropertyMarket',
        component: () => import('@/views/business/property/marketList.vue'),
        meta: { title: '市场管理', icon: 'OfficeBuilding', permission: 'market:list', isCache: true }
      },
      {
        path: 'tenant',
        name: 'PropertyTenant',
        component: () => import('@/views/business/property/tenantList.vue'),
        meta: { title: '租户管理', icon: 'User', permission: 'tenant:list', isCache: true }
      },
      {
        path: 'meter',
        name: 'WaterElecMeter',
        component: () => import('@/views/business/waterElec/waterElecMeter.vue'),
        meta: { title: '水电表管理', icon: 'Cpu', permission: 'waterElec:list', isCache: true }
      },
      {
        path: 'bill',
        name: 'WaterElecBill',
        component: () => import('@/views/business/waterElec/waterElecBill.vue'),
        meta: { title: '水电费记录', icon: 'Document', permission: 'waterElec:bill:list', isCache: true }
      },
      {
        path: 'feeBill',
        name: 'PropertyFeeBill',
        component: () => import('@/views/business/property/feeBill.vue'),
        meta: { title: '物业费记录', icon: 'Document', permission: 'property:feeBill:list', isCache: true }
      },
      {
        path: 'unpaidBill',
        name: 'PropertyUnpaidBill',
        component: () => import('@/views/business/property/unpaidBill.vue'),
        meta: { title: '未支付订单', icon: 'Wallet', permission: 'property:unpaidBill:list', isCache: true }
      },
      {
        path: 'pay',
        name: 'WaterElecPay',
        component: () => import('@/views/business/waterElec/waterElecPay.vue'),
        meta: { title: '缴费管理', icon: 'Wallet', permission: 'waterElec:pay:list', isCache: true }
      },
      {
        path: 'lease',
        redirect: '/property/lease/stall',
        meta: { title: '租赁管理', icon: 'Goods' },
        children: [
          {
            path: 'stall',
            name: 'PropertyLeaseStall',
            component: () => import('@/views/business/property/leaseStall.vue'),
            meta: { title: '铺位管理', icon: 'OfficeBuilding', permission: 'lease:stall:list', isCache: true }
          },
          {
            path: 'category',
            name: 'PropertyLeaseCategory',
            component: () => import('@/views/business/property/leaseCategory.vue'),
            meta: { title: '租赁分类', icon: 'Menu', permission: 'lease:category:list', isCache: true }
          },
          {
            path: 'contract',
            name: 'PropertyLeaseContract',
            component: () => import('@/views/business/property/leaseContract.vue'),
            meta: { title: '合同管理', icon: 'Document', permission: 'lease:contract:list', isCache: true }
          },
                    {
            path: 'canvas',
            name: 'PropertyLeaseCanvas',
            component: () => import('@/views/business/property/stallCanvas.vue'),
            meta: { title: '铺位画布', icon: 'Coordinate', permission: 'lease:stall:list', isCache: true }
          }
        ]
      }
    ]
  },
  {
    path: '/finance',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/finance/flow',
    meta: { title: '财务管理', icon: 'Money' },
    children: [
      {
        path: 'flow',
        name: 'FinanceFlow',
        component: () => import('@/views/finance/financeFlow.vue'),
        meta: { title: '财务流水', icon: 'List', permission: 'finance:flow:list', isCache: true }
      },
      {
        path: 'report',
        name: 'FinanceReport',
        component: () => import('@/views/finance/financeReport.vue'),
        meta: { title: '营收统计', icon: 'TrendCharts', permission: 'finance:report:list', isCache: true }
      },
      {
        path: 'feeItem',
        name: 'FeeItem',
        component: () => import('@/views/finance/feeItem/index.vue'),
        meta: { title: '收费类型管理', icon: 'Wallet', permission: 'fee:item:list', isCache: true }
      },
      {
        path: 'feeRule',
        name: 'FeeRule',
        component: () => import('@/views/finance/feeRule/index.vue'),
        meta: { title: '收费规则管理', icon: 'Money', permission: 'fee:rule:list', isCache: true }
      },
      {
        path: 'recvPayPlan',
        name: 'RecvPayPlan',
        component: () => import('@/views/finance/recvPayPlan/index.vue'),
        meta: { title: '应收应付计划', icon: 'Calendar', permission: 'plan:recvpay:list', isCache: true }
      },
      {
        path: 'discountPolicy',
        name: 'DiscountPolicy',
        component: () => import('@/views/finance/discountPolicy/index.vue'),
        meta: { title: '优惠策略', icon: 'Discount', permission: 'discount:policy:list', isCache: true }
      },
      {
        path: 'discountApply',
        name: 'DiscountApply',
        component: () => import('@/views/finance/discountApply/index.vue'),
        meta: { title: '优惠申请', icon: 'Ticket', permission: 'discount:apply:list', isCache: true }
      },
      {
        path: 'payOrder',
        name: 'FinancePay',
        component: () => import('@/views/finance/financePay.vue'),
        meta: { title: '缴费明细单', icon: 'DocumentChecked', permission: 'finance:payOrder:list', isCache: true }
      }
    ]
  },
  {
    path: '/flow',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/flow/task',
    meta: { title: '审批中心', icon: 'Finished' },
    children: [
      {
        path: 'task',
        name: 'FlowTask',
        component: () => import('@/views/oa/flow/todoList.vue'),
        meta: { title: '待办处理', icon: 'List', permission: 'flow:task:list', isCache: true }
      },
      {
        path: 'apply',
        name: 'FlowApply',
        component: () => import('@/views/oa/flow/applyList.vue'),
        meta: { title: '我的申请', icon: 'EditPen', permission: 'flow:apply:list', isCache: true }
      },
      {
        path: 'definition',
        name: 'FlowDefinition',
        component: () => import('@/views/oa/flow/defList.vue'),
        meta: { title: '流程定义', icon: 'Setting', permission: 'flow:def:list', isCache: true }
      },
      {
        path: 'instance',
        name: 'FlowInstance',
        component: () => import('@/views/oa/flow/instanceList.vue'),
        meta: { title: '流程实例', icon: 'Document', permission: 'flow:instance:list', isCache: true }
      }
    ]
  },
  {
    path: '/oa',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/oa/leave',
    meta: { title: '协同办公', icon: 'Notebook' },
    children: [
      {
        path: 'leave',
        name: 'OaLeave',
        component: () => import('@/views/oa/leave.vue'),
        meta: { title: '请假管理', icon: 'DocumentChecked', permission: 'oa:leave:list', isCache: true }
      },
      {
        path: 'announcement',
        name: 'OaAnnouncement',
        component: () => import('@/views/oa/announcement.vue'),
        meta: { title: '公告管理', icon: 'Bell', permission: 'oa:announcement:list', isCache: true }
      },
      {
        path: 'meetingRoom',
        name: 'OaMeetingRoom',
        component: () => import('@/views/oa/meetingRoom.vue'),
        meta: { title: '会议室管理', icon: 'Calendar', permission: 'oa:meeting-room:list', isCache: true }
      },
      {
        path: 'clock',
        name: 'OaClock',
        component: () => import('@/views/oa/clock.vue'),
        meta: { title: '打卡管理', icon: 'Watch', permission: 'oa:clock:list', isCache: true }
      },
      {
        path: 'workReport',
        name: 'OaWorkReport',
        component: () => import('@/views/oa/workReport.vue'),
        meta: { title: '工作汇报', icon: 'Files', permission: 'oa:work-report:list', isCache: true }
      }
    ]
  },
  {
    path: '/hr',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/hr/employee',
    meta: { title: '人力资源', icon: 'UserFilled' },
    children: [
      { path: 'org', name: 'HrOrg', component: () => import('@/views/hr/org/index.vue'),
        meta: { title: '组织岗位', icon: 'OfficeBuilding', permission: 'hr:org:list', isCache: true } },
      { path: 'employee', name: 'HrEmployee', component: () => import('@/views/hr/employee/index.vue'),
        meta: { title: '员工档案', icon: 'User', permission: 'hr:employee:list', isCache: true } },
      { path: 'transfer', name: 'HrTransfer', component: () => import('@/views/hr/transfer/index.vue'),
        meta: { title: '人事异动', icon: 'SwitchButton', permission: 'hr:entry:list', isCache: true } },
      { path: 'attendance', name: 'HrAttendance', component: () => import('@/views/hr/attendance/index.vue'),
        meta: { title: '考勤管理', icon: 'Calendar', permission: 'hr:attendance:list', isCache: true } },
      { path: 'salary', name: 'HrSalary', component: () => import('@/views/hr/salary/index.vue'),
        meta: { title: '薪酬管理', icon: 'Money', permission: 'hr:salary:month:list', isCache: true } },
      { path: 'social', name: 'HrSocial', component: () => import('@/views/hr/social/index.vue'),
        meta: { title: '社保公积金', icon: 'Document', permission: 'hr:social:list', isCache: true } },
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '403' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/finance/payOrderPrint/:id',
    name: 'FinancePayOrderPrint',
    component: () => import('@/views/finance/financePayPrint.vue'),
    meta: { title: '缴费单打印' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 白名单：无需登录
const WHITE_LIST = ['/login', '/403', '/finance/payOrderPrint']

/** 全局前置守卫：无 token 跳登录 / 无权限跳 403 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  // 设置页面标题
  document.title = `${to.meta?.title ? to.meta.title + ' - ' : ''}集团业务一体化管控平台`

  const hasToken = !!userStore.token
  if (!hasToken) {
    if (WHITE_LIST.some(p => to.path.startsWith(p))) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  // 已登录访问登录页 -> 跳首页
  if (to.path === '/login') {
    next('/dashboard')
    return
  }

  // 首次进入拉取用户信息与权限、主题
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
      // 拉取主题配置（失败使用默认主题，不阻塞登录）
      try {
        const themeConfig = await getThemeApi()
        useThemeStore().applyTheme(themeConfig as never)
      } catch {
        useThemeStore().applyTheme()
      }
    } catch {
      userStore.resetState()
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }

  // 页面权限校验（meta.permission 为空视为公共页面）
  const permission = to.meta?.permission as string | undefined
  if (permission && !userStore.hasPermission(permission)) {
    next('/403')
    return
  }
  next()
})

export default router