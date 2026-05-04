package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("carousel")
public class Carousel {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键

    private String title;               // 轮播图标题
    private String imageUrl;            // 图片 URL
    private String targetUrl;           // 跳转链接（可选）
    private Integer sortOrder;          // 排序顺序（数字越小越靠前）
    private String status;              // 状态（1-启用，0-禁用）

    private Long createBy;              // 创建人 ID
    private String createByName;        // 创建人姓名
    private LocalDateTime createTime;   // 创建时间

    private Long updateBy;              // 更新人 ID
    private String updateByName;        // 更新人姓名
    private LocalDateTime updateTime;   // 更新时间
}
