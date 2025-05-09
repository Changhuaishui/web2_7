package org.example.web2_7.service;

import org.example.web2_7.pojo.RelatedArticle;
import java.util.List;

/**
 * 相关文章服务接口
 * 用于处理相关文章的爬取和管理
 */
public interface RelatedArticleService {
    
    /**
     * 爬取并保存相关文章
     * @param articleId 文章ID
     * @param keywords 关键词，用于搜索相关文章
     * @return 爬取的相关文章数量
     */
    int crawlAndSaveRelatedArticles(Integer articleId, String keywords);
    
    /**
     * 获取文章的相关文章列表
     * @param articleId 文章ID
     * @return 相关文章列表
     */
    List<RelatedArticle> getRelatedArticles(Integer articleId);
    
    /**
     * 添加相关文章
     * @param articleId 文章ID
     * @param relatedUrl 相关文章URL
     * @param title 相关文章标题
     * @return 是否添加成功
     */
    boolean addRelatedArticle(Integer articleId, String relatedUrl, String title);
    
    /**
     * 删除文章的所有相关文章
     * @param articleId 文章ID
     * @return 是否删除成功
     */
    boolean deleteRelatedArticles(Integer articleId);
} 