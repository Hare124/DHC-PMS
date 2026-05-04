package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用记录表 - 实体类
 * 对应数据库表：fee_record
 */
@Data
@TableName("fee_record")
public class FeeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;                // 账单 ID（唯一编号）
    private Long ownerId;           // 业主 ID（关联 owner 表）
    private Long feeTypeId;         // 收费项目/费用类型 ID（关联 fee_type 表）
    private BigDecimal amount;      // 应收金额（精度 10,2）
    private BigDecimal discountAmount;  // 减免金额
    private BigDecimal lateFee;         // 滞纳金
    private BigDecimal actualAmount;    // 实收金额
    private BigDecimal arrearsAmount;   // 欠费金额
    private LocalDate dueDate;      // 缴费截止日期（DATE 类型）
    private Integer status;         // 状态（0-未缴/1-已缴/2-撤销）
    private String payType;         // 缴费方式（微信/支付宝/现金/银行卡/转账）
    private LocalDateTime payTime;  // 缴费时间
    private String transactionNo;   // 缴费流水号
    private Long operatorId;        // 物业操作员 ID（关联 user 表）
    private String remark;          // 备注
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
