-- ============================================================
-- 集团多业态一体化管控系统 安装脚本
-- 全量建表（整合 group_rent_db.sql 正式版 + 各版本升级修正）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < install.sql
-- ============================================================

SET NAMES utf8mb4;


-- ------------------------------
-- [v2.0]
-- ------------------------------
DROP TABLE IF EXISTS `bill_plan_rel`;
CREATE TABLE `bill_plan_rel`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账单-应收应付计划关联表（多计划合并账单）' ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `biz_discount_apply`;
CREATE TABLE `biz_discount_apply`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请编号',
  `policy_id` bigint(20) NOT NULL COMMENT '优惠策略ID',
  `policy_snapshot` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '策略快照JSON（审批后计算依据，固化不回溯）',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源类型 contract',
  `source_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据ID（合同ID）',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同编号（冗余便于列表展示）',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '摊位ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠申请单表' ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `biz_discount_policy`;
CREATE TABLE `biz_discount_policy`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团模板',
  `policy_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略名称',
  `discount_type` tinyint(4) NOT NULL COMMENT '优惠类型 1免租期 2折扣率 3减免金额 4组合',
  `waive_months` int(11) NOT NULL DEFAULT 0 COMMENT '免租期月数（type=1/4）',
  `discount_rate` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '折扣率%（100=无折扣，type=2/4）',
  `deduct_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '减免金额（type=3/4）',
  `scope_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '适用范围 1按合同 2按摊位',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠策略表（免租期/折扣/减免/组合）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.1补建]
-- ------------------------------
DROP TABLE IF EXISTS `biz_fee_bill`;
CREATE TABLE `biz_fee_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'property_fee' COMMENT '业务类型 property_fee=物业费 water_elec=水电费 rent=租赁费 kindergarten=幼儿园费',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID（可空，账单可无商户绑定）',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单周期标识（月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性once）',
  `rule_id` bigint(20) NOT NULL COMMENT '生成账单的规则ID（biz_fee_rule，锁定后仅记录不回溯）',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID（可空）',
  `period_type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '账单周期 0不使用 1按年 2按月 3按日（与biz_fee_rule一致）',
  `original_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '应收原价合计',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免金额合计',
  `adjust_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `real_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '实际应收 = 原价 - 优惠 + 调账',
  `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费完成时间',
  `locked_flag` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账单锁定 1锁定（生成即锁定，规则变更不回溯）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单备注',
  `source_bill_id` bigint(20) NULL DEFAULT NULL COMMENT '源账单ID（water_elec_bill.id / property_fee_bill.id，用于幂等和追溯）',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（缴费完成禁止删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stall_rule_period`(`company_id` ASC, `stall_id` ASC, `rule_id` ASC, `bill_month` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_stall_id`(`stall_id` ASC) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE,
  INDEX `idx_company_biztype_status`(`company_id` ASC, `biz_type` ASC, `pay_status` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_source_bill_id`(`source_bill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自定义收费周期账单表（收费规则账单未支付，经plan_id/bill_plan_rel挂靠应收应付计划）' ROW_FORMAT = Dynamic;

-- ----------------------------
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费账单明细快照表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v1.7]
-- ------------------------------
DROP TABLE IF EXISTS `biz_fee_item`;
CREATE TABLE `biz_fee_item`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自定义收费类型表（收费项）' ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `biz_fee_rule`;
CREATE TABLE `biz_fee_rule`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自定义收费规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `biz_fee_rule_stall_rel`;
CREATE TABLE `biz_fee_rule_stall_rel`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `rule_id` bigint(20) NOT NULL COMMENT '收费规则ID（biz_fee_rule）',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID（stall_info）',
  `override_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否特殊覆盖 0普通绑定 1单摊位覆盖（预留）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费规则-摊位绑定关联表（同收费类型限选一条）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.0+]
-- ------------------------------
DROP TABLE IF EXISTS `biz_finance_flow`;
CREATE TABLE `biz_finance_flow`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 rent租金/water_elec水电/deposit押金/marketing营销抵扣',
  `bill_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联业务单据ID',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID（biz_recv_pay_plan，核销时写入）',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '摊位ID',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位编号快照（写入时固化）',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位名称快照（写入时固化）',
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
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_time`(`company_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_bill_type`(`business_type` ASC, `bill_id` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_flow_no`(`flow_no` ASC) USING BTREE,
  INDEX `idx_contract_id`(`contract_id` ASC) USING BTREE,
  INDEX `idx_payer_name`(`payer_name` ASC) USING BTREE,
  INDEX `idx_flow_status`(`flow_status` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '全域财务资金流水表【禁止修改、禁止删除，只新增查询】' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.0]
-- ------------------------------
DROP TABLE IF EXISTS `biz_finance_writeoff`;
CREATE TABLE `biz_finance_writeoff`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收付款核销分摊明细表（资金流水↔应收应付计划精确对账）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.2]
-- ------------------------------
DROP TABLE IF EXISTS `biz_flow_seq`;
CREATE TABLE `biz_flow_seq`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL COMMENT '所属子公司ID',
  `seq_date` date NOT NULL COMMENT '日期 yyyy-MM-dd',
  `seq_no` int(11) NOT NULL DEFAULT 1 COMMENT '当日已分配序号',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_date`(`company_id` ASC, `seq_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '财务流水单号顺序号计数器（公司+日维度，防并发重号）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [扩展表]
-- ------------------------------
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '金蝶凭证推送记录表【预留，一期不执行业务写入】' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.0]
-- ------------------------------
DROP TABLE IF EXISTS `biz_recv_pay_plan`;
CREATE TABLE `biz_recv_pay_plan`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `plan_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划编号（AR-RENT-公司-日期-序号 / AP-REIMB-公司-日期-序号）',
  `direction` tinyint(4) NOT NULL DEFAULT 1 COMMENT '方向 1应收 2应付（二期报销采购）',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型 rent租金 deposit押金 property物业费 fee_bill收费规则账单 reimburse报销 purchase采购',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据类型 contract/reimburse/purchase/bill',
  `source_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单据ID（合同/报销单/采购单）',
  `market_id` bigint(20) NULL DEFAULT NULL COMMENT '市场ID',
  `stall_id` bigint(20) NULL DEFAULT NULL COMMENT '摊位ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 241 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应收应付计划主表（全系统唯一台账）' ROW_FORMAT = Dynamic;

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

-- ------------------------------
-- [v2.6 HR]
-- ------------------------------
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

-- ------------------------------
-- [基础表]
-- ------------------------------
DROP TABLE IF EXISTS `map_stall_point`;
CREATE TABLE `map_stall_point`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `map_id` bigint(20) NOT NULL COMMENT '关联地图ID',
  `market_id` bigint(20) NOT NULL COMMENT '所属市场ID',
  `stall_id` bigint(20) NOT NULL COMMENT '关联摊位ID',
  `point_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '单个摊位点位JSON数据',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_stall`(`map_id` ASC, `stall_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摊位点位明细表（点位超过500条启用）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v1.3]
-- ------------------------------
DROP TABLE IF EXISTS `market_info`;
CREATE TABLE `market_info`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '市场档案表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [扩展表]
-- ------------------------------
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

-- ------------------------------
-- [v2.4 OA]
-- ------------------------------
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

-- ------------------------------
-- [v1.12]
-- ------------------------------
DROP TABLE IF EXISTS `property_bill`;
CREATE TABLE `property_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份 yyyy-MM',
  `rule_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '计费规则ID（biz_fee_rule，快照）',
  `fee_item_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '收费类型ID（biz_fee_item，固定=物业费）',
  `calc_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '收费方式 1定额 2按面积',
  `period_type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '收费周期 0不使用 1按年 2按月 3按日',
  `usage` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '用量：定额=0，按面积=摊位面积（快照）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物业费月度账单表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v1.2]
-- ------------------------------
DROP TABLE IF EXISTS `stall_category`;
CREATE TABLE `stall_category`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租赁分类表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [基础表]
-- ------------------------------
DROP TABLE IF EXISTS `stall_contract`;
CREATE TABLE `stall_contract`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID（v2.0起合同改用租户体系，可空）',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户ID（关联stall_tenant）',
  `rent_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '月租金金额',
  `deposit_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '押金金额',
  `start_time` date NOT NULL COMMENT '租赁开始日期',
  `end_time` date NOT NULL COMMENT '租赁到期日期',
  `contract_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '合同状态 1生效 2已退租 3已到期',
  `flow_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '??????ID?????/???????',
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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摊位租赁合同表' ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `stall_info`;
CREATE TABLE `stall_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `market_id` bigint(20) NOT NULL COMMENT '关联市场ID',
  `stall_category_id` bigint(20) NULL DEFAULT NULL COMMENT '租赁分类ID（关联stall_category）',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '摊位编号',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位名称',
  `stall_area` decimal(10, 2) NULL DEFAULT NULL COMMENT '摊位面积(平方米)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '摊位状态 0空置 1已租 2欠费 3即将到期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stall_no_company`(`stall_number` ASC, `company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_company_id_is_delete`(`company_id` ASC, `is_delete` ASC) USING BTREE,
  INDEX `idx_market_id`(`market_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摊位基础信息表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户档案表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v1.2]
-- ------------------------------
DROP TABLE IF EXISTS `stall_tenant`;
CREATE TABLE `stall_tenant`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户档案表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [系统表]
-- ------------------------------
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
) ENGINE = InnoDB AUTO_INCREMENT = 645 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业务审计日志表【永久不可删除】' ROW_FORMAT = Dynamic;

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

-- ------------------------------
-- [v1.11重构]
-- ------------------------------
DROP TABLE IF EXISTS `water_elec_bill`;
CREATE TABLE `water_elec_bill`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户ID',
  `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '关联应收应付计划ID',
  `bill_month` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份 yyyy-MM',
  `category` tinyint(4) NOT NULL DEFAULT 3 COMMENT '收费类别 2物业费 3水费 4电费（复用 biz_fee_item.category_type）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '水电物业月度账单表（按类别拆行）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v2.0+]
-- ------------------------------
DROP TABLE IF EXISTS `water_elec_meter`;
CREATE TABLE `water_elec_meter`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint(20) NOT NULL COMMENT '绑定摊位ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能水电表设备表' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ------------------------------
-- [v1.1]
-- ------------------------------
DROP TABLE IF EXISTS `water_elec_pay_record`;
CREATE TABLE `water_elec_pay_record`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint(20) NOT NULL COMMENT '关联账单ID',
  `stall_id` bigint(20) NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商户ID',
  `stall_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位编号快照（写入时固化）',
  `stall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摊位名称快照（写入时固化）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '水电物业缴费记录表（幂等唯一键）' ROW_FORMAT = Dynamic;

-- ----------------------------

-- ============================================================
-- 中台平台初始化数据：角色 + 菜单 + 授权（admin 绑定超管）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < init_platform_menu.sql
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐
-- ============================================================

-- 1. 角色（集团全局 company_id=0）
INSERT INTO sys_role (id, company_id, role_name, role_code, remark, create_by) VALUES
(1, 0, '超级管理员', 'super_admin', '集团中台专属角色，拥有全部权限', 1),
(2, 0, '审计员', 'auditor', '审计日志查看/导出与权限复核审批角色', 1);

-- 2. 菜单（目录 + 页面 + 按钮，id 显式指定，后续 AUTO_INCREMENT 从 36 开始）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 目录
(1, 0, '中台管理', NULL, '/org', 'office-building', 1, 1, 1, 1),
(2, 0, '系统设置', NULL, '/sys', 'setting', 2, 1, 1, 1),
-- 页面
(3, 1, '组织管理', 'org:list', '/org', 'office-building', 1, 2, 1, 1),
(4, 1, '用户管理', 'user:list', '/org/user', 'user', 2, 2, 1, 1),
(5, 1, '角色管理', 'role:list', '/org/role', 'avatar', 3, 2, 1, 1),
(6, 1, '菜单管理', 'menu:list', '/org/menu', 'menu', 4, 2, 1, 1),
(7, 2, '字典管理', 'dict:list', '/sys/dict', 'collection', 1, 2, 1, 1),
(8, 2, '参数配置', 'config:list', '/sys/config', 'tools', 2, 2, 1, 1),
(9, 2, 'UI主题', 'theme:list', '/sys/theme', 'brush', 3, 2, 1, 1),
(10, 2, '审计日志', 'audit:list', '/sys/audit', 'document', 4, 2, 1, 1),
(11, 2, '权限复核', 'permission:audit:list', '/sys/permissionAudit', 'key', 5, 2, 1, 1),
-- 组织按钮
(12, 3, '组织新增', 'org:add', NULL, NULL, 1, 3, 0, 1),
(13, 3, '组织编辑', 'org:edit', NULL, NULL, 2, 3, 0, 1),
(14, 3, '组织删除', 'org:delete', NULL, NULL, 3, 3, 0, 1),
-- 用户按钮
(15, 4, '用户新增', 'user:add', NULL, NULL, 1, 3, 0, 1),
(16, 4, '用户编辑', 'user:edit', NULL, NULL, 2, 3, 0, 1),
(17, 4, '用户删除', 'user:delete', NULL, NULL, 3, 3, 0, 1),
(18, 4, '重置密码', 'user:resetPwd', NULL, NULL, 4, 3, 0, 1),
(19, 4, '启用禁用', 'user:changeStatus', NULL, NULL, 5, 3, 0, 1),
-- 角色按钮
(20, 5, '角色新增', 'role:add', NULL, NULL, 1, 3, 0, 1),
(21, 5, '角色编辑', 'role:edit', NULL, NULL, 2, 3, 0, 1),
(22, 5, '角色删除', 'role:delete', NULL, NULL, 3, 3, 0, 1),
(23, 5, '菜单授权', 'role:menu', NULL, NULL, 4, 3, 0, 1),
-- 菜单按钮
(24, 6, '菜单新增', 'menu:add', NULL, NULL, 1, 3, 0, 1),
(25, 6, '菜单编辑', 'menu:edit', NULL, NULL, 2, 3, 0, 1),
(26, 6, '菜单删除', 'menu:delete', NULL, NULL, 3, 3, 0, 1),
-- 字典按钮
(27, 7, '字典新增', 'dict:add', NULL, NULL, 1, 3, 0, 1),
(28, 7, '字典编辑', 'dict:edit', NULL, NULL, 2, 3, 0, 1),
(29, 7, '字典删除', 'dict:delete', NULL, NULL, 3, 3, 0, 1),
-- 参数按钮
(30, 8, '参数新增', 'config:add', NULL, NULL, 1, 3, 0, 1),
(31, 8, '参数编辑', 'config:edit', NULL, NULL, 2, 3, 0, 1),
(32, 8, '参数删除', 'config:delete', NULL, NULL, 3, 3, 0, 1),
-- 主题按钮
(33, 9, '主题保存', 'theme:edit', NULL, NULL, 1, 3, 0, 1),
-- 审计按钮
(34, 10, '日志导出', 'audit:export', NULL, NULL, 1, 3, 0, 1),
-- 权限复核按钮
(35, 11, '复核处理', 'permission:audit:audit', NULL, NULL, 1, 3, 0, 1);

-- 3. admin 绑定超级管理员角色
INSERT INTO sys_user_role_rel (user_id, role_id, create_by) VALUES (1, 1, 1);

-- 4. 超级管理员授予全部菜单（1-35）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 1 AND 35;

-- 5. 权限二级复核演示数据（待审核，admin 申请开通 user:delete）
INSERT INTO sys_permission_audit (company_id, target_user_id, apply_user_id, permission_list, apply_reason, audit_status)
VALUES (0, 1, 1, '["user:delete"]', '演示数据：申请开通用户删除权限，请复核', 0);

-- ============================================================
-- 升级脚本与修正脚本（按版本号排序，已过滤重复建表）
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < install.sql
-- ============================================================

-- ============================================================
-- 来源: reset_pwd.sql
-- ============================================================
UPDATE sys_user SET password='\\\' WHERE username='admin';

-- ============================================================
-- 来源: reset_admin_pwd.sql
-- ============================================================
UPDATE sys_user SET password='$2a$10$0zgq2ilQLN0kC6XLyR17COxA./GDdftY659m/FOsGoHxOfGQMVtwS' WHERE username='admin';

-- ============================================================
-- 来源: reset_admin_pwd_v2.sql
-- ============================================================
UPDATE sys_user SET password='$2a$10$N9qo8uLOickgx2ZMRZOMyeIjZAgcfl7p92ldGxad68LJbL17lhWy' WHERE username='admin';

-- ============================================================
-- 来源: fix_admin_pwd.sql
-- ============================================================
UPDATE sys_user SET password='$2a$10$0zgq2ilQLN0kC6XLyR17COxA./GDdftY659m/FOsGoHxOfGQMVtwS' WHERE username='admin';

-- ============================================================
-- 来源: fix_admin_pwd_correct.sql
-- ============================================================
UPDATE sys_user SET password='\$2a\$10\$0zgq2ilQLN0kC6XLyR17COxA./GDdftY659m/FOsGoHxOfGQMVtwS' WHERE username='admin';
SELECT id, username, LENGTH(password) as len, password FROM sys_user WHERE username='admin';

-- ============================================================
-- 来源: fix_pwd_final.sql
-- ============================================================
UPDATE sys_user SET password='\$2a\$10\$0zgq2ilQLN0kC6XLyR17COxA./GDdftY659m/FOsGoHxOfGQMVtwS' WHERE username='admin';
SELECT id, username, LENGTH(password) as len, password FROM sys_user WHERE username='admin';

-- ============================================================
-- 来源: upgrade_v1.1_waterElec_finance.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.1
-- 模块：水电物业模块 + 财务模块
-- 内容：water_elec_pay_record 缴费记录表 + 物业/财务菜单 + 超管授权 + 计费参数
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.1_waterElec_finance.sql
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐
-- ============================================================

SET NAMES utf8mb4;

-- 1. 水电物业缴费记录表（request_id 唯一索引支撑幂等，退费与缴费同表区分类型）

-- 2. 菜单：物业收费目录 + 财务管理目录（id 从 36 开始，与 init_platform_menu.sql 衔接）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 目录
(36, 0, '物业收费', NULL, '/waterElec', 'Odometer', 3, 1, 1, 1),
(40, 0, '财务管理', NULL, '/finance', 'Money', 4, 1, 1, 1),
-- 页面
(37, 36, '水电表管理', 'waterElec:list', '/waterElec/meter', 'Cpu', 1, 2, 1, 1),
(38, 36, '账单管理', 'waterElec:bill:list', '/waterElec/bill', 'Document', 2, 2, 1, 1),
(39, 36, '缴费管理', 'waterElec:pay:list', '/waterElec/pay', 'Wallet', 3, 2, 1, 1),
(41, 40, '财务流水', 'finance:flow:list', '/finance/flow', 'List', 1, 2, 1, 1),
(42, 40, '营收统计', 'finance:report:list', '/finance/report', 'TrendCharts', 2, 2, 1, 1),
-- 水电表按钮
(43, 37, '设备新增', 'waterElec:add', NULL, NULL, 1, 3, 0, 1),
(44, 37, '设备编辑', 'waterElec:edit', NULL, NULL, 2, 3, 0, 1),
(45, 37, '设备删除', 'waterElec:delete', NULL, NULL, 3, 3, 0, 1),
(46, 37, '远程抄表', 'waterElec:read', NULL, NULL, 4, 3, 0, 1),
(47, 37, '合闸断电', 'waterElec:switch', NULL, NULL, 5, 3, 0, 1),
-- 账单按钮
(48, 38, '生成账单', 'waterElec:bill:generate', NULL, NULL, 1, 3, 0, 1),
-- 缴费按钮
(49, 39, '线下缴费', 'waterElec:pay:add', NULL, NULL, 1, 3, 0, 1),
(50, 39, '退费', 'waterElec:pay:refund', NULL, NULL, 2, 3, 0, 1),
-- 财务按钮
(51, 41, '流水导出', 'finance:flow:export', NULL, NULL, 1, 3, 0, 1);

-- 3. 超级管理员授予全部新增菜单（36-51）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 36 AND 51
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 4. 计费参数初始化（集团全局 company_id=0，账单生成按此单价计算，子公司只读）
INSERT INTO sys_config (company_id, config_key, config_value, config_name, remark, create_by, is_delete) VALUES
(0, 'water_elec.water_price', '4.50', '水费单价（元/吨）', '水电物业账单生成计费参数，集团统一配置', 1, 0),
(0, 'water_elec.elec_price', '1.20', '电费单价（元/度）', '水电物业账单生成计费参数，集团统一配置', 1, 0),
(0, 'water_elec.property_price', '50.00', '物业费单价（元/摊位/月）', '水电物业账单生成计费参数，集团统一配置，一期按摊位固定费用', 1, 0)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- ============================================================
-- 来源: upgrade_v1.2_property_lease.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.2
-- 模块：物业管理模块（原"物业收费"改名）+ 租户管理 + 租赁管理
-- 内容：stall_tenant 租户档案表、stall_category 租赁分类表、
--       stall_info/stall_contract 增加字段、菜单改名+新增 52-67、超管授权、初始化分类
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.2_property_lease.sql"
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐
-- ============================================================

SET NAMES utf8mb4;

-- 1. 租户档案表（物业管理-租户管理，敏感证件字段密文存储）

-- 2. 租赁分类表（商铺/仓库/车位等，子公司可自定义）

-- 3. 摊位表增加租赁分类字段（stall_info 原表 ALTER）
ALTER TABLE `stall_info`
  ADD COLUMN `stall_category_id` bigint DEFAULT NULL COMMENT '租赁分类ID（关联stall_category）' AFTER `market_id`;

-- 4. 合同表增加租户字段（stall_contract 原表 ALTER）
ALTER TABLE `stall_contract`
  ADD COLUMN `tenant_id` bigint DEFAULT NULL COMMENT '租户ID（关联stall_tenant）' AFTER `stall_id`,
  ADD INDEX `idx_tenant_id` (`tenant_id`);

-- 5. 菜单：物业收费目录改名为"物业管理"，path /waterElec 改 /property，子页面路径同步
UPDATE sys_menu SET menu_name = '物业管理', path = '/property' WHERE id = 36;
UPDATE sys_menu SET path = '/property/meter' WHERE id = 37;
UPDATE sys_menu SET path = '/property/bill' WHERE id = 38;
UPDATE sys_menu SET path = '/property/pay' WHERE id = 39;

-- 6. 新增菜单：租户管理 + 租赁管理（目录+页面+按钮，id 52-67）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 页面
(52, 36, '租户管理', 'tenant:list', '/property/tenant', 'User', 1, 2, 1, 1),
(53, 36, '租赁管理', NULL, '/property/lease', 'Goods', 2, 1, 1, 1),
(54, 53, '摊位管理', 'lease:stall:list', '/property/lease/stall', 'OfficeBuilding', 1, 2, 1, 1),
(55, 53, '租赁分类', 'lease:category:list', '/property/lease/category', 'Menu', 2, 2, 1, 1),
(56, 53, '合同管理', 'lease:contract:list', '/property/lease/contract', 'Document', 3, 2, 1, 1),
-- 租户按钮
(57, 52, '租户新增', 'tenant:add', NULL, NULL, 1, 3, 0, 1),
(58, 52, '租户编辑', 'tenant:edit', NULL, NULL, 2, 3, 0, 1),
(59, 52, '租户删除', 'tenant:delete', NULL, NULL, 3, 3, 0, 1),
-- 摊位按钮
(60, 54, '摊位新增', 'lease:stall:add', NULL, NULL, 1, 3, 0, 1),
(61, 54, '摊位编辑', 'lease:stall:edit', NULL, NULL, 2, 3, 0, 1),
(62, 54, '摊位删除', 'lease:stall:delete', NULL, NULL, 3, 3, 0, 1),
-- 分类按钮
(63, 55, '分类新增', 'lease:category:add', NULL, NULL, 1, 3, 0, 1),
(64, 55, '分类编辑', 'lease:category:edit', NULL, NULL, 2, 3, 0, 1),
(65, 55, '分类删除', 'lease:category:delete', NULL, NULL, 3, 3, 0, 1),
-- 合同按钮
(66, 56, '合同新增', 'lease:contract:add', NULL, NULL, 1, 3, 0, 1),
(67, 56, '合同退租', 'lease:contract:terminate', NULL, NULL, 2, 3, 0, 1);

-- 7. 超级管理员授予全部新增菜单（52-67）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 52 AND 67
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 8. 初始化租赁分类（集团模板 company_id=0，子公司可自建自定义分类）
INSERT INTO stall_category (company_id, category_name, sort_order, status, remark, create_by) VALUES
(0, '商铺', 1, 1, '集团模板：商铺类租赁标的', 1),
(0, '仓库', 2, 1, '集团模板：仓库类租赁标的', 1),
(0, '车位', 3, 1, '集团模板：车位类租赁标的', 1);

-- ============================================================
-- 来源: upgrade_v1.3_market.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.3
-- 模块：物业管理-市场管理
-- 内容：market_info 市场档案表、菜单 68-71、超管授权、默认市场初始化
-- 说明：摊位/市场地图的 market_id 统一关联本表，杜绝手填错号
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.3_market.sql"
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐
-- ============================================================

SET NAMES utf8mb4;

-- 1. 市场档案表（园区/商圈维度，company_id + market_name 同公司唯一）

-- 2. 新增菜单：市场管理（页面+按钮，id 68-71，挂在物业管理目录 36 下）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
(68, 36, '市场管理', 'market:list', '/property/market', 'OfficeBuilding', 1, 2, 1, 1),
(69, 68, '市场新增', 'market:add', NULL, NULL, 1, 3, 0, 1),
(70, 68, '市场编辑', 'market:edit', NULL, NULL, 2, 3, 0, 1),
(71, 68, '市场删除', 'market:delete', NULL, NULL, 3, 3, 0, 1);

-- 3. 超级管理员授予全部新增菜单（68-71）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 68 AND 71
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 4. 初始化默认市场（集团模板 company_id=0）
-- 说明：一期升级前摊位手填 market_id=1 即对应本市场；子公司实际经营请在本公司创建市场档案
INSERT INTO market_info (company_id, market_name, market_address, contact_person, contact_phone, status, remark, create_by) VALUES
(0, '默认市场', '集团默认市场模板', NULL, NULL, 1, '集团模板：与一期摊位 market_id=1 对齐', 1);

-- ============================================================
-- 来源: fix_v1.4_remove_stall_type.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 修正脚本 V1.4
-- 模块：租赁摊位
-- 内容：移除 stall_info.stall_type 硬编码类型字段（1商铺 2库房）
-- 说明：该字段与 v1.2 引入的可配置租赁分类 stall_category 功能重复，
--       统一收敛为 stall_category_id 关联 stall_category 表（子公司可自定义），
--       前后端代码已同步移除 stallType 相关字段
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < fix_v1.4_remove_stall_type.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 删除硬编码摊位类型字段（开发库已确认无业务数据依赖；生产环境需先确认历史数据迁移到 stall_category 后再执行）
ALTER TABLE `stall_info` DROP COLUMN `stall_type`;

-- ============================================================
-- 来源: fix_v1.5_bill_read_column.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 修正脚本 V1.5
-- 模块：水电物业账单
-- 内容：water_elec_bill 增加本次水表/电表读数列
-- 说明：实体 WaterElecBill 与账单生成/上期读数逻辑依赖 water_read / elec_read，
--       但 v1.0 install.sql 建表时遗漏，导致账单生成时报 Unknown column 'water_read'
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < fix_v1.5_bill_read_column.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 账单表增加本次读数列（与 water_elec_meter.current_read 同精度，兼容抄表大读数）
ALTER TABLE `water_elec_bill`
  ADD COLUMN `water_read` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '本次水表读数' AFTER `bill_month`,
  ADD COLUMN `elec_read` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '本次电表读数' AFTER `water_read`;

-- ============================================================
-- 来源: fix_v1.6_bill_merchant_null.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 修正脚本 V1.6
-- 模块：水电物业账单
-- 内容：water_elec_bill.merchant_id 允许为空
-- 说明：v1.0 install.sql 将 merchant_id 定义为 NOT NULL 无默认值，但一期账单生成时
--       商户体系未强制绑定（生成代码 merchantId=null），严格模式 INSERT 报
--       "Field 'merchant_id' doesn't have a default value"
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < fix_v1.6_bill_merchant_null.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 商户ID允许为空（一期账单可无商户绑定，后续租赁联动再填充）
ALTER TABLE `water_elec_bill`
  MODIFY COLUMN `merchant_id` bigint DEFAULT NULL COMMENT '商户ID';

-- ============================================================
-- 来源: upgrade_v1.7_fee.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.7
-- 模块：财务管理-自定义收费
-- 内容：biz_fee_item 收费项表 + biz_fee_rule 收费规则表
--       + 菜单 72-79（挂财务管理目录 parent_id=40）+ 超管授权 + 初始化收费类型
-- 说明：收费类型（租金/物业费/水费/电费/押金/其他等）子公司可配置；
--       收费规则调用收费类型，收费方式（1定额 2按面积）、收费周期（1按年 2按月 3按日）、
--       滞纳金百分比；规则变更全部写审计日志（oper_module=fee_item / fee_rule）
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.7_fee.sql"
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐（fee:item:* / fee:rule:*）
-- ============================================================

SET NAMES utf8mb4;

-- 1. 收费项表（收费类型管理：租金/物业费/水费/电费/押金/其他等）

-- 2. 收费规则表（调用收费类型；收费方式 1定额 2按面积；收费周期 1按年 2按月 3按日；滞纳金百分比）

-- 3. 新增菜单：收费类型管理 + 收费规则管理（id 72-79，挂在财务管理目录 40 下）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 页面
(72, 40, '收费类型管理', 'fee:item:list', '/finance/feeItem', 'Wallet', 3, 2, 1, 1),
(76, 40, '收费规则管理', 'fee:rule:list', '/finance/feeRule', 'Money', 4, 2, 1, 1),
-- 收费类型按钮
(73, 72, '收费类型新增', 'fee:item:add', NULL, NULL, 1, 3, 0, 1),
(74, 72, '收费类型编辑', 'fee:item:edit', NULL, NULL, 2, 3, 0, 1),
(75, 72, '收费类型删除', 'fee:item:delete', NULL, NULL, 3, 3, 0, 1),
-- 收费规则按钮
(77, 76, '收费规则新增', 'fee:rule:add', NULL, NULL, 1, 3, 0, 1),
(78, 76, '收费规则编辑', 'fee:rule:edit', NULL, NULL, 2, 3, 0, 1),
(79, 76, '收费规则删除', 'fee:rule:delete', NULL, NULL, 3, 3, 0, 1);

-- 4. 超级管理员授予全部新增菜单（72-79）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 72 AND 79
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 5. 初始化收费类型（集团模板 company_id=0，子公司可自建扩展）
INSERT INTO biz_fee_item (company_id, fee_item_name, calc_unit, remark, create_by) VALUES
(0, '租金', '元/月', '集团模板：摊位租金收费', 1),
(0, '物业费', '元/月', '集团模板：物业管理服务费', 1),
(0, '水费', '元/吨', '集团模板：用水收费', 1),
(0, '电费', '元/度', '集团模板：用电收费', 1),
(0, '押金', '元', '集团模板：租赁押金', 1),
(0, '其他', '元', '集团模板：一次性杂费等', 1);

-- ============================================================
-- 来源: upgrade_v1.8_fee_rule_stall_rel.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.8
-- 模块：财务管理-自定义收费 × 租赁摊位
-- 内容：biz_fee_rule_stall_rel 收费规则-摊位绑定关联表
-- 说明：摊位管理页多选收费规则（biz_fee_rule），约束同一收费类型（biz_fee_item）只能选一条；
--       override_flag/override_price/override_config_json 为单摊位特殊规则覆盖预留（本期界面不提供）
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.8_fee_rule_stall_rel.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 收费规则-摊位绑定表（多对多；rule_id + stall_id 同公司唯一）

-- ============================================================
-- 来源: upgrade_v1.9_fee_period_none.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 修正脚本 V1.9
-- 模块：财务管理-自定义收费
-- 内容：biz_fee_rule.period_type 增加取值 0=不使用周期
-- 说明：水费/电费（仪表计量按用量收费）、押金（一次性）等类型不需要周期概念，
--       规则配置时可选 0 不使用周期；仅同步字段注释，不改变既有数据与索引
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.9_fee_period_none.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 收费周期字段注释同步（0=不使用周期）
ALTER TABLE `biz_fee_rule`
  MODIFY COLUMN `period_type` tinyint NOT NULL DEFAULT 2 COMMENT '收费周期 0不使用 1按年 2按月 3按日（水费/电费/押金等类型可配置0不使用周期）';

-- ============================================================
-- 来源: upgrade_v1.10_bill_fee_category.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.10
-- 模块：财务管理-自定义收费 x 水电物业账单
-- 内容1：biz_fee_item 增加 category_type 收费类别编码（1租金 2物业费 3水费 4电费 5押金 6其他）
--        -- 账单生成按类别编码匹配摊位绑定规则（名称可改、编码稳定），历史数据按名称回填
-- 内容2：water_elec_bill 单价快照说明（不再添加三列，由 v1.11 统一结构替代）
--        -- 新结构使用 category+usage+unit_price+total_amount 统一字段
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.10_bill_fee_category.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 收费类型增加类别编码字段
ALTER TABLE `biz_fee_item`
  ADD COLUMN `category_type` tinyint NOT NULL DEFAULT 6 COMMENT '收费类别 1租金 2物业费 3水费 4电费 5押金 6其他' AFTER `fee_item_name`;

-- 2. 历史数据按名称回填类别编码（未匹配名称的保持 6=其他）
UPDATE `biz_fee_item` SET `category_type` = 1 WHERE `fee_item_name` = '租金';
UPDATE `biz_fee_item` SET `category_type` = 2 WHERE `fee_item_name` = '物业费';
UPDATE `biz_fee_item` SET `category_type` = 3 WHERE `fee_item_name` = '水费';
UPDATE `biz_fee_item` SET `category_type` = 4 WHERE `fee_item_name` = '电费';
UPDATE `biz_fee_item` SET `category_type` = 5 WHERE `fee_item_name` = '押金';

-- 3. 水电物业账单单价快照说明：
--    原设计为添加 water_price/elec_price/property_price 三列，
--    但 v1.11 已将 water_elec_bill 重构为 category+usage+unit_price+total_amount 统一结构，
--    单价统一由 unit_price 字段承载，不再需要三列分拆。
--    请按顺序执行 upgrade_v1.11_water_elec_bill_restructure.sql 完成账单表重构。

-- ============================================================
-- 来源: upgrade_v1.11_water_elec_bill_restructure.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.11
-- 模块：水电物业账单表结构重构
-- 内容：water_elec_bill 由多字段（water_usage/elec_usage/property_amount...）
--       改为单类别行（category + usage + unit_price + total_amount）
-- 说明：此版本为设计稿，当前水费/电费/物业费分列字段，
--       重构后每条账单记录代表一个类别，账单月份+摊位可有多条记录
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.11_water_elec_bill_restructure.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 备份旧表数据（如有生产数据，先导出）
-- CREATE TABLE IF NOT EXISTS `water_elec_bill_bak` LIKE `water_elec_bill`;
-- INSERT INTO `water_elec_bill_bak` SELECT * FROM `water_elec_bill`;

-- 2. 删除旧表并重建（DROP + CREATE，与 install.sql 保持一致）

-- 3. 兼容旧 v1.10 脚本中的 water_price/elec_price/property_price 列说明：
--    v1.10 新增的这三个列在新表中已移除，统一由 unit_price 替代。
--    若已执行 v1.10，请在执行本脚本前先执行以下清理（如 v1.10 未执行则忽略）：
-- ALTER TABLE `water_elec_bill` DROP COLUMN `water_price`, DROP COLUMN `elec_price`, DROP COLUMN `property_price`;


-- ============================================================
-- 来源: upgrade_v1.12_property_bill.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V1.12
-- 模块：物业费账单独立表 property_bill
-- 内容：新建 property_bill 表（物业费独立账单），拆分自 water_elec_bill
-- 说明：water_elec_bill 现仅保留水费(category=3)和电费(category=4)，
--       物业费(category=2)历史数据保留但不再新增长，新账单写入 property_bill
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.12_property_bill.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 创建物业费账单表

-- 2. 更新 water_elec_bill 注释，明确物业费已拆分至 property_bill
ALTER TABLE `water_elec_bill` COMMENT = '水电费月度账单表（水费category=3/电费category=4，物业费已拆分至property_bill）';

-- 3. 新增物业费账单菜单及权限（menu_id 103-110，衔接 upgrade_v2.0 的 80-102）
-- 目录：物业费管理（parent=36 物业收费）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
(103, 36, '物业费账单', 'property:feeBill:list', '/property/feeBill', 'Document', 4, 2, 1, 1),
-- 物业费账单按钮
(104, 103, '批量生成', 'property:feeBill:generate', NULL, NULL, 1, 3, 0, 1),
(105, 103, '单条生成', 'property:feeBill:generateSingle', NULL, NULL, 2, 3, 0, 1),
(106, 103, '账单导出', 'property:feeBill:export', NULL, NULL, 3, 3, 0, 1);

-- 4. 超级管理员授予新增菜单（103-106）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 103 AND 106
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- ============================================================
-- 来源: upgrade_v2.0_recvpay_flow.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 升级脚本 V2.0
-- 模块：应收应付计划 + 统一审批引擎 + 优惠管理 + 核销分摊 + 自动对账 + 红冲
-- 内容：biz_recv_pay_plan / biz_finance_writeoff / bill_plan_rel
--       / flow_definition / flow_instance / flow_task / flow_record
--       / biz_discount_policy / biz_discount_apply 建表 + 现有表 ALTER 4 处
--       + 菜单 80-102 + 超管授权 + 初始流程定义 + 集团阈值参数 + 初始优惠策略
-- 执行方式: cmd /c "mysql.exe -uroot -p你的密码 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.0_recvpay_flow.sql"
-- 权限标识与后端 PermissionConst / 前端路由 meta.permission 对齐（plan:* / discount:* / flow:*）
-- 注意：ALTER 语句无存在性判断，重复执行前请确认目标列已存在，否则报错；
--       若此前执行在 10.3（biz_fee_bill 不存在）处中断，请改用 upgrade_v2.1_fee_bill_resume.sql 恢复
-- 依据规范：应收应付计划+统一审批引擎+优惠管理模块设计规范.md（V2）
-- ============================================================

SET NAMES utf8mb4;

-- 1. 应收应付计划主表（全系统唯一应收应付台账）

-- 2. 核销分摊明细表（资金流水↔应收应付计划精确对账，自动对账引擎关键）

-- 3. 账单-应收应付计划关联中间表（一账单多计划、多计划合并账单）

-- 4. 流程定义表（集团全局模板 company_id=0，子公司只读复用）

-- 5. 流程实例表

-- 6. 审批任务表（待办/已办）

-- 7. 流程流转记录表（全流程留痕）

-- 8. 优惠策略表（集团模板 company_id=0 全子公司可见）

-- 9. 优惠申请单表

-- 10. 现有表 ALTER（挂靠计划/审批实例，对账底座）
-- 注意：无存在性判断，重复执行前确认列已存在

-- 10.1 财务流水挂靠计划
ALTER TABLE `biz_finance_flow`
  ADD COLUMN `plan_id` bigint DEFAULT NULL COMMENT '关联应收应付计划ID（biz_recv_pay_plan，核销时写入）' AFTER `bill_id`,
  ADD INDEX `idx_plan_id` (`plan_id`);

-- 10.2 水电账单挂靠计划（一期保留 plan_id 直连字段兼容单计划；多计划走 bill_plan_rel）
ALTER TABLE `water_elec_bill`
  ADD COLUMN `plan_id` bigint DEFAULT NULL COMMENT '关联应收应付计划ID（同摊位月度计划，可空）' AFTER `merchant_id`;

-- 10.3 收费规则账单表（补建）+ 挂靠计划
-- 说明：biz_fee_bill/biz_fee_bill_detail 为收费规则周期账单表（V2 账单-计划联动预留），
--       v1.x 脚本未建表，此处补建后再加 plan_id；表结构对齐《MySQL8.0数据库设计规范》7.4.2，
--       period_type 与 biz_fee_rule 对齐（0不使用 1按年 2按月 3按日）
CREATE TABLE IF NOT EXISTS `biz_fee_bill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint DEFAULT NULL COMMENT '商户ID（可空，账单可无商户绑定）',
  `bill_month` varchar(32) NOT NULL COMMENT '账单周期标识（月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性once）',
  `rule_id` bigint NOT NULL COMMENT '生成账单的规则ID（biz_fee_rule，锁定后仅记录不回溯）',
  `period_type` tinyint NOT NULL DEFAULT 2 COMMENT '账单周期 0不使用 1按年 2按月 3按日（与biz_fee_rule一致）',
  `original_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收原价合计',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免金额合计',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `real_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际应收 = 原价 - 优惠 + 调账',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime DEFAULT NULL COMMENT '缴费完成时间',
  `locked_flag` tinyint NOT NULL DEFAULT 1 COMMENT '账单锁定 1锁定（生成即锁定，规则变更不回溯）',
  `remark` varchar(500) DEFAULT NULL COMMENT '账单备注',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（缴费完成禁止删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stall_rule_period` (`company_id`,`stall_id`,`rule_id`,`bill_month`,`is_delete`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_stall_id` (`stall_id`),
  INDEX `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义收费周期账单表（收费规则账单，经plan_id/bill_plan_rel挂靠应收应付计划）';

CREATE TABLE IF NOT EXISTS `biz_fee_bill_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint NOT NULL COMMENT '关联账单ID',
  `fee_item_id` bigint NOT NULL COMMENT '收费项ID',
  `fee_item_name` varchar(128) NOT NULL COMMENT '收费项名称快照（生成时固化，规则修改不回溯）',
  `calc_mode` tinyint NOT NULL DEFAULT 1 COMMENT '收费方式快照 1定额 2按面积',
  `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
  `base_value` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '计费基数（面积等）',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '该项金额小计',
  `detail_config_json` mediumtext DEFAULT NULL COMMENT '计费过程数据JSON（审计追溯，后端过滤脚本后入库）',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费账单明细快照表';

ALTER TABLE `biz_fee_bill`
  ADD COLUMN `plan_id` bigint DEFAULT NULL COMMENT '关联应收应付计划ID（可空）' AFTER `rule_id`;

-- 10.4 合同关联审批实例（作废终止/大额优惠审批）
ALTER TABLE `stall_contract`
  ADD COLUMN `flow_instance_id` bigint DEFAULT NULL COMMENT '关联审批实例ID（作废终止/大额优惠审批）' AFTER `contract_status`;

-- 11. 新增菜单（id 80-102，与 v1.7 已用 72-79 衔接；审批中心挂顶级中台目录）
INSERT INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 财务管理-计划/优惠页面（parent 40）
(80, 40, '应收应付计划', 'plan:recvpay:list', '/finance/recvPayPlan', 'Calendar', 5, 2, 1, 1),
(86, 40, '优惠策略', 'discount:policy:list', '/finance/discountPolicy', 'Discount', 6, 2, 1, 1),
(90, 40, '优惠申请', 'discount:apply:list', '/finance/discountApply', 'Ticket', 7, 2, 1, 1),
-- 应收应付计划按钮
(81, 80, '计划生成', 'plan:recvpay:generate', NULL, NULL, 1, 3, 0, 1),
(82, 80, '计划调账', 'plan:recvpay:adjust', NULL, NULL, 2, 3, 0, 1),
(83, 80, '计划作废终止', 'plan:recvpay:terminate', NULL, NULL, 3, 3, 0, 1),
(84, 80, '计划导出', 'plan:recvpay:export', NULL, NULL, 4, 3, 0, 1),
(85, 80, '计划对账', 'plan:recvpay:reconcile', NULL, NULL, 5, 3, 0, 1),
-- 优惠策略按钮
(87, 86, '优惠策略新增', 'discount:policy:add', NULL, NULL, 1, 3, 0, 1),
(88, 86, '优惠策略编辑', 'discount:policy:edit', NULL, NULL, 2, 3, 0, 1),
(89, 86, '优惠策略删除', 'discount:policy:delete', NULL, NULL, 3, 3, 0, 1),
-- 优惠申请按钮
(91, 90, '优惠申请撤销', 'discount:apply:cancel', NULL, NULL, 1, 3, 0, 1),
-- 审批中心目录（parent 0）
(92, 0, '审批中心', NULL, '/flow', 'Finished', 5, 1, 1, 1),
-- 审批中心页面
(93, 92, '待办处理', 'flow:task:list', '/flow/task', 'List', 1, 2, 1, 1),
(94, 92, '我的申请', 'flow:apply:list', '/flow/apply', 'EditPen', 2, 2, 1, 1),
(95, 92, '流程定义', 'flow:def:list', '/flow/definition', 'Setting', 3, 2, 1, 1),
(96, 92, '流程实例', 'flow:instance:list', '/flow/instance', 'Document', 4, 2, 1, 1),
-- 审批中心按钮
(97, 93, '审批处理', 'flow:task:handle', NULL, NULL, 1, 3, 0, 1),
(98, 93, '催办', 'flow:task:urge', NULL, NULL, 2, 3, 0, 1),
(99, 94, '撤销申请', 'flow:apply:cancel', NULL, NULL, 1, 3, 0, 1),
(100, 95, '流程定义新增', 'flow:def:add', NULL, NULL, 1, 3, 0, 1),
(101, 95, '流程定义编辑', 'flow:def:edit', NULL, NULL, 2, 3, 0, 1),
(102, 95, '流程定义删除', 'flow:def:delete', NULL, NULL, 3, 3, 0, 1);

-- 12. 超级管理员授予全部新增菜单（80-102）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 80 AND 102
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 13. 初始流程定义（集团全局 company_id=0）
-- 一期启用：contract / contract_discount / contract_terminate / plan_adjust
-- 二期预置：reimburse / reimburse_large / purchase / purchase_large（status=0 停用，由金额阈值参数控制启用）
INSERT INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by) VALUES
(0, '租赁合同审批', 'contract', 'contract',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 1, '租赁合同单据自身审批：新建/变更，通过后进入优惠阈值判定', 1),
(0, '租赁合同大额优惠审批', 'contract_discount', 'contract_discount',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 1, '优惠申请超集团阈值自动发起，通过后合同优惠生效', 1),
(0, '合同作废终止审批', 'contract_terminate', 'contract_terminate',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"集团财务复核","nodeMode":"single","handlerType":"role","handlerValue":"group_finance"}]}',
 1, '退租/作废高危操作，通过后执行红冲链', 1),
(0, '计划大额调账审批', 'plan_adjust', 'plan_adjust',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 1, '单笔调账绝对值超 plan.adjust_amount_limit 必须审批', 1),
(0, '报销审批', 'reimburse', 'reimburse',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 0, '二期启用：报销单自身审批，通过后生成应付计划(direction=2)', 1),
(0, '大额报销审批', 'reimburse_large', 'reimburse',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 0, '二期启用：报销金额超 reimburse.amount_limit 自动选用', 1),
(0, '采购审批', 'purchase', 'purchase',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 0, '二期启用：采购单自身审批，通过后生成应付计划(direction=2)', 1),
(0, '大额采购审批', 'purchase_large', 'purchase',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 0, '二期启用：采购金额超 purchase.amount_limit 自动选用', 1);

-- 14. 集团阈值参数（company_id=0 集团全局，子公司只读）
INSERT INTO sys_config (company_id, config_key, config_value, config_name, remark, create_by, is_delete) VALUES
(0, 'discount.waive_months_limit', '3', '免租期上限（月）', '优惠申请免租期超限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.min_rate_limit', '80', '折扣率下限（%）', '优惠申请折扣低于下限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.max_deduct_limit', '5000.00', '单合同减免金额上限（元）', '优惠申请减免超上限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.contract_ratio_limit', '10.00', '优惠占合同总租金比例上限（%）', '优惠总额占比超限 → need_audit=1 自动发起审批', 1, 0),
(0, 'plan.adjust_amount_limit', '2000.00', '计划单笔调账阈值（元）', '调账绝对值超阈值必须走审批引擎', 1, 0),
(0, 'plan.overdue_remind_days', '7', '逾期提醒天数', '计划逾期N天推送站内信/短信提醒', 1, 0),
(0, 'reimburse.amount_limit', '5000.00', '报销单金额阈值（元）', '报销金额超阈值自动选用 reimburse_large 大额流程', 1, 0),
(0, 'purchase.amount_limit', '20000.00', '采购单金额阈值（元）', '采购金额超阈值自动选用 purchase_large 大额流程', 1, 0)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 15. 初始优惠策略模板（集团模板 company_id=0，子公司可在此基础上自定义覆盖）
INSERT INTO biz_discount_policy (company_id, policy_name, discount_type, waive_months, discount_rate, deduct_amount, scope_type, status, remark, create_by) VALUES
(0, '开业免租1个月', 1, 1, 100.00, 0.00, 1, 1, '集团模板：开业免租1个月（免租期优惠）', 1);

-- ============================================================
-- 来源: upgrade_v2.1_fee_bill_resume.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 恢复脚本 V2.1
-- 用途：upgrade_v2.0_recvpay_flow.sql 在 10.3（biz_fee_bill 不存在）处中断后的恢复
-- 内容：补建 biz_fee_bill / biz_fee_bill_detail + 执行 v2.0 未完成的 10.3/10.4 ALTER
--       + 菜单 80-102 + 超管授权 + 初始流程定义 + 集团阈值参数 + 初始优惠策略
-- 幂等：全部语句可重复执行（IF NOT EXISTS / INSERT IGNORE / NOT EXISTS / ON DUPLICATE KEY）
-- 前置：v2.0 的 10.1（biz_finance_flow.plan_id）与 10.2（water_elec_bill.plan_id）已执行；
--       全新环境无需本脚本，直接执行修补后的 upgrade_v2.0_recvpay_flow.sql 即可
-- 执行方式: cmd /c "mysql.exe -uroot -p你的密码 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.1_fee_bill_resume.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 补建收费规则账单表（对齐《MySQL8.0数据库设计规范》7.4.2，period_type 与 biz_fee_rule 一致）
CREATE TABLE IF NOT EXISTS `biz_fee_bill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint DEFAULT NULL COMMENT '商户ID（可空，账单可无商户绑定）',
  `bill_month` varchar(32) NOT NULL COMMENT '账单周期标识（月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性once）',
  `rule_id` bigint NOT NULL COMMENT '生成账单的规则ID（biz_fee_rule，锁定后仅记录不回溯）',
  `period_type` tinyint NOT NULL DEFAULT 2 COMMENT '账单周期 0不使用 1按年 2按月 3按日（与biz_fee_rule一致）',
  `original_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收原价合计',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免金额合计',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `real_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际应收 = 原价 - 优惠 + 调账',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime DEFAULT NULL COMMENT '缴费完成时间',
  `locked_flag` tinyint NOT NULL DEFAULT 1 COMMENT '账单锁定 1锁定（生成即锁定，规则变更不回溯）',
  `remark` varchar(500) DEFAULT NULL COMMENT '账单备注',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除（缴费完成禁止删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stall_rule_period` (`company_id`,`stall_id`,`rule_id`,`bill_month`,`is_delete`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_stall_id` (`stall_id`),
  INDEX `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义收费周期账单表（收费规则账单，经plan_id/bill_plan_rel挂靠应收应付计划）';

CREATE TABLE IF NOT EXISTS `biz_fee_bill_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint NOT NULL COMMENT '关联账单ID',
  `fee_item_id` bigint NOT NULL COMMENT '收费项ID',
  `fee_item_name` varchar(128) NOT NULL COMMENT '收费项名称快照（生成时固化，规则修改不回溯）',
  `calc_mode` tinyint NOT NULL DEFAULT 1 COMMENT '收费方式快照 1定额 2按面积',
  `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
  `base_value` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '计费基数（面积等）',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '该项金额小计',
  `detail_config_json` mediumtext DEFAULT NULL COMMENT '计费过程数据JSON（审计追溯，后端过滤脚本后入库）',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费账单明细快照表';

-- 2. v2.0 未完成的 ALTER（中断库尚未应用，直接执行；若已存在该列则跳过本段）
ALTER TABLE `biz_fee_bill`
  ADD COLUMN `plan_id` bigint DEFAULT NULL COMMENT '关联应收应付计划ID（可空）' AFTER `rule_id`;

ALTER TABLE `stall_contract`
  ADD COLUMN `flow_instance_id` bigint DEFAULT NULL COMMENT '关联审批实例ID（作废终止/大额优惠审批）' AFTER `contract_status`;

-- 3. 菜单 80-102（INSERT IGNORE 幂等）
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, permission, path, icon, sort_order, menu_type, visible, create_by) VALUES
-- 财务管理-计划/优惠页面（parent 40）
(80, 40, '应收应付计划', 'plan:recvpay:list', '/finance/recvPayPlan', 'Calendar', 5, 2, 1, 1),
(86, 40, '优惠策略', 'discount:policy:list', '/finance/discountPolicy', 'Discount', 6, 2, 1, 1),
(90, 40, '优惠申请', 'discount:apply:list', '/finance/discountApply', 'Ticket', 7, 2, 1, 1),
-- 应收应付计划按钮
(81, 80, '计划生成', 'plan:recvpay:generate', NULL, NULL, 1, 3, 0, 1),
(82, 80, '计划调账', 'plan:recvpay:adjust', NULL, NULL, 2, 3, 0, 1),
(83, 80, '计划作废终止', 'plan:recvpay:terminate', NULL, NULL, 3, 3, 0, 1),
(84, 80, '计划导出', 'plan:recvpay:export', NULL, NULL, 4, 3, 0, 1),
(85, 80, '计划对账', 'plan:recvpay:reconcile', NULL, NULL, 5, 3, 0, 1),
-- 优惠策略按钮
(87, 86, '优惠策略新增', 'discount:policy:add', NULL, NULL, 1, 3, 0, 1),
(88, 86, '优惠策略编辑', 'discount:policy:edit', NULL, NULL, 2, 3, 0, 1),
(89, 86, '优惠策略删除', 'discount:policy:delete', NULL, NULL, 3, 3, 0, 1),
-- 优惠申请按钮
(91, 90, '优惠申请撤销', 'discount:apply:cancel', NULL, NULL, 1, 3, 0, 1),
-- 审批中心目录（parent 0）
(92, 0, '审批中心', NULL, '/flow', 'Finished', 5, 1, 1, 1),
-- 审批中心页面
(93, 92, '待办处理', 'flow:task:list', '/flow/task', 'List', 1, 2, 1, 1),
(94, 92, '我的申请', 'flow:apply:list', '/flow/apply', 'EditPen', 2, 2, 1, 1),
(95, 92, '流程定义', 'flow:def:list', '/flow/definition', 'Setting', 3, 2, 1, 1),
(96, 92, '流程实例', 'flow:instance:list', '/flow/instance', 'Document', 4, 2, 1, 1),
-- 审批中心按钮
(97, 93, '审批处理', 'flow:task:handle', NULL, NULL, 1, 3, 0, 1),
(98, 93, '催办', 'flow:task:urge', NULL, NULL, 2, 3, 0, 1),
(99, 94, '撤销申请', 'flow:apply:cancel', NULL, NULL, 1, 3, 0, 1),
(100, 95, '流程定义新增', 'flow:def:add', NULL, NULL, 1, 3, 0, 1),
(101, 95, '流程定义编辑', 'flow:def:edit', NULL, NULL, 2, 3, 0, 1),
(102, 95, '流程定义删除', 'flow:def:delete', NULL, NULL, 3, 3, 0, 1);

-- 4. 超级管理员授予全部新增菜单（NOT EXISTS 幂等）
INSERT INTO sys_role_menu_rel (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m WHERE m.id BETWEEN 80 AND 102
AND NOT EXISTS (SELECT 1 FROM sys_role_menu_rel r WHERE r.role_id = 1 AND r.menu_id = m.id);

-- 5. 初始流程定义（INSERT IGNORE，uk_def_code 幂等）
INSERT IGNORE INTO flow_definition (company_id, def_name, def_code, biz_type, node_config_json, status, remark, create_by) VALUES
(0, '租赁合同审批', 'contract', 'contract',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 1, '租赁合同单据自身审批：新建/变更，通过后进入优惠阈值判定', 1),
(0, '租赁合同大额优惠审批', 'contract_discount', 'contract_discount',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 1, '优惠申请超集团阈值自动发起，通过后合同优惠生效', 1),
(0, '合同作废终止审批', 'contract_terminate', 'contract_terminate',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"集团财务复核","nodeMode":"single","handlerType":"role","handlerValue":"group_finance"}]}',
 1, '退租/作废高危操作，通过后执行红冲链', 1),
(0, '计划大额调账审批', 'plan_adjust', 'plan_adjust',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 1, '单笔调账绝对值超 plan.adjust_amount_limit 必须审批', 1),
(0, '报销审批', 'reimburse', 'reimburse',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 0, '二期启用：报销单自身审批，通过后生成应付计划(direction=2)', 1),
(0, '大额报销审批', 'reimburse_large', 'reimburse',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 0, '二期启用：报销金额超 reimburse.amount_limit 自动选用', 1),
(0, '采购审批', 'purchase', 'purchase',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"}]}',
 0, '二期启用：采购单自身审批，通过后生成应付计划(direction=2)', 1),
(0, '大额采购审批', 'purchase_large', 'purchase',
 '{"nodes":[{"nodeName":"子公司经理审批","nodeMode":"single","handlerType":"role","handlerValue":"sub_manager"},{"nodeName":"财务复核","nodeMode":"single","handlerType":"role","handlerValue":"finance_admin"}]}',
 0, '二期启用：采购金额超 purchase.amount_limit 自动选用', 1);

-- 6. 集团阈值参数（ON DUPLICATE KEY UPDATE 幂等）
INSERT INTO sys_config (company_id, config_key, config_value, config_name, remark, create_by, is_delete) VALUES
(0, 'discount.waive_months_limit', '3', '免租期上限（月）', '优惠申请免租期超限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.min_rate_limit', '80', '折扣率下限（%）', '优惠申请折扣低于下限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.max_deduct_limit', '5000.00', '单合同减免金额上限（元）', '优惠申请减免超上限 → need_audit=1 自动发起审批', 1, 0),
(0, 'discount.contract_ratio_limit', '10.00', '优惠占合同总租金比例上限（%）', '优惠总额占比超限 → need_audit=1 自动发起审批', 1, 0),
(0, 'plan.adjust_amount_limit', '2000.00', '计划单笔调账阈值（元）', '调账绝对值超阈值必须走审批引擎', 1, 0),
(0, 'plan.overdue_remind_days', '7', '逾期提醒天数', '计划逾期N天推送站内信/短信提醒', 1, 0),
(0, 'reimburse.amount_limit', '5000.00', '报销单金额阈值（元）', '报销金额超阈值自动选用 reimburse_large 大额流程', 1, 0),
(0, 'purchase.amount_limit', '20000.00', '采购单金额阈值（元）', '采购金额超阈值自动选用 purchase_large 大额流程', 1, 0)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 7. 初始优惠策略模板（NOT EXISTS 幂等）
INSERT INTO biz_discount_policy (company_id, policy_name, discount_type, waive_months, discount_rate, deduct_amount, scope_type, status, remark, create_by)
SELECT 0, '开业免租1个月', 1, 1, 100.00, 0.00, 1, 1, '集团模板：开业免租1个月（免租期优惠）', 1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM biz_discount_policy WHERE company_id = 0 AND policy_name = '开业免租1个月');

-- ============================================================
-- 来源: upgrade_v2.2_flow_no.sql
-- ============================================================
-- ============================================================
-- upgrade_v2.2_flow_no.sql
-- 用途：
--   1. biz_finance_flow 增加财务流水单号字段 flow_no
--   2. water_elec_pay_record 增加关联流水单号字段 flow_no
--   3. 新增 biz_flow_seq 顺序号计数器表（公司+日维度，防并发重号）
-- 执行方式: cmd /c "mysql.exe -uroot -p你的密码 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.2_flow_no.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. biz_finance_flow 增加 flow_no 列及索引（幂等）
ALTER TABLE `biz_finance_flow`
  ADD COLUMN `flow_no` varchar(64) DEFAULT NULL COMMENT '财务流水单号（YO+公司编码+日期+流水序号）' AFTER `trade_no`,
  ADD INDEX `idx_flow_no` (`flow_no`);

-- 2. water_elec_pay_record 增加关联流水单号列及索引（幂等）
ALTER TABLE `water_elec_pay_record`
  ADD COLUMN `flow_no` varchar(64) DEFAULT NULL COMMENT '关联财务流水单号' AFTER `remark`,
  ADD INDEX `idx_flow_no` (`flow_no`);

-- 3. 新建顺序号计数器表（幂等）
CREATE TABLE IF NOT EXISTS `biz_flow_seq` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '所属子公司ID',
  `seq_date` date NOT NULL COMMENT '日期 yyyy-MM-dd',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '当日已分配序号',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_date` (`company_id`, `seq_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务流水单号顺序号计数器（公司+日维度，防并发重号）';

-- ============================================================
-- 来源: fix_v2.2_stall_contract_merchant_null.sql
-- ============================================================
-- ============================================================
-- 集团多业态一体化管控系统 修正脚本 V2.2
-- 模块：租赁合同
-- 内容：stall_contract.merchant_id 允许为空
-- 说明：合同业务 v1.2 起切换为租户体系（tenant_id 关联 stall_tenant），
--       新增合同不再强制绑定旧商户（stall_merchant），而 install.sql 将
--       merchant_id 定义为 NOT NULL 无默认值，严格模式 INSERT 报
--       "Field 'merchant_id' doesn't have a default value"
--       （与 fix_v1.6_bill_merchant_null.sql 同款问题）
-- 执行方式: cmd /c "mysql.exe -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < fix_v2.2_stall_contract_merchant_null.sql"
-- ============================================================

SET NAMES utf8mb4;

-- 1. 商户ID允许为空（合同归属以 tenant_id 为准，merchant_id 兼容旧数据）
ALTER TABLE `stall_contract`
  MODIFY COLUMN `merchant_id` bigint DEFAULT NULL COMMENT '商户ID（v2.0起合同改用租户体系，可空）';

-- ============================================================
-- 来源: upgrade_v2.3_flow_snapshot.sql
-- ============================================================
-- ============================================================
-- upgrade_v2.3_flow_snapshot.sql
-- 用途：biz_finance_flow 和 water_elec_pay_record 增加快照字段
--      实现财务流水审计合规：
--      1. 物业场景：摊位/商户信息写入时固化
--      2. 非物业场景：缴费人/合同信息写入时固化
--      3. 冲红/作废状态字段
-- 执行方式:
--   mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.3_flow_snapshot.sql
-- 幂等性：通过存储过程检查列是否存在，可重复执行
-- ============================================================

SET NAMES utf8mb4;

-- 辅助：检查列是否存在
DROP PROCEDURE IF EXISTS sp_add_column_if_not_exists;
DELIMITER $$
CREATE PROCEDURE sp_add_column_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND column_name = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 辅助：检查索引是否存在
DROP PROCEDURE IF EXISTS sp_add_index_if_not_exists;
DELIMITER $$
CREATE PROCEDURE sp_add_index_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_columns VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND index_name = p_index
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD INDEX ', p_index, ' (', p_columns, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 1. biz_finance_flow — 物业场景快照（摊位/商户）
CALL sp_add_column_if_not_exists('biz_finance_flow', 'stall_number', 'varchar(64) DEFAULT NULL COMMENT ''摊位编号快照（写入时固化）'' AFTER stall_id');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'stall_name', 'varchar(128) DEFAULT NULL COMMENT ''摊位名称快照（写入时固化）'' AFTER stall_number');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'stall_market_name', 'varchar(128) DEFAULT NULL COMMENT ''所属市场名称快照（写入时固化）'' AFTER stall_name');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'category_name', 'varchar(64) DEFAULT NULL COMMENT ''租赁分类名称快照（写入时固化）'' AFTER stall_market_name');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'merchant_name', 'varchar(128) DEFAULT NULL COMMENT ''商户名称快照（写入时固化）'' AFTER category_name');

-- 2. biz_finance_flow — 非物业场景快照（缴费人/合同）
CALL sp_add_column_if_not_exists('biz_finance_flow', 'payer_name', 'varchar(64) DEFAULT NULL COMMENT ''缴费人姓名（写入时固化）'' AFTER merchant_name');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'payer_phone', 'varchar(32) DEFAULT NULL COMMENT ''缴费人手机号（写入时固化）'' AFTER payer_name');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'payer_company_name', 'varchar(128) DEFAULT NULL COMMENT ''缴费人公司名称（写入时固化）'' AFTER payer_phone');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'payer_type', 'tinyint DEFAULT NULL COMMENT ''缴费人类型 1个人 2企业（写入时固化）'' AFTER payer_company_name');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'contract_no', 'varchar(64) DEFAULT NULL COMMENT ''关联合同编号（写入时固化）'' AFTER payer_type');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'contract_id', 'bigint DEFAULT NULL COMMENT ''关联合同ID（写入时固化）'' AFTER contract_no');

-- 3. biz_finance_flow — 冲红/作废状态字段
CALL sp_add_column_if_not_exists('biz_finance_flow', 'flow_status', 'tinyint NOT NULL DEFAULT 1 COMMENT ''冲红/作废状态 1正常 2冲红中 3已冲红 4已作废'' AFTER status');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'red_flush_flow_id', 'bigint DEFAULT NULL COMMENT ''冲红反向流水ID（已冲红时写入）'' AFTER flow_status');
CALL sp_add_column_if_not_exists('biz_finance_flow', 'void_reason', 'varchar(500) DEFAULT NULL COMMENT ''作废原因'' AFTER red_flush_flow_id');

-- 4. biz_finance_flow — 新增索引
CALL sp_add_index_if_not_exists('biz_finance_flow', 'idx_contract_id', 'contract_id');
CALL sp_add_index_if_not_exists('biz_finance_flow', 'idx_payer_name', 'payer_name');
CALL sp_add_index_if_not_exists('biz_finance_flow', 'idx_flow_status', 'flow_status');
CALL sp_add_index_if_not_exists('biz_finance_flow', 'idx_merchant_id', 'merchant_id');

-- 5. water_elec_pay_record — 物业场景快照（摊位/商户）
CALL sp_add_column_if_not_exists('water_elec_pay_record', 'stall_number', 'varchar(64) DEFAULT NULL COMMENT ''摊位编号快照（写入时固化）'' AFTER merchant_id');
CALL sp_add_column_if_not_exists('water_elec_pay_record', 'stall_name', 'varchar(128) DEFAULT NULL COMMENT ''摊位名称快照（写入时固化）'' AFTER stall_number');
CALL sp_add_column_if_not_exists('water_elec_pay_record', 'stall_market_name', 'varchar(128) DEFAULT NULL COMMENT ''所属市场名称快照（写入时固化）'' AFTER stall_name');
CALL sp_add_column_if_not_exists('water_elec_pay_record', 'category_name', 'varchar(64) DEFAULT NULL COMMENT ''租赁分类名称快照（写入时固化）'' AFTER stall_market_name');
CALL sp_add_column_if_not_exists('water_elec_pay_record', 'merchant_name', 'varchar(128) DEFAULT NULL COMMENT ''商户名称快照（写入时固化）'' AFTER category_name');

-- 6. 回填存量数据（biz_finance_flow）— 摊位/商户快照
UPDATE biz_finance_flow f
  LEFT JOIN stall_info s ON f.stall_id = s.id
  LEFT JOIN stall_category c ON s.stall_category_id = c.id
  LEFT JOIN stall_tenant t ON f.merchant_id = t.id
SET
  f.stall_number     = COALESCE(f.stall_number, s.stall_number),
  f.stall_name       = COALESCE(f.stall_name, s.stall_name),
  f.category_name    = COALESCE(f.category_name, c.category_name),
  f.merchant_name    = COALESCE(f.merchant_name, t.tenant_name)
WHERE (f.stall_number IS NULL OR f.stall_name IS NULL OR f.category_name IS NULL OR f.merchant_name IS NULL)
  AND (f.stall_id IS NOT NULL OR f.merchant_id IS NOT NULL);

-- 6b. 回填 market_name（需要子查询）
UPDATE biz_finance_flow f
  JOIN stall_info s ON f.stall_id = s.id
SET f.stall_market_name = (SELECT m.market_name FROM market_info m WHERE m.id = s.market_id)
WHERE f.stall_market_name IS NULL AND s.market_id IS NOT NULL;

-- 7. 回填存量数据（water_elec_pay_record）— 摊位/商户快照
UPDATE water_elec_pay_record r
  LEFT JOIN stall_info s ON r.stall_id = s.id
  LEFT JOIN stall_category c ON s.stall_category_id = c.id
  LEFT JOIN stall_tenant t ON r.merchant_id = t.id
SET
  r.stall_number     = COALESCE(r.stall_number, s.stall_number),
  r.stall_name       = COALESCE(r.stall_name, s.stall_name),
  r.category_name    = COALESCE(r.category_name, c.category_name),
  r.merchant_name    = COALESCE(r.merchant_name, t.tenant_name)
WHERE (r.stall_number IS NULL OR r.stall_name IS NULL OR r.category_name IS NULL OR r.merchant_name IS NULL)
  AND r.stall_id IS NOT NULL;

-- 7b. 回填 market_name
UPDATE water_elec_pay_record r
  JOIN stall_info s ON r.stall_id = s.id
SET r.stall_market_name = (SELECT m.market_name FROM market_info m WHERE m.id = s.market_id)
WHERE r.stall_market_name IS NULL AND s.market_id IS NOT NULL;

-- 清理存储过程
DROP PROCEDURE IF EXISTS sp_add_column_if_not_exists;
DROP PROCEDURE IF EXISTS sp_add_index_if_not_exists;


-- ============================================================
-- 来源: upgrade_v2.4_oa_module.sql
-- ============================================================
-- ============================================================
-- 升级脚本 v2.4：OA办公模块
-- 执行方式：mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.4_oa_module.sql
-- ============================================================
SET NAMES utf8mb4;

-- 1. 请假申请表

-- 2. 会议室表

-- 3. 会议室预约表

-- 4. 公告表

-- 5. 公告已读记录表

-- 6. 打卡记录表

-- 7. 工作汇报表

-- ============================================================
-- 来源: upgrade_v2.6_hr_module.sql
-- ============================================================
-- =====================================================
-- HR模块建表脚本 v2.6
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.6_hr_module.sql
-- =====================================================

-- 3.1 员工主档案

-- 3.2 岗位表

-- 3.3 员工附件表

-- 3.4 入职申请表

-- 3.5 转正申请表

-- 3.6 调岗申请表

-- 3.7 离职申请表

-- 3.8 HR考勤记录（按月聚合快照，聚合自 oa_clock_record）

-- 3.9 HR请假记录（同步自 oa_leave_apply）

-- 3.10 薪资档案

-- 3.11 月度薪资核算单

-- 3.12 社保公积金台账

-- ============================================================
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

