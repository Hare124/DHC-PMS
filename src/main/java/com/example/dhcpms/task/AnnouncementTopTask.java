package com.example.dhcpms.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dhcpms.entity.Announcement;
import com.example.dhcpms.service.AnnouncementService;
import com.example.dhcpms.service.MsgPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告置顶到期自动取消任务
 */
@Component
@RequiredArgsConstructor
public class AnnouncementTopTask {

    private final AnnouncementService announcementService;
    private final MsgPushService msgPushService;

    /**
     * 每天凌晨执行，取消7天前的置顶公告
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cancelExpiredTopAnnouncements() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getIsTop, 1)
                .lt(Announcement::getPublishTime, sevenDaysAgo)
                .eq(Announcement::getRecallStatus, 0);

        // 批量取消置顶
        Announcement updateEntity = new Announcement();
        updateEntity.setIsTop(0);
        announcementService.update(updateEntity, wrapper);

        // 推送消息
        List<Announcement> expiredList = announcementService.list(wrapper);
        expiredList.forEach(ann -> msgPushService.pushAnnouncementTopStatusUpdateMsg(ann.getId(), 0));
    }
}