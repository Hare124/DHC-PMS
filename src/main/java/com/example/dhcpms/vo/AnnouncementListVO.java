package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主端公告列表VO
 */
@Data
public class AnnouncementListVO {
    private Long id;                // 公告 ID
    private String title;           // 公告标题
    private String content;         // 公告内容
    private LocalDateTime publishTime; // 发布时间
    private Integer isTop;          // 是否置顶
    private Integer readCount;      // 阅读次数
    private String publisherName;   // 发布人姓名（物业管理员 XXX）
    private boolean unread;         // 是否未读
}