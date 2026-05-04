package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主公告已读记录扩展表（已禁用）
 */
@Data
@TableName("announcement_read_record")
public class AnnouncementReadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键
    private Long ownerUserId;       // 业主用户ID（关联user表）
    private Long announcementId;    // 公告ID
    private LocalDateTime readTime; // 阅读时间
}