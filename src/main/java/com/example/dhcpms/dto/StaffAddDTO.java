package com.example.dhcpms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物业人员新增 DTO
 */
@Data
public class StaffAddDTO {

    // ==================== User 表字段 ====================
    @NotBlank(message = "用户名不能为空")
    private String username;      // 用户名

    @NotBlank(message = "姓名不能为空")
    private String name;          // 姓名

    @NotBlank(message = "手机号不能为空")
    private String phone;         // 手机号

    private String password;      // 密码（可选，不填则使用默认密码）

    private Integer status = 1;   // 账号状态：0-禁用，1-正常

    // ==================== Staff 表字段 ====================
    @NotBlank(message = "工号不能为空")
    private String staffNo;       // 工号

    @NotBlank(message = "身份证号不能为空")
    private String idCard;        // 身份证号

    @NotNull(message = "员工状态不能为空")
    private Integer staffStatus = 1;  // 员工状态：0-离职，1-在职，2-试用期
}