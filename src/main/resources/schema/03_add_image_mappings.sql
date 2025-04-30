-- ===============================================================
-- 微信公众号爬虫系统 - 添加图片映射字段脚本
-- ===============================================================
-- 创建时间：2025年4月29日
-- 更新时间：2025年4月30日
-- 数据库：crawler_db
-- 表：article_table
-- 更新内容：
--   1. 添加image_mappings字段用于存储图片位置映射信息
--   2. 将字段类型设置为MEDIUMTEXT以支持更大的数据量
-- ===============================================================

-- 添加图片映射字段
ALTER TABLE article_table
ADD COLUMN image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息' AFTER images;

-- 验证字段添加
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'crawler_db' 
    AND TABLE_NAME = 'article_table'
    AND COLUMN_NAME = 'image_mappings'; 