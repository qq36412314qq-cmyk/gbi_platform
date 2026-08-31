## 【2026-08-28 10:30:00】账单页增加勾选+批量生成并跳转未支付订单
- 用户原始需求：账单页"生成账单"直接生成到未支付订单页；增加勾选列和批量生成按钮
- 涉及修改文件：
  - gbi_platform_admin/src/views/business/property/feeBill.vue
  - gbi_platform_admin/src/views/business/waterElec/waterElecBill.vue
- 变更摘要：
  1. feeBill.vue：新增 el-table-column type="selection" 勾选列；新增 handleBatchGenerate 批量生成接口；单条"生成账单"保留弹窗但增加"生成并查看"按钮，完成后跳转 /property/unpaidBill
  2. waterElecBill.vue：新增勾选列；新增 handleBatchGenerate（复用抄表弹窗，预设第一条记录月份）；goPay 单条生成完成后跳转 /property/unpaidBill
- 风险与注意事项：无特殊风险
