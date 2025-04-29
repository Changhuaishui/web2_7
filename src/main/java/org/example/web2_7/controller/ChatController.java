package org.example.web2_7.controller;

import org.example.web2_7.service.DeepSeekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 聊天控制器
 * 提供AI对话功能
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    /**
     * 处理聊天请求
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        logger.info("收到聊天请求, 用户ID: {}", request.getUserId());
        
        try {
            // 生成回复
            String response = deepSeekService.generateChatResponse(request.getMessage());
            
            return Map.of(
                "success", true,
                "message", response
            );
        } catch (Exception e) {
            logger.error("生成回复时出错: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String userId;
        private String message;
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 