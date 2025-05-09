-- ===============================================================
-- 微信公众号爬虫系统 - 数据库初始化脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：创建数据库和所有必要的表
-- ===============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS crawler_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE crawler_db;

-- 创建文章基本信息表
CREATE TABLE IF NOT EXISTS article_table (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  ulid VARCHAR(26) NOT NULL COMMENT 'ULID唯一标识',
  title VARCHAR(255) NOT NULL COMMENT '文章标题',
  author VARCHAR(100) COMMENT '文章作者',
  url VARCHAR(1024) NOT NULL COMMENT '文章原始URL',
  source_url VARCHAR(1024) COMMENT '文章来源URL',
  account_name VARCHAR(100) COMMENT '公众号名称',
  publish_time DATETIME COMMENT '发布时间',
  content MEDIUMTEXT NOT NULL COMMENT '文章文本内容',
  images MEDIUMTEXT COMMENT '图片路径列表，以逗号分隔',
  image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息',
  is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（逻辑删除）',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  summary MEDIUMTEXT COMMENT '文章摘要内容',
  UNIQUE KEY idx_article_url (url(255)),
  UNIQUE KEY idx_article_ulid (ulid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章基本信息表';

-- 创建文章HTML内容表
CREATE TABLE IF NOT EXISTS article_full_html (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  article_id INT NOT NULL COMMENT '关联article_table的ID',
  full_html MEDIUMTEXT NOT NULL COMMENT '文章完整HTML内容',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (article_id) REFERENCES article_table(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章HTML内容表';

-- 创建索引
CREATE INDEX idx_article_title ON article_table(title);
CREATE INDEX idx_article_author ON article_table(author);
CREATE INDEX idx_article_account_name ON article_table(account_name);
CREATE INDEX idx_article_publish_time ON article_table(publish_time);
CREATE INDEX idx_article_is_deleted ON article_table(is_deleted);

-- 验证表创建
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    ENGINE,
    TABLE_COLLATION
FROM 
    INFORMATION_SCHEMA.TABLES 
WHERE 
    TABLE_SCHEMA = 'crawler_db'; 