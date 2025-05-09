-- ===============================================================
-- 微信公众号爬虫系统 - 添加摘要字段脚本
-- ===============================================================
-- 创建时间：2025年4月29日
-- 数据库：crawler_db
-- 说明：为article_table表添加summary字段，用于存储DeepSeek生成的文章摘要
--       如果表结构已包含此字段则不会有任何影响
-- ===============================================================

-- 检查article_table表是否存在summary字段，如果不存在则添加
SET @exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'article_table' 
    AND COLUMN_NAME = 'summary'
);

-- 如果summary字段不存在，则添加该字段
SET @query = IF(@exists = 0, 
    'ALTER TABLE article_table ADD COLUMN summary TEXT COMMENT "文章摘要内容"', 
    'SELECT "摘要字段已存在，无需添加" AS message'
);

-- 执行添加字段或显示提示消息
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt; 