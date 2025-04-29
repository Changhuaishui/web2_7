package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.DeepSeekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章摘要控制器
 * 提供文章摘要生成功能
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
    
    /**
     * 为特定文章生成摘要
     * @param id 文章ID
     * @return 包含摘要的响应
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<Map<String, Object>> generateSummary(@PathVariable("id") Integer id) {
        logger.info("开始为文章ID:{}生成摘要", id);
        
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
            
            // 检查文章是否已有摘要
            if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
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
            
            // 生成摘要
            String summary = deepSeekService.summarizeText(content);
            
            // 保存摘要
            int result = articleMapper.updateArticleSummary(id, summary);
            if (result > 0) {
                logger.info("文章ID:{}摘要生成成功并保存到数据库", id);
                response.put("success", true);
                response.put("summary", summary);
                response.put("message", "摘要生成成功");
                response.put("isExisting", false);
                return ResponseEntity.ok(response);
            } else {
                logger.error("文章ID:{}摘要保存失败", id);
                response.put("success", false);
                response.put("message", "摘要保存失败");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            logger.error("为文章ID:{}生成摘要时发生错误", id, e);
            response.put("success", false);
            response.put("message", "生成摘要失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 