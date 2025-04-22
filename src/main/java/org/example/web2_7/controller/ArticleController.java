package org.example.web2_7.controller;

import org.example.web2_7.pojo.Article;
import org.example.web2_7.Dao.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ArticleController {

    @Autowired
    private ArticleMapper articleMapper;

    @GetMapping
    public ResponseEntity<List<Article>> getArticles() {
        try {
            List<Article> articles = articleMapper.findAllOrderByPublishTime();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable String id) {
        try {
            Integer articleId = Integer.parseInt(id);
            Article article = articleMapper.findById(articleId);
            if (article != null) {
                return ResponseEntity.ok(article);
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getArticleDetail(@PathVariable String id) {
        try {
            Integer articleId = Integer.parseInt(id);
            Article article = articleMapper.findById(articleId);
            
            if (article == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取文章HTML内容
            String fullHtml = articleMapper.getArticleHtml(articleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", article.getId());
            response.put("title", article.getTitle());
            response.put("author", article.getAuthor());
            response.put("publishTime", article.getPublishTime());
            response.put("content", article.getContent());
            response.put("url", article.getUrl());
            response.put("sourceUrl", article.getSourceUrl());
            response.put("accountName", article.getAccountName());
            
            // 如果有HTML内容，则添加到响应中
            if (fullHtml != null && !fullHtml.isEmpty()) {
                response.put("fullHtml", fullHtml);
            }

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 