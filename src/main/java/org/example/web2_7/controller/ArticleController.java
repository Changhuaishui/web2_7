package org.example.web2_7.controller;

import org.example.web2_7.pojo.Article;
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 * 提供文章相关API
 */
@RestController
@RequestMapping("/api/articles")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ArticleController {
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private ArticleService articleService;

    /**
     * 获取所有文章（按发布时间倒序）
     */
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        try {
            List<Article> articles = articleMapper.findAllOrderByPublishTime();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            logger.error("获取文章列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ULID获取文章
     */
    @GetMapping("/{ulid}")
    public ResponseEntity<Article> getArticleByUlid(@PathVariable("ulid") String ulid) {
        try {
            Article article = articleMapper.findByUlid(ulid);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            logger.error("获取文章失败: ULID={}", ulid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文章图片信息
     * 返回文章ULID和关联的图片ULID列表
     */
    @GetMapping("/{ulid}/images")
    public ResponseEntity<Map<String, Object>> getArticleImages(@PathVariable("ulid") String ulid) {
        try {
            Article article = articleMapper.findByUlid(ulid);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }

            // 解析图片路径
            String imagesStr = article.getImages();
            List<Map<String, String>> imageList = new ArrayList<>();

            if (imagesStr != null && !imagesStr.isEmpty()) {
                String[] imagePaths = imagesStr.split(",");
                for (String path : imagePaths) {
                    if (path.contains("/")) {
                        // 解析路径格式：articleUlid/imageUlid.jpg
                        String[] parts = path.split("/");
                        if (parts.length == 2) {
                            String imageUlid = parts[1];
                            // 移除扩展名
                            if (imageUlid.contains(".")) {
                                imageUlid = imageUlid.substring(0, imageUlid.lastIndexOf('.'));
                            }
                            
                            Map<String, String> imageInfo = new HashMap<>();
                            imageInfo.put("ulid", imageUlid);
                            imageInfo.put("url", "/api/images/" + ulid + "/" + imageUlid);
                            imageList.add(imageInfo);
                        }
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("articleUlid", ulid);
            result.put("title", article.getTitle());
            result.put("images", imageList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取文章图片信息失败: ULID={}", ulid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有文章的ULID和图片ULID列表
     * 提供给前端批量获取图片信息
     */
    @GetMapping("/images")
    public ResponseEntity<List<Map<String, Object>>> getAllArticleImages() {
        try {
            List<Article> articles = articleMapper.findAllOrderByPublishTime();
            List<Map<String, Object>> results = new ArrayList<>();

            for (Article article : articles) {
                String ulid = article.getUlid();
                if (ulid == null || ulid.isEmpty()) {
                    continue;
                }

                Map<String, Object> articleInfo = new HashMap<>();
                articleInfo.put("articleUlid", ulid);
                articleInfo.put("title", article.getTitle());

                // 解析图片路径
                String imagesStr = article.getImages();
                List<Map<String, String>> imageList = new ArrayList<>();

                if (imagesStr != null && !imagesStr.isEmpty()) {
                    String[] imagePaths = imagesStr.split(",");
                    for (String path : imagePaths) {
                        if (path.contains("/")) {
                            // 解析路径格式：articleUlid/imageUlid.jpg
                            String[] parts = path.split("/");
                            if (parts.length == 2) {
                                String imageUlid = parts[1];
                                // 移除扩展名
                                if (imageUlid.contains(".")) {
                                    imageUlid = imageUlid.substring(0, imageUlid.lastIndexOf('.'));
                                }
                                
                                Map<String, String> imageInfo = new HashMap<>();
                                imageInfo.put("ulid", imageUlid);
                                imageInfo.put("url", "/api/images/" + ulid + "/" + imageUlid);
                                imageList.add(imageInfo);
                            }
                        }
                    }
                }

                articleInfo.put("images", imageList);
                results.add(articleInfo);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("获取所有文章图片信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文章HTML内容
     * 返回处理后的HTML内容（图片URL已替换为本地路径）
     */
    @GetMapping("/{ulid}/html")
    public ResponseEntity<Map<String, Object>> getArticleHtml(@PathVariable("ulid") String ulid) {
        try {
            Article article = articleMapper.findByUlid(ulid);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取处理后的HTML内容
            String processedHtml = articleService.getProcessedArticleHtml(article.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("ulid", ulid);
            result.put("title", article.getTitle());
            result.put("html", processedHtml);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取文章HTML内容失败: ULID={}", ulid, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取原始文章HTML内容
     * 返回未处理的原始HTML内容
     */
    @GetMapping("/{ulid}/raw-html")
    public ResponseEntity<Map<String, Object>> getArticleRawHtml(@PathVariable("ulid") String ulid) {
        try {
            Article article = articleMapper.findByUlid(ulid);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取原始HTML内容
            String rawHtml = articleService.getArticleHtml(article.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("ulid", ulid);
            result.put("title", article.getTitle());
            result.put("html", rawHtml);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取文章原始HTML内容失败: ULID={}", ulid, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 