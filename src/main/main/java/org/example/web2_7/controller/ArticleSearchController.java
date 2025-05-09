package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.ArticleSearchService;
import org.example.web2_7.service.LuceneIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文章搜索控制器
 * 提供文章搜索相关API
 * 为文章搜索相关的操作提供统一的访问入口，
 * 使得客户端可以通过该路径访问和操作文章搜索资源。
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class ArticleSearchController {
    private static final Logger logger = LoggerFactory.getLogger(ArticleSearchController.class);
    
    private final ArticleSearchService searchService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private LuceneIndexService luceneIndexService;

    @Autowired
    public ArticleSearchController(ArticleSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<?> searchArticles(@RequestParam String keyword) {
        try {
            logger.info("搜索文章，关键词: {}", keyword);
            List<Article> results = searchService.searchArticles(keyword);
            logger.info("搜索结果数量: {}", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("搜索失败: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 重建所有文章的搜索索引
     * 用于修复搜索功能问题
     */
    @PostMapping("/rebuild-index")
    public ResponseEntity<?> rebuildSearchIndex() {
        try {
            logger.info("开始重建搜索索引...");
            
            // 获取所有文章
            List<Article> articles = articleMapper.findAllOrderByPublishTime();
            logger.info("获取到 {} 篇文章", articles.size());
            
            // 重建ArticleSearchService索引
            searchService.rebuildIndex(articles);
            logger.info("成功重建ArticleSearchService索引");
            
            // 重建LuceneIndexService索引
            luceneIndexService.rebuildIndex();
            logger.info("成功重建LuceneIndexService索引");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成功重建" + articles.size() + "篇文章的搜索索引",
                "count", articles.size()
            ));
        } catch (Exception e) {
            logger.error("重建索引失败: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
} 