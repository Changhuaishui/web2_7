package org.example.web2_7.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 失效链接处理管道
 * 当爬虫检测到失效链接时，调用后端API记录
 */
@Component
public class InvalidLinkPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(InvalidLinkPipeline.class);
    
    /**
     * 失效链接API地址
     */
    private static final String INVALID_LINK_API = "http://localhost:8080/api/crawler/invalid-links";
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 处理爬虫结果
     */
    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            // 检查是否为失效链接
            Boolean isInvalidLink = resultItems.get("isInvalidLink");
            
            if (isInvalidLink != null && isInvalidLink) {
                String url = resultItems.get("url");
                
                if (url != null && !url.isEmpty()) {
                    logger.warn("检测到失效链接: {}", url);
                    
                    // 调用API记录失效链接
                    try {
                        // URL编码
                        String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
                        String apiUrl = INVALID_LINK_API + "?url=" + encodedUrl;
                        
                        // 调用API
                        String response = restTemplate.getForObject(apiUrl, String.class);
                        logger.info("失效链接API响应: {}", response);
                    } catch (Exception e) {
                        logger.error("调用失效链接API失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理失效链接时发生错误", e);
        }
    }
} 