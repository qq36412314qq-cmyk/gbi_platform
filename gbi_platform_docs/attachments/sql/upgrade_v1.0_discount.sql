-- ============================================================
-- 集团多业态一体化管控系统 统一优惠策略升级脚本 V1.1
-- 模块：优惠策略多业务类型扩展 + 审批阈值配置 + 执行日志
-- 内容：finance_discount_policy / finance_discount_apply / finance_fee_pay_bill 表扩展
--       + finance_discount_log / finance_discount_threshold 新建表
--       + 数据迁移 + 初始阈值配置
-- 执行方式: cmd /c "mysql.exe -uroot -p密码 --default-character-set=utf8mb4 group_rent_db < upgrade_v1.0_discount.sql"
-- 时间：2026-09-06
-- ============================================================

-- ============================================================
-- 一、表结构扩展
-- ============================================================

-- 1.1 扩展 finance_discount_policy 表
ALTER TABLE finance_discount_policy
  ADD COLUMN biz_type VARCHAR(32) NOT NULL DEFAULT 'rent' COMMENT '业务类型 rent=租赁费 property_fee=物业费 water_elec=水电费 kindergarten=幼儿园费' AFTER policy_name,
  ADD COLUMN scope_ids VARCHAR(500) DEFAULT NULL COMMENT '适用范围ID列表JSON ["1","2","3"]' AFTER scope_type,
  ADD COLUMN fixed_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '定额优惠金额（每月固定减免，type=5）' AFTER deduct_amount,
  ADD COLUMN tier_config MEDIUMTEXT DEFAULT NULL COMMENT '阶梯配置JSON [{"min_amount":0,"discount_rate":100}]' AFTER fixed_amount,
  ADD COLUMN max_apply_months INT DEFAULT NULL COMMENT '最多申请月数限制，NULL表示不限' AFTER end_time,
  ADD COLUMN auto_approve TINYINT(4) NOT NULL DEFAULT 1 COMMENT '自动审批 0需审批 1自动生效' AFTER max_apply_months,
  ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重' AFTER status;

-- 修改 scope_type 字段注释（扩展适用范围枚举）
ALTER TABLE finance_discount_policy
  MODIFY COLUMN scope_type TINYINT(4) NOT NULL DEFAULT 1 COMMENT '适用范围 1按合同 2按铺位 3按租户 4按市场 5按分类';

-- 新增索引（使用唯一名称避免冲突）
CREATE INDEX idx_discount_policy_biz_status ON finance_discount_policy(company_id, biz_type, status);
CREATE INDEX idx_discount_policy_scope ON finance_discount_policy(biz_type, scope_type);

-- 1.2 扩展 finance_discount_apply 表
ALTER TABLE finance_discount_apply
  ADD COLUMN biz_type VARCHAR(32) NOT NULL DEFAULT 'rent' COMMENT '业务类型快照 rent/property_fee/water_elec/kindergarten' AFTER source_type,
  ADD COLUMN source_no VARCHAR(64) DEFAULT NULL COMMENT '来源单据编号（冗余便于列表展示）' AFTER source_id,
  ADD COLUMN fixed_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '定额优惠金额快照' AFTER deduct_amount,
  ADD COLUMN tier_config MEDIUMTEXT DEFAULT NULL COMMENT '阶梯配置快照JSON' AFTER fixed_amount,
  ADD COLUMN start_month VARCHAR(32) DEFAULT NULL COMMENT '优惠起始月份 yyyy-MM' AFTER tenant_id,
  ADD COLUMN end_month VARCHAR(32) DEFAULT NULL COMMENT '优惠结束月份 yyyy-MM' AFTER start_month,
  ADD COLUMN total_months INT NOT NULL DEFAULT 0 COMMENT '优惠总月数' AFTER end_month,
  ADD COLUMN original_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠基数金额快照' AFTER total_months,
  ADD COLUMN real_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际应收金额快照' AFTER original_amount,
  ADD COLUMN audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审批意见' AFTER audit_time;

-- 修改 source_type 字段注释（扩展来源类型）
ALTER TABLE finance_discount_apply
  MODIFY COLUMN source_type VARCHAR(32) DEFAULT NULL COMMENT '来源类型 contract/property_bill/water_elec_bill/other';

-- 新增索引（使用唯一名称避免冲突）
CREATE INDEX idx_discount_apply_biz_month ON finance_discount_apply(company_id, biz_type, start_month, end_month);
CREATE INDEX idx_discount_apply_source ON finance_discount_apply(source_type, source_id);

-- 1.3 扩展 finance_fee_pay_bill 表
ALTER TABLE finance_fee_pay_bill
  ADD COLUMN policy_id BIGINT DEFAULT NULL COMMENT '关联优惠策略ID' AFTER source_bill_id,
  ADD COLUMN apply_id BIGINT DEFAULT NULL COMMENT '关联优惠申请ID' AFTER policy_id,
  ADD COLUMN discount_type TINYINT DEFAULT NULL COMMENT '优惠类型快照' AFTER apply_id;

-- 新增索引（使用唯一名称避免冲突）
CREATE INDEX idx_fee_bill_policy ON finance_fee_pay_bill(policy_id);
CREATE INDEX idx_fee_bill_apply ON finance_fee_pay_bill(apply_id);

-- ============================================================
-- 二、新建表
-- ============================================================

-- 2.1 新建 finance_discount_log 表（优惠执行日志）
CREATE TABLE IF NOT EXISTS `finance_discount_log` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `apply_id` BIGINT DEFAULT NULL COMMENT '关联优惠申请ID',
  `policy_id` BIGINT DEFAULT NULL COMMENT '关联优惠策略ID',
  `bill_id` BIGINT DEFAULT NULL COMMENT '关联账单ID',
  `source_bill_id` BIGINT DEFAULT NULL COMMENT '源账单ID（property_fee_bill.id 等）',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
  `stall_id` BIGINT DEFAULT NULL COMMENT '铺位ID',
  `bill_month` VARCHAR(32) NOT NULL COMMENT '账单月份',
  `discount_type` TINYINT NOT NULL COMMENT '优惠类型',
  `original_amount` DECIMAL(12,2) NOT NULL COMMENT '原价金额',
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `real_amount` DECIMAL(12,2) NOT NULL COMMENT '实收金额',
  `calc_rule` VARCHAR(500) DEFAULT NULL COMMENT '计算规则说明JSON',
  `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '操作人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_log_apply` (`apply_id`),
  INDEX `idx_log_bill` (`bill_id`),
  INDEX `idx_log_source` (`source_bill_id`),
  INDEX `idx_log_stall_month` (`stall_id`, `bill_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠执行日志表';

-- 2.2 新建 finance_discount_threshold 表（审批阈值配置）
CREATE TABLE IF NOT EXISTS `finance_discount_threshold` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '所属子公司ID，0=集团统一',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型 rent/property_fee/water_elec/kindergarten',
  `threshold_type` TINYINT NOT NULL COMMENT '阈值类型 1免租期上限 2折扣率下限 3减免金额上限 4定额上限 5占比上限',
  `threshold_value` DECIMAL(12,2) NOT NULL COMMENT '阈值数值',
  `require_audit` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需审批 0否 1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_threshold` (`company_id`, `biz_type`, `threshold_type`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠审批阈值配置表';

-- ============================================================
-- 三、数据迁移
-- ============================================================

-- 3.1 迁移现有租赁优惠策略（biz_type 默认 rent）
UPDATE finance_discount_policy SET biz_type = 'rent' WHERE biz_type IS NULL OR biz_type = '';

-- 3.2 迁移现有租赁优惠申请（biz_type 默认 rent）
UPDATE finance_discount_apply SET biz_type = 'rent' WHERE biz_type IS NULL OR biz_type = '';

-- ============================================================
-- 四、初始数据（集团默认阈值配置）
-- ============================================================

INSERT INTO finance_discount_threshold (company_id, biz_type, threshold_type, threshold_value, require_audit, status, remark) VALUES
(0, 'rent', 1, 3.00, 1, 1, '租赁合同免租期上限3个月'),
(0, 'rent', 2, 80.00, 1, 1, '租赁合同折扣率下限80%'),
(0, 'rent', 3, 5000.00, 1, 1, '租赁合同减免金额上限5000元'),
(0, 'rent', 5, 30.00, 1, 1, '租赁合同优惠占比上限30%'),
(0, 'property_fee', 3, 2000.00, 1, 1, '物业费减免金额上限2000元'),
(0, 'property_fee', 5, 20.00, 1, 1, '物业费优惠占比上限20%'),
(0, 'water_elec', 3, 1000.00, 1, 1, '水电费减免金额上限1000元'),
(0, 'water_elec', 5, 15.00, 1, 1, '水电费优惠占比上限15%'),
(0, 'kindergarten', 3, 3000.00, 1, 1, '幼儿园费减免金额上限3000元'),
(0, 'kindergarten', 5, 25.00, 1, 1, '幼儿园费优惠占比上限25%');

-- ============================================================
-- 五、验证脚本
-- ============================================================

-- 验证策略表迁移
SELECT biz_type, COUNT(*) as cnt FROM finance_discount_policy GROUP BY biz_type;

-- 验证申请表迁移
SELECT biz_type, COUNT(*) as cnt FROM finance_discount_apply GROUP BY biz_type;

-- 验证阈值配置
SELECT id, company_id, biz_type, threshold_type, threshold_value, status FROM finance_discount_threshold ORDER BY biz_type, threshold_type;