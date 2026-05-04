package com.example.dhcpms.dto;

import lombok.Data;

import java.util.List;

/**
 * 业主提交报修申请的DTO
 */
@Data
public class RepairApplyDTO {
    private Long repairTypeId;      // 报修类型ID
    private String title;           // 报修标题
    private String content;         // 报修内容
    private String expectedTime;    // 预计上门时间
    private List<String> voucherUrls; // 故障凭证链接（前端存储的照片/视频链接）
}
