package org.example.web2_7.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 用于对外部的服务调用API,对DeepSeek的API调用
 * 具体是给springboot提供一个RestTemplate的bean，
 * 使得能为web应用提供Restful服务，
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  //创建RestTemplate对象
    }
} 