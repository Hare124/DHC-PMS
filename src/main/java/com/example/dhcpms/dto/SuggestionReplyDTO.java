package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 意见反馈回复 DTO
 */
@Data
public class SuggestionReplyDTO {

    private Long id;  // 意见反馈 ID

    @NotBlank(message = "回复内容不能为空")
    private String replyContent;

    private Long replyUserId;  // 回复人 ID
}
