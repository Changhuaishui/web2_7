package org.example.web2_7.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * HTML图片处理工具类
 * 用于处理HTML中的图片URL替换，将原始URL替换为本地URL
 */
public class HtmlImageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HtmlImageProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 替换HTML中的图片URL
     * @param html 原始HTML
     * @param urlMappingJson URL映射JSON字符串
     * @return 替换后的HTML
     */
    public static String replaceImageUrls(String html, String urlMappingJson) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        
        if (urlMappingJson == null || urlMappingJson.isEmpty()) {
            return html;
        }
        
        try {
            // 解析URL映射
            Map<String, String> urlMapping = objectMapper.readValue(
                urlMappingJson, 
                new TypeReference<Map<String, String>>() {}
            );
            
            // 解析HTML
            Document doc = Jsoup.parse(html);
            
            // 查找所有图片元素
            Elements imgs = doc.select("img[data-src], img[src]");
            
            for (Element img : imgs) {
                // 获取原始URL
                String originalUrl = img.attr("data-src");
                if (originalUrl.isEmpty()) {
                    originalUrl = img.attr("src");
                }
                
                // 查找对应的本地路径
                String localPath = urlMapping.get(originalUrl);
                if (localPath != null) {
                    // 替换src属性为本地路径
                    img.attr("src", localPath);
                    // 清除data-src属性
                    img.removeAttr("data-src");
                    logger.debug("Replaced image URL: {} -> {}", originalUrl, localPath);
                }
            }
            
            // 返回修改后的HTML
            return doc.outerHtml();
        } catch (IOException e) {
            logger.error("Error replacing image URLs: ", e);
            return html;
        }
    }
} 