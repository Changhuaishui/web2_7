package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 标签控制器
 * 提供文章标签的相关接口
 * RestController是Spring框架提供的注解，用于处理前端GET请求，返回json
 * 
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    
    // 添加一个标记，用于避免重复预热缓存
    private final AtomicBoolean cacheWarming = new AtomicBoolean(false);

    @Autowired
    private TagService tagService;
    
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 从tagService获取所有可用标签
     */
    @GetMapping
    /*
     * @GetMapping是Spring框架提供的注解，用于处理前端GET请求
     * 这里是返回一个包含所有标签json的列表，给前端使用
     */
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
    
    /**
     * 重新预热标签缓存
     * 管理员接口，用于手动触发缓存预热
     */
    @PostMapping("/warm-cache")
    public ResponseEntity<?> warmTagCache() {
        try {
            if (cacheWarming.compareAndSet(false, true)) {
                logger.info("手动触发标签缓存预热");
                
                // 在新线程中执行预热操作，避免阻塞请求
                new Thread(() -> {
                    try {
                        warmCache();
                    } finally {
                        cacheWarming.set(false);
                    }
                }).start();
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "标签缓存预热已启动，将在后台执行"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "标签缓存预热已在进行中，请稍后再试"
                ));
            }
        } catch (Exception e) {
            logger.error("触发标签缓存预热失败", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                        "success", false,
                        "message", "预热失败: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * 应用启动时自动预热标签缓存
     */
    @PostConstruct
    public void initTagCache() {
        logger.info("系统启动，准备预热标签缓存");
        
        // 在新线程中执行预热操作，避免阻塞应用启动
        new Thread(() -> {
            try {
                // 等待3秒，确保应用完全启动
                Thread.sleep(3000);
                
                if (cacheWarming.compareAndSet(false, true)) {
                    // 首先清除缓存，确保使用最新的匹配阈值
                    tagService.clearCache();
                    logger.info("已清除旧的标签缓存，准备使用新阈值预热");
                    
                    // 预热缓存
                    warmCache();
                }
            } catch (Exception e) {
                logger.error("自动预热标签缓存失败", e);
            } finally {
                cacheWarming.set(false);
            }
        }).start();
    }
    
    /**
     * 每天自动刷新标签缓存
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledCacheRefresh() {
        logger.info("定时任务：刷新标签缓存");
        
        if (cacheWarming.compareAndSet(false, true)) {
            try {
                // 清除旧缓存
                tagService.clearCache();
                
                // 重新预热
                warmCache();
            } catch (Exception e) {
                logger.error("定时刷新标签缓存失败", e);
            } finally {
                cacheWarming.set(false);
            }
        } else {
            logger.warn("另一个缓存预热任务正在执行，跳过本次定时刷新");
        }
    }
    
    /**
     * 预热缓存的具体实现方法
     */
    private void warmCache() {
        logger.info("开始预热标签缓存");
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取所有文章
            List<Article> allArticles = articleMapper.findAllOrderByPublishTime();
            logger.info("共获取到{}篇文章", allArticles.size());
            
            // 获取所有标签
            List<String> allTags = tagService.getAllTags();
            
            // 为每篇文章提取标签
            int count = 0;
            for (Article article : allArticles) {
                try {
                    tagService.extractTags(article);
                    count++;
                    
                    // 每处理100篇文章打印一次日志
                    if (count % 100 == 0) {
                        logger.info("已预热{}篇文章的标签", count);
                    }
                } catch (Exception e) {
                    logger.error("预热文章ID={}的标签失败", article.getId(), e);
                }
            }
            
            // 为每个标签预热文章列表
            for (String tag : allTags) {
                try {
                    List<Article> tagArticles = tagService.filterArticlesByTag(allArticles, tag);
                    logger.info("标签「{}」关联了{}篇文章", tag, tagArticles.size());
                } catch (Exception e) {
                    logger.error("预热标签「{}」的文章列表失败", tag, e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("标签缓存预热完成，共预热{}篇文章，耗时{}毫秒", allArticles.size(), (endTime - startTime));
        } catch (Exception e) {
            logger.error("预热标签缓存过程中出错", e);
        }
    }

    /**
     * 强制刷新标签缓存
     * 清除所有缓存并使用新的阈值重新计算
     */
    @PostMapping("/refresh-cache")
    public ResponseEntity<?> forceRefreshCache() {
        try {
            if (cacheWarming.compareAndSet(false, true)) {
                logger.info("强制刷新标签缓存，使用新的阈值");
                
                // 在新线程中执行刷新操作，避免阻塞请求
                new Thread(() -> {
                    try {
                        // 清除缓存
                        tagService.clearCache();
                        logger.info("已清除所有标签缓存");
                        
                        // 重新预热
                        warmCache();
                    } finally {
                        cacheWarming.set(false);
                    }
                }).start();
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "标签缓存强制刷新已启动，将使用新的阈值重新计算"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "另一个缓存操作正在进行中，请稍后再试"
                ));
            }
        } catch (Exception e) {
            logger.error("强制刷新标签缓存失败", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                        "success", false,
                        "message", "刷新失败: " + e.getMessage()
                    ));
        }
    }
} 