## 【2026-09-03 17:20:00】财务模块升级 - 缴费单聚合支付功能实施
- 用户原始需求：整合三个方案文档，实施缴费单聚合支付功能
- 涉及修改/新增文件：
  - D:\Office\Project\Java\gbi_platform\gbi_platform_docs\财务模块升级方案-v3.0-最终版.md（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/BizPayBill.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/BizPayBillItem.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/mapper/BizPayBillMapper.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/mapper/BizPayBillItemMapper.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/PayBillService.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/PayBillServiceImpl.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/controller/property/PayBillController.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/PayBillCreateDTO.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/PayBillPayDTO.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/PayBillRefundDTO.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/vo/PayBillVO.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/vo/PayBillItemVO.java（新建）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/BizFeeBill.java（修改：+payBillId）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/BizFinanceFlow.java（修改：+payBillId,+payBillNo）
  - gbi_platform_server/src/main/java/com/gbi/platform/vo/FinanceFlowVO.java（修改：+payBillId,+payBillNo）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/UnifiedPayDTO.java（修改：+payBillId）
  - gbi_platform_server/src/main/java/com/gbi/platform/common/constant/CommonConst.java（修改：+缴费状态常量）
  - gbi_platform_server/src/main/java/com/gbi/platform/common/constant/PermissionConst.java（修改：+缴费单权限）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/FinanceServiceImpl.java（修改：+payBillNo映射）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/UnifiedPayServiceImpl.java（修改：+payByPayBill路由）
- 变更摘要：
  1. 数据库：新建 biz_pay_bill 和 biz_pay_bill_item 表，修改 biz_fee_bill 和 biz_finance_flow 增加 pay_bill_id 字段，修正 flow_status 异常值
  2. 后端：新建缴费单完整 CRUD + 聚合缴费/退费/作废功能，UnifiedPayDTO 兼容新旧参数
  3. 前端：暂不修改（待后续实施）
  4. 编译：解决 Lombok+Java21 注解处理器冲突，使用 m2_repo_tmp 独立仓库绕过锁定
- 风险与注意事项：
  - Lombok 版本从 1.18.38 降至 1.18.36（Java 21 兼容性）
  - 历史流水 pay_bill_id 为 NULL，过渡期兼容
  - 前端 payBill.vue 尚未创建，需后续开发
  - 无特殊风险