package org.example.web2_7.service;

public interface CrawlerService {
    void crawlArticle(String url) throws Exception;
    
    /**
     * 检查链接状态
     * @param url 要检查的URL
     * @return 链接是否有效
     */
    boolean checkLinkStatus(String url) throws Exception;
} 