package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.VisitorRecord;
import com.example.dhcpms.mapper.VisitorRecordMapper;
import com.example.dhcpms.service.VisitorRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 访客记录Service实现类
 */
@Service
public class VisitorRecordServiceImpl extends ServiceImpl<VisitorRecordMapper, VisitorRecord> implements VisitorRecordService {

    @Override
    public VisitorRecord getByInviteId(Long inviteId) {
        LambdaQueryWrapper<VisitorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitorRecord::getInviteId, inviteId);
        return this.getOne(wrapper);
    }

    @Override
    public List<VisitorRecord> listByDate(LocalDate date) {
        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

        LambdaQueryWrapper<VisitorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(VisitorRecord::getCheckinTime, start, end)
                .orderByDesc(VisitorRecord::getCheckinTime);
        return this.list(wrapper);
    }
}