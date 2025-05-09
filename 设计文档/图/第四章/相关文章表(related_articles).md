| 字段名 | 数据类型 | 长度 | 允许空 | 约束 | 说明 |
|--------|---------|------|--------|------|------|
| id | INT | - | 否 | PRIMARY KEY, AUTO_INCREMENT | 自增主键ID |
| article_id | INT | - | 否 | FOREIGN KEY | 源文章ID |
| related_url | VARCHAR | 1024 | 否 | - | 相关文章URL |
| title | VARCHAR | 255 | 是 | - | 相关文章标题 |
| created_at | TIMESTAMP | - | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |