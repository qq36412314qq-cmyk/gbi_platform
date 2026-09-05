-- ============================================================
-- 数据库表名重命名升级脚本 v1.0
-- 日期：2026-09-04
-- 数据库：group_rent_db
-- 说明：物业模块 → property_ 前缀，财务模块 → finance_ 前缀
-- 执行前请先备份数据库！
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一部分：物业模块表重命名（property_ 前缀）
-- ============================================================

-- 1. market_info → property_market
ALTER TABLE market_info RENAME TO property_market;
-- 更新表注释
ALTER TABLE property_market COMMENT = '市场信息表';

-- 2. property_bill → property_fee_bill（物业费月度账单）
ALTER TABLE property_bill RENAME TO property_fee_bill;
-- 重建唯一键索引（rename 后原索引名保留但需确认）
ALTER TABLE property_fee_bill DROP INDEX uk_stall_month;
ALTER TABLE property_fee_bill ADD UNIQUE KEY uk_stall_month (company_id, stall_id, bill_month);
ALTER TABLE property_fee_bill COMMENT = '物业费月度账单表';

-- 3. stall_category → property_stall_category
ALTER TABLE stall_category RENAME TO property_stall_category;
ALTER TABLE property_stall_category COMMENT = '摊位分类表';

-- 4. stall_contract → property_stall_contract
ALTER TABLE stall_contract RENAME TO property_stall_contract;
ALTER TABLE property_stall_contract COMMENT = '摊位合同表';

-- 5. stall_info → property_stall_info
ALTER TABLE stall_info RENAME TO property_stall_info;
ALTER TABLE property_stall_info COMMENT = '摊位信息表';

-- 6. stall_tenant → property_stall_tenant
ALTER TABLE stall_tenant RENAME TO property_stall_tenant;
ALTER TABLE property_stall_tenant COMMENT = '租户信息表';

-- 7. water_elec_bill → property_water_elec_bill
ALTER TABLE water_elec_bill RENAME TO property_water_elec_bill;
ALTER TABLE property_water_elec_bill COMMENT = '水电物业月度账单表（按类别拆行）';

-- 8. water_elec_meter → property_water_elec_meter
ALTER TABLE water_elec_meter RENAME TO property_water_elec_meter;
ALTER TABLE property_water_elec_meter COMMENT = '水电表计设备表';

-- 9. water_elec_pay_record → property_water_elec_pay_record
ALTER TABLE water_elec_pay_record RENAME TO property_water_elec_pay_record;
ALTER TABLE property_water_elec_pay_record COMMENT = '水电缴费记录表';

-- ============================================================
-- 第二部分：财务模块表重命名（finance_ 前缀）
-- ============================================================

-- 1. biz_fee_bill → finance_fee_pay_bill（统一账单表，聚合各业务类型来源）
ALTER TABLE biz_fee_bill RENAME TO finance_fee_pay_bill;
ALTER TABLE finance_fee_pay_bill COMMENT = '统一账单表（聚合支付载体，含各业务来源账单）';

-- 2. biz_fee_item → finance_fee_item
ALTER TABLE biz_fee_item RENAME TO finance_fee_item;
ALTER TABLE finance_fee_item COMMENT = '收费项定义表';

-- 3. biz_fee_rule → finance_fee_rule
ALTER TABLE biz_fee_rule RENAME TO finance_fee_rule;
ALTER TABLE finance_fee_rule COMMENT = '收费规则表';

-- 4. biz_fee_rule_stall_rel → finance_fee_rule_stall_rel
ALTER TABLE biz_fee_rule_stall_rel RENAME TO finance_fee_rule_stall_rel;
ALTER TABLE finance_fee_rule_stall_rel COMMENT = '收费规则-摊位关联表';

-- 5. biz_finance_flow → finance_pay_flow（财务流水）
ALTER TABLE biz_finance_flow RENAME TO finance_pay_flow;
ALTER TABLE finance_pay_flow COMMENT = '全域财务资金流水表（唯一资金台账）';

-- 6. biz_finance_writeoff → finance_writeoff（财务核销）
ALTER TABLE biz_finance_writeoff RENAME TO finance_writeoff;
ALTER TABLE finance_writeoff COMMENT = '财务核销记录表';

-- 7. biz_pay_bill → finance_pay_order（缴费单主表，聚合支付载体）
-- 注意：与 finance_fee_pay_bill（统一账单）区分，前者是缴费单，后者是账单
ALTER TABLE biz_pay_bill RENAME TO finance_pay_order;
ALTER TABLE finance_pay_order COMMENT = '缴费单主表（聚合支付载体）';

-- 8. biz_pay_bill_item → finance_pay_order_item（缴费单明细）
ALTER TABLE biz_pay_bill_item RENAME TO finance_pay_order_item;
ALTER TABLE finance_pay_order_item COMMENT = '缴费单明细表（快照固化）';

-- 9. biz_recv_pay_plan → finance_recv_pay_plan（应收应付计划）
ALTER TABLE biz_recv_pay_plan RENAME TO finance_recv_pay_plan;
ALTER TABLE finance_recv_pay_plan COMMENT = '应收应付计划主表（全系统唯一台账）';

-- 10. bill_plan_rel → finance_pay_plan_rel（账单-计划关联）
ALTER TABLE bill_plan_rel RENAME TO finance_pay_plan_rel;
ALTER TABLE finance_pay_plan_rel COMMENT = '账单-应收应付计划关联表（多计划合并账单）';

-- 11. biz_discount_apply → finance_discount_apply
ALTER TABLE biz_discount_apply RENAME TO finance_discount_apply;
ALTER TABLE finance_discount_apply COMMENT = '优惠申请表';

-- 12. biz_discount_policy → finance_discount_policy
ALTER TABLE biz_discount_policy RENAME TO finance_discount_policy;
ALTER TABLE finance_discount_policy COMMENT = '优惠策略表';

-- ============================================================
-- 第三部分：验证
-- ============================================================
SELECT '物业模块表重命名验证：' AS step;
SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'property_%';

SELECT '财务模块表重命名验证：' AS step;
SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'finance_%';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 回滚 SQL（如需回滚，执行以下语句）
-- ============================================================
--
-- -- 物业模块回滚
-- ALTER TABLE property_market RENAME TO market_info;
-- ALTER TABLE property_fee_bill RENAME TO property_bill;
-- ALTER TABLE property_stall_category RENAME TO stall_category;
-- ALTER TABLE property_stall_contract RENAME TO stall_contract;
-- ALTER TABLE property_stall_info RENAME TO stall_info;
-- ALTER TABLE property_stall_tenant RENAME TO stall_tenant;
-- ALTER TABLE property_water_elec_bill RENAME TO water_elec_bill;
-- ALTER TABLE property_water_elec_meter RENAME TO water_elec_meter;
-- ALTER TABLE property_water_elec_pay_record RENAME TO water_elec_pay_record;
--
-- -- 财务模块回滚
-- ALTER TABLE finance_fee_item RENAME TO biz_fee_item;
-- ALTER TABLE finance_fee_rule RENAME TO biz_fee_rule;
-- ALTER TABLE finance_fee_rule_stall_rel RENAME TO biz_fee_rule_stall_rel;
-- ALTER TABLE finance_pay_flow RENAME TO biz_finance_flow;
-- ALTER TABLE finance_writeoff RENAME TO biz_finance_writeoff;
-- ALTER TABLE finance_pay_order RENAME TO biz_pay_bill;
-- ALTER TABLE finance_pay_order_item RENAME TO biz_pay_bill_item;
-- ALTER TABLE finance_recv_pay_plan RENAME TO biz_recv_pay_plan;
-- ALTER TABLE finance_pay_plan_rel RENAME TO bill_plan_rel;
-- ALTER TABLE finance_discount_apply RENAME TO biz_discount_apply;
-- ALTER TABLE finance_discount_policy RENAME TO biz_discount_policy;
-- -- finance_fee_pay_bill 回滚需特殊处理（原表名 biz_fee_bill）
-- ALTER TABLE finance_fee_pay_bill RENAME TO biz_fee_bill;
--
