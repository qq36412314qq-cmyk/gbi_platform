# AI 编辑日志

## 2026-09-06 多租户 company_id 架构分析与工作流修正

### 问题背景
之前的工作流文档中描述"新增市场 → 设置 company_id"创建子公司的步骤是错误的。市场是物业模块的下级分类，不是子公司创建入口。子公司的归属应在组织架构（sys_org）中建立，市场挂在子公司之下。

### 根因分析
1. 当前 sys_org 数据：飞宇汽车城（id=6, org_type=2）和幼儿园（id=10, org_type=2）的 company_id 均为 0，与集团混用，数据隔离完全失效
2. 部门节点（ids 7-9, 11-12）的 company_id 继承的是父节点 id（6或10），而非独立的 company_id 值
3. CommonConst 中不存在 ORG_TYPE_COMPANY / ORG_TYPE_DEPT 常量（org_type 值 1/2/3 为硬编码整数）
4. OrgServiceImpl 中 getMaxCompanyId() / getParentCompanyId() 方法尚未实现

### 影响范围
- 所有子公司业务数据（市场、铺位、商户、收费规则、账单等）因 company_id=0 与集团数据混同
- 多租户拦截器（TenantLineInnerInterceptor）自动追加 WHERE company_id=0，子公司数据无隔离效果

---

## 【2026-09-06 15:40:00】重新编写公司创建与数据隔离工作流
- 用户原始需求：修正此前关于"创建子公司"的工作流描述，明确正确的数据归属模型
- 涉及修改文件：无代码变更，仅输出工作流文档
- 变更摘要：
  1. 子公司通过组织架构（sys_org）创建，org_type=2，自动分配独立 company_id
  2. 部门通过组织架构创建，org_type=3，company_id 继承最近上级子公司节点
  3. 市场（property_market）挂在公司/子公司之下，使用子公司的 company_id
  4. OrgServiceImpl.add() 需新增 getMaxCompanyId() 和 getParentCompanyId() 两个私有方法
  5. 现有脏数据（id=6,10 的子公司 company_id=0）需通过 SQL 脚本修复
- 风险与注意事项：
  - CommonConst.ORG_TYPE_COMPANY 常量不存在，代码中需用硬编码整数 2 替代
  - getMaxCompanyId() 需查询 sys_org WHERE company_id > 0 的最大值
  - getParentCompanyId() 需向上递归查找 parent_id 链中最近的 org_type=2 节点
