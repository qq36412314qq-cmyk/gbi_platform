# MySQL8\.0数据库设计规范

# 数据库设计规范

## 文档描述

本规范基于项目 MySQL8\.0 数据库、单库多租户`company_id`逻辑隔离架构制定，统一数据表、字段、索引、主键、外键、SQL、多租户、摊位地图专项、财务数据表、数据生命周期、安全、版本迁移全套标准；对齐《集团多业态整体方案》《后端编码规范》《权限多租户规范》《安全开发规范》，单人开发统一建表、改表、SQL 编写标准，杜绝字段混乱、数据越权、资金脏数据、地图 JSON 存储漏洞，适配租赁、OA、财务、营销全业务模块。

# 一、数据库整体架构规范

## 1\.1 架构选型标准

1. 数据库版本固定：MySQL 8\.0，禁止 5\.x 及更低版本；

2. 存储引擎：统一 InnoDB，不使用 MyISAM；

3. 字符集：全局 utf8mb4，支持 emoji、特殊摊位备注、画布 JSON 特殊字符；

4. 排序规则：utf8mb4\_unicode\_ci；

5. 架构模式：单库多租户，一套库承载集团全业态，依靠`company_id`逻辑隔离，不分库、不分实例；

6. 分阶段扩展：新增幼儿园 / 建筑业态仅新增业务表，不改动底层公共表、隔离逻辑。

## 1\.2 全局通用基础字段（所有业务表强制携带）

所有业务实体表（不含集团全局配置表）必须包含以下标准字段，统一命名、统一类型：

|字段名|字段类型|说明|
|---|---|---|
|id|bigint unsigned NOT NULL AUTO\_INCREMENT|自增主键，全局唯一|
|company\_id|bigint NOT NULL DEFAULT 0|多租户隔离标识，0 = 集团全局数据|
|create\_by|bigint NOT NULL DEFAULT 0|创建人 user\_id|
|create\_time|datetime NOT NULL DEFAULT CURRENT\_TIMESTAMP|创建时间|
|update\_by|bigint NULL DEFAULT NULL|更新人 user\_id|
|update\_time|datetime NULL ON UPDATE CURRENT\_TIMESTAMP|更新时间，自动刷新|
|is\_delete|tinyint NOT NULL DEFAULT 0|逻辑删除 0 = 正常 1 = 已删除，禁止物理删除|

### 1\.2\.1 表分类区分规则

1. **集团全局公共表（无 company\_id）**
物资分类、系统字典、角色模板、流程模板、全局参数、集团组织架构；
作用：全子公司只读，仅集团管理员维护，无租户隔离。

2. **子公司业务私有表（必带 company\_id）**
摊位、商户、合同、市场地图、水电账单、物资出入库、OA 公文、营销线索；
所有数据绑定所属分公司，MyBatis-Plus 拦截器自动过滤。

3. **全域财务流水表（biz\_finance\_flow）**
带 company\_id，一套表承载全集团收支，双视图隔离（集团汇总 / 子公司对账），**禁止物理删除**。

4. **地图专属数据表（market\_map /map\_stall\_point）**
双隔离维度：company\_id \+ market\_id，画布点位 JSON 专用字段规范。

# 二、命名统一强制规范

## 2\.1 数据库 / 表命名

1. 库名：小写下划线，统一`group_rent_db`；

2. 表名：全小写，下划线分隔，禁止驼峰、中文；
示例：stall\_info、market\_map、biz\_finance\_flow；

3. 模块表前缀区分：

    - 中台 sys\_：sys\_user、sys\_role、sys\_dict、sys\_audit\_log

    - 租赁 stall\_：stall\_info、stall\_contract

    - 地图 market\_：market\_map、map\_stall\_point

    - 水电 water\_elec\_bill

    - 物资 material\_

    - OA oa\_

    - 营销 marketing\_

    - 财务 biz\\*finance\\*

4. 中间关联表：主表 1\_主表 2\_rel，如 stall\_merchant\_rel。

## 2\.2 字段命名

1. 全小写下划线，语义完整，禁止简写模糊命名；
错误：num /msg；正确：stall\_number /remark\_content

2. 状态统一前缀：status\_xxx；

3. 主键外键统一后缀`_id`：stall\_id、market\_id、company\_id、user\_id；

4. 时间统一后缀`_time`：create\_time、expire\_time；

5. 金额统一后缀`_amount`：rent\_amount、discount\_amount。

## 2\.3 索引命名规范

1. 普通索引：idx\_字段名，如 idx\_company\_id；

2. 联合索引：idx\_字段 1\_字段 2，如 idx\_company\_mark\_id；

3. 唯一索引：uk\_字段名，如 uk\_market\_name\_company；

4. 主键索引：默认 PRIMARY，不自定义命名。

# 三、字段类型、长度、约束规范

## 3\.1 基础类型选用标准

1. 主键 ID：bigint unsigned，自增，不使用 int（数据量大溢出风险）；

2. 关联外键（company\_id/market\_id/stall\_id）：bigint NOT NULL；

3. 金额类：decimal \(12,2\)，统一保留两位小数，禁止 float/double 浮点误差；

4. 短文本名称：varchar \(64\) /varchar \(128\)（摊位名、商户名）；

5. 长备注、描述：varchar \(500\)；

6. OSS 文件地址：varchar \(1000\)；

7. 画布点位 JSON 专用字段：text /mediumtext（market\_map\.point\_json）；

8. 状态标识：tinyint \(1\) 0/1/2；

9. 布尔开关：tinyint DEFAULT 0；

10. 日期时间：datetime，不使用 date 单独存储完整业务时间；

11. 手机号、身份证：varchar \(32\)，加密存储不限制过短；

## 3\.2 字段约束强制规则

1. 业务主键 id：NOT NULL AUTO\_INCREMENT；

2. 租户 company\_id：NOT NULL DEFAULT 0；

3. 核心业务字段（摊位编号、合同编号、市场名称）：NOT NULL，禁止为空；

4. 可空字段仅允许备注、扩展描述类；

5. 所有状态字段必须设置默认值 0；

6. 逻辑删除 is\_delete 默认 0；

7. 时间字段 create\_time 默认 CURRENT\_TIMESTAMP，update\_time 自动更新；

## 3\.3 禁止使用类型黑名单

1. 禁止 float、double 存储金额（浮点精度丢失）；

2. 禁止 char 固定长字符串，统一 varchar；

3. 禁止 blob 存储文本、画布 JSON，统一 text/mediumtext；

4. 禁止 enum 状态，统一 tinyint 数字 \+ 后台常量管理（易扩展）；

5. 禁止存储明文密钥、完整身份证，需加密字段单独设计。

# 四、索引设计规范

## 4\.1 必建通用索引（所有带 company\_id 业务表）

1. 联合索引：idx\_company\_id\_is\_delete
覆盖绝大多数分页列表查询（按公司过滤 \+ 排除删除数据）；

2. 关联外键单独索引：stall\_id、market\_id、merchant\_id；

3. 唯一索引：同公司下不可重复的字段（市场名称、摊位编号）
uk\_market\_name\_company \(company\_id,market\_name,is\_delete\)

## 4\.2 摊位地图表索引专项

market\_map：

1. 联合索引 idx\_company\_mark \(company\_id,market\_id,is\_delete\)

2. 唯一索引 uk\_market\_name \(company\_id,market\_name,is\_delete\)
map\_stall\_point：

3. 联合索引 idx\_map\_stall \(map\_id,stall\_id,is\_delete\)

## 4\.3 财务流水索引（biz\_finance\_flow）

1. idx\_company\_time \(company\_id,create\_time,is\_delete\) 对账分页核心索引

2. idx\_bill\_type \(business\_type,bill\_id\) 单据关联查询索引

## 4\.4 索引开发红线

1. 禁止单表超过 5 个索引（写入性能下降）；

2. 禁止过长字符串建立索引（varchar\>256 不单独建索引）；

3. 禁止无意义单字段索引（仅 company\_id 不单独建，用联合）；

4. 禁止索引包含 is\_delete 以外大量不常用字段；

5. 新增索引必须评估插入、更新性能，大表分批执行。

# 五、多租户 company\_id 数据隔离设计（核心强制规范）

## 5\.1 写入规则

1. 所有新增业务数据，后端从 JWT 上下文自动赋值 company\_id；

2. 前端传入 company\_id 参数全部丢弃，不参与 SQL 条件；

3. 市场地图双隔离：company\_id \+ market\_id，跨公司 market\_id 不可查询修改；

4. 画布点位保存时，校验 stall 所属 company 与当前登录 company 一致，拦截跨公司绑定。

## 5\.2 查询过滤规则

1. MyBatis-Plus 全局拦截器自动拼接 `WHERE company_id = #{loginCompanyId} AND is_delete = 0`；

2. 集团超级管理员、财务自动关闭拦截，查询全量数据；

3. 特殊全域报表、跨物资调拨，代码注释标注手动关闭过滤，同时写入审计日志；

4. 禁止任何接口、SQL 硬编码固定 company\_id，适配多环境。

## 5\.3 数据权限防越权设计

1. SQL 禁止不带 company\_id 查询业务数据；

2. 任何修改、删除 SQL 必须同时携带 company\_id、is\_delete；

3. 地图、财务等高敏感表禁止提供无租户 ID 的全表查询接口。

# 六、摊位可视化地图专属数据表规范

## 6\.1 market\_map 市场地图主表（必建）

```sql
CREATE TABLE `market_map` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '地图主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `market_id` bigint NOT NULL COMMENT '关联市场ID',
  `map_name` varchar(128) NOT NULL COMMENT '市场平面图名称',
  `img_url` varchar(1000) NOT NULL COMMENT '底图OSS地址',
  `point_json` mediumtext NULL COMMENT 'Fabric画布全点位序列化JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `create_by` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL,
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_market_name_company` (`company_id`,`market_name`,`is_delete`),
  INDEX `idx_company_mark` (`company_id`,`market_id`,`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场平面图主表';
```

### 核心字段约束

1. point\_json 使用 mediumtext，适配 300 \+ 摊位大图 JSON；

2. JSON 入库前后端统一过滤`<script>`、onload 等恶意脚本；

3. 不允许前端本地存储点位数据，所有布局持久化本表。

## 6\.2 map\_stall\_point 点位明细表（可选，点位超 500 启用）

拆分 point\_json 单条存储，支持按 stall 快速检索，适合大量摊位场景；
字段包含 point\_id、map\_id、market\_id、company\_id、stall\_id、point\_data（单点位 JSON）、sort\_order、全局基础通用字段。

## 6\.3 地图数据一致性约束

1. stall\_id 必须关联 stall\_info 有效数据，保存点位后端校验；

2. 不允许绑定其他 company、其他 market 下摊位；

3. 摊位删除 / 退租不删除点位，仅状态标记，画布灰色提示；

4. 地图删除为逻辑删除，保留历史点位 JSON 用于追溯审计。

# 七、财务流水数据表强制规范（biz\_finance\_flow）

## 7\.1 表定位

全集团唯一资金流水源头，所有租赁、水电、营销优惠、押金收支必须写入本表，**禁止物理删除、禁止修改历史记录**。

## 7\.2 关键字段标准

- company\_id：所属分公司

- business\_type：业务类型（租赁 / 水电 / 营销抵扣 / 押金）

- bill\_id：关联单据 ID（stall\_contract\_id /water\_bill\_id）

- original\_amount：应收原价

- discount\_amount：优惠抵扣金额

- real\_amount：实际实收金额

- pay\_type：支付渠道

- flow\_type：收入 / 支出

- status：待支付 / 已支付 / 退费完成

## 7\.3 约束红线

1. 所有资金操作加事务，失败全部回滚，无单边流水；

2. 历史流水仅支持查询，无 update/delete 接口；

3. 索引优先按 company \+ 时间分区对账；

4. 永久归档，不做数据清理。

## 7\.4 自定义收费规则数据表规范（biz\_fee\_*，一期业务）

### 7\.4\.1 表定位

集团汽车城租赁、物业水电收费支持**自定义收费规则**：收费项、计费模式、周期、减免全部可配置，无需改代码。归属子公司私有业务表（company\_id 隔离），集团仅只读查看。

**重要边界**：本项目不做代码脚本式自定义公式，全部通过界面下拉 / 表单参数完成配置，避免脚本安全风险。

### 7\.4\.2 表清单与建表标准

|表名|用途|
|---|---|
|biz\_fee\_item|收费项表（租金 / 物业费 / 水费 / 电费 / 公摊费 / 押金 / 违约金 / 一次性杂费）|
|biz\_fee\_rule|收费规则表（计费模式 / 单价 / 周期 / 生效时间 / 减免 / 违约金配置）|
|biz\_fee\_rule\_stall\_rel|规则与摊位绑定关系表（支持单摊位特殊规则覆盖）|
|biz\_fee\_bill|周期收费账单表（历史账单锁定，修改规则不回溯）|
|biz\_fee\_bill\_detail|账单明细表（收费项快照固化，用于对账追溯）|

```sql
-- 收费项表：子公司可配置自己的收费项目
CREATE TABLE `biz_fee_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `fee_item_name` varchar(128) NOT NULL COMMENT '收费项目名称（租金/物业费/水费/电费/公摊费/押金/违约金等）',
  `fee_type` tinyint NOT NULL DEFAULT 1 COMMENT '计费类型 1固定金额 2按面积 3仪表计量 4按租金百分比 5逾期违约金 6公摊分摊',
  `calc_unit` varchar(32) DEFAULT NULL COMMENT '计量单位（元/月、元/平米、元/吨、元/度、百分比等）',
  `remark` varchar(500) DEFAULT NULL COMMENT '收费项备注说明',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义收费项表';

-- 收费规则表：计费模式 + 周期 + 生效规则 + 减免 + 违约金配置
CREATE TABLE `biz_fee_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `fee_item_id` bigint NOT NULL COMMENT '关联收费项ID',
  `calc_mode` tinyint NOT NULL DEFAULT 1 COMMENT '计费模式 1固定金额 2按面积 3仪表计量 4按租金百分比 5逾期违约金 6公摊分摊',
  `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '单价（固定金额/单价/百分比数值，违约金等特殊模式见rule_config_json）',
  `period_type` tinyint NOT NULL DEFAULT 1 COMMENT '账单周期 1按月 2按季 3按年 4一次性',
  `start_time` date NOT NULL COMMENT '规则生效时间',
  `end_time` date DEFAULT NULL COMMENT '规则失效时间，NULL永久有效',
  `discount_rate` decimal(5,2) NOT NULL DEFAULT 100.00 COMMENT '折扣率（百分比，100=无折扣）',
  `waive_months` int NOT NULL DEFAULT 0 COMMENT '免租期月数（前N个月免收）',
  `rule_config_json` mediumtext DEFAULT NULL COMMENT '阶梯价/违约金/公摊分摊等条件配置JSON（前端表单生成，后端过滤脚本后入库）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '规则状态 0停用 1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '规则备注说明',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_name_company` (`rule_name`,`company_id`,`is_delete`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_fee_item_id` (`fee_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义收费规则表';

-- 规则摊位绑定表：一套规则绑定多个摊位，支持单摊位特殊规则覆盖
CREATE TABLE `biz_fee_rule_stall_rel` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `rule_id` bigint NOT NULL COMMENT '收费规则ID',
  `stall_id` bigint NOT NULL COMMENT '摊位ID',
  `override_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否特殊覆盖 0普通绑定 1单摊位覆盖',
  `override_price` decimal(12,2) DEFAULT NULL COMMENT '覆盖单价（override_flag=1时生效）',
  `override_config_json` mediumtext DEFAULT NULL COMMENT '覆盖配置JSON（阶梯/违约金等，覆盖规则配置）',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_stall` (`rule_id`,`stall_id`,`is_delete`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_stall_id` (`stall_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费规则摊位绑定表';

-- 周期收费账单表：历史账单锁定，规则修改不回溯旧账单
CREATE TABLE `biz_fee_bill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `stall_id` bigint NOT NULL COMMENT '摊位ID',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `bill_month` varchar(32) NOT NULL COMMENT '账单周期标识（月yyyy-MM / 季yyyy-Qn / 年yyyy / 一次性custom）',
  `rule_id` bigint NOT NULL COMMENT '生成账单的规则ID（历史锁定后仅记录，不参与回溯计算）',
  `period_type` tinyint NOT NULL DEFAULT 1 COMMENT '账单周期类型 1按月 2按季 3按年 4一次性',
  `original_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收原价合计',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免金额合计',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '人工调账金额（正负均可，0=未调账）',
  `real_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际应收金额 = 原价 - 优惠 + 调账',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '缴费状态 0待缴 1已缴 2部分缴费',
  `pay_time` datetime DEFAULT NULL COMMENT '缴费完成时间',
  `locked_flag` tinyint NOT NULL DEFAULT 1 COMMENT '账单锁定 1锁定（已生成即锁定，规则变更不回溯）',
  `remark` varchar(500) DEFAULT NULL COMMENT '账单备注',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stall_period_company` (`company_id`,`stall_id`,`bill_month`,`is_delete`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_stall_id` (`stall_id`),
  INDEX `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义收费周期账单表';

-- 账单明细表：收费项快照固化，对账追溯依据
CREATE TABLE `biz_fee_bill_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL DEFAULT 0 COMMENT '所属子公司ID',
  `bill_id` bigint NOT NULL COMMENT '关联账单ID',
  `fee_item_id` bigint NOT NULL COMMENT '收费项ID',
  `fee_item_name` varchar(128) NOT NULL COMMENT '收费项名称快照（规则修改不回溯，保留生成时名称）',
  `calc_mode` tinyint NOT NULL DEFAULT 1 COMMENT '计费模式快照',
  `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
  `base_value` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '计费基数（面积/用量/百分比基数等）',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '该项金额小计',
  `detail_config_json` mediumtext DEFAULT NULL COMMENT '阶梯明细等过程数据JSON（审计追溯）',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  INDEX `idx_company_id_is_delete` (`company_id`,`is_delete`),
  INDEX `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费账单明细快照表';
```

### 7\.4\.3 收费规则数据约束

1. `rule_config_json` / `override_config_json` 使用 mediumtext 存储，入库前后端双重过滤 `<script>`、onload 等脚本字符，禁止明文拼接公式脚本；

2. **历史账单锁定**：biz\_fee\_bill 生成即 locked\_flag=1，修改规则只作用于未来周期，禁止回溯重算旧账单；明细表快照固化生成时名称 / 单价 / 计费基数；

3. 账单金额三要素公式：real\_amount = original\_amount \- discount\_amount + adjust\_amount，生成与调账均写入 biz\_finance\_flow 统一流水（business\_type 新增 fee\_bill 类型）；

4. 同一摊位同一周期唯一（uk\_stall\_period\_company），批量生成账单幂等：已存在周期直接跳过，不重复生成；

5. 阶梯水价 / 电价、违约金、公摊分摊配置全部存 JSON 参数，禁止落业务代码硬编码；

6. 收费规则、账单、明细与摊位 / 市场 / 商户绑定均强制 company\_id 校验，禁止跨子公司绑定摊位与生成账单；

7. 账单仅支持逻辑删除，缴费完成的账单禁止删除，禁止物理删除；调账操作必须写入审计日志（oper\_module=fee\_bill）。

# 八、MyBatis-Plus \& SQL 编写规范

## 8\.1 MyBatis-Plus\-Plus 统一约束

1. 所有 Mapper 继承 BaseMapper，单表增删改查禁止手写 XML；

2. 全局开启逻辑删除配置（is\_delete=1 过滤）；

3. 主键策略统一：ASSIGN\_ID 雪花 ID；

4. 自动填充 create\_by/create\_time/update\_by/update\_time；

5. 禁止 LambdaQueryWrapper 忽略 company\_id 查询。

## 8\.2 XML 复杂 SQL 规范

1. 多表联查、统计报表、大数据分页写 resources/mapper/\*\.xml；

2. 全部使用 \#\{\} 参数占位，**禁止 $\{\} 拼接用户输入**，杜绝 SQL 注入；

3. WHERE 条件强制拼接 AND is\_delete = 0；

4. 分页统一使用 MyBatis-Plus 分页插件，不手动 limit 硬编码；

5. 禁止 select \*，必须指定业务所需字段，减少 IO；

6. JSON 查询使用 mysql json 函数，禁止字符串模糊匹配点位数据。

## 8\.3 SQL 开发红线

1. 禁止直接拼接前端参数到 SQL；

2. 禁止无 limit 全表查询（大数据表崩溃风险）；

3. 禁止 SELECT \*, 只查业务必要字段；

4. 禁止 join 三张以上大表，拆分查询；

5. 禁止函数作用于索引字段（失效）；

6. 禁止业务代码执行 DROP/ALTER/TRUNCATE，DDL 仅本地开发执行。

# 九、数据生命周期与删除规范

1. **逻辑删除全局标准**：所有业务表 is\_delete=1 标记删除，UPDATE 更新，不执行 DELETE；

2. **禁止物理删除表**：

    - 财务流水、审计日志、市场地图、合同、缴费记录永久禁止 DELETE；

    - 仅测试库可清理，生产库无物理删除权限；

3. 归档规则：

    - 超过 3 年历史合同、账单可导出备份，不删除库内数据；

    - 审计日志永久留存，满足内控、等保、财务审计。

# 十、审计日志数据表设计规范（sys\_audit\_log）

## 10\.1 强制记录字段

- id、company\_id、oper\_user\_id、oper\_user\_name、oper\_ip

- oper\_module（区分 market\_map 地图 / 财务 / 权限）

- oper\_type：新增 / 编辑 / 删除 / 导出 / 权限复核

- before\_json：操作前完整 JSON 快照

- after\_json：操作后完整 JSON 快照

- audit\_oper\_id（二级复核人，权限变更专用）

- create\_time、is\_delete

## 10\.2 地图专项日志要求

oper\_module 固定赋值`stall_map`，单独筛选地图所有编辑、删除、底图替换记录；

## 10\.3 数据规则

本表禁止清理、禁止逻辑删除，全量归档，仅集团管理员可导出。

# 十一、字段安全与加密存储规范

1. 敏感信息加密字段（AES 对称加密）：
商户身份证、商户对公账户、用户登录密码（BCrypt）、第三方 AppSecret；

2. 禁止数据库明文存储密钥、证件；

3. 画布 point\_json 前端提交过滤 XSS，后端二次清洗入库；

4. 导出接口查询时，敏感字段自动脱敏返回前端，数据库保留密文；

5. 数据库账号密码加密存放 yml，不硬编码项目、不提交 git。

# 十二、数据库环境、备份、迁移规范

## 12\.1 环境隔离

1. dev 开发库：允许测试数据、可执行 DDL；

2. prod 生产库：禁止手动执行 DDL、DML，所有变更提供 SQL 脚本；

3. 开发 / 生产库账号权限分离，生产库无 DROP/ALTER 权限。

## 12\.2 备份规范

1. 生产库每日凌晨全量备份；

2. 备份文件留存 7 天以上，加密存储；

3. 财务、地图、审计表单独备份优先级最高；

4. 故障回滚标准：使用前一日备份恢复，核对地图点位、资金流水完整性。

## 12\.3 版本迭代迁移规范

1. 所有新增表、新增字段、索引修改统一输出 upgrade\.sql 脚本；

2. 脚本按版本号命名 v1\.0\_xxx\.sql，按顺序执行；

3. 上线前本地执行脚本校验无语法报错；

4. 禁止线上直接手动 alter 表结构。

# 十三、数据库开发强制红线（禁止行为）

1. 业务表缺失 company\_id 全局租户字段；

2. 使用 float/double 存储租金、缴费金额；

3. SQL 使用 $\{\} 拼接前端输入，存在注入漏洞；

4. 地图 point\_json 不做脚本过滤直接入库；

5. 财务流水、审计日志执行 DELETE 物理删除；

6. 接口查询不携带 company\_id 过滤，造成跨公司数据泄露；

7. 单表创建超过 5 个索引，严重影响写入性能；

8. 硬编码固定 company\_id、market\_id 写死在 SQL；

9. 前端传入 company\_id 直接作为查询条件，不读取 JWT 上下文；

10. 地图点位保存不校验 stall 归属公司，允许跨子公司绑定；
11 生产环境手动执行 DROP、TRUNCATE、大批量 DELETE；
12 画布点位、商户敏感信息明文长期对外导出。

