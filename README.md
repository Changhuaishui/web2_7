# 微信公众号文章爬虫系统

## 项目简介
本项目是一个基于 Spring Boot 和 Vue.js 的微信公众号文章爬虫系统，实现了文章的爬取、存储、搜索和标签管理等功能。系统采用前后端分离架构，后端使用 Java 开发，前端使用 Vue.js 框架。

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
│   └── CorsConfig.java
├── controller/       # 控制器层
│   ├── CrawlerController.java
│   ├── ArticleController.java
│   ├── ArticleSearchController.java
│   ├── TagController.java
│   └── HomeController.java
├── Dao/              # 数据访问层
│   ├── ArticleMapper.java
│   └── ArticleDao.java
├── pojo/             # 实体类
│   └── Article.java
├── service/          # 服务层
│   ├── CrawlerService.java
│   ├── ArticleService.java
│   ├── ArticleSearchService.java
│   ├── LuceneIndexService.java
│   ├── TagService.java
│   └── impl/
│       ├── CrawlerServiceImpl.java
│       └── ArticleServiceImpl.java
├── crawler/          # 爬虫模块
│   ├── WeChatArticleSpider.java
│   ├── DatabasePipeline.java
│   └── ConsolePipeline.java
├── utils/            # 工具类
└── Main.java         # 应用入口
```

### 前端架构
```
frontend/
├── public/           # 静态资源目录
├── src/              # 源码目录
│   ├── assets/       # 资源文件(图片、字体等)
│   │   ├── ArticleDetail.vue  # 文章详情组件
│   │   ├── Search.vue         # 搜索组件
│   │   └── icons/             # 图标组件
│   ├── router/       # 路由配置
│   │   └── index.js           # 路由定义
│   ├── utils/        # 工具函数
│   │   └── request.js         # Axios请求封装
│   ├── views/        # 页面视图
│   │   ├── Home.vue           # 首页视图
│   │   ├── ArticleHtml.vue    # 文章HTML视图
│   │   ├── NotFound.vue       # 404页面
│   │   └── Search.vue         # 搜索页面
│   ├── App.vue       # 根组件
│   └── main.js       # 应用入口
├── index.html        # HTML模板
├── vite.config.js    # Vite配置
└── package.json      # 依赖配置
```

## 功能模块
### 控制器层
- **CrawlerController**：提供了文章爬取、获取所有文章、根据账号名获取文章、删除文章和图片访问等接口
- **ArticleSearchController**：提供了文章搜索和重建索引的接口
- **ArticleController**：提供文章基本操作接口
- **TagController**：提供标签管理相关接口
- **HomeController**：提供首页访问接口

### 服务层
- **CrawlerService**：负责文章的爬取任务
- **ArticleService**：负责文章的基本管理功能
- **ArticleSearchService**：实现文章的搜索功能
- **LuceneIndexService**：负责文章的索引和搜索功能，使用 Lucene 实现多字段高效搜索
- **TagService**：负责文章标签的管理功能

### 爬虫模块
- **WeChatArticleSpider**：负责解析微信公众号页面内容，提取文章的标题、作者、发布时间、正文、图片等信息，并支持图片下载到本地
- **DatabasePipeline**：负责将爬取的数据存储到数据库中，并创建搜索索引
- **ConsolePipeline**：用于控制台输出爬取结果，便于开发调试

### 前端模块
- **路由系统**：使用Vue Router实现SPA路由导航
  - 首页路由 (/)：展示文章列表和爬虫表单
  - 文章详情路由 (/article/:id)：展示单篇文章详情
  - HTML内容路由 (/html/:id)：展示文章原始HTML内容
  - 404路由：处理无效URL访问

- **组件系统**：
  - **App.vue**：应用根组件，包含整体布局、导航菜单和全局状态管理
  - **ArticleDetail**：文章详情组件，展示文章的完整信息，支持图片浏览
  - **Search**：搜索组件，提供高级搜索功能和搜索结果展示
  - **Home**：首页组件，集成爬虫表单、搜索框和文章列表展示

- **网络请求**：
  - 封装Axios进行API调用，统一处理请求和响应
  - 完善的错误处理和状态提示
  - 通过Vite代理解决跨域问题

- **UI框架**：
  - 基于Element Plus组件库构建用户界面
  - 响应式设计，适配不同屏幕尺寸
  - 中文本地化支持

- **安全性**：
  - 使用DOMPurify净化HTML内容，防止XSS攻击
  - 输入验证和错误处理
  - 资源访问控制

## 功能特性
1. **文章爬取**
   - 支持单个微信公众号文章URL爬取
   - 自动提取文章标题、作者、发布时间、内容等信息
   - 支持图片下载和本地存储，以文章标题命名创建图片文件夹
   - 处理图片路径，将网络路径转换为本地相对路径

2. **文章管理**
   - 文章列表展示，支持分页
   - 按公众号分类查看
   - 文章删除功能
   - 标签系统，支持对文章添加和管理标签

3. **搜索功能**
   - 基于Lucene的全文搜索
   - 支持多字段搜索（标题、内容、作者等）
   - 高级搜索（支持时间范围、公众号筛选）
   - 搜索索引可重建，保证搜索结果准确性

4. **图片资源管理**
   - 自动下载并存储文章中的图片
   - 提供图片访问API，支持不同格式图片展示
   - 图片资源与文章关联管理

5. **跨域支持**
   - 支持前后端分离部署
   - 完善的CORS配置，确保安全访问

6. **错误处理与用户体验**
   - 全局错误捕获和处理机制
   - 友好的用户提示和反馈
   - 加载状态和进度指示
   - 表单验证和数据校验

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
- URL: POST /api/crawler/crawl
- 参数：{"url": "文章URL"}
- 返回：爬取状态信息

2. 获取文章列表
- URL: GET /api/crawler/articles
- 返回：文章列表

3. 获取文章图片
- URL: GET /api/crawler/images/{folder}/{filename}
- 返回：图片资源

### 搜索接口
1. 搜索文章
- URL: GET /api/search?keyword=搜索关键词
- 返回：匹配的文章列表

2. 重建索引
- URL: POST /api/search/rebuild-index
- 返回：索引重建状态

### 标签接口
1. 获取所有标签
- URL: GET /api/tags
- 返回：标签列表

2. 为文章添加标签
- URL: POST /api/tags/article/{articleId}
- 参数：{"tags": ["标签1", "标签2"]}
- 返回：操作状态

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

## 许可证
本项目采用 MIT 许可证

