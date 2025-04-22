# 2025-04-05 工作日志 | 作者：chen

## 今日工作目标

1. **✅修复前端作者/公众号显示异常**
    - bug案例：[https://mp.weixin.qq.com/s/LPDETm1sUGlm6cRP1wM9Hg] 原作者爬虫失败（数据库数据一致，推测爬虫类逻辑问题）
    - 成功案例：[https://mp.weixin.qq.com/s/YTZrwg8_W9sLfSUGOMzkqg] 原作者爬虫成功（数据库数据一致，推测爬虫类逻辑问题）
    - 解决过程：在第一步检查meta_content区域时：
      首先检查是否有带"作者"前缀的文本
      如果没有，则取第一个非空且不是"原创"的span文本作为作者
      优化了选择器：
      使用:not(:contains(原创))排除包含"原创"的元素
      在选择器中直接过滤掉不需要的文本
      改进了文本匹配逻辑：
      不再只依赖"作者："或"作者:"的前缀
      增加了对特定文本（如"原创"和"卢克文工作室"）的排除
      直接使用第一个有效的非空文本作为作者名
2. **✅明确Lucene搜索功能**
    - 实现标题/文本/作者/公众号/原作者多字段索引
    - 前端搜索结果需与 `<el-card>` 组件一致（含标题跳转、阅读原文、逻辑删除）
    - 本次bug修改记录:
      - 问题：搜索结果排序出现unexpected docvalues type NONE for field 'publishTime'错误
      - 原因：publishTime字段缺少排序支持
      - 解决：添加SortedDocValuesField实现排序功能，并优化索引创建策略
      - 索引管理层：
        - LuceneIndexManager: 负责索引CRUD操作
        - ArticleSearchService: 搜索业务逻辑
        - ArticleSearchController: RESTful API接口
      - 多字段索引设计：
        - 标题 --分词索引
        - 文本 --分词索引
        - 作者 --分词索引
        - 公众号 --分词索引
        - 原文链接 --精确索引
        - 发布时间 --排序支持
      - 逻辑删除集成
        - 索引重建时删除文章内容
        - 搜索结果排除已删除内容
      - 前端组件：
        - 代码：

        ```vue
          <el-card v-for="article in searchResults">
        <!-- 标题跳转 -->
        <div @click="openArticle(article.url)">{{article.title}}</div>
        <!-- 阅读原文 -->
        <el-button @click="openSourceUrl(article.sourceUrl)">阅读原文</el-button>
        <!-- 逻辑删除 -->
        <el-button @click="deleteArticle(article.url)">删除</el-button>
        </el-card>
        ```

    - 后续建议：
      - 3. 当前功能
           多字段搜索
           支持标题、内容、作者、公众号多字段搜索
           实现按发布时间排序
           集成逻辑删除过滤
           搜索结果展示
           使用el-card组件展示
           支持标题跳转
           提供阅读原文链接
           实现逻辑删除功能
4. 后续建议
   搜索体验优化
   添加关键词高亮
   实现搜索建议
   支持高级过滤（时间范围、公众号筛选）
   性能优化
   实现索引增量更新
   添加搜索结果缓存
   优化大数据量索引性能
4. **导师要求实现功能**
    - 咱们抓取文章内容的时候，应该把文章的完整 HTML 也保存下来。这样你跳转到全文去阅读的时候，渲染就会有比较好的效果


## 一、已完成功能（代码更新）

### ✅ 1. 逻辑删除功能落地（核心改进）

| 模块         | 变更内容                                                                 | 代码位置                          |
|--------------|--------------------------------------------------------------------------|-----------------------------------|
| 数据库       | 新增 `article_table.is_deleted` 字段（默认`false`）                      | SQL建表语句                       |
| 实体类       | `Article.java` 新增 `private Boolean isDeleted`                          | model/Article.java                |
| Mapper       | 新增 `logicalDeleteByUrl` 逻辑删除方法，所有查询排除`is_deleted=1`       | dao/ArticleMapper.java            |
| 控制器       | `CrawlerController.delete()` 改为逻辑删除（调用`logicalDeleteByUrl`）    | controller/CrawlerController.java |
| Lucene索引   | `rebuildIndex()` 仅索引`isDeleted=false`的文章，删除时触发索引重建        | service/LuceneIndexService.java   |
| 前端         | 删除按钮仅触发逻辑删除，列表过滤已删除文章（通过接口`/api/crawler/articles`返回未删除数据） | frontend/src/views/Home.vue       |

### ✅ 2. 爬虫模块问题排查（部分进展）

- **发现现象**：部分文章作者字段为空，但数据库`author`与`account_name`值相同
- **代码验证**：  
  在`WeChatArticleSpider.extractAuthor()`添加日志：

  ```java
  // utils/WeChatArticleSpider.java
  private String extractAuthor(Document doc) {
      String author = doc.select("meta[property='og:article:author']").attr("content");
      System.out.println("[DEBUG] 作者提取结果：" + author + " | 公众号：" + accountName);
      return StringUtils.isBlank(author) ? accountName : author; // 临时兜底逻辑
  }
  ```

### ✅ 3. Lucene搜索实现总结

#### 核心组件结构

| 组件名称 | 职责 | 文件位置 |
| --- | --- | --- |
| LuceneIndexService | 主要索引服务，处理索引CRUD | service/LuceneIndexService.java |
| ArticleSearchService | 文章搜索服务，处理搜索业务逻辑 | service/ArticleSearchService.java |
| LuceneIndexManager | 索引管理工具，底层索引操作 | utils/LuceneIndexManager.java |
| ArticleSearchController | 搜索API接口 | controller/ArticleSearchController.java |

#### 技术栈

- Lucene 8.11.2
- IK Analyzer 8.5.0（中文分词）
- Spring Boot 集成

#### 索引字段设计

| 字段名 | 类型 | 说明 | 是否分词 |
| --- | --- | --- | --- |
| title | TextField | 文章标题 | 是 |
| content | TextField | 文章内容 | 是 |
| author | TextField | 作者 | 是 |
| accountName | TextField | 公众号名称 | 是 |
| url | StringField | 文章链接 | 否 |
| publishTime | StringField + SortedDocValuesField | 发布时间 | 否 |
| isDeleted | StringField | 删除标记 | 否 |

#### 功能实现

1. **索引管理**
   - 系统启动自动初始化索引
   - 支持单篇文章索引/更新
   - 支持批量索引操作
   - 支持索引删除
   - 支持全量索引重建

2. **搜索功能**
   - 多字段并发搜索（标题、内容、作者、公众号）
   - 支持 OR 逻辑操作符
   - 按发布时间排序
   - 支持分页（默认100条）
   - 已集成逻辑删除过滤

3. **性能优化**
   - 使用 SortedDocValuesField 优化排序
   - 合理使用 TextField/StringField 区分分词需求
   - 索引写入时使用 commit 保证数据一致性
   - 异常情况下自动重建索引

4. **错误处理**
   - 完整的异常处理机制
   - 索引异常自动重建
   - 事务支持保证数据一致性

#### API接口

```http
GET /api/search?keyword=关键词     # 搜索接口
DELETE /api/crawler/articles/{url} # 删除接口（同时删除索引）
```

#### 配置信息

```properties
# Lucene索引目录配置
lucene.index.directory=lucene/indexes
```

#### 后续优化建议

1. **搜索体验优化**
   - 添加关键词高亮显示
   - 实现搜索建议功能
   - 支持高级过滤（时间范围、公众号筛选）

2. **性能优化方向**
   - 实现索引增量更新
   - 添加搜索结果缓存
   - 优化大数据量索引性能
   - 考虑引入索引备份机制

3. **✅文章详情页数据获取bug修复**
   - 问题：文章详情页的作者、公众号、发布时间字段显示与主页面不一致
   - 原因：文章详情页和主页面使用了不同的SQL查询，导致字段映射不一致
   - 解决：
     - 在 `ArticleMapper` 中添加 `findById` 方法，使用与 `findAllOrderByPublishTime` 相同的字段映射
     - SQL查询保持一致：
       ```sql
       SELECT id, title, author, url, source_url AS sourceUrl, 
              account_name AS accountName, publish_time AS publishTime, 
              content, images, is_deleted AS isDeleted 
       FROM article_table 
       WHERE id = #{id} AND is_deleted = false
       ```
     - 修改 `ArticleController` 直接使用 `ArticleMapper` 进行数据访问
   - 效果：文章详情页的数据显示现在与主页面完全一致