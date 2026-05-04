package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 访客离园登记入参DTO
 */
@Data
public class VisitorCheckoutDTO {
    private Long inviteId; // 邀请ID
    private String visitorPhone; // 访客手机号
    @NotNull(message = "核销人员ID不能为空")
    private Long checkerId; // 操作人ID（物业）
}