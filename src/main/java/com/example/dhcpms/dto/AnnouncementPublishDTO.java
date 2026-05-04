package com.example.dhcpms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告发布/编辑入参DTO
 */
@Data
public class AnnouncementPublishDTO {
    private String title;           // 公告标题
    private String content;         // 公告正文
    private Integer isTop;          // 是否置顶
    private List<String> attachments; // 附件列表

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledPublishTime; // 定时发布时间

    private boolean resetReadCount; // 是否重置阅读次数
    private Long publisherId;       // 发布人 ID（定时任务用）
}