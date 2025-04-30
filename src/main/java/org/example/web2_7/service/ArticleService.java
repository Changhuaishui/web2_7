package org.example.web2_7.service;

import org.example.web2_7.pojo.Article;

public interface ArticleService {
    Article getArticleById(Integer id);
    
    /**
     * 获取文章原始HTML内容
     * @param articleId 文章ID
     * @return 原始HTML内容
     */
    String getArticleHtml(Integer articleId);
    
    /**
     * 获取处理后的文章HTML内容（图片URL已替换为本地路径）
     * @param articleId 文章ID
     * @return 处理后的HTML内容
     */
    String getProcessedArticleHtml(Integer articleId);
} 