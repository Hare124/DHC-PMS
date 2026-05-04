package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物业访客通行记录返回VO
 */
@Data
public class VisitorRecordVO {
    private Long id;
    private Long inviteId;
    private String visitorName;
    private String visitorPhone;
    private String ownerRoomNo;
    private LocalDateTime visitTime; // 预约时间
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String checkerName; // 核销人员姓名
    private String passStatus; // 已入园/已离园
}