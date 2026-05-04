package com.example.dhcpms.dto;

import lombok.Data;

/**
 * 物业派单的 DTO
 */
@Data
public class RepairAssignDTO {
    private Long repairOrderId;     // 报修订单 ID
    private String handlerName;     // 维修人员姓名
    private String handlerPhone;    // 维修人员联系方式
    private String expectedVisitTime; // 预计上门时间
    private String remark;          // 派单备注
}
