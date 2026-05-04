package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业主意见反馈实体类
 */
@Data
@TableName("suggestion")
public class Suggestion {

    @TableId(type = IdType.AUTO)
    private Long id;              // 主键

    private Long userId;          // 用户 ID（关联 user 表）
    private Long ownerId;         // 业主 ID（关联 owner 表）
    private String type;          // 意见类型：投诉/建议/报修/咨询
    private String title;         // 意见标题
    private String content;       // 意见内容
    private String images;        // 图片路径（多张图片用逗号分隔）
    private String contactPhone;  // 联系电话
    private String status;        // 处理状态：待处理/处理中/已处理/已回复
    private String replyContent;  // 回复内容
    private Long replyUserId;     // 回复人 ID（物业人员）
    private LocalDateTime replyTime; // 回复时间
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
