package com.example.dhcpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dhcpms.entity.RepairEvaluation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * RepairEvaluation Mapper
 */
public interface RepairEvaluationMapper extends BaseMapper<RepairEvaluation> {
    // 根据报修订单ID查询评价
    @Select("SELECT * FROM repair_evaluation WHERE repair_order_id = #{repairOrderId}")
    RepairEvaluation selectByRepairOrderId(@Param("repairOrderId") Long repairOrderId);
}
