-- ===============================================================
-- 微信公众号爬虫系统 - 表结构修改脚本
-- ===============================================================
-- 创建时间：2024年4月30日
-- 数据库：crawler_db
-- 用途：添加关键词和相关文章功能所需的表结构
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 向article_table添加keywords字段
ALTER TABLE article_table
ADD COLUMN keywords VARCHAR(255) COMMENT '文章关键词，逗号分隔' AFTER summary;

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

-- 验证字段和表是否创建成功
SELECT 
  COLUMN_NAME, 
  COLUMN_TYPE,
  COLUMN_COMMENT
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME = 'article_table'
  AND COLUMN_NAME = 'keywords';

-- 验证related_articles表是否创建成功
SELECT 
  TABLE_NAME,
  TABLE_COMMENT
FROM 
  INFORMATION_SCHEMA.TABLES 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME = 'related_articles'; 