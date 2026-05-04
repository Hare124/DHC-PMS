package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SuggestionStatusDTO {

    private Long id;  // 意见反馈 ID

    @NotBlank(message = "处理状态不能为空")
    private String status;  // pending/processing/processed/replied
}