-- ===============================================================
-- 微信公众号爬虫系统 - 数据库创建脚本
-- ===============================================================
-- 创建时间：2025年4月18日
-- 数据库：crawler_db
-- 说明：创建爬虫系统所需的数据库
-- ===============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS crawler_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE crawler_db;

-- 创建专门的用户并赋予权限（可选，根据需要使用）
-- CREATE USER 'crawler_user'@'localhost' IDENTIFIED BY 'password';
-- GRANT ALL PRIVILEGES ON crawler_db.* TO 'crawler_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 注意：创建数据库后，请执行01_create_tables.sql脚本创建表结构 