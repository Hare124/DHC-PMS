package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Announcement;
import com.example.dhcpms.mapper.AnnouncementMapper;
import com.example.dhcpms.dto.AnnouncementPublishDTO;
import com.example.dhcpms.dto.AnnouncementQueryDTO;
import com.example.dhcpms.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 公告Service实现类
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    // 模拟定时任务存储（实际项目建议用数据库/Redis）
    private final Map<Long, AnnouncementPublishDTO> scheduledTasks = new HashMap<>();

    @Override
    public IPage<Announcement> queryOwnerAnnouncementList(Page<Announcement> page, AnnouncementQueryDTO queryDTO) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        // 时间范围筛选
        if (queryDTO.getStartTime() != null) wrapper.ge(Announcement::getPublishTime, queryDTO.getStartTime());
        if (queryDTO.getEndTime() != null) wrapper.le(Announcement::getPublishTime, queryDTO.getEndTime());
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Announcement::getTitle, queryDTO.getKeyword())
                    .or().like(Announcement::getContent, queryDTO.getKeyword()));
        }
        // 仅展示未撤回公告
        wrapper.eq(Announcement::getRecallStatus, 0);
        // 排序：置顶降序 + 发布时间降序
        wrapper.orderByDesc(Announcement::getIsTop, Announcement::getPublishTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createScheduledPublishTask(AnnouncementPublishDTO publishDTO, Long managerUserId) {
        // 创建公告记录并保存到数据库
        Announcement announcement = new Announcement();
        announcement.setTitle(publishDTO.getTitle());
        announcement.setContent(publishDTO.getContent());
        announcement.setPublisherId(managerUserId);
        // 设置定时发布时间（未来时间）
        announcement.setPublishTime(publishDTO.getScheduledPublishTime());
        announcement.setIsTop(publishDTO.getIsTop() == null ? 0 : publishDTO.getIsTop());
        announcement.setReadCount(0);
        announcement.setRecallStatus(0); // 未撤回状态

        // 保存到数据库
        if (this.save(announcement)) {
            System.out.println("定时发布公告已保存：ID=" + announcement.getId() + "，发布时间=" + publishDTO.getScheduledPublishTime());
        } else {
            throw new RuntimeException("定时发布公告保存失败");
        }
    }

    /**
     * 定时任务执行器（每分钟检查）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeScheduledTasks() {
        LocalDateTime now = LocalDateTime.now();
        scheduledTasks.forEach((taskId, dto) -> {
            if (dto.getScheduledPublishTime().isBefore(now) || dto.getScheduledPublishTime().isEqual(now)) {
                Announcement announcement = new Announcement();
                announcement.setTitle(dto.getTitle());
                announcement.setContent(dto.getContent());
                announcement.setPublisherId(dto.getPublisherId());
                announcement.setPublishTime(LocalDateTime.now());
                announcement.setIsTop(dto.getIsTop() == null ? 0 : dto.getIsTop());
                announcement.setReadCount(0);
                announcement.setRecallStatus(0);
                this.save(announcement);
                scheduledTasks.remove(taskId);
                System.out.println("定时任务执行：公告ID=" + announcement.getId());
            }
        });
    }
}