-- ===============================================================
-- 微信公众号爬虫系统 - 更新字段类型脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：将所有TEXT类型字段更新为MEDIUMTEXT类型
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 更新article_table表中的字段类型
ALTER TABLE article_table 
MODIFY COLUMN content MEDIUMTEXT NOT NULL COMMENT '文章文本内容',
MODIFY COLUMN images MEDIUMTEXT COMMENT '图片路径列表，以逗号分隔',
MODIFY COLUMN image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息',
MODIFY COLUMN summary MEDIUMTEXT COMMENT '文章摘要内容';

-- 更新article_full_html表中的字段类型
ALTER TABLE article_full_html 
MODIFY COLUMN full_html MEDIUMTEXT NOT NULL COMMENT '文章完整HTML内容';

-- 验证修改是否成功
SELECT 
  TABLE_NAME,
  COLUMN_NAME, 
  COLUMN_TYPE,
  IS_NULLABLE
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME IN ('article_table', 'article_full_html')
  AND COLUMN_TYPE LIKE '%TEXT%';

-- 执行说明
-- 1. 此脚本将所有TEXT类型字段更新为MEDIUMTEXT类型
-- 2. 更新了以下字段：
--    - article_table.content
--    - article_table.images
--    - article_table.image_mappings
--    - article_table.summary
--    - article_full_html.full_html
-- 3. 此修改不会影响现有数据，只是增加了字段的容量
-- 4. MEDIUMTEXT类型最多可存储16MB数据，而TEXT类型只能存储64KB 