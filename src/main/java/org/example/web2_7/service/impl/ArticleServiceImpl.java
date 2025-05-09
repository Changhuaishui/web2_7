package org.example.web2_7.service.impl;

import org.example.web2_7.Dao.ArticleDao;
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.ArticleService;
import org.example.web2_7.utils.HtmlImageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/*@Service 注解用于标识一个类是 Spring 框架中的服务类，
 * 此后，Spring 框架会自动将
 *  ArticleServiceImpl 注入到需要使用 ArticleService 的地方，
 * 实现了接口和实现类的解耦。
 * 
 */
public class ArticleServiceImpl implements ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    @Autowired
    /*
     * @Autowired 注解用于自动装配 Bean，
     * 此处是注入 ArticleDao的，
     * 具体是ArticleDao的findById方法，
     *
     * 
     * 
     */
    private ArticleDao articleDao;
    
    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public Article getArticleById(Integer id) {
        return articleDao.findById(id);
    }
    
    @Override
    public String getArticleHtml(Integer articleId) {
        return articleMapper.getArticleHtml(articleId);
    }
    
    @Override
    public String getProcessedArticleHtml(Integer articleId) {
        try {
            // 获取原始HTML内容
            String html = articleMapper.getArticleHtml(articleId);
            
            // 获取URL映射
            String urlMapping = articleMapper.getArticleUrlMapping(articleId);
            
            // 如果没有URL映射，直接返回原始HTML
            if (urlMapping == null || urlMapping.isEmpty()) {
                logger.warn("No URL mapping found for article ID: {}", articleId);
                return html;
            }
            
            // 处理HTML内容，替换图片URL
            String processedHtml = HtmlImageProcessor.replaceImageUrls(html, urlMapping);
            logger.info("Successfully processed HTML content for article ID: {}", articleId);
            
            return processedHtml;
        } catch (Exception e) {
            logger.error("Error processing article HTML: ", e);
            // 出错时返回原始HTML
            return articleMapper.getArticleHtml(articleId);
        }
    }
} 