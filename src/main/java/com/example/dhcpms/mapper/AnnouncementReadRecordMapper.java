package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.AnnouncementReadRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告已读记录Mapper
 */
@Mapper
public interface AnnouncementReadRecordMapper extends BaseMapper<AnnouncementReadRecord> {
}