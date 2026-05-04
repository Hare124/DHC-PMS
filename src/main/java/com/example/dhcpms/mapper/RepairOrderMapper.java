package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.RepairOrder;
import com.example.dhcpms.vo.RepairOrderDetailVO;
import com.example.dhcpms.vo.RepairOrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

import java.util.List;

/**
 * RepairOrder Mapper
 */
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
    @Select("SELECT ro.id, ro.repair_order_no, ro.title, ro.status, ro.create_time, ro.address, rt.type_name AS repair_type_name FROM repair_order ro LEFT JOIN repair_type rt ON ro.repair_type_id = rt.id WHERE ro.owner_id = #{ownerId} ORDER BY ro.create_time DESC")
    @Results(id = "repairOrderVOMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "repairOrderNo", column = "repair_order_no"),
            @Result(property = "title", column = "title"),
            @Result(property = "repairTypeName", column = "repair_type_name"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "address", column = "address")
    })
    List<RepairOrderVO> selectOwnerRepairList(@Param("ownerId") Long ownerId);

    @Select("SELECT ro.id, ro.title, ro.content, ro.status, ro.create_time, ro.complete_time, ro.address, rt.type_name AS repair_type_name, u.username AS handler_name FROM repair_order ro LEFT JOIN repair_type rt ON ro.repair_type_id = rt.id LEFT JOIN user u ON ro.handler_id = u.id WHERE ro.id = #{repairOrderId}")
    @Results(id = "repairOrderDetailVOMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "completeTime", column = "complete_time"),
        @Result(property = "address", column = "address"),
        @Result(property = "repairTypeName", column = "repair_type_name"),
        @Result(property = "handlerName", column = "handler_name")
    })
    RepairOrderDetailVO selectRepairDetail(@Param("repairOrderId") Long repairOrderId);
}
