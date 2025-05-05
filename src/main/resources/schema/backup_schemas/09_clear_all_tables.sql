-- SQL脚本：清除所有表数据但保留表结构
-- 文件名: 09_clear_all_tables.sql
-- 创建时间: 2025-05-02
-- 描述: 此脚本用于清除系统中所有表的数据，但保留表结构和约束

-- 禁用外键约束检查，以便能够清空有关联的表
SET FOREIGN_KEY_CHECKS = 0;

-- 清空相关文章表
TRUNCATE TABLE related_articles;

-- 清空文章全文HTML表
TRUNCATE TABLE article_full_html;

-- 清空文章标签关联表（如果存在）
-- TRUNCATE TABLE article_tag_mapping;

-- 清空标签表（如果存在）
-- TRUNCATE TABLE tags;

-- 清空主文章表
TRUNCATE TABLE article_table;

-- 重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 提示清理完成
SELECT 'All tables have been cleared successfully.' AS 'Status'; 