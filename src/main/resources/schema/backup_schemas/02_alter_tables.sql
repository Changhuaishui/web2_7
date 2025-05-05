-- ===============================================================
-- 微信公众号爬虫系统 - 表结构修改脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：修改表结构，添加必要字段
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 添加ULID字段
ALTER TABLE article_table
ADD COLUMN ulid VARCHAR(26) NOT NULL COMMENT 'ULID唯一标识' AFTER id,
ADD UNIQUE KEY idx_article_ulid (ulid);

-- 添加图片映射字段
ALTER TABLE article_table
ADD COLUMN image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息' AFTER images;

-- 添加文章摘要字段
ALTER TABLE article_table
ADD COLUMN summary MEDIUMTEXT COMMENT '文章摘要内容' AFTER content;

-- 验证字段添加
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'crawler_db' 
    AND TABLE_NAME = 'article_table'
    AND COLUMN_NAME IN ('ulid', 'image_mappings', 'summary'); 