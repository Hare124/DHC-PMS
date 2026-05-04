package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物业核销邀请入参DTO
 */
@Data
public class VisitorVerifyDTO {
    // 加密后的邀请ID（扫码核销用）
    private String encryptInviteId;

    // 访客手机号（手动核销用）
    private String visitorPhone;

    @NotNull(message = "核销人员ID不能为空")
    private Long checkerId; // 关联user表的id

    @NotBlank(message = "核销类型不能为空")
    private String checkType; // 自动/手动

    private String remark; // 备注（手动核销时填写）
}