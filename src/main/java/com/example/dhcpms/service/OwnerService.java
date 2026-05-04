package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Owner;

/**
 * 业主服务接口
 */
public interface OwnerService extends IService<Owner> {

    /**
     * 分页查询业主列表
     * @param page 分页对象
     * @param searchType 筛选类型（userId/buildingNo/roomNo/idCard）
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    IPage<Owner> getOwnerList(Page<Owner> page, String searchType, String keyword);

    /**
     * 新增业主
     * @param owner 业主信息
     */
    void addOwner(Owner owner);

    /**
     * 修改业主信息
     * @param owner 待修改信息
     */
    void updateOwner(Owner owner);

    /**
     * 删除业主
     * @param id 业主ID
     */
    void deleteOwner(Long id);
    /**
     * 根据用户ID查询业主信息
     * @param userId 用户ID
     * @return 业主信息
     */
    Owner getByUserId(Long userId);

}