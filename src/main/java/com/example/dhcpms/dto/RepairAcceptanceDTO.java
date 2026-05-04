package com.example.dhcpms.dto;

import lombok.Data;

/**
 * 业主验收报修单的DTO
 */
@Data
public class RepairAcceptanceDTO {
    private Long repairOrderId;     // 报修订单ID
    private Boolean acceptResult;   // 验收结果：true通过，false不通过
    private Integer score;          // 评分（1-5分，仅通过时填写）
    private String comment;         // 评价内容/未修复原因
}