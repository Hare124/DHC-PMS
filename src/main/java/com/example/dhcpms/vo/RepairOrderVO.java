package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修订单列表 VO（业主/物业查看列表）
 */

@Data
public class RepairOrderVO {
    private Long id;                // 报修订单 ID
    private String repairOrderNo;   // 报修单号
    private String title;           // 报修标题
    private String repairTypeName;  // 报修类型名称
    private Integer status;         // 状态编码
    private String statusDesc;      // 状态描述
    private LocalDateTime createTime; // 创建时间
    private String address;         // 报修地址
}
