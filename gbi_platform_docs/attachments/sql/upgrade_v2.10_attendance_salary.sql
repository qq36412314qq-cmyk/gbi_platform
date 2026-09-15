-- =====================================================
-- 升级脚本 v2.10：考勤数据接入月度薪资核算
-- 执行顺序：第1~4步，依次执行
-- 关联规范：薪酬核算考勤联动规范.md
-- =====================================================

-- ===================== Step 1：hr_attendance_record 扩展 =====================
ALTER TABLE hr_attendance_record
  ADD COLUMN leave_type TINYINT NOT NULL DEFAULT 0 COMMENT '请假类型 0无薪事假 1有薪年假 2婚假 3产假 4病假 5工伤假 6公差 7调休假' AFTER leave_days,
  ADD COLUMN leave_days_detail JSON NULL COMMENT '请假明细JSON（格式：{"totalLeaveDays":N,"items":[{type,typeName,days,startDate,endDate,remark}]}）' AFTER leave_type;

-- 存量数据补默认值（HR需后续人工修正）
UPDATE hr_attendance_record SET leave_type = 0 WHERE leave_type IS NULL AND is_delete = 0;

-- ===================== Step 2：hr_salary_rule 扩展 =====================
ALTER TABLE hr_salary_rule
  ADD COLUMN skip_attendance TINYINT NOT NULL DEFAULT 0 COMMENT '是否跳过考勤核算 0参与 1跳过' AFTER housing_fund_rate,
  ADD COLUMN late_penalty_rate DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '迟到扣款费率倍数（默认1.0=全额扣）' AFTER skip_attendance,
  ADD COLUMN early_penalty_rate DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '早退扣款费率倍数（默认1.0=全额扣）' AFTER late_penalty_rate;

-- ===================== Step 3：hr_salary_month 扩展 =====================
ALTER TABLE hr_salary_month
  ADD COLUMN attendance_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '考勤扣款合计（旷工+迟到+早退+事假）' AFTER deduction_amount,
  ADD COLUMN absent_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '旷工扣款' AFTER attendance_deduction,
  ADD COLUMN late_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '迟到扣款' AFTER absent_deduction,
  ADD COLUMN early_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '早退扣款' AFTER late_deduction,
  ADD COLUMN unpaid_leave_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '无薪事假扣款' AFTER early_deduction,
  ADD COLUMN min_wage_protected TINYINT NOT NULL DEFAULT 0 COMMENT '是否触发最低工资保护 0否 1是' AFTER unpaid_leave_deduction,
  ADD COLUMN skip_attendance TINYINT NOT NULL DEFAULT 0 COMMENT '是否跳过考勤（快照） 0否 1是' AFTER min_wage_protected;

-- ===================== Step 4：sys_city 扩展 =====================
ALTER TABLE sys_city
  ADD COLUMN min_wage DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当月最低工资标准（元），0表示未配置不执行保护' AFTER city_name;
