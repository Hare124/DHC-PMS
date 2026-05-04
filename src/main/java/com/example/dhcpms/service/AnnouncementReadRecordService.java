package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.AnnouncementReadRecord;

import java.util.List;

/**
 * 公告已读记录Service接口
 */
public interface AnnouncementReadRecordService extends IService<AnnouncementReadRecord> {
    List<Long> getReadAnnouncementIdsByOwnerUserId(Long ownerUserId);
    void markAsRead(Long ownerUserId, Long announcementId);
}