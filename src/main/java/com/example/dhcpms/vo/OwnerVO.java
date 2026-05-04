package com.example.dhcpms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业主列表展示 VO
 */
@Data
public class OwnerVO {
    private Long id;              // 业主 ID
    private Long userId;          // 用户 ID
    private String roomNo;        // 房号（楼栋号 + 房间号）
    private String ownerName;     // 产权人姓名
    private String phone;         // 联系电话
    private String idCard;        // 身份证号
    private LocalDateTime registerTime;   // 注册时间

    // ===== 房屋面积信息 =====
    private BigDecimal houseArea;         // 房屋建筑面积
    private BigDecimal internalArea;      // 套内建筑面积

    // ===== 房屋基本信息 =====
    private String houseLayout;           // 房屋户型
    private String houseUsage;            // 房屋用途
    private String houseStructure;        // 房屋结构

    // ===== 共有情况 =====
    private String ownershipType;         // 共有情况
    private String coOwnerName;           // 共有人姓名
    private String coOwnerIdCard;         // 共有人身份证号

    // ===== 产权证书信息 =====
    private String propertyCertNo;        // 不动产权证书号
    private String propertyUnitNo;        // 不动产单元号
    private LocalDate registrationDate;   // 登记日期
    private String registrationAuthority; // 发证机关
    private Integer landUseYears;         // 土地使用年限
    private String landNature;            // 土地性质

    // ===== 抵押查封情况 =====
    private Boolean isMortgaged;          // 是否抵押
    private BigDecimal mortgageAmount;    // 抵押金额
    private String mortgageeName;         // 抵押权人姓名
    private Boolean isSeized;             // 是否查封

    // ===== 居住权情况 =====
    private Boolean hasResidenceRight;    // 是否设立居住权

    // ===== 房屋交付信息 =====
    private LocalDate deliveryDate;       // 房屋交付日期
    private String registeredUsage;       // 产权登记用途

    // ===== 配套设施信息 =====
    private Boolean hasParkingSpace;      // 是否配套车位
    private Boolean hasStorageRoom;       // 是否配套储藏室
    private String parkingSpaceType;      // 车位产权类型
    private String parkingSpaceNo;        // 车位编号
}
