package com.jason.classroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 解决跨域问题
 */

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // 实现 WebMvcConfigurer 接口来自定义 Spring MVC 配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有路径
                .allowedOriginPatterns("*") // 允许所有源进行跨域请求
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowCredentials(true) // 允许携带凭证（cookies 或者 HTTP authentication）
                .maxAge(3600) // 设置预检请求的缓存时间，单位秒，表示多少秒内不需要再发送预检请求
                .allowedHeaders("*"); // 允许所有头部信息
    }
}
