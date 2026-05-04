package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Equipment;

/**
 * 设备信息服务接口
 */
public interface EquipmentService extends IService<Equipment> {

    /**
     * 分页查询设备列表
     */
    IPage<Equipment> getEquipmentList(Page<Equipment> page, String keyword, String type, String status);
}
