package com.example.dhcpms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 1. 从配置文件读取JWT密钥（替代硬编码）
    @Value("${jwt.secret}")
    private String secretKey;

    // 2. 从配置文件读取Token过期时间（秒），默认1小时（3600秒）
    @Value("${jwt.expire:3600}")
    private long expireSeconds;

    // 封装密钥生成逻辑（避免重复代码）
    private SecretKey getSecretKey() {
        // 核心修复：创建合规的SecretKey，避免Base64解码异常
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 保留原有参数类型（Integer role），仅修复Token生成逻辑
    public String generateToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role); // 添加角色信息

        // 使用配置文件的过期时间（秒转毫秒）
        Date expireDate = new Date(System.currentTimeMillis() + expireSeconds * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(expireDate) // 改用配置的过期时间
                // 修复：使用新版signWith方法（仅传SecretKey）
                .signWith(getSecretKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        // 修复：解析Token时使用新版parserBuilder + SecretKey
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey()) // 复用密钥生成逻辑
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            // 修复：验证Token时使用新版parserBuilder + SecretKey
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey()) // 复用密钥生成逻辑
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            // 捕获所有Token异常（签名错误/过期/格式错误等）
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        // 修复：过期校验时使用新版parserBuilder + SecretKey
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey()) // 复用密钥生成逻辑
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date());
    }
}