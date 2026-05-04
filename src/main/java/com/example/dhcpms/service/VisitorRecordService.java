package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.VisitorRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 访客记录Service接口
 */
public interface VisitorRecordService extends IService<VisitorRecord> {
    // 根据邀请ID查询通行记录
    VisitorRecord getByInviteId(Long inviteId);

    // 查询指定日期的访客记录
    List<VisitorRecord> listByDate(LocalDate date);
}