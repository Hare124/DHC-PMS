package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主邀请记录返回VO
 */
@Data
public class InviteRecordVO {
    private Long id;
    private String visitorName;
    private String visitorPhone;
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private String statusDesc; // 状态描述：待核销/已核销/已过期
    private boolean qrCodeValid; // 二维码是否有效
    private LocalDateTime checkinTime; // 核销时间（已核销时展示）
    private String roomNo; // 业主房号
}