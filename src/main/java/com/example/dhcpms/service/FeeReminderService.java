package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.FeeReminder;

import java.util.Map;

/**
 * 催缴记录服务层接口
 */
public interface FeeReminderService extends IService<FeeReminder> {
    /**
     * 业主欠费风险预测
     * @param ownerId 业主ID
     * @return 风险评估结果
     */
    Map<String, Object> predictArrearsRisk(Long ownerId);

}