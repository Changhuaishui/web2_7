package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.pojo.RelatedArticle;
import org.example.web2_7.service.DeepSeekService;
import org.example.web2_7.service.RelatedArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章摘要控制器
 * 提供文章摘要生成、关键词提取和相关文章功能
 */
@RestController
@RequestMapping("/api/articles")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class SummaryController {
    
    private static final Logger logger = LoggerFactory.getLogger(SummaryController.class);
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private RelatedArticleService relatedArticleService;
    
    /**
     * 为特定文章生成摘要
     * @param id 文章ID
     * @param force 是否强制重新生成摘要和关键词，忽略现有值
     * @return 包含摘要的响应
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<Map<String, Object>> generateSummary(
            @PathVariable("id") Integer id,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
        
        logger.info("开始为文章ID:{}生成摘要, 强制模式: {}", id, force);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取文章
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                response.put("success", false);
                response.put("message", "文章不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文章是否已有摘要，且不是强制模式
            if (!force && article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
                logger.info("文章ID:{}已有摘要，直接返回", id);
                response.put("success", true);
                response.put("summary", article.getSummary());
                response.put("message", "文章已有摘要");
                response.put("isExisting", true);
                return ResponseEntity.ok(response);
            }
            
            // 检查文章内容
            String content = article.getContent();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("文章ID:{}内容为空，无法生成摘要", id);
                response.put("success", false);
                response.put("message", "文章内容为空，无法生成摘要");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 同时生成摘要和关键词
            String[] result = deepSeekService.generateSummaryAndKeywords(content);
            String summary = result[0];
            String keywords = result[1];
            
            // 保存摘要和关键词
            int updateResult = articleMapper.updateArticleSummaryAndKeywords(id, summary, keywords);
            if (updateResult > 0) {
                logger.info("文章ID:{}摘要和关键词生成成功并保存到数据库", id);
                response.put("success", true);
                response.put("summary", summary);
                response.put("keywords", keywords);
                response.put("message", "摘要和关键词生成成功");
                response.put("isExisting", false);
                
                // 尝试爬取相关文章
                try {
                    int relatedCount = relatedArticleService.crawlAndSaveRelatedArticles(id, keywords);
                    logger.info("为文章ID:{}爬取并保存了{}篇相关文章", id, relatedCount);
                    response.put("relatedArticlesCount", relatedCount);
                } catch (Exception e) {
                    logger.error("爬取相关文章时发生错误", e);
                    response.put("relatedArticlesError", e.getMessage());
                }
                
                return ResponseEntity.ok(response);
            } else {
                logger.error("文章ID:{}摘要和关键词保存失败", id);
                response.put("success", false);
                response.put("message", "摘要和关键词保存失败");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            logger.error("为文章ID:{}生成摘要和关键词时发生错误", id, e);
            response.put("success", false);
            response.put("message", "生成失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取文章关键词
     * @param id 文章ID
     * @return 包含关键词的响应
     */
    @GetMapping("/{id}/keywords")
    public ResponseEntity<Map<String, Object>> getKeywords(@PathVariable("id") Integer id) {
        logger.info("获取文章ID:{}的关键词", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取文章
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                response.put("success", false);
                response.put("message", "文章不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文章是否已有关键词
            if (article.getKeywords() != null && !article.getKeywords().trim().isEmpty()) {
                logger.info("返回文章ID:{}的现有关键词", id);
                response.put("success", true);
                response.put("keywords", article.getKeywords());
                return ResponseEntity.ok(response);
            } else {
                // 需要生成关键词
                String content = article.getContent();
                if (content == null || content.trim().isEmpty()) {
                    logger.warn("文章ID:{}内容为空，无法生成关键词", id);
                    response.put("success", false);
                    response.put("message", "文章内容为空，无法生成关键词");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 生成关键词
                String keywords = deepSeekService.generateKeywords(content);
                
                // 保存关键词
                int result = articleMapper.updateArticleKeywords(id, keywords);
                if (result > 0) {
                    logger.info("文章ID:{}关键词生成成功并保存到数据库", id);
                    response.put("success", true);
                    response.put("keywords", keywords);
                    response.put("message", "关键词生成成功");
                    return ResponseEntity.ok(response);
                } else {
                    logger.error("文章ID:{}关键词保存失败", id);
                    response.put("success", false);
                    response.put("message", "关键词保存失败");
                    return ResponseEntity.internalServerError().body(response);
                }
            }
            
        } catch (Exception e) {
            logger.error("获取文章ID:{}关键词时发生错误", id, e);
            response.put("success", false);
            response.put("message", "获取关键词失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取文章的相关文章
     * @param id 文章ID
     * @return 包含相关文章的响应
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<Map<String, Object>> getRelatedArticles(@PathVariable("id") Integer id) {
        logger.info("获取文章ID:{}的相关文章", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取文章
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                response.put("success", false);
                response.put("message", "文章不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取相关文章
            List<RelatedArticle> relatedArticles = relatedArticleService.getRelatedArticles(id);
            
            response.put("success", true);
            response.put("relatedArticles", relatedArticles);
            response.put("count", relatedArticles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取文章ID:{}相关文章时发生错误", id, e);
            response.put("success", false);
            response.put("message", "获取相关文章失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 为文章爬取相关文章
     * @param id 文章ID
     * @return 包含爬取结果的响应
     */
    @PostMapping("/{id}/crawl-related")
    public ResponseEntity<Map<String, Object>> crawlRelatedArticles(@PathVariable("id") Integer id) {
        logger.info("为文章ID:{}爬取相关文章", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取文章
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                response.put("success", false);
                response.put("message", "文章不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文章是否有关键词
            String keywords = article.getKeywords();
            if (keywords == null || keywords.trim().isEmpty()) {
                // 需要先生成关键词
                String content = article.getContent();
                if (content == null || content.trim().isEmpty()) {
                    logger.warn("文章ID:{}内容为空，无法生成关键词", id);
                    response.put("success", false);
                    response.put("message", "文章内容为空，无法生成关键词");
                    return ResponseEntity.badRequest().body(response);
                }
                
                keywords = deepSeekService.generateKeywords(content);
                articleMapper.updateArticleKeywords(id, keywords);
                logger.info("为文章ID:{}生成了关键词:{}", id, keywords);
            }
            
            // 爬取相关文章
            int count = relatedArticleService.crawlAndSaveRelatedArticles(id, keywords);
            
            response.put("success", true);
            response.put("count", count);
            response.put("message", "成功爬取" + count + "篇相关文章");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("为文章ID:{}爬取相关文章时发生错误", id, e);
            response.put("success", false);
            response.put("message", "爬取相关文章失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 为文章生成关键词
     * @param id 文章ID
     * @return 包含关键词的响应
     */
    @PostMapping("/{id}/generate-keywords")
    public ResponseEntity<Map<String, Object>> generateKeywords(@PathVariable("id") Integer id) {
        logger.info("为文章ID:{}生成关键词", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取文章
            Article article = articleMapper.findById(id);
            if (article == null) {
                logger.warn("未找到ID为{}的文章", id);
                response.put("success", false);
                response.put("message", "文章不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文章内容
            String content = article.getContent();
            if (content == null || content.trim().isEmpty()) {
                logger.warn("文章ID:{}内容为空，无法生成关键词", id);
                response.put("success", false);
                response.put("message", "文章内容为空，无法生成关键词");
                return ResponseEntity.badRequest().body(response);
            }
            
            try {
                // 生成关键词
                String keywords = deepSeekService.generateKeywords(content);
                
                if (keywords == null || keywords.trim().isEmpty()) {
                    logger.warn("API返回的关键词为空");
                    response.put("success", false);
                    response.put("message", "生成的关键词为空");
                    return ResponseEntity.ok(response);
                }
                
                // 保存关键词
                int updateResult = articleMapper.updateArticleKeywords(id, keywords);
                if (updateResult > 0) {
                    logger.info("文章ID:{}关键词生成成功并保存到数据库: {}", id, keywords);
                    response.put("success", true);
                    response.put("keywords", keywords);
                    response.put("message", "关键词生成成功");
                    
                    // 尝试爬取相关文章
                    try {
                        int relatedCount = relatedArticleService.crawlAndSaveRelatedArticles(id, keywords);
                        logger.info("为文章ID:{}爬取并保存了{}篇相关文章", id, relatedCount);
                        response.put("relatedArticlesCount", relatedCount);
                    } catch (Exception e) {
                        logger.error("爬取相关文章时发生错误", e);
                        response.put("relatedArticlesError", e.getMessage());
                    }
                    
                    return ResponseEntity.ok(response);
                } else {
                    logger.error("文章ID:{}关键词保存失败", id);
                    response.put("success", false);
                    response.put("message", "关键词保存失败");
                    return ResponseEntity.internalServerError().body(response);
                }
            } catch (Exception e) {
                logger.error("为文章ID:{}生成关键词时出错", id, e);
                response.put("success", false);
                response.put("message", "生成关键词出错: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            logger.error("为文章ID:{}生成关键词时发生错误", id, e);
            response.put("success", false);
            response.put("message", "生成失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 