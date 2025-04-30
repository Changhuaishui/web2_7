# 微信公众号文章爬虫系统 - 开发日志

## 项目开发周期
2025-03-27 至今

## 开发日志

### 阶段一：项目初始化与基础架构搭建
#### 2025-03-15
1. 项目初始化
   - 创建 Spring Boot 项目
   - 配置 Maven 依赖
   - 设置基本项目结构

2. 数据库设计
   - 创建 MySQL 数据库
   - 设计文章表结构
   - 配置 MyBatis

3. 前端项目搭建
   - 创建 Vue.js 项目
   - 集成 Element Plus
   - 配置开发环境

### 阶段二：核心功能开发
#### 2025-03-20
1. 爬虫功能实现
   - 集成 WebMagic 框架
   - 实现文章内容抓取
   - 开发图片下载功能

2. 搜索功能开发
   - 集成 Lucene 搜索引擎
   - 实现文章索引创建
   - 开发搜索接口

3. 前端界面开发
   - 实现文章列表展示
   - 开发搜索组件
   - 添加文章管理功能

### 阶段三：系统优化与问题修复
#### 2025-03-27
1. 性能优化
   - 优化数据库查询
   - 实现缓存机制
   - 改进搜索性能

2. 问题修复
   - 解决 CORS 跨域问题
   - 修复日期解析错误
   - 完善错误处理

## 技术难点解决方案

### 1. 爬虫实现
#### 问题描述
- 需要准确提取微信公众号文章内容
- 处理文章发布时间格式多样性
- 管理图片下载和存储

#### 解决方案
1. 使用 WebMagic 框架
   - 自定义页面处理器
   - 实现智能解析规则
   - 处理异常情况

2. 日期解析优化
   - 使用正则表达式匹配多种格式
   - 统一转换为标准格式
   - 添加格式验证

3. 图片处理
   - 异步下载图片
   - 本地化存储
   - 建立索引关联

### 2. 搜索功能
#### 问题描述
- 需要支持全文搜索
- 要求搜索结果准确性
- 性能需要优化

#### 解决方案
1. Lucene 索引设计
   - 合理设计索引字段
   - 优化索引策略
   - 实现实时更新

2. 搜索优化
   - 实现多字段搜索
   - 添加结果评分
   - 支持高级筛选

### 3. 前后端交互
#### 问题描述
- 跨域访问限制
- 数据传输格式
- 异步操作处理

#### 解决方案
1. CORS 配置
   - 统一配置跨域策略
   - 规范请求头设置
   - 处理预检请求

2. 数据交互
   - 统一响应格式
   - 规范错误处理
   - 实现数据验证

## 性能优化记录

### 1. 数据库优化
1. 索引优化
   - 添加合适的索引
   - 优化查询语句
   - 定期维护索引

2. 连接池配置
   - 调整连接池大小
   - 优化超时设置
   - 监控连接状态

### 2. 搜索优化
1. 索引优化
   - 定期重建索引
   - 优化索引结构
   - 实现增量索引

2. 查询优化
   - 实现缓存机制
   - 优化查询逻辑
   - 提高响应速度

### 3. 前端优化
1. 页面性能
   - 组件懒加载
   - 资源压缩
   - 缓存策略

2. 用户体验
   - 添加加载提示
   - 优化交互流程
   - 提升响应速度

## 待优化项目

### 功能优化
1. 爬虫功能
   - [ ] 支持批量爬取
   - [ ] 添加定时任务
   - [ ] 优化错误重试

2. 搜索功能
   - [ ] 添加搜索建议
   - [ ] 支持高级过滤
   - [ ] 优化排序算法

3. 用户界面
   - [ ] 优化移动端适配
   - [ ] 添加数据可视化
   - [ ] 完善操作反馈

### 性能优化
1. 后端优化
   - [ ] 添加缓存层
   - [ ] 优化数据库查询
   - [ ] 实现分布式部署

2. 前端优化
   - [ ] 实现虚拟滚动
   - [ ] 优化资源加载
   - [ ] 改进状态管理

## 项目维护注意事项

### 1. 代码维护
- 遵循代码规范
- 及时更新文档
- 定期代码审查

### 2. 运行维护
- 定期备份数据
- 监控系统状态
- 及时处理异常

### 3. 性能维护
- 定期优化数据库
- 监控系统性能
- 及时处理性能问题

## 联系方式
- 后端开发：[chen]
- 前端开发：[chen]
- 技术支持：[2021011100@bistu.edu.cn]

## 2025-04-29 - DeepSeek模型集成用于文章摘要生成

### 主要更新内容
1. 集成DeepSeek API用于文章摘要生成
2. 文章表增加summary字段存储摘要
3. 前端实现摘要显示和手动触发功能
4. 优化API调用逻辑，避免重复调用

### 开发过程

#### 1. 初始环境配置
- 在`application.properties`中添加DeepSeek API配置
- 创建`DeepSeekService`接口和实现类，提供摘要生成功能

#### 2. 数据库表结构更新
- 为`article_table`表添加`summary`字段存储摘要
- 创建`07_add_summary_column.sql`脚本，用于给已有表结构添加字段
- 创建`06_clear_table_data.sql`脚本，提供清空表数据但保留表结构功能
- 修复`01_create_tables.sql`中缺少表名的BUG

#### 3. 后端功能实现
- 实现`DeepSeekServiceImpl`，调用DeepSeek API生成摘要
- 更新`ArticleMapper`接口，添加摘要更新方法
- 修改`DatabasePipeline`，爬取文章后自动生成摘要
- 创建`SummaryController`，提供手动生成摘要API

#### 4. 前端功能实现
- 修改`ArticleDetail.vue`，添加摘要显示区域
- 实现"生成摘要"按钮及相关逻辑
- 优化前端交互提示，区分已有摘要和新生成摘要

### 遇到的问题及解决方案

#### BUG1: 模型名称错误
- **问题**：DeepSeekServiceImpl中使用的模型名称"deepseek-v3"不存在
- **解决**：通过查询DeepSeek API文档，确认正确的模型名称是"deepseek-chat"

#### BUG2: ChatController引用错误
- **问题**：`ChatController`尝试调用不存在的`generateChatResponse`方法
- **解决**：在`DeepSeekService`接口和实现类中添加该方法

#### BUG3: 数据库表结构错误
- **问题**：`article_full_html`表创建语句缺少表名
- **解决**：修复SQL语句，正确添加表名

#### BUG4: 重复调用API问题
- **问题**：即使文章已有摘要，点击生成摘要按钮仍会调用API
- **解决**：修改`SummaryController`，先检查数据库中是否已有摘要，如有则直接返回

### 开发心得
1. 集成第三方API时，务必仔细查阅API文档，特别是模型名称等关键参数
2. 数据库脚本应当更加严谨，SQL语句要完整
3. 在涉及付费API调用时，应当尽量避免重复调用，节约资源
4. 前后端交互中，适当的错误处理和提示信息很重要

## 4月21日更新 - 爬虫图片下载功能优化

### 功能更新
1. 优化了爬虫的图片下载逻辑
2. 现在为每个爬取的文章创建单独的文件夹，文件夹名为公众号文章标题
3. 图片命名格式改为"公众号标题_序号.jpg"
4. 优化了CrawlerService的日志处理和异常处理

### 技术细节
- 修改了`WeChatArticleSpider.java`中的图片下载逻辑
- 图片现在保存在以文章标题命名的子文件夹中
- 处理了文件名中可能存在的非法字符
- 增强了爬虫服务的日志记录

### 后续优化方向
- 考虑添加图片去重功能
- 考虑添加图片压缩功能
- 改进文章标题提取以获得更准确的文件夹命名

## 4月22日更新 - 添加文章头图爬取功能

### 功能更新
1. 增加了爬取微信公众号文章头图的功能
2. 头图以"公众号标题_head.jpg"格式命名并保存
3. 在控制台输出头图链接，方便调试和跟踪
4. 将头图URL添加到数据库存储中

### 技术细节
- 新增`extractHeadImage`方法，从多个来源提取头图URL：
  - 首选从meta标签`og:image`获取
  - 尝试其他常见的头图相关meta标签
  - 如无法找到meta标签，则使用文章中第一张图片作为头图
- 优化了图片下载逻辑，支持头图下载
- 在数据库管道中添加头图处理逻辑
- 完善了错误处理和日志输出

### 后续优化方向
- 增加头图处理（如缩放、压缩）功能
- 优化头图提取的准确性和速度
- 提供头图预览功能

## 4月23日更新 - 前端显示文章头图功能

### 功能更新
1. 添加了在文章列表瀑布流中显示头图的功能
2. 实现了在文章详情页中显示大图版头图
3. 添加了后端API接口用于提供头图访问服务
4. 优化了用户体验，使界面更加美观和信息丰富

### 技术细节
- 新增后端接口`/api/crawler/image/{articleId}`用于获取文章头图URL
- 新增后端接口`/api/crawler/images/{folder}/{filename:.+}`用于直接提供图片文件访问
- 前端实现文章头图动态加载和显示
- 添加了加载失败时的静默处理，确保即使头图不存在也不会影响界面
- 优化了头图样式，添加了悬停效果和合适的尺寸约束

### 后续优化方向
- 考虑实现头图的懒加载，提高页面加载性能
- 添加图片缺失时的默认占位图
- 优化图片加载速度，可考虑添加图片缓存机制

## 4月24日更新 - 标签化推荐文章功能

### 功能更新
1. 实现了基于内容的文章标签提取功能
2. 添加了标签筛选文章功能
3. 在文章列表页面提供了标签选择界面
4. 在文章详情页中显示文章标签

### 技术细节
- 创建了`TagService`服务类，负责提取和管理文章标签
- 添加了预定义的标签分类（科技、AI、金融等）
- 基于内容关键词匹配实现了简单的标签提取算法
- 新增后端接口`/api/tags`用于获取所有可用标签
- 新增后端接口`/api/tags/article/{id}`用于获取特定文章的标签
- 新增后端接口`/api/tags/filter`用于根据标签筛选文章
- 前端实现了标签选择和筛选功能，通过点击标签可快速筛选相关文章

### 后续优化方向
- 实现更复杂的基于TF-IDF的标签提取算法
- 添加用户自定义标签功能
- 实现标签权重排序，提高推荐准确性
- 基于标签实现相似文章推荐功能
- 添加标签云组件，直观展示热门标签

## 4月25日更新 - 搜索功能修复与性能优化

### 功能更新
1. 修复了标签功能导致的搜索索引问题
2. 添加了重建索引API
3. 在前端添加了重建索引按钮
4. 优化了爬虫保存文章后的索引更新逻辑

### 技术细节
- 修改了`DatabasePipeline`类，确保新爬取的文章同时更新两个搜索索引
- 添加了`/api/search/rebuild-index`接口，支持手动重建所有文章的索引
- 在前端搜索表单旁添加了重建索引按钮，方便用户操作
- 完善了日志记录，便于追踪索引更新过程
- 通过两种索引服务的协同工作，确保搜索功能稳定性

### TF-IDF降序排序实现
- 使用Lucene的`SortField`和`Sort`实现搜索结果排序
- 在`ArticleSearchService`中添加了排序代码：
  ```java
  Sort sort = new Sort(new SortField("publishTime", SortField.Type.STRING, true));
  TopDocs topDocs = searcher.search(query, 100, sort);
  ```
- 当前排序是基于发布时间的降序，未来可以扩展为TF-IDF值排序

### 后续优化方向
- 完善TF-IDF计算逻辑，存储为独立字段用于排序
- 优化索引更新性能，考虑异步更新机制
- 添加索引状态监控功能
- 实现更复杂的搜索功能，如关键词高亮、相关度排序等
- 考虑分词优化，提高中文搜索准确性

## 2025-04-22: 修复搜索功能无响应问题

### 问题描述
前端搜索功能发送请求后，后端成功返回数据（日志显示"搜索结果数量: 2"），但页面未渲染搜索结果。用户在搜索框中输入关键词并点击搜索后，页面没有任何变化。

### 问题定位过程
1. **网络请求验证**：
   - 使用浏览器开发者工具检查网络请求
   - 确认请求URL: `http://localhost:5173/api/search?keyword=ai`
   - 请求状态码: 200 OK
   - 响应正确包含搜索结果数据

2. **后端日志分析**：
   ```
   2025-04-22T10:27:40.263+08:00  INFO 21232 --- [web2_7] [nio-8081-exec-8] o.e.w.c.ArticleSearchController          : 搜索文章，关键词: ai
   2025-04-22T10:27:40.267+08:00  INFO 21232 --- [web2_7] [nio-8081-exec-8] o.e.w.c.ArticleSearchController          : 搜索结果数量: 2
   ```
   确认后端正确处理了搜索请求并找到2条匹配结果

3. **前端代码审查**：
   - 检查 `Home.vue` 中的搜索函数实现
   - 发现在 `searchArticles` 函数中，搜索结果只更新到了 `articles` 数组，但未更新到页面实际渲染使用的 `filteredArticles` 数组
   ```javascript
   const searchArticles = async () => {
     try {
       // ...省略部分代码
       const response = await axios.get(`/api/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`);
       articles.value = response.data;
       // 这里缺少对 filteredArticles 的更新
       // ...省略部分代码
     } catch (error) {
       // ...省略错误处理代码
     }
   }
   ```

4. **问题根源**：
   经过分析代码结构，发现页面使用的是两层数据模型：
   - `articles`: 存储所有文章数据
   - `filteredArticles`: 用于实际页面渲染，支持标签筛选功能
   
   在搜索时只更新了`articles`而未同步更新`filteredArticles`，导致页面显示的内容没有变化

### 解决方案
在`searchArticles`函数中添加对`filteredArticles`的更新：
```javascript
const searchArticles = async () => {
  try {
    if (!searchKeyword.value.trim()) {
      await loadArticles();
      return;
    }
    const response = await axios.get(`/api/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`);
    console.log('搜索结果响应:', response);
    console.log('搜索结果数据:', response.data);
    articles.value = response.data;
    filteredArticles.value = response.data; // 添加此行，确保搜索结果也更新到filteredArticles
    if (articles.value.length === 0) {
      ElMessage.info('未找到匹配的文章');
    } else {
      console.log(`找到${articles.value.length}条搜索结果`);
    }
  } catch (error) {
    console.error('搜索失败:', error);
    ElMessage.error('搜索失败：' + (error.response?.data || error.message));
  }
}
```

### 修复效果
1. 搜索关键词时，结果现在同时更新到两个数组
2. 页面能正确显示搜索结果
3. 添加了额外调试日志以便于确认数据流向

### 经验总结
1. **数据流向一致性**：在Vue等响应式框架中，当有多个相关联的响应式数据源时，需确保所有用于渲染的数据都被正确更新
2. **调试日志的重要性**：添加关键点的日志输出有助于快速定位问题
3. **前后端联合调试**：结合后端日志和前端网络请求分析，能够更全面地了解数据流向

### 后续优化建议
1. 考虑重构数据模型，使用单一数据源或明确的数据流向
2. 为搜索功能添加加载状态指示
3. 考虑添加单元测试，防止类似问题再次出现

## 4月22日更新 - 图片处理系统功能分析与调优

### 系统架构分析

#### 1. 存储结构
- 图片物理存储在`image`目录下
- 采用ULID格式组织：`{articleUlid}/{imageUlid}.jpg`
- 数据库中`Article`表的`images`字段存储图片路径列表，以逗号分隔

#### 2. API接口设计
- `/api/images/{articleUlid}/{imageUlid}` - 通过文章ULID和图片ULID获取图片资源（新版）
- `/api/crawler/image/{articleId}` - 通过文章ID获取头图URL（旧版）
- `/api/{ulid}/images` - 获取指定文章的所有图片信息
- `/api/images` - 获取所有文章的图片信息

#### 3. 控制器实现
`ImageController`负责处理新版ULID格式的图片请求：
- 验证文章存在性
- 构建图片资源路径
- 返回图片资源或错误状态

`CrawlerController`中包含处理旧版图片请求的方法：
- 支持基于文章标题的旧格式图片路径
- 提供向下兼容的API端点

### 前端实现

#### 1. 文章列表图片加载 (`Home.vue`)
- `loadArticleImage`方法为每篇文章加载头图
- 兼容新旧两种图片格式
- 使用响应式对象`articleImages`存储文章头图URL

#### 2. 文章详情页图片处理 (`ArticleDetail.vue`)
- `loadHeadImage`方法负责加载文章头图
- 同样兼容新旧两种格式
- 使用`v-if="headImage"`条件渲染确保图片加载成功才显示

### 系统兼容性设计

系统设计实现了双重兼容机制：

1. **旧格式**：基于文章标题构建图片路径
   - 图片命名：`{安全化标题}_head.jpg`
   - API路径：`/api/crawler/images/{安全化标题}/{头图文件名}`

2. **新格式**：基于ULID标识符
   - 更高效的查询
   - 更健壮的文件组织结构
   - 支持多张图片管理

### 图片加载流程

1. 前端加载文章列表/详情
2. 对每篇文章调用图片加载方法
3. 检查文章是否有ULID
4. 如有ULID，解析`images`字段获取图片路径
5. 构建新版API路径(`/api/images/{articleUlid}/{imageUlid}`)
6. 如无ULID，回退使用旧版API
7. 将结果存储在`articleImages`对象中，用于页面渲染

### 调试与发现的问题

1. **兼容性问题**
   - 系统中存在部分没有ULID的旧数据
   - 前端需要处理两套图片获取逻辑，增加了复杂度

2. **图片加载错误处理**
   - 前端已实现静默处理图片加载失败的情况
   - 加载失败时不显示图片，但不影响文章其他信息的显示

3. **路径解析逻辑**
   - 图片路径格式多样，需要解析逻辑来匹配不同格式
   - 当前使用`includes('/')`判断是否为新格式路径

### 优化建议

1. **数据迁移**
   - 考虑对旧数据进行统一迁移，为所有文章和图片分配ULID
   - 使系统统一使用新格式，减少维护两套逻辑的负担

2. **错误监控**
   - 增加图片加载错误的监控和统计
   - 便于发现和解决系统中的图片问题

3. **前端缓存**
   - 考虑在前端增加图片URL的缓存机制
   - 减少重复API调用，提高页面加载速度

4. **统一接口**
   - 考虑设计统一的图片接口，在后端处理新旧格式转换
   - 简化前端调用逻辑

## 2025-04-28 开发日志

### 功能增强：失效链接检测

今天实现了微信公众号文章爬取时的失效链接检测功能，优化了用户体验。微信公众号文章在原作者删除后会显示"临时链接已失效"，之前系统无法识别这种情况，现在已增加专门的检测和处理逻辑。

#### 代码更新内容

1. **爬虫失效链接检测**
   - 修改 `WeChatArticleSpider.java`，添加对微信失效链接的识别逻辑：
     - 检测页面中是否包含 `<div class="weui-msg__title warn">临时链接已失效</div>` 内容
     - 当识别到失效链接时，标记页面并跳过后续爬取处理

2. **失效链接处理管道**
   - 在 `CrawlerServiceImpl.java` 中添加 `InvalidLinkPipeline` 来处理失效链接：
     - 将失效链接放在数据库管道前处理，避免对失效链接进行数据库操作

3. **链接有效性验证**
   - 新增 `checkLinkStatus` 方法在 `CrawlerService` 接口和实现类中
   - 该方法允许在爬取前检查链接是否有效

4. **REST API 更新**
   - 在 `CrawlerController` 中添加 `/api/crawler/check-link` 接口
   - 该接口返回链接状态信息，包括链接是否有效、是否已被爬取等

5. **前端优化**
   - 修改 `Home.vue` 中的爬取逻辑，在爬取前先检查链接状态
   - 对不同状态（有效、无效、已爬取）展示不同的用户提示

### Bug修复：数据库插入HTML内容错误

修复了在插入文章HTML内容时的 MyBatis 错误。错误原因是在 `ArticleMapper.java` 中 `insertArticleHtml` 方法的注解配置有误：

```java
// 错误的配置
@Insert("INSERT INTO article_full_html (article_id, full_html) VALUES (#{articleId}, #{fullHtml})")
@Options(useGeneratedKeys = true, keyProperty = "articleId")
int insertArticleHtml(@Param("articleId") Integer articleId, @Param("fullHtml") String fullHtml);
```

错误原因：在 `@Options` 注解中，`keyProperty = "articleId"` 设置导致 MyBatis 尝试将自动生成的主键回写到 `articleId` 参数中，但 `articleId` 是作为外键传入的参数，而不是需要获取自动生成主键的字段。

修复方法：移除不必要的 `@Options` 注解，因为我们不需要获取自动生成的主键：

```java
// 修复后的代码
@Insert("INSERT INTO article_full_html (article_id, full_html) VALUES (#{articleId}, #{fullHtml})")
int insertArticleHtml(@Param("articleId") Integer articleId, @Param("fullHtml") String fullHtml);
```

### 测试验证

1. 测试有效链接爬取 - 通过
2. 测试失效链接检测 - 通过
3. 测试HTML内容存储 - 通过

### 后续计划

1. 考虑对失效链接进行记录存储，方便后续分析
2. 优化爬虫性能，提高爬取速度
3. 增加更多的异常处理逻辑，提高系统稳定性

## 2025-04-30 - 修复微信公众号文章中图片显示问题

### 功能更新内容
1. 修复数据库结构问题，添加缺失的`ulid`和`image_mappings`字段
2. 优化爬虫中的图片处理逻辑，确保占位符ID与下载的图片文件名一致
3. 增强`ImageController`的图片查找能力，改进图片请求路径处理
4. 创建完整的数据库重建脚本`recreate_tables.sql`，统一表结构

### 问题分析

#### 数据库结构问题
- 数据库表中缺少了必要的`ulid`和`image_mappings`字段，导致启动应用时出现"Unknown column 'ulid' in 'field list'"错误
- SQL语句同时存在于代码和数据库脚本中，导致不一致性
- 数据库中表结构不完整，缺少外键约束和索引

#### 图片处理逻辑问题
- `WeChatArticleSpider.java`中图片处理逻辑不一致，占位符中的图片ID与下载的图片文件名不匹配
- 当下载图片和生成占位符时使用不同的ID，导致无法正确关联和显示图片
- 图片路径信息存储不完整，前端无法准确请求到正确的图片路径

#### 图片请求路径问题
- 前端请求图片的路径与后端处理路径不匹配
- `ImageController`的图片查找逻辑不够健壮，无法处理多种图片请求格式
- 缺少适当的错误处理和回退机制，当图片不存在时没有合适的替代方案

### 修复过程

#### 1. 数据库结构修复
1. 创建完整的数据库重建脚本`recreate_tables.sql`，确保包含所有必要字段：
   - 添加`ulid`字段存储文章唯一标识符
   - 添加`image_mappings`字段存储图片位置映射
   - 为`article_full_html`表添加`url_mapping`字段存储URL映射关系
   - 添加必要的索引和约束，提高查询性能

2. 修改`ArticleMapper.java`中的SQL语句，确保与数据库结构一致：
   ```java
   @Insert("INSERT INTO article_table (ulid, title, author, url, source_url, account_name, publish_time, content, images, image_mappings, is_deleted) " +
           "VALUES (#{ulid}, #{title}, #{author}, #{url}, #{sourceUrl}, #{accountName}, #{publishTime}, #{content}, #{images}, #{imageMappings}, #{isDeleted})")
   ```

#### 2. 图片处理逻辑修复
1. 修改`WeChatArticleSpider.java`中的图片处理流程：
   - 为每张图片生成唯一的ULID标识符
   - 确保占位符ID与实际下载的图片文件名一致
   - 完善图片URL到本地路径的映射关系存储
   ```java
   // 使用之前生成的ULID，确保与占位符一致
   String imageId = imageUrlToUlidMap.get(imageUrl);
   if (imageId == null) {
       // 生成新ID
       imageId = UlidUtils.generate();
   }
   ```

2. 优化图片文件名和存储结构：
   - 使用`articleUlid`创建图片子文件夹
   - 使用`imageId`作为图片文件名，保证唯一性
   - 添加适当的文件扩展名处理

#### 3. 图片控制器增强
1. 增强`ImageController`的图片查找能力：
   - 优先使用精确路径查找图片
   - 添加多层回退策略，包括按ID查找、按索引查找和使用替代图片
   - 改进错误处理，提供更明确的日志信息
   ```java
   // 首先尝试精确查找指定的图片
   File exactImageFile = Paths.get(IMAGE_ROOT_DIR, articleUlid, imageUlid + extension).toFile();
   
   // 如果精确匹配的图片不存在，则尝试查找类似名称的图片
   if (!exactImageFile.exists() && imageFolder.exists() && imageFolder.isDirectory()) {
       // 尝试多种查找策略...
   }
   ```

### 遇到的问题及解决方案

#### 问题1: SQL错误
- **问题**: 启动应用时出现"Unknown column 'ulid' in 'field list'"错误
- **原因**: 数据库表中缺少必要字段
- **解决**: 执行完整的重建表脚本，确保所有必要字段存在

#### 问题2: 图片显示不一致
- **问题**: 文章中的图片占位符无法正确关联到实际图片
- **原因**: 图片ID生成逻辑不一致
- **解决**: 修改`WeChatArticleSpider.java`，确保为图片生成唯一ID并在整个处理流程中保持一致

#### 问题3: 图片查找失败
- **问题**: 前端请求图片时返回404错误
- **原因**: 路径格式不一致或图片文件不存在
- **解决**: 增强`ImageController`，添加多层回退策略

### 测试验证
1. 数据库结构验证:
   - 执行重建表脚本
   - 验证所有必要字段和约束存在
   - 成功运行应用，无SQL错误

2. 爬取新文章测试:
   - 爬取新文章并验证图片处理
   - 确认图片正确下载并分类存储
   - 检查图片占位符与实际图片ID一致

3. 前端显示测试:
   - 浏览文章列表和详情页
   - 验证所有图片正确显示
   - 测试各种图片请求格式

### 总结与经验
1. **数据库设计**:
   - 数据库结构应当集中管理，避免分散在代码中的SQL定义
   - 使用版本化的数据库迁移脚本，便于跟踪变更

2. **图片处理**:
   - 为媒体资源生成唯一ID时，需要确保在整个处理流程中保持一致
   - 建立清晰的映射关系，关联原始URL、处理后的ID和存储路径

3. **错误处理**:
   - 实现多层回退策略，当首选方案失败时有替代方案
   - 提供详细的日志信息，便于问题诊断

### 后续优化方向
1. 考虑实现图片缓存机制，提高图片加载速度
2. 添加图片压缩处理，优化存储空间和加载时间
3. 考虑使用统一的图片命名规则，简化图片查找逻辑
4. 探索使用云存储服务存储图片，提高可靠性和访问速度