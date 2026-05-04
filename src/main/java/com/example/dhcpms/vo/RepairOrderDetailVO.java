package com.example.dhcpms.vo;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 报修订单详情 VO
 */
@Data
public class RepairOrderDetailVO {
    // 基础信息
    private Long id;                // 报修订单 ID
    private String repairOrderNo;   // 报修单号
    private String title;           // 报修标题
    private String content;         // 报修内容
    private String address;         // 详细地址
    private LocalDateTime createTime; // 创建时间
    private String voucherUrls;     // 故障凭证链接（拼接后的字符串）

    // 状态信息
    private Integer status;         // 状态编码
    private String statusDesc;      // 状态描述
    private Long handlerId;         // 维修人员 ID
    private String handlerName;     // 维修人员姓名
    private String handlerPhone;    // 维修人员联系方式
    private LocalDateTime completeTime; // 完成时间
    private String expectedVisitTime; // 预计上门时间
    private String assignRemark;    // 派单备注

    // 报修类型信息
    private String repairTypeName;  // 报修类型名称

    // 评价信息（仅已完成状态展示）
    private Integer evaluationScore;          // 评分
    private String evaluationComment;         // 评价内容
    private LocalDateTime evaluationTime;     // 评价时间
}
