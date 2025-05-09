package org.example.web2_7.service.impl;
/*
 * @author chen
 * 爬虫实现服务
 * 爬虫逻辑：
 * 1. 接收URL
 * 2. 利用Jsoup连接URL，判断是否有效
 * 3. 利用Webmagic创建爬虫实例，可支持多线程
 * 4. 配置线程和数据管道，先添加失效链接管道，再添加数据库管道
 * 5. 运行爬虫
 * 6. 保存爬取结果到数据库，并下载图片到以文件
 */
import org.example.web2_7.service.CrawlerService;
import org.example.web2_7.crawler.DatabasePipeline;
import org.example.web2_7.crawler.InvalidLinkPipeline;
import org.example.web2_7.crawler.WeChatArticleSpider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CrawlerServiceImpl implements CrawlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlerServiceImpl.class);

    @Autowired  // 注入数据库管道
    private DatabasePipeline databasePipeline;
    
    @Autowired  // 注入失效链接管道
    private InvalidLinkPipeline invalidLinkPipeline;

    @Override  // 提交爬取任务
    public void crawlArticle(String url) {
        logger.info("开始爬取文章: {}", url);
        
        try {
            // 创建爬虫实例并配置
            Spider spider = Spider.create(new WeChatArticleSpider())
                    .addUrl(url)
                    .thread(1)  // 设置线程数为1，可以支持多线程
                    .addPipeline(invalidLinkPipeline)  // 先添加失效链接管道
                    .addPipeline(databasePipeline);    // 再添加数据库管道
            
            // 运行爬虫
            spider.run();
            
            logger.info("文章爬取完成: {}", url);
        } catch (Exception e) {
            logger.error("爬取文章出错: {}", url, e);
            throw new RuntimeException("爬取文章失败: " + e.getMessage(), e);
        }
    }
    
    @Override  // 检查链接有效性
    public boolean checkLinkStatus(String url) throws Exception {
        logger.info("检查链接状态: {}", url);
        
        // 异常处理，判断URL是否有效
        try {
            // 直接使用Jsoup连接URL
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .timeout(10000)  // 保险起见，设置超时时间为10秒
                    .get();
            
            // 检测微信失效链接，返回异常信息
            Element invalidLinkElement = doc.selectFirst("div.weui-msg__title.warn");
            if (invalidLinkElement != null && invalidLinkElement.text().contains("临时链接已失效")) {
                logger.warn("链接无效: {} - 原因: {}", url, invalidLinkElement.text());
                return false;
            }
            
            // 检查是否包含正文内容元素
            Element contentElement = doc.selectFirst("div.rich_media_content");
            if (contentElement == null) {
                contentElement = doc.selectFirst("div#js_content");
            }
            
            if (contentElement == null) {
                logger.warn("链接无法解析正文内容: {}", url);
                return false;
            }
            
            logger.info("链接检查通过: {}", url);
            return true;
        } catch (Exception e) {
            logger.error("检查链接状态出错: {}", url, e);
            return false;
        }
    }
} 