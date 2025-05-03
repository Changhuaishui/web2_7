package org.example.web2_7.service;

/**
 * DeepSeek服务接口
 * 用于调用DeepSeek API生成文章摘要
 */
public interface DeepSeekService {
    
    /**
     * 根据输入文本生成摘要
     * 
     * @param textToSummarize 需要摘要的文本内容
     * @return 生成的摘要文本
     */
    String summarizeText(String textToSummarize);
    
    /**
     * 根据文本内容生成关键词
     * 
     * @param textContent 文本内容
     * @return 生成的关键词，以逗号分隔
     */
    String generateKeywords(String textContent);
    
    /**
     * 同时生成摘要和关键词
     * 
     * @param textContent 文本内容
     * @return 包含摘要和关键词的数组，索引0为摘要，索引1为关键词
     */
    String[] generateSummaryAndKeywords(String textContent);
    
    /**
     * 生成聊天回复
     * 
     * @param message 用户消息
     * @return 生成的回复文本
     */
    String generateChatResponse(String message);
} 