-- =====================================================
-- 升级脚本 v2.7：字典表创建
-- 执行顺序：第1步
-- =====================================================

-- 1. 城市字典表
CREATE TABLE IF NOT EXISTS sys_city (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  city_code   VARCHAR(32)  NOT NULL COMMENT '城市编码，如 BJ/GZ/SZ',
  city_name   VARCHAR(64)  NOT NULL COMMENT '城市名称，如 北京/广州/深圳',
  province    VARCHAR(32)  DEFAULT NULL COMMENT '省份',
  sort        INT NOT NULL DEFAULT 0 COMMENT '排序',
  status      TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by   BIGINT DEFAULT NULL,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete   TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_city_code (city_code, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市字典表';

-- 2. 险种字典表
CREATE TABLE IF NOT EXISTS sys_insurance_type (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  insurance_code  VARCHAR(32)  NOT NULL COMMENT '险种编码，如 PENSION/MEDICAL/UNEMPLOYMENT/WORK_INJURY/MATERNITY/LONG_CARE/SUPPLEMENT_MEDICAL/ANNUITY',
  insurance_name  VARCHAR(64)  NOT NULL COMMENT '险种名称',
  insurance_type  TINYINT NOT NULL COMMENT '分类 1法定五险 2补充福利 3试点险种',
  personal_share  TINYINT NOT NULL DEFAULT 1 COMMENT '是否含个人缴纳部分 0否 1是',
  company_share   TINYINT NOT NULL DEFAULT 1 COMMENT '是否含单位缴纳部分 0否 1是',
  status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  remark          VARCHAR(500) DEFAULT NULL,
  create_by       BIGINT NOT NULL DEFAULT 0,
  create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_delete       TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_insurance_code (insurance_code, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='险种字典表';

-- 3. 行业字典表
CREATE TABLE IF NOT EXISTS sys_industry (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  industry_code       VARCHAR(32) NOT NULL COMMENT '行业编码',
  industry_name       VARCHAR(128) NOT NULL COMMENT '行业名称',
  work_injury_rate_base DECIMAL(6,4) DEFAULT 0.0000 COMMENT '工伤保险行业基准费率(%)',
  status              TINYINT NOT NULL DEFAULT 1,
  remark              VARCHAR(500) DEFAULT NULL,
  create_by           BIGINT NOT NULL DEFAULT 0,
  create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_delete           TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_industry_code (industry_code, is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业字典表';
