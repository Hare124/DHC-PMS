package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Long id;                  // 主键
    private Long userId;              // 用户 ID（关联 User 表）
    private String staffNo;           // 工号（唯一标识）
    private String idCard;            // 身份证号
    private Integer status;           // 状态：0-离职，1-在职，2-试用期
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private String remark;           // 备注
}
