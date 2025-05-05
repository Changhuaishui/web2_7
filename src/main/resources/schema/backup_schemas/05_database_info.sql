-- ===============================================================
-- 微信公众号爬虫系统 - 数据库结构信息查询脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 用途：查询数据库中的所有表结构、索引和约束信息
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 1. 查询所有表的基本信息
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    ENGINE,
    TABLE_COLLATION,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH,
    CREATE_TIME,
    UPDATE_TIME
FROM 
    INFORMATION_SCHEMA.TABLES 
WHERE 
    TABLE_SCHEMA = 'crawler_db'
ORDER BY 
    TABLE_NAME;

-- 2. 查询所有表的字段信息
SELECT 
    t.TABLE_NAME,
    t.COLUMN_NAME,
    t.COLUMN_TYPE,
    t.IS_NULLABLE,
    t.COLUMN_DEFAULT,
    t.COLUMN_COMMENT,
    t.EXTRA,
    t.COLUMN_KEY
FROM 
    INFORMATION_SCHEMA.COLUMNS t
WHERE 
    t.TABLE_SCHEMA = 'crawler_db'
ORDER BY 
    t.TABLE_NAME,
    t.ORDINAL_POSITION;

-- 3. 查询所有表的索引信息
SELECT 
    t.TABLE_NAME,
    t.INDEX_NAME,
    t.NON_UNIQUE,
    t.INDEX_TYPE,
    GROUP_CONCAT(t.COLUMN_NAME ORDER BY t.SEQ_IN_INDEX) AS COLUMNS,
    t.INDEX_COMMENT
FROM 
    INFORMATION_SCHEMA.STATISTICS t
WHERE 
    t.TABLE_SCHEMA = 'crawler_db'
GROUP BY 
    t.TABLE_NAME,
    t.INDEX_NAME,
    t.NON_UNIQUE,
    t.INDEX_TYPE,
    t.INDEX_COMMENT
ORDER BY 
    t.TABLE_NAME,
    t.INDEX_NAME;

-- 4. 查询所有表的外键约束信息
SELECT 
    k.TABLE_NAME,
    k.CONSTRAINT_NAME,
    k.COLUMN_NAME,
    k.REFERENCED_TABLE_NAME,
    k.REFERENCED_COLUMN_NAME,
    r.UPDATE_RULE,
    r.DELETE_RULE
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE k
JOIN 
    INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS r
    ON k.CONSTRAINT_NAME = r.CONSTRAINT_NAME
    AND k.TABLE_SCHEMA = r.CONSTRAINT_SCHEMA
WHERE 
    k.TABLE_SCHEMA = 'crawler_db'
    AND k.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY 
    k.TABLE_NAME,
    k.CONSTRAINT_NAME;

-- 5. 查询所有表的字符集和排序规则信息
SELECT 
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM 
    INFORMATION_SCHEMA.TABLES t
JOIN 
    INFORMATION_SCHEMA.COLLATIONS c
    ON t.TABLE_COLLATION = c.COLLATION_NAME
WHERE 
    t.TABLE_SCHEMA = 'crawler_db'
ORDER BY 
    TABLE_NAME; 