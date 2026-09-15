-- =====================================================
-- v2.8 客户端设备校验安全加固
-- 新增 sys_client_device_auth 表（授权设备清单）
-- 新增 sys_config 配置项（设备校验开关、RSA密钥）
-- =====================================================

-- 1. 授权设备清单表（集团统一授权，无 company_id 隔离）
CREATE TABLE IF NOT EXISTS `sys_client_device_auth` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `motherboard_sn` varchar(128) NOT NULL COMMENT '主板SN',
  `cpu_id` varchar(128) NOT NULL COMMENT 'CPU编号',
  `disk_sn` varchar(128) DEFAULT NULL COMMENT '硬盘序列号（可选）',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称（备注）',
  `authorized_by` bigint NOT NULL DEFAULT 0 COMMENT '授权人ID',
  `expire_time` datetime DEFAULT NULL COMMENT '授权有效期（NULL表示永久）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sn_cpu` (`motherboard_sn`, `cpu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端设备授权清单';

-- 2. 插入系统配置项
INSERT INTO `sys_config` (`company_id`, `config_key`, `config_name`, `config_value`, `remark`) VALUES
(0, 'device.auth.enabled', '客户端设备校验开关', 'false', '开启后登录需校验设备授权'),
(0, 'device.auth.rsa_private_key', '设备校验RSA私钥', '', 'PEM格式私钥，仅后端使用'),
(0, 'device.auth.rsa_public_key', '设备校验RSA公钥', '', 'PEM格式公钥，用于验签');
