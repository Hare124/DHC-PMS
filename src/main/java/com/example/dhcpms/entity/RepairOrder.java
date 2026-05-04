package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修订单实体类
 * 对应数据库表：repair_order
 */
@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;                 // 主键（报修单号）
    private String repairOrderNo;    // 报修单号（格式：BX + 年月日时分秒 + 4 位随机数）
    private Long ownerId;            // 业主ID（关联owner表）
    private Long repairTypeId;       // 报修类型ID（关联repair_type表）
    private String title;            // 报修标题
    private String content;          // 报修内容
    private String address;          // 详细地址（楼栋+房间号）
    private Integer status;          // 状态（0-待审核/1-已审核/2-维修中/3-已完成/4-已取消/5-待验收/6-已验收）
    private LocalDateTime createTime;// 创建时间
    private Long handlerId;          // 维修人员ID（关联user表）
    private LocalDateTime completeTime; // 完成时间
    private String remark;           // 备注
    private LocalDateTime updateTime; // 更新时间

    // 维修人员信息字段
    private String handlerName;      // 维修人员姓名
    private String handlerPhone;     // 维修人员联系方式

    // 以下为原 RepairEvaluation 的字段（合并后）
    private Integer evaluationScore;     // 评价评分（1-5 分）
    private String evaluationComment;    // 评价内容
    private LocalDateTime evaluationTime;// 评价时间

    // 非数据库字段（用于前端展示）
    private transient String repairTypeName; // 报修类型名称
    private transient String statusDesc;     // 状态描述
}
