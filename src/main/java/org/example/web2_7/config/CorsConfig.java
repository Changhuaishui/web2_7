package org.example.web2_7.config;
/*
 * @author: chen
 * 使前端Vue应用能够正常访问后端API
 * 也就是跨域资源共享配置
 * 
 */
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 使用allowedOriginPatterns代替allowedOrigins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")//允许的请求方法
                .allowedHeaders("*")//允许的请求头,允许所有
                .allowCredentials(true)//允许携带凭证,允许跨域请求携带凭证
                .maxAge(3600);  // 设置缓存时间
    }
}