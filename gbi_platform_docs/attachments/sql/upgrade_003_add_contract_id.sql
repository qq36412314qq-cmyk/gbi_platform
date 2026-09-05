-- ============================================================
-- 升级脚本 v1.0
-- 日期：2026-09-04
-- 说明：为水电费和物业费账单表增加 contract_id 字段，关联租赁合同
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. property_water_elec_bill 增加 contract_id
ALTER TABLE property_water_elec_bill
    ADD COLUMN contract_id bigint(20) DEFAULT NULL COMMENT '关联合同ID（property_stall_contract.id）' AFTER merchant_id;

-- 2. property_fee_bill 增加 contract_id
ALTER TABLE property_fee_bill
    ADD COLUMN contract_id bigint(20) DEFAULT NULL COMMENT '关联合同ID（property_stall_contract.id）' AFTER merchant_id;

-- ============================================================
-- 回滚 SQL
-- ============================================================
-- ALTER TABLE property_water_elec_bill DROP COLUMN contract_id;
-- ALTER TABLE property_fee_bill DROP COLUMN contract_id;

SET FOREIGN_KEY_CHECKS = 1;
