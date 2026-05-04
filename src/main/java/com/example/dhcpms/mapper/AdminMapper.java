package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口（原UserMapper）
 */
@Mapper
public interface AdminMapper extends BaseMapper<User> {
}