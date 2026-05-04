package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.FeeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 费用记录Mapper接口
 */
@Mapper
public interface FeeRecordMapper extends BaseMapper<FeeRecord> {
}
