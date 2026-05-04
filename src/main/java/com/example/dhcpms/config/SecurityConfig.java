package com.example.dhcpms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 优先放行所有 OPTIONS 预检请求（跨域必备）
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. 核心修改：通配符放行所有 /user 开头的 POST 接口
                        // 替代原来的逐个接口放行，彻底避免遗漏
                        .antMatchers(HttpMethod.POST, "/user/**").permitAll()

                        // 3. （可选）保留原有公开接口配置（如有需要）
                        .antMatchers("/api/public/**", "/login", "/register").permitAll()

                        // 4. 其他接口需要认证（生产环境用）
                        // .anyRequest().authenticated()
                        // 开发阶段：临时放行所有接口（避免其他接口干扰测试）
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端域名跨域（精准匹配）
        config.addAllowedOriginPattern("http://localhost:5173");
        config.setAllowCredentials(true); // 允许携带 Cookie/Token
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有请求方法（GET/POST/OPTIONS 等）
        config.setMaxAge(3600L); // 跨域配置缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 所有接口生效
        return source;
    }
}