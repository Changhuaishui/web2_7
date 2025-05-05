-- ===============================================================
-- 微信公众号爬虫系统 - 数据库表重建脚本
-- ===============================================================
-- 创建时间：2025年4月30日
-- 数据库：crawler_db
-- 执行顺序：
-- 1. 确保数据库存在
-- 2. 删除现有表（如果存在）
-- 3. 创建新表（包含所有必要字段）
-- 4. 创建索引
-- ===============================================================

-- 确保使用正确的数据库
USE crawler_db;

-- 首先删除现有表（如果存在）- 注意删除顺序（先删除有外键依赖的表）
DROP TABLE IF EXISTS article_full_html;
DROP TABLE IF EXISTS article_table;

-- 创建文章基本信息表
CREATE TABLE article_table (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  ulid VARCHAR(26) NOT NULL COMMENT 'ULID唯一标识',
  title VARCHAR(255) NOT NULL COMMENT '文章标题',
  author VARCHAR(100) COMMENT '文章作者',
  url VARCHAR(1024) NOT NULL COMMENT '文章原始URL',
  source_url VARCHAR(1024) COMMENT '文章来源URL',
  account_name VARCHAR(100) COMMENT '公众号名称',
  publish_time DATETIME COMMENT '发布时间',
  content MEDIUMTEXT NOT NULL COMMENT '文章文本内容',
  images TEXT COMMENT '图片路径列表，以逗号分隔',
  image_mappings MEDIUMTEXT COMMENT '图片位置映射JSON信息',
  is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（逻辑删除）',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  summary TEXT COMMENT '文章摘要内容',
  UNIQUE KEY idx_article_url (url(255)),
  UNIQUE KEY idx_article_ulid (ulid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章基本信息表';

-- 创建文章HTML内容表
CREATE TABLE article_full_html (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
  article_id INT NOT NULL COMMENT '关联article_table的ID',
  full_html MEDIUMTEXT NOT NULL COMMENT '文章完整HTML内容',
  url_mapping MEDIUMTEXT COMMENT '存储原始URL到本地路径的映射关系JSON',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (article_id) REFERENCES article_table(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章HTML内容表';

-- 创建索引
CREATE INDEX idx_article_title ON article_table(title);
CREATE INDEX idx_article_author ON article_table(author);
CREATE INDEX idx_article_account_name ON article_table(account_name);
CREATE INDEX idx_article_publish_time ON article_table(publish_time);
CREATE INDEX idx_article_is_deleted ON article_table(is_deleted);

-- 验证字段是否创建成功
SELECT 
  COLUMN_NAME, 
  COLUMN_TYPE,
  IS_NULLABLE
FROM 
  INFORMATION_SCHEMA.COLUMNS 
WHERE 
  TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME = 'article_table';

-- 验证ulid列是否存在并有正确的约束
SELECT
  CONSTRAINT_NAME,
  COLUMN_NAME
FROM
  INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
  TABLE_SCHEMA = 'crawler_db'
  AND TABLE_NAME = 'article_table'
  AND COLUMN_NAME = 'ulid';

-- 执行说明
-- 1. 此脚本重建了所有必需的表结构
-- 2. 包含了所有必要的字段以满足代码需求
-- 3. 确保了image_mappings字段存在于article_table表中
-- 4. 确保了url_mapping字段存在于article_full_html表中
-- 5. 确保了summary字段存在于article_table表中
-- 6. 添加了必要的索引以提高查询性能
-- 7. 添加了验证步骤以确保ulid字段被正确创建
-- 注意：标签处理是通过TagService动态计算，不使用数据库存储 