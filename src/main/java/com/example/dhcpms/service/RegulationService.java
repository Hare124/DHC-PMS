package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Regulation;

/**
 * 物业制度服务接口
 */
public interface RegulationService extends IService<Regulation> {

    /**
     * 分页查询制度列表
     */
    IPage<Regulation> getRegulationList(Page<Regulation> page, String keyword, String type, String status);

    /**
     * 创建新版本制度
     */
    Regulation createNewVersion(Long regulationId, Regulation newRegulation);
}
