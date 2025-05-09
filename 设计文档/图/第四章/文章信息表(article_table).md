文章信息表(article_table)

| 字段名         | 数据类型   | 允许空 | 约束                      | 说明                   |
| -------------- | ---------- | ------ | ------------------------- | ---------------------- |
| id             | INT        | 否     | 主键, 自动递增           | 自增主键ID             |
| ulid           | VARCHAR    | 否     | 唯一                     | 全局唯一标识符         |
| title          | VARCHAR    | 否     | -                        | 文章标题               |
| author         | VARCHAR    | 是     | -                        | 文章作者               |
| url            | VARCHAR    | 否     | 唯一                     | 文章原始URL            |
| source_url     | VARCHAR    | 是     | -                        | 文章来源URL            |
| account_name   | VARCHAR    | 是     | -                        | 公众号名称             |
| publish_time   | DATETIME   | 是     | -                        | 发布时间               |
| content        | MEDIUMTEXT | 否     | -                        | 文章文本内容           |
| images         | TEXT       | 是     | -                        | 图片路径列表，逗号分隔 |
| image_mappings | MEDIUMTEXT | 是     | -                        | 图片位置映射JSON信息   |
| summary        | TEXT       | 是     | -                        | 文章摘要内容           |
| keywords       | VARCHAR    | 是     | -                        | 文章关键词，逗号分隔   |
| is_deleted     | BOOLEAN    | 否     | 默认值为假               | 逻辑删除标记           |
| created_at     | TIMESTAMP  | 否     | 默认为当前时间           | 创建时间               |
| updated_at     | TIMESTAMP  | 否     | 默认为当前时间，自动更新 | 更新时间               |
