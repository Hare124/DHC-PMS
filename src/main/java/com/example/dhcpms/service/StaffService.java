package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Staff;

public interface StaffService extends IService<Staff> {

    /**
     * 根据 userId 查询员工信息
     */
    Staff getByUserId(Long userId);

    /**
     * 根据 userId 更新或创建员工信息
     */
    void saveOrUpdateByUserId(Staff staff);
}
