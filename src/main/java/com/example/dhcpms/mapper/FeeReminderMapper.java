package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.FeeReminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 催缴记录Mapper接口
 */
@Mapper
public interface FeeReminderMapper extends BaseMapper<FeeReminder> {
}
