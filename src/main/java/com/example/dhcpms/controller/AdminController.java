package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.entity.*;
import com.example.dhcpms.service.*;
import com.example.dhcpms.vo.StaffVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员控制器 - 物业人员信息管理（基于User实体，仅操作role=1的用户）
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @Autowired
    private AdminService adminService; // 注入AdminService

    @Autowired
    private EquipmentService equipmentService; // 注入 EquipmentService

    @Autowired
    private RegulationService regulationService; // 注入 RegulationService

    @Autowired
    private CarouselService carouselService; // 注入 CarouselService

    @Autowired
    private StaffService staffService; // 注入 StaffService

    // 1. 查询物业人员列表（仅查询role=1的用户）
    @GetMapping("/property-staff")
    public Result<?> getStaffList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> staffPage = adminService.getStaffList(page, keyword);

        // 转换为 StaffVO 并关联 Staff 表数据
        List<StaffVO> voList = staffPage.getRecords().stream().map(user -> {
            StaffVO vo = new StaffVO();
            // 复制 User 字段
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setName(user.getName());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setRole(user.getRole());
            vo.setCreateTime(user.getCreateTime());

            // 关联 Staff 表字段
            Staff staff = staffService.getByUserId(user.getId());
            if (staff != null) {
                vo.setStaffId(staff.getId());
                vo.setStaffNo(staff.getStaffNo());
                vo.setIdCard(staff.getIdCard());
                vo.setStaffStatus(staff.getStatus());
            }

            return vo;
        }).collect(Collectors.toList());

        // 构造分页结果
        Page<StaffVO> voPage = new Page<>(staffPage.getCurrent(), staffPage.getSize(), staffPage.getTotal());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    // 2. 新增物业人员（强制设置role=1）
    @PostMapping("/property-staff")
    public Result<?> addStaff(@RequestBody @Valid com.example.dhcpms.dto.StaffAddDTO staffAddDTO) {
        try {
            adminService.addStaff(staffAddDTO);
            return Result.success("新增物业人员成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增物业人员失败：" + e.getMessage());
        }
    }

    // 3. 修改物业人员信息（禁止修改role字段）
    @PutMapping("/property-staff/{id}")
    public Result<?> updateStaff(@PathVariable Long id, @RequestBody com.example.dhcpms.dto.StaffUpdateDTO staffUpdateDTO) {
        try {
            // 确保路径 ID 和 DTO 中的 ID 一致
            if (!id.equals(staffUpdateDTO.getId())) {
                return Result.error("ID不匹配");
            }

            adminService.updateStaff(staffUpdateDTO);
            return Result.success("修改物业人员信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改物业人员信息失败：" + e.getMessage());
        }
    }

    // 4. 删除物业人员
    @DeleteMapping("/property-staff/{id}")
    public Result<?> deleteStaff(@PathVariable Long id) {
        adminService.deleteStaff(id);
        return Result.success("删除物业人员成功");
    }

    // 5. 修改密码
    @PutMapping("/change-password")
    public Result<?> changePassword(
            @RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        System.out.println("===== 开始修改密码 =====");
        System.out.println("userId: " + userId);

        // 1. 查询用户
        User user = adminService.getById(userId);
        if (user == null) {
            System.out.println("用户不存在");
            return Result.error("用户不存在");
        }

        System.out.println("查询到的用户：" + user.getUsername());

        // 2. 验证旧密码
        PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            System.out.println("旧密码错误");
            return Result.error("原密码错误");
        }

        System.out.println("旧密码验证通过");

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        adminService.updateById(user);

        System.out.println("密码修改成功");
        System.out.println("===== 修改密码完成 =====");

        return Result.success("密码修改成功");
    }

    // 6. 获取首页概览统计数据
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        System.out.println("===== 开始获取概览统计数据 =====");

        Map<String, Object> overview = new HashMap<>();

        try {
            // TODO: 调用 Service 层获取真实数据
            // 这里先返回模拟数据，后续由 Service 层实现
            overview.put("buildingCount", adminService.getBuildingCount());
            overview.put("totalHouseholds", adminService.getTotalHouseholds());
            overview.put("occupiedHouseholds", adminService.getOccupiedHouseholds());
            overview.put("paymentRate", adminService.getPaymentRate());
            overview.put("arrearsHouseholds", adminService.getArrearsHouseholds());
            overview.put("pendingRepairs", adminService.getPendingRepairs());
            overview.put("complaints", adminService.getComplaints());
            overview.put("announcements", adminService.getAnnouncements());

            System.out.println("概览数据：" + overview);
            System.out.println("===== 获取概览数据完成 =====");

            return Result.success(overview);
        } catch (Exception e) {
            System.out.println("获取概览数据失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error("获取概览数据失败");
        }
    }

    // ==================== 设备管理接口 ====================

    /**
     * 分页查询设备列表
     */
    @GetMapping("/equipment/list")
    public Result<IPage<Equipment>> getEquipmentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        Page<Equipment> page = new Page<>(pageNum, pageSize);
        IPage<Equipment> equipmentPage = equipmentService.getEquipmentList(page, keyword, type, status);
        return Result.success(equipmentPage);
    }

    /**
     * 根据 ID 查询设备详情
     */
    @GetMapping("/equipment/{id}")
    public Result<Equipment> getEquipmentById(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return Result.error("设备不存在");
        }
        return Result.success(equipment);
    }

    /**
     * 新增设备
     */
    @PostMapping("/equipment")
    public Result<?> addEquipment(@RequestBody @Valid Equipment equipment) {
        equipment.setCreateTime(LocalDateTime.now());
        equipment.setUpdateTime(LocalDateTime.now());
        boolean success = equipmentService.save(equipment);
        return success ? Result.success("新增设备成功") : Result.error("新增设备失败");
    }

    /**
     * 修改设备信息
     */
    @PutMapping("/equipment/{id}")
    public Result<?> updateEquipment(@PathVariable Long id, @RequestBody @Valid Equipment equipment) {
        equipment.setId(id);
        equipment.setUpdateTime(LocalDateTime.now());
        boolean success = equipmentService.updateById(equipment);
        return success ? Result.success("修改设备信息成功") : Result.error("修改设备信息失败");
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/equipment/{id}")
    public Result<?> deleteEquipment(@PathVariable Long id) {
        boolean success = equipmentService.removeById(id);
        return success ? Result.success("删除设备成功") : Result.error("删除设备失败");
    }

    /**
     * 更新设备状态
     */
    @PutMapping("/equipment/{id}/status")
    public Result<?> updateEquipmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return Result.error("设备不存在");
        }

        equipment.setStatus(status);
        equipment.setUpdateTime(LocalDateTime.now());
        boolean success = equipmentService.updateById(equipment);
        return success ? Result.success("设备状态更新成功") : Result.error("设备状态更新失败");
    }

    // ==================== 制度管理接口 ====================

    /**
     * 分页查询制度列表
     */
    @GetMapping("/regulation/list")
    public Result<IPage<Regulation>> getRegulationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        Page<Regulation> page = new Page<>(pageNum, pageSize);
        IPage<Regulation> regulationPage = regulationService.getRegulationList(page, keyword, type, status);
        return Result.success(regulationPage);
    }

    /**
     * 根据 ID 查询制度详情
     */
    @GetMapping("/regulation/{id}")
    public Result<Regulation> getRegulationById(@PathVariable Long id) {
        Regulation regulation = regulationService.getById(id);
        if (regulation == null) {
            return Result.error("制度不存在");
        }
        return Result.success(regulation);
    }

    /**
     * 新增制度
     */
    @PostMapping("/regulation")
    public Result<?> addRegulation(@RequestBody @Valid Regulation regulation,
                                   @RequestParam Long createBy,
                                   @RequestParam String createByName) {
        regulation.setCreateBy(createBy);
        regulation.setCreateByName(createByName);
        regulation.setCreateTime(LocalDateTime.now());
        regulation.setUpdateTime(LocalDateTime.now());
        regulation.setVersion(1);
        regulation.setStatus("draft");
        boolean success = regulationService.save(regulation);
        return success ? Result.success("新增制度成功") : Result.error("新增制度失败");
    }

    /**
     * 修改制度信息
     */
    @PutMapping("/regulation/{id}")
    public Result<?> updateRegulation(@PathVariable Long id,
                                      @RequestBody @Valid Regulation regulation,
                                      @RequestParam Long updateBy,
                                      @RequestParam String updateByName) {
        regulation.setId(id);
        regulation.setUpdateBy(updateBy);
        regulation.setUpdateByName(updateByName);
        regulation.setUpdateTime(LocalDateTime.now());
        boolean success = regulationService.updateById(regulation);
        return success ? Result.success("修改制度信息成功") : Result.error("修改制度信息失败");
    }

    /**
     * 删除制度
     */
    @DeleteMapping("/regulation/{id}")
    public Result<?> deleteRegulation(@PathVariable Long id) {
        boolean success = regulationService.removeById(id);
        return success ? Result.success("删除制度成功") : Result.error("删除制度失败");
    }

    /**
     * 创建新版本制度
     */
    @PostMapping("/regulation/{id}/newVersion")
    public Result<?> createNewVersion(@PathVariable Long id,
                                      @RequestBody @Valid Regulation newRegulation,
                                      @RequestParam Long createBy,
                                      @RequestParam String createByName) {
        newRegulation.setCreateBy(createBy);
        newRegulation.setCreateByName(createByName);
        Regulation regulation = regulationService.createNewVersion(id, newRegulation);
        if (regulation == null) {
            return Result.error("原制度不存在");
        }
        return Result.success("创建新版本制度成功，新版本 ID: " + regulation.getId());
    }

    /**
     * 发布制度
     */
    @PutMapping("/regulation/{id}/publish")
    public Result<?> publishRegulation(@PathVariable Long id) {
        Regulation regulation = regulationService.getById(id);
        if (regulation == null) {
            return Result.error("制度不存在");
        }
        regulation.setStatus("published");
        regulation.setUpdateTime(LocalDateTime.now());
        boolean success = regulationService.updateById(regulation);
        return success ? Result.success("制度发布成功") : Result.error("制度发布失败");
    }

    /**
     * 下架制度
     */
    @PutMapping("/regulation/{id}/suspend")
    public Result<?> suspendRegulation(@PathVariable Long id) {
        Regulation regulation = regulationService.getById(id);
        if (regulation == null) {
            return Result.error("制度不存在");
        }
        regulation.setStatus("suspended");
        regulation.setUpdateTime(LocalDateTime.now());
        boolean success = regulationService.updateById(regulation);
        return success ? Result.success("制度下架成功") : Result.error("制度下架失败");
    }

    /**
     * 作废制度
     */
    @PutMapping("/regulation/{id}/invalidate")
    public Result<?> invalidateRegulation(@PathVariable Long id) {
        Regulation regulation = regulationService.getById(id);
        if (regulation == null) {
            return Result.error("制度不存在");
        }
        regulation.setStatus("invalid");
        regulation.setUpdateTime(LocalDateTime.now());
        boolean success = regulationService.updateById(regulation);
        return success ? Result.success("制度作废成功") : Result.error("制度作废失败");
    }

    // ==================== 轮播图管理接口 ====================

    /**
     * 分页查询轮播图列表
     */
    @GetMapping("/carousel/list")
    public Result<IPage<Carousel>> getCarouselList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<Carousel> page = new Page<>(pageNum, pageSize);
        IPage<Carousel> carouselPage = carouselService.getCarouselList(page, keyword, status);
        return Result.success(carouselPage);
    }

    /**
     * 查询启用的轮播图（前端首页使用）
     */
    @GetMapping("/carousel/active")
    public Result<List<Carousel>> getActiveCarousels() {
        List<Carousel> carousels = carouselService.getActiveCarousels();
        return Result.success(carousels);
    }

    /**
     * 根据 ID 查询轮播图详情
     */
    @GetMapping("/carousel/{id}")
    public Result<Carousel> getCarouselById(@PathVariable Long id) {
        Carousel carousel = carouselService.getById(id);
        if (carousel == null) {
            return Result.error("轮播图不存在");
        }
        return Result.success(carousel);
    }

    /**
     * 新增轮播图
     */
    @PostMapping("/carousel")
    public Result<?> addCarousel(@RequestBody @Valid Carousel carousel,
                                 @RequestParam Long createBy,
                                 @RequestParam String createByName) {
        carousel.setCreateBy(createBy);
        carousel.setCreateByName(createByName);
        carousel.setCreateTime(LocalDateTime.now());
        carousel.setUpdateTime(LocalDateTime.now());
        if (carousel.getStatus() == null) {
            carousel.setStatus("1"); // 默认启用
        }
        boolean success = carouselService.save(carousel);
        return success ? Result.success("新增轮播图成功") : Result.error("新增轮播图失败");
    }

    /**
     * 修改轮播图信息
     */
    @PutMapping("/carousel/{id}")
    public Result<?> updateCarousel(@PathVariable Long id,
                                    @RequestBody @Valid Carousel carousel,
                                    @RequestParam Long updateBy,
                                    @RequestParam String updateByName) {
        carousel.setId(id);
        carousel.setUpdateBy(updateBy);
        carousel.setUpdateByName(updateByName);
        carousel.setUpdateTime(LocalDateTime.now());
        boolean success = carouselService.updateById(carousel);
        return success ? Result.success("修改轮播图信息成功") : Result.error("修改轮播图信息失败");
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/carousel/{id}")
    public Result<?> deleteCarousel(@PathVariable Long id) {
        boolean success = carouselService.removeById(id);
        return success ? Result.success("删除轮播图成功") : Result.error("删除轮播图失败");
    }

    /**
     * 更新轮播图状态
     */
    @PutMapping("/carousel/{id}/status")
    public Result<?> updateCarouselStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Carousel carousel = carouselService.getById(id);
        if (carousel == null) {
            return Result.error("轮播图不存在");
        }

        carousel.setStatus(status);
        carousel.setUpdateTime(LocalDateTime.now());
        boolean success = carouselService.updateById(carousel);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
}