# 微信公众号文章爬虫系统

## 项目简介
本项目是一个基于 Spring Boot 和 Vue.js 的微信公众号文章爬虫系统，实现了文章的爬取、存储、搜索等功能。系统采用前后端分离架构，后端使用 Java 开发，前端使用 Vue.js 框架。

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
#### 页面组件
- Home.vue： 负责具体的文章爬取表单、搜索框和文章列表展示
- App.vue：  作为主应用组件，包含了系统的整体布局，如头部和主内容区域
#### 功能实现
- 爬取文章：用户可以在输入框中输入微信公众号文章的 URL，点击 “开始爬取” 按钮，前端会向后端发送爬取请求。通过 WebMagic 框架实现，支持单个文章URL爬取，自动提取文章标题、作者、发布时间等信息，支持图片下载和存储。
- 搜索文章：用户可以输入关键词进行搜索，系统会返回匹配的文章列表。支持全文搜索和高级搜索（时间范围、公众号筛选）。
- 文章删除：在文章列表中，用户可以点击 “删除” 按钮，前端会向后端发送删除请求，后端会对文章进行逻辑删除，并更新索引。
## 系统架构
```
src/main/java/org/example/web2_7/
├── config/           # 配置类
│   └── CorsConfig.java
├── controller/       # 控制器层
│   ├── CrawlerController.java
│   ├── SearchController.java
│   └── HomeController.java
├── dao/             # 数据访问层
│   └── ArticleMapper.java
├── model/           # 实体类
│   └── Article.java
├── service/         # 服务层
│   ├── CrawlerService.java
│   ├── SearchService.java
│   └── impl/
│       ├── CrawlerServiceImpl.java
│       └── SearchServiceImpl.java
├── utils/           # 工具类
│   ├── DatabasePipeline.java
│   └── WeChatArticleSpider.java
└── Main.java        # 应用入口
```
## 功能模块
### 控制器层
- 主要有 CrawlerController 和 SearchController 两个控制器。
- CrawlerController：提供了文章爬取、获取所有文章、根据账号名获取文章和删除文章等接口。
- SearchController：提供了文章搜索和索引文章的接口。
### 服务层
- CrawlerService：负责文章的爬取任务。
- LuceneIndexService：负责文章的索引和搜索功能，使用 Lucene 实现多字段搜索。
- SearchServiceImpl：实现了 SearchService 接口，调用 LuceneIndexService 的方法进行文章搜索和索引。
### 爬虫模块
- 使用 WebMagic 框架实现文章的爬取，WeChatArticleSpider 类负责解析页面内容，提取文章的标题、作者、发布时间、正文、图片等信息，并将图片下载到本地。DatabasePipeline 类负责将爬取的数据存储到数据库中，并创建搜索索引。
### 数据库MySQL
- 使用 ArticleMapper 进行数据的持久化操作，包括插入文章、查找文章、逻辑删除文章等。
## 功能特性
1. 文章爬取
   - 支持单个微信公众号文章URL爬取
   - 自动提取文章标题、作者、发布时间等信息
   - 支持图片下载和存储

2. 文章管理
   - 文章列表展示
   - 按公众号分类查看
   - 文章删除功能

3. 搜索功能
   - 全文搜索
   - 高级搜索（支持时间范围、公众号筛选）
   - 实时索引更新

## 安装部署
### 环境要求
- JDK 17+
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+

### 后端部署
1. 克隆项目
```bash
git clone [项目地址]
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

### 搜索接口
1. 搜索文章
- URL: GET /api/search
- 参数：keyword=搜索关键词
- 返回：匹配的文章列表

## 开发指南
### 代码规范
1. 命名规范
   - 类名：驼峰命名，首字母大写
   - 方法名：驼峰命名，首字母小写
   - 变量名：驼峰命名，首字母小写
   - 常量名：全大写，下划线分隔

2. 注释规范
   - 类注释：说明类的功能和作用
   - 方法注释：说明方法的功能、参数和返回值
   - 关键代码注释：说明复杂逻辑

### 开发流程
1. 功能开发
   - 创建功能分支
   - 编写代码和单元测试
   - 提交代码审查
   - 合并到主分支

2. 测试流程
   - 单元测试
   - 集成测试
   - 功能测试
   - 性能测试

## 常见问题
1. 爬虫相关
   - Q: 爬取失败如何处理？
   - A: 检查URL格式，确保文章可访问，查看错误日志

2. 搜索相关
   - Q: 搜索结果不准确？
   - A: 检查索引是否更新，确认搜索关键词正确

## 更新日志
### v1.0.0 (2025-03-27)
- 实现基础爬虫功能
- 完成文章管理系统
- 集成全文搜索功能

## 维护团队
- 后端开发：[chen]
- 前端开发：[chen]
- 技术支持：[2021011100@bistu.edu.cn]

## 许可证
本项目采用 MIT 许可证 