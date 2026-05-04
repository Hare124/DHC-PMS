package com.example.dhcpms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class VisitorInviteDTO {
    @NotBlank(message = "访客姓名不能为空")
    @Length(max = 50, message = "访客姓名长度不能超过 50")
    private String visitorName;

    @NotBlank(message = "访客手机号不能为空")
    @Length(max = 20, message = "访客手机号长度不能超过 20")
    private String visitorPhone;

    @NotNull(message = "预约时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime visitTime;

    // 邀请有效期（小时），默认 2 小时
    private Integer validHours;
}
