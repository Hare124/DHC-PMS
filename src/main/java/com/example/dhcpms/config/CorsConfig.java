// src/main/java/com/example/dhcpms/common/config/CorsConfig.java
package com.example.dhcpms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 整合后的跨域配置类
 * 优先使用 CorsFilter（全局过滤器，优先级高于 WebMvcConfigurer）
 * 无需同时实现 WebMvcConfigurer，避免配置冲突
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer { // 保留接口实现，方便后续扩展其他MVC配置

    /**
     * 全局跨域过滤器（核心配置，覆盖所有请求）
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 允许的源（生产环境建议指定具体域名，而非*）
        // 开发环境：允许前端本地地址
        config.addAllowedOriginPattern("http://localhost:5173");
        // 生产环境示例：config.addAllowedOriginPattern("https://your-domain.com");

        // 2. 允许携带Cookie（前后端分离场景常用）
        config.setAllowCredentials(true);

        // 3. 允许的请求头（* 表示所有）
        config.addAllowedHeader(CorsConfiguration.ALL);

        // 4. 允许的请求方法（* 表示所有：GET/POST/PUT/DELETE/OPTIONS等）
        config.addAllowedMethod(CorsConfiguration.ALL);

        // 5. 预检请求（OPTIONS）的缓存时间，减少请求次数
        config.setMaxAge(3600L);

        // 6. 配置生效的路径（/** 表示所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 返回跨域过滤器
        return new CorsFilter(source);
    }

    // 【可选】如果需要针对特定路径精细化配置（比如仅/api开头），可添加以下方法
    // 注：若已配置全局/**，此方法可省略，避免重复
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 仅对/api开头的接口生效
                .allowedOriginPatterns("http://localhost:5173") // 替代allowedOrigins（Spring Boot 2.4+推荐）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}