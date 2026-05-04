package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.dto.FeeQueryDTO;
import com.example.dhcpms.vo.FeeSummaryVO;
import com.example.dhcpms.entity.FeeRecord;

/**
 * 费用记录服务层接口
 */
public interface FeeRecordService extends IService<FeeRecord> {
    /**
     * 分页查询费用记录（多条件筛选）
     */
    IPage<FeeRecord> queryFeeRecordPage(IPage<FeeRecord> page, FeeQueryDTO queryDTO);

    /**
     * 计算业主费用汇总（待缴/逾期/已缴）
     */
    FeeSummaryVO calculateFeeSummary(Long ownerId);
}
