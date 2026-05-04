package com.example.dhcpms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 费用查询条件 DTO
 */
@Data
public class FeeQueryDTO {
    private Long ownerId;               // 业主 ID
    private String typeName;            // 费用类型名称
    private Integer status;             // 缴费状态（0-未缴/1-已缴）
    private LocalDate startDueDate;     // 截止日期开始（DATE 类型）
    private LocalDate endDueDate;       // 截止日期结束（DATE 类型）
    private String keyword;             // 模糊搜索关键词（房号/业主姓名/费用单号）
    private Integer pageNum = 1;        // 页码
    private Integer pageSize = 20;      // 每页条数
    private Integer overdueStatus;      // 逾期状态（0-未逾期/1-已逾期）
    private BigDecimal minAmount;       // 最小金额
    private BigDecimal maxAmount;       // 最大金额
}
