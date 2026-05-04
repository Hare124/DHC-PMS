package com.example.dhcpms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 缴费单DTO
 */
@Data
public class PaymentOrderDTO {
    private String roomNo;              // 房号（楼栋号+房间号）
    private String feeTypeName;         // 费用类型名称
    private BigDecimal amount;          // 应收金额
    private BigDecimal overdueFine;     // 滞纳金
    private BigDecimal totalAmount;     // 实缴总金额
    private LocalDate dueDate;          // 缴费截止日期（DATE类型）
    private List<Long> feeRecordIds;    // 费用记录ID列表（批量缴费）
    private String payType;             // 支付方式：WECHAT/ALIPAY/BANK
}