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
import org.springframework.web.client.HttpClientErrorException;
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
    
    // 重试次数和截断比例设置
    private static final int MAX_RETRY_COUNT = 3;
    private static final float[] TRUNCATION_RATIOS = {0.75f, 0.5f, 0.25f};
    
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
     * 根据文本内容生成关键词
     * 
     * @param textContent 文本内容
     * @return 生成的关键词，以逗号分隔
     */
    @Override
    public String generateKeywords(String textContent) {
        logger.info("开始调用DeepSeek API生成关键词");
        
        if (textContent == null || textContent.trim().isEmpty()) {
            logger.warn("文章内容为空，无法生成关键词");
            return "";
        }
        
        String truncatedContent = textContent;
        
        // 实现重试机制
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                // 第一次尝试使用保守截断
                if (attempt == 0) {
                    // 对文本内容进行截断，避免超出模型最大token限制
                    // DeepSeek模型最大接受65536个token，保守起见限制在30000个字符内
                    if (textContent.length() > 30000) {
                        truncatedContent = textContent.substring(0, 30000);
                        logger.info("文章内容过长({}字符)，已截断至30000字符", textContent.length());
                    }
                } else {
                    // 重试时进一步截断内容
                    float ratio = TRUNCATION_RATIOS[Math.min(attempt - 1, TRUNCATION_RATIOS.length - 1)];
                    int maxLength = (int)(30000 * ratio);
                    truncatedContent = textContent.substring(0, Math.min(textContent.length(), maxLength));
                    logger.info("第{}次重试，将内容截断至{}字符 (原始内容的{}%)", 
                                attempt, truncatedContent.length(),
                                Math.round((float)truncatedContent.length() / textContent.length() * 100));
                }
                
                // 准备HTTP请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);
                
                // 准备请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "deepseek-chat");
                
                // 准备消息内容
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 系统消息，设置任务说明
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", "你是一个专业的文章关键词提取助手。请从文章中提取5-10个最具代表性的关键词，关键词之间用逗号分隔。这些关键词应当能够准确反映文章的主题和内容，便于用户搜索和分类。");
                messages.add(systemMessage);
                
                // 用户消息，提供文章内容
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", "请为以下文章提取关键词（直接返回关键词列表，以逗号分隔）：\n\n" + truncatedContent);
                messages.add(userMessage);
                
                requestBody.put("messages", messages);
                requestBody.put("temperature", 0.2); // 较低的温度以获得更确定性的输出
                requestBody.put("max_tokens", 100); // 关键词一般不会太长
                
                // 创建HTTP请求
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                // 发送请求并获取响应
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                
                // 处理响应
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map responseBody = response.getBody();
                    
                    // 从响应中提取关键词文本
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        @SuppressWarnings("unchecked")
                        Map<String, String> message = (Map<String, String>) choice.get("message");
                        String keywords = message.get("content").trim();
                        
                        // 处理可能的格式问题
                        if (keywords.startsWith("关键词：") || keywords.startsWith("关键词:")) {
                            keywords = keywords.substring(keywords.indexOf("：") + 1).trim();
                        }
                        
                        logger.info("成功生成关键词：{}", keywords);
                        return keywords;
                    }
                } else {
                    logger.error("调用DeepSeek API生成关键词失败: {}", response.getStatusCode());
                    return "";
                }
                
            } catch (HttpClientErrorException.BadRequest e) {
                // 对于400错误，检查是否是Token超限问题
                String message = e.getMessage();
                if (message.contains("maximum context length") && message.contains("tokens") && attempt < MAX_RETRY_COUNT) {
                    logger.warn("Token超限错误，准备第{}次重试，将进一步截断内容: {}", attempt + 1, e.getMessage());
                    continue; // 继续下一次重试
                } else {
                    logger.error("调用DeepSeek API生成关键词出错，非Token超限错误或已达到最大重试次数", e);
                    // 最后一次尝试失败，返回一些基本关键词
                    if (attempt == MAX_RETRY_COUNT) {
                        logger.info("所有重试都失败，返回基本关键词");
                        return extractBasicKeywords(textContent);
                    }
                    return "";
                }
            } catch (Exception e) {
                logger.error("调用DeepSeek API生成关键词出错", e);
                // 最后一次尝试失败，返回一些基本关键词
                if (attempt == MAX_RETRY_COUNT) {
                    logger.info("所有重试都失败，返回基本关键词");
                    return extractBasicKeywords(textContent);
                }
                return "";
            }
        }
        
        // 如果所有尝试都失败，返回一些基本关键词
        logger.info("所有API调用尝试都失败，返回基本关键词");
        return extractBasicKeywords(textContent);
    }
    
    /**
     * 同时生成摘要和关键词
     * 
     * @param textContent 文本内容
     * @return 包含摘要和关键词的数组，索引0为摘要，索引1为关键词
     */
    @Override
    public String[] generateSummaryAndKeywords(String textContent) {
        logger.info("开始同时生成摘要和关键词");
        
        if (textContent == null || textContent.trim().isEmpty()) {
            logger.warn("文章内容为空，无法生成摘要和关键词");
            return new String[]{"无法生成摘要：文章内容为空", ""};
        }
        
        String truncatedContent = textContent;
        
        // 实现重试机制
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                // 第一次尝试使用保守截断
                if (attempt == 0) {
                    // 对文本内容进行截断，避免超出模型最大token限制
                    // DeepSeek模型最大接受65536个token，保守起见限制在30000个字符内
                    if (textContent.length() > 30000) {
                        truncatedContent = textContent.substring(0, 30000);
                        logger.info("文章内容过长({}字符)，已截断至30000字符", textContent.length());
                    }
                } else {
                    // 重试时进一步截断内容
                    float ratio = TRUNCATION_RATIOS[Math.min(attempt - 1, TRUNCATION_RATIOS.length - 1)];
                    int maxLength = (int)(30000 * ratio);
                    truncatedContent = textContent.substring(0, Math.min(textContent.length(), maxLength));
                    logger.info("第{}次重试，将内容截断至{}字符 (原始内容的{}%)", 
                                attempt, truncatedContent.length(),
                                Math.round((float)truncatedContent.length() / textContent.length() * 100));
                }
                
                // 准备HTTP请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);
                
                // 准备请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "deepseek-chat");
                
                // 准备消息内容
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 系统消息，设置任务说明
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", 
                    "你是一个专业的文章分析助手，擅长提取文章摘要和关键词。" +
                    "请对提供的文章内容生成：\n" +
                    "1. 一个简洁明了的摘要，突出文章的主要观点和要点，不超过300字\n" +
                    "2. 5-10个最具代表性的关键词，用逗号分隔\n\n" +
                    "请按照以下格式返回结果：\n" +
                    "摘要：[摘要内容]\n" +
                    "关键词：[关键词1,关键词2,关键词3,...]"
                );
                messages.add(systemMessage);
                
                // 用户消息，提供文章内容
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", "请为以下文章生成摘要和关键词：\n\n" + truncatedContent);
                messages.add(userMessage);
                
                requestBody.put("messages", messages);
                requestBody.put("temperature", 0.3); // 较低的温度以获得更确定性的输出
                requestBody.put("max_tokens", 600); // 摘要和关键词一起需要更多token
                
                // 创建HTTP请求
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                // 发送请求并获取响应
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                
                // 处理响应
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map responseBody = response.getBody();
                    
                    // 从响应中提取内容
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        @SuppressWarnings("unchecked")
                        Map<String, String> message = (Map<String, String>) choice.get("message");
                        String content = message.get("content");
                        
                        // 解析摘要和关键词
                        String summary = "";
                        String keywords = "";
                        
                        if (content.contains("摘要：") && content.contains("关键词：")) {
                            int summaryStart = content.indexOf("摘要：") + 3;
                            int keywordsStart = content.indexOf("关键词：");
                            
                            if (summaryStart >= 3 && keywordsStart > summaryStart) {
                                summary = content.substring(summaryStart, keywordsStart).trim();
                                keywords = content.substring(keywordsStart + 4).trim();
                            }
                        } else {
                            // 如果返回格式不符合预期，尝试分别生成
                            logger.info("API返回格式不符合预期，使用备用逻辑生成摘要和关键词");
                            return new String[]{
                                generateSimpleSummary(truncatedContent), 
                                extractBasicKeywords(truncatedContent)
                            };
                        }
                        
                        logger.info("成功生成摘要和关键词");
                        return new String[]{summary, keywords};
                    }
                } else {
                    logger.error("调用DeepSeek API生成摘要和关键词失败: {}", response.getStatusCode());
                    if (attempt == MAX_RETRY_COUNT) {
                        return new String[]{
                            generateSimpleSummary(truncatedContent), 
                            extractBasicKeywords(truncatedContent)
                        };
                    }
                }
            } catch (HttpClientErrorException.BadRequest e) {
                // 对于400错误，检查是否是Token超限问题
                String message = e.getMessage();
                if (message.contains("maximum context length") && message.contains("tokens") && attempt < MAX_RETRY_COUNT) {
                    logger.warn("Token超限错误，准备第{}次重试，将进一步截断内容: {}", attempt + 1, e.getMessage());
                    continue; // 继续下一次重试
                } else {
                    logger.error("调用DeepSeek API生成摘要和关键词出错，非Token超限错误或已达到最大重试次数", e);
                    if (attempt == MAX_RETRY_COUNT) {
                        return new String[]{
                            generateSimpleSummary(truncatedContent), 
                            extractBasicKeywords(truncatedContent)
                        };
                    }
                }
            } catch (Exception e) {
                logger.error("调用DeepSeek API生成摘要和关键词出错", e);
                if (attempt == MAX_RETRY_COUNT) {
                    return new String[]{
                        generateSimpleSummary(truncatedContent), 
                        extractBasicKeywords(truncatedContent)
                    };
                }
            }
        }
        
        // 如果所有尝试都失败，返回一个简单的摘要和基本关键词
        logger.info("所有API调用尝试都失败，返回简单摘要和基本关键词");
        return new String[]{
            generateSimpleSummary(textContent), 
            extractBasicKeywords(textContent)
        };
    }
    
    /**
     * 从文本中提取基本关键词（当API调用失败时的后备方案）
     * @param text 文本内容
     * @return 逗号分隔的关键词
     */
    private String extractBasicKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 取文章的前100个字符作为简单提取
        String shortText = text.length() > 200 ? text.substring(0, 200) : text;
        
        // 分词并过滤停用词（简化版实现）
        String[] words = shortText.split("[\\s,.。，、；:：''()（）\\[\\]【】<>《》?？!！]");
        Map<String, Integer> wordCount = new HashMap<>();
        
        for (String word : words) {
            word = word.trim();
            if (word.length() > 1 && !isStopWord(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        
        // 取出现频率最高的5个词
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordCount.entrySet());
        sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        StringBuilder keywords = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedWords) {
            if (count > 0) {
                keywords.append(",");
            }
            keywords.append(entry.getKey());
            count++;
            if (count >= 5) {
                break;
            }
        }
        
        return keywords.toString();
    }
    
    /**
     * 生成简单摘要（当API调用失败时的后备方案）
     * @param text 文本内容
     * @return 简单摘要
     */
    private String generateSimpleSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "无法生成摘要：文章内容为空";
        }
        
        // 简单摘要：取文章的前100个字符
        int summaryLength = Math.min(text.length(), 100);
        String summary = text.substring(0, summaryLength);
        
        // 如果摘要不是在句子结尾处截断，则找到最近的句子结束符
        if (summaryLength < text.length()) {
            int lastPeriod = Math.max(
                summary.lastIndexOf('。'),
                Math.max(summary.lastIndexOf('.'), summary.lastIndexOf('!'))
            );
            if (lastPeriod > 0) {
                summary = summary.substring(0, lastPeriod + 1);
            }
            summary += "..."; // 添加省略号表示摘要未完
        }
        
        return summary;
    }
    
    /**
     * 判断是否为停用词
     * @param word 词语
     * @return 是否为停用词
     */
    private boolean isStopWord(String word) {
        // 简单停用词列表
        String[] stopWords = {"的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"};
        for (String stopWord : stopWords) {
            if (stopWord.equals(word)) {
                return true;
            }
        }
        return false;
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