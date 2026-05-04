package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Announcement;
import com.example.dhcpms.dto.AnnouncementPublishDTO;
import com.example.dhcpms.dto.AnnouncementQueryDTO;

/**
 * 公告Service接口
 */
public interface AnnouncementService extends IService<Announcement> {
    void createScheduledPublishTask(AnnouncementPublishDTO publishDTO, Long managerUserId);
    IPage<Announcement> queryOwnerAnnouncementList(Page<Announcement> page, AnnouncementQueryDTO queryDTO);
}