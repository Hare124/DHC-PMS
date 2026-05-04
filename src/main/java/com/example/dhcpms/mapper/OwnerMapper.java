package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.Owner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 业主Mapper接口
 */
@Mapper
public interface OwnerMapper extends BaseMapper<Owner> {
    // 根据user_id查询业主信息
    @Select("SELECT * FROM owner WHERE user_id = #{userId}")
    Owner selectByUserId(@Param("userId") Long userId);
}