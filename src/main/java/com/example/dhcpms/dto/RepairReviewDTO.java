package com.example.dhcpms.dto;

import lombok.Data;

/**
 * 物业审核报修单的DTO
 */
@Data
public class RepairReviewDTO {
    private Long repairOrderId;     // 报修订单ID
    private Boolean reviewResult;   // 审核结果：true通过，false驳回
    private String rejectReason;    // 驳回原因（仅驳回时填写）
    private Long reviewerId;        // 审核人ID（关联user表）
}