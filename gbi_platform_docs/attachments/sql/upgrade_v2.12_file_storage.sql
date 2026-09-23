-- ============================================================
-- 升级 v2.12：文件上传存储模块
-- 执行方式: mysql -uroot -p21145211 --default-character-set=utf8mb4 group_rent_db < upgrade_v2.12_file_storage.sql
-- ============================================================
SET NAMES utf8mb4;

-- 1. 新建 sys_file 表
CREATE TABLE IF NOT EXISTS `sys_file` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id`        BIGINT           NOT NULL DEFAULT 0   COMMENT '租户ID，0=集团全局',
  `biz_type`          VARCHAR(32)      NOT NULL DEFAULT ''  COMMENT '业务类型：contract/oa/hr/finance/property/audit等',
  `biz_id`            BIGINT           NOT NULL DEFAULT 0   COMMENT '关联业务单据ID',
  `file_key`          VARCHAR(512)     NOT NULL DEFAULT ''  COMMENT '跨存储统一相对路径唯一标识（不含域名）',
  `md5`               VARCHAR(32)      NOT NULL DEFAULT ''  COMMENT '文件MD5值，用于去重提示和完整性校验',
  `file_name`         VARCHAR(256)     NOT NULL DEFAULT ''  COMMENT '原始文件名（含后缀）',
  `file_size`         BIGINT           NOT NULL DEFAULT 0   COMMENT '文件大小（字节）',
  `file_type`         VARCHAR(64)      NOT NULL DEFAULT ''  COMMENT 'MIME类型，如 image/png application/pdf',
  `file_ext`          VARCHAR(16)      NOT NULL DEFAULT ''  COMMENT '文件后缀，如 jpg pdf',
  `storage_type`      VARCHAR(16)      NOT NULL DEFAULT 'local' COMMENT '存储类型快照（审计追溯用，运行时不以此为驱动依据）',
  `is_delete`         TINYINT          NOT NULL DEFAULT 0   COMMENT '逻辑删除 0=正常 1=已删除',
  `create_by`         BIGINT           NOT NULL DEFAULT 0   COMMENT '创建人user_id',
  `create_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         BIGINT           NULL DEFAULT NULL    COMMENT '更新人user_id',
  `update_time`       DATETIME         NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_file_key` (`company_id`, `file_key`),
  KEY `idx_company_biz` (`company_id`, `biz_type`, `biz_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统文件记录表';

-- 2. sys_config 初始化：文件存储配置（仅插入不存在的行）
INSERT IGNORE INTO `sys_config` (`company_id`, `config_key`, `config_name`, `config_value`, `remark`) VALUES
(0, 'file.storage.type', '文件存储类型', 'local', '枚举：local / alioss / tianyoss，仅集团管理员可修改。生产环境必须使用云存储'),
(0, 'file.max-size', '文件最大上传大小(MB)', '20', '单个文件最大上传大小，单位MB'),
(0, 'file.aes-key', '文件加密密钥', '', 'AES加密密钥，用于加密存储云存储AK/SK，通过环境变量注入，禁止硬编码'),
(0, 'file.oss-alibaba.endpoint', '阿里云OSS Endpoint', '', '示例：oss-cn-hangzhou.aliyuncs.com，请在控制台获取'),
(0, 'file.oss-alibaba.bucket', '阿里云OSS Bucket名称', '', '请在控制台获取'),
(0, 'file.oss-alibaba.access-key', '阿里云OSS AccessKey', '', '加密存储，禁止明文日志打印'),
(0, 'file.oss-alibaba.secret-key', '阿里云OSS SecretKey', '', '加密存储，禁止明文日志打印'),
(0, 'file.oss-alibaba.domain', '阿里云OSS CDN域名', '', '示例：https://cdn.example.com，私有桶必填'),
(0, 'file.oss-tianyi.endpoint', '天翼云ZOS Endpoint', '', '示例：https://cos.ap-nanjing.myhwclouds.com'),
(0, 'file.oss-tianyi.bucket', '天翼云ZOS Bucket名称', '', '请在控制台获取'),
(0, 'file.oss-tianyi.access-key', '天翼云ZOS AccessKey', '', '加密存储，禁止明文日志打印'),
(0, 'file.oss-tianyi.secret-key', '天翼云ZOS SecretKey', '', '加密存储，禁止明文日志打印'),
(0, 'file.oss-tianyi.domain', '天翼云ZOS CDN域名', '', '示例：https://cdn.example.com，私有桶必填');
