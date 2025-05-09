相关文章表(related_articles)

| 字段名 | 数据类型 | 允许空 | 约束 | 说明 |
|--------|----------|--------|------|------|
| id | INT | 否 | 主键, 自动递增 | 自增主键ID |
| article_id | INT | 否 | 外键 | 源文章ID |
| related_url | VARCHAR | 否 | - | 相关文章URL |
| title | VARCHAR | 是 | - | 相关文章标题 |
| created_at | TIMESTAMP | 否 | 默认为当前时间 | 创建时间 |