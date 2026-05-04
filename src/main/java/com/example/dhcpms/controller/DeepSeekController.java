package com.example.dhcpms.controller;

import com.example.dhcpms.service.DeepSeekApiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DeepSeekController {

    private final DeepSeekApiService deepSeekApiService;

    @Data
    public static class ChatRequest {
        private String message;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        log.info("接收用户请求：{}", request.getMessage());
        return deepSeekApiService.chatWithDeepSeek(request.getMessage());
    }
}
