package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StaffVO {
    // User 表字段
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Integer status;
    private Integer role;
    private LocalDateTime createTime;

    // Staff 表字段
    private Long staffId;
    private String staffNo;
    private String idCard;
    private Integer staffStatus;
}
