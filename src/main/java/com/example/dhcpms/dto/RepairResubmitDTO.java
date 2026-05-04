package com.example.dhcpms.dto;

import lombok.Data;

import java.util.List;

/**
 * 重新提交报修申请的DTO
 */
@Data
public class RepairResubmitDTO {
    private Long repairOrderId;     // 原报修订单ID
    private String title;           // 修改后的标题
    private String content;         // 修改后的内容
    private List<String> voucherUrls; // 补充的凭证链接
}
