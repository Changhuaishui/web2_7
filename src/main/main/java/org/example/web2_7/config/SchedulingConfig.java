package org.example.web2_7.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring的任务调度功能
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 空配置类，只需要通过注解启用功能
} 