package com.example.dhcpms.service;

import com.alibaba.fastjson.JSON;
import com.example.dhcpms.common.DeepSeekRequest;
import com.example.dhcpms.common.DeepSeekResponse;
import com.example.dhcpms.entity.DeepSeekMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeepSeekApiService {

    private final RestTemplate restTemplate;

    // 读取配置文件参数
    @Value("${deepseek.api.key}")
    private String apiKey;
    @Value("${deepseek.api.url}")
    private String apiUrl;
    @Value("${deepseek.api.model}")
    private String model;

    /**
     * 初始化检查：验证配置是否加载成功
     */
    @PostConstruct
    public void checkConfig() {
        // 1. 检查配置是否为空
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("⚠️ API Key 配置为空！请检查 application.properties 配置");
        } else {
            // 打印前8位，避免泄露完整Key
            log.info("✅ API Key 注入成功，前8位：{}", apiKey.substring(0, Math.min(apiKey.length(), 8)));
        }
        log.info("✅ API URL 配置：{}", apiUrl);
        log.info("✅ 模型配置：{}", model);

        // 2. 检查 Authorization 头格式
        String testAuthHeader = "Bearer " + apiKey;
        log.info("✅ 测试 Authorization 头格式：{}", testAuthHeader);
        if (!testAuthHeader.startsWith("Bearer ")) {
            log.error("⚠️ Authorization 头格式错误！必须以 'Bearer ' 开头（包含空格）");
        }
    }

    public String chatWithDeepSeek(String userMessage) {
        // ========== 1. 前置参数校验 ==========
        log.info("\n===== 开始调用 DeepSeek API =====");
        log.info("用户输入消息：{}", userMessage);

        // 校验核心参数
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.error("⚠️ 用户消息为空，终止调用");
            return "用户消息不能为空";
        }

        // ========== 2. 构建请求头 ==========
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authHeader = "Bearer " + apiKey;
        headers.set("Authorization", authHeader);

        // 打印完整请求头（脱敏）
        log.info("✅ 构建请求头：");
        headers.forEach((key, value) -> {
            String displayValue = key.equals("Authorization")
                    ? "Bearer " + apiKey.substring(0, 8) + "****"
                    : value.toString();
            log.info("  {}: {}", key, displayValue);
        });

        // ========== 3. 构建请求体 ==========
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(model);
        DeepSeekMessage message = new DeepSeekMessage();
        message.setRole("user");
        message.setContent(userMessage);
        request.setMessages(Collections.singletonList(message));

        String requestJson = JSON.toJSONString(request);
        log.info("✅ 构建请求体：{}", requestJson);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        // ========== 4. 发送请求（增强异常捕获） ==========
        try {
            log.info("✅ 发送请求到：{}", apiUrl);
            ResponseEntity<DeepSeekResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    DeepSeekResponse.class
            );

            // ========== 5. 解析响应 ==========
            log.info("✅ 收到响应，状态码：{}", response.getStatusCode());

            // 打印响应头
            log.info("✅ 响应头：");
            response.getHeaders().forEach((key, value) ->
                    log.info("  {}: {}", key, value));

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("✅ 响应结果（脱敏）：{}",
                        JSON.toJSONString(response.getBody()).replace(apiKey, "****"));
                if (!response.getBody().getChoices().isEmpty()) {
                    String answer = response.getBody().getChoices().get(0).getMessage().getContent();
                    log.info("✅ 解析到AI回答：{}", answer);
                    return answer;
                } else {
                    log.error("⚠️ 响应体中无Choices数据");
                }
            } else {
                log.error("⚠️ 响应状态码异常：{}，响应体：{}",
                        response.getStatusCode(),
                        response.getBody() == null ? "null" : JSON.toJSONString(response.getBody()));
            }

        } catch (RestClientResponseException e) {
            // 重点：捕获HTTP响应异常（如401/403/500），打印完整响应信息
            String responseBody = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
            log.error("❌ HTTP请求异常 - 状态码：{}，响应体：{}",
                    e.getRawStatusCode(),
                    responseBody);
            throw new RuntimeException(String.format("调用DeepSeek失败：%s，详情：%s",
                    e.getStatusText(),
                    responseBody));
        } catch (Exception e) {
            // 捕获其他异常（如网络超时、JSON解析失败）
            log.error("❌ 调用DeepSeek API失败（非HTTP异常）", e);
            throw new RuntimeException("调用DeepSeek接口异常：" + e.getMessage(), e);
        }

        log.info("===== 调用结束：未获取到有效响应 =====");
        return "未获取到有效响应";
    }
}