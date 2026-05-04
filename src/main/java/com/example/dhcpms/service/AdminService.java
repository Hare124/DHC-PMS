package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.User;

/**
 * 管理员服务接口 - 聚焦物业人员管理（原UserService）
 */
public interface AdminService extends IService<User> {

    /**
     * 分页查询物业人员列表（role=1）
     * @param page 分页对象
     * @param keyword 搜索关键词（姓名/手机号）
     * @return 分页结果
     */
    IPage<User> getStaffList(Page<User> page, String keyword);

    /**
     * 新增物业人员
      * @param staffAddDTO 物业人员信息
     */
    void addStaff(com.example.dhcpms.dto.StaffAddDTO staffAddDTO);

    /**
     * 修改物业人员信息
      * @param staffUpdateDTO 待修改的信息
     */
    void updateStaff(com.example.dhcpms.dto.StaffUpdateDTO staffUpdateDTO);

    /**
     * 删除物业人员
     * @param id 物业人员ID
     */
    void deleteStaff(Long id);

    /**
     * 获取小区楼栋数
     * @return 楼栋数
     */
    Long getBuildingCount();

    /**
     * 获取总户数
     * @return 总户数
     */
    Long getTotalHouseholds();

    /**
     * 获取在住户数
     * @return 在住户数
     */
    Long getOccupiedHouseholds();

    /**
     * 获取本月物业费收缴率
     * @return 收缴率（百分比）
     */
    Double getPaymentRate();

    /**
     * 获取欠费户数
     * @return 欠费户数
     */
    Long getArrearsHouseholds();

    /**
     * 获取待处理报修数
     * @return 待处理报修数
     */
    Long getPendingRepairs();

    /**
     * 获取投诉建议数
     * @return 投诉建议数
     */
    Long getComplaints();

    /**
     * 获取公告数量
     * @return 公告数量
     */
    Long getAnnouncements();

}