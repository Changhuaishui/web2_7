package org.example.web2_7.crawler;
/*
 * @author chen
 * 数据管道实现
 * 用于处理爬取的数据并将其存储到数据库中，同时创建搜索索引。
 * 爬标题、作者、发布时间、内容、图片链接
 * -->
 * 验证时间（我这里爬发布时间会有识别不了的小bug，若不符合预期则设置为当前时间。）
 * -->
 * 将图片链接列表转换为逗号分隔的字符串并存储（暂未打算如何处理图片，打算后期优化前端界面时，展示公众号图片，暂未实现）
 * -->
 * 将爬取的数据插入到数据库中（ArticleMapper ），同时创建搜索索引（SearchService ）。
 */
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.ArticleSearchService;
import org.example.web2_7.service.LuceneIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DatabasePipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePipeline.class);

    @Autowired  // 插入数据库
    private ArticleMapper articleMapper;

    @Autowired  // 创建搜索索引
    private ArticleSearchService searchService;
    
    @Autowired  // Lucene索引服务
    private LuceneIndexService luceneIndexService;

    // 日期时间格式正则表达式
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}.*");

    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            Article article = new Article();
            
            // 获取必需字段
            String url = resultItems.get("url");
            String title = resultItems.get("title");
            String content = resultItems.get("content");
            String fullHtml = resultItems.get("fullHtml");
            
            // 验证必需字段
            if (url == null || url.isEmpty()) {
                logger.error("URL cannot be null or empty");
                return;
            }
            if (title == null || title.isEmpty()) {
                logger.error("Title cannot be null or empty for URL: {}", url);
                return;
            }
            if (content == null || content.isEmpty()) {
                logger.error("Content cannot be null or empty for URL: {}", url);
                return;
            }
            if (fullHtml == null || fullHtml.isEmpty()) {
                logger.error("Full HTML cannot be null or empty for URL: {}", url);
                return;
            }

            // 设置必需字段
            article.setUrl(url);
            article.setTitle(title);
            article.setContent(content);

            // 设置原文链接
            String sourceUrl = resultItems.get("sourceUrl");
            if (sourceUrl != null && !sourceUrl.isEmpty() && !sourceUrl.equals("javascript:;")) {
                article.setSourceUrl(sourceUrl);
                logger.info("Setting source URL: {}", sourceUrl);
            } else {
                logger.warn("No valid source URL found for article: {}", url);
            }

            // 设置可选字段
            article.setAuthor(resultItems.get("author"));
            article.setAccountName(resultItems.get("accountName"));

            // 处理发布时间
            String publishTimeStr = resultItems.get("publishTime");
            if (publishTimeStr != null && !publishTimeStr.isEmpty()) {
                try {
                    LocalDateTime publishTime = LocalDateTime.parse(publishTimeStr, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    article.setPublishTime(publishTime);
                } catch (Exception e) {
                    logger.warn("Failed to parse publish time: {}, setting to null", publishTimeStr);
                    article.setPublishTime(null);
                }
            } else {
                logger.warn("No publish time found, setting to null");
                article.setPublishTime(null);
            }

            // 处理图片
            List<String> imageUrls = resultItems.get("imageUrls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                article.setImages(String.join(",", imageUrls));
            }
            
            // 处理头图
            String headImageUrl = resultItems.get("headImageUrl");
            if (headImageUrl != null && !headImageUrl.isEmpty()) {
                // 如果有专门的头图字段，则设置
                // article.setHeadImage(headImageUrl);
                
                // 如果没有专门的头图字段，可以将头图添加到图片列表的开头
                if (article.getImages() == null || article.getImages().isEmpty()) {
                    article.setImages(headImageUrl);
                } else {
                    article.setImages(headImageUrl + "," + article.getImages());
                }
                
                logger.info("添加文章头图: {}", headImageUrl);
            }

            // 检查文章是否已存在
            Article existingArticle = articleMapper.findByUrl(url);
            if (existingArticle != null) {
                logger.info("Article already exists: {}", url);
                return;
            }

            // 插入文章基本信息
            int result = articleMapper.insertArticle(article);
            if (result > 0) {
                // 获取新插入文章的ID
                Article insertedArticle = articleMapper.findByUrl(url);
                if (insertedArticle != null) {
                    // 插入HTML内容
                    Integer articleId = insertedArticle.getId();
                    logger.info("Inserting HTML content for article ID: {}", articleId);
                    articleMapper.insertArticleHtml(articleId, fullHtml);
                    logger.info("Successfully inserted article and HTML content for URL: {}", url);
                    
                    // 添加到搜索索引
                    try {
                        // 更新ArticleSearchService索引
                        searchService.updateArticleIndex(insertedArticle);
                        logger.info("Updated ArticleSearchService index for article ID: {}", articleId);
                        
                        // 更新LuceneIndexService索引
                        luceneIndexService.addToIndex(
                            insertedArticle.getTitle(),
                            insertedArticle.getContent(),
                            insertedArticle.getUrl()
                        );
                        logger.info("Updated LuceneIndexService index for article ID: {}", articleId);
                    } catch (Exception e) {
                        logger.error("Failed to update search index for article ID: {}", articleId, e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing article", e);
        }
    }
}