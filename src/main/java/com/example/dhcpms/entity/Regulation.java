package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物业制度实体类，用于表示物业规章制度信息。
 */
@Data
@TableName("regulation")
public class Regulation {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键

    private String regulationNo;        // 制度编号
    private String regulationName;      // 制度名称
    private String regulationType;      // 制度类型（收费管理/消防安全/装修管理/车辆管理/其他）

    private String content;             // 制度内容（富文本）
    private String attachmentUrl;       // 附件 URL（PDF 等文件）
    private String attachmentName;      // 附件名称

    private String applyScope;          // 适用范围（全体业主/某栋楼）
    private LocalDate effectiveDate;    // 生效日期
    private LocalDate invalidDate;      // 失效日期

    private Integer version;            // 版本号（从 1 开始）
    private Long parentRegulationId;    // 父制度 ID（用于版本控制）

    private String status;              // 状态（draft-草稿/published-已发布/suspended-已下架/invalid-已作废）

    private Long createBy;              // 创建人 ID
    private String createByName;        // 创建人姓名
    private LocalDateTime createTime;   // 创建时间

    private Long updateBy;              // 更新人 ID
    private String updateByName;        // 更新人姓名
    private LocalDateTime updateTime;   // 更新时间
}
