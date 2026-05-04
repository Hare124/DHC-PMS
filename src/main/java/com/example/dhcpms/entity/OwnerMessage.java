package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主群聊消息实体类
 */
@Data
@TableName("owner_message")
public class OwnerMessage {

    @TableId(type = IdType.AUTO)
    private Long id;              // 主键

    private String chatRoomName;  // 群聊名称（如：阳光小区业主群）
    private Long senderId;        // 发送人 ID（用户 ID）
    private String senderName;    // 发送人姓名（从 User 表 username 字段关联）
    private String messageContent; // 消息内容
    private String messageType;   // 消息类型：text-文本/image-图片/file-文件
    private String fileUrl;       // 文件 URL（图片/文件时）
    private String fileName;      // 文件名称（文件时）
    private LocalDateTime sendTime; // 发送时间
    private Boolean isWithdrawn;  // 是否已撤回
    private LocalDateTime withdrawTime; // 撤回时间
    private LocalDateTime createTime; // 创建时间
}
