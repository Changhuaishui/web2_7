package org.example.web2_7.service.impl;
/*
 * @author chen
 * 爬虫实现服务
 * 爬虫逻辑：
 * 1. 接收URL
 * 2. 创建爬虫实例
 * 3. 配置线程和数据管道
 * 4. 运行爬虫
 * 5. 保存爬取结果到数据库，并下载图片到以文章标题命名的文件夹中
 */
import org.example.web2_7.service.CrawlerService;
import org.example.web2_7.crawler.DatabasePipeline;
import org.example.web2_7.crawler.WeChatArticleSpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

@Service
public class CrawlerServiceImpl implements CrawlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlerServiceImpl.class);

    @Autowired  // 注入数据库管道
    private DatabasePipeline databasePipeline;

    @Override  // 提交爬取任务
    public void crawlArticle(String url) {
        logger.info("开始爬取文章: {}", url);
        
        try {
            // 创建爬虫实例并配置
            Spider spider = Spider.create(new WeChatArticleSpider())
                    .addUrl(url)
                    .thread(1)
                    .addPipeline(databasePipeline);
            
            // 运行爬虫
            spider.run();
            
            logger.info("文章爬取完成: {}", url);
        } catch (Exception e) {
            logger.error("爬取文章出错: {}", url, e);
            throw new RuntimeException("爬取文章失败: " + e.getMessage(), e);
        }
    }
} 