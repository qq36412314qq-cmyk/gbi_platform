# AI_EDIT_LOG

## 【2026-09-06 12:30:00】统一优惠策略升级 - 代码变更执行

### 一、变更概述
将现有仅支持租赁合同的优惠系统扩展为统一优惠策略，支持物业费、水电费、幼儿园费等多业务类型，新增定额优惠和阶梯优惠类型。

### 二、涉及文件清单

#### 2.1 实体类（Entity）
- BizDiscountPolicy.java - 扩展新增字段
- BizDiscountApply.java - 扩展新增字段
- BizFeeBill.java - 新增 policy_id/apply_id/discount_type
- BizDiscountLog.java - 新建
- BizDiscountThreshold.java - 新建

#### 2.2 Mapper 接口
- BizDiscountLogMapper.java - 新建
- BizDiscountThresholdMapper.java - 新建

#### 2.3 DTO 类
- DiscountPolicyDTO.java - 新建
- DiscountApplyDTO.java - 新建
- DiscountCalcDTO.java - 新建
- DiscountThresholdDTO.java - 新建
- DiscountThresholdQueryDTO.java - 新建
- DiscountPolicyQueryDTO.java - 扩展
- DiscountApplyQueryDTO.java - 扩展

#### 2.4 VO 类
- DiscountPolicyVO.java - 扩展
- DiscountApplyVO.java - 扩展
- DiscountThresholdVO.java - 新建
- DiscountCalcVO.java - 新建

#### 2.5 计算引擎（engine/discount）
- DiscountCalculator.java - 新建接口
- WaiveMonthsCalculator.java - 新建
- DiscountRateCalculator.java - 新建
- DeductAmountCalculator.java - 新建
- FixedAmountCalculator.java - 新建
- TierCalculator.java - 新建
- ComboCalculator.java - 新建
- DiscountCalculatorFactory.java - 新建

#### 2.6 Service 层
- DiscountPolicyService.java - 不变
- DiscountApplyService.java - 新增方法
- DiscountThresholdService.java - 新建
- DiscountCalcService.java - 新建
- DiscountPolicyServiceImpl.java - 扩展
- DiscountApplyServiceImpl.java - 重写扩展
- DiscountThresholdServiceImpl.java - 新建
- DiscountCalcServiceImpl.java - 新建

#### 2.7 Controller 层
- DiscountController.java - 扩展

#### 2.8 常量类
- CommonConst.java - 新增常量
- PermissionConst.java - 新增权限常量

#### 2.9 SQL 脚本
- upgrade_v1.0_discount.sql - 新建

### 三、变更摘要

1. 实体类扩展：新增 biz_type、fixed_amount、tier_config、scope_ids 等字段
2. 新建表：finance_discount_log（执行日志）、finance_discount_threshold（阈值配置）
3. 计算引擎：采用策略模式，新增定额/阶梯计算器
4. Service 扩展：新增多业务类型申请、阈值判断、计算预览
5. Controller 扩展：新增阈值配置、申请提交、计算预览接口
6. 常量扩展：新增优惠类型、业务类型、阈值类型常量

### 四、向后兼容

- 现有合同优惠接口 createForContract() 保持不变
- 历史数据迁移：biz_type 默认 'rent'

### 五、部署注意事项

1. 先执行 SQL 升级脚本，再部署新代码
2. 需在 sys_menu 表新增菜单权限：discount:threshold:*
3. 测试重点：合同优惠流程不变、物业费定额优惠、水电费阶梯优惠

### 六、风险与注意事项

- 无特殊风险：现有合同优惠逻辑完全兼容
- 上线校验点：SQL 脚本执行后验证数据完整性
## 【2026-09-06 13:00:00】SQL 升级执行 + 权限配置
- 数据库：group_rent_db @ 127.0.0.1:3306 (root/21145211)
- 执行内容：
  1. finance_fee_pay_bill 新增 policy_id、apply_id、discount_type 字段
  2. 创建 finance_discount_log 表（优惠执行日志）
  3. 创建 finance_discount_threshold 表（审批阈值配置）
  4. 插入 10 条集团默认阈值配置
  5. 创建索引 idx_fee_bill_policy、idx_fee_bill_apply
  6. 配置 sys_menu 权限（ID 131-134）
- 验证结果：所有表结构正确，数据完整