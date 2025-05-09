# 微信公众号爬虫系统 - 数据库脚本说明

本目录包含微信公众号爬虫系统所需的数据库脚本。

## 脚本列表及执行顺序

### 初始化数据库（新环境）

1. `05_create_database.sql` - 创建数据库
2. `01_create_tables.sql` - 创建表结构（包含ULID字段）

### 迁移现有数据库（添加ULID支持）

- `02_alter_tables_add_ulid.sql` - 为现有表添加ULID字段

### 维护脚本

- `03_truncate_tables.sql` - 清空所有表数据（保留表结构）
- `04_drop_tables.sql` - 删除所有表（慎用！）

## 脚本详细说明

### 05_create_database.sql

创建`crawler_db`数据库，设置字符集为utf8mb4。如果数据库已存在，则不会重复创建。

### 01_create_tables.sql

创建系统所需的表结构：
- `article_table` - 存储文章基本信息
- `article_full_html` - 存储文章的完整HTML内容

这个脚本已经包含ULID字段，适用于新建数据库环境。

### 02_alter_tables_add_ulid.sql

为现有的`article_table`表添加ULID字段，用于支持基于ULID的图片命名方案。
此脚本解决了以下问题：
- 添加ULID字段
- 为现有记录生成临时ID
- 设置适当的列大小
- 添加唯一索引
- 设置非空约束

### 03_truncate_tables.sql

清空所有表的数据，但保留表结构。通常用于测试环境或需要重置数据时。
执行此脚本会：
- 关闭外键约束检查
- 清空article_full_html表
- 清空article_table表
- 重新启用外键约束
- 重置自增ID

### 04_drop_tables.sql

完全删除系统相关的数据表。慎用！
执行此脚本会：
- 关闭外键约束检查
- 删除article_full_html表
- 删除article_table表
- 重新启用外键约束

## 注意事项

1. 执行SQL脚本前请确保已备份重要数据
2. 不同的MySQL版本可能对某些语法有差异
3. 生产环境中请谨慎使用清空和删除表的脚本 