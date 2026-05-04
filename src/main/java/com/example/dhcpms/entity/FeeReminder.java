package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 催缴记录表 - 实体类
 * 对应数据库表：fee_reminder
 */
@Data
@TableName("fee_reminder")
public class FeeReminder {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键，自增
    private Long feeRecordId;       // 费用记录ID（关联 fee_record 表）
    private LocalDateTime reminderTime; // 催缴时间
    private Integer reminderType;   // 催缴类型（1-模板消息/2-短信）
    private Integer status;         // 状态（0-未发送/1-已发送）
}