package com.example.dhcpms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Spring MVC 全局参数转换器配置
 * 解决 LocalDate 类型参数自动转换问题
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册 LocalDate 转换器（字符串 -> LocalDate）
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateConverter());
    }

    /**
     * 自定义字符串转 LocalDate 转换器
     */
    @Bean
    public Converter<String, LocalDate> stringToLocalDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                // 适配前端传递的 yyyy-MM-dd 格式日期字符串
                return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };
    }
    /**
     * 添加静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /images/** 到 uploads/images/ 目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/");

        // 映射 /uploads/** 到 uploads/ 目录（兼容其他上传文件）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}