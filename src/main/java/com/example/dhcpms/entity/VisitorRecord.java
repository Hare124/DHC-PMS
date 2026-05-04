package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客记录实体类
 */
@Data
@TableName("visitor_record") // 对应数据库表名
public class VisitorRecord {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;              // 主键
    private Long inviteId;        // 邀请ID（关联visitor_invite表）
    private LocalDateTime checkinTime; // 核销时间（进入时间）
    private LocalDateTime checkoutTime; // 离开时间
    private Long checkerId;       // 核销人员ID（关联user表）
}