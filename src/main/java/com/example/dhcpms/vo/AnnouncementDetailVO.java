package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告详情VO
 */
@Data
public class AnnouncementDetailVO {
    private Long id;                // 公告ID
    private String title;           // 公告标题
    private String content;         // 公告正文
    private LocalDateTime publishTime; // 发布时间
    private Integer isTop;          // 是否置顶
    private Integer readCount;      // 阅读次数
    private String publisherName;   // 发布人姓名
    private List<String> attachments; // 附件列表
}