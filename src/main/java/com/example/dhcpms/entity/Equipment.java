package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 配套设备实体类，用于表示小区配套设备信息。
 */
@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键

    private String equipmentNo;         // 设备编号
    private String equipmentName;       // 设备名称
    private String equipmentType;       // 设备类型（电梯、消防、供水、供电、照明、安防等）
    private String brand;               // 品牌
    private String model;               // 型号

    private String installLocation;     // 安装位置
    private String buildingNo;          // 所属楼栋号
    private String area;                // 所属区域

    private LocalDate purchaseDate;     // 采购日期
    private Integer warrantyPeriod;     // 质保期（月）
    private Integer serviceLife;        // 使用年限（年）

    private String status;              // 设备状态（normal-正常、fault-故障、maintaining-维修中、disabled-停用、scrapped-报废）

    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
