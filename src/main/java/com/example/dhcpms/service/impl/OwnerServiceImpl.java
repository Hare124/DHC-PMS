package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.exception.BusinessException;
import com.example.dhcpms.mapper.*;
import com.example.dhcpms.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 业主服务实现类
 */
@Service
public class OwnerServiceImpl extends ServiceImpl<OwnerMapper, Owner> implements OwnerService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FeeRecordMapper feeRecordMapper;
    @Autowired
    private FeeReminderMapper feeReminderMapper;
    @Autowired
    private RepairOrderMapper repairOrderMapper;
    @Autowired
    private SuggestionMapper suggestionMapper;
    @Autowired
    private OwnerMessageMapper ownerMessageMapper;

    private static final Logger log = LoggerFactory.getLogger(OwnerServiceImpl.class);

    @Override
    public IPage<Owner> getOwnerList(Page<Owner> page, String searchType, String keyword) {
        // 如果是模糊查询，使用自定义方法
        if ("fuzzy".equals(searchType) && StringUtils.hasText(keyword)) {
            return fuzzySearchOwner(page, keyword);
        }

        LambdaQueryWrapper<Owner> queryWrapper = new LambdaQueryWrapper<>();

        // 多条件筛选
        if (StringUtils.hasText(searchType) && StringUtils.hasText(keyword)) {
            try {
                switch (searchType) {
                    case "userName":
                        // 通过姓名查询用户 ID 列表，再查询业主
                        List<Long> userIdsByName = findUserIdsByName(keyword);
                        queryWrapper.in(Owner::getUserId, userIdsByName);
                        break;
                    case "phone":
                        // 通过电话查询用户 ID 列表，再查询业主
                        List<Long> userIdsByPhone = findUserIdsByPhone(keyword);
                        queryWrapper.in(Owner::getUserId, userIdsByPhone);
                        break;
                    case "buildingNo":
                        queryWrapper.like(Owner::getBuildingNo, keyword);
                        break;
                    case "roomNo":
                        queryWrapper.like(Owner::getRoomNo, keyword);
                        break;
                    case "idCard":
                        queryWrapper.like(Owner::getIdCard, keyword);
                        break;
                    // 房屋面积信息
                    case "houseArea":
                        BigDecimal houseArea = new BigDecimal(keyword);
                        queryWrapper.eq(Owner::getHouseArea, houseArea);
                        break;
                    case "internalArea":
                        BigDecimal internalArea = new BigDecimal(keyword);
                        queryWrapper.eq(Owner::getInternalArea, internalArea);
                        break;
                    // 房屋基本信息
                    case "houseLayout":
                        queryWrapper.like(Owner::getHouseLayout, keyword);
                        break;
                    case "houseUsage":
                        queryWrapper.eq(Owner::getHouseUsage, keyword);
                        break;
                    case "houseStructure":
                        queryWrapper.eq(Owner::getHouseStructure, keyword);
                        break;
                    // 共有情况
                    case "ownershipType":
                        queryWrapper.eq(Owner::getOwnershipType, keyword);
                        break;
                    case "coOwnerName":
                        queryWrapper.like(Owner::getCoOwnerName, keyword);
                        break;
                    // 产权证书信息
                    case "propertyCertNo":
                        queryWrapper.like(Owner::getPropertyCertNo, keyword);
                        break;
                    case "propertyUnitNo":
                        queryWrapper.like(Owner::getPropertyUnitNo, keyword);
                        break;
                    // 日期查询优化：支持模糊查询，输入 2024 可匹配 2024 年的日期
                    case "registrationDate":
                        // 尝试将 keyword 作为年份、月份或完整日期进行模糊匹配
                        queryWrapper.and(wrapper ->
                                wrapper.like(Owner::getRegistrationDate, keyword)
                        );
                        break;
                    case "registrationAuthority":
                        queryWrapper.like(Owner::getRegistrationAuthority, keyword);
                        break;
                    case "landUseYears":
                        Integer landUseYears = Integer.parseInt(keyword);
                        queryWrapper.eq(Owner::getLandUseYears, landUseYears);
                        break;
                    case "landNature":
                        queryWrapper.eq(Owner::getLandNature, keyword);
                        break;
                    // 抵押查封情况
                    case "isMortgaged":
                        Boolean isMortgaged = "是".equals(keyword) || "true".equals(keyword);
                        queryWrapper.eq(Owner::getIsMortgaged, isMortgaged);
                        break;
                    case "mortgageAmount":
                        BigDecimal mortgageAmount = new BigDecimal(keyword);
                        queryWrapper.eq(Owner::getMortgageAmount, mortgageAmount);
                        break;
                    case "mortgageeName":
                        queryWrapper.like(Owner::getMortgageeName, keyword);
                        break;
                    case "isSeized":
                        Boolean isSeized = "是".equals(keyword) || "true".equals(keyword);
                        queryWrapper.eq(Owner::getIsSeized, isSeized);
                        break;
                    // 居住权情况
                    case "hasResidenceRight":
                        Boolean hasResidenceRight = "是".equals(keyword) || "true".equals(keyword);
                        queryWrapper.eq(Owner::getHasResidenceRight, hasResidenceRight);
                        break;
                    // 房屋交付信息
                    case "deliveryDate":
                        // 尝试将 keyword 作为年份、月份或完整日期进行模糊匹配
                        queryWrapper.and(wrapper ->
                                wrapper.like(Owner::getDeliveryDate, keyword)
                        );
                        break;
                    case "registeredUsage":
                        queryWrapper.eq(Owner::getRegisteredUsage, keyword);
                        break;
                    // 配套设施信息
                    case "hasParkingSpace":
                        Boolean hasParkingSpace = "是".equals(keyword) || "true".equals(keyword);
                        queryWrapper.eq(Owner::getHasParkingSpace, hasParkingSpace);
                        break;
                    case "hasStorageRoom":
                        Boolean hasStorageRoom = "是".equals(keyword) || "true".equals(keyword);
                        queryWrapper.eq(Owner::getHasStorageRoom, hasStorageRoom);
                        break;
                    case "parkingSpaceType":
                        queryWrapper.eq(Owner::getParkingSpaceType, keyword);
                        break;
                    case "parkingSpaceNo":
                        queryWrapper.like(Owner::getParkingSpaceNo, keyword);
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                log.warn("数字类型转换失败，keyword={}", keyword, e);
            } catch (Exception e) {
                log.warn("查询条件处理失败，searchType={}, keyword={}", searchType, keyword, e);
            }
        }

        // 排序规则：先按 buildingNo 升序，再按 roomNo 升序
        queryWrapper.orderByAsc(Owner::getBuildingNo)
                .orderByAsc(Owner::getRoomNo);

        // 执行分页查询
        IPage<Owner> ownerPage = baseMapper.selectPage(page, queryWrapper);

        // 兜底：确保 records 不为 null
        if (ownerPage.getRecords() == null) {
            ownerPage.setRecords(new ArrayList<>());
        }

        return ownerPage;
    }

    /**
     * 根据姓名查询用户 ID 列表
     */
    private List<Long> findUserIdsByName(String name) {
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(User::getId)
                .like(User::getName, name);
        return userMapper.selectList(userQueryWrapper)
                .stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据电话查询用户 ID 列表
     */
    private List<Long> findUserIdsByPhone(String phone) {
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(User::getId)
                .like(User::getPhone, phone);
        return userMapper.selectList(userQueryWrapper)
                .stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 模糊查询业主信息（支持楼房号、产权人姓名、联系电话）
     */
    private IPage<Owner> fuzzySearchOwner(Page<Owner> page, String keyword) {
        // 1. 先查询符合条件的用户 ID 列表
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(User::getId)
                .and(wrapper -> wrapper.like(User::getName, keyword)
                        .or()
                        .like(User::getPhone, keyword));
        List<User> users = userMapper.selectList(userQueryWrapper);
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toList());

        // 2. 查询业主表，匹配楼房号或匹配业主 ID
        LambdaQueryWrapper<Owner> ownerQueryWrapper = new LambdaQueryWrapper<>();
        ownerQueryWrapper.and(wrapper ->
                wrapper.like(Owner::getBuildingNo, keyword)
                        .or()
                        .like(Owner::getRoomNo, keyword)
                        .or()
                        .in(Owner::getUserId, userIds.isEmpty() ? java.util.Collections.singletonList(-1L) : userIds)
        );

        // 排序规则：先按 buildingNo 升序，再按 roomNo 升序
        ownerQueryWrapper.orderByAsc(Owner::getBuildingNo)
                .orderByAsc(Owner::getRoomNo);

        // 执行分页查询
        IPage<Owner> ownerPage = baseMapper.selectPage(page, ownerQueryWrapper);

        // 兜底：确保 records 不为 null
        if (ownerPage.getRecords() == null) {
            ownerPage.setRecords(new ArrayList<>());
        }

        return ownerPage;
    }

    @Override
    public void addOwner(Owner owner) {
        // 1. 校验userId是否存在且角色为业主（role=2）
        User user = userMapper.selectById(owner.getUserId());
        if (user == null) {
            throw new BusinessException("关联的用户ID不存在");
        }
        // 修复核心：用基本类型对比（==）替代 equals() 方法
        if (user.getRole() != 2) {
            throw new BusinessException("该用户非业主角色，无法关联");
        }

        // 2. 校验楼栋号+房号是否已被占用
        LambdaQueryWrapper<Owner> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Owner::getBuildingNo, owner.getBuildingNo())
                .eq(Owner::getRoomNo, owner.getRoomNo());
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该楼栋+房号已被其他业主占用");
        }

        // 3. 自动填充字段
        owner.setRegisterTime(LocalDateTime.now());

        // 4. 插入数据库
        baseMapper.insert(owner);

        // 5. 记录操作日志（示例）
        logOperation("新增业主", owner.getId(), "用户ID：" + owner.getUserId() + "，房号：" + owner.getBuildingNo() + "-" + owner.getRoomNo());
    }

    @Override
    public void updateOwner(Owner owner) {
        // 1. 校验业主记录是否存在
        if (baseMapper.selectById(owner.getId()) == null) {
            throw new BusinessException("业主记录不存在");
        }

        // 2. 校验userId是否存在且为业主角色（核心修复：用 != 替代 equals）
        User user = userMapper.selectById(owner.getUserId());
        if (user == null || user.getRole() != 2) { // 修复这一行！
            throw new BusinessException("关联的用户ID无效或非业主角色");
        }

        // 3. 校验楼栋号+房号是否被其他业主占用
        LambdaQueryWrapper<Owner> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Owner::getBuildingNo, owner.getBuildingNo())
                .eq(Owner::getRoomNo, owner.getRoomNo())
                .ne(Owner::getId, owner.getId()); // 排除当前记录
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该楼栋+房号已被其他业主占用");
        }

        // 4. 更新数据库
        baseMapper.updateById(owner);

        // 5. 记录操作日志（示例）
        logOperation("修改业主", owner.getId(), "房号变更为：" + owner.getBuildingNo() + "-" + owner.getRoomNo());
    }

    @Override
    public void deleteOwner(Long id) {
        // 1. 校验业主记录是否存在
        Owner owner = baseMapper.selectById(id);
        if (owner == null) {
            throw new BusinessException("业主记录不存在");
        }

        // 2. 校验是否有未处理关联数据（示例：可扩展物业费/报修关联校验）
        // if (checkRelatedData(id)) {
        //     throw new BusinessException("该业主存在未处理的关联数据，无法删除");
        // }

        // 3. 逻辑删除（物理删除可直接调用baseMapper.deleteById(id)）
        // 注：如需物理删除，需在Owner实体添加@TableLogic注解或修改逻辑
        baseMapper.deleteById(id);

        // 4. 记录操作日志
        logOperation("删除业主", id, "删除时间：" + LocalDateTime.now());
    }

    /**
     * 操作日志记录（可扩展为存入数据库）
     */
    private void logOperation(String operation, Long targetId, String content) {
        System.out.println(String.format("[%s] 操作人：管理员，目标ID：%d，内容：%s", operation, targetId, content));
    }

    /**
     * 校验关联数据（示例方法）
     */
    private boolean checkRelatedData(Long ownerId) {
        // 此处可添加物业费、报修单等关联校验逻辑
        return false;
    }

    /**
     * 根据用户ID获取业主信息
     */
    @Override
    public Owner getByUserId(Long userId) {
        LambdaQueryWrapper<Owner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Owner::getUserId, userId);
        return this.getOne(wrapper);
    }


}