-- =====================================================
-- 升级脚本 v2.9：薪酬社保核算体系升级（废除静态快照，废弃手工台账）
-- 执行顺序：在 v2.7 / v2.8 之后，v2.10 之前执行
-- 关联规范：薪酬社保核算体系升级方案-v2.0.md
-- =====================================================

-- ===================== Step 1：hr_salary_archive 字段清理与注释 =====================
-- 1.1 移除静态快照字段（社保/公积金改为运行时动态计算）
ALTER TABLE hr_salary_archive
  DROP COLUMN IF EXISTS social_security_personal,
  DROP COLUMN IF EXISTS housing_fund_personal;

-- 1.2 补充字段注释
ALTER TABLE hr_salary_archive
  MODIFY COLUMN id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN company_id         BIGINT       NOT NULL DEFAULT 0     COMMENT '所属公司ID，0=集团总部',
  MODIFY COLUMN version_no         INT          NOT NULL DEFAULT 1     COMMENT '版本号，同一员工可有多条历史版本',
  MODIFY COLUMN source_type        TINYINT      NOT NULL DEFAULT 1     COMMENT '来源类型 1模板生成 2人工录入 3批量调薪 4晋升调级',
  MODIFY COLUMN source_id          BIGINT       NULL     DEFAULT NULL  COMMENT '来源记录ID（关联批量调薪/晋升等主表ID）',
  MODIFY COLUMN grade_code         VARCHAR(32)  NULL     DEFAULT NULL  COMMENT '薪酬级别编码（快照）',
  MODIFY COLUMN grade_name         VARCHAR(64)  NULL     DEFAULT NULL  COMMENT '薪酬级别名称快照',
  MODIFY COLUMN rule_id            BIGINT       NULL     DEFAULT NULL  COMMENT '薪资规则ID',
  MODIFY COLUMN rule_name          VARCHAR(128) NULL     DEFAULT NULL  COMMENT '薪资规则名称快照',
  MODIFY COLUMN effective_date     DATE         NOT NULL                COMMENT '生效日期',
  MODIFY COLUMN is_current         TINYINT      NOT NULL DEFAULT 1     COMMENT '是否当前生效版本 1是 0否',
  MODIFY COLUMN employee_id        BIGINT       NOT NULL                COMMENT '员工ID',
  MODIFY COLUMN employee_name      VARCHAR(64)  NOT NULL                COMMENT '员工姓名快照',
  MODIFY COLUMN basic_salary       DECIMAL(12,2) NOT NULL DEFAULT 0.00  COMMENT '基本工资',
  MODIFY COLUMN performance_salary DECIMAL(12,2) NOT NULL DEFAULT 0.00  COMMENT '绩效工资',
  MODIFY COLUMN position_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00  COMMENT '岗位津贴',
  MODIFY COLUMN other_allowance    DECIMAL(12,2) NOT NULL DEFAULT 0.00  COMMENT '其他补贴',
  MODIFY COLUMN remark             VARCHAR(500) NULL     DEFAULT NULL  COMMENT '备注',
  MODIFY COLUMN create_by          BIGINT       NOT NULL DEFAULT 0     COMMENT '创建人',
  MODIFY COLUMN create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN update_by          BIGINT       NULL     DEFAULT NULL  COMMENT '更新人',
  MODIFY COLUMN update_time        DATETIME     NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN is_delete          TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除 0未删除 1已删除';

-- ===================== Step 2：hr_social_security 废弃标记 =====================
-- 2.1 新增 deprecated 列
ALTER TABLE hr_social_security
  ADD COLUMN deprecated TINYINT NOT NULL DEFAULT 0 COMMENT '是否已废弃 0否 1是' AFTER remark;

-- 2.2 存量记录全部标记为已废弃（新数据由核算服务自动写入 hr_social_calc_detail）
UPDATE hr_social_security SET deprecated = 1 WHERE is_delete = 0;

-- ===================== Step 3：hr_social_calc_detail 索引补充 =====================
-- 按公司+月份查询核算明细
ALTER TABLE hr_social_calc_detail
  ADD INDEX IF NOT EXISTS idx_company_month (company_id, salary_month, is_delete);

-- ===================== Step 4：数据迁移提示（仅供参考，不执行）=====================
-- 如需将历史手工台账中的社保/公积金基数迁移到员工表：
-- UPDATE hr_employee e
-- JOIN hr_social_security s ON e.id = s.employee_id AND s.is_delete = 0 AND s.deprecated = 0
-- SET e.social_declare_base = s.social_security_base,
--     e.housing_fund_declare_base = s.housing_fund_base
-- WHERE e.social_declare_base IS NULL OR e.housing_fund_declare_base IS NULL;

SELECT 'upgrade_v2.9 completed' AS result;
