-- 添加ULID字段到article_table表中
ALTER TABLE article_table ADD COLUMN ulid VARCHAR(32) AFTER id;

-- 为现有记录生成临时ID
UPDATE article_table SET ulid = LEFT(REPLACE(UUID(), '-', ''), 26) WHERE ulid IS NULL;

-- 将列大小改为26（ULID大小）
ALTER TABLE article_table MODIFY COLUMN ulid VARCHAR(26);

-- 创建唯一索引
ALTER TABLE article_table ADD UNIQUE INDEX idx_article_ulid (ulid);

-- 添加非空约束和注释
ALTER TABLE article_table MODIFY COLUMN ulid VARCHAR(26) NOT NULL COMMENT 'ULID，用于唯一标识文章';