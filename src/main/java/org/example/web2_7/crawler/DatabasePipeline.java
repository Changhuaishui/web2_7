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
import org.example.web2_7.service.DeepSeekService;
import org.example.web2_7.service.LuceneIndexService;
import org.example.web2_7.utils.UlidUtils;
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
import java.util.Map;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class DatabasePipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePipeline.class);

    @Autowired  // 插入数据库
    private ArticleMapper articleMapper;

    @Autowired  // 创建搜索索引
    private ArticleSearchService searchService;
    
    @Autowired  // Lucene索引服务
    private LuceneIndexService luceneIndexService;
    
    @Autowired  // DeepSeek服务
    private DeepSeekService deepSeekService;

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

            // 获取文章ULID
            String ulid = resultItems.get("ulid");
            if (ulid == null || ulid.isEmpty()) {
                // 如果没有生成ULID，在这里生成一个
                ulid = UlidUtils.generate();
            }
            
            // 设置必需字段
            article.setUrl(url);
            article.setTitle(title);
            article.setContent(content);
            article.setUlid(ulid);

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

            // 处理图片 - 现在使用ULID作为图片标识
            List<String> imageUlids = resultItems.get("imageUlids");
            Map<String, String> imageUlidMap = resultItems.get("imageUlidMap");
            
            if (imageUlids != null && !imageUlids.isEmpty()) {
                // 以ULID格式存储图片路径：articleUlid/imageUlid.jpg
                List<String> imagePaths = new ArrayList<>();
                for (int i = 0; i < imageUlids.size(); i++) {
                    String imageUlid = imageUlids.get(i);
                    
                    // 获取图片扩展名
                    String extension = ".jpg"; // 默认扩展名
                    if (imageUlidMap != null) {
                        String storedExt = imageUlidMap.get(i + "_ext");
                        if (storedExt != null && !storedExt.isEmpty()) {
                            extension = storedExt;
                        }
                    }
                    
                    imagePaths.add(ulid + "/" + imageUlid + extension);
                }
                article.setImages(String.join(",", imagePaths));
            } else {
                // 如果没有图片ULID列表，尝试处理原始URL列表
                List<String> imageUrls = resultItems.get("imageUrls");
                
                if (imageUrls != null && !imageUrls.isEmpty() && imageUlidMap != null && !imageUlidMap.isEmpty()) {
                    List<String> imagePaths = new ArrayList<>();
                    for (int i = 0; i < imageUrls.size(); i++) {
                        String imageUlid = imageUlidMap.get(String.valueOf(i));
                        if (imageUlid != null) {
                            // 获取图片扩展名
                            String extension = ".jpg"; // 默认扩展名
                            String storedExt = imageUlidMap.get(String.valueOf(i) + "_ext");
                            if (storedExt != null && !storedExt.isEmpty()) {
                                extension = storedExt;
                            }
                            
                            imagePaths.add(ulid + "/" + imageUlid + extension);
                        }
                    }
                    if (!imagePaths.isEmpty()) {
                        article.setImages(String.join(",", imagePaths));
                    }
                }
            }
            
            // 处理头图
            String headImageUrl = resultItems.get("headImageUrl");
            if (headImageUrl != null && !headImageUrl.isEmpty() && imageUlidMap != null) {
                String headImageUlid = imageUlidMap.get("head");
                if (headImageUlid != null) {
                    // 获取头图扩展名
                    String extension = ".jpg"; // 默认扩展名
                    String storedExt = imageUlidMap.get("head_ext");
                    if (storedExt != null && !storedExt.isEmpty()) {
                        extension = storedExt;
                    }
                    
                    String headImagePath = ulid + "/" + headImageUlid + extension;
                    
                    // 如果有专门的头图字段，则设置
                    // article.setHeadImage(headImagePath);
                    
                    // 如果没有专门的头图字段，可以将头图添加到图片列表的开头
                    if (article.getImages() == null || article.getImages().isEmpty()) {
                        article.setImages(headImagePath);
                    } else {
                        article.setImages(headImagePath + "," + article.getImages());
                    }
                    
                    logger.info("添加文章头图: {}", headImagePath);
                }
            }

            // 设置图片映射信息
            String imageInfo = resultItems.get("imageInfo");
            if (imageInfo != null && !imageInfo.isEmpty()) {
                article.setImageMappings(imageInfo);
                logger.info("保存图片映射信息成功，大小：{} 字节", imageInfo.length());
                
                // 检查映射信息是否有效
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> mappings = mapper.readValue(imageInfo, Map.class);
                    logger.info("图片映射信息有效，包含 {} 个图片映射", mappings.size());
                    
                    // 检查文章内容中的占位符
                    String articleContent = article.getContent();
                    int placeholderCount = 0;
                    
                    for (String imageId : mappings.keySet()) {
                        String placeholder = "[[IMG:" + imageId + "]]";
                        if (articleContent.contains(placeholder)) {
                            placeholderCount++;
                        }
                    }
                    
                    logger.info("文章内容中找到 {} 个占位符", placeholderCount);
                    
                    if (placeholderCount == 0) {
                        logger.warn("警告：文章内容中没有找到占位符，图片可能无法正确显示");
                    }
                    
                } catch (Exception e) {
                    logger.error("解析图片映射信息失败", e);
                }
            } else {
                logger.warn("没有找到图片映射信息");
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
                    // 获取HTML内容和URL映射
                    Integer articleId = insertedArticle.getId();
                    logger.info("Inserting HTML content for article ID: {}", articleId);
                    
                    // 检查是否有URL映射数据
                    String urlMapping = resultItems.get("urlMapping");
                    if (urlMapping != null && !urlMapping.isEmpty()) {
                        // 使用包含URL映射的方法插入数据
                        articleMapper.insertArticleHtmlWithUrlMapping(articleId, fullHtml, urlMapping);
                        logger.info("Successfully inserted article with HTML content and URL mapping for URL: {}", url);
                    } else {
                        // 使用原来的方法插入数据
                        articleMapper.insertArticleHtml(articleId, fullHtml);
                        logger.info("Successfully inserted article and HTML content for URL: {}", url);
                    }
                    
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
                        
                        // 调用DeepSeek生成文章摘要
                        try {
                            // 获取文章内容
                            String articleContent = insertedArticle.getContent();
                            if (articleContent != null && !articleContent.trim().isEmpty()) {
                                // 调用DeepSeek API生成摘要
                                String summary = deepSeekService.summarizeText(articleContent);
                                logger.info("文章摘要已生成: {}", summary);
                                
                                // 将摘要输出到控制台
                                System.out.println("=== 文章摘要 ===");
                                System.out.println(summary);
                                System.out.println("===============");
                                
                                // 将摘要保存到数据库
                                articleMapper.updateArticleSummary(articleId, summary);
                                logger.info("文章摘要已保存到数据库, 文章ID: {}", articleId);
                            } else {
                                logger.warn("文章内容为空，无法生成摘要，文章ID: {}", articleId);
                            }
                        } catch (Exception e) {
                            logger.error("生成文章摘要失败，文章ID: {}", articleId, e);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to update search index for article ID: {}", articleId, e);
                    }
                } else {
                    logger.error("Failed to retrieve inserted article by URL: {}", url);
                }
            } else {
                logger.error("Failed to insert article: {}", url);
            }
        } catch (Exception e) {
            logger.error("Error processing article in pipeline: ", e);
        }
    }
}