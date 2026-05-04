package com.example.dhcpms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用汇总VO
 */
@Data
public class FeeSummaryVO {
    private BigDecimal unpaidAmount;    // 待缴金额（status=0且dueDate≥当前日期）
    private BigDecimal overdueAmount;   // 逾期金额（status=0且dueDate<当前日期）
    private BigDecimal paidAmount;      // 已缴金额（status=1）
}