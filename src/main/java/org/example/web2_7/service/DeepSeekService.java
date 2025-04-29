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
     * 生成聊天回复
     * 
     * @param message 用户消息
     * @return 生成的回复文本
     */
    String generateChatResponse(String message);
} 