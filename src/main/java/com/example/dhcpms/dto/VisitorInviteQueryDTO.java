package com.example.dhcpms.dto;

import lombok.Data;

/**
 * 物业查询邀请记录入参 DTO
 */
@Data
public class VisitorInviteQueryDTO {
    private Integer status; // 0-待核销 1-已核销 2-已过期
    private String keyword; // 访客姓名/手机号模糊搜索
    private String startTime; // 开始时间（YYYY-MM-DD HH:mm:ss）
    private String endTime; // 结束时间（YYYY-MM-DD HH:mm:ss）
}
