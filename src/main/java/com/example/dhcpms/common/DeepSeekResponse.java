package com.example.dhcpms.common;

import com.example.dhcpms.entity.DeepSeekMessage;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    // 内部类：响应选项
    @Data
    public static class Choice {
        private Integer index;
        private DeepSeekMessage message;
        private String finish_reason;
    }

    // 内部类：token使用统计
    @Data
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}