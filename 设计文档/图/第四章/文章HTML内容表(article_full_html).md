文章HTML内容表(article_full_html)

| 字段名 | 数据类型 | 允许空 | 约束 | 说明 |
|--------|----------|--------|------|------|
| id | INT | 否 | 主键, 自动递增 | 自增主键ID |
| article_id | INT | 否 | 外键 | 关联的文章ID |
| html_content | LONGTEXT | 否 | - | 完整的HTML内容 |
| created_at | TIMESTAMP | 否 | 默认为当前时间 | 创建时间 |
| updated_at | TIMESTAMP | 否 | 默认为当前时间，自动更新 | 更新时间 |