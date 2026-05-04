package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修评价实体类(已禁用）
 * 对应数据库表：repair_evaluation
 */
@Data
@TableName("repair_evaluation")
public class RepairEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;                 // 主键
    private Long repairOrderId;      // 报修订单ID（关联repair_order表）
    private Integer score;           // 评分（1-5分）
    private String comment;          // 评价内容
    private LocalDateTime createTime;// 创建时间
}
