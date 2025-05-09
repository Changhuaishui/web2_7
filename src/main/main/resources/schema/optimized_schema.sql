-- ===============================================================
-- 微信公众号爬虫系统 - 优化版数据库脚本
-- ===============================================================
-- 更新时间：2025年5月5日
-- 数据库：crawler_db
-- 用途：提供一套完整的数据库操作脚本，包括创建、查询、清空和删除操作
-- ===============================================================

-- 第一部分：创建数据库
-- ===============================================================
CREATE DATABASE IF NOT EXISTS crawler_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE crawler_db;

-- 第二部分：创建表结构
-- ===============================================================

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
  images TEXT COMMENT '图片路径列表，以逗号分隔',
  image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息',
  summary TEXT COMMENT '文章摘要内容',
  keywords VARCHAR(255) COMMENT '文章关键词，逗号分隔',
  is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（逻辑删除）',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY idx_article_url (url(255)),
  UNIQUE KEY idx_article_ulid (ulid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章基本信息表';

-- 创建文章HTML内容表
CREATE TABLE IF NOT EXISTS article_full_html (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  article_id INT NOT NULL COMMENT '关联article_table的ID',
  full_html MEDIUMTEXT NOT NULL COMMENT '文章完整HTML内容',
  url_mapping MEDIUMTEXT COMMENT '存储原始URL到本地路径的映射关系JSON',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (article_id) REFERENCES article_table(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章HTML内容表';

-- 创建相关文章表
CREATE TABLE IF NOT EXISTS related_articles (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  article_id INT NOT NULL COMMENT '源文章ID',
  related_url VARCHAR(1024) NOT NULL COMMENT '相关文章URL',
  title VARCHAR(255) COMMENT '相关文章标题',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (article_id) REFERENCES article_table(id) ON DELETE CASCADE,
  UNIQUE KEY idx_article_related_url (article_id, related_url(255)) COMMENT '确保同一文章不重复添加相同URL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相关文章表';

-- 创建索引以提高查询性能
CREATE INDEX idx_article_title ON article_table(title);
CREATE INDEX idx_article_author ON article_table(author);
CREATE INDEX idx_article_account_name ON article_table(account_name);
CREATE INDEX idx_article_publish_time ON article_table(publish_time);
CREATE INDEX idx_article_is_deleted ON article_table(is_deleted);

-- 第三部分：常用数据查询SQL
-- ===============================================================

-- 1. 查询表结构
-- 查询所有表
SELECT 
  TABLE_NAME, 
  TABLE_COMMENT, 
  ENGINE, 
  TABLE_COLLATION
FROM 
  INFORMATION_SCHEMA.TABLES 
WHERE 
  TABLE_SCHEMA = 'crawler_db';

-- 查询表字段信息
SELECT 
  TABLE_NAME,
  COLUMN_NAME, 
  COLUMN_TYPE, 
  IS_NULLABLE,
  COLUMN_DEFAULT,
  COLUMN_COMMENT
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
ORDER BY 
  TABLE_NAME, ORDINAL_POSITION;

-- 查询表索引信息
SELECT 
  TABLE_NAME,
  INDEX_NAME,
  COLUMN_NAME,
  NON_UNIQUE
FROM
  INFORMATION_SCHEMA.STATISTICS
WHERE
  TABLE_SCHEMA = 'crawler_db'
ORDER BY
  TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 查询表外键信息
SELECT 
  CONSTRAINT_NAME,
  TABLE_NAME,
  COLUMN_NAME,
  REFERENCED_TABLE_NAME,
  REFERENCED_COLUMN_NAME
FROM
  INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
  TABLE_SCHEMA = 'crawler_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY
  TABLE_NAME, CONSTRAINT_NAME;

-- 2. 查询表数据
-- 查询所有文章（按发布时间倒序，排除已删除的）
SELECT id, ulid, title, author, url, source_url, account_name, publish_time, LEFT(content, 100) as content_preview, 
       summary, keywords, is_deleted, created_at, updated_at
FROM article_table 
WHERE is_deleted = false
ORDER BY publish_time DESC;

-- 查询文章详情
SELECT a.id, a.ulid, a.title, a.author, a.url, a.source_url, a.account_name, 
       a.publish_time, a.content, a.images, a.image_mappings, a.summary, a.keywords,
       h.full_html
FROM article_table a
LEFT JOIN article_full_html h ON a.id = h.article_id
WHERE a.id = ?;  -- 替换?为实际文章ID

-- 查询相关文章
SELECT r.id, r.article_id, r.related_url, r.title, r.created_at,
       a.title as source_article_title
FROM related_articles r
JOIN article_table a ON r.article_id = a.id
WHERE r.article_id = ?;  -- 替换?为实际文章ID

-- 根据关键词搜索文章
SELECT id, ulid, title, author, account_name, publish_time, LEFT(content, 100) as content_preview
FROM article_table
WHERE (title LIKE ? OR content LIKE ? OR keywords LIKE ?)
      AND is_deleted = false
ORDER BY publish_time DESC;
-- 用法：将?替换为'%关键词%'

-- 统计数据
SELECT 
  (SELECT COUNT(*) FROM article_table WHERE is_deleted = false) as total_articles,
  (SELECT COUNT(DISTINCT account_name) FROM article_table WHERE account_name IS NOT NULL AND is_deleted = false) as total_accounts,
  (SELECT COUNT(*) FROM article_full_html) as articles_with_html,
  (SELECT COUNT(*) FROM related_articles) as total_related_articles;

-- 第四部分：清空表数据（保留表结构）
-- ===============================================================
-- 警告：此操作将删除所有数据，请谨慎执行

-- 按照外键关系的正确顺序清空表
-- 1. 先清空从表
TRUNCATE TABLE related_articles;
TRUNCATE TABLE article_full_html;

-- 2. 清空主表 (需要先禁用外键检查)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE article_table;
SET FOREIGN_KEY_CHECKS = 1;

-- 第五部分：删除表（完全删除表结构及数据）
-- ===============================================================
-- 警告：此操作将删除所有表和数据，请谨慎执行

-- 按照外键关系的正确顺序删除表
-- 1. 先删除从表
DROP TABLE IF EXISTS related_articles;
DROP TABLE IF EXISTS article_full_html;

-- 2. 删除主表
DROP TABLE IF EXISTS article_table;

-- 3. 删除数据库（慎用）
-- DROP DATABASE IF EXISTS crawler_db;

-- 第六部分：有用的维护脚本
-- ===============================================================

-- 1. 删除所有逻辑删除的文章（物理删除）
DELETE FROM article_table WHERE is_deleted = true;

-- 2. 批量更新某公众号的文章
UPDATE article_table 
SET account_name = ?, is_deleted = false  -- 替换为实际的公众号名称
WHERE account_name LIKE ?;  -- 用于模糊匹配，如'%公众号名%'

-- 3. 检查孤立记录（文章HTML内容表中存在但文章表中不存在的记录）
SELECT h.* 
FROM article_full_html h
LEFT JOIN article_table a ON h.article_id = a.id
WHERE a.id IS NULL;

-- 4. 检查缺失记录（文章表中存在但文章HTML内容表中不存在的记录）
SELECT a.id, a.title
FROM article_table a
LEFT JOIN article_full_html h ON a.id = h.article_id
WHERE h.id IS NULL AND a.is_deleted = false;

-- 完整性检查
SELECT 'Database schema verification completed' as message; 