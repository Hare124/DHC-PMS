package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业主实体类，用于表示业主的详细信息。
 */
@Data
@TableName("owner") // 对应数据库表名
public class Owner {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;              // 主键
    private Long userId;          // 用户 ID（关联 User 表）
    private String idCard;        // 身份证号
    private String buildingNo;    // 楼栋号
    private String roomNo;        // 房间号

    // ===== 房屋面积信息 =====
    private BigDecimal houseArea;         // 房屋建筑面积（平方米）
    private BigDecimal internalArea;      // 套内建筑面积（平方米）

    // ===== 房屋基本信息 =====
    private String houseLayout;           // 房屋户型（如：三室两厅一卫）
    private String houseUsage;            // 房屋用途（住宅 / 商业 / 办公）
    private String houseStructure;        // 房屋结构（砖混 / 框架等）

    // ===== 共有情况 =====
    private String ownershipType;         // 共有情况：单独所有 / 共同共有 / 按份共有
    private String coOwnerName;           // 共有人姓名（如有）
    private String coOwnerIdCard;         // 共有人身份证号（如有）

    // ===== 产权证书信息 =====
    private String propertyCertNo;        // 不动产权证书号（房产证号）
    private String propertyUnitNo;        // 不动产单元号
    private LocalDate registrationDate;   // 登记日期
    private String registrationAuthority; // 发证机关
    private Integer landUseYears;         // 土地使用年限（年）
    private String landNature;            // 土地性质（出让/划拨等）

    // ===== 抵押查封情况 =====
    private Boolean isMortgaged;          // 是否抵押
    private BigDecimal mortgageAmount;    // 抵押金额
    private String mortgageeName;         // 抵押权人姓名（如有）
    private Boolean isSeized;             // 是否查封

    // ===== 居住权情况 =====
    private Boolean hasResidenceRight;    // 是否设立居住权

    // ===== 房屋交付信息 =====
    private LocalDate deliveryDate;       // 房屋交付日期
    private String registeredUsage;       // 产权登记用途

    // ===== 配套设施信息 =====
    private Boolean hasParkingSpace;      // 是否配套车位
    private Boolean hasStorageRoom;       // 是否配套储藏室
    private String parkingSpaceType;      // 车位产权类型（产权/租赁）
    private String parkingSpaceNo;        // 车位编号（如有）

    private LocalDateTime registerTime;   // 注册时间
}
