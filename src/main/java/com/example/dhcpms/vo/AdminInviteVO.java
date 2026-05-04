package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物业端邀请记录返回VO
 */
@Data
public class AdminInviteVO {
    private Long id;
    private String visitorName;
    private String visitorPhone;
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Integer status;
    private String ownerRoomNo; // 业主房号（楼栋+房间）
    private String ownerName; // 业主姓名（邀请人姓名）
}