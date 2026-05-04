package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备信息 Mapper 接口
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
}
