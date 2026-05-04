package com.example.dhcpms.dto;

import lombok.Data;

/**
 * 取消报修申请的 DTO
 */
@Data
public class RepairCancelDTO {
    private Long repairOrderId;     // 报修订单 ID
    private String cancelReason;    // 取消原因
}
