package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.*;
import com.example.dhcpms.mapper.*;
import com.example.dhcpms.service.AdminService;
import com.example.dhcpms.service.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理员服务实现类 - 物业人员管理核心逻辑（原UserServiceImpl）
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, User> implements AdminService {

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private FeeRecordMapper feeRecordMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StaffService staffService;

    @Override
    public Long getBuildingCount() {
        // 统计不同的楼栋数量
        // 修复：直接查询所有楼栋，然后在 Java 中去重
        List<Owner> allOwners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>()
                .select(Owner::getBuildingNo));

        // 使用 Stream 去重统计，返回 Long 类型
        return allOwners.stream()
                .map(Owner::getBuildingNo)
                .filter(Objects::nonNull)  // 过滤空值
                .distinct()
                .count();
    }

    @Override
    public Long getTotalHouseholds() {
        // 统计业主总数（一户一个业主）
        return ownerMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    public Long getOccupiedHouseholds() {
        // 统计在住户数（假设已入住的业主）
        // 这里简单统计所有业主，实际可根据具体字段判断
        return ownerMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    public Double getPaymentRate() {
        // 计算本月物业费收缴率 = 已缴费金额 / 应缴费金额 * 100%
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(FeeRecord::getDueDate, startOfMonth)
                .le(FeeRecord::getDueDate, now);

        List<FeeRecord> feeRecords = feeRecordMapper.selectList(queryWrapper);

        if (feeRecords.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;

        for (FeeRecord record : feeRecords) {
            totalAmount = totalAmount.add(record.getAmount());
            if (record.getStatus() == 1) { // 已缴费
                paidAmount = paidAmount.add(record.getActualAmount() != null ? record.getActualAmount() : record.getAmount());
            }
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return paidAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    @Override
    public Long getArrearsHouseholds() {
        // 统计欠费户数（状态=0 未缴费的业主数量）
        LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeRecord::getStatus, 0); // 未缴费

        List<FeeRecord> arrearsRecords = feeRecordMapper.selectList(queryWrapper);

        // 统计不同的业主 ID
        Map<Long, Boolean> uniqueOwners = new HashMap<>();
        for (FeeRecord record : arrearsRecords) {
            uniqueOwners.put(record.getOwnerId(), true);
        }

        return (long) uniqueOwners.size();
    }

    @Override
    public Long getPendingRepairs() {
        // 统计待处理报修数（status=0 待接单 或 status=1 已接单）
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(RepairOrder::getStatus, 0)
                        .or()
                        .eq(RepairOrder::getStatus, 1));

        return repairOrderMapper.selectCount(queryWrapper);
    }

    @Override
    public Long getComplaints() {
        // TODO: 统计投诉建议数（需要投诉表）
        // 暂时返回 0，后续添加投诉表后实现
        return 0L;
    }

    @Override
    public Long getAnnouncements() {
        // 统计公告数量（未撤回的公告：recallStatus=0 或 null）
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(Announcement::getRecallStatus, 0)
                        .or()
                        .isNull(Announcement::getRecallStatus)
        );
        return announcementMapper.selectCount(queryWrapper);
    }
    @Override
    public IPage<User> getStaffList(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 仅查询物业人员（role=1）
        queryWrapper.eq(User::getRole, 1);
        // 支持按姓名/手机号模糊搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreateTime);
        // 分页查询（使用AdminMapper）
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public void addStaff(com.example.dhcpms.dto.StaffAddDTO staffAddDTO) {
        // 1. 创建 User 对象
        User user = new User();
        user.setUsername(staffAddDTO.getUsername());
        user.setName(staffAddDTO.getName());
        user.setPhone(staffAddDTO.getPhone());
        user.setPassword(staffAddDTO.getPassword());
        user.setStatus(staffAddDTO.getStatus() != null ? staffAddDTO.getStatus() : 1);
        user.setRole(1); // 强制设置为物业人员
        user.setCreateTime(LocalDateTime.now());

        // 2. 密码处理（使用 BCrypt 加密）
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("12345678")); // 使用默认密码并加密
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword())); // 对输入的密码加密
        }

        // 3. 插入 User 表
        baseMapper.insert(user);
        log.info("新增物业人员 User 记录成功，userId: {}", user.getId());

        // 4. 插入 Staff 表（关联 userId）
        try {
            Staff staff = new Staff();
            staff.setUserId(user.getId());
            staff.setStaffNo(staffAddDTO.getStaffNo());
            staff.setIdCard(staffAddDTO.getIdCard());
            staff.setStatus(staffAddDTO.getStaffStatus() != null ? staffAddDTO.getStaffStatus() : 1);
            staff.setCreateTime(LocalDateTime.now());
            staff.setUpdateTime(LocalDateTime.now());

            staffService.save(staff);
            log.info("新增物业人员 Staff 记录成功，staffId: {}, userId: {}", staff.getId(), user.getId());
        } catch (Exception e) {
            log.error("新增 Staff 记录失败，userId: {}", user.getId(), e);
            // 如果 Staff 插入失败，回滚 User 记录
            baseMapper.deleteById(user.getId());
            throw new RuntimeException("新增员工信息失败：" + e.getMessage());
        }
    }

    @Override
    public void updateStaff(com.example.dhcpms.dto.StaffUpdateDTO staffUpdateDTO) {
        // 1. 创建 User 对象
        User user = new User();
        user.setId(staffUpdateDTO.getId());
        user.setUsername(staffUpdateDTO.getUsername());
        user.setName(staffUpdateDTO.getName());
        user.setPhone(staffUpdateDTO.getPhone());
        user.setStatus(staffUpdateDTO.getStatus());
        user.setPassword(staffUpdateDTO.getPassword());

        // 2. 禁止修改创建时间，仅更新其他字段
        user.setCreateTime(null);

        // 3. 如果传入了密码且不为空，则进行加密处理
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 如果密码为空，查询原用户信息，保持原密码不变
            User existingUser = baseMapper.selectById(user.getId());
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        }

        // 4. 更新 User 表
        baseMapper.updateById(user);
        log.info("更新物业人员 User 记录成功，userId: {}", user.getId());

        // 5. 更新 Staff 表
        try {
            LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Staff::getUserId, user.getId());
            Staff existingStaff = staffService.getOne(wrapper);

            if (existingStaff != null) {
                // 已存在，更新
                existingStaff.setStaffNo(staffUpdateDTO.getStaffNo());
                existingStaff.setIdCard(staffUpdateDTO.getIdCard());
                existingStaff.setStatus(staffUpdateDTO.getStaffStatus() != null ? staffUpdateDTO.getStaffStatus() : 1);
                existingStaff.setUpdateTime(LocalDateTime.now());
                staffService.updateById(existingStaff);
                log.info("更新物业人员 Staff 记录成功，staffId: {}, userId: {}", existingStaff.getId(), user.getId());
            } else {
                // 不存在，新增
                Staff staff = new Staff();
                staff.setUserId(user.getId());
                staff.setStaffNo(staffUpdateDTO.getStaffNo());
                staff.setIdCard(staffUpdateDTO.getIdCard());
                staff.setStatus(staffUpdateDTO.getStaffStatus() != null ? staffUpdateDTO.getStaffStatus() : 1);
                staff.setCreateTime(LocalDateTime.now());
                staff.setUpdateTime(LocalDateTime.now());
                staffService.save(staff);
                log.info("新增物业人员 Staff 记录成功，staffId: {}, userId: {}", staff.getId(), user.getId());
            }
        } catch (Exception e) {
            log.error("更新 Staff 记录失败，userId: {}", user.getId(), e);
            throw new RuntimeException("更新员工信息失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteStaff(Long id) {
        // 物理删除：直接删除记录
        baseMapper.deleteById(id);
        // 若需逻辑删除，可改为：
        // User user = new User();
        // user.setId(id);
        // user.setStatus(0); // 禁用状态
        // baseMapper.updateById(user);
    }
}