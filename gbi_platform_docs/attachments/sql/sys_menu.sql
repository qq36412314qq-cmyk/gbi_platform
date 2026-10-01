/*
 Navicat Premium Dump SQL

 Source Server         : oa
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : group_rent_db

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 29/09/2026 14:46:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID，0顶级',
  `menu_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识 模块:操作',
  `path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端路由地址',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `menu_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '菜单类型 1目录 2菜单页面 3按钮',
  `visible` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否显示 0隐藏 1显示',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 260 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限菜单表（集团全局）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '中台管理', NULL, '/org', 'office-building', 1, 1, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (2, 1, '系统设置', NULL, '/sys', 'setting', 2, 1, 1, 1, '2026-08-18 12:51:36', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (3, 1, '组织管理', 'org:list', '/org', 'office-building', 1, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (4, 1, '用户管理', 'user:list', '/org/user', 'user', 2, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (5, 1, '角色管理', 'role:list', '/org/role', 'avatar', 3, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (6, 1, '菜单管理', 'menu:list', '/org/menu', 'menu', 4, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (7, 2, '字典管理', 'dict:list', '/sys/dict', 'collection', 1, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (8, 2, '参数配置', 'config:list', '/sys/config', 'tools', 2, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (9, 2, 'UI主题', 'theme:list', '/sys/theme', 'brush', 3, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (10, 2, '审计日志', 'audit:list', '/sys/audit', 'document', 4, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (11, 2, '权限复核', 'permission:audit:list', '/sys/permissionAudit', 'key', 5, 2, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (12, 3, '组织新增', 'org:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (13, 3, '组织编辑', 'org:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (14, 3, '组织删除', 'org:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (15, 4, '用户新增', 'user:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (16, 4, '用户编辑', 'user:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (17, 4, '用户删除', 'user:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (18, 4, '重置密码', 'user:resetPwd', NULL, NULL, 4, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (19, 4, '启用禁用', 'user:changeStatus', NULL, NULL, 5, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (20, 5, '角色新增', 'role:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (21, 5, '角色编辑', 'role:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (22, 5, '角色删除', 'role:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (23, 5, '菜单授权', 'role:menu', NULL, NULL, 4, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (24, 6, '菜单新增', 'menu:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (25, 6, '菜单编辑', 'menu:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (26, 6, '菜单删除', 'menu:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (27, 7, '字典新增', 'dict:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (28, 7, '字典编辑', 'dict:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (29, 7, '字典删除', 'dict:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (30, 8, '参数新增', 'config:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (31, 8, '参数编辑', 'config:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (32, 8, '参数删除', 'config:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (33, 9, '主题保存', 'theme:edit', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (34, 10, '日志导出', 'audit:export', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (35, 11, '复核处理', 'permission:audit:audit', NULL, NULL, 1, 3, 0, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (36, 0, '物业管理', NULL, '/property', 'Odometer', 3, 1, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 16:38:27', 0);
INSERT INTO `sys_menu` VALUES (37, 36, '水电表管理', 'waterElec:list', '/property/meter', 'Cpu', 1, 2, 1, 1, '2026-08-18 15:53:31', 1, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (38, 36, '水电费账单', 'waterElec:bill:list', '/property/bill', 'Document', 2, 2, 1, 1, '2026-08-18 15:53:31', 1, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (40, 0, '财务管理', NULL, '/finance', 'Money', 4, 1, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (41, 40, '财务流水', 'finance:flow:list', '/finance/flow', 'List', 1, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-09-21 09:20:46', 0);
INSERT INTO `sys_menu` VALUES (42, 40, '营收统计', 'finance:report:list', '/finance/report', 'TrendCharts', 2, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (43, 37, '设备新增', 'waterElec:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (44, 37, '设备编辑', 'waterElec:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (45, 37, '设备删除', 'waterElec:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (46, 37, '远程抄表', 'waterElec:read', NULL, NULL, 4, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (47, 37, '合闸断电', 'waterElec:switch', NULL, NULL, 5, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (48, 38, '生成账单', 'waterElec:bill:generate', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (51, 41, '流水导出', 'finance:flow:export', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (52, 36, '租户管理', 'tenant:list', '/property/tenant', 'User', 1, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (53, 36, '租赁管理', NULL, '/property/lease', 'Goods', 2, 1, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (54, 53, '铺位管理', 'lease:stall:list', '/property/lease/stall', 'OfficeBuilding', 1, 2, 1, 1, '2026-08-18 16:38:27', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (55, 53, '租赁分类', 'lease:category:list', '/property/lease/category', 'Menu', 2, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (56, 53, '合同管理', 'lease:contract:list', '/property/lease/contract', 'Document', 3, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (57, 52, '租户新增', 'tenant:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (58, 52, '租户编辑', 'tenant:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (59, 52, '租户删除', 'tenant:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (60, 54, '摊位新增', 'lease:stall:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (61, 54, '摊位编辑', 'lease:stall:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (62, 54, '摊位删除', 'lease:stall:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (63, 55, '分类新增', 'lease:category:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (64, 55, '分类编辑', 'lease:category:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (65, 55, '分类删除', 'lease:category:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (66, 56, '合同新增', 'lease:contract:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (67, 56, '合同退租', 'lease:contract:terminate', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (68, 36, '市场管理', 'market:list', '/property/market', 'OfficeBuilding', 1, 2, 1, 1, '2026-08-18 18:17:04', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (69, 68, '市场新增', 'market:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 18:17:04', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (70, 68, '市场编辑', 'market:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 18:17:04', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (71, 68, '市场删除', 'market:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 18:17:04', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (72, 40, '收费类型管理', 'fee:item:list', '/finance/feeItem', 'Wallet', 3, 2, 1, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (73, 72, '收费类型新增', 'fee:item:add', NULL, NULL, 1, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (74, 72, '收费类型编辑', 'fee:item:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (75, 72, '收费类型删除', 'fee:item:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (76, 40, '收费规则管理', 'fee:rule:list', '/finance/feeRule', 'Money', 4, 2, 1, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (77, 76, '收费规则新增', 'fee:rule:add', NULL, NULL, 1, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (78, 76, '收费规则编辑', 'fee:rule:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (79, 76, '收费规则删除', 'fee:rule:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-19 12:24:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (80, 40, '应收应付计划', 'plan:recvpay:list', '/finance/recvPayPlan', 'Calendar', 5, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (81, 80, '计划生成', 'plan:recvpay:generate', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (82, 80, '计划调账', 'plan:recvpay:adjust', NULL, NULL, 2, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (83, 80, '计划作废终止', 'plan:recvpay:terminate', NULL, NULL, 3, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (84, 80, '计划导出', 'plan:recvpay:export', NULL, NULL, 4, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (85, 80, '计划对账', 'plan:recvpay:reconcile', NULL, NULL, 5, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (86, 40, '优惠策略', 'discount:policy:list', '/finance/discountPolicy', 'Discount', 6, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (87, 86, '优惠策略新增', 'discount:policy:add', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (88, 86, '优惠策略编辑', 'discount:policy:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (89, 86, '优惠策略删除', 'discount:policy:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (90, 40, '优惠申请', 'discount:apply:list', '/finance/discountApply', 'Ticket', 7, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (91, 90, '优惠申请撤销', 'discount:apply:cancel', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (92, 0, '审批中心', NULL, '/flow', 'Finished', 5, 1, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (93, 92, '待办处理', 'flow:task:list', '/flow/task', 'List', 1, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (94, 92, '我的申请', 'flow:apply:list', '/flow/apply', 'EditPen', 2, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (95, 92, '流程定义', 'flow:def:list', '/flow/definition', 'Setting', 3, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (96, 92, '流程实例', 'flow:instance:list', '/flow/instance', 'Document', 4, 2, 1, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (97, 93, '审批处理', 'flow:task:handle', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (98, 93, '催办', 'flow:task:urge', NULL, NULL, 2, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (99, 94, '撤销申请', 'flow:apply:cancel', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (100, 95, '流程定义新增', 'flow:def:add', NULL, NULL, 1, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (101, 95, '流程定义编辑', 'flow:def:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (102, 95, '流程定义删除', 'flow:def:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-20 13:50:19', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (107, 0, '人力资源', NULL, '/hr', 'UserFilled', 3, 1, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (108, 107, '员工档案', 'hr:employee:list', '/hr/employee', 'User', 1, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (109, 107, '组织岗位', 'hr:org:list', '/hr/org', 'OfficeBuilding', 2, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (110, 107, '人事异动', 'hr:entry:list', '/hr/transfer', 'SwitchButton', 3, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (111, 107, '考勤管理', 'hr:attendance:list', '/hr/attendance', 'Calendar', 4, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (112, 107, '薪酬管理', 'hr:salary:month:list', '/hr/salary', 'Money', 5, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (113, 107, '社保公积金', 'hr:social:list', '/hr/social', 'Document', 6, 2, 1, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (114, 108, '新增', 'hr:employee:add', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (115, 108, '编辑', 'hr:employee:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (116, 108, '删除', 'hr:employee:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (117, 108, '导出', 'hr:employee:export', NULL, NULL, 4, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (118, 109, '新增岗位', 'hr:org:post:add', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (119, 109, '编辑岗位', 'hr:org:post:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (120, 109, '删除岗位', 'hr:org:post:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (121, 110, '入职申请', 'hr:entry:add', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (122, 110, '离职申请', 'hr:resign:add', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (123, 233, '同步考勤', 'hr:attendance:sync', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, '2026-09-27 18:42:25', 0);
INSERT INTO `sys_menu` VALUES (124, 233, '导出', 'hr:attendance:export', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, '2026-09-27 18:42:28', 0);
INSERT INTO `sys_menu` VALUES (125, 112, '生成核算', 'hr:salary:month:generate', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (126, 112, '薪资发放', 'hr:salary:month:pay', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (127, 112, '导出', 'hr:salary:month:export', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (128, 113, '新增', 'hr:social:add', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (129, 113, '编辑', 'hr:social:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (130, 113, '删除', 'hr:social:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (135, 112, '薪酬级别', 'hr:salary:grade:list', '/hr/salary/grade', 'Rank', 6, 2, 1, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (136, 112, '薪资模板', 'hr:salary:rule:list', '/hr/salary/rule', 'EditPen', 7, 2, 1, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (137, 112, '薪资档案', 'hr:salary:archive:list', '/hr/salary', 'DocumentChecked', 8, 2, 1, 1, '2026-09-11 15:29:28', 1, '2026-09-22 13:20:44', 0);
INSERT INTO `sys_menu` VALUES (138, 112, '批量调薪', 'hr:salary:batch:list', '/hr/salary/batchAdjust', 'Sort', 9, 2, 1, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (139, 112, '年终奖管理', 'hr:salary:yearBonus:list', '/hr/salary/yearBonus', 'Present', 10, 2, 1, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (148, 135, '新增', 'hr:salary:grade:add', NULL, NULL, 1, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (149, 135, '编辑', 'hr:salary:grade:edit', NULL, NULL, 2, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (150, 135, '停用', 'hr:salary:grade:disable', NULL, NULL, 3, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (151, 136, '新增', 'hr:salary:rule:add', NULL, NULL, 1, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (152, 136, '编辑', 'hr:salary:rule:edit', NULL, NULL, 2, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (153, 136, '停用', 'hr:salary:rule:disable', NULL, NULL, 3, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (154, 136, '提交审批', 'hr:salary:rule:submit', NULL, NULL, 4, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (155, 138, '创建任务', 'hr:salary:batch:add', NULL, NULL, 1, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (156, 138, '提交审批', 'hr:salary:batch:submit', NULL, NULL, 2, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (157, 139, '新增', 'hr:salary:yearBonus:add', NULL, NULL, 1, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (158, 139, '编辑', 'hr:salary:yearBonus:edit', NULL, NULL, 2, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (159, 139, '提交审批', 'hr:salary:yearBonus:submit', NULL, NULL, 3, 3, 0, 1, '2026-09-11 15:29:28', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (160, 207, '城市字典', 'hr:city:list', '/platform/city', 'Location', 1, 2, 1, 0, '2026-09-13 19:47:38', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (161, 207, '险种字典', 'hr:insurance:list', '/platform/insuranceType', 'Tickets', 2, 2, 1, 0, '2026-09-13 19:47:38', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (162, 207, '行业字典', 'hr:industry:list', '/platform/industry', 'Briefcase', 3, 2, 1, 0, '2026-09-13 19:47:38', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (163, 207, '社保参数配置', 'hr:social:param:list', '/platform/socialParam', 'Setting', 4, 2, 1, 0, '2026-09-13 19:47:38', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (164, 207, '公积金参数配置', 'hr:housing:fund:list', '/platform/housingFundConfig', 'Coin', 5, 2, 1, 0, '2026-09-13 19:47:38', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (165, 113, '社保核算明细', 'hr:social:calc:list', '/hr/socialCalc', 'Document', 1, 2, 1, 0, '2026-09-13 19:47:38', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (166, 113, '年度基数重算', 'hr:recalc:trigger', '/hr/annualRecalc', 'Refresh', 2, 2, 1, 0, '2026-09-13 19:47:38', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (167, 160, '新增', 'hr:city:add', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (168, 160, '编辑', 'hr:city:edit', '', '', 2, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (169, 160, '删除', 'hr:city:delete', '', '', 3, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (170, 161, '新增', 'hr:insurance:add', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (171, 161, '编辑', 'hr:insurance:edit', '', '', 2, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (172, 161, '删除', 'hr:insurance:delete', '', '', 3, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (173, 162, '新增', 'hr:industry:add', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (174, 162, '编辑', 'hr:industry:edit', '', '', 2, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (175, 162, '删除', 'hr:industry:delete', '', '', 3, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (176, 163, '新增', 'hr:social:param:add', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (177, 163, '编辑', 'hr:social:param:edit', '', '', 2, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (178, 163, '激活', 'hr:social:param:activate', '', '', 3, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (179, 163, '停用', 'hr:social:param:deactivate', '', '', 4, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (180, 163, '删除', 'hr:social:param:delete', '', '', 5, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (181, 164, '新增', 'hr:housing:fund:add', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (182, 164, '编辑', 'hr:housing:fund:edit', '', '', 2, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (183, 164, '激活', 'hr:housing:fund:activate', '', '', 3, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (184, 164, '删除', 'hr:housing:fund:delete', '', '', 4, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (185, 165, '导出', 'hr:social:calc:export', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (186, 166, '执行重算', 'hr:recalc:execute', '', '', 1, 3, 0, 0, '2026-09-13 19:47:38', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (187, 1, '设备授权管理', 'device:auth:list', '/platform/deviceAuth', 'Lock', 16, 2, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (188, 0, 'OA办公', NULL, '/oa', 'Notebook', 4, 1, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (189, 188, '公告管理', 'oa:announcement:list', '/oa/announcement', 'Bell', 1, 2, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (190, 188, '会议室管理', 'oa:meeting-room:list', '/oa/meetingRoom', 'Calendar', 2, 2, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (191, 188, '打卡管理', 'oa:clock:list', '/oa/clock', 'Watch', 3, 2, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (192, 188, '工作汇报', 'oa:work-report:list', '/oa/workReport', 'Files', 4, 2, 1, 0, '2026-09-20 13:09:32', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (193, 36, '物业费账单', '', '/property/feeBill', 'Money', 3, 2, 1, 0, '2026-09-20 13:09:32', 1, '2026-09-24 09:19:40', 1);
INSERT INTO `sys_menu` VALUES (194, 36, '物业费账单', 'property:feeBill:list', '/property/feeBill/list', 'Document', 1, 2, 1, 0, '2026-09-20 13:09:32', 1, '2026-09-24 08:52:17', 0);
INSERT INTO `sys_menu` VALUES (195, 36, '未支付订单', 'property:unpaidBill:list', '/property/unpaidBill', 'Wallet', 4, 2, 1, 0, '2026-09-20 13:09:32', NULL, '2026-09-21 10:06:00', 0);
INSERT INTO `sys_menu` VALUES (196, 36, '缴费管理', 'waterElec:pay:list', '/property/meter/pay', 'Money', 3, 2, 1, 0, '2026-09-20 13:10:30', 1, '2026-09-24 08:51:17', 0);
INSERT INTO `sys_menu` VALUES (197, 188, '请假管理', 'oa:leave:list', '/oa/leave', 'Document', 5, 2, 1, 0, '2026-09-20 13:10:30', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (198, 188, '会议室预约', 'oa:meeting:booking:list', '/oa/meetingBooking', 'Calendar', 6, 2, 1, 0, '2026-09-20 13:10:30', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (199, 36, '缴费管理', 'property:feePay:list', '/property/feePay', 'Money', 5, 2, 1, 0, '2026-09-20 13:10:30', 1, '2026-09-21 13:45:40', 1);
INSERT INTO `sys_menu` VALUES (200, 196, '在线缴费', 'waterElec:pay:add', NULL, NULL, 1, 3, 0, 0, '2026-09-20 13:10:59', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (201, 196, '退费处理', 'waterElec:pay:refund', NULL, NULL, 2, 3, 0, 0, '2026-09-20 13:10:59', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (202, 197, '新增申请', 'oa:leave:add', NULL, NULL, 1, 3, 0, 0, '2026-09-20 13:10:59', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (203, 53, '铺位画布', 'lease:stall:canvas', '/property/lease/stall/canvas', 'Coordinate', 4, 2, 1, 0, '2026-09-21 10:06:00', 1, '2026-09-21 13:35:27', 0);
INSERT INTO `sys_menu` VALUES (204, 40, '缴费明细单', 'finance:payOrder:list', '/finance/payOrder', 'DocumentChecked', 8, 2, 1, 0, '2026-09-21 10:06:00', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (207, 1, '社保参数', NULL, '/platform/socialParam', 'Setting', 10, 1, 1, 0, '2026-09-21 10:06:00', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (208, 110, '撤销入职', 'hr:entry:revoke', NULL, NULL, 2, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (210, 110, '新增转正', 'hr:regular:add', NULL, NULL, 1, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-23 21:32:37', 0);
INSERT INTO `sys_menu` VALUES (211, 110, '撤销转正', 'hr:regular:revoke', NULL, NULL, 2, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-23 21:32:37', 0);
INSERT INTO `sys_menu` VALUES (213, 110, '新增调岗', 'hr:transfer:add', NULL, NULL, 1, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-23 21:32:37', 0);
INSERT INTO `sys_menu` VALUES (214, 110, '撤销调岗', 'hr:transfer:revoke', NULL, NULL, 2, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-23 21:32:37', 0);
INSERT INTO `sys_menu` VALUES (217, 110, '撤销离职', 'hr:resign:revoke', NULL, NULL, 2, 3, 0, 1, '2026-09-23 21:23:43', NULL, '2026-09-23 21:32:37', 0);
INSERT INTO `sys_menu` VALUES (218, 137, '提交审批', 'hr:salary:archive:submit', NULL, NULL, 1, 3, 0, 1, '2026-09-23 21:23:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (219, 137, '审批通过', 'hr:salary:archive:approve', NULL, NULL, 2, 3, 0, 1, '2026-09-23 21:23:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (220, 112, '待处理档案', 'hr:salary:month:pending', NULL, NULL, 4, 3, 0, 1, '2026-09-23 21:23:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (221, 137, '待审批档案', 'hr:salary:pending:archive', NULL, NULL, 3, 3, 0, 1, '2026-09-23 21:23:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (222, 136, '按岗位查询', 'hr:salary:rule:listByPost', NULL, NULL, 5, 3, 0, 1, '2026-09-23 21:23:43', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (223, 111, '考勤异常管理', 'hr:attendance:exception:view', '/hr/attendance/exception', 'Bell', 1, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (225, 111, '同步预览', 'hr:attendance:preview', NULL, '', 3, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (226, 111, '异常查看', 'hr:attendance:exception:view', NULL, '', 4, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (227, 111, '异常处理', 'hr:attendance:exception:handle', NULL, '', 5, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (229, 226, '参数编辑', 'hr:config:attendance:edit', NULL, '', 1, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (230, 226, '班次配置', 'hr:config:shift', NULL, '', 2, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (231, 226, '休息日配置', 'hr:config:workweek', NULL, '', 3, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (232, 226, '节假日配置', 'hr:config:holiday', NULL, '', 4, 3, 0, 1, '2026-09-27 17:40:02', NULL, '2026-09-28 08:21:33', 0);
INSERT INTO `sys_menu` VALUES (234, 0, '企微配置管理', 'wecom:config:list', NULL, NULL, 99, 1, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (235, 234, '基础配置', 'wecom:config:view', NULL, NULL, 1, 2, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (236, 234, '同步记录', 'wecom:sync:view', NULL, NULL, 2, 2, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (237, 234, '消息记录', 'wecom:message:view', NULL, NULL, 3, 2, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (238, 234, '编辑配置', 'wecom:config:edit', NULL, NULL, 1, 3, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (239, 234, '刷新Token', 'wecom:token:refresh', NULL, NULL, 2, 3, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (240, 234, '手动触发同步', 'wecom:sync:trigger', NULL, NULL, 3, 3, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (241, 234, '导出同步记录', 'wecom:sync:export', NULL, NULL, 4, 3, 0, 1, '2026-09-29 12:34:47', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (242, 107, '加班管理', 'hr:overtime:list', '', '', 99, 1, 0, 1, '2026-09-29 13:03:08', 1, '2026-09-29 14:33:42', 0);
INSERT INTO `sys_menu` VALUES (243, 242, '加班申请', 'hr:overtime:apply:list', '/hr/overtime/apply', NULL, 1, 2, 0, 1, '2026-09-29 13:03:08', NULL, '2026-09-29 14:34:57', 0);
INSERT INTO `sys_menu` VALUES (244, 242, '加班记录', 'hr:overtime:record:list', '/hr/overtime/record', NULL, 2, 2, 0, 1, '2026-09-29 13:03:08', NULL, '2026-09-29 14:34:57', 0);
INSERT INTO `sys_menu` VALUES (245, 242, '加班补偿', 'hr:overtime:compensate:list', '/hr/overtime/compensate', NULL, 3, 2, 0, 1, '2026-09-29 13:03:08', NULL, '2026-09-29 14:34:57', 0);
INSERT INTO `sys_menu` VALUES (246, 242, '加班配置', 'hr:overtime:config:list', '/hr/overtime/config', NULL, 4, 2, 0, 1, '2026-09-29 13:03:08', NULL, '2026-09-29 14:34:57', 0);
INSERT INTO `sys_menu` VALUES (247, 242, '新增加班申请', 'hr:overtime:apply:add', NULL, NULL, 1, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (248, 242, '编辑加班申请', 'hr:overtime:apply:edit', NULL, NULL, 2, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (249, 242, '删除加班申请', 'hr:overtime:apply:delete', NULL, NULL, 3, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (250, 242, '撤回加班申请', 'hr:overtime:apply:revoke', NULL, NULL, 4, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (251, 242, '确认加班记录', 'hr:overtime:record:confirm', NULL, NULL, 1, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (252, 242, '驳回加班记录', 'hr:overtime:record:reject', NULL, NULL, 2, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (253, 242, '触发自动识别', 'hr:overtime:record:detect', NULL, NULL, 3, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (254, 242, '导出加班记录', 'hr:overtime:record:export', NULL, NULL, 4, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (255, 242, '核算加班补偿', 'hr:overtime:compensate:calculate', NULL, NULL, 1, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (256, 242, '发放加班费', 'hr:overtime:compensate:pay', NULL, NULL, 2, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (257, 242, '导出补偿明细', 'hr:overtime:compensate:export', NULL, NULL, 3, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (258, 242, '保存配置', 'hr:overtime:config:save', NULL, NULL, 1, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (259, 242, '启用/禁用', 'hr:overtime:config:status', NULL, NULL, 2, 3, 0, 1, '2026-09-29 13:03:08', NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
