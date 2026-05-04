package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Equipment;
import com.example.dhcpms.mapper.EquipmentMapper;
import com.example.dhcpms.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 设备服务实现类
 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public IPage<Equipment> getEquipmentList(Page<Equipment> page, String keyword, String type, String status) {
        LambdaQueryWrapper<Equipment> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索（设备名称、编号、位置）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Equipment::getEquipmentName, keyword)
                    .or()
                    .like(Equipment::getEquipmentNo, keyword)
                    .or()
                    .like(Equipment::getInstallLocation, keyword));
        }

        // 按类型筛选
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Equipment::getEquipmentType, type);
        }

        // 按状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Equipment::getStatus, status);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Equipment::getCreateTime);

        return baseMapper.selectPage(page, queryWrapper);
    }
}
