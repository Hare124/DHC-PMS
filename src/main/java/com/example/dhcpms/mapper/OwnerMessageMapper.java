package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.OwnerMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群聊消息 Mapper 接口
 */
@Mapper
public interface OwnerMessageMapper extends BaseMapper<OwnerMessage> {
}
