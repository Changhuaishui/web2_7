package org.example.web2_7.controller;
/*
 * @author chen
 * 接收HTTP请求，参数校验，调用服务层
 * 设计提供REST API接口，vue与后端交互
 * 有：
 * POST   /crawl              提交爬取任务
 * GET    /articles          获取所有文章
 * GET    /articles/account/* 获取指定公众号文章
 * DELETE /articles/*        删除文章
 * GET    /{id}/html         获取文章HTML内容
 * 
 */
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.CrawlerService;
import org.example.web2_7.service.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/crawler")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class CrawlerController {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private LuceneIndexService luceneIndexService;

    @PostMapping("/crawl")    // 提交爬取任务
    public ResponseEntity<?> crawlArticle(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body("URL不能为空");
        }

        // 检查URL是否已经爬取过
        Article existingArticle = articleMapper.findByUrl(url);
        if (existingArticle != null) {
            return ResponseEntity.ok("该文章已经爬取过");
        }

        try {
            crawlerService.crawlArticle(url);
            return ResponseEntity.ok("爬取任务已提交");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("爬取失败：" + e.getMessage());
        }
    }

    @GetMapping("/articles")    // 获取所有文章
    public ResponseEntity<List<Article>> getArticles() {
        return ResponseEntity.ok(articleMapper.findAllOrderByPublishTime());
    }

    // 获取文章基本信息，访问MySQL的article_table表，查询id的记录
    @GetMapping("/articles/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Integer id) {
        try {
            Article article = articleMapper.findById(id);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取文章失败：" + e.getMessage());
        }
    }

    @PostMapping("/articles/detail")  // 获取文章详情的POST接口
    public ResponseEntity<?> getArticleDetailByUrl(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            if (url == null || url.isEmpty()) {
                return ResponseEntity.badRequest().body("URL不能为空");
            }

            System.out.println("收到文章详情请求，URL: " + url);
            
            // URL解码
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            System.out.println("URL解码后: " + decodedUrl);

            // 尝试查找文章
            Article article = articleMapper.findByUrl(decodedUrl);
            
            // 如果找不到，尝试不同的URL变体
            if (article == null) {
                System.out.println("未找到精确匹配，尝试其他变体");
                // 尝试添加/移除https前缀
                String urlWithHttps = decodedUrl.startsWith("https://") ? decodedUrl : "https://" + decodedUrl;
                String urlWithoutHttps = decodedUrl.replace("https://", "").replace("http://", "");
                
                article = articleMapper.findByUrl(urlWithHttps);
                if (article == null) {
                    article = articleMapper.findByUrl(urlWithoutHttps);
                }
                
                if (article == null) {
                    System.out.println("所有URL变体都未找到匹配的文章");
                    return ResponseEntity.status(404).body("未找到匹配的文章");
                }
            }

            // 获取文章HTML内容
            String fullHtml = null;
            if (article.getId() != null) {
                fullHtml = articleMapper.getArticleHtml(article.getId());
            }
            
            // 构建返回对象
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", article.getId());
            result.put("ulid", article.getUlid());
            result.put("title", article.getTitle());
            result.put("author", article.getAuthor());
            result.put("url", article.getUrl());
            result.put("sourceUrl", article.getSourceUrl());
            result.put("accountName", article.getAccountName());
            result.put("publishTime", article.getPublishTime());
            result.put("content", article.getContent());
            result.put("images", article.getImages());
            
            if (fullHtml != null) {
                result.put("fullHtml", fullHtml);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取文章详情失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("获取文章详情失败：" + e.getMessage());
        }
    }

    @GetMapping("/articles/account/{accountName}")  // 根据账号名获取文章
    public ResponseEntity<List<Article>> getArticlesByAccount(@PathVariable String accountName) {
        return ResponseEntity.ok(articleMapper.findByAccountName(accountName));
    }

    @DeleteMapping("/articles/{url}")  // 删除文章
    @Transactional
    public ResponseEntity<?> deleteArticle(@PathVariable String url) {
        try {
            if (url == null || url.isEmpty()) {
                return ResponseEntity.badRequest().body("URL不能为空");
            }

            System.out.println("收到删除请求，原始URL: " + url);
            
            // URL双重解码
            String decodedUrl = java.net.URLDecoder.decode(
                java.net.URLDecoder.decode(url, "UTF-8"), 
                "UTF-8"
            );
            System.out.println("URL解码后: " + decodedUrl);

            // 如果URL包含逗号，说明是多个URL，只取第一个
            if (decodedUrl.contains(",")) {
                decodedUrl = decodedUrl.split(",")[0].trim();
                System.out.println("提取第一个URL: " + decodedUrl);
            }

            // 尝试查找文章
            Article article = articleMapper.findByUrl(decodedUrl);
            
            // 如果找不到，尝试不同的URL变体
            if (article == null) {
                System.out.println("未找到精确匹配，尝试其他变体");
                // 尝试添加/移除https前缀
                String urlWithHttps = decodedUrl.startsWith("https://") ? decodedUrl : "https://" + decodedUrl;
                String urlWithoutHttps = decodedUrl.replace("https://", "").replace("http://", "");
                
                article = articleMapper.findByUrl(urlWithHttps);
                if (article == null) {
                    article = articleMapper.findByUrl(urlWithoutHttps);
                }
                
                if (article == null) {
                    System.out.println("所有URL变体都未找到匹配的文章");
                    return ResponseEntity.status(404).body("未找到匹配的文章");
                }
                
                decodedUrl = article.getUrl(); // 使用数据库中实际存储的URL
            }

            System.out.println("找到文章，准备删除，URL: " + decodedUrl);

            // 执行逻辑删除（更新is_deleted标志为true）
            int result = articleMapper.logicalDeleteByUrl(decodedUrl);
            System.out.println("数据库逻辑删除结果: " + result);

            if (result > 0) {
                try {
                    // 从Lucene索引中删除（物理删除索引）
                    luceneIndexService.deleteFromIndex(decodedUrl);
                    System.out.println("索引删除成功");
                    
                    // 重建索引以确保一致性
                    luceneIndexService.rebuildIndex();
                    System.out.println("索引重建成功");
                    
                    return ResponseEntity.ok("文章删除成功");
                } catch (Exception e) {
                    System.err.println("索引操作失败: " + e.getMessage());
                    // 由于使用了@Transactional，如果发生异常会自动回滚数据库操作
                    throw new RuntimeException("删除文章时发生错误: " + e.getMessage());
                }
            } else {
                return ResponseEntity.status(500).body("数据库删除失败");
            }
        } catch (Exception e) {
            System.err.println("删除操作发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("删除失败：" + e.getMessage());
        }
    }

    // 获取文章HTML内容
    @GetMapping("/{id}/html")
    public ResponseEntity<?> getArticleHtml(@PathVariable Integer id) {
        try {
            logger.info("接收到获取文章HTML请求, ID: {}", id);
            
            // 获取文章基本信息
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                return ResponseEntity.notFound().build();
            }
            
            // 获取HTML内容，访问MySQL的article_full_html表，使用article_id作为查询参数
            String fullHtml = articleMapper.getArticleHtml(id);
            if (fullHtml == null) {
                logger.warn("未找到ID为{}的文章HTML内容", id);
                return ResponseEntity.status(404).body("未找到文章HTML内容");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("title", article.getTitle());
            response.put("ulid", article.getUlid());
            response.put("fullHtml", fullHtml);
            
            logger.info("成功获取文章HTML, ID: {}, HTML长度: {}", id, fullHtml.length());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取文章HTML失败, ID: " + id, e);
            return ResponseEntity.status(500)
                .body("获取文章HTML失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章头图
     * 根据文章标题查找对应的头图并返回
     * 更新支持ULID格式的图片路径
     */
    @GetMapping("/image/{articleId}")
    public ResponseEntity<?> getArticleImage(@PathVariable Integer articleId) {
        try {
            logger.info("接收到获取文章头图请求, ID: {}", articleId);
            
            // 获取文章基本信息
            Article article = articleMapper.findById(articleId);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", articleId);
                return ResponseEntity.notFound().build();
            }
            
            // 检查文章是否有ULID和图片
            String ulid = article.getUlid();
            if (ulid == null || ulid.isEmpty()) {
                logger.warn("文章没有ULID: ID={}", articleId);
                
                // 兼容旧版标题方式
                // 处理文章标题，移除特殊字符
                String safeFolderName = article.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
            
                // 构建头图路径
                String headImageName = safeFolderName + "_head.jpg";
                File imageFolder = new File("image/" + safeFolderName);
                File headImageFile = new File(imageFolder, headImageName);
                
                // 检查头图是否存在
                if (!headImageFile.exists()) {
                    logger.warn("未找到文章头图: {}", headImageFile.getAbsolutePath());
                    return ResponseEntity.status(404).body("未找到文章头图");
                }
                
                // 头图URL
                String imageUrl = "/api/crawler/images/" + safeFolderName + "/" + headImageName;
                
                Map<String, String> response = new HashMap<>();
                response.put("imageUrl", imageUrl);
                
                logger.info("成功获取文章头图URL(旧版): {}", imageUrl);
                return ResponseEntity.ok(response);
            }
            
            // 使用ULID方式 - 解析图片路径
            String images = article.getImages();
            if (images == null || images.isEmpty()) {
                logger.warn("文章没有图片: ID={}", articleId);
                return ResponseEntity.status(404).body("未找到文章头图");
            }
            
            // 解析图片路径，使用第一张图片作为头图
            String[] imagePaths = images.split(",");
            if (imagePaths.length == 0) {
                logger.warn("文章图片路径解析错误: ID={}", articleId);
                return ResponseEntity.status(404).body("未找到文章头图");
            }
            
            String firstImagePath = imagePaths[0];
            
            // 检查图片路径格式
            String imageUrl;
            if (firstImagePath.contains("/")) {
                // 新格式: articleUlid/imageUlid.jpg
                imageUrl = "/api/images/" + firstImagePath;
                logger.info("使用新API路径获取头图: {}", imageUrl);
            } else {
                // 旧格式或不符合要求的格式，尝试使用旧的路径格式
                imageUrl = firstImagePath; // 直接使用存储的路径
                logger.info("使用旧格式图片路径: {}", imageUrl);
            }
            
            // 返回成功响应
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            
            logger.info("成功获取文章头图URL: {}", imageUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取文章头图失败, ID: " + articleId, e);
            return ResponseEntity.status(500)
                .body("获取文章头图失败: " + e.getMessage());
        }
    }
    
    /**
     * 直接提供图片访问
     */
    @GetMapping("/images/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String folder,
            @PathVariable String filename) {
        try {
            File imageFile = new File("image/" + folder, filename);
            if (!imageFile.exists()) {
                logger.warn("未找到图片文件: {}", imageFile.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            Resource resource = new org.springframework.core.io.FileSystemResource(imageFile);
            
            // 确定内容类型
            String contentType = "image/jpeg"; // 默认为JPEG
            String lowerFilename = filename.toLowerCase();
            
            if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lowerFilename.endsWith(".bmp")) {
                contentType = "image/bmp";
            } else if (lowerFilename.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }
            
            logger.info("提供图片访问: {}, Content-Type: {}", imageFile.getPath(), contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("获取图片失败: " + folder + "/" + filename, e);
            return ResponseEntity.status(500).build();
        }
    }
} 