package com.example.dhcpms.common;

import com.example.dhcpms.entity.DeepSeekMessage;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequest {
    // 模型名称
    private String model;
    // 对话消息列表
    private List<DeepSeekMessage> messages;
    // 温度（0-1，值越高越随机）
    private Double temperature = 0.7;
    // 最大生成token数
    private Integer max_tokens = 2048;
}
