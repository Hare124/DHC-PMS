package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 报修类型实体类
 * 对应数据库表：repair_type
 */
@Data
@TableName("repair_type")
public class RepairType {
    @TableId(type = IdType.AUTO)
    private Long id;             // 主键
    private String typeName;     // 报修类型名称（水电维修/家电维修/公共区域维修）
}