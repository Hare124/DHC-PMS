package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.VisitorRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客记录Mapper接口
 */
@Mapper
public interface VisitorRecordMapper extends BaseMapper<VisitorRecord> {
}