package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.RepairStatusEnum;
import com.example.dhcpms.dto.*;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.RepairOrder;
import com.example.dhcpms.entity.RepairType;
import com.example.dhcpms.mapper.OwnerMapper;
import com.example.dhcpms.mapper.RepairOrderMapper;
import com.example.dhcpms.mapper.RepairTypeMapper;
import com.example.dhcpms.mapper.UserMapper;
import com.example.dhcpms.service.RepairService;
import com.example.dhcpms.vo.RepairOrderDetailVO;
import com.example.dhcpms.vo.RepairOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报修服务实现类
 * 适配新的Result<T>返回类，补充权限校验和参数校验逻辑
 */
@Service
@Slf4j
public class RepairServiceImpl implements RepairService {

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RepairTypeMapper repairTypeMapper;

    // @Autowired
    // private RepairEvaluationMapper repairEvaluationMapper;

    @Autowired
    private UserMapper userMapper;

    // ====================== 业主端核心方法 ======================

    /**
     * 获取业主报修自动填充的地址
     */
    @Override
    public String getRepairAddress(Long userId) {
        Owner owner = ownerMapper.selectByUserId(userId);
        if (owner == null) {
            throw new RuntimeException("未查询到业主信息");
        }
        return owner.getBuildingNo() + "-" + owner.getRoomNo();
    }

    /**
     * 提交报修申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRepairApply(Long userId, RepairApplyDTO repairDTO) {
        // 1. 参数校验
        if (repairDTO.getRepairTypeId() == null) {
            throw new IllegalArgumentException("报修类型不能为空");
        }
        if (repairDTO.getTitle() == null || repairDTO.getTitle().isEmpty()) {
            throw new IllegalArgumentException("报修标题不能为空");
        }
        if (repairDTO.getContent() == null || repairDTO.getContent().isEmpty()) {
            throw new IllegalArgumentException("报修内容不能为空");
        }

        // 2. 根据 userId 查询业主信息
        Owner owner = ownerMapper.selectByUserId(userId);
        if (owner == null) {
            throw new RuntimeException("未查询到业主信息，请先完成业主认证");
        }

        // 3. 校验报修类型是否存在
        RepairType repairType = repairTypeMapper.selectById(repairDTO.getRepairTypeId());
        if (repairType == null) {
            throw new RuntimeException("报修类型不存在");
        }

        // 4. 生成报修单号：BX + 年月日时分秒 + 4 位随机数
        String repairOrderNo = generateRepairOrderNo();

        // 5. 组装报修订单数据
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setRepairOrderNo(repairOrderNo);
        repairOrder.setOwnerId(owner.getId());
        repairOrder.setRepairTypeId(repairDTO.getRepairTypeId());
        repairOrder.setTitle(repairDTO.getTitle());
        repairOrder.setContent(repairDTO.getContent());
        // 自动填充地址：楼栋号 + 房间号
        repairOrder.setAddress(owner.getBuildingNo() + "-" + owner.getRoomNo());
        repairOrder.setStatus(RepairStatusEnum.PENDING_REVIEW.getCode()); // 待审核
        repairOrder.setCreateTime(LocalDateTime.now());

        // 6. 插入数据库
        repairOrderMapper.insert(repairOrder);

        // 7. 返回报修订单 ID（主键自增）
        return repairOrder.getId();
    }

    /**
     * 生成报修单号：BX + 年月日时分秒 + 4 位随机数
     * 例如：BX202503231815163842
     */
    private String generateRepairOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "BX" + timeStr + randomStr;
    }

    /**
     * 查询我的报修列表
     */
    @Override
    public List<RepairOrderVO> getMyRepairList(Long userId) {
        // 1. 根据userId查询业主ID
        Owner owner = ownerMapper.selectByUserId(userId);
        if (owner == null) {
            throw new RuntimeException("未查询到业主信息");
        }

        // 2. 查询业主的报修列表
        List<RepairOrderVO> list = repairOrderMapper.selectOwnerRepairList(owner.getId());

        // 3. 填充状态描述
        list.forEach(vo -> {
            vo.setStatusDesc(RepairStatusEnum.getDescByCode(vo.getStatus()));
        });

        return list;
    }

    /**
     * 查询报修单详情（增加权限校验，返回null而非异常）
     */
    @Override
    public RepairOrderDetailVO getRepairDetail(Long repairOrderId, Long userId) {
        // 1. 校验报修单是否存在
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        if (repairOrder == null) {
            return null; // 返回null，由Controller层处理
        }

        // 2. 权限校验：确保是当前业主的报修单
        Owner owner = ownerMapper.selectByUserId(userId);
        if (!repairOrder.getOwnerId().equals(owner.getId())) {
            throw new SecurityException("无权限查看该报修单");
        }

        // 3. 查询详情
        RepairOrderDetailVO detail = repairOrderMapper.selectRepairDetail(repairOrderId);
        detail.setStatusDesc(RepairStatusEnum.getDescByCode(detail.getStatus()));

        // 4. 如果是已完成状态，查询评价信息
        //if (RepairStatusEnum.COMPLETED.getCode().equals(detail.getStatus())) {
        //    RepairEvaluation evaluation = repairEvaluationMapper.selectByRepairOrderId(repairOrderId);
        //    if (evaluation != null) {
        //        detail.setScore(evaluation.getScore());
        //        detail.setComment(evaluation.getComment());
        //        detail.setEvaluateTime(evaluation.getCreateTime());
        //    }
        //}

        // 新增：直接从 RepairOrder 读取评价字段并映射到 VO
        if (repairOrder.getEvaluationScore() != null) {
            detail.setEvaluationScore(repairOrder.getEvaluationScore());
            detail.setEvaluationComment(repairOrder.getEvaluationComment());
            detail.setEvaluationTime(repairOrder.getEvaluationTime());
        }

        return detail;
    }

    /**
     * 业主验收报修单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleRepairAcceptance(Long userId, RepairAcceptanceDTO acceptanceDTO) {
        // 1. 校验报修单是否存在
        RepairOrder repairOrder = repairOrderMapper.selectById(acceptanceDTO.getRepairOrderId());
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }

        // 2. 权限校验
        Owner owner = ownerMapper.selectByUserId(userId);
        if (!repairOrder.getOwnerId().equals(owner.getId())) {
            throw new SecurityException("无权限操作该报修单");
        }

        // 3. 校验报修单状态（仅待验收状态可验收）
        if (!RepairStatusEnum.PENDING_ACCEPTANCE.getCode().equals(repairOrder.getStatus())) {
            throw new RuntimeException("仅待验收状态的报修单可进行验收操作");
        }

        // 4. 处理验收逻辑
        if (acceptanceDTO.getAcceptResult()) {
            // 验收通过：更新状态为已完成，写入评价
            repairOrder.setStatus(RepairStatusEnum.COMPLETED.getCode());
            repairOrder.setCompleteTime(LocalDateTime.now());

            // 写入评价表
            // RepairEvaluation evaluation = new RepairEvaluation();
            // evaluation.setRepairOrderId(acceptanceDTO.getRepairOrderId());
            // evaluation.setScore(acceptanceDTO.getScore());
            // evaluation.setComment(acceptanceDTO.getComment());
            // evaluation.setCreateTime(LocalDateTime.now());
            // repairEvaluationMapper.insert(evaluation);

            // 写入评价数据
            repairOrder.setEvaluationScore(acceptanceDTO.getScore());
            repairOrder.setEvaluationComment(acceptanceDTO.getComment());
            repairOrder.setEvaluationTime(LocalDateTime.now());

            repairOrderMapper.updateById(repairOrder);


            return "验收通过，感谢您的评价，报修单已完成";
        } else {
            // 验收不通过：回退状态为维修中
            UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", acceptanceDTO.getRepairOrderId())
                    .set("status", RepairStatusEnum.UNDER_REPAIR.getCode())
                    .set("content", repairOrder.getContent() + "[业主验收不通过原因：" + acceptanceDTO.getComment() + "]");
            repairOrderMapper.update(null, updateWrapper);

            return "验收不通过，物业将安排二次维修，请耐心等待";
        }
    }

    /**
     * 重新提交报修申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitRepairApply(Long userId, RepairResubmitDTO resubmitDTO) {
        // 1. 校验报修单是否存在且状态为已取消
        RepairOrder repairOrder = repairOrderMapper.selectById(resubmitDTO.getRepairOrderId());
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }
        if (!RepairStatusEnum.CANCELED.getCode().equals(repairOrder.getStatus())) {
            throw new RuntimeException("仅驳回的报修单可重新提交");
        }

        // 2. 权限校验
        Owner owner = ownerMapper.selectByUserId(userId);
        if (!repairOrder.getOwnerId().equals(owner.getId())) {
            throw new SecurityException("无权限操作该报修单");
        }

        // 3. 更新报修单信息
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", resubmitDTO.getRepairOrderId())
                .set("title", resubmitDTO.getTitle())
                .set("content", resubmitDTO.getContent())
                .set("status", RepairStatusEnum.PENDING_REVIEW.getCode()) // 重置为待审核
                .set("create_time", LocalDateTime.now()); // 更新创建时间
        repairOrderMapper.update(null, updateWrapper);
    }

    // ====================== 物业端核心方法 ======================

    /**
     * 物业审核报修单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRepairReview(RepairReviewDTO reviewDTO) {
        // 1. 校验报修单是否存在且状态为待审核
        RepairOrder repairOrder = repairOrderMapper.selectById(reviewDTO.getRepairOrderId());
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }
        if (!RepairStatusEnum.PENDING_REVIEW.getCode().equals(repairOrder.getStatus())) {
            throw new RuntimeException("仅待审核的报修单可审核");
        }

        // 2. 处理审核逻辑
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", reviewDTO.getRepairOrderId());

        if (reviewDTO.getReviewResult()) {
            // 审核通过：更新为已派单
            updateWrapper.set("status", RepairStatusEnum.ASSIGNED.getCode());
        } else {
            // 审核驳回：更新为已取消，记录驳回原因
            updateWrapper.set("status", RepairStatusEnum.CANCELED.getCode())
                    .set("content", repairOrder.getContent() + "[驳回原因：" + reviewDTO.getRejectReason() + "]");
        }
        repairOrderMapper.update(null, updateWrapper);
    }

    /**
     * 物业派单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRepairOrder(RepairAssignDTO assignDTO) {
        // 1. 校验报修单是否存在且状态为已审核
        RepairOrder repairOrder = repairOrderMapper.selectById(assignDTO.getRepairOrderId());
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }
        if (!RepairStatusEnum.ASSIGNED.getCode().equals(repairOrder.getStatus())) {
            throw new RuntimeException("仅已审核的报修单可分配维修人员");
        }

        // 2. 更新派单信息
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", assignDTO.getRepairOrderId())
                .set("handler_name", assignDTO.getHandlerName())
                .set("handler_phone", assignDTO.getHandlerPhone())
                .set("expected_visit_time", assignDTO.getExpectedVisitTime())
                .set("assign_remark", assignDTO.getRemark())
                .set("status", RepairStatusEnum.UNDER_REPAIR.getCode()); // 状态改为维修中

        repairOrderMapper.update(null, updateWrapper);
    }

    /**
     * 更新维修进度（标记为待验收）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRepairProgress(Long repairOrderId) {
        // 1. 校验报修单状态
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }
        if (!RepairStatusEnum.UNDER_REPAIR.getCode().equals(repairOrder.getStatus())) {
            throw new RuntimeException("仅维修中的报修单可标记为待验收");
        }

        // 2. 更新状态为待验收
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", repairOrderId)
                .set("status", RepairStatusEnum.PENDING_ACCEPTANCE.getCode());
        repairOrderMapper.update(null, updateWrapper);
    }

    /**
     * 物业端获取所有报修列表
     */
    @Override
    public Map<String, Object> getAdminRepairListWithPage(Integer status, Long repairTypeId, Integer page, Integer size) {
        // 使用 MyBatis-Plus 的分页插件
        Page<RepairOrder> pageParam = new Page<>(page, size);

        // 使用 MyBatis-Plus 的 QueryWrapper 构建查询条件
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 添加状态筛选
        if (status != null) {
            queryWrapper.eq(RepairOrder::getStatus, status);
        }

        // 添加报修类型筛选
        if (repairTypeId != null) {
            queryWrapper.eq(RepairOrder::getRepairTypeId, repairTypeId);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(RepairOrder::getCreateTime);

        // 分页查询
        Page<RepairOrder> repairOrderPage = repairOrderMapper.selectPage(pageParam, queryWrapper);

        // 转换为 VO（补充报修类型名称和状态描述）
        java.util.List<RepairOrderVO> voList = repairOrderPage.getRecords().stream().map(order -> {
            RepairOrderVO vo = new RepairOrderVO();
            vo.setId(order.getId());
            vo.setRepairOrderNo(order.getRepairOrderNo());
            vo.setTitle(order.getTitle());
            vo.setStatus(order.getStatus());
            vo.setCreateTime(order.getCreateTime());
            vo.setAddress(order.getAddress());

            // 查询报修类型名称
            RepairType repairType = repairTypeMapper.selectById(order.getRepairTypeId());
            vo.setRepairTypeName(repairType != null ? repairType.getTypeName() : "未知类型");

            // 填充状态描述
            vo.setStatusDesc(RepairStatusEnum.getDescByCode(order.getStatus()));

            return vo;
        }).collect(java.util.stream.Collectors.toList());

        // 返回分页结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", voList);
        resultMap.put("total", repairOrderPage.getTotal());
        resultMap.put("size", repairOrderPage.getSize());
        resultMap.put("current", repairOrderPage.getCurrent());
        resultMap.put("pages", repairOrderPage.getPages());

        return resultMap;
    }

    /**
     * 业主取消报修申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRepairApply(Long userId, RepairCancelDTO cancelDTO) {
        // 1. 校验报修单是否存在
        RepairOrder repairOrder = repairOrderMapper.selectById(cancelDTO.getRepairOrderId());
        if (repairOrder == null) {
            throw new IllegalArgumentException("报修单不存在");
        }

        // 2. 校验是否是本人的报修单
        if (!userId.equals(repairOrder.getOwnerId())) {
            throw new SecurityException("无权取消他人的报修单");
        }

        // 3. 校验状态：仅待审核状态可以取消
        if (!RepairStatusEnum.PENDING_REVIEW.getCode().equals(repairOrder.getStatus())) {
            throw new IllegalArgumentException("仅待审核状态的报修单可以取消");
        }

        // 4. 更新状态为已取消（状态 4），并将取消原因写入备注
        UpdateWrapper<RepairOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", cancelDTO.getRepairOrderId())
                .set("status", RepairStatusEnum.CANCELED.getCode())
                .set("content", repairOrder.getContent() + "[业主取消原因：" + (cancelDTO.getCancelReason() != null ? cancelDTO.getCancelReason() : "未填写") + "]");

        int rows = repairOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("取消报修申请失败");
        }
    }

    /**
     * 报修进度实时预测算法
     */
    @Override
    public Map<String, Object> predictRepairProgress(Long repairOrderId) {
        Map<String, Object> prediction = new HashMap<>();

        // 1. 查询报修单
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        if (repairOrder == null) {
            throw new RuntimeException("报修单不存在");
        }

        Integer status = repairOrder.getStatus();
        LocalDateTime createTime = repairOrder.getCreateTime();
        LocalDateTime now = LocalDateTime.now();

        // 2. 阶段定义
        Map<Integer, String> stageNames = new HashMap<>();
        stageNames.put(0, "待审核");
        stageNames.put(1, "已审核");
        stageNames.put(2, "维修中");
        stageNames.put(3, "已完成");
        stageNames.put(4, "已取消");
        stageNames.put(5, "待验收");
        stageNames.put(6, "已验收");

        String currentStage = stageNames.getOrDefault(status, "未知");
        prediction.put("currentStage", currentStage);

        // 3. 计算进度百分比
        double progress = calculateProgressPercentage(status);
        prediction.put("progressPercent", progress);

        // 4. 预测剩余时间
        long elapsedMinutes = java.time.Duration.between(createTime, now).toMinutes();
        long estimatedTotalMinutes = estimateTotalHandleTime(repairOrder);
        long remainingMinutes = Math.max(0, estimatedTotalMinutes - elapsedMinutes);

        prediction.put("elapsedMinutes", elapsedMinutes);
        prediction.put("estimatedTotalMinutes", estimatedTotalMinutes);
        prediction.put("remainingMinutes", remainingMinutes);
        prediction.put("estimatedCompleteTime", now.plusMinutes(remainingMinutes));

        // 5. 下一步骤预测
        String nextStep = predictNextStep(status);
        prediction.put("nextStep", nextStep);

        // 6. 超时风险评估
        boolean isOverdueRisk = assessOverdueRisk(repairOrder, remainingMinutes);
        prediction.put("isOverdueRisk", isOverdueRisk);

        if (isOverdueRisk) {
            prediction.put("riskLevel", "HIGH");
            prediction.put("suggestion", "建议联系维修人员催促");
        } else {
            prediction.put("riskLevel", "LOW");
            prediction.put("suggestion", "维修正常进行中");
        }

        log.info("报修单 {} 进度预测完成，当前阶段: {}, 进度: {}%",
                repairOrderId, currentStage, progress);
        return prediction;
    }

    /**
     * 计算进度百分比
     */
    private double calculateProgressPercentage(Integer status) {
        Map<Integer, Double> progressMap = new HashMap<>();
        progressMap.put(0, 10.0);
        progressMap.put(1, 30.0);
        progressMap.put(2, 60.0);
        progressMap.put(3, 90.0);
        progressMap.put(4, 100.0);
        progressMap.put(5, 95.0);
        progressMap.put(6, 100.0);

        return progressMap.getOrDefault(status, 0.0);
    }

    /**
     * 预估总处理时长(分钟)
     */
    private long estimateTotalHandleTime(RepairOrder repairOrder) {
        // 基于历史同类工单的平均时长
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RepairOrder::getRepairTypeId, repairOrder.getRepairTypeId())
                .eq(RepairOrder::getStatus, 6)
                .isNotNull(RepairOrder::getCompleteTime)
                .last("LIMIT 30");

        List<RepairOrder> similarOrders = repairOrderMapper.selectList(queryWrapper);

        if (similarOrders.isEmpty()) {
            return 1440; // 默认24小时
        }

        double avgMinutes = similarOrders.stream()
                .mapToLong(o -> java.time.Duration.between(o.getCreateTime(), o.getCompleteTime()).toMinutes())
                .average()
                .orElse(1440);

        // 考虑当前负载调整
        if (repairOrder.getHandlerId() != null) {
            int pendingTasks = countPendingTasks(repairOrder.getHandlerId());
            avgMinutes *= (1 + pendingTasks * 0.15); // 每个待办增加15%
        }

        return (long) avgMinutes;
    }

    /**
     * 预测下一步骤
     */
    private String predictNextStep(Integer status) {
        Map<Integer, String> nextSteps = new HashMap<>();
        nextSteps.put(0, "等待物业审核");
        nextSteps.put(1, "等待维修人员接单");
        nextSteps.put(2, "维修进行中,请耐心等待");
        nextSteps.put(3, "等待业主验收");
        nextSteps.put(5, "等待业主确认验收");
        nextSteps.put(6, "工单已结束");

        return nextSteps.getOrDefault(status, "未知状态");
    }

    /**
     * 评估超时风险
     */
    private boolean assessOverdueRisk(RepairOrder repairOrder, long remainingMinutes) {
        // 根据报修类型设定标准时长(分钟)
        Map<Long, Long> standardMinutes = new HashMap<>();
        standardMinutes.put(1L, 240L);   // 水电维修: 4小时
        standardMinutes.put(2L, 1440L);  // 家电维修: 24小时
        standardMinutes.put(3L, 2880L);  // 公共区域: 48小时

        Long standardTime = standardMinutes.get(repairOrder.getRepairTypeId());
        if (standardTime == null) {
            standardTime = 1440L; // 默认24小时
        }

        long elapsedMinutes = java.time.Duration.between(
                repairOrder.getCreateTime(),
                LocalDateTime.now()
        ).toMinutes();

        // 超过标准时长120%视为有风险
        return (elapsedMinutes + remainingMinutes) > standardTime * 1.2;
    }

    /**
     * 统计维修人员待办任务数
     */
    private int countPendingTasks(Long handlerId) {
        LambdaQueryWrapper<RepairOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RepairOrder::getHandlerId, handlerId)
                .in(RepairOrder::getStatus, java.util.Arrays.asList(1, 2, 5));

        Long count = repairOrderMapper.selectCount(queryWrapper);
        return count != null ? count.intValue() : 0;
    }
}