package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 物业人员更新 DTO
 */
@Data
public class StaffUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;              // 用户ID

    // ==================== User 表字段 ====================
    private String username;      // 用户名
    private String name;          // 姓名
    private String phone;         // 手机号
    private String password;      // 密码（可选，不填则保持原密码）
    private Integer status;       // 账号状态：0-禁用，1-正常

    // ==================== Staff 表字段 ====================
    private String staffNo;       // 工号
    private String idCard;        // 身份证号
    private Integer staffStatus;  // 员工状态：0-离职，1-在职，2-试用期
}