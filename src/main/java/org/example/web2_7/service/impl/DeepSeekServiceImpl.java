package org.example.web2_7.service.impl;

import org.example.web2_7.service.DeepSeekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek服务实现类
 * 调用DeepSeek API生成文章摘要
 */
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekServiceImpl.class);
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.url}")
    private String apiUrl;
    
    /**
     * 调用DeepSeek API生成文章摘要
     * 
     * @param textToSummarize 需要摘要的文本内容
     * @return 生成的摘要文本
     */
    @Override
    public String summarizeText(String textToSummarize) {
        logger.info("开始调用DeepSeek API生成摘要");
        
        if (textToSummarize == null || textToSummarize.trim().isEmpty()) {
            logger.warn("文章内容为空，无法生成摘要");
            return "无法生成摘要：文章内容为空";
        }
        
        try {
            // 准备HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat"); // 使用DeepSeek-V3模型，正确的模型名称是"deepseek-chat"
            
            // 准备消息内容
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息，设置任务说明
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的文章摘要生成助手。请对提供的文章内容生成一个简洁明了的摘要，突出文章的主要观点和要点。摘要应当言简意赅，不超过300字。");
            messages.add(systemMessage);
            
            // 用户消息，提供文章内容
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请为以下文章生成摘要：\n\n" + textToSummarize);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3); // 较低的温度以获得更确定性的输出
            requestBody.put("max_tokens", 500); // 限制输出token数量
            
            // 创建HTTP请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求并获取响应
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map responseBody = response.getBody();
                
                // 从响应中提取摘要文本
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String summary = message.get("content");
                    
                    logger.info("成功生成摘要：{}", summary);
                    return summary;
                }
            } else {
                logger.error("调用DeepSeek API失败: {}", response.getStatusCode());
                return "生成摘要失败：API调用错误";
            }
        } catch (Exception e) {
            logger.error("调用DeepSeek API出错", e);
            return "生成摘要失败：" + e.getMessage();
        }
        return null; // This line should never be reached
    }
    
    /**
     * 调用DeepSeek API生成聊天回复
     * 
     * @param message 用户消息
     * @return 生成的回复文本
     */
    @Override
    public String generateChatResponse(String message) {
        logger.info("开始调用DeepSeek API生成聊天回复");
        
        if (message == null || message.trim().isEmpty()) {
            logger.warn("用户消息为空，无法生成回复");
            return "无法回复：消息内容为空";
        }
        
        try {
            // 准备HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat"); // 使用DeepSeek-V3模型
            
            // 准备消息内容
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息，设置助手角色
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个友好、专业的AI助手，能够简洁明了地回答用户问题。");
            messages.add(systemMessage);
            
            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7); // 适当的温度使回复更自然
            requestBody.put("max_tokens", 1000); // 限制输出token数量
            
            // 创建HTTP请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求并获取响应
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map responseBody = response.getBody();
                
                // 从响应中提取回复文本
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> responseMessage = (Map<String, String>) choice.get("message");
                    String reply = responseMessage.get("content");
                    
                    logger.info("成功生成回复");
                    return reply;
                }
            } else {
                logger.error("调用DeepSeek API失败: {}", response.getStatusCode());
                return "生成回复失败：API调用错误";
            }
            
            return "无法生成回复";
        } catch (Exception e) {
            logger.error("调用DeepSeek API出错", e);
            return "生成回复失败：" + e.getMessage();
        }
    }
} 