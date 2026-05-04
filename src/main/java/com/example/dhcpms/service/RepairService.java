package com.example.dhcpms.service;

import com.example.dhcpms.dto.*;
import com.example.dhcpms.vo.RepairOrderDetailVO;
import com.example.dhcpms.vo.RepairOrderVO;

import java.util.List;
import java.util.Map;

/**
 * 报修服务接口
 */
public interface RepairService {
    // ====================== 业主端核心方法 ======================
    /**
     * 提交报修申请
     */
    Long submitRepairApply(Long userId, RepairApplyDTO repairDTO);

    /**
     * 获取业主报修自动填充的地址
     */
    String getRepairAddress(Long userId);

    /**
     * 查询我的报修列表
     */
    List<RepairOrderVO> getMyRepairList(Long userId);

    /**
     * 查询报修单详情
     */
    RepairOrderDetailVO getRepairDetail(Long repairOrderId, Long userId);

    /**
     * 业主验收报修单
     */
    String handleRepairAcceptance(Long userId, RepairAcceptanceDTO acceptanceDTO);

    /**
     * 重新提交报修申请
     */
    void resubmitRepairApply(Long userId, RepairResubmitDTO resubmitDTO);

    // ====================== 物业端核心方法 ======================
    /**
     * 物业审核报修单
     */
    void handleRepairReview(RepairReviewDTO reviewDTO);

    /**
     * 物业派单
     */
    void assignRepairOrder(RepairAssignDTO assignDTO);

    /**
     * 更新维修进度（标记为待验收）
     */
    void updateRepairProgress(Long repairOrderId);

    /**
     * 物业端获取所有报修列表（带分页）
     * @param status 状态筛选（可选）
     * @param repairTypeId 报修类型筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Map<String, Object> getAdminRepairListWithPage(Integer status, Long repairTypeId, Integer page, Integer size);

    /**
     * 业主取消报修申请
     * @param userId 业主 ID
     * @param cancelDTO 取消报修 DTO
     */
    void cancelRepairApply(Long userId, RepairCancelDTO cancelDTO);

    /**
     * 报修进度实时预测
     * @param repairOrderId 报修单ID
     * @return 预测结果
     */
    Map<String, Object> predictRepairProgress(Long repairOrderId);

}