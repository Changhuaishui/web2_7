-- ===============================================================
-- 微信公众号爬虫系统 - 数据库表修改脚本（添加ULID字段）
-- ===============================================================
-- 创建时间：2023年4月18日
-- 数据库：crawler_db
-- 修改说明：为现有的article_table表添加ULID字段，用于替代基于标题的图片命名
-- ===============================================================

-- 为现有表添加ULID字段
ALTER TABLE article_table ADD COLUMN ulid VARCHAR(32) AFTER id;

-- 为现有记录生成临时ID（这里使用MySQL的UUID函数，实际应用中应该通过Java代码更新为正确的ULID）
-- 截取为26位以匹配ULID长度
UPDATE article_table SET ulid = LEFT(REPLACE(UUID(), '-', ''), 26) WHERE ulid IS NULL;

-- 将列大小修改为标准ULID大小
ALTER TABLE article_table MODIFY COLUMN ulid VARCHAR(26);

-- 创建唯一索引
ALTER TABLE article_table ADD UNIQUE INDEX idx_article_ulid (ulid);

-- 添加非空约束和注释
ALTER TABLE article_table MODIFY COLUMN ulid VARCHAR(26) NOT NULL COMMENT 'ULID，用于唯一标识文章';

-- 注意：此脚本仅用于向现有数据库添加ULID字段
-- 新建数据库时应直接使用01_create_tables.sql脚本，其中已包含ULID字段 