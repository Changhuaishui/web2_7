# 微信公众号爬虫系统数据库设计文档

## 数据库概述

本系统使用MySQL数据库存储微信公众号文章的内容、HTML、图片映射关系以及相关文章信息。使用UTF-8编码支持中文内容，并设计了合理的外键关系和索引以保证数据完整性和查询性能。

## 数据库结构

### 表关系图

```
+------------------+       +--------------------+       +------------------+
| article_table    |       | article_full_html  |       | related_articles |
+------------------+       +--------------------+       +------------------+
| id (PK)          |<----->| article_id (FK)    |       | id (PK)          |
| ulid             |       | full_html          |       | article_id (FK)  |
| title            |       | url_mapping        |       | related_url      |
| author           |       | created_at         |       | title            |
| url              |       +--------------------+       | created_at       |
| source_url       |                                    +------------------+
| account_name     |
| publish_time     |
| content          |
| images           |
| image_mappings   |
| summary          |
| keywords         |
| is_deleted       |
| created_at       |
| updated_at       |
+------------------+
```

### 表说明

1. **article_table**：存储文章的基本信息
   - 主键：`id`（自增整数）
   - 唯一键：`ulid`（用于前端URL路由）和`url`（微信原始URL）
   - 外键关系：作为主表被其他表引用
   - 主要字段：标题、作者、内容、发布时间、公众号名称等
   - 特殊字段：`image_mappings`（存储图片位置映射的JSON）、`summary`（文章摘要）、`keywords`（文章关键词）

2. **article_full_html**：存储文章的完整HTML内容
   - 主键：`id`（自增整数）
   - 外键：`article_id`（关联到article_table的id）
   - 主要字段：`full_html`（完整HTML内容）、`url_mapping`（存储URL映射的JSON）

3. **related_articles**：存储文章的相关文章信息
   - 主键：`id`（自增整数）
   - 外键：`article_id`（关联到article_table的id）
   - 主要字段：`related_url`（相关文章URL）、`title`（相关文章标题）
   - 唯一约束：`article_id`和`related_url`组合唯一，防止重复添加相同相关文章

## 优化后的数据库脚本

为了简化数据库操作，我们创建了一个统一的SQL脚本文件`optimized_schema.sql`，包含了以下几个部分：

### 1. 创建数据库和表

脚本的第一部分和第二部分用于创建数据库和所有必要的表结构，包括：
- 创建`crawler_db`数据库
- 创建三个主要表：`article_table`、`article_full_html`和`related_articles`
- 设置正确的字段类型、默认值和注释
- 创建必要的索引提高查询性能

### 2. 常用查询SQL

脚本的第三部分提供了一系列常用的查询SQL语句，方便进行数据库操作：
- 查询数据库表结构信息
- 查询文章列表和详情
- 查询相关文章
- 根据关键词搜索文章
- 查看数据库统计信息

### 3. 表数据管理

脚本的第四部分和第五部分提供了清空和删除表的操作：
- 按照正确的顺序（考虑外键约束）清空表数据
- 按照正确的顺序删除表结构
- 提供了相关警告，防止误操作

### 4. 维护脚本

脚本的第六部分提供了一些有用的维护功能：
- 物理删除已标记为逻辑删除的文章
- 批量更新公众号文章信息
- 检查数据完整性（查找孤立记录和缺失记录）

## 使用指南

### 初始化数据库

1. 登录到MySQL：
   ```bash
   mysql -u username -p
   ```

2. 执行完整脚本创建数据库和表：
   ```sql
   SOURCE /path/to/optimized_schema.sql
   ```
   
   或者选择性地复制脚本的特定部分执行。

### 查询数据

使用脚本中的查询SQL，根据需要修改参数：

```sql
-- 查询所有文章
USE crawler_db;
SELECT id, ulid, title, author, account_name, publish_time
FROM article_table 
WHERE is_deleted = false
ORDER BY publish_time DESC;

-- 查询特定文章详情（替换1为实际文章ID）
SELECT a.id, a.title, a.author, a.content, h.full_html
FROM article_table a
LEFT JOIN article_full_html h ON a.id = h.article_id
WHERE a.id = 1;

-- 搜索特定关键词的文章
SELECT id, title, author, publish_time
FROM article_table
WHERE title LIKE '%关键词%' AND is_deleted = false;
```

### 清空数据（谨慎使用）

在测试环境中，可以使用以下命令清空表数据：

```sql
USE crawler_db;

-- 按正确顺序清空表
TRUNCATE TABLE related_articles;
TRUNCATE TABLE article_full_html;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE article_table;
SET FOREIGN_KEY_CHECKS = 1;
```

### 数据维护

定期检查数据完整性：

```sql
-- 检查孤立记录
SELECT h.* 
FROM article_full_html h
LEFT JOIN article_table a ON h.article_id = a.id
WHERE a.id IS NULL;

-- 检查缺失HTML内容的文章
SELECT a.id, a.title
FROM article_table a
LEFT JOIN article_full_html h ON a.id = h.article_id
WHERE h.id IS NULL AND a.is_deleted = false;
```

## 表详细结构

### article_table
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | INT | 自增主键 |
| ulid | VARCHAR(26) | ULID唯一标识（用于URL） |
| title | VARCHAR(255) | 文章标题 |
| author | VARCHAR(100) | 文章作者 |
| url | VARCHAR(1024) | 文章原始URL |
| source_url | VARCHAR(1024) | 文章来源URL |
| account_name | VARCHAR(100) | 公众号名称 |
| publish_time | DATETIME | 发布时间 |
| content | MEDIUMTEXT | 文章文本内容 |
| images | TEXT | 图片路径列表，以逗号分隔 |
| image_mappings | MEDIUMTEXT | 图片位置映射JSON信息 |
| summary | TEXT | 文章摘要内容 |
| keywords | VARCHAR(255) | 文章关键词，逗号分隔 |
| is_deleted | BOOLEAN | 是否逻辑删除 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### article_full_html
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | INT | 自增主键 |
| article_id | INT | 关联到article_table的ID |
| full_html | MEDIUMTEXT | 文章完整HTML内容 |
| url_mapping | MEDIUMTEXT | 存储原始URL到本地路径的映射关系JSON |
| created_at | TIMESTAMP | 创建时间 |

### related_articles
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | INT | 自增主键 |
| article_id | INT | 源文章ID |
| related_url | VARCHAR(1024) | 相关文章URL |
| title | VARCHAR(255) | 相关文章标题 |
| created_at | TIMESTAMP | 创建时间 |

## 注意事项

1. 执行删除或清空表操作前，请确保已备份重要数据
2. 所有外键关系使用CASCADE删除规则，删除文章会自动删除相关的HTML内容和相关文章记录
3. 本系统中的标签处理功能是通过Java代码中的`TagService`动态计算实现的，不依赖数据库表存储标签信息 