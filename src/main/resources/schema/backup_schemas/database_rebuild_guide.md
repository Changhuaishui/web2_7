# 数据库重建指南

本指南将帮助你重建并正确配置爬虫系统的数据库。

## 1. 创建数据库

首先，您需要创建数据库，可以使用MySQL命令行或图形化工具（如MySQL Workbench）。

```sql
CREATE DATABASE IF NOT EXISTS crawler_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crawler_db;
```

## 2. 执行重建表脚本

接下来，执行重建表脚本。这个脚本会删除现有表（如果存在），然后创建新表（包含所有必要字段）。

### 使用命令行执行:

```bash
# 进入MySQL命令行
mysql -u 用户名 -p

# 选择数据库
USE crawler_db;

# 执行脚本
SOURCE /path/to/recreate_tables.sql;
```

### 或者使用MySQL Workbench:
1. 打开MySQL Workbench
2. 连接到您的数据库服务器
3. 选择crawler_db数据库
4. 打开recreate_tables.sql文件
5. 执行脚本

## 3. 验证表结构

执行完脚本后，应该会看到验证结果，确保所有字段都正确创建。特别注意确认`ulid`列已正确创建，因为这是错误的根源。

可以手动验证：

```sql
-- 检查article_table表结构
DESCRIBE article_table;

-- 特别检查ulid字段是否存在
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'crawler_db' 
  AND TABLE_NAME = 'article_table'
  AND COLUMN_NAME = 'ulid';
```

## 4. 更新数据库连接配置

确保项目的`application.properties`文件中有正确的数据库连接配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crawler_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

## 5. 重启应用

重建数据库后，重启应用以确保所有更改生效。

## 故障排除

如果再次遇到"Unknown column 'ulid' in 'field list'"错误，请检查：

1. 确认执行脚本时没有错误
2. 检查MySQL用户是否有足够的权限
3. 检查数据库名称是否正确（默认是crawler_db）
4. 尝试直接在MySQL中手动添加列：
   ```sql
   ALTER TABLE article_table ADD COLUMN ulid VARCHAR(26) NOT NULL COMMENT 'ULID唯一标识' AFTER id;
   CREATE UNIQUE INDEX idx_article_ulid ON article_table(ulid);
   ```

## 常见问题

### Q: 为什么会发生"Unknown column 'ulid' in 'field list'"错误？
A: 这是因为查询中包含了`ulid`字段，但该字段在数据库表中不存在。通常是由于创建表时未包含此字段，或者使用了旧版本的表结构。

### Q: 重建数据库会丢失现有数据吗？
A: 是的，执行重建脚本会删除现有表并创建新表，所有数据都会丢失。如果您需要保留数据，请在执行前进行备份。

### Q: 我用的是H2/PostgreSQL/其他数据库，这个脚本适用吗？
A: 此脚本专为MySQL设计。如果您使用其他数据库，可能需要调整语法。 