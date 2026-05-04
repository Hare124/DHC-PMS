package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Staff;
import com.example.dhcpms.mapper.StaffMapper;
import com.example.dhcpms.service.StaffService;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

    @Override
    public Staff getByUserId(Long userId) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getUserId, userId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void saveOrUpdateByUserId(Staff staff) {
        Staff existingStaff = getByUserId(staff.getUserId());
        if (existingStaff != null) {
            staff.setId(existingStaff.getId());
            baseMapper.updateById(staff);
        } else {
            baseMapper.insert(staff);
        }
    }
}
