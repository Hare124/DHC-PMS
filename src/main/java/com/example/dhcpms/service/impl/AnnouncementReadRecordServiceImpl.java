package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.AnnouncementReadRecord;
import com.example.dhcpms.mapper.AnnouncementReadRecordMapper;
import com.example.dhcpms.service.AnnouncementReadRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告已读记录Service实现类
 */
@Service
@RequiredArgsConstructor
public class AnnouncementReadRecordServiceImpl extends ServiceImpl<AnnouncementReadRecordMapper, AnnouncementReadRecord>
        implements AnnouncementReadRecordService {

    @Override
    public List<Long> getReadAnnouncementIdsByOwnerUserId(Long ownerUserId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getOwnerUserId, ownerUserId);
        return this.list(wrapper).stream()
                .map(AnnouncementReadRecord::getAnnouncementId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long ownerUserId, Long announcementId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getOwnerUserId, ownerUserId)
                .eq(AnnouncementReadRecord::getAnnouncementId, announcementId);

        AnnouncementReadRecord readRecord = this.getOne(wrapper);
        if (readRecord == null) {
            readRecord = new AnnouncementReadRecord();
            readRecord.setOwnerUserId(ownerUserId);
            readRecord.setAnnouncementId(announcementId);
            readRecord.setReadTime(LocalDateTime.now());
            this.save(readRecord);
        } else {
            readRecord.setReadTime(LocalDateTime.now());
            this.updateById(readRecord);
        }
    }
}