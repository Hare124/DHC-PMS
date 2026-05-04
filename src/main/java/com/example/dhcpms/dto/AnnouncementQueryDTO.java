package com.example.dhcpms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 公告查询入参DTO（分页+筛选）
 */
@Data
public class AnnouncementQueryDTO {
    private Integer pageNum = 1;    // 页码
    private Integer pageSize = 10;  // 页大小

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;    // 开始时间（DATE 类型）

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;      // 结束时间（DATE 类型）

    private Long publisherId;       // 发布人 ID
    private Integer isTop;          // 是否置顶
    private String keyword;         // 搜索关键词
    private Integer recallStatus;   // 撤回状态（物业端筛选用）
}