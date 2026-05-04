package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.dto.FeeQueryDTO;
import com.example.dhcpms.entity.FeeRecord;
import com.example.dhcpms.entity.FeeType;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.mapper.FeeRecordMapper;
import com.example.dhcpms.service.FeeRecordService;
import com.example.dhcpms.service.FeeTypeService;
import com.example.dhcpms.service.OwnerService;
import com.example.dhcpms.service.UserService;
import com.example.dhcpms.vo.FeeSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 费用记录服务实现类
 * 适配：FeeQueryDTO中startDueDate/endDueDate为LocalDate类型
 * 修复：1. 类型匹配校验；2. 模糊查询逻辑；3. 逾期状态筛选
 */
@Service
public class FeeRecordServiceImpl extends ServiceImpl<FeeRecordMapper, FeeRecord> implements FeeRecordService {

    @Autowired
    private OwnerService ownerService;
    @Autowired
    private UserService userService;
    @Autowired
    private FeeTypeService feeTypeService;

    @Override
    public IPage<FeeRecord> queryFeeRecordPage(IPage<FeeRecord> page, FeeQueryDTO queryDTO) {
        LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 业主 ID 过滤（业主端：仅查当前业主的费用）
        if (queryDTO.getOwnerId() != null) {
            queryWrapper.eq(FeeRecord::getOwnerId, queryDTO.getOwnerId());
        }

        // 2. 核心：关键词模糊查询（房号/业主姓名）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            // 查询符合关键词的业主 ID 列表（关联 owner+user 表）
            LambdaQueryWrapper<Owner> ownerWrapper = new LambdaQueryWrapper<>();
            ownerWrapper.like(Owner::getBuildingNo, queryDTO.getKeyword())
                    .or()
                    .like(Owner::getRoomNo, queryDTO.getKeyword())
                    .or()
                    .inSql(Owner::getUserId,
                            "SELECT id FROM user WHERE name LIKE '%" + queryDTO.getKeyword() + "%'");

            List<Owner> matchOwners = ownerService.list(ownerWrapper);
            if (!matchOwners.isEmpty()) {
                List<Long> ownerIds = matchOwners.stream()
                        .map(Owner::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(FeeRecord::getOwnerId, ownerIds);
            } else {
                // 无匹配业主，返回空结果
                queryWrapper.eq(FeeRecord::getId, -1);
            }
        }

        // 3. 费用类型筛选（关联 fee_type 表）
        if (StringUtils.hasText(queryDTO.getTypeName())) {
            LambdaQueryWrapper<FeeType> feeTypeWrapper = new LambdaQueryWrapper<>();
            feeTypeWrapper.eq(FeeType::getTypeName, queryDTO.getTypeName());
            FeeType matchType = feeTypeService.getOne(feeTypeWrapper);

            if (matchType != null) {
                queryWrapper.eq(FeeRecord::getFeeTypeId, matchType.getId());
            } else {
                // 无匹配类型，返回空结果
                queryWrapper.eq(FeeRecord::getId, -1);
            }
        }

        // 4. 缴费状态筛选（0-未缴/1-已缴）
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(FeeRecord::getStatus, queryDTO.getStatus());
        }

        // 5. 截止日期范围筛选
        if (queryDTO.getStartDueDate() != null) {
            queryWrapper.ge(FeeRecord::getDueDate, queryDTO.getStartDueDate());
        }
        if (queryDTO.getEndDueDate() != null) {
            queryWrapper.le(FeeRecord::getDueDate, queryDTO.getEndDueDate());
        }

        // 6. 金额区间筛选
        if (queryDTO.getMinAmount() != null) {
            queryWrapper.ge(FeeRecord::getAmount, queryDTO.getMinAmount());
        }
        if (queryDTO.getMaxAmount() != null) {
            queryWrapper.le(FeeRecord::getAmount, queryDTO.getMaxAmount());
        }

        // 7. 逾期状态筛选（0-未逾期/1-已逾期）
        if (queryDTO.getOverdueStatus() != null) {
            LocalDate now = LocalDate.now();
            if (queryDTO.getOverdueStatus() == 1) {
                // 已逾期：截止日期 < 当前日期 且 未缴费
                queryWrapper.lt(FeeRecord::getDueDate, now)
                        .eq(FeeRecord::getStatus, 0);
            } else if (queryDTO.getOverdueStatus() == 0) {
                // 未逾期：截止日期 >= 当前日期 或 已缴费
                queryWrapper.and(wrapper -> wrapper
                        .ge(FeeRecord::getDueDate, now)
                        .or()
                        .eq(FeeRecord::getStatus, 1)
                );
            }
        }

        // 8. 排序：按 ownerId 排序，保证顺序一致
        queryWrapper.orderByAsc(FeeRecord::getOwnerId);

        // 9. 执行分页查询
        return baseMapper.selectPage(page, queryWrapper);
    }


    @Override
    public FeeSummaryVO calculateFeeSummary(Long ownerId) {
        FeeSummaryVO summary = new FeeSummaryVO();
        summary.setUnpaidAmount(BigDecimal.ZERO);
        summary.setOverdueAmount(BigDecimal.ZERO);
        summary.setPaidAmount(BigDecimal.ZERO);

        if (ownerId == null) {
            return summary;
        }

        LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeRecord::getOwnerId, ownerId);

        // 查询所有费用记录
        List<FeeRecord> records = baseMapper.selectList(queryWrapper);

        LocalDate now = LocalDate.now();

        for (FeeRecord record : records) {
            if (record.getStatus() == 1) {
                // 已缴费用
                summary.setPaidAmount(summary.getPaidAmount().add(record.getAmount()));
            } else {
                // 未缴费用
                if (record.getDueDate().isAfter(now) || record.getDueDate().isEqual(now)) {
                    // 待缴费用
                    summary.setUnpaidAmount(summary.getUnpaidAmount().add(record.getAmount()));
                } else {
                    // 逾期费用
                    summary.setOverdueAmount(summary.getOverdueAmount().add(record.getAmount()));
                }
            }
        }

        return summary;
    }
}