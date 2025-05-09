| 接口路径                      | 方法   | 说明                         |
|:------------------------------|:-------|:-----------------------------|
| /api/crawler/submit           | POST   | 提交文章URL爬取任务           |
| /api/crawler/list             | GET    | 获取已爬取文章列表            |
| /api/crawler/status/{taskId}  | GET    | 检查爬虫任务或链接状态         |
| /api/articles/{ulid}          | GET    | 获取指定ULID的文章详情         |
| /api/articles/html/{ulid}     | GET    | 获取文章HTML内容               |
| /api/articles                 | POST   | 新增文章                      |
| /api/articles/{ulid}          | DELETE | 删除文章                      |
| /api/ai/summary               | POST   | 生成文章摘要                   |
| /api/ai/keywords              | POST   | 提取关键词                     |
| /api/ai/related               | POST   | 推荐相关文章                   |
| /api/search                   | GET    | 文章全文检索                   |
| /api/search/rebuild-index     | POST   | 触发索引重建                   |
| /api/tags                     | GET    | 获取标签列表                   |
| /api/tags/refresh             | POST   | 刷新标签缓存                   |
| /api/tags/articles/{tag}      | GET    | 获取某标签下的文章列表          |
| /api/images/{imageId}         | GET    | 获取图片资源                   |
| /api/images/upload            | POST   | 上传图片                       |

---

**系统最关键RESTful接口一览**

| 接口路径                  | 方法   | 说明                         |
|:--------------------------|:-------|:-----------------------------|
| /api/search               | GET    | 文章全文检索，核心内容入口     |
| /api/articles/{ulid}      | GET    | 获取指定ULID的文章详情         |
| /api/crawler/submit       | POST   | 提交文章URL爬取任务           |
| /api/ai/summary           | POST   | 生成文章摘要，AI增强亮点       |
| /api/tags                 | GET    | 获取标签列表，支撑分类与推荐   |
| /api/images/{imageId}     | GET    | 获取图片资源，保证内容完整性   | 