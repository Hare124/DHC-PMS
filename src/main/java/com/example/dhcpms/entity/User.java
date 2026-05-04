package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，用于表示物业管理人员或业主的基本信息。
 */
@Data
@TableName("user") // 对应数据库表名
public class User {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;              // 主键
    private String username;      // 用户名
    private String password;      // 密码（加密存储）
    private Integer role;         // 角色：1-物业人员，2-业主，3-物业经理
    private String name;          // 姓名
    private String phone;         // 手机号
    private Integer status;       // 状态：0-禁用，1-正常
    private LocalDateTime createTime; // 创建时间
}
