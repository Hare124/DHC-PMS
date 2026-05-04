package com.example.dhcpms.entity;

import lombok.Data;

@Data
public class DeepSeekMessage {
    // 角色：user（用户）、assistant（助手）、system（系统）
    private String role;
    // 消息内容
    private String content;
}
