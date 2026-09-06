-- ============================================================
-- 集团多业态一体化管控系统 安装脚本
-- 全量建表（整合 group_rent_db.sql 正式版 + 各版本升级修正）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < install.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `biz_fee_bill_detail`;
CREATE TABLE `biz_fee_bill_detail`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint(20) NOT NULL COMMENT '关联账单ID',
  `fee_item_id` bigint(20) NOT NULL COMMENT '收费项ID',
  `fee_item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费项名称快照（生成时固化，规则修改不回溯）',
  `calc_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '收费方式快照 1定额 2按面积',
  `price` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
  `base_value` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '计费基数（面积等）',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '该项金额小计',
  `detail_config_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '计费过程数据JSON（审计追溯，后端过滤脚本后入库）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_bill_id`(`bill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费账单明细快照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_fee_bill_detail
-- ----------------------------

-- ----------------------------
-- Table structure for biz_flow_seq
-- ----------------------------

DROP TABLE IF EXISTS `biz_flow_seq`;
CREATE TABLE `biz_flow_seq`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL COMMENT '所属子公司ID',
  `seq_date` date NOT NULL COMMENT '日期 yyyy-MM-dd',
  `seq_no` int(11) NOT NULL DEFAULT 1 COMMENT '当日已分配序号',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_date`(`company_id` ASC, `seq_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '财务流水单号顺序号计数器（公司+日维度，防并发重号）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_flow_seq
-- ----------------------------
INSERT INTO `biz_flow_seq` VALUES (13, 0, '2026-09-04', 3, '2026-09-04 22:21:14');
INSERT INTO `biz_flow_seq` VALUES (20, 0, '2026-09-05', 5, '2026-09-05 07:49:38');

-- ----------------------------
-- Table structure for biz_kingdee_push
-- ----------------------------

DROP TABLE IF EXISTS `biz_kingdee_push`;
CREATE TABLE `biz_kingdee_push`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `finance_flow_id` bigint(20) NOT NULL COMMENT '关联财务流水ID',
  `push_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '推送状态0待推送1成功2失败',
  `kingdee_voucher_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '金蝶凭证编号',
  `push_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '推送返回报文摘要',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '金蝶凭证推送记录表【预留，一期不执行业务写入】' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_kingdee_push
-- ----------------------------

-- ----------------------------
-- Table structure for finance_discount_apply
-- ----------------------------

DROP TABLE IF EXISTS `finance_discount_apply`;
CREATE TABLE `finance_discount_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请编号',
  `policy_id` bigint(20) NOT NULL COMMENT '优惠策略ID',
  `policy_snapshot` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '策略快照JSON（审批后计算依据，固化不回溯）',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源类型 contract',
  `source_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据ID（合同ID）',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同编号（冗余便于列表展示）',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '铺位ID',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户ID',
  `waive_months` int(11) NOT NULL DEFAULT 0 COMMENT '申请免租期月数',
  `discount_rate` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '申请折扣率%',
  `deduct_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '申请减免金额',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠总额（免租折算+折扣+减免）',
  `need_audit` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需审批 超集团阈值自动置1',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批实例ID',
  `apply_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '申请状态 0草稿 1审批中 2通过 3驳回 4作废',
  `apply_user_id` bigint(20) NULL DEFAULT NULL COMMENT '申请人用户ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审批完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_no`(`apply_no` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_flow_instance_id`(`flow_instance_id` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_discount_apply
-- ----------------------------
INSERT INTO `finance_discount_apply` VALUES (1, 0, 'DA-0-20260820-886681', 1, '{\"discountRate\":\"100.00\",\"deductAmount\":\"0.00\",\"waiveMonths\":1}', 'contract', '6', 'HT202608201628048145', 1, 1, 1, 100.00, 0.00, 2000.00, 0, NULL, 2, 1, '2026-08-20 16:28:04', '', 1, '2026-08-20 16:28:04', 1, '2026-08-20 16:28:04', 0);

-- ----------------------------
-- Table structure for finance_discount_policy
-- ----------------------------

DROP TABLE IF EXISTS `finance_discount_policy`;
CREATE TABLE `finance_discount_policy`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团模板',
  `policy_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略名称',
  `discount_type` tinyint(4) NOT NULL COMMENT '优惠类型 1免租期 2折扣率 3减免金额 4组合',
  `waive_months` int(11) NOT NULL DEFAULT 0 COMMENT '免租期月数（type=1/4）',
  `discount_rate` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '折扣率%（100=无折扣，type=2/4）',
  `deduct_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '减免金额（type=3/4）',
  `scope_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '适用范围 1按合同 2按铺位',
  `start_time` date NULL DEFAULT NULL COMMENT '策略生效时间',
  `end_time` date NULL DEFAULT NULL COMMENT '策略失效时间，NULL永久',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_policy_name_company`(`policy_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠策略表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_discount_policy
-- ----------------------------
INSERT INTO `finance_discount_policy` VALUES (1, 0, '开业免租1个月', 1, 1, 100.00, 0.00, 1, NULL, NULL, 1, '集团模板：开业免租1个月（免租期优惠）', 1, '2026-08-20 14:38:31', NULL, NULL, 0);

-- ----------------------------
-- Table structure for finance_fee_item
-- ----------------------------

DROP TABLE IF EXISTS `finance_fee_item`;
CREATE TABLE `finance_fee_item`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团模板',
  `fee_item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费类型名称（租金/物业费/水费/电费/押金/其他）',
  `category_type` tinyint(4) NOT NULL DEFAULT 6 COMMENT '收费类别 1租金 2物业费 3水费 4电费 5押金 6其他',
  `calc_unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计量单位（元/月、元/平米、元/吨等）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收费类型备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fee_item_name_company`(`fee_item_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费项定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_fee_item
-- ----------------------------
INSERT INTO `finance_fee_item` VALUES (1, 0, '租金', 1, '元/月', '集团模板：铺位租金收费', 1, '2026-08-19 12:24:43', NULL, '2026-08-19 13:53:24', 0);
INSERT INTO `finance_fee_item` VALUES (2, 0, '物业费', 2, '元/月', '集团模板：物业管理服务费', 1, '2026-08-19 12:24:43', NULL, '2026-08-19 13:53:24', 0);
INSERT INTO `finance_fee_item` VALUES (3, 0, '水费', 3, '元/吨', '集团模板：用水收费', 1, '2026-08-19 12:24:43', NULL, '2026-08-19 13:53:24', 0);
INSERT INTO `finance_fee_item` VALUES (4, 0, '电费', 4, '元/度', '集团模板：用电收费', 1, '2026-08-19 12:24:43', NULL, '2026-08-19 13:53:24', 0);
INSERT INTO `finance_fee_item` VALUES (5, 0, '押金', 5, '元', '集团模板：租赁押金', 1, '2026-08-19 12:24:43', NULL, '2026-08-19 13:53:24', 0);
INSERT INTO `finance_fee_item` VALUES (6, 0, '其他', 6, '元', '集团模板：一次性杂费等', 1, '2026-08-19 12:24:43', NULL, NULL, 0);

-- ----------------------------
-- Table structure for finance_fee_pay_bill
-- ----------------------------

DROP TABLE IF EXISTS `finance_fee_pay_bill`;
CREATE TABLE `finance_fee_pay_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'property_fee' COMMENT '业务类型 property_fee=物业费 water_elec=水电费 rent=租赁费 kindergarten=幼儿园费',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID（可空，账单可无商户绑定）',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单周期标识（月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性once）',
  `rule_id` bigint(20) NOT NULL COMMENT '生成账单的规则ID（biz_fee_rule，锁定后仅记录不回溯）',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID（可空）',
  `pay_bill_id` bigint(20) NULL DEFAULT NULL COMMENT '关联缴费单ID（聚合支付时写入）',
  `period_type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '账单周期 0不使用 1按年 2按月 3按日（与biz_fee_rule一致）',
  `original_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '应收原价合计',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免金额合计',
  `adjust_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `real_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '实际应收 = 原价 - 优惠 + 调账',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费完成时间',
  `locked_flag` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账单锁定 1锁定（生成即锁定，规则变更不回溯）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单备注',
  `source_bill_id` bigint(20) NULL DEFAULT NULL COMMENT '源记录单ID（water_elec_bill.id / property_fee_bill.id，用于幂等和追溯）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（缴费完成禁止删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE,
  INDEX `idx_company_biztype_status`(`company_id` ASC, `biz_type` ASC, `pay_status` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_source_bill_id`(`source_bill_id` ASC) USING BTREE,
  INDEX `idx_pay_bill_id`(`pay_bill_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_stall_rule_source`(`company_id` ASC, `stall_id` ASC, `rule_id` ASC, `source_bill_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一账单表（未支付订单，聚合支付载体，含各业务来源账单）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_fee_pay_bill
-- ----------------------------
INSERT INTO `finance_fee_pay_bill` VALUES (75, 0, 'water_elec', 1, NULL, '2026-09', 4, 476, 11, 2, 60.00, 0.00, 0.00, 60.00, 2, '2026-09-05 00:37:27', 1, NULL, 53, 1, '2026-09-04 22:20:52', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (76, 0, 'water_elec', 1, NULL, '2026-09', 4, 477, 12, 2, 90.00, 0.00, 0.00, 90.00, 2, '2026-09-05 07:49:22', 1, NULL, 48, 1, '2026-09-05 00:30:55', 1, '2026-09-05 07:49:22', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (77, 0, 'property_fee', 1, NULL, '2026-10', 5, 478, 12, 2, 25.00, 0.00, 0.00, 25.00, 2, '2026-09-05 07:49:22', 1, NULL, 45, 1, '2026-09-05 07:48:20', 1, '2026-09-05 07:49:22', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (78, 0, 'property_fee', 1, NULL, '2026-09', 5, 479, 12, 2, 25.00, 0.00, 0.00, 25.00, 2, '2026-09-05 07:49:22', 1, NULL, 44, 1, '2026-09-05 07:48:23', 1, '2026-09-05 07:49:22', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (79, 0, 'property_fee', 1, NULL, '2026-11', 5, NULL, 13, 2, 25.00, 0.00, 0.00, 25.00, 2, '2026-09-05 07:49:39', 1, NULL, 46, 1, '2026-09-05 07:48:48', 1, '2026-09-05 07:49:39', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (80, 0, 'property_fee', 1, NULL, '2026-12', 5, NULL, NULL, 2, 25.00, 0.00, 0.00, 25.00, 0, NULL, 1, NULL, 47, 1, '2026-09-05 07:48:59', 1, '2026-09-05 07:48:59', 0);
INSERT INTO `finance_fee_pay_bill` VALUES (81, 0, 'water_elec', 1, NULL, '2026-09', 4, 482, NULL, 2, 60.00, 0.00, 0.00, 60.00, 0, NULL, 1, NULL, 55, 1, '2026-09-05 09:41:44', 1, '2026-09-05 09:41:44', 0);

-- ----------------------------
-- Table structure for finance_fee_rule
-- ----------------------------

DROP TABLE IF EXISTS `finance_fee_rule`;
CREATE TABLE `finance_fee_rule`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团模板',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `fee_item_id` bigint(20) NOT NULL COMMENT '关联收费类型ID（biz_fee_item）',
  `calc_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '收费方式 1定额 2按面积',
  `price` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '单价（定额=固定金额/月；按面积=每平米单价）',
  `period_type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '收费周期 0不使用 1按年 2按月 3按日（水费/电费/押金等类型可配置0不使用周期）',
  `overdue_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '滞纳金百分比（逾期每日/每周期加收比例，0=不收滞纳金）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '规则状态 0停用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_name_company`(`rule_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_fee_item_id`(`fee_item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_fee_rule
-- ----------------------------
INSERT INTO `finance_fee_rule` VALUES (1, 0, '租金', 1, 1, 1000.00, 2, 0.50, 1, '', 1, '2026-08-19 13:00:08', 1, '2026-08-19 13:00:08', 0);
INSERT INTO `finance_fee_rule` VALUES (2, 0, '电费', 4, 1, 0.50, 0, 0.50, 1, '', 1, '2026-08-19 13:00:34', 1, '2026-08-19 13:13:38', 0);
INSERT INTO `finance_fee_rule` VALUES (3, 0, '租金1', 1, 1, 2000.00, 1, 0.50, 1, '', 1, '2026-08-19 13:01:30', 1, '2026-08-20 15:08:55', 0);
INSERT INTO `finance_fee_rule` VALUES (4, 0, '水费', 3, 1, 6.00, 0, 0.50, 1, '', 1, '2026-08-19 14:15:33', 1, '2026-08-19 14:15:33', 0);
INSERT INTO `finance_fee_rule` VALUES (5, 0, '物业费', 2, 2, 0.50, 2, 0.00, 1, '', 1, '2026-08-21 19:04:26', 1, '2026-08-21 19:04:26', 0);
INSERT INTO `finance_fee_rule` VALUES (6, 0, '押金', 5, 1, 1000.00, 0, 0.00, 1, '', 1, '2026-08-24 07:24:25', 1, '2026-08-24 07:24:25', 0);

-- ----------------------------
-- Table structure for finance_fee_rule_stall_rel
-- ----------------------------

DROP TABLE IF EXISTS `finance_fee_rule_stall_rel`;
CREATE TABLE `finance_fee_rule_stall_rel`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `rule_id` bigint(20) NOT NULL COMMENT '收费规则ID（biz_fee_rule）',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID（stall_info）',
  `override_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否特殊覆盖 0普通绑定 1单铺位覆盖（预留）',
  `override_price` decimal(12, 2) NULL DEFAULT NULL COMMENT '覆盖单价（预留，override_flag=1时生效）',
  `override_config_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '覆盖配置JSON（预留，后端过滤脚本后入库）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_stall`(`rule_id` ASC, `stall_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费规则-铺位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_fee_rule_stall_rel
-- ----------------------------
INSERT INTO `finance_fee_rule_stall_rel` VALUES (20, 0, 1, 2, 0, NULL, NULL, 1, '2026-08-24 07:24:46', 1, '2026-08-24 07:24:46', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (21, 0, 2, 2, 0, NULL, NULL, 1, '2026-08-24 07:24:46', 1, '2026-08-24 07:24:46', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (22, 0, 4, 2, 0, NULL, NULL, 1, '2026-08-24 07:24:46', 1, '2026-08-24 07:24:46', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (23, 0, 5, 2, 0, NULL, NULL, 1, '2026-08-24 07:24:46', 1, '2026-08-24 07:24:46', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (24, 0, 6, 2, 0, NULL, NULL, 1, '2026-08-24 07:24:46', 1, '2026-08-24 07:24:46', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (43, 0, 2, 1, 0, NULL, NULL, 1, '2026-08-31 17:57:07', 1, '2026-08-31 17:57:07', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (44, 0, 3, 1, 0, NULL, NULL, 1, '2026-08-31 17:57:07', 1, '2026-08-31 17:57:07', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (45, 0, 4, 1, 0, NULL, NULL, 1, '2026-08-31 17:57:07', 1, '2026-08-31 17:57:07', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (46, 0, 5, 1, 0, NULL, NULL, 1, '2026-08-31 17:57:07', 1, '2026-08-31 17:57:07', 0);
INSERT INTO `finance_fee_rule_stall_rel` VALUES (47, 0, 6, 1, 0, NULL, NULL, 1, '2026-08-31 17:57:07', 1, '2026-08-31 17:57:07', 0);

-- ----------------------------
-- Table structure for finance_pay_flow
-- ----------------------------

DROP TABLE IF EXISTS `finance_pay_flow`;
CREATE TABLE `finance_pay_flow`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 rent租金/water_elec水电/deposit押金/marketing营销抵扣',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收费规则名称（写入时固化）',
  `bill_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联业务单据ID',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID（biz_recv_pay_plan，核销时写入）',
  `pay_bill_id` bigint(20) NULL DEFAULT NULL COMMENT '关联缴费单ID（聚合支付时写入）',
  `pay_bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缴费单编号快照（写入时固化）',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '铺位ID',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位编号快照（写入时固化）',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位名称快照（写入时固化）',
  `stall_market_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属市场名称快照（写入时固化）',
  `category_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租赁分类名称快照（写入时固化）',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商户名称快照（写入时固化）',
  `payer_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缴费人姓名（写入时固化）',
  `payer_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缴费人手机号（写入时固化）',
  `payer_company_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缴费人公司名称（写入时固化）',
  `payer_type` tinyint(4) NULL DEFAULT NULL COMMENT '缴费人类型 1个人 2企业（写入时固化）',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联合同编号（写入时固化）',
  `contract_id` bigint(20) NULL DEFAULT NULL COMMENT '关联合同ID（写入时固化）',
  `original_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '应收原价金额',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠抵扣金额',
  `real_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '实际实收金额',
  `pay_type` tinyint(4) NULL DEFAULT NULL COMMENT '支付渠道1微信2支付宝3线下现金',
  `flow_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '流水类型1收入 2支出退费',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '流水状态1正常',
  `flow_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '冲红/作废状态 1正常 2冲红中 3已冲红 4已作废',
  `red_flush_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '冲红反向流水ID（已冲红时写入）',
  `void_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作废原因',
  `trade_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方支付交易号',
  `flow_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '财务流水单号（YO+公司编码+日期+流水序号）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流水备注说明',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '操作人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流水生成时间',
  `discount_apply_id` bigint(20) NULL DEFAULT NULL COMMENT '优惠申请单ID',
  `discount_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '优惠快照JSON（固化不回溯）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_time`(`company_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_bill_type`(`business_type` ASC, `bill_id` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_flow_no`(`flow_no` ASC) USING BTREE,
  INDEX `idx_contract_id`(`contract_id` ASC) USING BTREE,
  INDEX `idx_payer_name`(`payer_name` ASC) USING BTREE,
  INDEX `idx_flow_status`(`flow_status` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_pay_bill_id`(`pay_bill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '全域财务资金流水表（唯一资金台账）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_pay_flow
-- ----------------------------
INSERT INTO `finance_pay_flow` VALUES (51, 0, 'water_elec', NULL, '53', 470, 11, NULL, NULL, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', NULL, NULL, NULL, NULL, NULL, NULL, 60.00, 0.00, 60.00, 3, 1, 1, 6, NULL, NULL, NULL, 'YO0020260905000002', '统一缴费', 1, '2026-09-05 00:37:27', 1, '{\"applyId\":1,\"discountRate\":\"100.00\",\"policyId\":1,\"auditTime\":\"2026-08-20T16:28:04\",\"applyNo\":\"DA-0-20260820-886681\",\"deductAmount\":\"0.00\",\"waiveMonths\":1,\"discountAmount\":\"2000.00\",\"applyStatus\":2}');
INSERT INTO `finance_pay_flow` VALUES (52, 0, 'pay_bill', NULL, '76', 477, 12, 'PY0020260905000004', NULL, 1, '001', '铺位名称', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 140.00, 0.00, 140.00, 3, 1, 1, 1, NULL, NULL, NULL, 'PAY1788565757322-arxia3qg', NULL, 1, '2026-09-05 07:49:21', NULL, NULL);
INSERT INTO `finance_pay_flow` VALUES (53, 0, 'property_fee', NULL, '46', 480, 13, NULL, NULL, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', NULL, NULL, NULL, NULL, NULL, NULL, 25.00, 0.00, 25.00, 3, 1, 1, 6, NULL, NULL, NULL, 'YO0020260905000004', '统一缴费', 1, '2026-09-05 07:49:38', 1, '{\"applyId\":1,\"discountRate\":\"100.00\",\"policyId\":1,\"auditTime\":\"2026-08-20T16:28:04\",\"applyNo\":\"DA-0-20260820-886681\",\"deductAmount\":\"0.00\",\"waiveMonths\":1,\"discountAmount\":\"2000.00\",\"applyStatus\":2}');

-- ----------------------------
-- Table structure for finance_pay_order
-- ----------------------------

DROP TABLE IF EXISTS `finance_pay_order`;
CREATE TABLE `finance_pay_order`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `pay_bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '缴费单编号（PY+公司编码+日期+序号）',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型 contract=租赁合同 fee_bill=收费账单',
  `source_id` bigint(20) NOT NULL COMMENT '来源单据ID',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '缴费单总金额',
  `paid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额',
  `unpaid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '未缴金额',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1部分缴费 2已缴 3已退费 4已冲红 5已作废',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pay_bill_no`(`pay_bill_no` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '缴费单主表（聚合支付载体）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of finance_pay_order
-- ----------------------------
INSERT INTO `finance_pay_order` VALUES (11, 0, 'PY0020260905000003', 'fee_bill', 75, 1, 1, 60.00, 60.00, 0.00, 2, '2026-09-05 00:37:27', NULL, 1, '2026-09-05 00:37:27', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `finance_pay_order` VALUES (12, 0, 'PY0020260905000004', 'fee_bill', 76, 1, NULL, 140.00, 140.00, 0.00, 2, '2026-09-05 07:49:22', '', 1, '2026-09-05 07:49:22', 1, '2026-09-05 07:49:22', 0);
INSERT INTO `finance_pay_order` VALUES (13, 0, 'PY0020260905000005', 'fee_bill', 79, 1, 1, 25.00, 25.00, 0.00, 2, '2026-09-05 07:49:39', NULL, 1, '2026-09-05 07:49:39', 1, '2026-09-05 07:49:39', 0);

-- ----------------------------
-- Table structure for finance_pay_order_item
-- ----------------------------

DROP TABLE IF EXISTS `finance_pay_order_item`;
CREATE TABLE `finance_pay_order_item`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pay_bill_id` bigint(20) NOT NULL COMMENT '缴费单ID',
  `bill_id` bigint(20) NOT NULL COMMENT '关联账单ID（biz_fee_bill.id）',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 rent/deposit/property_fee/water_elec',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费规则名称快照',
  `fee_item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费项名称快照',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单周期标识',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '应收金额',
  `discount_amount` decimal(12, 2) NOT NULL COMMENT '优惠金额',
  `paid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额',
  `unpaid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '未缴金额',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pay_bill_id`(`pay_bill_id` ASC) USING BTREE,
  INDEX `idx_bill_id`(`bill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '缴费单明细表（快照固化）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of finance_pay_order_item
-- ----------------------------
INSERT INTO `finance_pay_order_item` VALUES (13, 11, 75, 'water_elec', '水费', '水费', '2026-09', 60.00, 0.00, 60.00, 0.00, 1, '2026-09-05 00:37:27');
INSERT INTO `finance_pay_order_item` VALUES (14, 12, 76, 'water_elec', '水费', '水费', '2026-09', 90.00, 0.00, 90.01, -0.01, 1, '2026-09-05 07:49:21');
INSERT INTO `finance_pay_order_item` VALUES (15, 12, 77, 'property_fee', '物业费', '物业费', '2026-10', 25.00, 0.00, 25.00, 0.00, 1, '2026-09-05 07:49:21');
INSERT INTO `finance_pay_order_item` VALUES (16, 12, 78, 'property_fee', '物业费', '物业费', '2026-09', 25.00, 0.00, 25.00, 0.00, 1, '2026-09-05 07:49:21');
INSERT INTO `finance_pay_order_item` VALUES (17, 13, 79, 'property_fee', '物业费', '物业费', '2026-11', 25.00, 0.00, 25.00, 0.00, 1, '2026-09-05 07:49:38');

-- ----------------------------
-- Table structure for finance_pay_plan_rel
-- ----------------------------

DROP TABLE IF EXISTS `finance_pay_plan_rel`;
CREATE TABLE `finance_pay_plan_rel`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单类型 water_elec/fee_bill',
  `bill_id` bigint(20) NOT NULL COMMENT '账单ID',
  `plan_id` bigint(20) NOT NULL COMMENT '应收应付计划ID',
  `split_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '该计划在本账单的分摊金额',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bill_plan`(`bill_type` ASC, `bill_id` ASC, `plan_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账单-应收应付计划关联表（多计划合并账单）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_pay_plan_rel
-- ----------------------------
INSERT INTO `finance_pay_plan_rel` VALUES (99, 0, 'water_elec', 48, 462, 90.00, 1, '2026-09-04 07:53:19', 1, '2026-09-04 07:53:19', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (100, 0, 'property_fee', 44, 463, 25.00, 1, '2026-09-04 07:53:34', 1, '2026-09-04 07:53:34', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (101, 0, 'water_elec', 66, 464, 90.00, 1, '2026-09-04 07:53:56', 1, '2026-09-04 07:53:56', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (110, 0, 'property_fee', 45, 469, 25.00, 1, '2026-09-04 14:55:33', 1, '2026-09-04 14:55:33', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (111, 0, 'water_elec', 53, 470, 60.00, 1, '2026-09-04 15:04:10', 1, '2026-09-04 15:04:10', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (112, 0, 'water_elec', 54, 471, 30.00, 1, '2026-09-04 17:50:06', 1, '2026-09-04 17:50:06', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (113, 0, 'water_elec', 55, 472, 60.00, 1, '2026-09-04 18:31:12', 1, '2026-09-04 18:31:12', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (114, 0, 'water_elec', 72, 473, 60.00, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (115, 0, 'water_elec', 73, 474, 30.00, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (116, 0, 'water_elec', 74, 475, 60.00, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (117, 0, 'water_elec', 75, 476, 60.00, 1, '2026-09-04 22:20:52', 1, '2026-09-04 22:20:52', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (118, 0, 'water_elec', 76, 477, 90.00, 1, '2026-09-05 00:30:55', 1, '2026-09-05 00:30:55', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (119, 0, 'property_fee', 77, 478, 25.00, 1, '2026-09-05 07:48:20', 1, '2026-09-05 07:48:20', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (120, 0, 'property_fee', 78, 479, 25.00, 1, '2026-09-05 07:48:23', 1, '2026-09-05 07:48:23', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (121, 0, 'property_fee', 46, 480, 25.00, 1, '2026-09-05 07:48:48', 1, '2026-09-05 07:48:48', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (122, 0, 'property_fee', 47, 481, 25.00, 1, '2026-09-05 07:48:59', 1, '2026-09-05 07:48:59', 0);
INSERT INTO `finance_pay_plan_rel` VALUES (123, 0, 'water_elec', 81, 482, 60.00, 1, '2026-09-05 09:41:44', 1, '2026-09-05 09:41:44', 0);

-- ----------------------------
-- Table structure for finance_recv_pay_plan
-- ----------------------------

DROP TABLE IF EXISTS `finance_recv_pay_plan`;
CREATE TABLE `finance_recv_pay_plan`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `plan_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划编号（AR-RENT-公司-日期-序号 / AP-REIMB-公司-日期-序号）',
  `direction` tinyint(4) NOT NULL DEFAULT 1 COMMENT '方向 1应收 2应付（二期报销采购）',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 rent租金 deposit押金 property物业费 fee_bill收费规则账单 reimburse报销 purchase采购',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据类型 contract/reimburse/purchase/bill',
  `source_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据ID（合同/报销单/采购单）',
  `market_id` bigint(20) NULL DEFAULT NULL COMMENT '市场ID',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '铺位ID',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `period_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '期次标识 月度yyyy-MM 季度yyyy-Qn 年度yyyy 一次性once',
  `period_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '周期类型 1按月 2按季 3按年 4一次性',
  `due_date` date NOT NULL COMMENT '应收/应付日期',
  `original_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '原应收金额（不含优惠）',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（来自优惠申请快照）',
  `adjust_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `plan_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '计划应收 = 原价 - 优惠 + 调账',
  `paid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '已收/已付金额（核销累加）',
  `unpaid_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '未收/未付金额 = plan_amount - paid_amount',
  `plan_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '计划状态 0待执行 1部分核销 2完成 3逾期 4作废 5终止',
  `red_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '红冲标记 0正常计划 1反向冲销计划（金额为负）',
  `orig_plan_id` bigint(20) NULL DEFAULT NULL COMMENT '溯源计划ID（红冲计划指向被冲销计划；单据变更重建新旧互指）',
  `overdue_days` int(11) NOT NULL DEFAULT 0 COMMENT '逾期天数（定时任务刷新）',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批实例ID（终止/作废/大额调账审批）',
  `discount_apply_id` bigint(20) NULL DEFAULT NULL COMMENT '关联优惠申请ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（单据变更/红冲原因留痕）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（已核销/红冲计划禁止物理删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plan_no_company`(`plan_no` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  UNIQUE INDEX `uk_source_period`(`company_id` ASC, `source_type` ASC, `source_id` ASC, `period_no` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_orig_plan_id`(`orig_plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 483 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应收应付计划主表（全系统唯一台账）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_recv_pay_plan
-- ----------------------------
INSERT INTO `finance_recv_pay_plan` VALUES (462, 0, 'AR-WATER_ELEC-0-20260904-102984', 1, 'water_elec', 'bill', '48', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 90.00, 0.00, 0.00, 90.00, 90.00, 0.00, 1, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 07:53:19', 1, '2026-09-04 22:21:00', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (463, 0, 'AR-PROPERTY-0-20260904-191141', 1, 'property', 'bill', '44', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 25.00, 0.00, 0.00, 25.00, 25.00, 0.00, 1, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 07:53:34', 1, '2026-09-04 22:21:14', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (464, 0, 'AR-FEE_BILL-0-20260904-384730', 1, 'fee_bill', 'bill', '66', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 90.00, 0.00, 0.00, 90.00, 0.00, 90.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 07:53:56', 1, '2026-09-04 07:53:56', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (469, 0, 'AR-PROPERTY-0-20260904-613251', 1, 'property', 'bill', '45', NULL, 1, 1, NULL, '2026-10', 1, '2026-10-01', 25.00, 0.00, 0.00, 25.00, 0.00, 25.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 14:55:33', 1, '2026-09-04 14:55:33', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (470, 0, 'AR-WATER_ELEC-0-20260904-602027', 1, 'water_elec', 'bill', '53', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 60.00, 0.00, 1, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 15:04:10', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (471, 0, 'AR-WATER_ELEC-0-20260904-515958', 1, 'water_elec', 'bill', '54', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 30.00, 0.00, 0.00, 30.00, 0.00, 30.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 17:50:06', 1, '2026-09-04 17:50:06', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (472, 0, 'AR-WATER_ELEC-0-20260904-265193', 1, 'water_elec', 'bill', '55', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 0.00, 60.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 18:31:12', 1, '2026-09-04 18:31:12', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (473, 0, 'AR-FEE_BILL-0-20260904-309681', 1, 'fee_bill', 'bill', '72', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 0.00, 60.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (474, 0, 'AR-FEE_BILL-0-20260904-180637', 1, 'fee_bill', 'bill', '73', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 30.00, 0.00, 0.00, 30.00, 0.00, 30.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (475, 0, 'AR-FEE_BILL-0-20260904-926482', 1, 'fee_bill', 'bill', '74', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 0.00, 60.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 22:14:46', 1, '2026-09-04 22:14:46', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (476, 0, 'AR-FEE_BILL-0-20260904-577957', 1, 'fee_bill', 'bill', '75', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 0.00, 60.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-04 22:20:52', 1, '2026-09-04 22:20:52', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (477, 0, 'AR-FEE_BILL-0-20260905-979772', 1, 'fee_bill', 'bill', '76', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 90.00, 0.00, 0.00, 90.00, 140.00, 0.00, 1, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 00:30:55', 1, '2026-09-05 07:49:21', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (478, 0, 'AR-FEE_BILL-0-20260905-003732', 1, 'fee_bill', 'bill', '77', NULL, 1, 1, NULL, '2026-10', 1, '2026-10-01', 25.00, 0.00, 0.00, 25.00, 0.00, 25.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 07:48:20', 1, '2026-09-05 07:48:20', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (479, 0, 'AR-FEE_BILL-0-20260905-745569', 1, 'fee_bill', 'bill', '78', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 25.00, 0.00, 0.00, 25.00, 0.00, 25.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 07:48:23', 1, '2026-09-05 07:48:23', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (480, 0, 'AR-PROPERTY-0-20260905-664322', 1, 'property', 'bill', '46', NULL, 1, 1, NULL, '2026-11', 1, '2026-11-01', 25.00, 0.00, 0.00, 25.00, 25.00, 0.00, 1, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 07:48:48', 1, '2026-09-05 07:49:38', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (481, 0, 'AR-PROPERTY-0-20260905-319866', 1, 'property', 'bill', '47', NULL, 1, 1, NULL, '2026-12', 1, '2026-12-01', 25.00, 0.00, 0.00, 25.00, 0.00, 25.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 07:48:59', 1, '2026-09-05 07:48:59', 0);
INSERT INTO `finance_recv_pay_plan` VALUES (482, 0, 'AR-FEE_BILL-0-20260905-221542', 1, 'fee_bill', 'bill', '81', NULL, 1, 1, NULL, '2026-09', 1, '2026-09-01', 60.00, 0.00, 0.00, 60.00, 0.00, 60.00, 0, 0, NULL, 0, NULL, NULL, NULL, 1, '2026-09-05 09:41:44', 1, '2026-09-05 09:41:44', 0);

-- ----------------------------
-- Table structure for finance_writeoff
-- ----------------------------

DROP TABLE IF EXISTS `finance_writeoff`;
CREATE TABLE `finance_writeoff`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `finance_flow_id` bigint(20) NOT NULL COMMENT '资金主流水ID（biz_finance_flow）',
  `plan_id` bigint(20) NOT NULL COMMENT '应收应付计划ID（biz_recv_pay_plan）',
  `bill_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联账单类型（water_elec/fee_bill，可空=直接核销计划）',
  `bill_id` bigint(20) NULL DEFAULT NULL COMMENT '关联账单ID（可空）',
  `writeoff_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '本次分摊核销金额（正=核销入账，负=退款/红冲冲减）',
  `writeoff_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '核销类型 1缴费核销 2退款冲减 3红冲冲销',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '操作人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_flow_id`(`finance_flow_id` ASC) USING BTREE,
  INDEX `idx_bill`(`bill_type` ASC, `bill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '财务核销记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of finance_writeoff
-- ----------------------------
INSERT INTO `finance_writeoff` VALUES (10, 0, 44, 462, 'water_elec', 48, 90.00, 1, '水电费缴费核销', 1, '2026-09-04 22:21:00', 1, '2026-09-04 22:21:00', 0);
INSERT INTO `finance_writeoff` VALUES (11, 0, 45, 463, 'property', 44, 25.00, 1, '物业费缴费核销', 1, '2026-09-04 22:21:15', 1, '2026-09-04 22:21:15', 0);
INSERT INTO `finance_writeoff` VALUES (12, 0, 51, 470, 'water_elec', 53, 60.00, 1, '水电费缴费核销', 1, '2026-09-05 00:37:27', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `finance_writeoff` VALUES (13, 0, 52, 477, 'fee_bill', 76, 140.00, 1, '缴费单聚合缴费核销', 1, '2026-09-05 07:49:22', 1, '2026-09-05 07:49:22', 0);
INSERT INTO `finance_writeoff` VALUES (14, 0, 53, 480, 'property', 46, 25.00, 1, '物业费缴费核销', 1, '2026-09-05 07:49:39', 1, '2026-09-05 07:49:39', 0);

-- ----------------------------
-- Table structure for flow_definition
-- ----------------------------

DROP TABLE IF EXISTS `flow_definition`;
CREATE TABLE `flow_definition`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团全局模板',
  `def_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程名称（如：租赁合同审批/租赁合同大额优惠审批）',
  `def_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程编码（唯一）：contract/contract_discount/contract_terminate/plan_adjust/reimburse/reimburse_large/purchase/purchase_large',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用业务类型（对齐计划biz_type/单据类型）',
  `node_config_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '节点配置JSON（后端FlowConfigUtil过滤脚本后入库）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（大额流程定义标注适用金额阈值）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（有实例禁止删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_def_code`(`def_code` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一审批流程定义表（集团全局模板）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flow_definition
-- ----------------------------
INSERT INTO `flow_definition` VALUES (1, 0, '租赁合同审批', 'contract', 'contract', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"}]', 1, '租赁合同单据自身审批：新建/变更，通过后进入优惠阈值判定', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (2, 0, '租赁合同大额优惠审批', 'contract_discount', 'contract_discount', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"},{\"nodeName\":\"财务复核\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"finance_admin\"}]', 1, '优惠申请超集团阈值自动发起，通过后合同优惠生效', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (3, 0, '合同作废终止审批', 'contract_terminate', 'contract_terminate', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"},{\"nodeName\":\"集团财务复核\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"group_finance\"}]', 1, '退租/作废高危操作，通过后执行红冲链', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (4, 0, '计划大额调账审批', 'plan_adjust', 'plan_adjust', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"}]', 1, '单笔调账绝对值超 plan.adjust_amount_limit 必须审批', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (5, 0, '报销审批', 'reimburse', 'reimburse', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"}]', 0, '二期启用：报销单自身审批，通过后生成应付计划(direction=2)', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (6, 0, '大额报销审批', 'reimburse_large', 'reimburse', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"},{\"nodeName\":\"财务复核\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"finance_admin\"}]', 0, '二期启用：报销金额超 reimburse.amount_limit 自动选用', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (7, 0, '采购审批', 'purchase', 'purchase', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"}]', 0, '二期启用：采购单自身审批，通过后生成应付计划(direction=2)', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (8, 0, '大额采购审批', 'purchase_large', 'purchase', '[{\"nodeName\":\"子公司经理审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"sub_manager\"},{\"nodeName\":\"财务复核\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"finance_admin\"}]', 0, '二期启用：采购金额超 purchase.amount_limit 自动选用', 1, '2026-08-20 14:36:07', NULL, NULL, 0);
INSERT INTO `flow_definition` VALUES (9, 0, '财务流水冲红审批', 'finance_red_flush', 'finance_red_flush', '[{\"nodeName\":\"冲红审批\",\"nodeMode\":\"single\",\"handlerType\":\"role\",\"handlerValue\":\"super_admin\"}]', 1, '财务流水冲红申请审批流程', 0, '2026-08-27 17:01:57', NULL, '2026-08-27 17:03:16', 0);

-- ----------------------------
-- Table structure for flow_instance
-- ----------------------------

DROP TABLE IF EXISTS `flow_instance`;
CREATE TABLE `flow_instance`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID（业务单据归属公司）',
  `instance_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程实例编号',
  `def_id` bigint(20) NOT NULL COMMENT '流程定义ID',
  `def_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程名称快照',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型（contract/contract_discount等）',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据类型（contract/reimburse/purchase/plan）',
  `source_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据ID',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批标题（单据摘要）',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人用户ID',
  `apply_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人姓名',
  `instance_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '实例状态 0审批中 1通过 2驳回 3撤回 4终止',
  `current_node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前节点名称',
  `current_handlers` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前节点审批人ID集合JSON',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_instance_no`(`instance_no` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_def_id`(`def_id` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_apply_user_id`(`apply_user_id` ASC, `instance_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一审批流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flow_instance
-- ----------------------------

-- ----------------------------
-- Table structure for flow_record
-- ----------------------------

DROP TABLE IF EXISTS `flow_record`;
CREATE TABLE `flow_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `instance_id` bigint(20) NOT NULL COMMENT '流程实例ID',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作 submit提交 pass通过 reject驳回 revoke撤回 transfer转交 urge催办 cc抄送 terminate终止',
  `handler_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人用户ID',
  `handler_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作说明/审批意见',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '操作人（自动填充）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（自动填充）',
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一审批流程流转记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flow_record
-- ----------------------------
INSERT INTO `flow_record` VALUES (1, 0, 5, '冲红审批', 'submit', 1, '集团超级管理员', '冲红申请-流水ID:13', 1, '2026-08-27 17:14:34', 1, '2026-08-27 17:14:34', 0);
INSERT INTO `flow_record` VALUES (2, 0, 6, '冲红审批', 'submit', 1, '集团超级管理员', '冲红申请-流水ID:11', 1, '2026-08-27 17:16:51', 1, '2026-08-27 17:16:51', 0);
INSERT INTO `flow_record` VALUES (3, 0, 5, '冲红审批', 'pass', 1, '集团超级管理员', '', 1, '2026-08-27 17:17:08', 1, '2026-08-27 17:17:08', 0);
INSERT INTO `flow_record` VALUES (12, 0, 12, '冲红审批', 'submit', 1, '集团超级管理员', '冲红申请-流水ID:11', 1, '2026-08-27 17:55:59', 1, '2026-08-27 17:55:59', 0);
INSERT INTO `flow_record` VALUES (13, 0, 12, '冲红审批', 'pass', 1, '集团超级管理员', 'approved', 1, '2026-08-27 17:56:30', 1, '2026-08-27 17:56:30', 0);
INSERT INTO `flow_record` VALUES (14, 0, 13, '冲红审批', 'submit', 1, '集团超级管理员', '冲红申请-流水ID:13', 1, '2026-08-27 20:05:01', 1, '2026-08-27 20:05:01', 0);
INSERT INTO `flow_record` VALUES (15, 0, 13, '冲红审批', 'pass', 1, '集团超级管理员', '', 1, '2026-08-27 20:05:31', 1, '2026-08-27 20:05:31', 0);

-- ----------------------------
-- Table structure for flow_task
-- ----------------------------

DROP TABLE IF EXISTS `flow_task`;
CREATE TABLE `flow_task`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `instance_id` bigint(20) NOT NULL COMMENT '流程实例ID',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_order` int(11) NOT NULL COMMENT '节点顺序号（从1开始）',
  `handler_id` bigint(20) NOT NULL COMMENT '审批人用户ID（会签一个节点多条任务）',
  `handler_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `task_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '任务状态 0待办 1已办 2已转交 3流程终止作废',
  `approve_result` tinyint(4) NULL DEFAULT NULL COMMENT '审批结果 0驳回 1通过（仅已办节点）',
  `opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `parent_task_id` bigint(20) NULL DEFAULT NULL COMMENT '转交来源任务ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_handler_status`(`handler_id` ASC, `task_status` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一审批任务表（待办/已办）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flow_task
-- ----------------------------
INSERT INTO `flow_task` VALUES (4, 0, 5, '冲红审批', 1, 1, '集团超级管理员', 1, 1, '', '2026-08-27 17:17:08', NULL, 1, '2026-08-27 17:14:34', 1, '2026-08-27 17:14:34', 0);
INSERT INTO `flow_task` VALUES (11, 0, 12, '冲红审批', 1, 1, '集团超级管理员', 1, 1, 'approved', '2026-08-27 17:56:30', NULL, 1, '2026-08-27 17:55:59', 1, '2026-08-27 17:55:59', 0);
INSERT INTO `flow_task` VALUES (12, 0, 13, '冲红审批', 1, 1, '集团超级管理员', 1, 1, '', '2026-08-27 20:05:31', NULL, 1, '2026-08-27 20:05:01', 1, '2026-08-27 20:05:01', 0);

-- ----------------------------
-- Table structure for hr_attendance_record
-- ----------------------------

DROP TABLE IF EXISTS `hr_attendance_record`;
CREATE TABLE `hr_attendance_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `attendance_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考勤月份 yyyy-MM',
  `attendance_day` date NOT NULL COMMENT '考勤日期',
  `clock_in_time` datetime NULL DEFAULT NULL COMMENT '上班打卡时间',
  `clock_out_time` datetime NULL DEFAULT NULL COMMENT '下班打卡时间',
  `clock_type` tinyint(4) NULL DEFAULT NULL COMMENT '打卡状态 1正常 2迟到 3早退 4缺卡',
  `late_minutes` int(11) NULL DEFAULT 0 COMMENT '迟到分钟数',
  `early_minutes` int(11) NULL DEFAULT 0 COMMENT '早退分钟数',
  `absent` int(11) NULL DEFAULT 0 COMMENT '旷工天数',
  `leave_days` decimal(4, 1) NULL DEFAULT 0.0 COMMENT '请假天数',
  `work_days` int(11) NULL DEFAULT 0 COMMENT '应出勤天数',
  `actual_days` int(11) NULL DEFAULT 0 COMMENT '实际出勤天数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_emp_day`(`employee_id` ASC, `attendance_day` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_attendance_month`(`attendance_month` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'HR考勤记录（按月聚合快照）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_attendance_record
-- ----------------------------
INSERT INTO `hr_attendance_record` VALUES (1, 0, 1, 'admin', '2026-08', '2026-08-25', '2026-08-25 09:25:46', NULL, 2, 25, 0, 0, 0.0, 1, 1, NULL, 1, 1, '2026-08-26 07:30:27', '2026-08-26 07:30:27', 0);

-- ----------------------------
-- Table structure for hr_employee
-- ----------------------------

DROP TABLE IF EXISTS `hr_employee`;
CREATE TABLE `hr_employee`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团总部',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '关联 sys_user.id，离职后可置NULL',
  `employee_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工工号（公司内唯一）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `id_card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（AES密文）',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（AES密文）',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别 1男 2女',
  `birthdate` date NULL DEFAULT NULL COMMENT '出生日期',
  `entry_date` date NULL DEFAULT NULL COMMENT '入职日期',
  `regular_date` date NULL DEFAULT NULL COMMENT '转正日期',
  `resign_date` date NULL DEFAULT NULL COMMENT '离职日期',
  `employment_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '用工类型 1正式 2试用期 3劳务派遣 4临时工',
  `employee_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0待入职 1在职 2试用期 3离职 4终止合同',
  `org_id` bigint(20) NULL DEFAULT NULL COMMENT '所属组织ID（sys_org）',
  `post_id` bigint(20) NULL DEFAULT NULL COMMENT '岗位ID（hr_post）',
  `post_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位职级快照（冗余避免关联查询）',
  `org_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称快照（冗余避免关联查询）',
  `supervisor_id` bigint(20) NULL DEFAULT NULL COMMENT '直属上级用户ID（sys_user.id，用于审批链）',
  `bank_account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工资卡号（密文）',
  `social_security_base` decimal(12, 2) NULL DEFAULT NULL COMMENT '社保公积金缴纳基数',
  `basic_salary` decimal(12, 2) NULL DEFAULT NULL COMMENT '基本工资快照（用于薪资核算）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_no_company`(`employee_no` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_employee_status`(`employee_status` ASC, `company_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工主档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_employee
-- ----------------------------
INSERT INTO `hr_employee` VALUES (1, 0, NULL, '00001', '员工名', NULL, '13112312312', '123@qqw.com', 1, NULL, '2026-08-26', NULL, NULL, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 5000.00, '备注', 1, '2026-08-26 07:29:54', 1, '2026-08-26 07:29:54', 0);

-- ----------------------------
-- Table structure for hr_employee_file
-- ----------------------------

DROP TABLE IF EXISTS `hr_employee_file`;
CREATE TABLE `hr_employee_file`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `file_type` tinyint(4) NOT NULL COMMENT '附件类型 1身份证 2学历 3劳动合同 4其他',
  `file_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OSS存储地址',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_employee_file
-- ----------------------------

-- ----------------------------
-- Table structure for hr_entry_apply
-- ----------------------------

DROP TABLE IF EXISTS `hr_entry_apply`;
CREATE TABLE `hr_entry_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工工号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `id_card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（密文）',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（密文）',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别 1男 2女',
  `birthdate` date NULL DEFAULT NULL COMMENT '出生日期',
  `entry_date` date NOT NULL COMMENT '入职日期',
  `employment_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '用工类型',
  `org_id` bigint(20) NULL DEFAULT NULL COMMENT '所属组织ID',
  `post_id` bigint(20) NULL DEFAULT NULL COMMENT '岗位ID',
  `basic_salary` decimal(12, 2) NULL DEFAULT NULL COMMENT '基本工资',
  `bank_account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工资卡号',
  `auto_create_user` tinyint(4) NULL DEFAULT 1 COMMENT '是否自动创建系统账号 0否 1是',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1审批中 2已通过 3已驳回 4已撤回',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_no`(`employee_no` ASC) USING BTREE,
  INDEX `idx_flow_instance`(`flow_instance_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入职申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_entry_apply
-- ----------------------------

-- ----------------------------
-- Table structure for hr_leave_record
-- ----------------------------

DROP TABLE IF EXISTS `hr_leave_record`;
CREATE TABLE `hr_leave_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `leave_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT 'HR考勤类型 1计入出勤 2不计入出勤',
  `leave_apply_id` bigint(20) NOT NULL COMMENT '关联 oa_leave_apply.id',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `leave_days` decimal(4, 1) NOT NULL COMMENT '请假天数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_leave_apply_id`(`leave_apply_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'HR请假记录（同步自OA请假申请）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_leave_record
-- ----------------------------

-- ----------------------------
-- Table structure for hr_post
-- ----------------------------

DROP TABLE IF EXISTS `hr_post`;
CREATE TABLE `hr_post`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团通用',
  `post_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `post_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位编码',
  `post_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位职级',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '所属部门ID（sys_org）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_code_company`(`post_code` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_post
-- ----------------------------
INSERT INTO `hr_post` VALUES (1, 0, '测试岗位', '0001', 'P0', NULL, 1, '', 1, '2026-08-26 07:29:04', 1, '2026-08-26 07:29:04', 0);

-- ----------------------------
-- Table structure for hr_regular_apply
-- ----------------------------

DROP TABLE IF EXISTS `hr_regular_apply`;
CREATE TABLE `hr_regular_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `regular_date` date NOT NULL COMMENT '转正日期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1审批中 2已通过 3已驳回 4已撤回',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '转正申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_regular_apply
-- ----------------------------

-- ----------------------------
-- Table structure for hr_resign_apply
-- ----------------------------

DROP TABLE IF EXISTS `hr_resign_apply`;
CREATE TABLE `hr_resign_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `resign_date` date NOT NULL COMMENT '离职日期',
  `resign_type` tinyint(4) NULL DEFAULT NULL COMMENT '离职类型 1主动辞职 2合同到期 3辞退 4终止合同',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离职原因',
  `handover_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作交接说明',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1审批中 2已通过 3已驳回 4已撤回',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_flow_instance`(`flow_instance_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '离职申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_resign_apply
-- ----------------------------

-- ----------------------------
-- Table structure for hr_salary_archive
-- ----------------------------

DROP TABLE IF EXISTS `hr_salary_archive`;
CREATE TABLE `hr_salary_archive`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `basic_salary` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '基本工资',
  `performance_salary` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '绩效工资',
  `position_allowance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '岗位津贴',
  `other_allowance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '其他补贴',
  `social_security_personal` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '社保个人扣款',
  `housing_fund_personal` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '公积金个人扣款',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee`(`employee_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工薪资档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_salary_archive
-- ----------------------------

-- ----------------------------
-- Table structure for hr_salary_month
-- ----------------------------

DROP TABLE IF EXISTS `hr_salary_month`;
CREATE TABLE `hr_salary_month`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL,
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `salary_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '核算月份 yyyy-MM',
  `basic_salary` decimal(12, 2) NOT NULL COMMENT '基本工资快照',
  `performance_salary` decimal(12, 2) NOT NULL COMMENT '绩效工资快照',
  `allowance_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '补贴合计快照',
  `social_security` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '社保扣款快照',
  `housing_fund` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '公积金扣款快照',
  `tax_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '个税快照',
  `deduction_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '其他扣款快照',
  `gross_amount` decimal(12, 2) NOT NULL COMMENT '应发合计',
  `net_amount` decimal(12, 2) NOT NULL COMMENT '实发合计',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发放状态 0未发放 1已发放 2发放失败',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '实际发放时间',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '大额调薪审批实例ID',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联 biz_recv_pay_plan.id（应付计划）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_month`(`employee_id` ASC, `salary_month` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_salary_month`(`salary_month` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '月度薪资核算单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_salary_month
-- ----------------------------

-- ----------------------------
-- Table structure for hr_social_security
-- ----------------------------

DROP TABLE IF EXISTS `hr_social_security`;
CREATE TABLE `hr_social_security`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL,
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `social_security_base` decimal(12, 2) NOT NULL COMMENT '社保缴纳基数',
  `housing_fund_base` decimal(12, 2) NOT NULL COMMENT '公积金缴纳基数',
  `social_security_company` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '公司承担社保金额',
  `social_security_personal` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '个人承担社保金额',
  `housing_fund_company` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '公司承担公积金金额',
  `housing_fund_personal` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '个人承担公积金金额',
  `start_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参保起始月份',
  `end_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参保截止月份（NULL=未停保）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0停保 1参保',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee`(`employee_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社保公积金台账' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_social_security
-- ----------------------------

-- ----------------------------
-- Table structure for hr_transfer_apply
-- ----------------------------

DROP TABLE IF EXISTS `hr_transfer_apply`;
CREATE TABLE `hr_transfer_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0,
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名快照',
  `old_org_id` bigint(20) NULL DEFAULT NULL COMMENT '原组织ID',
  `old_post_id` bigint(20) NULL DEFAULT NULL COMMENT '原岗位ID',
  `new_org_id` bigint(20) NOT NULL COMMENT '新组织ID',
  `new_post_id` bigint(20) NOT NULL COMMENT '新岗位ID',
  `new_post_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新岗位职级快照',
  `transfer_date` date NOT NULL COMMENT '调岗日期',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调岗原因',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1审批中 2已通过 3已驳回 4已撤回',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调岗申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_transfer_apply
-- ----------------------------

-- ----------------------------
-- Table structure for map_stall_point
-- ----------------------------

DROP TABLE IF EXISTS `map_stall_point`;
CREATE TABLE `map_stall_point`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `map_id` bigint(20) NOT NULL COMMENT '关联地图ID',
  `market_id` bigint(20) NOT NULL COMMENT '所属市场ID',
  `stall_id` bigint(20) NOT NULL COMMENT '关联铺位ID',
  `point_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '单个铺位点位JSON数据',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_stall`(`map_id` ASC, `stall_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '铺位点位明细表（点位超过500条启用）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of map_stall_point
-- ----------------------------

-- ----------------------------
-- Table structure for market_map
-- ----------------------------

DROP TABLE IF EXISTS `market_map`;
CREATE TABLE `market_map`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `market_id` bigint(20) NOT NULL COMMENT '关联市场ID',
  `map_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平面图名称',
  `img_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '底图图片OSS访问地址',
  `point_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Fabric画布全点位序列化JSON',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0停用1启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_market_name_company`(`company_id` ASC, `map_name` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_mark`(`company_id` ASC, `market_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '市场平面图主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of market_map
-- ----------------------------

-- ----------------------------
-- Table structure for material_category
-- ----------------------------

DROP TABLE IF EXISTS `material_category`;
CREATE TABLE `material_category`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团分类模板',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物资分类名称',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of material_category
-- ----------------------------

-- ----------------------------
-- Table structure for material_goods
-- ----------------------------

DROP TABLE IF EXISTS `material_goods`;
CREATE TABLE `material_goods`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `category_id` bigint(20) NOT NULL COMMENT '物资分类ID',
  `goods_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物资名称',
  `spec` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计量单位',
  `safe_stock` int(11) NOT NULL DEFAULT 0 COMMENT '安全库存预警数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资物料档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of material_goods
-- ----------------------------

-- ----------------------------
-- Table structure for material_warehouse
-- ----------------------------

DROP TABLE IF EXISTS `material_warehouse`;
CREATE TABLE `material_warehouse`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `warehouse_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `warehouse_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `manager_user_id` bigint(20) NULL DEFAULT NULL COMMENT '仓库管理员用户ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0停用1启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资仓库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of material_warehouse
-- ----------------------------

-- ----------------------------
-- Table structure for oa_announcement
-- ----------------------------

DROP TABLE IF EXISTS `oa_announcement`;
CREATE TABLE `oa_announcement`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请单号',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告正文',
  `publish_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '发布范围 1全员 2指定部门 3指定分公司',
  `target_dept_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标部门ID集合JSON',
  `target_user_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标人员ID集合JSON',
  `publish_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0草稿 1已发布 2已撤回',
  `priority` tinyint(4) NOT NULL DEFAULT 0 COMMENT '优先级 0普通 1重要 2紧急',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人用户ID',
  `publisher_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '发布人姓名快照',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已撤回',
  `read_count` int(11) NOT NULL DEFAULT 0 COMMENT '已读人数',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_announcement
-- ----------------------------
INSERT INTO `oa_announcement` VALUES (1, NULL, 0, '测试公告', '测试内容', 1, NULL, NULL, 4, 0, 1, '管理员', NULL, '2026-08-26 07:48:48', NULL, 0, 0, 1, '2026-08-26 07:44:13', 1, '2026-08-26 07:44:13', 0);
INSERT INTO `oa_announcement` VALUES (2, NULL, 0, '测试公告', '测试内容', 1, NULL, NULL, 0, 0, 1, '管理员', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 07:45:30', 1, '2026-08-26 07:45:30', 0);
INSERT INTO `oa_announcement` VALUES (3, NULL, 0, '测试公告2', '测试内容2', 1, NULL, NULL, 0, 0, 1, '管理员', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 07:48:48', 1, '2026-08-26 07:48:48', 0);
INSERT INTO `oa_announcement` VALUES (4, NULL, 0, '测试', '内容', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 07:53:53', 1, '2026-08-26 07:53:53', 0);
INSERT INTO `oa_announcement` VALUES (5, NULL, 0, '测试3', '内容3', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 07:57:43', 1, '2026-08-26 07:57:43', 0);
INSERT INTO `oa_announcement` VALUES (6, NULL, 1, '测试公告', '测试内容', 1, NULL, NULL, 1, 0, 1, 'admin', NULL, '2026-08-26 08:01:10', NULL, 0, 0, 1, '2026-08-26 08:00:52', 1, '2026-08-26 08:00:52', 0);
INSERT INTO `oa_announcement` VALUES (7, NULL, 0, '测试公告', '测试内容', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:02:14', 1, '2026-08-26 08:02:14', 0);
INSERT INTO `oa_announcement` VALUES (8, NULL, 0, '代理测试', '测试内容', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:03:31', 1, '2026-08-26 08:03:31', 0);
INSERT INTO `oa_announcement` VALUES (9, NULL, 0, '权限测试', '测试', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:06:05', 1, '2026-08-26 08:06:05', 0);
INSERT INTO `oa_announcement` VALUES (10, NULL, 0, '调试测试公告', '这是调试用的公告内容', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:07:13', 1, '2026-08-26 08:07:13', 0);
INSERT INTO `oa_announcement` VALUES (11, NULL, 0, '测试插入', '测试', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 0, '2026-08-26 08:09:00', NULL, NULL, 0);
INSERT INTO `oa_announcement` VALUES (12, NULL, 0, '测试零发布人', '测试内容', 1, NULL, NULL, 0, 0, 0, '', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:09:24', 1, '2026-08-26 08:09:24', 0);
INSERT INTO `oa_announcement` VALUES (13, NULL, 0, '代理测试', '测试内容', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:09:35', 1, '2026-08-26 08:09:35', 0);
INSERT INTO `oa_announcement` VALUES (14, NULL, 0, '测试公告修复验证', '这是一条测试公告，验证发布公告功能是否正常', 1, NULL, NULL, 1, 0, 1, 'admin', NULL, '2026-08-26 08:51:38', NULL, 0, 0, 1, '2026-08-26 08:46:45', 1, '2026-08-26 08:46:45', 0);
INSERT INTO `oa_announcement` VALUES (15, NULL, 0, '最终验证公告', '验证发布公告功能', 1, NULL, NULL, 4, 0, 1, 'admin', NULL, '2026-08-26 08:54:35', NULL, 0, 0, 1, '2026-08-26 08:50:51', 1, '2026-08-26 08:50:51', 0);
INSERT INTO `oa_announcement` VALUES (16, NULL, 0, '修复验证公告', '所有接口测试通过', 1, NULL, NULL, 0, 0, 1, 'admin', NULL, NULL, NULL, 0, 0, 1, '2026-08-26 08:52:09', 1, '2026-08-26 08:52:09', 0);

-- ----------------------------
-- Table structure for oa_announcement_read
-- ----------------------------

DROP TABLE IF EXISTS `oa_announcement_read`;
CREATE TABLE `oa_announcement_read`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `announcement_id` bigint(20) NOT NULL COMMENT '公告ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_au_user`(`announcement_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告已读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_announcement_read
-- ----------------------------

-- ----------------------------
-- Table structure for oa_clock_record
-- ----------------------------

DROP TABLE IF EXISTS `oa_clock_record`;
CREATE TABLE `oa_clock_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户姓名快照',
  `clock_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '打卡类型 1上班 2下班',
  `clock_time` datetime NOT NULL COMMENT '打卡时间',
  `location_lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `location_lng` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `location_addr` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡地址',
  `is_early` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否早退 0否 1是',
  `is_late` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否迟到 0否 1是',
  `is_absent` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否缺卡 0否 1是',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `device_type` tinyint(4) NULL DEFAULT NULL COMMENT '设备类型 1移动端 2PC端',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_clock_time`(`clock_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '打卡记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_clock_record
-- ----------------------------
INSERT INTO `oa_clock_record` VALUES (1, 0, 1, 'admin', 1, '2026-08-25 09:25:46', NULL, NULL, '测试打卡', 0, 0, 0, NULL, 1, '2026-08-25 09:25:46', 0, 1, '2026-08-25 09:25:46', NULL);
INSERT INTO `oa_clock_record` VALUES (2, 0, 1, 'admin', 1, '2026-08-27 16:27:17', NULL, NULL, '', 0, 0, 0, NULL, 1, '2026-08-27 16:27:17', 0, 1, '2026-08-27 16:27:17', NULL);
INSERT INTO `oa_clock_record` VALUES (3, 0, 1, 'admin', 2, '2026-08-27 16:27:20', NULL, NULL, '', 0, 0, 0, NULL, 1, '2026-08-27 16:27:20', 0, 1, '2026-08-27 16:27:20', NULL);

-- ----------------------------
-- Table structure for oa_leave_apply
-- ----------------------------

DROP TABLE IF EXISTS `oa_leave_apply`;
CREATE TABLE `oa_leave_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人用户ID',
  `applicant_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '申请人姓名快照',
  `leave_type` tinyint(4) NOT NULL COMMENT '请假类型 1事假 2病假 3年假 4调休 5产假 6其他',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '请假天数',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请假事由',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1审批中 2已通过 3已驳回 4已撤回',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_leave_apply
-- ----------------------------

-- ----------------------------
-- Table structure for oa_meeting_booking
-- ----------------------------

DROP TABLE IF EXISTS `oa_meeting_booking`;
CREATE TABLE `oa_meeting_booking`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `room_id` bigint(20) NOT NULL COMMENT '会议室ID',
  `booker_id` bigint(20) NOT NULL COMMENT '预约人用户ID',
  `booker_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '预约人姓名快照',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会议主题',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `attendees` int(11) NOT NULL DEFAULT 0 COMMENT '参会人数',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '会议说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0待确认 1已确认 2已取消 3已完成',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会议室预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting_booking
-- ----------------------------

-- ----------------------------
-- Table structure for oa_meeting_room
-- ----------------------------

DROP TABLE IF EXISTS `oa_meeting_room`;
CREATE TABLE `oa_meeting_room`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会议室名称',
  `building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在楼栋',
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在楼层',
  `capacity` int(11) NOT NULL DEFAULT 0 COMMENT '容纳人数',
  `has_projector` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否有投影仪 0否 1是',
  `has_video_conf` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否有视频会议设备',
  `has_phone` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否有电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '会议室描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会议室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting_room
-- ----------------------------

-- ----------------------------
-- Table structure for oa_work_report
-- ----------------------------

DROP TABLE IF EXISTS `oa_work_report`;
CREATE TABLE `oa_work_report`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户姓名快照',
  `report_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '报告类型 1日报 2周报 3月报',
  `report_date` date NOT NULL COMMENT '报告日期（日报=当天，周报=周一，月报=当月1号）',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '报告内容',
  `supervisor_id` bigint(20) NULL DEFAULT NULL COMMENT '直属上级ID（用于审批）',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1已提交 2已通过 3已驳回',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '上级批注意见',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批流程实例ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_report_date`(`report_date` ASC) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作汇报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_work_report
-- ----------------------------

-- ----------------------------
-- Table structure for property_fee_bill
-- ----------------------------

DROP TABLE IF EXISTS `property_fee_bill`;
CREATE TABLE `property_fee_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `merchant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  `contract_id` bigint(20) NULL DEFAULT NULL COMMENT '关联合同ID',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份 yyyy-MM',
  `rule_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '计费规则ID（biz_fee_rule，快照）',
  `fee_item_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '收费类型ID（biz_fee_item，固定=物业费）',
  `calc_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '收费方式 1定额 2按面积',
  `period_type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '收费周期 0不使用 1按年 2按月 3按日',
  `usage` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '用量：定额=0，按面积=铺位面积（快照）',
  `unit_price` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '计费单价快照（规则修改不回溯）',
  `period_factor` decimal(10, 4) NOT NULL DEFAULT 1.0000 COMMENT '周期系数（按年/12、按日×当月天数、按月=1）',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '本条账单金额',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费完成时间',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stall_month`(`company_id` ASC, `stall_id` ASC, `bill_month` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_bill_month`(`bill_month` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物业费月度记录单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_fee_bill
-- ----------------------------
INSERT INTO `property_fee_bill` VALUES (44, 0, 1, 1, 23, '2026-09', 5, 2, 2, 2, 50.00, 0.50, 1.0000, 25.00, 0, '2026-09-04 22:21:14', 463, 1, '2026-09-04 07:53:34', 1, '2026-09-05 00:38:34', 0);
INSERT INTO `property_fee_bill` VALUES (45, 0, 1, 1, 23, '2026-10', 5, 2, 2, 2, 50.00, 0.50, 1.0000, 25.00, 0, NULL, 469, 1, '2026-09-04 14:55:33', 1, '2026-09-04 18:40:52', 0);
INSERT INTO `property_fee_bill` VALUES (46, 0, 1, 1, 23, '2026-11', 5, 2, 2, 2, 50.00, 0.50, 1.0000, 25.00, 2, '2026-09-05 07:49:39', 480, 1, '2026-09-05 07:48:48', 1, '2026-09-05 07:49:39', 0);
INSERT INTO `property_fee_bill` VALUES (47, 0, 1, 1, 23, '2026-12', 5, 2, 2, 2, 50.00, 0.50, 1.0000, 25.00, 0, NULL, 481, 1, '2026-09-05 07:48:59', 1, '2026-09-05 07:48:59', 0);

-- ----------------------------
-- Table structure for property_market
-- ----------------------------

DROP TABLE IF EXISTS `property_market`;
CREATE TABLE `property_market`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团模板',
  `market_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '市场名称',
  `market_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市场地址',
  `contact_person` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市场联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0停用1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_market_name_company`(`market_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '市场信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_market
-- ----------------------------
INSERT INTO `property_market` VALUES (1, 0, '默认市场', '集团默认市场模板', NULL, NULL, 1, '集团模板：与一期铺位 market_id=1 对齐', 1, '2026-08-18 18:17:04', NULL, NULL, 0);

-- ----------------------------
-- Table structure for property_stall_category
-- ----------------------------

DROP TABLE IF EXISTS `property_stall_category`;
CREATE TABLE `property_stall_category`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团模板',
  `category_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称（商铺/仓库/车位等）',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0停用1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_name_company`(`category_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '铺位分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_stall_category
-- ----------------------------
INSERT INTO `property_stall_category` VALUES (1, 0, '商铺', 1, 1, '集团模板：商铺类租赁标的', 1, '2026-08-18 16:38:27', 1, '2026-08-19 13:25:43', 0);
INSERT INTO `property_stall_category` VALUES (2, 0, '仓库', 2, 1, '集团模板：仓库类租赁标的', 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `property_stall_category` VALUES (3, 0, '车位', 3, 1, '集团模板：车位类租赁标的', 1, '2026-08-18 16:38:27', NULL, NULL, 0);

-- ----------------------------
-- Table structure for property_stall_contract
-- ----------------------------

DROP TABLE IF EXISTS `property_stall_contract`;
CREATE TABLE `property_stall_contract`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID（v2.0起合同改用租户体系，可空）',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户ID（关联stall_tenant）',
  `rent_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '月租金金额',
  `deposit_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '押金金额',
  `start_time` date NOT NULL COMMENT '租赁开始日期',
  `end_time` date NOT NULL COMMENT '租赁到期日期',
  `contract_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '合同状态 1生效 2已退租 3已到期',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '关联审批实例ID（终止/作废/大额调账审批）',
  `attachment_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同附件OSS地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no_company`(`contract_no` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '铺位合同表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_stall_contract
-- ----------------------------
INSERT INTO `property_stall_contract` VALUES (2, 0, 'HT202608201536005513', NULL, 1, 1, 2000.00, 0.00, '2026-08-20', '2026-08-25', 2, NULL, NULL, '备注', 1, '2026-08-20 15:36:01', 1, '2026-08-24 09:42:57', 0);
INSERT INTO `property_stall_contract` VALUES (3, 0, 'HT202608201620384964', NULL, 1, 1, 2000.00, 0.00, '2026-08-20', '2026-10-16', 2, NULL, NULL, '', 1, '2026-08-20 16:20:39', 1, '2026-08-20 16:26:43', 0);
INSERT INTO `property_stall_contract` VALUES (6, 0, 'HT202608201628048145', NULL, 1, 1, 2000.00, 0.00, '2026-08-20', '2027-08-20', 2, NULL, NULL, '', 1, '2026-08-20 16:28:04', 1, '2026-08-20 21:15:45', 0);
INSERT INTO `property_stall_contract` VALUES (7, 0, 'HT202608211624544996', NULL, 1, 1, 2000.00, 0.00, '2026-08-21', '2027-08-21', 2, NULL, NULL, '', 1, '2026-08-21 16:24:55', 1, '2026-08-21 19:01:33', 0);
INSERT INTO `property_stall_contract` VALUES (8, 0, 'HT202608211902253543', NULL, 1, 1, 2000.00, 0.00, '2026-08-21', '2027-08-21', 2, NULL, NULL, '', 1, '2026-08-21 19:02:26', 1, '2026-08-21 19:02:32', 0);
INSERT INTO `property_stall_contract` VALUES (9, 0, 'HT202608211905266097', NULL, 1, 1, 2000.00, 0.00, '2026-08-21', '2027-08-21', 2, NULL, NULL, '', 1, '2026-08-21 19:05:26', 1, '2026-08-31 09:28:02', 0);
INSERT INTO `property_stall_contract` VALUES (10, 0, 'HT202608240725173862', NULL, 2, 1, 1000.00, 1000.00, '2026-08-24', '2027-08-24', 2, NULL, NULL, '', 1, '2026-08-24 07:25:17', 1, '2026-08-31 09:28:00', 0);
INSERT INTO `property_stall_contract` VALUES (23, 0, 'HT202609010757577682', NULL, 1, 1, 2000.00, 1000.00, '2026-09-01', '2027-09-01', 1, NULL, NULL, '', 1, '2026-09-01 07:57:57', 1, '2026-09-03 09:53:08', 0);

-- ----------------------------
-- Table structure for property_stall_info
-- ----------------------------

DROP TABLE IF EXISTS `property_stall_info`;
CREATE TABLE `property_stall_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `market_id` bigint(20) NOT NULL COMMENT '关联市场ID',
  `stall_category_id` bigint(20) NULL DEFAULT NULL COMMENT '租赁分类ID（关联stall_category）',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '铺位编号',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位名称',
  `stall_area` decimal(10, 2) NULL DEFAULT NULL COMMENT '铺位面积(平方米)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '铺位状态 0空置 1已租 2欠费 3即将到期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stall_no_company`(`stall_number` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_market_id`(`market_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '铺位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_stall_info
-- ----------------------------
INSERT INTO `property_stall_info` VALUES (1, 0, 1, 3, '001', '铺位名称', 50.00, 1, '测试', 1, '2026-08-18 18:31:52', 1, '2026-09-01 07:57:57', 0);
INSERT INTO `property_stall_info` VALUES (2, 0, 1, 1, '002', '商铺', 80.00, 0, '', 1, '2026-08-23 18:08:41', 1, '2026-08-31 09:28:00', 0);

-- ----------------------------
-- Table structure for property_stall_tenant
-- ----------------------------

DROP TABLE IF EXISTS `property_stall_tenant`;
CREATE TABLE `property_stall_tenant`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `tenant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称',
  `tenant_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '租户类型 1个体工商户 2企业 3个人',
  `contact_person` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话（密文存储，VO脱敏）',
  `id_card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号码（AES密文）',
  `social_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `bank_account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行账号（密文，VO仅后四位）',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系地址',
  `mini_openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信小程序openid',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0停用1正常',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_name_company`(`tenant_name` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_stall_tenant
-- ----------------------------
INSERT INTO `property_stall_tenant` VALUES (1, 0, '测试优惠租户', 3, '优惠', '13112312312', '', '', '', '', NULL, 1, '', 1, '2026-08-20 14:41:32', 1, '2026-08-20 14:41:32', 0);

-- ----------------------------
-- Table structure for property_water_elec_bill
-- ----------------------------

DROP TABLE IF EXISTS `property_water_elec_bill`;
CREATE TABLE `property_water_elec_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `merchant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  `contract_id` bigint(20) NULL DEFAULT NULL COMMENT '关联合同ID',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份 yyyy-MM',
  `category` tinyint(4) NOT NULL DEFAULT 3 COMMENT '收费类别 2物业费 3水费 4电费（复用 finance_fee_item.category_type）',
  `prev_meter_read` decimal(12, 2) NULL DEFAULT NULL COMMENT '上期抄表读数（用于计算用量）',
  `usage` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '用量（吨/度/月，按类别含义不同）',
  `unit_price` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '计费单价快照（元/吨或元/度或元/月，规则修改不回溯）',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '本条账单金额（用量×单价）',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '缴费状态0待缴1已缴2部分缴费',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费完成时间',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_bill_month`(`bill_month` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '水电物业月度记录单表（按类别拆行）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_water_elec_bill
-- ----------------------------
INSERT INTO `property_water_elec_bill` VALUES (48, 0, 1, 1, 23, 462, '2026-09', 3, 0.00, 15.00, 6.00, 90.00, 0, '2026-09-04 22:21:00', 1, '2026-09-04 07:53:19', 1, '2026-09-05 00:30:41', 0);
INSERT INTO `property_water_elec_bill` VALUES (53, 0, 1, 1, 23, 470, '2026-09', 3, 15.00, 10.00, 6.00, 60.00, 2, '2026-09-05 00:37:27', 1, '2026-09-04 15:04:10', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `property_water_elec_bill` VALUES (54, 0, 1, 1, 23, 471, '2026-09', 3, 40.00, 5.00, 6.00, 30.00, 0, NULL, 1, '2026-09-04 17:50:06', 1, '2026-09-04 18:30:51', 0);
INSERT INTO `property_water_elec_bill` VALUES (55, 0, 1, 1, 23, 472, '2026-09', 3, 45.00, 10.00, 6.00, 60.00, 0, NULL, 1, '2026-09-04 18:31:12', 1, '2026-09-04 18:31:11', 0);

-- ----------------------------
-- Table structure for property_water_elec_meter
-- ----------------------------

DROP TABLE IF EXISTS `property_water_elec_meter`;
CREATE TABLE `property_water_elec_meter`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '绑定铺位ID',
  `meter_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '智能表设备编号',
  `meter_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '表类型1水表 2电表',
  `gateway_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物联网网关编码',
  `current_read` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '当前读数',
  `balance_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '设备状态 0断电 1通电正常',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_meter_no_company`(`meter_no` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '水电表计设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_water_elec_meter
-- ----------------------------
INSERT INTO `property_water_elec_meter` VALUES (1, 0, 1, '0000a1', 1, '', 55.00, 0.00, 1, 1, '2026-08-19 14:05:29', 1, '2026-09-04 18:31:12', 0);

-- ----------------------------
-- Table structure for property_water_elec_pay_record
-- ----------------------------

DROP TABLE IF EXISTS `property_water_elec_pay_record`;
CREATE TABLE `property_water_elec_pay_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint(20) NOT NULL COMMENT '关联账单ID',
  `stall_id` bigint(20) NOT NULL COMMENT '铺位ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位编号快照（写入时固化）',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铺位名称快照（写入时固化）',
  `stall_market_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属市场名称快照（写入时固化）',
  `category_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租赁分类名称快照（写入时固化）',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商户名称快照（写入时固化）',
  `pay_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '缴费/退费金额',
  `pay_type` tinyint(4) NOT NULL DEFAULT 3 COMMENT '支付渠道 1微信 2支付宝 3线下现金',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '幂等请求ID（唯一键防重复提交）',
  `record_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '记录类型 1缴费 2退费',
  `refund_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '退费状态 0未退 1已退（仅缴费记录使用）',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退费完成时间',
  `refund_record_id` bigint(20) NULL DEFAULT NULL COMMENT '退费记录ID（record_type=2时指向原缴费记录）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `flow_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联财务流水单号',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '操作人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_request`(`company_id` ASC, `request_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_bill_id`(`bill_id` ASC) USING BTREE,
  INDEX `idx_flow_no`(`flow_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '水电缴费记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_water_elec_pay_record
-- ----------------------------
INSERT INTO `property_water_elec_pay_record` VALUES (21, 0, 48, 1, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', 90.00, 3, 'PAY1788531656032-54wo9zaq', 1, 0, NULL, NULL, '', 'YO0020260904000002', 1, '2026-09-04 22:21:00', 1, '2026-09-04 22:21:00', 0);
INSERT INTO `property_water_elec_pay_record` VALUES (22, 0, 44, 1, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', 25.00, 3, 'PAY1788531672342-cy3vfp0u', 1, 0, NULL, NULL, '', 'YO0020260904000003', 1, '2026-09-04 22:21:14', 1, '2026-09-04 22:21:15', 0);
INSERT INTO `property_water_elec_pay_record` VALUES (28, 0, 53, 1, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', 60.00, 3, 'PAY1788539845846-vhh91gw9', 1, 0, NULL, NULL, '', 'YO0020260905000002', 1, '2026-09-05 00:37:27', 1, '2026-09-05 00:37:27', 0);
INSERT INTO `property_water_elec_pay_record` VALUES (29, 0, 46, 1, 1, '001', '铺位名称', '默认市场', '车位', '测试优惠租户', 25.00, 3, 'PAY1788565777181-vnstgfis', 1, 0, NULL, NULL, '', 'YO0020260905000004', 1, '2026-09-05 07:49:39', 1, '2026-09-05 07:49:39', 0);

-- ----------------------------
-- Table structure for stall_merchant
-- ----------------------------

DROP TABLE IF EXISTS `stall_merchant`;
CREATE TABLE `stall_merchant`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户名称',
  `contact_person` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话（密文）',
  `id_card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号码（AES密文）',
  `social_credit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `bank_account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对公银行账号（密文）',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商户联系地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '商户状态0停用1正常',
  `mini_openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信小程序openid',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stall_merchant
-- ----------------------------

-- ----------------------------
-- Table structure for sys_audit_log
-- ----------------------------

DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '操作人所属子公司ID',
  `oper_user_id` bigint(20) NOT NULL COMMENT '操作人用户ID',
  `oper_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人姓名',
  `oper_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作客户端IP地址',
  `oper_module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块 market_map/finance/org等',
  `oper_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：新增/编辑/删除/导出/审核',
  `biz_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务单据ID',
  `before_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作前数据快照JSON',
  `after_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作后数据快照JSON',
  `audit_oper_id` bigint(20) NULL DEFAULT NULL COMMENT '二级复核审核人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_create_time`(`company_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_oper_module`(`oper_module` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 856 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业务审计日志表【永久不可删除】' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_audit_log
-- ----------------------------
INSERT INTO `sys_audit_log` VALUES (1, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:05:43');
INSERT INTO `sys_audit_log` VALUES (2, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:05:52');
INSERT INTO `sys_audit_log` VALUES (3, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:14:05');
INSERT INTO `sys_audit_log` VALUES (4, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:16:19');
INSERT INTO `sys_audit_log` VALUES (5, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:16:26');
INSERT INTO `sys_audit_log` VALUES (6, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:16:34');
INSERT INTO `sys_audit_log` VALUES (7, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '保存主题', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:33.8607858\",\"updateBy\":1,\"updateTime\":\"2026-08-18T13:16:33.8607858\",\"isDelete\":null,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"top\",\"cardRadius\":8,\"darkMode\":0}', NULL, '2026-08-18 13:16:34');
INSERT INTO `sys_audit_log` VALUES (8, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '审核', '1', NULL, '{\"id\":1,\"companyId\":0,\"targetUserId\":1,\"applyUserId\":1,\"auditUserId\":1,\"permissionList\":\"[\\\"user:delete\\\"]\",\"applyReason\":\"演示数据：申请开通用户删除权限，请复核\",\"auditStatus\":1,\"auditComment\":\"????\",\"applyTime\":\"2026-08-18T12:51:36\",\"auditTime\":\"2026-08-18T13:16:33.9376458\"}', NULL, '2026-08-18 13:16:34');
INSERT INTO `sys_audit_log` VALUES (9, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34.0115768\",\"updateBy\":1,\"updateTime\":\"2026-08-18T13:16:34.0115768\",\"isDelete\":null,\"dictCode\":\"test_type\",\"dictName\":\"????\",\"status\":1,\"remark\":\"????\"}', NULL, '2026-08-18 13:16:34');
INSERT INTO `sys_audit_log` VALUES (10, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 13:18:22');
INSERT INTO `sys_audit_log` VALUES (11, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '导出', 'audit_20260818131822.csv', NULL, NULL, NULL, '2026-08-18 13:18:22');
INSERT INTO `sys_audit_log` VALUES (12, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 14:10:24');
INSERT INTO `sys_audit_log` VALUES (13, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 14:10:30');
INSERT INTO `sys_audit_log` VALUES (14, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 14:10:55');
INSERT INTO `sys_audit_log` VALUES (15, 0, 1, '集团超级管理员', '127.0.0.1', 'base', '退出', '1', NULL, NULL, NULL, '2026-08-18 14:20:31');
INSERT INTO `sys_audit_log` VALUES (16, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 14:20:34');
INSERT INTO `sys_audit_log` VALUES (17, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 17:01:53');
INSERT INTO `sys_audit_log` VALUES (18, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_category', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T16:38:27\",\"updateBy\":null,\"updateTime\":null,\"isDelete\":0,\"companyId\":0,\"categoryName\":\"商铺\",\"sortOrder\":1,\"status\":1,\"remark\":\"集团模板：商铺类租赁标的\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-18T17:02:25.9993965\",\"isDelete\":null,\"companyId\":null,\"categoryName\":\"商铺1\",\"sortOrder\":1,\"status\":1,\"remark\":\"集团模板：商铺类租赁标的\"}', NULL, '2026-08-18 17:02:26');
INSERT INTO `sys_audit_log` VALUES (19, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_category', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T16:38:27\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:02:26\",\"isDelete\":0,\"companyId\":0,\"categoryName\":\"商铺1\",\"sortOrder\":1,\"status\":1,\"remark\":\"集团模板：商铺类租赁标的\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-18T17:02:37.5106131\",\"isDelete\":null,\"companyId\":null,\"categoryName\":\"商铺\",\"sortOrder\":1,\"status\":1,\"remark\":\"集团模板：商铺类租赁标的\"}', NULL, '2026-08-18 17:02:38');
INSERT INTO `sys_audit_log` VALUES (20, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '导出', 'finance_flow_20260818170339.csv', NULL, NULL, NULL, '2026-08-18 17:03:40');
INSERT INTO `sys_audit_log` VALUES (21, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '3', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":null,\"updateTime\":null,\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.property_price\",\"configValue\":\"50.00\",\"configName\":\"???????/??/??\",\"remark\":\"?????????????????????????????\"}', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:06.8942708\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.property_price\",\"configValue\":\"50.00\",\"configName\":\"111\",\"remark\":\"?????????????????????????????\"}', NULL, '2026-08-18 17:09:07');
INSERT INTO `sys_audit_log` VALUES (22, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '3', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:07\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.property_price\",\"configValue\":\"50.00\",\"configName\":\"111\",\"remark\":\"?????????????????????????????\"}', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:07\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.property_price\",\"configValue\":\"50.00\",\"configName\":\"物业费单价（元/铺位/月）\",\"remark\":\"水电物业账单生成计费参数，集团统一配置，一期按铺位固定费用\"}', NULL, '2026-08-18 17:09:25');
INSERT INTO `sys_audit_log` VALUES (23, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":null,\"updateTime\":null,\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.elec_price\",\"configValue\":\"1.20\",\"configName\":\"??????/??\",\"remark\":\"???????????????????\"}', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:39.4782107\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.elec_price\",\"configValue\":\"1.20\",\"configName\":\"电费单价（元/度）\",\"remark\":\"水电物业账单生成计费参数，集团统一配置\"}', NULL, '2026-08-18 17:09:39');
INSERT INTO `sys_audit_log` VALUES (24, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":null,\"updateTime\":null,\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.water_price\",\"configValue\":\"4.50\",\"configName\":\"??????/??\",\"remark\":\"???????????????????\"}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:55.2961922\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.water_price\",\"configValue\":\"4.50\",\"configName\":\"水费单价（元/吨）\",\"remark\":\"水电物业账单生成计费参数，集团统一配置\"}', NULL, '2026-08-18 17:09:55');
INSERT INTO `sys_audit_log` VALUES (25, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-18 18:28:48');
INSERT INTO `sys_audit_log` VALUES (26, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:51.904362\",\"updateBy\":1,\"updateTime\":\"2026-08-18T18:31:51.904362\",\"isDelete\":null,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"stallType\":2,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-18 18:31:52');
INSERT INTO `sys_audit_log` VALUES (27, 0, 1, '集团超级管理员', '127.0.0.1', 'org', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T19:13:38.9456685\",\"updateBy\":1,\"updateTime\":\"2026-08-18T19:13:38.9456685\",\"isDelete\":null,\"companyId\":0,\"parentId\":0,\"orgName\":\"集团总经理室\",\"orgType\":1,\"sortOrder\":0,\"status\":1}', NULL, '2026-08-18 19:13:39');
INSERT INTO `sys_audit_log` VALUES (28, 0, 1, '集团超级管理员', '127.0.0.1', 'org', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T19:13:47.2881162\",\"updateBy\":1,\"updateTime\":\"2026-08-18T19:13:47.2881162\",\"isDelete\":null,\"companyId\":0,\"parentId\":0,\"orgName\":\"集团综合管理部\",\"orgType\":1,\"sortOrder\":0,\"status\":1}', NULL, '2026-08-18 19:13:47');
INSERT INTO `sys_audit_log` VALUES (29, 0, 1, '集团超级管理员', '127.0.0.1', 'org', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-18T19:13:53.7619489\",\"updateBy\":1,\"updateTime\":\"2026-08-18T19:13:53.7619489\",\"isDelete\":null,\"companyId\":0,\"parentId\":0,\"orgName\":\"集团财务中心\",\"orgType\":1,\"sortOrder\":0,\"status\":1}', NULL, '2026-08-18 19:13:54');
INSERT INTO `sys_audit_log` VALUES (30, 0, 1, '集团超级管理员', '127.0.0.1', 'org', '新增', '4', NULL, '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-18T19:14:04.8862384\",\"updateBy\":1,\"updateTime\":\"2026-08-18T19:14:04.8862384\",\"isDelete\":null,\"companyId\":0,\"parentId\":0,\"orgName\":\"集团人力行政中心\",\"orgType\":1,\"sortOrder\":0,\"status\":1}', NULL, '2026-08-18 19:14:05');
INSERT INTO `sys_audit_log` VALUES (31, 0, 1, '集团超级管理员', '127.0.0.1', 'org', '新增', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-18T19:14:21.2595746\",\"updateBy\":1,\"updateTime\":\"2026-08-18T19:14:21.2595746\",\"isDelete\":null,\"companyId\":0,\"parentId\":0,\"orgName\":\"汽车城经营分公司\",\"orgType\":1,\"sortOrder\":0,\"status\":1}', NULL, '2026-08-18 19:14:21');
INSERT INTO `sys_audit_log` VALUES (32, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 10:35:39');
INSERT INTO `sys_audit_log` VALUES (33, 0, 1, '集团超级管理员', '127.0.0.1', 'base', '退出', '1', NULL, NULL, NULL, '2026-08-19 11:24:47');
INSERT INTO `sys_audit_log` VALUES (34, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 11:24:50');
INSERT INTO `sys_audit_log` VALUES (35, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 11:35:28');
INSERT INTO `sys_audit_log` VALUES (36, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '保存主题', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":0}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":1}', NULL, '2026-08-19 11:38:00');
INSERT INTO `sys_audit_log` VALUES (37, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '保存主题', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":0}', NULL, '2026-08-19 11:38:05');
INSERT INTO `sys_audit_log` VALUES (38, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '保存主题', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":0}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":1}', NULL, '2026-08-19 11:38:08');
INSERT INTO `sys_audit_log` VALUES (39, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '保存主题', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T13:16:34\",\"updateBy\":1,\"updateTime\":\"2026-08-18T14:14:38\",\"isDelete\":0,\"companyId\":0,\"primaryColor\":\"#409EFF\",\"layoutMode\":\"side\",\"cardRadius\":8,\"darkMode\":0}', NULL, '2026-08-19 11:38:14');
INSERT INTO `sys_audit_log` VALUES (40, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T11:48:23.5565096\",\"updateBy\":1,\"updateTime\":\"2026-08-19T11:48:23.5565096\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"meterNo\":\"00001\",\"meterType\":2,\"gatewayCode\":\"\",\"currentRead\":0,\"balanceAmount\":0,\"status\":1}', NULL, '2026-08-19 11:48:24');
INSERT INTO `sys_audit_log` VALUES (41, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '抄表', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T11:48:24\",\"updateBy\":1,\"updateTime\":\"2026-08-19T11:48:24\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"00001\",\"meterType\":2,\"gatewayCode\":\"\",\"currentRead\":0.00,\"balanceAmount\":0.00,\"status\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T11:48:24\",\"updateBy\":1,\"updateTime\":\"2026-08-19T11:48:24\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"00001\",\"meterType\":2,\"gatewayCode\":\"\",\"currentRead\":10,\"balanceAmount\":0.00,\"status\":1}', NULL, '2026-08-19 11:48:50');
INSERT INTO `sys_audit_log` VALUES (42, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '抄表', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T11:48:24\",\"updateBy\":1,\"updateTime\":\"2026-08-19T11:48:24\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"00001\",\"meterType\":2,\"gatewayCode\":\"\",\"currentRead\":10.00,\"balanceAmount\":0.00,\"status\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T11:48:24\",\"updateBy\":1,\"updateTime\":\"2026-08-19T11:48:24\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"00001\",\"meterType\":2,\"gatewayCode\":\"\",\"currentRead\":20,\"balanceAmount\":0.00,\"status\":1}', NULL, '2026-08-19 11:49:01');
INSERT INTO `sys_audit_log` VALUES (43, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:04:48');
INSERT INTO `sys_audit_log` VALUES (44, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:08:08');
INSERT INTO `sys_audit_log` VALUES (45, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:11:17');
INSERT INTO `sys_audit_log` VALUES (46, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:11:23');
INSERT INTO `sys_audit_log` VALUES (47, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:12:26');
INSERT INTO `sys_audit_log` VALUES (48, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T12:12:25.8149548\",\"updateBy\":1,\"updateTime\":\"2026-08-19T12:12:25.8149548\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-08\",\"waterRead\":null,\"elecRead\":25,\"waterUsage\":0,\"elecUsage\":25,\"waterAmount\":0.00,\"elecAmount\":30.00,\"propertyAmount\":50.00,\"totalAmount\":80.00,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 12:12:26');
INSERT INTO `sys_audit_log` VALUES (49, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T12:13:38.5139122\",\"updateBy\":1,\"updateTime\":\"2026-08-19T12:13:38.5139122\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-07\",\"waterRead\":null,\"elecRead\":29,\"waterUsage\":0,\"elecUsage\":29,\"waterAmount\":0.00,\"elecAmount\":34.80,\"propertyAmount\":50.00,\"totalAmount\":84.80,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 12:13:39');
INSERT INTO `sys_audit_log` VALUES (50, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:39\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.elec_price\",\"configValue\":\"1.20\",\"configName\":\"电费单价（元/度）\",\"remark\":\"水电物业账单生成计费参数，集团统一配置\"}', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-18T15:53:31\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:09:39\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"water_elec.elec_price\",\"configValue\":\"1.90\",\"configName\":\"电费单价（元/度）\",\"remark\":\"水电物业账单生成计费参数，集团统一配置\"}', NULL, '2026-08-19 12:14:54');
INSERT INTO `sys_audit_log` VALUES (51, 0, 1, '集团超级管理员', '127.0.0.1', 'base', '退出', '1', NULL, NULL, NULL, '2026-08-19 12:30:41');
INSERT INTO `sys_audit_log` VALUES (52, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:30:44');
INSERT INTO `sys_audit_log` VALUES (53, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:34:41');
INSERT INTO `sys_audit_log` VALUES (54, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_category', '删除', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T16:38:27\",\"updateBy\":1,\"updateTime\":\"2026-08-18T17:02:38\",\"isDelete\":0,\"companyId\":0,\"categoryName\":\"商铺\",\"sortOrder\":1,\"status\":1,\"remark\":\"集团模板：商铺类租赁标的\"}', NULL, NULL, '2026-08-19 12:34:41');
INSERT INTO `sys_audit_log` VALUES (55, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:44:46');
INSERT INTO `sys_audit_log` VALUES (56, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 12:59:27');
INSERT INTO `sys_audit_log` VALUES (57, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T13:00:08.1579033\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:00:08.1579033\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"租金\",\"feeItemId\":1,\"calcMode\":1,\"price\":1000,\"periodType\":2,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-19 13:00:08');
INSERT INTO `sys_audit_log` VALUES (58, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T13:00:33.6298095\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:00:33.6298095\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"电费\",\"feeItemId\":4,\"calcMode\":1,\"price\":0.5,\"periodType\":2,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-19 13:00:34');
INSERT INTO `sys_audit_log` VALUES (59, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-19T13:01:29.5126527\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:01:29.5126527\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"租金1\",\"feeItemId\":1,\"calcMode\":1,\"price\":2000,\"periodType\":2,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-19 13:01:30');
INSERT INTO `sys_audit_log` VALUES (60, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[]', '[{\"id\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1}]', NULL, '2026-08-19 13:01:45');
INSERT INTO `sys_audit_log` VALUES (61, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-18T18:31:52\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":0,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-19T13:01:44.9284435\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-19 13:01:45');
INSERT INTO `sys_audit_log` VALUES (62, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:02:50');
INSERT INTO `sys_audit_log` VALUES (63, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:02:55');
INSERT INTO `sys_audit_log` VALUES (64, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:03:00');
INSERT INTO `sys_audit_log` VALUES (65, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:03:12');
INSERT INTO `sys_audit_log` VALUES (66, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:03:19');
INSERT INTO `sys_audit_log` VALUES (67, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:03:34');
INSERT INTO `sys_audit_log` VALUES (68, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":1,\"ruleId\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50},{\"relId\":2,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1}]', NULL, '2026-08-19 13:03:34');
INSERT INTO `sys_audit_log` VALUES (69, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:01:45\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":0,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-19T13:03:33.8421788\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":null,\"stallNumber\":\"001\",\"stallName\":null,\"stallArea\":null,\"status\":0,\"remark\":null}', NULL, '2026-08-19 13:03:34');
INSERT INTO `sys_audit_log` VALUES (70, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 13:12:35');
INSERT INTO `sys_audit_log` VALUES (71, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '编辑', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T13:00:34\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:00:34\",\"isDelete\":0,\"companyId\":0,\"ruleName\":\"电费\",\"feeItemId\":4,\"calcMode\":1,\"price\":0.50,\"periodType\":2,\"overdueRate\":0.50,\"status\":1,\"remark\":\"\"}', '{\"id\":2,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-19T13:13:38.1535152\",\"isDelete\":null,\"companyId\":null,\"ruleName\":\"电费\",\"feeItemId\":4,\"calcMode\":1,\"price\":0.5,\"periodType\":0,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-19 13:13:38');
INSERT INTO `sys_audit_log` VALUES (72, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:28.6744161\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:05:28.6744161\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"meterNo\":\"0000a1\",\"meterType\":1,\"gatewayCode\":\"\",\"currentRead\":0,\"balanceAmount\":0,\"status\":1}', NULL, '2026-08-19 14:05:29');
INSERT INTO `sys_audit_log` VALUES (73, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:44.7295159\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:05:44.7295159\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-08\",\"waterRead\":0,\"elecRead\":null,\"waterPrice\":4.50,\"elecPrice\":0.50,\"propertyPrice\":50.00,\"waterUsage\":0,\"elecUsage\":0,\"waterAmount\":0.00,\"elecAmount\":0.00,\"propertyAmount\":50.00,\"totalAmount\":50.00,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 14:05:45');
INSERT INTO `sys_audit_log` VALUES (74, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T14:06:26.0127469\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:06:26.0127469\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-09\",\"waterRead\":25,\"elecRead\":null,\"waterPrice\":4.50,\"elecPrice\":0.50,\"propertyPrice\":50.00,\"waterUsage\":25.00,\"elecUsage\":0.00,\"waterAmount\":112.5000,\"elecAmount\":0.0000,\"propertyAmount\":50.00,\"totalAmount\":162.5000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 14:06:26');
INSERT INTO `sys_audit_log` VALUES (75, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:08:08.4637548\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:08:08.4637548\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-07\",\"waterRead\":20,\"elecRead\":null,\"waterPrice\":4.50,\"elecPrice\":0.50,\"propertyPrice\":50.00,\"waterUsage\":20,\"elecUsage\":0,\"waterAmount\":90.00,\"elecAmount\":0.00,\"propertyAmount\":50.00,\"totalAmount\":140.00,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 14:08:08');
INSERT INTO `sys_audit_log` VALUES (76, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '4', NULL, '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-19T14:15:33.3568033\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:15:33.3568033\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"水费\",\"feeItemId\":3,\"calcMode\":1,\"price\":6,\"periodType\":0,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-19 14:15:33');
INSERT INTO `sys_audit_log` VALUES (77, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":3,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":4,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1}]', NULL, '2026-08-19 14:15:50');
INSERT INTO `sys_audit_log` VALUES (78, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:03:34\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":0,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-19T14:15:49.6203475\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-19 14:15:50');
INSERT INTO `sys_audit_log` VALUES (79, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:16:03.4900647\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:16:03.4900647\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-07\",\"waterRead\":10,\"elecRead\":null,\"waterPrice\":6.00,\"elecPrice\":0.50,\"propertyPrice\":0,\"waterUsage\":10,\"elecUsage\":0,\"waterAmount\":60.00,\"elecAmount\":0.00,\"propertyAmount\":0,\"totalAmount\":60.00,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 14:16:03');
INSERT INTO `sys_audit_log` VALUES (80, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:16:24.1794244\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:16:24.1794244\",\"isDelete\":null,\"companyId\":0,\"billId\":1,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787120180250-zpeao73c\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', NULL, '2026-08-19 14:16:24');
INSERT INTO `sys_audit_log` VALUES (81, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T14:16:46.3713942\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:16:46.3713942\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-08\",\"waterRead\":15,\"elecRead\":null,\"waterPrice\":6.00,\"elecPrice\":0.50,\"propertyPrice\":0,\"waterUsage\":5.00,\"elecUsage\":0.00,\"waterAmount\":30.0000,\"elecAmount\":0.0000,\"propertyAmount\":0,\"totalAmount\":30.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-19 14:16:46');
INSERT INTO `sys_audit_log` VALUES (82, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T14:17:45.2726718\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:17:45.2726718\",\"isDelete\":null,\"companyId\":0,\"billId\":2,\"stallId\":1,\"merchantId\":null,\"payAmount\":30.00,\"payType\":1,\"requestId\":\"WE1787120262140-j0zftuu7\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', NULL, '2026-08-19 14:17:45');
INSERT INTO `sys_audit_log` VALUES (83, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '退费', '3', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:16:24\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:16:24\",\"isDelete\":0,\"companyId\":0,\"billId\":1,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787120180250-zpeao73c\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-19T14:18:09.5599668\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:18:09.5599668\",\"isDelete\":null,\"companyId\":0,\"billId\":1,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787120289217-nz21hm3n\",\"recordType\":2,\"refundStatus\":1,\"refundTime\":\"2026-08-19T14:18:09.5599668\",\"refundRecordId\":1,\"remark\":\"线下退费\"}', NULL, '2026-08-19 14:18:10');
INSERT INTO `sys_audit_log` VALUES (84, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 18:10:38');
INSERT INTO `sys_audit_log` VALUES (85, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-19 18:10:43');
INSERT INTO `sys_audit_log` VALUES (86, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 13:44:13');
INSERT INTO `sys_audit_log` VALUES (87, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 14:34:49');
INSERT INTO `sys_audit_log` VALUES (88, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_tenant', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-20T14:41:32.1546945\",\"updateBy\":1,\"updateTime\":\"2026-08-20T14:41:32.1546945\",\"isDelete\":null,\"companyId\":0,\"tenantName\":\"测试优惠租户\",\"tenantType\":3,\"contactPerson\":\"优惠\",\"contactPhone\":\"13112312312\",\"idCardNo\":\"\",\"socialCreditCode\":\"\",\"bankAccount\":\"\",\"address\":\"\",\"miniOpenid\":null,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-20 14:41:32');
INSERT INTO `sys_audit_log` VALUES (89, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '编辑', '3', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-19T13:01:30\",\"updateBy\":1,\"updateTime\":\"2026-08-19T13:01:30\",\"isDelete\":0,\"companyId\":0,\"ruleName\":\"租金1\",\"feeItemId\":1,\"calcMode\":1,\"price\":2000.00,\"periodType\":2,\"overdueRate\":0.50,\"status\":1,\"remark\":\"\"}', '{\"id\":3,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-20T15:08:55.4054214\",\"isDelete\":null,\"companyId\":null,\"ruleName\":\"租金1\",\"feeItemId\":1,\"calcMode\":1,\"price\":2000,\"periodType\":1,\"overdueRate\":0.5,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-20 15:08:55');
INSERT INTO `sys_audit_log` VALUES (90, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 15:31:21');
INSERT INTO `sys_audit_log` VALUES (91, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 15:34:03');
INSERT INTO `sys_audit_log` VALUES (92, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-20T15:34:03.0516459\",\"updateBy\":1,\"updateTime\":\"2026-08-20T15:34:03.0516459\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608201534030071\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":1000,\"depositAmount\":500,\"startTime\":\"2026-09-01\",\"endTime\":\"2027-08-31\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"????-??ID????\"}', NULL, '2026-08-20 15:34:03');
INSERT INTO `sys_audit_log` VALUES (93, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '1', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (94, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '2', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (95, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '3', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (96, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '4', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (97, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '5', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (98, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '6', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (99, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '7', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (100, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '8', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (101, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '9', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (102, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '10', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (103, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '11', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (104, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '12', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (105, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '13', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (106, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-20T15:34:03\",\"updateBy\":1,\"updateTime\":\"2026-08-20T15:34:03\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608201534030071\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":1000.00,\"depositAmount\":500.00,\"startTime\":\"2026-09-01\",\"endTime\":\"2027-08-31\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"????-??ID????\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-20T15:35:11.8739661\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"????-??ID????\"}', NULL, '2026-08-20 15:35:12');
INSERT INTO `sys_audit_log` VALUES (107, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-20T15:36:00.9924569\",\"updateBy\":1,\"updateTime\":\"2026-08-20T15:36:00.9924569\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608201536005513\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-20\",\"endTime\":\"2027-08-20\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"备注\"}', NULL, '2026-08-20 15:36:01');
INSERT INTO `sys_audit_log` VALUES (108, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '27', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (109, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '28', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (110, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '29', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (111, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '30', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (112, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '31', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (113, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '32', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (114, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '33', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (115, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '34', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (116, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '35', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (117, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '36', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (118, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '37', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (119, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '38', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (120, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '39', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (121, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '40', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (122, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-20T15:36:01\",\"updateBy\":1,\"updateTime\":\"2026-08-20T15:36:01\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608201536005513\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-20\",\"endTime\":\"2027-08-20\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"备注\"}', '{\"id\":2,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-20T16:20:13.7377904\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"备注\"}', NULL, '2026-08-20 16:20:14');
INSERT INTO `sys_audit_log` VALUES (123, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-20T16:20:38.9987308\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:20:38.9987308\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608201620384964\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-20\",\"endTime\":\"2026-10-16\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-20 16:20:39');
INSERT INTO `sys_audit_log` VALUES (124, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '54', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:26:43');
INSERT INTO `sys_audit_log` VALUES (125, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '55', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:26:43');
INSERT INTO `sys_audit_log` VALUES (126, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '56', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:26:43');
INSERT INTO `sys_audit_log` VALUES (127, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '57', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 16:26:43');
INSERT INTO `sys_audit_log` VALUES (128, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '3', '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-20T16:20:39\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:20:39\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608201620384964\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-20\",\"endTime\":\"2026-10-16\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":3,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-20T16:26:42.6245348\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-20 16:26:43');
INSERT INTO `sys_audit_log` VALUES (129, 0, 1, '集团超级管理员', '127.0.0.1', 'discount', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-20T16:28:04.4934286\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:28:04.4934286\",\"isDelete\":null,\"companyId\":0,\"applyNo\":\"DA-0-20260820-886681\",\"policyId\":1,\"policySnapshot\":\"{\\\"discountRate\\\":\\\"100.00\\\",\\\"deductAmount\\\":\\\"0.00\\\",\\\"waiveMonths\\\":1}\",\"sourceType\":\"contract\",\"sourceId\":\"6\",\"contractNo\":\"HT202608201628048145\",\"stallId\":1,\"tenantId\":1,\"waiveMonths\":1,\"discountRate\":100.00,\"deductAmount\":0.00,\"discountAmount\":2000.00,\"needAudit\":0,\"flowInstanceId\":null,\"applyStatus\":2,\"applyUserId\":1,\"auditTime\":\"2026-08-20T16:28:04.4934286\",\"remark\":\"\"}', NULL, '2026-08-20 16:28:05');
INSERT INTO `sys_audit_log` VALUES (130, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '6', NULL, '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-20T16:28:04.4779032\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:28:04.4779032\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608201628048145\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-20\",\"endTime\":\"2027-08-20\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-20 16:28:05');
INSERT INTO `sys_audit_log` VALUES (131, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 21:14:47');
INSERT INTO `sys_audit_log` VALUES (132, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '61', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (133, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '62', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (134, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '63', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (135, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '64', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (136, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '65', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (137, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '66', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (138, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '67', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (139, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '68', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (140, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '69', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (141, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '70', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (142, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '71', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (143, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '72', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (144, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '73', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (145, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '6', '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-20T16:28:04\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:28:04\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608201628048145\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-20\",\"endTime\":\"2027-08-20\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":6,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-20T21:15:44.5243538\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-20 21:15:45');
INSERT INTO `sys_audit_log` VALUES (146, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 22:14:48');
INSERT INTO `sys_audit_log` VALUES (147, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 22:19:15');
INSERT INTO `sys_audit_log` VALUES (148, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 22:25:13');
INSERT INTO `sys_audit_log` VALUES (149, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 22:26:30');
INSERT INTO `sys_audit_log` VALUES (150, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-20 22:26:46');
INSERT INTO `sys_audit_log` VALUES (151, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-21 00:25:21');
INSERT INTO `sys_audit_log` VALUES (152, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-21 08:46:19');
INSERT INTO `sys_audit_log` VALUES (153, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '13', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":null,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"0\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"1\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', NULL, '2026-08-21 08:47:07');
INSERT INTO `sys_audit_log` VALUES (154, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '13', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"1\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"0\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', NULL, '2026-08-21 09:01:47');
INSERT INTO `sys_audit_log` VALUES (155, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '13', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"0\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"1\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', NULL, '2026-08-21 09:05:08');
INSERT INTO `sys_audit_log` VALUES (156, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-21T11:40:46.4039448\",\"updateBy\":1,\"updateTime\":\"2026-08-21T11:40:46.4039448\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"waterRead\":45,\"elecRead\":null,\"waterPrice\":6.00,\"elecPrice\":0.50,\"propertyPrice\":0,\"waterUsage\":30.00,\"elecUsage\":0.00,\"waterAmount\":180.0000,\"elecAmount\":0.0000,\"propertyAmount\":0,\"totalAmount\":180.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-21 11:40:46');
INSERT INTO `sys_audit_log` VALUES (157, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-21 12:49:12');
INSERT INTO `sys_audit_log` VALUES (158, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-21 13:28:04');
INSERT INTO `sys_audit_log` VALUES (159, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-21T13:28:38.6103545\",\"updateBy\":1,\"updateTime\":\"2026-08-21T13:28:38.6103545\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-11\",\"waterRead\":55,\"elecRead\":null,\"waterPrice\":6.00,\"elecPrice\":0.50,\"propertyPrice\":0,\"waterUsage\":10.00,\"elecUsage\":0.00,\"waterAmount\":60.0000,\"elecAmount\":0.0000,\"propertyAmount\":0,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-21 13:28:39');
INSERT INTO `sys_audit_log` VALUES (160, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '86', NULL, '\"核销金额=60.00，账单=water_elec/8\"', NULL, '2026-08-21 13:29:19');
INSERT INTO `sys_audit_log` VALUES (161, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '4', NULL, '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-21T13:29:18.9690107\",\"updateBy\":1,\"updateTime\":\"2026-08-21T13:29:18.9690107\",\"isDelete\":null,\"companyId\":0,\"billId\":8,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787290155170-avdgfnhf\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', NULL, '2026-08-21 13:29:19');
INSERT INTO `sys_audit_log` VALUES (162, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-21T13:32:13.2672838\",\"updateBy\":1,\"updateTime\":\"2026-08-21T13:32:13.2672838\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-12\",\"waterRead\":100,\"elecRead\":null,\"waterPrice\":6.00,\"elecPrice\":0.50,\"propertyPrice\":0,\"waterUsage\":45.00,\"elecUsage\":0.00,\"waterAmount\":270.0000,\"elecAmount\":0.0000,\"propertyAmount\":0,\"totalAmount\":270.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-21 13:32:13');
INSERT INTO `sys_audit_log` VALUES (163, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '退款冲减', '86', NULL, '\"核销金额=-60.00，账单=water_elec/8\"', NULL, '2026-08-21 16:08:40');
INSERT INTO `sys_audit_log` VALUES (164, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '退费', '5', '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-21T13:29:19\",\"updateBy\":1,\"updateTime\":\"2026-08-21T13:29:19\",\"isDelete\":0,\"companyId\":0,\"billId\":8,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787290155170-avdgfnhf\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-21T16:08:39.9278627\",\"updateBy\":1,\"updateTime\":\"2026-08-21T16:08:39.9278627\",\"isDelete\":null,\"companyId\":0,\"billId\":8,\"stallId\":1,\"merchantId\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787299719555-eqz85sx4\",\"recordType\":2,\"refundStatus\":1,\"refundTime\":\"2026-08-21T16:08:39.9274608\",\"refundRecordId\":4,\"remark\":\"线下退费\"}', NULL, '2026-08-21 16:08:40');
INSERT INTO `sys_audit_log` VALUES (165, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '调账', '87', '{\"planAmount\":270.00}', '{\"adjustAmount\":12,\"remark\":\"调账测试\",\"planAmount\":282.00}', NULL, '2026-08-21 16:19:15');
INSERT INTO `sys_audit_log` VALUES (166, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '退费', '6', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-19T14:17:45\",\"updateBy\":1,\"updateTime\":\"2026-08-19T14:17:45\",\"isDelete\":0,\"companyId\":0,\"billId\":2,\"stallId\":1,\"merchantId\":null,\"payAmount\":30.00,\"payType\":1,\"requestId\":\"WE1787120262140-j0zftuu7\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-21T16:21:26.9913957\",\"updateBy\":1,\"updateTime\":\"2026-08-21T16:21:26.9913957\",\"isDelete\":null,\"companyId\":0,\"billId\":2,\"stallId\":1,\"merchantId\":null,\"payAmount\":30.00,\"payType\":1,\"requestId\":\"WE1787300486973-ga15ufkv\",\"recordType\":2,\"refundStatus\":1,\"refundTime\":\"2026-08-21T16:21:26.9913117\",\"refundRecordId\":2,\"remark\":\"线下退费\"}', NULL, '2026-08-21 16:21:27');
INSERT INTO `sys_audit_log` VALUES (167, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '7', NULL, '{\"id\":7,\"createBy\":1,\"createTime\":\"2026-08-21T16:24:54.5662767\",\"updateBy\":1,\"updateTime\":\"2026-08-21T16:24:54.5662767\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608211624544996\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-21 16:24:55');
INSERT INTO `sys_audit_log` VALUES (168, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '88', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (169, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '89', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (170, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '90', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (171, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '91', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (172, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '92', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (173, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '93', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (174, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '94', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (175, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '95', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (176, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '96', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (177, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '97', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (178, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '98', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (179, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '99', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (180, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '100', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (181, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '101', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (182, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '7', '{\"id\":7,\"createBy\":1,\"createTime\":\"2026-08-21T16:24:55\",\"updateBy\":1,\"updateTime\":\"2026-08-21T16:24:55\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608211624544996\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":7,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-21T19:01:33.4525535\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-21 19:01:34');
INSERT INTO `sys_audit_log` VALUES (183, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-21T19:02:25.5006697\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:02:25.5006697\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608211902253543\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-21 19:02:26');
INSERT INTO `sys_audit_log` VALUES (184, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '115', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (185, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '116', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (186, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '117', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (187, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '118', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (188, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '119', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (189, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '120', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (190, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '121', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (191, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '122', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (192, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '123', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (193, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '124', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (194, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '125', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (195, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '126', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (196, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '127', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (197, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '128', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (198, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '8', '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-21T19:02:26\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:02:26\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608211902253543\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":8,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-21T19:02:32.1078461\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-21 19:02:32');
INSERT INTO `sys_audit_log` VALUES (199, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-21T19:04:26.0549247\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:04:26.0549247\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"物业费\",\"feeItemId\":2,\"calcMode\":2,\"price\":0.5,\"periodType\":2,\"overdueRate\":0,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-21 19:04:26');
INSERT INTO `sys_audit_log` VALUES (200, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":5,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":6,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":7,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-21 19:04:57');
INSERT INTO `sys_audit_log` VALUES (201, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:02:32\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":0,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-21T19:04:57.3142944\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-21 19:04:57');
INSERT INTO `sys_audit_log` VALUES (202, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-21T19:05:26.3231268\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:05:26.3231268\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608211905266097\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-21 19:05:26');
INSERT INTO `sys_audit_log` VALUES (203, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 10:09:50');
INSERT INTO `sys_audit_log` VALUES (204, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '2', '[]', '[{\"id\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-23 18:08:41');
INSERT INTO `sys_audit_log` VALUES (205, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-23T18:08:41.3697757\",\"updateBy\":1,\"updateTime\":\"2026-08-23T18:08:41.3697757\",\"isDelete\":null,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":1,\"stallNumber\":\"002\",\"stallName\":\"\",\"stallArea\":80,\"status\":0,\"remark\":\"\"}', NULL, '2026-08-23 18:08:41');
INSERT INTO `sys_audit_log` VALUES (206, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '2', '[{\"relId\":12,\"ruleId\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50},{\"relId\":13,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":14,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":15,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00}]', '[{\"id\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-23 18:09:03');
INSERT INTO `sys_audit_log` VALUES (207, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-23T18:08:41\",\"updateBy\":1,\"updateTime\":\"2026-08-23T18:08:41\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":1,\"stallNumber\":\"002\",\"stallName\":\"\",\"stallArea\":80.00,\"status\":0,\"remark\":\"\"}', '{\"id\":2,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-23T18:09:03.4578365\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":1,\"stallNumber\":\"002\",\"stallName\":\"商铺\",\"stallArea\":80,\"status\":0,\"remark\":\"\"}', NULL, '2026-08-23 18:09:03');
INSERT INTO `sys_audit_log` VALUES (208, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 19:49:30');
INSERT INTO `sys_audit_log` VALUES (209, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 19:49:43');
INSERT INTO `sys_audit_log` VALUES (210, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 19:55:20');
INSERT INTO `sys_audit_log` VALUES (211, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:08:23');
INSERT INTO `sys_audit_log` VALUES (212, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:08:42');
INSERT INTO `sys_audit_log` VALUES (213, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-23T20:08:42.379666\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:08:42.379666\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"usage\":120,\"unitPrice\":6.00,\"totalAmount\":720.00,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:08:42');
INSERT INTO `sys_audit_log` VALUES (214, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:09:41');
INSERT INTO `sys_audit_log` VALUES (215, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:09:56');
INSERT INTO `sys_audit_log` VALUES (216, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'propertyFee', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-23T20:09:55.8374827\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:09:55.8374827\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-08\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.0000,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:09:56');
INSERT INTO `sys_audit_log` VALUES (217, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:27:04');
INSERT INTO `sys_audit_log` VALUES (218, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-23T20:27:04.3139742\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:27:04.3139742\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":110.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:27:04');
INSERT INTO `sys_audit_log` VALUES (219, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:30:09');
INSERT INTO `sys_audit_log` VALUES (220, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:30:18');
INSERT INTO `sys_audit_log` VALUES (221, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:30:19');
INSERT INTO `sys_audit_log` VALUES (222, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:30:45');
INSERT INTO `sys_audit_log` VALUES (223, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-23 20:31:45');
INSERT INTO `sys_audit_log` VALUES (224, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-23T20:33:15.872083\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:33:15.872083\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":120.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:33:16');
INSERT INTO `sys_audit_log` VALUES (225, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '4', NULL, '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-23T20:33:39.8997479\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:33:39.8997479\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":130.00,\"usage\":20.00,\"unitPrice\":6.00,\"totalAmount\":120.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:33:40');
INSERT INTO `sys_audit_log` VALUES (226, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-23T20:34:23.3512664\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:34:23.3512664\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-09\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.0000,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:34:23');
INSERT INTO `sys_audit_log` VALUES (227, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-08-23T20:34:46.3591424\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:34:46.3591424\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-11\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.0000,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:34:46');
INSERT INTO `sys_audit_log` VALUES (228, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '4', NULL, '{\"id\":4,\"createBy\":1,\"createTime\":\"2026-08-23T20:34:46.3844705\",\"updateBy\":1,\"updateTime\":\"2026-08-23T20:34:46.3844705\",\"isDelete\":null,\"companyId\":0,\"stallId\":2,\"merchantId\":null,\"billMonth\":\"2026-11\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":80.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":40.0000,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-23 20:34:46');
INSERT INTO `sys_audit_log` VALUES (229, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '新增', '6', NULL, '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-24T07:24:25.4059225\",\"updateBy\":1,\"updateTime\":\"2026-08-24T07:24:25.4059225\",\"isDelete\":null,\"companyId\":0,\"ruleName\":\"押金\",\"feeItemId\":5,\"calcMode\":1,\"price\":1000,\"periodType\":0,\"overdueRate\":0,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-24 07:24:25');
INSERT INTO `sys_audit_log` VALUES (230, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '2', '[{\"relId\":16,\"ruleId\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50},{\"relId\":17,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":18,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":19,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00}]', '[{\"id\":1,\"ruleName\":\"租金\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.50,\"status\":1},{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1},{\"id\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-24 07:24:46');
INSERT INTO `sys_audit_log` VALUES (231, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '2', '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-08-23T18:08:41\",\"updateBy\":1,\"updateTime\":\"2026-08-23T18:09:03\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":1,\"stallNumber\":\"002\",\"stallName\":\"商铺\",\"stallArea\":80.00,\"status\":0,\"remark\":\"\"}', '{\"id\":2,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-24T07:24:46.0859929\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":1,\"stallNumber\":\"002\",\"stallName\":\"商铺\",\"stallArea\":80,\"status\":0,\"remark\":\"\"}', NULL, '2026-08-24 07:24:46');
INSERT INTO `sys_audit_log` VALUES (232, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '10', NULL, '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-08-24T07:25:17.1350561\",\"updateBy\":1,\"updateTime\":\"2026-08-24T07:25:17.1350561\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608240725173862\",\"merchantId\":null,\"tenantId\":1,\"stallId\":2,\"rentAmount\":1000,\"depositAmount\":1000,\"startTime\":\"2026-08-24\",\"endTime\":\"2027-08-24\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-24 07:25:17');
INSERT INTO `sys_audit_log` VALUES (233, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 07:45:16');
INSERT INTO `sys_audit_log` VALUES (234, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 07:48:07');
INSERT INTO `sys_audit_log` VALUES (235, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 07:52:44');
INSERT INTO `sys_audit_log` VALUES (236, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '159', NULL, '\"核销金额=120.00，账单=water_elec/4\"', NULL, '2026-08-24 07:59:59');
INSERT INTO `sys_audit_log` VALUES (237, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '7', NULL, '{\"id\":7,\"createBy\":1,\"createTime\":\"2026-08-24T07:59:58.7317968\",\"updateBy\":1,\"updateTime\":\"2026-08-24T07:59:58.7317968\",\"isDelete\":null,\"companyId\":0,\"billId\":4,\"stallId\":1,\"merchantId\":0,\"payAmount\":120.00,\"payType\":3,\"requestId\":\"WE1787529595604-4yzhjlgd\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', NULL, '2026-08-24 07:59:59');
INSERT INTO `sys_audit_log` VALUES (238, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 08:18:23');
INSERT INTO `sys_audit_log` VALUES (239, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 08:40:30');
INSERT INTO `sys_audit_log` VALUES (240, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 08:57:59');
INSERT INTO `sys_audit_log` VALUES (241, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-24 10:13:40');
INSERT INTO `sys_audit_log` VALUES (242, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-24T11:38:24.1315652\",\"updateBy\":1,\"updateTime\":\"2026-08-24T11:38:24.1315652\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":150.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-24 11:38:24');
INSERT INTO `sys_audit_log` VALUES (243, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '177', NULL, '\"核销金额=60.00，账单=water_elec/5\"', NULL, '2026-08-24 11:39:06');
INSERT INTO `sys_audit_log` VALUES (244, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-24T11:39:05.5059933\",\"updateBy\":1,\"updateTime\":\"2026-08-24T11:39:05.5059933\",\"isDelete\":null,\"companyId\":0,\"billId\":5,\"stallId\":1,\"merchantId\":0,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787542743243-o3bbsi8q\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\"}', NULL, '2026-08-24 11:39:06');
INSERT INTO `sys_audit_log` VALUES (245, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '作废', '2', '{\"id\":2,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"2\",\"planId\":null,\"merchantId\":null,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":30.00,\"discountAmount\":0.00,\"realAmount\":30.00,\"payType\":1,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":null,\"remark\":\"水电物业缴费-2026-08\",\"createBy\":0,\"createTime\":\"2026-08-19T14:17:45\"}', NULL, NULL, '2026-08-24 16:10:23');
INSERT INTO `sys_audit_log` VALUES (246, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '冲红申请', '3', '{\"id\":3,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"1\",\"planId\":null,\"merchantId\":null,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":2,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":null,\"remark\":\"水电物业退费\",\"createBy\":0,\"createTime\":\"2026-08-19T14:18:09\"}', NULL, NULL, '2026-08-24 16:11:06');
INSERT INTO `sys_audit_log` VALUES (247, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '158', NULL, '\"核销金额=60.00，账单=water_elec/3\"', NULL, '2026-08-24 16:23:39');
INSERT INTO `sys_audit_log` VALUES (248, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-24T16:23:38.2371223\",\"updateBy\":1,\"updateTime\":\"2026-08-24T16:23:38.2371223\",\"isDelete\":null,\"companyId\":0,\"billId\":3,\"stallId\":1,\"merchantId\":0,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"WE1787559815038-k7jitdur\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-08-24 16:23:39');
INSERT INTO `sys_audit_log` VALUES (249, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:35:27');
INSERT INTO `sys_audit_log` VALUES (250, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:35:37');
INSERT INTO `sys_audit_log` VALUES (251, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:35:37');
INSERT INTO `sys_audit_log` VALUES (252, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:35:53');
INSERT INTO `sys_audit_log` VALUES (253, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:35:53');
INSERT INTO `sys_audit_log` VALUES (254, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:36:25');
INSERT INTO `sys_audit_log` VALUES (255, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 08:37:38');
INSERT INTO `sys_audit_log` VALUES (256, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:08:15');
INSERT INTO `sys_audit_log` VALUES (257, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:08:15');
INSERT INTO `sys_audit_log` VALUES (258, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:08:30');
INSERT INTO `sys_audit_log` VALUES (259, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:09:20');
INSERT INTO `sys_audit_log` VALUES (260, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:09:40');
INSERT INTO `sys_audit_log` VALUES (261, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:09:40');
INSERT INTO `sys_audit_log` VALUES (262, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:09:58');
INSERT INTO `sys_audit_log` VALUES (263, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:09:58');
INSERT INTO `sys_audit_log` VALUES (264, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:10:12');
INSERT INTO `sys_audit_log` VALUES (265, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:10:12');
INSERT INTO `sys_audit_log` VALUES (266, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:10:26');
INSERT INTO `sys_audit_log` VALUES (267, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:11:01');
INSERT INTO `sys_audit_log` VALUES (268, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:17:15');
INSERT INTO `sys_audit_log` VALUES (269, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:17:40');
INSERT INTO `sys_audit_log` VALUES (270, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:19:35');
INSERT INTO `sys_audit_log` VALUES (271, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:20:00');
INSERT INTO `sys_audit_log` VALUES (272, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:20:13');
INSERT INTO `sys_audit_log` VALUES (273, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:31');
INSERT INTO `sys_audit_log` VALUES (274, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:31');
INSERT INTO `sys_audit_log` VALUES (275, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:31');
INSERT INTO `sys_audit_log` VALUES (276, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:31');
INSERT INTO `sys_audit_log` VALUES (277, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:51');
INSERT INTO `sys_audit_log` VALUES (278, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:24:51');
INSERT INTO `sys_audit_log` VALUES (279, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:25:03');
INSERT INTO `sys_audit_log` VALUES (280, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:25:46');
INSERT INTO `sys_audit_log` VALUES (281, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:26:00');
INSERT INTO `sys_audit_log` VALUES (282, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:26:16');
INSERT INTO `sys_audit_log` VALUES (283, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:28:14');
INSERT INTO `sys_audit_log` VALUES (284, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:34:08');
INSERT INTO `sys_audit_log` VALUES (285, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 09:34:58');
INSERT INTO `sys_audit_log` VALUES (286, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 11:35:31');
INSERT INTO `sys_audit_log` VALUES (287, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 11:37:31');
INSERT INTO `sys_audit_log` VALUES (288, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 11:40:47');
INSERT INTO `sys_audit_log` VALUES (289, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 11:51:07');
INSERT INTO `sys_audit_log` VALUES (290, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 12:47:21');
INSERT INTO `sys_audit_log` VALUES (291, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 12:48:25');
INSERT INTO `sys_audit_log` VALUES (292, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 12:49:04');
INSERT INTO `sys_audit_log` VALUES (293, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 12:57:54');
INSERT INTO `sys_audit_log` VALUES (294, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 13:19:21');
INSERT INTO `sys_audit_log` VALUES (295, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 13:22:34');
INSERT INTO `sys_audit_log` VALUES (296, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 13:22:48');
INSERT INTO `sys_audit_log` VALUES (297, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 13:25:33');
INSERT INTO `sys_audit_log` VALUES (298, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 18:11:27');
INSERT INTO `sys_audit_log` VALUES (299, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 18:14:35');
INSERT INTO `sys_audit_log` VALUES (300, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:07');
INSERT INTO `sys_audit_log` VALUES (301, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:10');
INSERT INTO `sys_audit_log` VALUES (302, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:26');
INSERT INTO `sys_audit_log` VALUES (303, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:26');
INSERT INTO `sys_audit_log` VALUES (304, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:26');
INSERT INTO `sys_audit_log` VALUES (305, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:43:46');
INSERT INTO `sys_audit_log` VALUES (306, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-25 20:46:16');
INSERT INTO `sys_audit_log` VALUES (307, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:26:42');
INSERT INTO `sys_audit_log` VALUES (308, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:36:06');
INSERT INTO `sys_audit_log` VALUES (309, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:36:06');
INSERT INTO `sys_audit_log` VALUES (310, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:37:17');
INSERT INTO `sys_audit_log` VALUES (311, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:38:17');
INSERT INTO `sys_audit_log` VALUES (312, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:39:14');
INSERT INTO `sys_audit_log` VALUES (313, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:40:07');
INSERT INTO `sys_audit_log` VALUES (314, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:40:07');
INSERT INTO `sys_audit_log` VALUES (315, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:40:23');
INSERT INTO `sys_audit_log` VALUES (316, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:40:59');
INSERT INTO `sys_audit_log` VALUES (317, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:40:59');
INSERT INTO `sys_audit_log` VALUES (318, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:41:13');
INSERT INTO `sys_audit_log` VALUES (319, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:43:08');
INSERT INTO `sys_audit_log` VALUES (320, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:43:08');
INSERT INTO `sys_audit_log` VALUES (321, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:46:30');
INSERT INTO `sys_audit_log` VALUES (322, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:46:53');
INSERT INTO `sys_audit_log` VALUES (323, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:50:45');
INSERT INTO `sys_audit_log` VALUES (324, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 01:50:45');
INSERT INTO `sys_audit_log` VALUES (325, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 02:01:55');
INSERT INTO `sys_audit_log` VALUES (326, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 02:04:28');
INSERT INTO `sys_audit_log` VALUES (327, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:25:22');
INSERT INTO `sys_audit_log` VALUES (328, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:25:30');
INSERT INTO `sys_audit_log` VALUES (329, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:26:17');
INSERT INTO `sys_audit_log` VALUES (330, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:27:06');
INSERT INTO `sys_audit_log` VALUES (331, 0, 1, '集团超级管理员', '127.0.0.1', 'base', '退出', '1', NULL, NULL, NULL, '2026-08-26 07:28:37');
INSERT INTO `sys_audit_log` VALUES (332, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:28:42');
INSERT INTO `sys_audit_log` VALUES (333, 0, 1, '集团超级管理员', '127.0.0.1', 'hr_org', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-26T07:29:03.6761399\",\"updateBy\":1,\"updateTime\":\"2026-08-26T07:29:03.6761399\",\"isDelete\":null,\"companyId\":0,\"postName\":\"测试岗位\",\"postCode\":\"0001\",\"postLevel\":\"P0\",\"deptId\":null,\"status\":1,\"remark\":\"\"}', NULL, '2026-08-26 07:29:04');
INSERT INTO `sys_audit_log` VALUES (334, 0, 1, '集团超级管理员', '127.0.0.1', 'hr_employee', '新增', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-26T07:29:53.5092994\",\"updateBy\":1,\"updateTime\":\"2026-08-26T07:29:53.5092994\",\"isDelete\":null,\"companyId\":0,\"userId\":null,\"employeeNo\":\"00001\",\"name\":\"员工名\",\"idCardNo\":null,\"phone\":\"13112312312\",\"email\":\"123@qqw.com\",\"gender\":1,\"birthdate\":null,\"entryDate\":\"2026-08-26\",\"regularDate\":null,\"resignDate\":null,\"employmentType\":1,\"employeeStatus\":1,\"orgId\":null,\"postId\":null,\"postLevel\":null,\"orgName\":null,\"supervisorId\":null,\"bankAccount\":null,\"socialSecurityBase\":null,\"basicSalary\":5000,\"remark\":\"备注\"}', NULL, '2026-08-26 07:29:54');
INSERT INTO `sys_audit_log` VALUES (335, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '6', NULL, '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-26T07:32:07.2113052\",\"updateBy\":1,\"updateTime\":\"2026-08-26T07:32:07.2113052\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":160.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-26 07:32:07');
INSERT INTO `sys_audit_log` VALUES (336, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:38:19');
INSERT INTO `sys_audit_log` VALUES (337, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:38:27');
INSERT INTO `sys_audit_log` VALUES (338, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:43:04');
INSERT INTO `sys_audit_log` VALUES (339, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:43:20');
INSERT INTO `sys_audit_log` VALUES (340, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:43:41');
INSERT INTO `sys_audit_log` VALUES (341, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:44:13');
INSERT INTO `sys_audit_log` VALUES (342, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:44:21');
INSERT INTO `sys_audit_log` VALUES (343, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:45:30');
INSERT INTO `sys_audit_log` VALUES (344, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:48:47');
INSERT INTO `sys_audit_log` VALUES (345, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:48:47');
INSERT INTO `sys_audit_log` VALUES (346, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:48:58');
INSERT INTO `sys_audit_log` VALUES (347, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:49:26');
INSERT INTO `sys_audit_log` VALUES (348, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:53:53');
INSERT INTO `sys_audit_log` VALUES (349, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 07:57:43');
INSERT INTO `sys_audit_log` VALUES (350, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:00:40');
INSERT INTO `sys_audit_log` VALUES (351, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:00:51');
INSERT INTO `sys_audit_log` VALUES (352, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:01:10');
INSERT INTO `sys_audit_log` VALUES (353, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:01:10');
INSERT INTO `sys_audit_log` VALUES (354, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:02:14');
INSERT INTO `sys_audit_log` VALUES (355, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:02:14');
INSERT INTO `sys_audit_log` VALUES (356, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:03:31');
INSERT INTO `sys_audit_log` VALUES (357, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:03:31');
INSERT INTO `sys_audit_log` VALUES (358, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:06:05');
INSERT INTO `sys_audit_log` VALUES (359, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:06:05');
INSERT INTO `sys_audit_log` VALUES (360, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:07:13');
INSERT INTO `sys_audit_log` VALUES (361, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:09:24');
INSERT INTO `sys_audit_log` VALUES (362, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:09:35');
INSERT INTO `sys_audit_log` VALUES (363, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:46:24');
INSERT INTO `sys_audit_log` VALUES (364, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:46:37');
INSERT INTO `sys_audit_log` VALUES (365, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:46:37');
INSERT INTO `sys_audit_log` VALUES (366, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:46:44');
INSERT INTO `sys_audit_log` VALUES (367, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:46:44');
INSERT INTO `sys_audit_log` VALUES (368, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:47:00');
INSERT INTO `sys_audit_log` VALUES (369, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:47:15');
INSERT INTO `sys_audit_log` VALUES (370, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:47:36');
INSERT INTO `sys_audit_log` VALUES (371, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:47:54');
INSERT INTO `sys_audit_log` VALUES (372, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:48:09');
INSERT INTO `sys_audit_log` VALUES (373, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:48:59');
INSERT INTO `sys_audit_log` VALUES (374, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:49:10');
INSERT INTO `sys_audit_log` VALUES (375, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:50:49');
INSERT INTO `sys_audit_log` VALUES (376, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:50:51');
INSERT INTO `sys_audit_log` VALUES (377, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:50:51');
INSERT INTO `sys_audit_log` VALUES (378, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:51:01');
INSERT INTO `sys_audit_log` VALUES (379, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:51:10');
INSERT INTO `sys_audit_log` VALUES (380, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:51:19');
INSERT INTO `sys_audit_log` VALUES (381, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:51:38');
INSERT INTO `sys_audit_log` VALUES (382, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:51:56');
INSERT INTO `sys_audit_log` VALUES (383, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:52:08');
INSERT INTO `sys_audit_log` VALUES (384, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:52:31');
INSERT INTO `sys_audit_log` VALUES (385, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-26 08:52:49');
INSERT INTO `sys_audit_log` VALUES (386, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:16:34');
INSERT INTO `sys_audit_log` VALUES (387, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:16:48');
INSERT INTO `sys_audit_log` VALUES (388, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:27:28');
INSERT INTO `sys_audit_log` VALUES (389, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:27:39');
INSERT INTO `sys_audit_log` VALUES (390, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:32:35');
INSERT INTO `sys_audit_log` VALUES (391, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 12:35:06');
INSERT INTO `sys_audit_log` VALUES (392, 0, 1, '集团超级管理员', '127.0.0.1', 'sys', '编辑', '13', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"1\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', '{\"id\":13,\"createBy\":0,\"createTime\":\"2026-08-20T16:49:26\",\"updateBy\":1,\"updateTime\":\"2026-08-20T16:49:45\",\"isDelete\":0,\"companyId\":0,\"configKey\":\"contract.discount_editable\",\"configValue\":\"0\",\"configName\":\"合同优惠参数可写开关\",\"remark\":\"1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）\"}', NULL, '2026-08-27 16:08:10');
INSERT INTO `sys_audit_log` VALUES (393, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 16:08:12');
INSERT INTO `sys_audit_log` VALUES (394, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 16:59:27');
INSERT INTO `sys_audit_log` VALUES (395, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:00:00');
INSERT INTO `sys_audit_log` VALUES (396, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:00:30');
INSERT INTO `sys_audit_log` VALUES (397, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:00:30');
INSERT INTO `sys_audit_log` VALUES (399, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:02:25');
INSERT INTO `sys_audit_log` VALUES (401, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:03:17');
INSERT INTO `sys_audit_log` VALUES (403, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:04:08');
INSERT INTO `sys_audit_log` VALUES (404, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:05:50');
INSERT INTO `sys_audit_log` VALUES (406, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:10:56');
INSERT INTO `sys_audit_log` VALUES (408, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:14:34');
INSERT INTO `sys_audit_log` VALUES (409, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:14:34');
INSERT INTO `sys_audit_log` VALUES (410, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '13', '{\"id\":13,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"3\",\"planId\":158,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000003\",\"remark\":\"水电物业缴费\",\"createBy\":0,\"createTime\":\"2026-08-24T16:23:38\"}', NULL, NULL, '2026-08-27 17:14:34');
INSERT INTO `sys_audit_log` VALUES (411, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-27T17:14:34.1387077\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:14:34.1387077\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271714346211\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"13\",\"title\":\"冲红申请-流水ID:13\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:14:34.1377065\",\"finishTime\":null}', NULL, '2026-08-27 17:14:34');
INSERT INTO `sys_audit_log` VALUES (412, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:15:01');
INSERT INTO `sys_audit_log` VALUES (413, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:16:05');
INSERT INTO `sys_audit_log` VALUES (414, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:16:31');
INSERT INTO `sys_audit_log` VALUES (415, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '冲红申请', '11', '{\"id\":11,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002\",\"remark\":\"水电物业缴费\",\"createBy\":0,\"createTime\":\"2026-08-24T11:39:05\"}', NULL, NULL, '2026-08-27 17:16:51');
INSERT INTO `sys_audit_log` VALUES (416, 0, 1, '集团超级管理员', '127.0.0.1', 'flow_engine', '提交', '6', NULL, '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-27T17:16:51.4836429\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:16:51.4836429\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271716518059\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:16:51.4836429\",\"finishTime\":null}', NULL, '2026-08-27 17:16:51');
INSERT INTO `sys_audit_log` VALUES (417, 0, 1, '集团超级管理员', '127.0.0.1', 'flow_engine', '完成', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-27T17:14:34\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:14:34\",\"isDelete\":0,\"companyId\":0,\"instanceNo\":\"FL202608271714346211\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"13\",\"title\":\"冲红申请-流水ID:13\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":1,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:14:34\",\"finishTime\":\"2026-08-27T17:17:08.4319551\"}', NULL, '2026-08-27 17:17:08');
INSERT INTO `sys_audit_log` VALUES (418, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:25:45');
INSERT INTO `sys_audit_log` VALUES (419, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:26:14');
INSERT INTO `sys_audit_log` VALUES (420, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:26:14');
INSERT INTO `sys_audit_log` VALUES (421, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '完成', '6', NULL, '{\"id\":6,\"createBy\":1,\"createTime\":\"2026-08-27T17:16:51\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:16:51\",\"isDelete\":0,\"companyId\":0,\"instanceNo\":\"FL202608271716518059\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":1,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:16:51\",\"finishTime\":\"2026-08-27T17:26:14.6127116\"}', NULL, '2026-08-27 17:26:15');
INSERT INTO `sys_audit_log` VALUES (422, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:28:06');
INSERT INTO `sys_audit_log` VALUES (423, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:28:48');
INSERT INTO `sys_audit_log` VALUES (424, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:28:48');
INSERT INTO `sys_audit_log` VALUES (425, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '20', '{\"id\":20,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":-60.00,\"payType\":3,\"flowType\":2,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002-RED\",\"remark\":\"冲红反向流水（原流水ID=11，审批通过后生效）\",\"createBy\":1,\"createTime\":\"2026-08-27T17:16:51\"}', NULL, NULL, '2026-08-27 17:28:48');
INSERT INTO `sys_audit_log` VALUES (426, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '7', NULL, '{\"id\":7,\"createBy\":1,\"createTime\":\"2026-08-27T17:28:48.0641336\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:28:48.0641336\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271728486164\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"20\",\"title\":\"冲红申请-流水ID:20\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:28:48.0641336\",\"finishTime\":null}', NULL, '2026-08-27 17:28:48');
INSERT INTO `sys_audit_log` VALUES (427, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:29:22');
INSERT INTO `sys_audit_log` VALUES (428, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:29:54');
INSERT INTO `sys_audit_log` VALUES (429, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '19', '{\"id\":19,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"3\",\"planId\":158,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":-60.00,\"payType\":3,\"flowType\":2,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000003-RED\",\"remark\":\"冲红反向流水（原流水ID=13，审批通过后生效）\",\"createBy\":1,\"createTime\":\"2026-08-27T17:14:34\"}', NULL, NULL, '2026-08-27 17:29:54');
INSERT INTO `sys_audit_log` VALUES (430, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-27T17:29:54.4289163\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:29:54.4289163\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271729540166\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"19\",\"title\":\"冲红申请-流水ID:19\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:29:54.4289163\",\"finishTime\":null}', NULL, '2026-08-27 17:29:54');
INSERT INTO `sys_audit_log` VALUES (431, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:31:03');
INSERT INTO `sys_audit_log` VALUES (432, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:31:29');
INSERT INTO `sys_audit_log` VALUES (433, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:31:29');
INSERT INTO `sys_audit_log` VALUES (434, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '11', '{\"id\":11,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002\",\"remark\":\"水电物业缴费【冲红审批中：测试冲红】\",\"createBy\":0,\"createTime\":\"2026-08-24T11:39:05\"}', NULL, NULL, '2026-08-27 17:31:29');
INSERT INTO `sys_audit_log` VALUES (435, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-27T17:31:28.9935372\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:31:28.9935372\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271731282915\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:31:28.9911434\",\"finishTime\":null}', NULL, '2026-08-27 17:31:29');
INSERT INTO `sys_audit_log` VALUES (436, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:31:58');
INSERT INTO `sys_audit_log` VALUES (437, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:32:25');
INSERT INTO `sys_audit_log` VALUES (438, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:32:54');
INSERT INTO `sys_audit_log` VALUES (439, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:33:26');
INSERT INTO `sys_audit_log` VALUES (440, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:33:54');
INSERT INTO `sys_audit_log` VALUES (442, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:36:40');
INSERT INTO `sys_audit_log` VALUES (443, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:40:12');
INSERT INTO `sys_audit_log` VALUES (444, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:40:37');
INSERT INTO `sys_audit_log` VALUES (445, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:41:59');
INSERT INTO `sys_audit_log` VALUES (446, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:42:14');
INSERT INTO `sys_audit_log` VALUES (447, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:42:53');
INSERT INTO `sys_audit_log` VALUES (448, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:44:57');
INSERT INTO `sys_audit_log` VALUES (449, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:45:13');
INSERT INTO `sys_audit_log` VALUES (450, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:46:20');
INSERT INTO `sys_audit_log` VALUES (451, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '11', '{\"id\":11,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002\",\"remark\":\"水电物业缴费【冲红审批中：测试冲红】【冲红审批中：端到端测试】\",\"createBy\":0,\"createTime\":\"2026-08-24T11:39:05\"}', NULL, NULL, '2026-08-27 17:46:20');
INSERT INTO `sys_audit_log` VALUES (452, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '10', NULL, '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-08-27T17:46:20.0047357\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:46:20.0047357\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271746206244\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:46:20.0047357\",\"finishTime\":null}', NULL, '2026-08-27 17:46:20');
INSERT INTO `sys_audit_log` VALUES (453, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:46:51');
INSERT INTO `sys_audit_log` VALUES (454, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:48:00');
INSERT INTO `sys_audit_log` VALUES (455, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:48:17');
INSERT INTO `sys_audit_log` VALUES (456, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:50:01');
INSERT INTO `sys_audit_log` VALUES (457, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:51:10');
INSERT INTO `sys_audit_log` VALUES (459, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:52:58');
INSERT INTO `sys_audit_log` VALUES (460, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:52:58');
INSERT INTO `sys_audit_log` VALUES (461, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '11', '{\"id\":11,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002\",\"remark\":\"水电物业缴费【冲红审批中：测试冲红】【冲红审批中：端到端测试】【冲红审批中：test】\",\"createBy\":0,\"createTime\":\"2026-08-24T11:39:05\"}', NULL, NULL, '2026-08-27 17:52:58');
INSERT INTO `sys_audit_log` VALUES (462, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '11', NULL, '{\"id\":11,\"createBy\":1,\"createTime\":\"2026-08-27T17:52:58.1015911\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:52:58.1015911\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271752580135\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:52:58.1005862\",\"finishTime\":null}', NULL, '2026-08-27 17:52:58');
INSERT INTO `sys_audit_log` VALUES (463, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:55:03');
INSERT INTO `sys_audit_log` VALUES (464, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:55:03');
INSERT INTO `sys_audit_log` VALUES (465, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:55:59');
INSERT INTO `sys_audit_log` VALUES (466, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红申请', '11', '{\"id\":11,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"5\",\"planId\":177,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000002\",\"remark\":\"水电物业缴费【冲红审批中：测试冲红】【冲红审批中：端到端测试】【冲红审批中：test】【冲红审批中：test】\",\"createBy\":0,\"createTime\":\"2026-08-24T11:39:05\"}', NULL, NULL, '2026-08-27 17:55:59');
INSERT INTO `sys_audit_log` VALUES (467, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '提交', '12', NULL, '{\"id\":12,\"createBy\":1,\"createTime\":\"2026-08-27T17:55:59.199313\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:55:59.199313\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608271755592931\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:55:59.1973021\",\"finishTime\":null}', NULL, '2026-08-27 17:55:59');
INSERT INTO `sys_audit_log` VALUES (468, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:56:30');
INSERT INTO `sys_audit_log` VALUES (469, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'flow_engine', '完成', '12', NULL, '{\"id\":12,\"createBy\":1,\"createTime\":\"2026-08-27T17:55:59\",\"updateBy\":1,\"updateTime\":\"2026-08-27T17:55:59\",\"isDelete\":0,\"companyId\":0,\"instanceNo\":\"FL202608271755592931\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"11\",\"title\":\"冲红申请-流水ID:11\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":1,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T17:55:59\",\"finishTime\":\"2026-08-27T17:56:29.8071876\"}', NULL, '2026-08-27 17:56:30');
INSERT INTO `sys_audit_log` VALUES (470, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'finance', '冲红审批通过', '11', NULL, NULL, NULL, '2026-08-27 17:56:30');
INSERT INTO `sys_audit_log` VALUES (471, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:56:47');
INSERT INTO `sys_audit_log` VALUES (472, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:57:32');
INSERT INTO `sys_audit_log` VALUES (473, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 17:58:19');
INSERT INTO `sys_audit_log` VALUES (474, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '冲红申请', '13', '{\"id\":13,\"companyId\":0,\"businessType\":\"water_elec\",\"billId\":\"3\",\"planId\":158,\"merchantId\":0,\"stallId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payerName\":null,\"payerPhone\":null,\"payerCompanyName\":null,\"payerType\":null,\"contractNo\":null,\"contractId\":null,\"originalAmount\":60.00,\"discountAmount\":0.00,\"realAmount\":60.00,\"payType\":3,\"flowType\":1,\"status\":1,\"flowStatus\":1,\"redFlushFlowId\":null,\"voidReason\":null,\"tradeNo\":null,\"flowNo\":\"YO0020260824000003\",\"remark\":\"水电物业缴费【冲红审批中：测试冲红修复】\",\"createBy\":0,\"createTime\":\"2026-08-24T16:23:38\"}', NULL, NULL, '2026-08-27 20:05:01');
INSERT INTO `sys_audit_log` VALUES (475, 0, 1, '集团超级管理员', '127.0.0.1', 'flow_engine', '提交', '13', NULL, '{\"id\":13,\"createBy\":1,\"createTime\":\"2026-08-27T20:05:00.7523456\",\"updateBy\":1,\"updateTime\":\"2026-08-27T20:05:00.7523456\",\"isDelete\":null,\"companyId\":0,\"instanceNo\":\"FL202608272005003408\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"13\",\"title\":\"冲红申请-流水ID:13\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":0,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T20:05:00.7523456\",\"finishTime\":null}', NULL, '2026-08-27 20:05:01');
INSERT INTO `sys_audit_log` VALUES (476, 0, 1, '集团超级管理员', '127.0.0.1', 'flow_engine', '完成', '13', NULL, '{\"id\":13,\"createBy\":1,\"createTime\":\"2026-08-27T20:05:01\",\"updateBy\":1,\"updateTime\":\"2026-08-27T20:05:01\",\"isDelete\":0,\"companyId\":0,\"instanceNo\":\"FL202608272005003408\",\"defId\":9,\"defName\":\"财务流水冲红审批\",\"bizType\":\"finance_red_flush\",\"sourceType\":\"finance_flow\",\"sourceId\":\"13\",\"title\":\"冲红申请-流水ID:13\",\"applyUserId\":1,\"applyUserName\":\"集团超级管理员\",\"instanceStatus\":1,\"currentNodeName\":\"冲红审批\",\"currentHandlers\":\"[1]\",\"submitTime\":\"2026-08-27T20:05:01\",\"finishTime\":\"2026-08-27T20:05:31.1305287\"}', NULL, '2026-08-27 20:05:31');
INSERT INTO `sys_audit_log` VALUES (477, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '冲红审批通过', '13', NULL, NULL, NULL, '2026-08-27 20:05:31');
INSERT INTO `sys_audit_log` VALUES (478, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:04:05');
INSERT INTO `sys_audit_log` VALUES (479, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:04:28');
INSERT INTO `sys_audit_log` VALUES (480, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:05:53');
INSERT INTO `sys_audit_log` VALUES (481, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:42:26');
INSERT INTO `sys_audit_log` VALUES (482, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:42:27');
INSERT INTO `sys_audit_log` VALUES (483, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:42:29');
INSERT INTO `sys_audit_log` VALUES (484, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:44:31');
INSERT INTO `sys_audit_log` VALUES (485, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:44:56');
INSERT INTO `sys_audit_log` VALUES (486, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:51:35');
INSERT INTO `sys_audit_log` VALUES (487, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:58:55');
INSERT INTO `sys_audit_log` VALUES (488, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 21:59:52');
INSERT INTO `sys_audit_log` VALUES (489, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 22:11:01');
INSERT INTO `sys_audit_log` VALUES (490, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 22:15:36');
INSERT INTO `sys_audit_log` VALUES (491, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-27 22:16:03');
INSERT INTO `sys_audit_log` VALUES (492, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '7', NULL, '{\"id\":7,\"createBy\":1,\"createTime\":\"2026-08-27T22:20:20.8200139\",\"updateBy\":1,\"updateTime\":\"2026-08-27T22:20:20.8200139\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":170.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-27 22:20:21');
INSERT INTO `sys_audit_log` VALUES (493, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-08-28T12:31:08.4749414\",\"updateBy\":1,\"updateTime\":\"2026-08-28T12:31:08.4749414\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":180.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-28 12:31:08');
INSERT INTO `sys_audit_log` VALUES (494, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 12:44:54');
INSERT INTO `sys_audit_log` VALUES (495, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:32:29');
INSERT INTO `sys_audit_log` VALUES (496, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:33:01');
INSERT INTO `sys_audit_log` VALUES (497, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:33:30');
INSERT INTO `sys_audit_log` VALUES (498, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '5', NULL, '{\"id\":5,\"createBy\":1,\"createTime\":\"2026-08-28T13:33:39.082567\",\"updateBy\":1,\"updateTime\":\"2026-08-28T13:33:39.082567\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-12\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":0,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":0.50,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-28 13:33:39');
INSERT INTO `sys_audit_log` VALUES (499, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:34:07');
INSERT INTO `sys_audit_log` VALUES (500, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:34:31');
INSERT INTO `sys_audit_log` VALUES (501, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:34:43');
INSERT INTO `sys_audit_log` VALUES (502, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:36:51');
INSERT INTO `sys_audit_log` VALUES (503, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:53:25');
INSERT INTO `sys_audit_log` VALUES (504, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:53:34');
INSERT INTO `sys_audit_log` VALUES (505, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:54:03');
INSERT INTO `sys_audit_log` VALUES (506, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 13:55:10');
INSERT INTO `sys_audit_log` VALUES (507, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-28T13:57:02.6302994\",\"updateBy\":1,\"updateTime\":\"2026-08-28T13:57:02.6302994\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":190.00,\"usage\":20.00,\"unitPrice\":6.00,\"totalAmount\":120.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-28 13:57:03');
INSERT INTO `sys_audit_log` VALUES (508, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '10', NULL, '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-08-28T13:58:06.7960578\",\"updateBy\":1,\"updateTime\":\"2026-08-28T13:58:06.7960578\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":210.00,\"usage\":40.00,\"unitPrice\":6.00,\"totalAmount\":240.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-28 13:58:07');
INSERT INTO `sys_audit_log` VALUES (509, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 14:02:02');
INSERT INTO `sys_audit_log` VALUES (510, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 14:02:28');
INSERT INTO `sys_audit_log` VALUES (511, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 16:20:49');
INSERT INTO `sys_audit_log` VALUES (512, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 16:32:43');
INSERT INTO `sys_audit_log` VALUES (513, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 16:34:48');
INSERT INTO `sys_audit_log` VALUES (514, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 16:47:48');
INSERT INTO `sys_audit_log` VALUES (515, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 16:48:03');
INSERT INTO `sys_audit_log` VALUES (516, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:12:32');
INSERT INTO `sys_audit_log` VALUES (517, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:27:31');
INSERT INTO `sys_audit_log` VALUES (518, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:36:40');
INSERT INTO `sys_audit_log` VALUES (519, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:36:56');
INSERT INTO `sys_audit_log` VALUES (520, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:37:16');
INSERT INTO `sys_audit_log` VALUES (521, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 18:39:05');
INSERT INTO `sys_audit_log` VALUES (522, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:07:28');
INSERT INTO `sys_audit_log` VALUES (523, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:07:35');
INSERT INTO `sys_audit_log` VALUES (524, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:07:44');
INSERT INTO `sys_audit_log` VALUES (525, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:12:48');
INSERT INTO `sys_audit_log` VALUES (526, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:12:48');
INSERT INTO `sys_audit_log` VALUES (527, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 19:12:57');
INSERT INTO `sys_audit_log` VALUES (528, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:17:02');
INSERT INTO `sys_audit_log` VALUES (529, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:17:48');
INSERT INTO `sys_audit_log` VALUES (530, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:39:12');
INSERT INTO `sys_audit_log` VALUES (531, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:46:31');
INSERT INTO `sys_audit_log` VALUES (532, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:47:24');
INSERT INTO `sys_audit_log` VALUES (533, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:48:18');
INSERT INTO `sys_audit_log` VALUES (534, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 20:49:10');
INSERT INTO `sys_audit_log` VALUES (535, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:06:26');
INSERT INTO `sys_audit_log` VALUES (536, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:07:20');
INSERT INTO `sys_audit_log` VALUES (537, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:28:01');
INSERT INTO `sys_audit_log` VALUES (538, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:28:07');
INSERT INTO `sys_audit_log` VALUES (539, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:30:03');
INSERT INTO `sys_audit_log` VALUES (540, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:30:20');
INSERT INTO `sys_audit_log` VALUES (541, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:31:04');
INSERT INTO `sys_audit_log` VALUES (542, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 21:32:02');
INSERT INTO `sys_audit_log` VALUES (543, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 22:38:47');
INSERT INTO `sys_audit_log` VALUES (544, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 22:40:51');
INSERT INTO `sys_audit_log` VALUES (545, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-28 22:44:55');
INSERT INTO `sys_audit_log` VALUES (546, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:38:54');
INSERT INTO `sys_audit_log` VALUES (547, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:39:25');
INSERT INTO `sys_audit_log` VALUES (548, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:41:02');
INSERT INTO `sys_audit_log` VALUES (549, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:41:26');
INSERT INTO `sys_audit_log` VALUES (550, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:44:18');
INSERT INTO `sys_audit_log` VALUES (551, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:44:30');
INSERT INTO `sys_audit_log` VALUES (552, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:45:03');
INSERT INTO `sys_audit_log` VALUES (553, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:45:45');
INSERT INTO `sys_audit_log` VALUES (554, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:46:44');
INSERT INTO `sys_audit_log` VALUES (555, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:47:50');
INSERT INTO `sys_audit_log` VALUES (556, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:50:34');
INSERT INTO `sys_audit_log` VALUES (557, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:53:16');
INSERT INTO `sys_audit_log` VALUES (558, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:54:54');
INSERT INTO `sys_audit_log` VALUES (559, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 07:55:41');
INSERT INTO `sys_audit_log` VALUES (560, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:01:02');
INSERT INTO `sys_audit_log` VALUES (561, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:01:02');
INSERT INTO `sys_audit_log` VALUES (562, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:01:07');
INSERT INTO `sys_audit_log` VALUES (563, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:02:10');
INSERT INTO `sys_audit_log` VALUES (564, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:10:17');
INSERT INTO `sys_audit_log` VALUES (565, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '24', NULL, '{\"id\":24,\"createBy\":1,\"createTime\":\"2026-08-30T08:10:16.7689647\",\"updateBy\":1,\"updateTime\":\"2026-08-30T08:10:16.7689647\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":250.00,\"usage\":50.00,\"unitPrice\":6.00,\"totalAmount\":300.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 08:10:17');
INSERT INTO `sys_audit_log` VALUES (566, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:24:07');
INSERT INTO `sys_audit_log` VALUES (567, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '31', NULL, '{\"id\":31,\"createBy\":1,\"createTime\":\"2026-08-30T08:24:07.6110994\",\"updateBy\":1,\"updateTime\":\"2026-08-30T08:24:07.6110994\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":300.00,\"usage\":50.00,\"unitPrice\":6.00,\"totalAmount\":300.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 08:24:08');
INSERT INTO `sys_audit_log` VALUES (568, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:24:19');
INSERT INTO `sys_audit_log` VALUES (569, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:24:30');
INSERT INTO `sys_audit_log` VALUES (570, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '32', NULL, '{\"id\":32,\"createBy\":1,\"createTime\":\"2026-08-30T08:24:29.9341007\",\"updateBy\":1,\"updateTime\":\"2026-08-30T08:24:29.9341007\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":250.00,\"usage\":50.00,\"unitPrice\":6.00,\"totalAmount\":300.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 08:24:30');
INSERT INTO `sys_audit_log` VALUES (571, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:27:27');
INSERT INTO `sys_audit_log` VALUES (572, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'waterElec', '新增', '33', NULL, '{\"id\":33,\"createBy\":1,\"createTime\":\"2026-08-30T08:27:27.1685557\",\"updateBy\":1,\"updateTime\":\"2026-08-30T08:27:27.1685557\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":250.00,\"usage\":50.00,\"unitPrice\":6.00,\"totalAmount\":300.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 08:27:27');
INSERT INTO `sys_audit_log` VALUES (573, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:31:02');
INSERT INTO `sys_audit_log` VALUES (574, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:31:25');
INSERT INTO `sys_audit_log` VALUES (575, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:31:37');
INSERT INTO `sys_audit_log` VALUES (576, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:32:51');
INSERT INTO `sys_audit_log` VALUES (577, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:32:56');
INSERT INTO `sys_audit_log` VALUES (578, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '37', NULL, '{\"id\":37,\"createBy\":1,\"createTime\":\"2026-08-30T08:33:06.408211\",\"updateBy\":1,\"updateTime\":\"2026-08-30T08:33:06.408211\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-08\",\"category\":3,\"prevMeterRead\":300.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 08:33:07');
INSERT INTO `sys_audit_log` VALUES (579, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:33:28');
INSERT INTO `sys_audit_log` VALUES (580, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:33:46');
INSERT INTO `sys_audit_log` VALUES (581, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:35:43');
INSERT INTO `sys_audit_log` VALUES (582, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:37:45');
INSERT INTO `sys_audit_log` VALUES (583, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:37:56');
INSERT INTO `sys_audit_log` VALUES (584, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:39:49');
INSERT INTO `sys_audit_log` VALUES (585, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:39:49');
INSERT INTO `sys_audit_log` VALUES (586, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 08:40:14');
INSERT INTO `sys_audit_log` VALUES (587, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 09:10:25');
INSERT INTO `sys_audit_log` VALUES (588, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '220', NULL, '\"核销金额=300.00，账单=water_elec/33\"', NULL, '2026-08-30 09:48:43');
INSERT INTO `sys_audit_log` VALUES (589, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '10', NULL, '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-08-30T09:48:42.8913498\",\"updateBy\":1,\"updateTime\":\"2026-08-30T09:48:42.8913498\",\"isDelete\":null,\"companyId\":0,\"billId\":33,\"stallId\":1,\"merchantId\":0,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payAmount\":300.00,\"payType\":3,\"requestId\":\"PAY1788054518248-giogeew5\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-08-30 09:48:43');
INSERT INTO `sys_audit_log` VALUES (590, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:36:53');
INSERT INTO `sys_audit_log` VALUES (591, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:38:52');
INSERT INTO `sys_audit_log` VALUES (592, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:40:33');
INSERT INTO `sys_audit_log` VALUES (593, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:41:19');
INSERT INTO `sys_audit_log` VALUES (594, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:42:24');
INSERT INTO `sys_audit_log` VALUES (595, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:46:26');
INSERT INTO `sys_audit_log` VALUES (596, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:46:45');
INSERT INTO `sys_audit_log` VALUES (597, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:47:22');
INSERT INTO `sys_audit_log` VALUES (598, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:47:51');
INSERT INTO `sys_audit_log` VALUES (599, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:49:28');
INSERT INTO `sys_audit_log` VALUES (600, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:53:24');
INSERT INTO `sys_audit_log` VALUES (601, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:53:24');
INSERT INTO `sys_audit_log` VALUES (602, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:54:21');
INSERT INTO `sys_audit_log` VALUES (603, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:55:29');
INSERT INTO `sys_audit_log` VALUES (604, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 13:55:40');
INSERT INTO `sys_audit_log` VALUES (605, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 16:28:55');
INSERT INTO `sys_audit_log` VALUES (606, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '16', NULL, '{\"id\":16,\"createBy\":1,\"createTime\":\"2026-08-30T16:30:21.8275468\",\"updateBy\":1,\"updateTime\":\"2026-08-30T16:30:21.8275468\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-12\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 16:30:22');
INSERT INTO `sys_audit_log` VALUES (607, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '46', NULL, '{\"id\":46,\"createBy\":1,\"createTime\":\"2026-08-30T16:56:42.7506223\",\"updateBy\":1,\"updateTime\":\"2026-08-30T16:56:42.7506223\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":310.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 16:56:43');
INSERT INTO `sys_audit_log` VALUES (608, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:20:09');
INSERT INTO `sys_audit_log` VALUES (609, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:21:42');
INSERT INTO `sys_audit_log` VALUES (610, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:22:50');
INSERT INTO `sys_audit_log` VALUES (611, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:24:40');
INSERT INTO `sys_audit_log` VALUES (612, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:38:37');
INSERT INTO `sys_audit_log` VALUES (613, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:38:49');
INSERT INTO `sys_audit_log` VALUES (614, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:38:49');
INSERT INTO `sys_audit_log` VALUES (615, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'propertyFee', '新增', '38', NULL, '{\"id\":38,\"createBy\":1,\"createTime\":\"2026-08-30T18:38:49.3693145\",\"updateBy\":1,\"updateTime\":\"2026-08-30T18:38:49.3693145\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-11\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 18:38:49');
INSERT INTO `sys_audit_log` VALUES (616, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:39:58');
INSERT INTO `sys_audit_log` VALUES (617, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:40:14');
INSERT INTO `sys_audit_log` VALUES (618, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:40:43');
INSERT INTO `sys_audit_log` VALUES (619, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'propertyFee', '新增', '39', NULL, '{\"id\":39,\"createBy\":1,\"createTime\":\"2026-08-30T18:40:43.4943743\",\"updateBy\":1,\"updateTime\":\"2026-08-30T18:40:43.4943743\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2027-01\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 18:40:44');
INSERT INTO `sys_audit_log` VALUES (620, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:41:37');
INSERT INTO `sys_audit_log` VALUES (621, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:44:28');
INSERT INTO `sys_audit_log` VALUES (622, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 18:46:21');
INSERT INTO `sys_audit_log` VALUES (623, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '40', NULL, '{\"id\":40,\"createBy\":1,\"createTime\":\"2026-08-30T18:46:35.6746378\",\"updateBy\":1,\"updateTime\":\"2026-08-30T18:46:35.6746378\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2027-02\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-08-30 18:46:36');
INSERT INTO `sys_audit_log` VALUES (624, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:36:22');
INSERT INTO `sys_audit_log` VALUES (625, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:36:32');
INSERT INTO `sys_audit_log` VALUES (626, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:37:43');
INSERT INTO `sys_audit_log` VALUES (627, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'writeoff', '缴费核销', '238', NULL, '\"核销金额=25.00，账单=property/38\"', NULL, '2026-08-30 19:37:44');
INSERT INTO `sys_audit_log` VALUES (628, 0, 1, '集团超级管理员', '0:0:0:0:0:0:0:1', 'propertyFee', '缴费', '11', NULL, '{\"id\":11,\"createBy\":1,\"createTime\":\"2026-08-30T19:37:43.3627101\",\"updateBy\":1,\"updateTime\":\"2026-08-30T19:37:43.3627101\",\"isDelete\":null,\"companyId\":0,\"billId\":38,\"stallId\":1,\"merchantId\":null,\"stallNumber\":null,\"stallName\":null,\"stallMarketName\":null,\"categoryName\":null,\"merchantName\":null,\"payAmount\":25.00,\"payType\":3,\"requestId\":\"TEST123456789\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"测试缴费\",\"flowNo\":null}', NULL, '2026-08-30 19:37:44');
INSERT INTO `sys_audit_log` VALUES (629, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:39:36');
INSERT INTO `sys_audit_log` VALUES (630, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:40:54');
INSERT INTO `sys_audit_log` VALUES (631, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:43:18');
INSERT INTO `sys_audit_log` VALUES (632, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:44:27');
INSERT INTO `sys_audit_log` VALUES (633, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:56:59');
INSERT INTO `sys_audit_log` VALUES (634, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:58:50');
INSERT INTO `sys_audit_log` VALUES (635, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 19:59:39');
INSERT INTO `sys_audit_log` VALUES (636, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 20:10:45');
INSERT INTO `sys_audit_log` VALUES (637, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '240', NULL, '\"核销金额=25.00，账单=property/40\"', NULL, '2026-08-30 20:35:55');
INSERT INTO `sys_audit_log` VALUES (638, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '缴费', '12', NULL, '{\"id\":12,\"createBy\":1,\"createTime\":\"2026-08-30T20:35:54.2452053\",\"updateBy\":1,\"updateTime\":\"2026-08-30T20:35:54.2452053\",\"isDelete\":null,\"companyId\":0,\"billId\":40,\"stallId\":1,\"merchantId\":null,\"stallNumber\":null,\"stallName\":null,\"stallMarketName\":null,\"categoryName\":null,\"merchantName\":null,\"payAmount\":25.00,\"payType\":3,\"requestId\":\"PAY1788093350088-qkl1sawy\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-08-30 20:35:55');
INSERT INTO `sys_audit_log` VALUES (639, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 22:56:23');
INSERT INTO `sys_audit_log` VALUES (640, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 23:07:36');
INSERT INTO `sys_audit_log` VALUES (641, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 23:36:34');
INSERT INTO `sys_audit_log` VALUES (642, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-30 23:36:46');
INSERT INTO `sys_audit_log` VALUES (643, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '239', NULL, '\"核销金额=25.00，账单=property/39\"', NULL, '2026-08-30 23:38:35');
INSERT INTO `sys_audit_log` VALUES (644, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '缴费', '13', NULL, '{\"id\":13,\"createBy\":1,\"createTime\":\"2026-08-30T23:38:35.0384368\",\"updateBy\":1,\"updateTime\":\"2026-08-30T23:38:35.0384368\",\"isDelete\":null,\"companyId\":0,\"billId\":39,\"stallId\":1,\"merchantId\":null,\"stallNumber\":null,\"stallName\":null,\"stallMarketName\":null,\"categoryName\":null,\"merchantName\":null,\"payAmount\":25.00,\"payType\":3,\"requestId\":\"PAY1788104274592-y7omzzqx\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-08-30 23:38:35');
INSERT INTO `sys_audit_log` VALUES (645, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 00:34:48');
INSERT INTO `sys_audit_log` VALUES (646, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 00:34:56');
INSERT INTO `sys_audit_log` VALUES (647, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 00:35:27');
INSERT INTO `sys_audit_log` VALUES (648, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 08:30:47');
INSERT INTO `sys_audit_log` VALUES (649, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 08:30:58');
INSERT INTO `sys_audit_log` VALUES (650, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '14', NULL, '{\"id\":14,\"createBy\":1,\"createTime\":\"2026-08-31T09:26:25.5293173\",\"updateBy\":1,\"updateTime\":\"2026-08-31T09:26:25.5293173\",\"isDelete\":null,\"companyId\":0,\"billId\":37,\"stallId\":1,\"merchantId\":0,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payAmount\":60.00,\"payType\":3,\"requestId\":\"PAY1788139584168-vg7adk0l\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-08-31 09:26:26');
INSERT INTO `sys_audit_log` VALUES (651, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '10', '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-08-24T07:25:17\",\"updateBy\":1,\"updateTime\":\"2026-08-24T07:25:17\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608240725173862\",\"merchantId\":null,\"tenantId\":1,\"stallId\":2,\"rentAmount\":1000.00,\"depositAmount\":1000.00,\"startTime\":\"2026-08-24\",\"endTime\":\"2027-08-24\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":10,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T09:27:59.5907954\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 09:28:00');
INSERT INTO `sys_audit_log` VALUES (652, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '9', '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-08-21T19:05:26\",\"updateBy\":1,\"updateTime\":\"2026-08-21T19:05:26\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608211905266097\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-21\",\"endTime\":\"2027-08-21\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":9,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T09:28:01.90451\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 09:28:02');
INSERT INTO `sys_audit_log` VALUES (653, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '11', NULL, '{\"id\":11,\"createBy\":1,\"createTime\":\"2026-08-31T09:28:47.9637366\",\"updateBy\":1,\"updateTime\":\"2026-08-31T09:28:47.9637366\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608310928476108\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 09:28:48');
INSERT INTO `sys_audit_log` VALUES (654, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '242', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (655, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '243', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (656, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '244', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (657, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '245', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (658, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '246', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (659, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '247', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (660, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '248', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (661, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '249', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (662, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '250', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (663, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '251', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (664, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '252', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (665, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '253', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (666, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '254', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (667, 0, 1, '集团超级管理员', '127.0.0.1', 'recv_pay_plan', '红冲', '255', NULL, '\"红冲链完成：作废计划+反向冲销计划+负向核销+反向流水\"', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (668, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '退租', '11', '{\"id\":11,\"createBy\":1,\"createTime\":\"2026-08-31T09:28:48\",\"updateBy\":1,\"updateTime\":\"2026-08-31T09:28:48\",\"isDelete\":0,\"companyId\":0,\"contractNo\":\"HT202608310928476108\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000.00,\"depositAmount\":0.00,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":1,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', '{\"id\":11,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T11:20:04.4694476\",\"isDelete\":null,\"companyId\":null,\"contractNo\":null,\"merchantId\":null,\"tenantId\":null,\"stallId\":null,\"rentAmount\":null,\"depositAmount\":null,\"startTime\":null,\"endTime\":null,\"contractStatus\":2,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 11:20:05');
INSERT INTO `sys_audit_log` VALUES (669, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '12', NULL, '{\"id\":12,\"createBy\":1,\"createTime\":\"2026-08-31T11:20:21.6865467\",\"updateBy\":1,\"updateTime\":\"2026-08-31T11:20:21.6865467\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311120219601\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 11:20:22');
INSERT INTO `sys_audit_log` VALUES (670, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 12:43:53');
INSERT INTO `sys_audit_log` VALUES (671, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 12:45:10');
INSERT INTO `sys_audit_log` VALUES (672, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":8,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":9,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":10,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":11,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-31 12:46:53');
INSERT INTO `sys_audit_log` VALUES (673, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-31T11:20:22\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":1,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T12:46:52.873114\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-31 12:46:53');
INSERT INTO `sys_audit_log` VALUES (674, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '13', NULL, '{\"id\":13,\"createBy\":1,\"createTime\":\"2026-08-31T12:47:11.4301489\",\"updateBy\":1,\"updateTime\":\"2026-08-31T12:47:11.4301489\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311247118124\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 12:47:12');
INSERT INTO `sys_audit_log` VALUES (675, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":25,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":26,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":27,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":28,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-31 12:48:10');
INSERT INTO `sys_audit_log` VALUES (676, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-31T12:47:11\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":1,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T12:48:09.6032622\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-31 12:48:10');
INSERT INTO `sys_audit_log` VALUES (677, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '14', NULL, '{\"id\":14,\"createBy\":1,\"createTime\":\"2026-08-31T12:48:37.1015497\",\"updateBy\":1,\"updateTime\":\"2026-08-31T12:48:37.1015497\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311248370138\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":0,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 12:48:37');
INSERT INTO `sys_audit_log` VALUES (678, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 13:36:40');
INSERT INTO `sys_audit_log` VALUES (679, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 13:36:53');
INSERT INTO `sys_audit_log` VALUES (680, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 13:38:54');
INSERT INTO `sys_audit_log` VALUES (681, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 13:59:30');
INSERT INTO `sys_audit_log` VALUES (682, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 13:59:55');
INSERT INTO `sys_audit_log` VALUES (683, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:21:28');
INSERT INTO `sys_audit_log` VALUES (684, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:21:43');
INSERT INTO `sys_audit_log` VALUES (685, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":29,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":30,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":31,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":32,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1},{\"id\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-31 17:22:01');
INSERT INTO `sys_audit_log` VALUES (686, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-31T12:48:37\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":1,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T17:22:00.9892914\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":1,\"remark\":\"测试\"}', NULL, '2026-08-31 17:22:01');
INSERT INTO `sys_audit_log` VALUES (687, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":33,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":34,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":35,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":36,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00},{\"relId\":37,\"ruleId\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1},{\"id\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-31 17:23:29');
INSERT INTO `sys_audit_log` VALUES (688, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-31T17:22:01\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":1,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T17:23:28.9827191\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-31 17:23:29');
INSERT INTO `sys_audit_log` VALUES (689, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '15', NULL, '{\"id\":15,\"createBy\":1,\"createTime\":\"2026-08-31T17:23:50.2443312\",\"updateBy\":1,\"updateTime\":\"2026-08-31T17:23:50.2443312\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311723502046\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 17:23:51');
INSERT INTO `sys_audit_log` VALUES (690, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:39:15');
INSERT INTO `sys_audit_log` VALUES (691, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:40:59');
INSERT INTO `sys_audit_log` VALUES (692, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:55:29');
INSERT INTO `sys_audit_log` VALUES (693, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 17:56:02');
INSERT INTO `sys_audit_log` VALUES (694, 0, 1, '集团超级管理员', '127.0.0.1', 'fee_rule', '绑定', '1', '[{\"relId\":38,\"ruleId\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":39,\"ruleId\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50},{\"relId\":40,\"ruleId\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50},{\"relId\":41,\"ruleId\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00},{\"relId\":42,\"ruleId\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00}]', '[{\"id\":2,\"ruleName\":\"电费\",\"feeItemId\":4,\"feeItemName\":\"电费\",\"categoryType\":4,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":0.50,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":3,\"ruleName\":\"租金1\",\"feeItemId\":1,\"feeItemName\":\"租金\",\"categoryType\":1,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":2000.00,\"periodType\":1,\"periodTypeText\":\"按年\",\"overdueRate\":0.50,\"status\":1},{\"id\":4,\"ruleName\":\"水费\",\"feeItemId\":3,\"feeItemName\":\"水费\",\"categoryType\":3,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":6.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.50,\"status\":1},{\"id\":5,\"ruleName\":\"物业费\",\"feeItemId\":2,\"feeItemName\":\"物业费\",\"categoryType\":2,\"calcMode\":2,\"calcModeText\":\"按面积\",\"price\":0.50,\"periodType\":2,\"periodTypeText\":\"按月\",\"overdueRate\":0.00,\"status\":1},{\"id\":6,\"ruleName\":\"押金\",\"feeItemId\":5,\"feeItemName\":\"押金\",\"categoryType\":5,\"calcMode\":1,\"calcModeText\":\"定额\",\"price\":1000.00,\"periodType\":0,\"periodTypeText\":\"不使用周期\",\"overdueRate\":0.00,\"status\":1}]', NULL, '2026-08-31 17:57:07');
INSERT INTO `sys_audit_log` VALUES (695, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_lease', '编辑', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-18T18:31:52\",\"updateBy\":1,\"updateTime\":\"2026-08-31T17:23:50\",\"isDelete\":0,\"companyId\":0,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50.00,\"status\":1,\"remark\":\"测试\"}', '{\"id\":1,\"createBy\":null,\"createTime\":null,\"updateBy\":1,\"updateTime\":\"2026-08-31T17:57:06.692021\",\"isDelete\":null,\"companyId\":null,\"marketId\":1,\"stallCategoryId\":3,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallArea\":50,\"status\":0,\"remark\":\"测试\"}', NULL, '2026-08-31 17:57:07');
INSERT INTO `sys_audit_log` VALUES (696, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '16', NULL, '{\"id\":16,\"createBy\":1,\"createTime\":\"2026-08-31T17:57:28.3518707\",\"updateBy\":1,\"updateTime\":\"2026-08-31T17:57:28.3518707\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311757285693\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 17:57:29');
INSERT INTO `sys_audit_log` VALUES (697, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 18:11:06');
INSERT INTO `sys_audit_log` VALUES (698, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 18:11:49');
INSERT INTO `sys_audit_log` VALUES (699, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '17', NULL, '{\"id\":17,\"createBy\":1,\"createTime\":\"2026-08-31T18:12:56.8546457\",\"updateBy\":1,\"updateTime\":\"2026-08-31T18:12:56.8546457\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311812569778\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 18:12:57');
INSERT INTO `sys_audit_log` VALUES (700, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 18:41:16');
INSERT INTO `sys_audit_log` VALUES (701, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 18:41:46');
INSERT INTO `sys_audit_log` VALUES (702, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '18', NULL, '{\"id\":18,\"createBy\":1,\"createTime\":\"2026-08-31T18:44:26.2590256\",\"updateBy\":1,\"updateTime\":\"2026-08-31T18:44:26.2590256\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608311844269689\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 18:44:27');
INSERT INTO `sys_audit_log` VALUES (703, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 18:59:55');
INSERT INTO `sys_audit_log` VALUES (704, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-08-31 21:25:31');
INSERT INTO `sys_audit_log` VALUES (705, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '22', NULL, '{\"id\":22,\"createBy\":1,\"createTime\":\"2026-08-31T21:27:27.8483188\",\"updateBy\":1,\"updateTime\":\"2026-08-31T21:27:27.8483188\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202608312127275432\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-08-31\",\"endTime\":\"2027-08-31\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-08-31 21:27:28');
INSERT INTO `sys_audit_log` VALUES (706, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-01 07:55:36');
INSERT INTO `sys_audit_log` VALUES (707, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-01 07:57:08');
INSERT INTO `sys_audit_log` VALUES (708, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '新增', '23', NULL, '{\"id\":23,\"createBy\":1,\"createTime\":\"2026-09-01T07:57:57.4509853\",\"updateBy\":1,\"updateTime\":\"2026-09-01T07:57:57.4509853\",\"isDelete\":null,\"companyId\":0,\"contractNo\":\"HT202609010757577682\",\"merchantId\":null,\"tenantId\":1,\"stallId\":1,\"rentAmount\":2000,\"depositAmount\":1000,\"startTime\":\"2026-09-01\",\"endTime\":\"2027-09-01\",\"contractStatus\":0,\"flowInstanceId\":null,\"attachmentUrl\":null,\"remark\":\"\"}', NULL, '2026-09-01 07:57:58');
INSERT INTO `sys_audit_log` VALUES (709, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-01 08:06:17');
INSERT INTO `sys_audit_log` VALUES (710, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-01 08:06:24');
INSERT INTO `sys_audit_log` VALUES (711, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-01 08:06:25');
INSERT INTO `sys_audit_log` VALUES (712, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 08:35:52');
INSERT INTO `sys_audit_log` VALUES (713, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '缴费', '18', NULL, '{\"id\":18,\"createBy\":1,\"createTime\":\"2026-09-03T09:53:07.6257982\",\"updateBy\":1,\"updateTime\":\"2026-09-03T09:53:07.6257982\",\"isDelete\":null,\"companyId\":0,\"billId\":2,\"stallId\":1,\"merchantId\":0,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":null,\"payAmount\":25.00,\"payType\":3,\"requestId\":\"PAY1788400386573-s8a9k1i0\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-03 09:53:08');
INSERT INTO `sys_audit_log` VALUES (714, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '缴费', '59', NULL, '{\"id\":19,\"createBy\":1,\"createTime\":\"2026-09-03T09:53:20.2845261\",\"updateBy\":1,\"updateTime\":\"2026-09-03T09:53:20.2845261\",\"isDelete\":null,\"companyId\":0,\"billId\":59,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":2000.00,\"payType\":3,\"requestId\":\"PAY1788400398725-vnhdr8zw\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-03 09:53:20');
INSERT INTO `sys_audit_log` VALUES (715, 0, 1, '集团超级管理员', '127.0.0.1', 'stall_contract', '缴费', '58', NULL, '{\"id\":20,\"createBy\":1,\"createTime\":\"2026-09-03T14:58:27.2436624\",\"updateBy\":1,\"updateTime\":\"2026-09-03T14:58:27.2436624\",\"isDelete\":null,\"companyId\":0,\"billId\":58,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":1000.00,\"payType\":3,\"requestId\":\"PAY1788418705907-o99ccsx1\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-03 14:58:27');
INSERT INTO `sys_audit_log` VALUES (716, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 17:18:28');
INSERT INTO `sys_audit_log` VALUES (717, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 17:19:20');
INSERT INTO `sys_audit_log` VALUES (718, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 17:19:20');
INSERT INTO `sys_audit_log` VALUES (719, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 17:19:33');
INSERT INTO `sys_audit_log` VALUES (720, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '41', NULL, '{\"id\":41,\"createBy\":1,\"createTime\":\"2026-09-03T17:21:35.8903427\",\"updateBy\":1,\"updateTime\":\"2026-09-03T17:21:35.8903427\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-01\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-03 17:21:36');
INSERT INTO `sys_audit_log` VALUES (721, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '42', NULL, '{\"id\":42,\"createBy\":1,\"createTime\":\"2026-09-03T17:22:17.8719715\",\"updateBy\":1,\"updateTime\":\"2026-09-03T17:22:17.8719715\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2027-04\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-03 17:22:18');
INSERT INTO `sys_audit_log` VALUES (722, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '1', NULL, '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-09-03T18:19:11\",\"updateBy\":1,\"updateTime\":\"2026-09-03T18:19:11\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260903000001\",\"sourceType\":\"fee_bill\",\"sourceId\":61,\"stallId\":1,\"merchantId\":null,\"totalAmount\":50.00,\"paidAmount\":0.00,\"unpaidAmount\":50.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-03 18:19:12');
INSERT INTO `sys_audit_log` VALUES (723, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '2', NULL, '{\"id\":2,\"createBy\":1,\"createTime\":\"2026-09-03T18:19:28\",\"updateBy\":1,\"updateTime\":\"2026-09-03T18:19:28\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260903000002\",\"sourceType\":\"fee_bill\",\"sourceId\":61,\"stallId\":1,\"merchantId\":null,\"totalAmount\":50.00,\"paidAmount\":0.00,\"unpaidAmount\":50.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-03 18:19:28');
INSERT INTO `sys_audit_log` VALUES (724, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '3', NULL, '{\"id\":3,\"createBy\":1,\"createTime\":\"2026-09-03T18:20:41\",\"updateBy\":1,\"updateTime\":\"2026-09-03T18:20:41\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260903000003\",\"sourceType\":\"fee_bill\",\"sourceId\":61,\"stallId\":1,\"merchantId\":null,\"totalAmount\":50.00,\"paidAmount\":0.00,\"unpaidAmount\":50.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-03 18:20:41');
INSERT INTO `sys_audit_log` VALUES (725, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:39:58');
INSERT INTO `sys_audit_log` VALUES (726, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:54:23');
INSERT INTO `sys_audit_log` VALUES (727, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:54:40');
INSERT INTO `sys_audit_log` VALUES (728, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:54:59');
INSERT INTO `sys_audit_log` VALUES (729, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:55:06');
INSERT INTO `sys_audit_log` VALUES (730, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 19:55:50');
INSERT INTO `sys_audit_log` VALUES (731, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 20:39:14');
INSERT INTO `sys_audit_log` VALUES (732, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 20:39:18');
INSERT INTO `sys_audit_log` VALUES (733, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 20:45:53');
INSERT INTO `sys_audit_log` VALUES (734, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-03 20:46:08');
INSERT INTO `sys_audit_log` VALUES (735, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '8', NULL, '{\"id\":8,\"createBy\":1,\"createTime\":\"2026-09-03T20:51:13\",\"updateBy\":1,\"updateTime\":\"2026-09-03T20:51:13\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260903000004\",\"sourceType\":\"fee_bill\",\"sourceId\":61,\"stallId\":1,\"merchantId\":null,\"totalAmount\":50.00,\"paidAmount\":0.00,\"unpaidAmount\":50.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-03 20:51:13');
INSERT INTO `sys_audit_log` VALUES (736, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:22:08');
INSERT INTO `sys_audit_log` VALUES (737, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:22:10');
INSERT INTO `sys_audit_log` VALUES (738, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:23:23');
INSERT INTO `sys_audit_log` VALUES (739, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '47', NULL, '{\"id\":47,\"createBy\":1,\"createTime\":\"2026-09-04T07:28:22.2768719\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:28:22.2768719\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":320.00,\"usage\":40.00,\"unitPrice\":6.00,\"totalAmount\":240.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 07:28:23');
INSERT INTO `sys_audit_log` VALUES (740, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '43', NULL, '{\"id\":43,\"createBy\":1,\"createTime\":\"2026-09-04T07:28:56.8551339\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:28:56.8551339\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2027-05\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 07:28:57');
INSERT INTO `sys_audit_log` VALUES (741, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '9', NULL, '{\"id\":9,\"createBy\":1,\"createTime\":\"2026-09-04T07:29:30\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:29:30\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260904000001\",\"sourceType\":\"fee_bill\",\"sourceId\":63,\"stallId\":1,\"merchantId\":null,\"totalAmount\":265.00,\"paidAmount\":0.00,\"unpaidAmount\":265.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-04 07:29:30');
INSERT INTO `sys_audit_log` VALUES (742, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:42:58');
INSERT INTO `sys_audit_log` VALUES (743, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:43:02');
INSERT INTO `sys_audit_log` VALUES (744, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 07:44:08');
INSERT INTO `sys_audit_log` VALUES (745, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '48', NULL, '{\"id\":48,\"createBy\":1,\"createTime\":\"2026-09-04T07:53:19.2576538\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:53:19.2576538\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":0.00,\"usage\":15.00,\"unitPrice\":6.00,\"totalAmount\":90.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 07:53:19');
INSERT INTO `sys_audit_log` VALUES (746, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '44', NULL, '{\"id\":44,\"createBy\":1,\"createTime\":\"2026-09-04T07:53:34.3728322\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:53:34.3728322\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-09\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 07:53:34');
INSERT INTO `sys_audit_log` VALUES (747, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '10', NULL, '{\"id\":10,\"createBy\":1,\"createTime\":\"2026-09-04T07:54:08\",\"updateBy\":1,\"updateTime\":\"2026-09-04T07:54:08\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260904000002\",\"sourceType\":\"fee_bill\",\"sourceId\":65,\"stallId\":1,\"merchantId\":null,\"totalAmount\":115.00,\"paidAmount\":0.00,\"unpaidAmount\":115.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-04 07:54:09');
INSERT INTO `sys_audit_log` VALUES (748, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 08:57:34');
INSERT INTO `sys_audit_log` VALUES (749, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 09:45:32');
INSERT INTO `sys_audit_log` VALUES (750, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '45', NULL, '{\"id\":45,\"createBy\":1,\"createTime\":\"2026-09-04T14:55:33.3336004\",\"updateBy\":1,\"updateTime\":\"2026-09-04T14:55:33.3336004\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"billMonth\":\"2026-10\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 14:55:33');
INSERT INTO `sys_audit_log` VALUES (751, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 15:03:38');
INSERT INTO `sys_audit_log` VALUES (752, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '53', NULL, '{\"id\":53,\"createBy\":1,\"createTime\":\"2026-09-04T15:04:10.0222517\",\"updateBy\":1,\"updateTime\":\"2026-09-04T15:04:10.0222517\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":null,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":15.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 15:04:10');
INSERT INTO `sys_audit_log` VALUES (753, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 15:11:33');
INSERT INTO `sys_audit_log` VALUES (754, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 15:32:53');
INSERT INTO `sys_audit_log` VALUES (755, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 15:36:30');
INSERT INTO `sys_audit_log` VALUES (756, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 16:24:33');
INSERT INTO `sys_audit_log` VALUES (757, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '抄表', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:29\",\"updateBy\":1,\"updateTime\":\"2026-09-04T15:04:10\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"0000a1\",\"meterType\":1,\"gatewayCode\":\"\",\"currentRead\":25.00,\"balanceAmount\":0.00,\"status\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:29\",\"updateBy\":1,\"updateTime\":\"2026-09-04T15:04:10\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"0000a1\",\"meterType\":1,\"gatewayCode\":\"\",\"currentRead\":35,\"balanceAmount\":0.00,\"status\":1}', NULL, '2026-09-04 16:25:03');
INSERT INTO `sys_audit_log` VALUES (758, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '抄表', '1', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:29\",\"updateBy\":1,\"updateTime\":\"2026-09-04T15:04:10\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"0000a1\",\"meterType\":1,\"gatewayCode\":\"\",\"currentRead\":35.00,\"balanceAmount\":0.00,\"status\":1}', '{\"id\":1,\"createBy\":1,\"createTime\":\"2026-08-19T14:05:29\",\"updateBy\":1,\"updateTime\":\"2026-09-04T15:04:10\",\"isDelete\":0,\"companyId\":0,\"stallId\":1,\"meterNo\":\"0000a1\",\"meterType\":1,\"gatewayCode\":\"\",\"currentRead\":40,\"balanceAmount\":0.00,\"status\":1}', NULL, '2026-09-04 16:25:24');
INSERT INTO `sys_audit_log` VALUES (759, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 17:49:38');
INSERT INTO `sys_audit_log` VALUES (760, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '54', NULL, '{\"id\":54,\"createBy\":1,\"createTime\":\"2026-09-04T17:50:06.0287976\",\"updateBy\":1,\"updateTime\":\"2026-09-04T17:50:06.0287976\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":1,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":40.00,\"usage\":5.00,\"unitPrice\":6.00,\"totalAmount\":30.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 17:50:06');
INSERT INTO `sys_audit_log` VALUES (761, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:29:47');
INSERT INTO `sys_audit_log` VALUES (762, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:30:25');
INSERT INTO `sys_audit_log` VALUES (763, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '新增', '55', NULL, '{\"id\":55,\"createBy\":1,\"createTime\":\"2026-09-04T18:31:11.5557684\",\"updateBy\":1,\"updateTime\":\"2026-09-04T18:31:11.5557684\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":1,\"contractId\":23,\"planId\":null,\"billMonth\":\"2026-09\",\"category\":3,\"prevMeterRead\":45.00,\"usage\":10.00,\"unitPrice\":6.00,\"totalAmount\":60.0000,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-04 18:31:12');
INSERT INTO `sys_audit_log` VALUES (764, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:37:28');
INSERT INTO `sys_audit_log` VALUES (765, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:37:28');
INSERT INTO `sys_audit_log` VALUES (766, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:37:37');
INSERT INTO `sys_audit_log` VALUES (767, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:40:28');
INSERT INTO `sys_audit_log` VALUES (768, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:40:53');
INSERT INTO `sys_audit_log` VALUES (769, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:41:52');
INSERT INTO `sys_audit_log` VALUES (770, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:55:53');
INSERT INTO `sys_audit_log` VALUES (771, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 18:59:15');
INSERT INTO `sys_audit_log` VALUES (772, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:03:37');
INSERT INTO `sys_audit_log` VALUES (773, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:03:37');
INSERT INTO `sys_audit_log` VALUES (774, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:03:37');
INSERT INTO `sys_audit_log` VALUES (775, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:03:47');
INSERT INTO `sys_audit_log` VALUES (776, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:04:13');
INSERT INTO `sys_audit_log` VALUES (777, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:04:13');
INSERT INTO `sys_audit_log` VALUES (778, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:06:44');
INSERT INTO `sys_audit_log` VALUES (779, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:06:44');
INSERT INTO `sys_audit_log` VALUES (780, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:06:44');
INSERT INTO `sys_audit_log` VALUES (781, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:07:02');
INSERT INTO `sys_audit_log` VALUES (782, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:14:46');
INSERT INTO `sys_audit_log` VALUES (783, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:15:04');
INSERT INTO `sys_audit_log` VALUES (784, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:15:17');
INSERT INTO `sys_audit_log` VALUES (785, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:15:35');
INSERT INTO `sys_audit_log` VALUES (786, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:15:53');
INSERT INTO `sys_audit_log` VALUES (787, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:16:40');
INSERT INTO `sys_audit_log` VALUES (788, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '462', NULL, '\"核销金额=90.00，账单=water_elec/48\"', NULL, '2026-09-04 22:21:00');
INSERT INTO `sys_audit_log` VALUES (789, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '21', NULL, '{\"id\":21,\"createBy\":1,\"createTime\":\"2026-09-04T22:20:59.9905933\",\"updateBy\":1,\"updateTime\":\"2026-09-04T22:20:59.9905933\",\"isDelete\":null,\"companyId\":0,\"billId\":48,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":90.00,\"payType\":3,\"requestId\":\"PAY1788531656032-54wo9zaq\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-04 22:21:00');
INSERT INTO `sys_audit_log` VALUES (790, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '463', NULL, '\"核销金额=25.00，账单=property/44\"', NULL, '2026-09-04 22:21:15');
INSERT INTO `sys_audit_log` VALUES (791, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '缴费', '22', NULL, '{\"id\":22,\"createBy\":1,\"createTime\":\"2026-09-04T22:21:14.3919698\",\"updateBy\":1,\"updateTime\":\"2026-09-04T22:21:14.3919698\",\"isDelete\":null,\"companyId\":0,\"billId\":44,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":25.00,\"payType\":3,\"requestId\":\"PAY1788531672342-cy3vfp0u\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-04 22:21:15');
INSERT INTO `sys_audit_log` VALUES (792, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 22:26:37');
INSERT INTO `sys_audit_log` VALUES (793, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:09:04');
INSERT INTO `sys_audit_log` VALUES (794, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:09:17');
INSERT INTO `sys_audit_log` VALUES (795, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:09:29');
INSERT INTO `sys_audit_log` VALUES (796, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:09:38');
INSERT INTO `sys_audit_log` VALUES (797, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:10:01');
INSERT INTO `sys_audit_log` VALUES (798, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:10:01');
INSERT INTO `sys_audit_log` VALUES (799, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-04 23:11:01');
INSERT INTO `sys_audit_log` VALUES (800, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:27:32');
INSERT INTO `sys_audit_log` VALUES (801, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:27:44');
INSERT INTO `sys_audit_log` VALUES (802, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:28:29');
INSERT INTO `sys_audit_log` VALUES (803, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '470', NULL, '\"核销金额=60.00，账单=water_elec/53\"', NULL, '2026-09-05 00:37:27');
INSERT INTO `sys_audit_log` VALUES (804, 0, 1, '集团超级管理员', '127.0.0.1', 'waterElec', '缴费', '28', NULL, '{\"id\":28,\"createBy\":1,\"createTime\":\"2026-09-05T00:37:27.1049801\",\"updateBy\":1,\"updateTime\":\"2026-09-05T00:37:27.1049801\",\"isDelete\":null,\"companyId\":0,\"billId\":53,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":60.00,\"payType\":3,\"requestId\":\"PAY1788539845846-vhh91gw9\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-05 00:37:27');
INSERT INTO `sys_audit_log` VALUES (805, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:37:28');
INSERT INTO `sys_audit_log` VALUES (806, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:37:45');
INSERT INTO `sys_audit_log` VALUES (807, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 00:37:55');
INSERT INTO `sys_audit_log` VALUES (808, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:33:30');
INSERT INTO `sys_audit_log` VALUES (809, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:33:38');
INSERT INTO `sys_audit_log` VALUES (810, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:39:01');
INSERT INTO `sys_audit_log` VALUES (811, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:45:05');
INSERT INTO `sys_audit_log` VALUES (812, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:45:18');
INSERT INTO `sys_audit_log` VALUES (813, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:46:11');
INSERT INTO `sys_audit_log` VALUES (814, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 07:47:31');
INSERT INTO `sys_audit_log` VALUES (815, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '46', NULL, '{\"id\":46,\"createBy\":1,\"createTime\":\"2026-09-05T07:48:47.5867494\",\"updateBy\":1,\"updateTime\":\"2026-09-05T07:48:47.5867494\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":1,\"contractId\":23,\"billMonth\":\"2026-11\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-05 07:48:48');
INSERT INTO `sys_audit_log` VALUES (816, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '新增', '47', NULL, '{\"id\":47,\"createBy\":1,\"createTime\":\"2026-09-05T07:48:59.4289953\",\"updateBy\":1,\"updateTime\":\"2026-09-05T07:48:59.4289953\",\"isDelete\":null,\"companyId\":0,\"stallId\":1,\"merchantId\":1,\"contractId\":23,\"billMonth\":\"2026-12\",\"ruleId\":5,\"feeItemId\":2,\"calcMode\":2,\"periodType\":2,\"usage\":50.00,\"unitPrice\":0.50,\"periodFactor\":1,\"amount\":25.00,\"planId\":null,\"payStatus\":0,\"payTime\":null}', NULL, '2026-09-05 07:48:59');
INSERT INTO `sys_audit_log` VALUES (817, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '477', NULL, '\"核销金额=140.00，账单=fee_bill/76\"', NULL, '2026-09-05 07:49:22');
INSERT INTO `sys_audit_log` VALUES (818, 0, 1, '集团超级管理员', '127.0.0.1', 'finance', '缴费', '12', NULL, '{\"id\":12,\"createBy\":1,\"createTime\":\"2026-09-05T07:49:22\",\"updateBy\":1,\"updateTime\":\"2026-09-05T07:49:22\",\"isDelete\":0,\"companyId\":0,\"payBillNo\":\"PY0020260905000004\",\"sourceType\":\"fee_bill\",\"sourceId\":76,\"stallId\":1,\"merchantId\":null,\"totalAmount\":140.00,\"paidAmount\":0.00,\"unpaidAmount\":140.00,\"payStatus\":0,\"payTime\":null,\"remark\":\"\"}', NULL, '2026-09-05 07:49:22');
INSERT INTO `sys_audit_log` VALUES (819, 0, 1, '集团超级管理员', '127.0.0.1', 'writeoff', '缴费核销', '480', NULL, '\"核销金额=25.00，账单=property/46\"', NULL, '2026-09-05 07:49:39');
INSERT INTO `sys_audit_log` VALUES (820, 0, 1, '集团超级管理员', '127.0.0.1', 'propertyFee', '缴费', '29', NULL, '{\"id\":29,\"createBy\":1,\"createTime\":\"2026-09-05T07:49:38.5045715\",\"updateBy\":1,\"updateTime\":\"2026-09-05T07:49:38.5045715\",\"isDelete\":null,\"companyId\":0,\"billId\":46,\"stallId\":1,\"merchantId\":1,\"stallNumber\":\"001\",\"stallName\":\"铺位名称\",\"stallMarketName\":\"默认市场\",\"categoryName\":\"车位\",\"merchantName\":\"测试优惠租户\",\"payAmount\":25.00,\"payType\":3,\"requestId\":\"PAY1788565777181-vnstgfis\",\"recordType\":1,\"refundStatus\":0,\"refundTime\":null,\"refundRecordId\":null,\"remark\":\"\",\"flowNo\":null}', NULL, '2026-09-05 07:49:39');
INSERT INTO `sys_audit_log` VALUES (821, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:07:41');
INSERT INTO `sys_audit_log` VALUES (822, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:20:42');
INSERT INTO `sys_audit_log` VALUES (823, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:20:56');
INSERT INTO `sys_audit_log` VALUES (824, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:22:56');
INSERT INTO `sys_audit_log` VALUES (825, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:24:39');
INSERT INTO `sys_audit_log` VALUES (826, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:26:17');
INSERT INTO `sys_audit_log` VALUES (827, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:28:24');
INSERT INTO `sys_audit_log` VALUES (828, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:28:37');
INSERT INTO `sys_audit_log` VALUES (829, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:33:34');
INSERT INTO `sys_audit_log` VALUES (830, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:34:13');
INSERT INTO `sys_audit_log` VALUES (831, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:34:21');
INSERT INTO `sys_audit_log` VALUES (832, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:37:53');
INSERT INTO `sys_audit_log` VALUES (833, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:41:08');
INSERT INTO `sys_audit_log` VALUES (834, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 08:45:58');
INSERT INTO `sys_audit_log` VALUES (835, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:03:44');
INSERT INTO `sys_audit_log` VALUES (836, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:36:14');
INSERT INTO `sys_audit_log` VALUES (837, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:36:27');
INSERT INTO `sys_audit_log` VALUES (838, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:36:27');
INSERT INTO `sys_audit_log` VALUES (839, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:36:27');
INSERT INTO `sys_audit_log` VALUES (840, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 09:36:53');
INSERT INTO `sys_audit_log` VALUES (841, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 10:39:40');
INSERT INTO `sys_audit_log` VALUES (842, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 12:29:00');
INSERT INTO `sys_audit_log` VALUES (843, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 12:29:11');
INSERT INTO `sys_audit_log` VALUES (844, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 12:29:11');
INSERT INTO `sys_audit_log` VALUES (845, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 13:36:15');
INSERT INTO `sys_audit_log` VALUES (846, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 13:36:25');
INSERT INTO `sys_audit_log` VALUES (847, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:35:03');
INSERT INTO `sys_audit_log` VALUES (848, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:35:18');
INSERT INTO `sys_audit_log` VALUES (849, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:39:36');
INSERT INTO `sys_audit_log` VALUES (850, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:40:03');
INSERT INTO `sys_audit_log` VALUES (851, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:40:03');
INSERT INTO `sys_audit_log` VALUES (852, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 15:55:08');
INSERT INTO `sys_audit_log` VALUES (853, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 16:11:44');
INSERT INTO `sys_audit_log` VALUES (854, 0, 0, 'system', '0:0:0:0:0:0:0:1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 17:14:32');
INSERT INTO `sys_audit_log` VALUES (855, 0, 0, 'system', '127.0.0.1', 'base', '登录', '1', NULL, NULL, NULL, '2026-09-05 17:14:50');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团全局参数',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数key',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '参数值',
  `config_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数显示名称',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key_company`(`config_key` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 0, 'water_elec.water_price', '4.50', '水费单价（元/吨）', '水电物业账单生成计费参数，集团统一配置', 1, '2026-08-18 15:53:31', 1, '2026-08-18 17:09:55', 0);
INSERT INTO `sys_config` VALUES (2, 0, 'water_elec.elec_price', '1.90', '电费单价（元/度）', '水电物业账单生成计费参数，集团统一配置', 1, '2026-08-18 15:53:31', 1, '2026-08-18 17:09:39', 0);
INSERT INTO `sys_config` VALUES (3, 0, 'water_elec.property_price', '50.00', '物业费单价（元/铺位/月）', '水电物业账单生成计费参数，集团统一配置，一期按铺位固定费用', 1, '2026-08-18 15:53:31', 1, '2026-08-18 17:09:07', 0);
INSERT INTO `sys_config` VALUES (4, 0, 'discount.waive_months_limit', '3', '免租期上限（月）', '优惠申请免租期超限 → need_audit=1 自动发起审批', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (5, 0, 'discount.min_rate_limit', '80', '折扣率下限（%）', '优惠申请折扣低于下限 → need_audit=1 自动发起审批', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (6, 0, 'discount.max_deduct_limit', '5000.00', '单合同减免金额上限（元）', '优惠申请减免超上限 → need_audit=1 自动发起审批', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (7, 0, 'discount.contract_ratio_limit', '10.00', '优惠占合同总租金比例上限（%）', '优惠总额占比超限 → need_audit=1 自动发起审批', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (8, 0, 'plan.adjust_amount_limit', '2000.00', '计划单笔调账阈值（元）', '调账绝对值超阈值必须走审批引擎', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (9, 0, 'plan.overdue_remind_days', '7', '逾期提醒天数', '计划逾期N天推送站内信/短信提醒', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (10, 0, 'reimburse.amount_limit', '5000.00', '报销单金额阈值（元）', '报销金额超阈值自动选用 reimburse_large 大额流程', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (11, 0, 'purchase.amount_limit', '20000.00', '采购单金额阈值（元）', '采购金额超阈值自动选用 purchase_large 大额流程', 1, '2026-08-20 14:40:35', NULL, NULL, 0);
INSERT INTO `sys_config` VALUES (12, 0, 'contract.rent_editable', '0', '合同租金押金可写开关', '1=新增合同时租金/押金可手工填写；0=只读（按铺位收费规则自动带出，禁止手改）', 0, '2026-08-20 16:49:26', NULL, '2026-08-20 16:49:43', 0);
INSERT INTO `sys_config` VALUES (13, 0, 'contract.discount_editable', '0', '合同优惠参数可写开关', '1=新增合同时免租/折扣/减免可手工调整；0=只读（随所选优惠策略自动带出，禁止手改）', 0, '2026-08-20 16:49:26', 1, '2026-08-20 16:49:45', 0);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type_id` bigint(20) NOT NULL COMMENT '字典类型ID',
  `dict_value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典显示文本',
  `dict_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典存储值',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0禁用1启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type_id`(`dict_type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表【集团全局】' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '空置', '0', 1, 1, 0, '2026-08-18 12:34:31', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (2, 1, '已租', '1', 2, 1, 0, '2026-08-18 12:34:31', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (3, 1, '欠费', '2', 3, 1, 0, '2026-08-18 12:34:31', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (4, 1, '即将到期', '3', 4, 1, 0, '2026-08-18 12:34:31', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------

DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型编码',
  `dict_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型名称',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表【集团全局】' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 'stall_status', '铺位状态', 1, NULL, 0, '2026-08-18 12:34:31', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_ding_sync_record
-- ----------------------------

DROP TABLE IF EXISTS `sys_ding_sync_record`;
CREATE TABLE `sys_ding_sync_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `sync_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '同步类型 dept/user/checkin',
  `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钉钉业务唯一ID',
  `sync_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '同步状态0待同步1成功2失败',
  `sync_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '同步返回消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '钉钉同步记录表【预留，一期不执行业务写入】' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ding_sync_record
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限菜单表（集团全局）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '中台管理', NULL, '/org', 'office-building', 1, 1, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (2, 0, '系统设置', NULL, '/sys', 'setting', 2, 1, 1, 1, '2026-08-18 12:51:36', NULL, NULL, 0);
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
INSERT INTO `sys_menu` VALUES (37, 36, '水电表管理', 'waterElec:list', '/property/meter', 'Cpu', 1, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 16:38:27', 0);
INSERT INTO `sys_menu` VALUES (38, 36, '水电费账单', 'waterElec:bill:list', '/property/bill', 'Document', 2, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-24 07:28:31', 0);
INSERT INTO `sys_menu` VALUES (39, 36, '缴费管理', 'waterElec:pay:list', '/property/pay', 'Wallet', 3, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 16:38:27', 0);
INSERT INTO `sys_menu` VALUES (40, 0, '财务管理', NULL, '/finance', 'Money', 4, 1, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (41, 40, '财务流水', 'finance:flow:list', '/finance/flow', 'List', 1, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (42, 40, '营收统计', 'finance:report:list', '/finance/report', 'TrendCharts', 2, 2, 1, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (43, 37, '设备新增', 'waterElec:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (44, 37, '设备编辑', 'waterElec:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (45, 37, '设备删除', 'waterElec:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (46, 37, '远程抄表', 'waterElec:read', NULL, NULL, 4, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (47, 37, '合闸断电', 'waterElec:switch', NULL, NULL, 5, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (48, 38, '生成账单', 'waterElec:bill:generate', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (49, 39, '线下缴费', 'waterElec:pay:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (50, 39, '退费', 'waterElec:pay:refund', NULL, NULL, 2, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (51, 41, '流水导出', 'finance:flow:export', NULL, NULL, 1, 3, 0, 1, '2026-08-18 15:53:31', NULL, '2026-08-18 15:54:22', 0);
INSERT INTO `sys_menu` VALUES (52, 36, '租户管理', 'tenant:list', '/property/tenant', 'User', 1, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (53, 36, '租赁管理', NULL, '/property/lease', 'Goods', 2, 1, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (54, 53, '铺位管理', 'lease:stall:list', '/property/lease/stall', 'OfficeBuilding', 1, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (55, 53, '租赁分类', 'lease:category:list', '/property/lease/category', 'Menu', 2, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (56, 53, '合同管理', 'lease:contract:list', '/property/lease/contract', 'Document', 3, 2, 1, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (57, 52, '租户新增', 'tenant:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (58, 52, '租户编辑', 'tenant:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (59, 52, '租户删除', 'tenant:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (60, 54, '铺位新增', 'lease:stall:add', NULL, NULL, 1, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (61, 54, '铺位编辑', 'lease:stall:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (62, 54, '铺位删除', 'lease:stall:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-18 16:38:27', NULL, NULL, 0);
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
INSERT INTO `sys_menu` VALUES (123, 111, '同步考勤', 'hr:attendance:sync', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (124, 111, '导出', 'hr:attendance:export', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (125, 112, '生成核算', 'hr:salary:month:generate', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (126, 112, '薪资发放', 'hr:salary:month:pay', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (127, 112, '导出', 'hr:salary:month:export', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (128, 113, '新增', 'hr:social:add', NULL, NULL, 1, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (129, 113, '编辑', 'hr:social:edit', NULL, NULL, 2, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (130, 113, '删除', 'hr:social:delete', NULL, NULL, 3, 3, 0, 1, '2026-08-25 17:51:45', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_org
-- ----------------------------

DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团总公司',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '上级组织ID',
  `org_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织部门名称',
  `org_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '组织类型：1集团 2子公司 3部门',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态0禁用1启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_org
-- ----------------------------
INSERT INTO `sys_org` VALUES (1, 0, 0, '琰越控股集团', 1, 1, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (2, 0, 1, '总经办', 3, 1, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (3, 0, 1, '财务部', 3, 2, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (4, 0, 1, '人力资源部', 3, 3, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (5, 0, 1, '技术中心', 3, 4, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (6, 0, 1, '飞宇汽车城', 2, 5, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (7, 6, 6, '飞宇‑销售部', 3, 1, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (8, 6, 6, '飞宇‑运维部', 3, 2, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (9, 6, 6, '飞宇‑综合管理部', 3, 3, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (10, 0, 1, '幼儿园', 2, 6, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (11, 10, 10, '幼儿园‑业务部', 3, 1, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);
INSERT INTO `sys_org` VALUES (12, 10, 10, '幼儿园‑后勤保障部', 3, 2, 1, 1, '2026-08-21 09:20:52', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_permission_audit
-- ----------------------------

DROP TABLE IF EXISTS `sys_permission_audit`;
CREATE TABLE `sys_permission_audit`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `target_user_id` bigint(20) NOT NULL COMMENT '被授权用户ID',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人ID（集团运维）',
  `audit_user_id` bigint(20) NULL DEFAULT NULL COMMENT '复核审批人ID（超级管理员）',
  `permission_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '待变更权限标识集合JSON',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请变更原因',
  `audit_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2驳回',
  `audit_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审批完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id_audit_status`(`company_id` ASC, `audit_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感权限二级复核申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission_audit
-- ----------------------------
INSERT INTO `sys_permission_audit` VALUES (1, 0, 1, 1, NULL, '[\"user:delete\"]', '演示数据：申请开通用户删除权限，请复核', 0, NULL, '2026-08-18 12:51:36', NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团全局角色模板',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色备注说明',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code_company`(`role_code` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 0, '超级管理员', 'super_admin', '集团中台专属角色，拥有全部权限', 1, '2026-08-18 12:51:36', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (2, 0, '审计员', 'auditor', '审计日志查看/导出与权限复核审批角色', 1, '2026-08-18 12:51:36', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_role_menu_rel
-- ----------------------------

DROP TABLE IF EXISTS `sys_role_menu_rel`;
CREATE TABLE `sys_role_menu_rel`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 192 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu_rel
-- ----------------------------
INSERT INTO `sys_role_menu_rel` VALUES (1, 1, 1, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (2, 1, 2, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (3, 1, 3, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (4, 1, 4, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (5, 1, 5, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (6, 1, 6, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (7, 1, 7, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (8, 1, 8, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (9, 1, 9, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (10, 1, 10, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (11, 1, 11, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (12, 1, 12, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (13, 1, 13, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (14, 1, 14, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (15, 1, 15, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (16, 1, 16, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (17, 1, 17, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (18, 1, 18, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (19, 1, 19, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (20, 1, 20, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (21, 1, 21, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (22, 1, 22, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (23, 1, 23, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (24, 1, 24, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (25, 1, 25, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (26, 1, 26, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (27, 1, 27, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (28, 1, 28, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (29, 1, 29, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (30, 1, 30, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (31, 1, 31, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (32, 1, 32, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (33, 1, 33, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (34, 1, 34, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (35, 1, 35, '2026-08-18 12:51:36');
INSERT INTO `sys_role_menu_rel` VALUES (64, 1, 36, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (65, 1, 37, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (66, 1, 38, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (67, 1, 39, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (68, 1, 40, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (69, 1, 41, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (70, 1, 42, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (71, 1, 43, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (72, 1, 44, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (73, 1, 45, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (74, 1, 46, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (75, 1, 47, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (76, 1, 48, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (77, 1, 49, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (78, 1, 50, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (79, 1, 51, '2026-08-18 15:53:31');
INSERT INTO `sys_role_menu_rel` VALUES (95, 1, 52, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (96, 1, 53, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (97, 1, 54, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (98, 1, 55, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (99, 1, 56, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (100, 1, 57, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (101, 1, 58, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (102, 1, 59, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (103, 1, 60, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (104, 1, 61, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (105, 1, 62, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (106, 1, 63, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (107, 1, 64, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (108, 1, 65, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (109, 1, 66, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (110, 1, 67, '2026-08-18 16:38:27');
INSERT INTO `sys_role_menu_rel` VALUES (126, 1, 68, '2026-08-18 18:17:04');
INSERT INTO `sys_role_menu_rel` VALUES (127, 1, 69, '2026-08-18 18:17:04');
INSERT INTO `sys_role_menu_rel` VALUES (128, 1, 70, '2026-08-18 18:17:04');
INSERT INTO `sys_role_menu_rel` VALUES (129, 1, 71, '2026-08-18 18:17:04');
INSERT INTO `sys_role_menu_rel` VALUES (130, 1, 72, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (131, 1, 73, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (132, 1, 74, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (133, 1, 75, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (134, 1, 76, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (135, 1, 77, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (136, 1, 78, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (137, 1, 79, '2026-08-19 12:24:43');
INSERT INTO `sys_role_menu_rel` VALUES (145, 1, 80, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (146, 1, 81, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (147, 1, 82, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (148, 1, 83, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (149, 1, 84, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (150, 1, 85, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (151, 1, 86, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (152, 1, 87, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (153, 1, 88, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (154, 1, 89, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (155, 1, 90, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (156, 1, 91, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (157, 1, 92, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (158, 1, 93, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (159, 1, 94, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (160, 1, 95, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (161, 1, 96, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (162, 1, 97, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (163, 1, 98, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (164, 1, 99, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (165, 1, 100, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (166, 1, 101, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (167, 1, 102, '2026-08-20 13:42:31');
INSERT INTO `sys_role_menu_rel` VALUES (168, 1, 107, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (169, 1, 108, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (170, 1, 109, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (171, 1, 110, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (172, 1, 111, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (173, 1, 112, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (174, 1, 113, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (175, 1, 114, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (176, 1, 115, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (177, 1, 116, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (178, 1, 117, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (179, 1, 118, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (180, 1, 119, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (181, 1, 120, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (182, 1, 121, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (183, 1, 122, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (184, 1, 123, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (185, 1, 124, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (186, 1, 125, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (187, 1, 126, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (188, 1, 127, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (189, 1, 128, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (190, 1, 129, '2026-08-25 17:55:15');
INSERT INTO `sys_role_menu_rel` VALUES (191, 1, 130, '2026-08-25 17:55:15');

-- ----------------------------
-- Table structure for sys_ui_theme
-- ----------------------------

DROP TABLE IF EXISTS `sys_ui_theme`;
CREATE TABLE `sys_ui_theme`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团全局配置',
  `primary_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统主色十六进制',
  `layout_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'side' COMMENT '布局模式 side/top',
  `card_radius` int(11) NOT NULL DEFAULT 6 COMMENT '卡片圆角像素',
  `dark_mode` tinyint(4) NOT NULL DEFAULT 0 COMMENT '暗黑模式 0关闭 1开启',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'UI主题配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ui_theme
-- ----------------------------
INSERT INTO `sys_ui_theme` VALUES (1, 0, '#409EFF', 'side', 8, 0, 1, '2026-08-18 13:16:34', 1, '2026-08-18 14:14:38', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0集团',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BCrypt加密密码',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码（密文存储）',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像OSS地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账号状态 0禁用 1正常',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 0, 'admin', '$2a$10$0zgq2ilQLN0kC6XLyR17COxA./GDdftY659m/FOsGoHxOfGQMVtwS', '集团超级管理员', '', NULL, NULL, 1, 0, '2026-08-18 12:34:31', 0, '2026-08-26 07:25:05', 0);

-- ----------------------------
-- Table structure for sys_user_role_rel
-- ----------------------------

DROP TABLE IF EXISTS `sys_user_role_rel`;
CREATE TABLE `sys_user_role_rel`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '系统用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role_rel
-- ----------------------------
INSERT INTO `sys_user_role_rel` VALUES (1, 1, 1, 1, '2026-08-18 12:51:36');



SET FOREIGN_KEY_CHECKS = 1;

-- 来源: upgrade_v2.7_unified_bill.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V2.7
-- 模块：全域统一账单表 biz_fee_bill 扩展 biz_type 字段
-- 内容：
--   1. 新增 biz_type 列（已有，此处为幂等校验）
--   2. 新增复合索引 idx_company_biztype_status
--   3. 数据迁移：存量未缴账单从 property_bill / water_elec_bill 迁入 biz_fee_bill
--
-- 设计原则：
--   - biz_type 字段区分费用类型（property_fee/water_elec/rent/kindergarten...）
--   - 新增费用类型只需在 biz_fee_bill 写入对应 biz_type 记录，无需改表结构
--   - 兼容存量：保留 property_bill / water_elec_bill，新数据写入 biz_fee_bill
--
-- 执行方式:
--   cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.7_unified_bill.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 确认 biz_type 列存在（幂等）
ALTER TABLE `biz_fee_bill`
  ADD COLUMN IF NOT EXISTS `biz_type` VARCHAR(32) NOT NULL DEFAULT 'property_fee'
    COMMENT '业务类型 property_fee=物业费 water_elec=水电费 rent=租赁费 kindergarten=幼儿园费'
    AFTER `company_id`;

-- 2. 确认索引存在（幂等）
ALTER TABLE `biz_fee_bill`
  ADD INDEX `idx_company_biztype_status` (`company_id`, `biz_type`, `pay_status`, `is_delete`);

-- 3. 数据迁移：物业费账单（仅未缴/部分缴费）
INSERT IGNORE INTO `biz_fee_bill`
  (`id`, `company_id`, `biz_type`, `stall_id`, `merchant_id`, `bill_month`, `rule_id`, `plan_id`,
   `original_amount`, `discount_amount`, `adjust_amount`, `real_amount`, `pay_status`, `pay_time`,
   `locked_flag`, `create_by`, `create_time`)
SELECT
  id, company_id, 'property_fee', stall_id, COALESCE(merchant_id, 0), bill_month,
  COALESCE(rule_id, 0), plan_id,
  amount, 0.00, 0.00, amount, pay_status, pay_time,
  1, create_by, create_time
FROM `property_bill`
WHERE pay_status IN (0, 1)
  AND is_delete = 0
  AND NOT EXISTS (SELECT 1 FROM biz_fee_bill b WHERE b.id = property_bill.id AND b.biz_type = 'property_fee');

-- 4. 数据迁移：水电费账单（仅未缴/部分缴费）
INSERT IGNORE INTO `biz_fee_bill`
  (`id`, `company_id`, `biz_type`, `stall_id`, `merchant_id`, `bill_month`, `rule_id`, `plan_id`,
   `original_amount`, `discount_amount`, `adjust_amount`, `real_amount`, `pay_status`, `pay_time`,
   `locked_flag`, `create_by`, `create_time`)
SELECT
  id, company_id, 'water_elec', stall_id, merchant_id, bill_month,
  0, plan_id,
  total_amount, 0.00, 0.00, total_amount, pay_status, pay_time,
  1, create_by, create_time
FROM `water_elec_bill`
WHERE pay_status IN (0, 1)
  AND is_delete = 0
  AND NOT EXISTS (SELECT 1 FROM biz_fee_bill b WHERE b.id = water_elec_bill.id AND b.biz_type = 'water_elec');

-- 5. 变更日志
-- V2.7: biz_fee_bill 新增 biz_type 字段，聚合 property_bill + water_elec_bill 未缴数据
-- 后续新增费用类型（租赁费/幼儿园费/停车费等）只需在 biz_fee_bill 写入对应 biz_type 记录即可

-- ============================================================
-- 来源: upgrade_biz_fee_bill_source_bill_id.sql
-- ============================================================
-- 为 biz_fee_bill 添加 source_bill_id 字段（用于幂等检查和追溯源账单）
ALTER TABLE biz_fee_bill ADD COLUMN source_bill_id bigint DEFAULT NULL COMMENT '源账单ID（water_elec_bill.id / property_fee_bill.id，用于幂等和追溯）' AFTER remark;
CREATE INDEX idx_source_bill_id ON biz_fee_bill(source_bill_id);
CREATE INDEX idx_source_bill_id ON biz_fee_bill(source_bill_id);
CREATE INDEX idx_source_bill_id ON biz_fee_bill(source_bill_id);

-- ============================================================
-- 来源: init_hr_menu.sql
-- ============================================================
-- =====================================================
-- HR模块菜单初始化脚本（修正版）
-- 字段与 install.sql 的 sys_menu 结构保持一致：
--   id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by
-- menu_type: 1=目录 2=页面 3=按钮
-- visible: 1=显示 0=隐藏（按钮默认0）
-- id 从 107 开始（install.sql 最大id=106）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < init_hr_menu.sql
-- =====================================================

-- 1. 人力资源主目录（id=107）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES (107, 0, '人力资源', NULL, '/hr', 'UserFilled', 3, 1, 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2. 子页面（id=108~113）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (108, 107, '员工档案',   'hr:employee:list',     '/hr/employee',   'User',           1, 2, 1, 1),
  (109, 107, '组织岗位',   'hr:org:list',          '/hr/org',        'OfficeBuilding', 2, 2, 1, 1),
  (110, 107, '人事异动',   'hr:entry:list',        '/hr/transfer',   'SwitchButton',   3, 2, 1, 1),
  (111, 107, '考勤管理',   'hr:attendance:list',   '/hr/attendance', 'Calendar',       4, 2, 1, 1),
  (112, 107, '薪酬管理',   'hr:salary:month:list', '/hr/salary',     'Money',          5, 2, 1, 1),
  (113, 107, '社保公积金', 'hr:social:list',       '/hr/social',     'Document',       6, 2, 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3. 按钮权限：员工档案（id=114~117）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (114, 108, '新增', 'hr:employee:add',   NULL, NULL, 1, 3, 0, 1),
  (115, 108, '编辑', 'hr:employee:edit',  NULL, NULL, 2, 3, 0, 1),
  (116, 108, '删除', 'hr:employee:delete',NULL, NULL, 3, 3, 0, 1),
  (117, 108, '导出', 'hr:employee:export',NULL, NULL, 4, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 4. 按钮权限：组织岗位（id=118~120）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (118, 109, '新增岗位', 'hr:org:post:add',    NULL, NULL, 1, 3, 0, 1),
  (119, 109, '编辑岗位', 'hr:org:post:edit',   NULL, NULL, 2, 3, 0, 1),
  (120, 109, '删除岗位', 'hr:org:post:delete', NULL, NULL, 3, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 5. 按钮权限：人事异动（id=121~122）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (121, 110, '入职申请', 'hr:entry:add',  NULL, NULL, 1, 3, 0, 1),
  (122, 110, '离职申请', 'hr:resign:add', NULL, NULL, 2, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 6. 按钮权限：考勤管理（id=123~124）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (123, 111, '同步考勤', 'hr:attendance:sync', NULL, NULL, 1, 3, 0, 1),
  (124, 111, '导出',     'hr:attendance:export',NULL, NULL, 2, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 7. 按钮权限：薪酬管理（id=125~127）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (125, 112, '生成核算', 'hr:salary:month:generate', NULL, NULL, 1, 3, 0, 1),
  (126, 112, '薪资发放', 'hr:salary:month:pay',      NULL, NULL, 2, 3, 0, 1),
  (127, 112, '导出',     'hr:salary:month:export',   NULL, NULL, 3, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 8. 按钮权限：社保公积金（id=128~130）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by)
VALUES
  (128, 113, '新增', 'hr:social:add',    NULL, NULL, 1, 3, 0, 1),
  (129, 113, '编辑', 'hr:social:edit',   NULL, NULL, 2, 3, 0, 1),
  (130, 113, '删除', 'hr:social:delete', NULL, NULL, 3, 3, 0, 1)
ON DUPLICATE KEY UPDATE permission = VALUES(permission);

-- 9. 超级管理员授予 HR 全部菜单（107~130）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m
WHERE m.id BETWEEN 107 AND 130
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);


