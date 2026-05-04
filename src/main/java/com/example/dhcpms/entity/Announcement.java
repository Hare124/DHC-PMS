package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体（匹配数据库表）
 */
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键
    private String title;           // 公告标题
    private String content;         // 公告内容
    private Long publisherId;       // 发布人ID（关联user表）
    private LocalDateTime publishTime; // 发布时间
    private Integer isTop;          // 是否置顶（0-否/1-是）
    private Integer readCount;      // 阅读次数
    private Integer recallStatus;   // 撤回状态：0-未撤回/1-已撤回
    private LocalDateTime recallTime; // 撤回时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime createTime; // 创建时间
}