package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客邀请实体类
 */
@Data
@TableName("visitor_invite") // 对应数据库表名
public class VisitorInvite {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;              // 主键
    private Long ownerId;         // 业主ID（关联owner表）
    private String visitorName;   // 访客姓名
    private String visitorPhone;  // 访客电话
    private LocalDateTime visitTime; // 预约时间
    private String qrcode;        // 二维码唯一标识
    private Integer status;       // 状态（0-待核销/1-已核销/2-已过期）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime expireTime; // 过期时间
    private LocalDateTime updateTime; // 更新时间
}