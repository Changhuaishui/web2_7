package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签控制器
 * 提供文章标签的相关接口
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagService tagService;
    
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 获取所有可用标签
     */
    @GetMapping
    public ResponseEntity<List<String>> getAllTags() {
        logger.info("获取所有标签");
        List<String> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取文章的标签
     */
    @GetMapping("/article/{id}")
    public ResponseEntity<?> getArticleTags(@PathVariable Integer id) {
        try {
            logger.info("获取文章ID={}的标签", id);
            
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                return ResponseEntity.notFound().build();
            }
            
            List<String> tags = tagService.extractTags(article);
            
            Map<String, Object> response = new HashMap<>();
            response.put("articleId", id);
            response.put("title", article.getTitle());
            response.put("tags", tags);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取文章标签失败", e);
            return ResponseEntity.status(500)
                    .body("获取文章标签失败: " + e.getMessage());
        }
    }

    /**
     * 根据标签筛选文章
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterArticlesByTag(@RequestParam String tag) {
        try {
            logger.info("根据标签「{}」筛选文章", tag);
            
            List<Article> allArticles = articleMapper.findAllOrderByPublishTime();
            List<Article> filteredArticles = tagService.filterArticlesByTag(allArticles, tag);
            
            return ResponseEntity.ok(filteredArticles);
        } catch (Exception e) {
            logger.error("根据标签筛选文章失败", e);
            return ResponseEntity.status(500)
                    .body("筛选文章失败: " + e.getMessage());
        }
    }
} 