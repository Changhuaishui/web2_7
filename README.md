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
- Vue.js 3
- Element Plus
- Axios
- Vite (构建工具)

## 系统架构
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

### 前端组件
- **Home.vue**：首页组件，提供文章爬取表单、搜索框和文章列表展示
- **ArticleDetail.vue**：文章详情页组件，展示文章详细内容
- **ArticleHtml.vue**：文章HTML渲染组件，用于展示文章的原始HTML格式
- **Search.vue**：搜索组件，提供高级搜索功能

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




## 维护
- 开发者：[chen]
- 技术支持：[2021011100@bistu.edu.cn]

