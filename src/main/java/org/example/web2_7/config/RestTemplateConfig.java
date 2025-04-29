package org.example.web2_7.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 用于在服务间调用API
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 