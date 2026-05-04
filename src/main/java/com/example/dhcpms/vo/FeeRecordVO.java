package com.example.dhcpms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用列表展示 VO
 */
@Data
public class FeeRecordVO {
    private Long id;                    // 账单 ID
    private Long ownerId;               // 业主 ID
    private String roomNo;              // 房号（楼栋号 - 房间号）
    private String buildingNo;          // 楼栋号
    private String ownerName;           // 业主姓名
    private String phone;               // 联系电话
    private String feeTypeName;         // 费用类型名称
    private BigDecimal unitPrice;       // 单价
    private BigDecimal houseArea;       // 房屋建筑面积
    private BigDecimal internalArea;    // 套内建筑面积
    private String chargePeriod;        // 计费周期
    private LocalDate dueDate;          // 缴费截止日期
    private BigDecimal amount;          // 应收金额
    private BigDecimal discountAmount;  // 减免金额
    private BigDecimal lateFee;         // 滞纳金
    private BigDecimal actualAmount;    // 实收金额
    private BigDecimal arrearsAmount;   // 欠费金额
    private String statusDesc;          // 状态描述：已缴费/待缴费/逾期欠费/已撤销
    private String payType;             // 缴费方式
    private LocalDateTime payTime;      // 缴费时间
    private String transactionNo;       // 缴费流水号
    private String operatorName;        // 操作员姓名
    private String remark;              // 备注
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
    private String latestReminder;      // 最新催缴信息
    private Integer overdueDays;        // 逾期天数
}
