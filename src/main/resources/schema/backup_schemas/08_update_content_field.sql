-- ===============================================================
-- 微信公众号爬虫系统 - 更新content和image_mappings字段类型
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：将content和image_mappings字段类型从TEXT修改为MEDIUMTEXT
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 修改content字段类型
ALTER TABLE article_table MODIFY COLUMN content MEDIUMTEXT NOT NULL COMMENT '文章文本内容';

-- 修改image_mappings字段类型
ALTER TABLE article_table MODIFY COLUMN image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息';

-- 验证修改是否成功
SELECT 
  COLUMN_NAME, 
  COLUMN_TYPE,
  IS_NULLABLE
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME = 'article_table'
  AND COLUMN_NAME IN ('content', 'image_mappings');

-- 执行说明
-- 1. 此脚本解决了文章内容过长导致的数据截断问题
-- 2. MEDIUMTEXT类型最多可存储16MB数据，而TEXT类型只能存储64KB
-- 3. 对于特别长的内容，可以考虑使用LONGTEXT类型(4GB)
-- 4. 此修改不会影响现有数据，只是增加了字段的容量 