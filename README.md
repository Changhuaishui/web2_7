# 微信公众号文章爬虫系统

## 项目简介
本项目是一个基于 Spring Boot 和 Vue.js 的微信公众号文章爬虫系统，实现了文章的爬取、存储、搜索和标签管理等功能。系统采用前后端分离架构，后端使用 Java 开发，前端使用 Vue.js 框架。
## 功能介绍
本系统主要提供微信公众号文章的爬取、管理、搜索和标签功能。以下是功能演示视频和主要功能的截图展示：

### 设计文档
[https://github.com/Changhuaishui/web2_7/blob/master/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3/5-8%E8%AE%BA%E6%96%87%E6%8F%90%E7%BA%B2-%E5%88%9D%E7%A8%BF.pdf]
[https://qq0fqhnu1rx.feishu.cn/docx/AZ1ZdmifuoTIm8x0YGJckc9snjh?from=from_copylink]

### 功能演示视频
[https://www.bilibili.com/video/BV1LqV6z4EEd/?share_source=copy_web&vd_source=289e94bde278ed942d54eaf0108f4aa5]

### 首页
![功能截图](https://raw.githubusercontent.com/Changhuaishui/web2_7/master/image/README/1.png)

### 详情页功能1
![文章管理截图](https://raw.githubusercontent.com/Changhuaishui/web2_7/master/image/README/2.png)

### 详情页功能2
![搜索功能截图](https://raw.githubusercontent.com/Changhuaishui/web2_7/master/image/README/3.png)

### 详情页功能3
![推荐同类型公众号文章](https://raw.githubusercontent.com/Changhuaishui/web2_7/master/image/README/4.png)


## 开发日志（尚未整理完善）
DEVELOPMENT_LOG.md  [https://github.com/Changhuaishui/web2_7/blob/b0bc368e56a81f8c8b41b242bb761d277dc5d6c9/DEVELOPMENT_LOG.md]
## 技术栈
### 后端技术
- Spring Boot 3.4.3
- MyBatis 3.0.3
- Apache Lucene 8.11.2
- WebMagic 1.0.3
- MySQL 8.0

### 前端技术
- Vue.js 3.5.13
- Vue Router 4.5.0
- Element Plus 2.9.7
- Axios 1.8.4
- Vite 6.2.1 (构建工具)
- DOMPurify 3.0.9 (HTML净化)
- Dayjs 1.11.13 (日期处理)

## 系统架构
### 后端架构
```
src/main/java/org/example/web2_7/
├── config/           # 配置类
│   └── CorsConfig.java                 # 跨域配置
├── controller/       # 控制器层
│   ├── ArticleController.java          # 文章基本CRUD操作
│   ├── ArticleSearchController.java    # 文章搜索和索引重建
│   ├── ChatController.java             # AI对话功能请求处理
│   ├── CrawlerController.java          # 爬虫控制，提供爬取任务提交、文章获取等接口
│   ├── HomeController.java             # 首页接口
│   ├── ImageController.java            # 图片资源处理，提供上传下载功能
│   ├── SummaryController.java          # 文章摘要生成和管理
│   └── TagController.java              # 标签管理，提供增删改查功能
├── Dao/              # 数据访问层
│   ├── ArticleMapper.java              # 文章数据库操作接口
│   └── ArticleDao.java                 # 文章数据访问对象
├── pojo/             # 实体类
│   └── Article.java                    # 文章实体类，定义文章属性和结构
├── service/          # 服务层
│   ├── ArticleService.java             # 文章业务逻辑实现
│   ├── ArticleSearchService.java       # 文章搜索服务，封装搜索功能
│   ├── CrawlerService.java             # 爬虫服务，处理爬取逻辑
│   ├── DeepSeekService.java            # AI服务接口，提供摘要生成和关键词提取
│   ├── LuceneIndexService.java         # Lucene索引服务，实现全文检索
│   ├── RelatedArticleService.java      # 相关文章服务，处理相关文章爬取和管理
│   ├── TagService.java                 # 标签服务，实现标签业务逻辑
│   ├── testService.java                # 测试服务
│   └── impl/
│       └── 实现类                      # 各服务接口的具体实现
├── crawler/          # 爬虫模块
│   ├── WeChatArticleSpider.java        # 微信公众号文章爬虫，解析页面内容
│   ├── DatabasePipeline.java           # 数据库管道，将爬取数据存入数据库
│   └── ConsolePipeline.java            # 控制台管道，输出爬取结果到控制台
├── utils/            # 工具类
├── test/             # 测试模块
└── Main.java         # 应用入口
```

### 前端架构
```
frontend/
├── public/           # 静态资源目录
├── src/              # 源码目录
│   ├── assets/       # 资源文件(图片、字体等)
│   ├── components/   # 组件
│   │   ├── ArticleDetail.vue  # 文章详情组件，展示文章内容和图片
│   │   ├── Home.vue           # 首页组件，展示文章列表和爬虫表单
│   │   ├── Search.vue         # 搜索组件，提供搜索功能和结果展示
│   │   └── icons/             # 图标组件集，提供UI图标
│   ├── router/       # 路由配置
│   │   └── index.js           # 路由定义，配置前端页面路由
│   ├── utils/        # 工具函数
│   │   └── request.js         # Axios请求封装，统一处理API请求
│   ├── views/        # 页面视图
│   │   ├── Home.vue           # 首页视图，整合首页组件和布局
│   │   ├── ArticleHtml.vue    # 文章HTML视图，展示原始HTML内容
│   │   ├── ArticleDetail.vue  # 文章详情视图，包含详情组件和相关操作
│   │   ├── NotFound.vue       # 404页面，处理无效路径访问
│   │   └── Search.vue         # 搜索页面，整合搜索组件和结果展示
│   ├── App.vue       # 根组件，提供整体布局和全局状态管理
│   └── main.js       # 应用入口，初始化Vue应用和全局配置
├── index.html        # HTML模板，前端应用的主HTML文件
├── vite.config.js    # Vite配置，设置构建和开发环境配置
└── package.json      # 依赖配置，管理项目依赖和脚本命令
```

## 功能模块
### 控制器层
- **ArticleController**：提供文章基本操作接口，如获取文章详情、创建和更新文章、分页查询等
- **ArticleSearchController**：提供文章搜索和重建索引的接口，支持基于关键词的全文检索
- **ChatController**：处理聊天交互相关请求，集成DeepSeek AI功能提供智能对话
- **CrawlerController**：提供文章爬取功能，包括爬取任务提交、获取文章列表、根据账号名获取文章、删除文章以及图片访问等接口
- **HomeController**：提供首页访问接口，处理系统入口页面的请求
- **ImageController**：负责图片资源管理，提供基于ULID的智能图片访问接口，支持多种图片格式和匹配策略
- **SummaryController**：提供文章摘要生成、关键词提取和相关文章推荐功能，集成AI技术自动分析文章内容
- **TagController**：提供完整的标签管理功能，包括获取所有标签、获取文章标签、根据标签筛选文章、标签缓存预热等

### 服务层
- **ArticleService**：负责文章的基本管理功能，处理文章的增删改查等基础业务逻辑
- **ArticleSearchService**：实现文章的搜索功能，支持多字段搜索和结果排序
- **CrawlerService**：负责文章的爬取任务，包括页面抓取、内容解析和图片下载
- **DeepSeekService**：提供AI辅助功能，包括文章摘要生成、关键词提取和智能对话等功能
- **LuceneIndexService**：负责文章的索引和搜索功能，使用Lucene实现高效的全文检索，支持索引重建和多字段查询
- **RelatedArticleService**：提供相关文章推荐功能，基于文章关键词爬取和管理相关内容
- **TagService**：负责文章标签的管理功能，包括标签提取、标签过滤和标签缓存管理

### 爬虫模块
- **WeChatArticleSpider**：负责解析微信公众号页面内容，提取文章的标题、作者、发布时间、正文、图片等信息，并支持图片下载到本地
- **DatabasePipeline**：负责将爬取的数据存储到数据库中，同时创建搜索索引以支持全文检索
- **ConsolePipeline**：用于控制台输出爬取结果，便于开发调试和监控爬虫运行状态

### 前端模块
- **路由系统**：使用Vue Router实现SPA路由导航
  - 首页路由 (/)：展示文章列表和爬虫表单，支持文章分页浏览
  - 文章详情路由 (/article/:id)：展示单篇文章详情，包括内容、图片和相关文章
  - HTML内容路由 (/html/:id)：展示文章原始HTML内容，用于调试和特殊查看需求
  - 搜索路由 (/search)：提供高级搜索功能界面
  - 404路由：处理无效URL访问

- **组件系统**：
  - **App.vue**：应用根组件，包含整体布局、导航菜单和全局状态管理
  - **components/ArticleDetail.vue**：文章详情组件，展示文章的完整信息，支持图片浏览和内容渲染
  - **components/Home.vue**：首页组件，集成爬虫表单、文章列表和分页控制
  - **components/Search.vue**：搜索组件，提供多条件搜索功能和结果展示
  - **components/icons**：图标组件集，提供统一的UI图标系统
  - **views/Home.vue**：首页视图，整合首页组件和页面布局
  - **views/ArticleDetail.vue**：文章详情视图，包含详情组件和相关操作如生成摘要、查看相关文章等
  - **views/ArticleHtml.vue**：文章HTML视图，用于展示原始HTML内容
  - **views/NotFound.vue**：404页面，提供友好的错误提示
  - **views/Search.vue**：搜索页面，整合搜索组件和搜索结果展示逻辑

- **网络请求**：
  - 封装Axios进行API调用，统一处理请求和响应
  - 支持请求拦截和响应拦截，提供统一的错误处理机制
  - 完善的错误处理和状态提示
  - 通过Vite代理解决跨域问题

- **UI框架**：
  - 基于Element Plus组件库构建用户界面，提供现代化的视觉体验
  - 响应式设计，适配不同屏幕尺寸，支持移动端访问
  - 中文本地化支持
  - 自定义主题和样式系统

- **安全性**：
  - 使用DOMPurify净化HTML内容，防止XSS攻击
  - 输入验证和参数校验，防止恶意请求
  - 错误处理和异常捕获机制
  - 资源访问控制和权限管理

## 功能特性
1. **文章爬取**
   - 支持单个微信公众号文章URL爬取，自动提取文章内容
   - 自动提取文章标题、作者、发布时间、账号名称和正文内容
   - 支持图片下载和本地存储，智能创建以ULID命名的图片文件夹
   - 智能处理图片路径，建立图片映射关系，将网络路径转换为本地相对路径
   - 支持多种URL格式处理，包括带参数的URL和不同域名的微信文章链接

2. **文章管理**
   - 文章列表展示，支持分页和排序
   - 按公众号分类查看，提供分组浏览功能
   - 文章删除功能，支持逻辑删除和物理删除
   - 标签系统，支持对文章自动提取标签、手动添加标签和标签筛选
   - 文章详情查看，包括原始HTML查看和格式化内容查看

3. **AI增强功能**
   - 基于DeepSeek的智能摘要生成，自动提取文章关键内容
   - 智能关键词提取，用于内容标记和相关文章推荐
   - 相关文章推荐系统，基于关键词匹配爬取和展示相关内容

4. **搜索功能**
   - 基于Lucene的高性能全文搜索
   - 支持多字段搜索（标题、内容、作者、公众号名称等）
   - 高级搜索（支持时间范围、公众号筛选、关键词组合）
   - 搜索索引可重建，保证搜索结果准确性

5. **图片资源管理**
   - 自动下载并存储文章中的图片
   - 图片资源与文章关联管理，通过映射表实现高效访问
   - 支持图片占位符替换，确保文章内容完整性
   - 处理图片缺失和格式不一致的情况

6. **标签系统**
   - 自动标签提取功能，基于文章生成标签
   - 标签缓存系统，提高标签操作性能
   - 定时任务自动更新标签缓存，保持数据一致性
   - 按标签筛选文章，实现内容分类浏览

7. **跨域支持**
   - 支持前后端分离部署，完善的CORS配置

8. **用户体验优化**
   - 全局错误捕获和友好的错误提示

## 数据库设计

系统使用MySQL数据库，采用了简洁高效的数据结构设计：

### 数据库表关系
```
+------------------+       +--------------------+       +------------------+
| article_table    |       | article_full_html  |       | related_articles |
+------------------+       +--------------------+       +------------------+
| id (PK)          |<----->| article_id (FK)    |       | id (PK)          |
| ulid             |       | full_html          |       | article_id (FK)  |
| title            |       | url_mapping        |       | related_url      |
| author           |       | created_at         |       | title            |
| url              |       +--------------------+       | created_at       |
| source_url       |                                    +------------------+
| account_name     |
| publish_time     |
| content          |
| images           |
| image_mappings   |
| summary          |
| keywords         |
| is_deleted       |
| created_at       |
| updated_at       |
+------------------+
```

### 主要表说明

1. **article_table**：存储文章基本信息
   - 核心字段：标题、作者、URL、正文内容、公众号名称、发布时间
   - 特殊字段：ULID唯一标识（用于前端URL路由）、图片映射JSON、文章摘要、关键词
   - 索引优化：为标题、作者、URL、发布时间等字段创建索引提升查询性能

2. **article_full_html**：存储文章完整HTML内容
   - 与article_table形成一对一关系，通过article_id外键关联
   - 避免大量HTML内容影响主表查询性能
   - URL映射功能：存储原始URL到本地路径的映射，支持图片资源本地化

3. **related_articles**：存储相关文章信息
   - 与article_table形成一对多关系，一篇文章可有多个相关文章
   - 存储相关文章URL和标题，便于快速展示
   - 独特约束：article_id和related_url组合唯一，防止重复添加相同相关文章

### 数据库功能特点

1. **数据完整性保障**
   - 外键关系使用CASCADE删除规则，删除文章自动删除相关记录
   - 唯一约束确保数据不重复，如URL和ULID唯一性约束
   - 合理设计字段类型和长度，MEDIUMTEXT存储大文本提高性能

2. **查询性能优化**
   - 关键字段建立索引提高查询速度
   - 表结构拆分避免大字段影响查询性能
   - 符合范式设计减少数据冗余

3. **灵活的数据处理**
   - 逻辑删除功能（is_deleted字段），保护数据不被意外删除
   - 时间戳自动维护（created_at和updated_at字段）
   - JSON字段存储复杂结构数据（如image_mappings）

4. **标签系统特性**
   - 标签生成完全通过Java代码中的TagService动态计算
   - 不依赖独立的标签表，降低数据库复杂度
   - 采用高效的缓存机制提升标签操作性能

5. **数据库工具支持**
   - 提供完整的数据库维护脚本，包括查询、清空和重建功能
   - 支持数据库结构查询和优化工具
   - 数据完整性检查工具，发现和修复数据不一致问题

> 详细的数据库设计文档和SQL脚本可在 `src/main/resources/schema/` 目录中查看

## 安装部署
### 环境要求
- JDK 17+
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+
- Git 2.49.0+

### 后端部署
1. 克隆项目
```bash
git clone https://github.com/Changhuaishui/web2_7.git
cd web2_7
```

2. 配置数据库
```sql
CREATE DATABASE crawler_db;
USE crawler_db;

CREATE TABLE article_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    url VARCHAR(500) UNIQUE NOT NULL,
    account_name VARCHAR(100),
    publish_time DATETIME,
    content TEXT,
    images TEXT
);
```

3. 修改配置文件
编辑 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crawler_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. 构建运行
```bash
mvn clean package
java -jar target/web2_7-0.0.1-SNAPSHOT.jar
```

### 前端部署
1. 进入前端目录
```bash
cd frontend
```

2. 安装依赖
```bash
npm install
```

3. 开发环境运行
```bash
npm run dev
```

4. 生产环境构建
```bash
npm run build
```

## API 文档
### 爬虫接口
1. 提交爬取任务
- URL: `POST /api/crawler/crawl`
- 请求体: `{"url": "文章URL"}`
- 响应: 爬取状态信息
- 说明: 提交微信公众号文章URL进行爬取

2. 获取所有文章
- URL: `GET /api/crawler/articles`
- 响应: 文章列表
- 说明: 获取所有已爬取的文章，按发布时间排序

3. 获取文章详情
- URL: `GET /api/crawler/articles/{id}`
- 参数: `id` - 文章ID
- 响应: 文章详情
- 说明: 根据ID获取特定文章的基本信息

4. 根据URL获取文章详情
- URL: `POST /api/crawler/articles/detail`
- 请求体: `{"url": "文章URL"}`
- 响应: 文章详情，包含图片映射和关键词
- 说明: 根据文章URL获取详细信息，支持多种URL格式

5. 获取特定公众号的文章
- URL: `GET /api/crawler/articles/account/{accountName}`
- 参数: `accountName` - 公众号名称
- 响应: 该公众号的文章列表
- 说明: 根据公众号名称筛选文章

6. 删除文章
- URL: `DELETE /api/crawler/articles/{url}`
- 参数: `url` - 文章URL (URL编码)
- 响应: 删除操作状态
- 说明: 根据URL删除特定文章

7. 获取文章HTML内容
- URL: `GET /api/crawler/{id}/html`
- 参数: `id` - 文章ID
- 响应: 文章原始HTML内容
- 说明: 获取文章的原始HTML格式，用于调试

8. 获取文章图片
- URL: `GET /api/crawler/images/{folder}/{filename}`
- 参数: 
  - `folder` - 图片文件夹名
  - `filename` - 图片文件名
- 响应: 图片资源
- 说明: 获取文章中的图片资源

### 搜索接口
1. 搜索文章
- URL: `GET /api/search?keyword=搜索关键词`
- 参数: `keyword` - 搜索关键词
- 响应: 匹配的文章列表
- 说明: 使用Lucene进行全文检索，支持标题、内容等多字段搜索

2. 重建索引
- URL: `POST /api/search/rebuild-index`
- 响应: 索引重建状态
- 说明: 重建所有文章的搜索索引，用于修复搜索功能

### 标签接口
1. 获取所有标签
- URL: `GET /api/tags`
- 响应: 标签列表
- 说明: 获取系统中所有可用标签

2. 获取文章标签
- URL: `GET /api/tags/article/{id}`
- 参数: `id` - 文章ID
- 响应: 文章标签信息，包含文章ID、标题和标签列表
- 说明: 获取特定文章的所有标签

3. 根据标签筛选文章
- URL: `GET /api/tags/filter?tag=标签名`
- 参数: `tag` - 标签名称
- 响应: 包含该标签的文章列表
- 说明: 根据指定标签查找相关文章

4. 预热标签缓存
- URL: `POST /api/tags/warm-cache`
- 响应: 缓存预热状态
- 说明: 管理员接口，手动触发标签缓存预热

### 摘要和关键词接口
1. 生成文章摘要
- URL: `POST /api/articles/{id}/summarize`
- 参数: 
  - `id` - 文章ID
  - `force` - 是否强制重新生成(可选，默认false)
- 响应: 生成的摘要和关键词
- 说明: 使用AI为文章生成摘要和关键词，同时触发相关文章爬取

2. 获取文章关键词
- URL: `GET /api/articles/{id}/keywords`
- 参数: `id` - 文章ID
- 响应: 文章关键词
- 说明: 获取或自动生成文章关键词

3. 获取相关文章
- URL: `GET /api/articles/{id}/related`
- 参数: `id` - 文章ID
- 响应: 相关文章列表
- 说明: 获取与特定文章相关的其他文章

4. 爬取相关文章
- URL: `POST /api/articles/{id}/crawl-related`
- 参数: `id` - 文章ID
- 响应: 爬取状态
- 说明: 根据文章关键词爬取相关文章

### 图片接口
1. 根据ULID获取图片
- URL: `GET /api/images/{articleUlid}/{imageUlid}`
- 参数:
  - `articleUlid` - 文章的ULID
  - `imageUlid` - 图片的ULID
- 响应: 图片资源
- 说明: 智能图片访问接口，支持多种匹配策略和格式

### 聊天接口
1. AI对话
- URL: `POST /api/chat`
- 请求体: `{"userId": "用户ID", "message": "用户消息"}`
- 响应: AI生成的回复
- 说明: 基于DeepSeek的AI对话功能

## 版本控制
本项目使用Git进行版本控制，托管在GitHub上。

## 常见问题
1. 爬虫相关
   - Q: 爬取失败如何处理？
   - A: 检查URL格式，确保文章可访问，查看错误日志。尝试重建索引。

2. 搜索相关
   - Q: 搜索结果不准确？
   - A: 使用"重建索引"功能更新所有文章的搜索索引，确保数据一致性。

3. 图片相关
   - Q: 图片无法显示？
   - A: 检查图片文件夹权限和路径是否正确，确认图片已成功下载。

4. 前端相关
   - Q: 跨域请求失败？
   - A: 确认后端CORS配置是否正确，检查Vite代理配置是否正确。

   - Q: 组件渲染错误？
   - A: 查看浏览器控制台报错信息，检查组件传参是否正确。

## 维护团队
- 开发者：[chen]
- 技术支持：[2021011100@bistu.edu.cn]

## 系统截图
