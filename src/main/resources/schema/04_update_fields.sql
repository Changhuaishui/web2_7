-- ===============================================================
-- 微信公众号爬虫系统 - 字段更新脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：更新字段类型和属性
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