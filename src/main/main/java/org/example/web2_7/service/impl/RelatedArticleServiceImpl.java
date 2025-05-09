package org.example.web2_7.service.impl;

import org.example.web2_7.Dao.RelatedArticleMapper;
import org.example.web2_7.pojo.RelatedArticle;
import org.example.web2_7.service.RelatedArticleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 相关文章服务实现类
 */
@Service
public class RelatedArticleServiceImpl implements RelatedArticleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RelatedArticleServiceImpl.class);
    private static final String SOGOU_SEARCH_URL = "https://weixin.sogou.com/weixin?type=2&query=%s";
    private static final int MAX_RELATED_ARTICLES = 3; // 最多爬取3篇相关文章
    
    @Autowired
    private RelatedArticleMapper relatedArticleMapper;
    
    /**
     * 爬取并保存相关文章
     * @param articleId 文章ID
     * @param keywords 关键词，用于搜索相关文章
     * @return 爬取的相关文章数量
     */
    @Override
    public int crawlAndSaveRelatedArticles(Integer articleId, String keywords) {
        logger.info("开始为文章ID:{}爬取相关文章，关键词: {}", articleId, keywords);
        
        if (articleId == null || keywords == null || keywords.isEmpty()) {
            logger.warn("文章ID或关键词为空，无法爬取相关文章");
            return 0;
        }
        
        try {
            // 处理关键词，只使用前1-3个关键词
            String[] keywordArray = keywords.split(",");
            String searchKeywords;
            
            if (keywordArray.length > 2) {
                // 如果关键词超过2个，只取前2个
                searchKeywords = keywordArray[0] + " " + keywordArray[1];
                logger.info("关键词太多，仅使用前2个关键词进行搜索: {}", searchKeywords);
            } else if (keywordArray.length > 0) {
                // 使用第一个关键词
                searchKeywords = keywordArray[0];
                logger.info("使用第一个关键词进行搜索: {}", searchKeywords);
            } else {
                searchKeywords = keywords;
            }
            
            // 对关键词进行编码
            String encodedKeywords = URLEncoder.encode(searchKeywords, StandardCharsets.UTF_8);
            String searchUrl = String.format(SOGOU_SEARCH_URL, encodedKeywords);
            
            logger.info("搜索URL: {}", searchUrl);
            
            // 设置请求头，模拟浏览器访问
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "max-age=0")
                    .referrer("https://weixin.sogou.com/")
                    .timeout(10000)
                    .get();
            
            // 选择搜索结果列表中的文章
            Elements articles = doc.select(".news-list li .txt-box");
            logger.info("在搜狗搜索结果中找到了{}篇文章", articles.size());
            
            // 如果没有找到文章，尝试仅使用第一个关键词
            if (articles.isEmpty() && keywordArray.length > 1) {
                searchKeywords = keywordArray[0];
                encodedKeywords = URLEncoder.encode(searchKeywords, StandardCharsets.UTF_8);
                searchUrl = String.format(SOGOU_SEARCH_URL, encodedKeywords);
                
                logger.info("没有找到文章，重试仅使用第一个关键词: {}", searchKeywords);
                logger.info("新的搜索URL: {}", searchUrl);
                
                doc = Jsoup.connect(searchUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .header("Cache-Control", "max-age=0")
                        .referrer("https://weixin.sogou.com/")
                        .timeout(10000)
                        .get();
                
                articles = doc.select(".news-list li .txt-box");
                logger.info("使用第一个关键词在搜狗搜索结果中找到了{}篇文章", articles.size());
            }
            
            int savedCount = 0;
            
            for (int i = 0; i < Math.min(articles.size(), MAX_RELATED_ARTICLES); i++) {
                Element article = articles.get(i);
                
                // 提取文章标题和链接
                Element titleElement = article.selectFirst("h3 a");
                if (titleElement == null) {
                    logger.warn("第{}篇文章没有标题元素，跳过", i+1);
                    continue;
                }
                
                String title = titleElement.text();
                // 获取搜狗的中转链接
                String sogouUrl = titleElement.attr("href");
                
                logger.info("搜索结果 #{}: 标题={}, 链接={}", i+1, title, sogouUrl);
                
                if (sogouUrl == null || sogouUrl.isEmpty()) {
                    logger.warn("第{}篇文章没有链接，跳过", i+1);
                    continue;
                }
                
                try {
                    // 尝试获取真实的微信文章URL
                    String actualUrl = getActualWeixinUrl(sogouUrl);
                    
                    if (actualUrl != null && !actualUrl.isEmpty()) {
                        logger.info("获取到真实微信链接: {}", actualUrl);
                        
                        // 检查是否已存在相同的相关文章记录
                        if (relatedArticleMapper.existsByArticleIdAndUrl(articleId, actualUrl) > 0) {
                            logger.info("相关文章已存在，跳过: {}", title);
                            continue;
                        }
                        
                        // 添加相关文章
                        if (addRelatedArticle(articleId, actualUrl, title)) {
                            savedCount++;
                            logger.info("保存相关文章成功: {}", title);
                        }
                    } else {
                        // 如果无法获取实际URL，则使用搜狗URL（虽然不理想）
                        logger.warn("无法获取实际链接，使用搜狗链接: {}", sogouUrl);
                        
                        // 检查是否已存在相同的相关文章记录
                        if (relatedArticleMapper.existsByArticleIdAndUrl(articleId, sogouUrl) > 0) {
                            logger.info("相关文章已存在，跳过: {}", title);
                            continue;
                        }
                        
                        // 添加相关文章
                        if (addRelatedArticle(articleId, sogouUrl, title)) {
                            savedCount++;
                            logger.info("保存相关文章成功(使用搜狗链接): {}", title);
                        }
                    }
                    
                    // 添加随机延迟，避免触发反爬虫机制
                    TimeUnit.SECONDS.sleep(1 + (int)(Math.random() * 2));
                    
                } catch (Exception e) {
                    logger.error("获取真实URL时出错: {}", e.getMessage());
                    
                    // 使用搜狗链接作为后备
                    if (addRelatedArticle(articleId, sogouUrl, title)) {
                        savedCount++;
                        logger.info("保存相关文章成功(使用搜狗链接，因为出错): {}", title);
                    }
                }
            }
            
            logger.info("为文章ID:{}成功爬取并保存{}篇相关文章", articleId, savedCount);
            return savedCount;
            
        } catch (IOException e) {
            logger.error("爬取相关文章时发生IO异常: {}", e.getMessage(), e);
            return 0;
        } catch (Exception e) {
            logger.error("爬取相关文章时发生异常: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 尝试获取搜狗链接背后的真实微信文章URL
     * @param sogouUrl 搜狗中转链接
     * @return 真实的微信文章URL
     */
    private String getActualWeixinUrl(String sogouUrl) {
        try {
            logger.info("尝试获取搜狗链接的实际URL: {}", sogouUrl);
            
            // 修复相对URL问题 - 如果链接以/开头，添加搜狗域名前缀
            String absoluteUrl = sogouUrl;
            if (sogouUrl.startsWith("/link?url=")) {
                absoluteUrl = "https://weixin.sogou.com" + sogouUrl;
                logger.info("将相对URL转换为绝对URL: {}", absoluteUrl);
            }
            
            // 确保URL不包含空格，这可能导致前端无法正确解析
            if (absoluteUrl.contains(" ")) {
                absoluteUrl = absoluteUrl.replace(" ", "%20");
                logger.info("将URL中的空格替换为%20: {}", absoluteUrl);
            }
            
            // 首选方案：直接从URL参数提取目标链接
            // 由于反爬机制，我们优先采用直接解析URL的方式而不是访问链接
            try {
                if (absoluteUrl.contains("url=")) {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("url=([^&]+)");
                    java.util.regex.Matcher matcher = pattern.matcher(absoluteUrl);
                    if (matcher.find()) {
                        String encodedUrl = matcher.group(1);
                        // 双重解码，确保完全解码
                        String decodedUrl = java.net.URLDecoder.decode(encodedUrl, java.nio.charset.StandardCharsets.UTF_8);
                        // 如果解码后的URL不以http开头但包含mp.weixin.qq.com，则添加https://前缀
                        if (!decodedUrl.startsWith("http") && decodedUrl.contains("mp.weixin.qq.com")) {
                            decodedUrl = "https://" + decodedUrl;
                        }
                        
                        if (decodedUrl.contains("mp.weixin.qq.com") || decodedUrl.startsWith("http")) {
                            logger.info("直接从URL参数提取到微信链接: {}", decodedUrl);
                            return decodedUrl;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("直接提取URL参数失败: {}", e.getMessage());
            }
            
            // 如果无法从URL直接提取，则存储搜狗链接
            // 确保链接格式正确，可以被前端正确处理
            logger.warn("无法从URL直接提取微信链接，使用原始搜狗链接: {}", absoluteUrl);
            return absoluteUrl;
        } catch (Exception e) {
            logger.error("获取实际URL时出错: {}", e.getMessage(), e);
            return sogouUrl; // 失败时返回原始URL
        }
    }
    
    /**
     * 获取文章的相关文章列表
     * @param articleId 文章ID
     * @return 相关文章列表
     */
    @Override
    public List<RelatedArticle> getRelatedArticles(Integer articleId) {
        if (articleId == null) {
            return Collections.emptyList();
        }
        
        try {
            return relatedArticleMapper.findByArticleId(articleId);
        } catch (Exception e) {
            logger.error("获取文章ID:{}的相关文章列表失败", articleId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 添加相关文章
     * @param articleId 文章ID
     * @param relatedUrl 相关文章URL
     * @param title 相关文章标题
     * @return 是否添加成功
     */
    @Override
    public boolean addRelatedArticle(Integer articleId, String relatedUrl, String title) {
        if (articleId == null || relatedUrl == null || relatedUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 使用insertIgnore避免重复插入
            int result = relatedArticleMapper.insertIgnore(articleId, relatedUrl, title);
            return result > 0;
        } catch (Exception e) {
            logger.error("添加相关文章失败", e);
            return false;
        }
    }
    
    /**
     * 删除文章的所有相关文章
     * @param articleId 文章ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteRelatedArticles(Integer articleId) {
        if (articleId == null) {
            return false;
        }
        
        try {
            int result = relatedArticleMapper.deleteByArticleId(articleId);
            logger.info("删除文章ID:{}的相关文章，影响行数: {}", articleId, result);
            return true;
        } catch (Exception e) {
            logger.error("删除文章ID:{}的相关文章失败", articleId, e);
            return false;
        }
    }
} 