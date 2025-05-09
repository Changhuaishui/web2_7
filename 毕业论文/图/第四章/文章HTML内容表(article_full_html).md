| 字段名 | 数据类型 | 长度 | 允许空 | 约束 | 说明 |
|--------|---------|------|--------|------|------|
| id | INT | - | 否 | PRIMARY KEY, AUTO_INCREMENT | 自增主键ID |
| article_id | INT | - | 否 | FOREIGN KEY | 关联article_table的ID |
| full_html | MEDIUMTEXT | - | 否 | - | 文章完整HTML内容 |
| url_mapping | MEDIUMTEXT | - | 是 | - | URL到本地路径的映射JSON |
| created_at | TIMESTAMP | - | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |