package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.FeeRecord;
import com.example.dhcpms.entity.FeeReminder;
import com.example.dhcpms.mapper.FeeReminderMapper;
import com.example.dhcpms.service.FeeRecordService;
import com.example.dhcpms.service.FeeReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 催缴记录服务实现类
 */
@Slf4j
@Service
public class FeeReminderServiceImpl extends ServiceImpl<FeeReminderMapper, FeeReminder> implements FeeReminderService {

    @Autowired
    private FeeRecordService feeRecordService;

    /**
     * 业主欠费风险预测算法
     * @param ownerId 业主ID
     * @return 风险评估结果
     */
    @Override
    public Map<String, Object> predictArrearsRisk(Long ownerId) {
        Map<String, Object> riskAssessment = new HashMap<>();

        // 1. 查询业主所有费用记录
        List<FeeRecord> allRecords = feeRecordService.list(
                new LambdaQueryWrapper<FeeRecord>()
                        .eq(FeeRecord::getOwnerId, ownerId)
                        .orderByDesc(FeeRecord::getCreateTime)
        );

        if (allRecords.isEmpty()) {
            riskAssessment.put("riskLevel", "LOW");
            riskAssessment.put("riskScore", 50);
            riskAssessment.put("message", "无历史缴费记录，无法评估风险");
            return riskAssessment;
        }

        // 2. 计算多维度风险指标（直接返回风险分数，分数越高风险越高）
        double paymentTimelinessRisk = calculatePaymentTimeliness(allRecords);
        double arrearsFrequencyRisk = calculateArrearsFrequency(allRecords);
        double overdueDaysRisk = calculateOverdueDays(allRecords);
        double amountTrendRisk = calculateAmountTrend(allRecords);

        // 3. 综合风险评分（加权平均，分数越高整体风险越高）
        double totalRiskScore = paymentTimelinessRisk * 0.35
                + arrearsFrequencyRisk * 0.30
                + overdueDaysRisk * 0.20
                + amountTrendRisk * 0.15;

        // 4. 确定风险等级
        String riskLevel;
        String suggestion;
        if (totalRiskScore >= 80) {
            riskLevel = "HIGH";
            suggestion = "高风险业主，建议提前催缴并关注缴费动态";
        } else if (totalRiskScore >= 60) {
            riskLevel = "MEDIUM";
            suggestion = "中等风险，建议定期提醒缴费";
        } else if (totalRiskScore >= 40) {
            riskLevel = "LOW";
            suggestion = "低风险，正常管理即可";
        } else {
            riskLevel = "VERY_LOW";
            suggestion = "优质业主，缴费及时";
        }

        // 5. 统计当前欠费情况
        long currentUnpaidCount = allRecords.stream()
                .filter(r -> r.getStatus() == 0 && r.getDueDate().isBefore(LocalDate.now()))
                .count();

        BigDecimal currentUnpaidAmount = allRecords.stream()
                .filter(r -> r.getStatus() == 0 && r.getDueDate().isBefore(LocalDate.now()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. 组装返回结果
        riskAssessment.put("riskLevel", riskLevel);
        riskAssessment.put("riskScore", Math.round(totalRiskScore * 100.0) / 100.0);
        riskAssessment.put("suggestion", suggestion);

        // 详细指标（直接返回风险分数）
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("paymentTimeliness", Math.round(paymentTimelinessRisk * 100.0) / 100.0);
        indicators.put("arrearsFrequency", Math.round(arrearsFrequencyRisk * 100.0) / 100.0);
        indicators.put("overdueDays", Math.round(overdueDaysRisk * 100.0) / 100.0);
        indicators.put("amountTrend", Math.round(amountTrendRisk * 100.0) / 100.0);
        riskAssessment.put("indicators", indicators);

        // 当前欠费统计
        riskAssessment.put("currentUnpaidCount", currentUnpaidCount);
        riskAssessment.put("currentUnpaidAmount", currentUnpaidAmount);

        // 历史记录统计
        riskAssessment.put("totalRecords", allRecords.size());
        riskAssessment.put("paidCount", allRecords.stream().filter(r -> r.getStatus() == 1).count());
        riskAssessment.put("unpaidCount", allRecords.stream().filter(r -> r.getStatus() == 0).count());

        log.info("业主 {} 欠费风险评估完成，风险等级: {}, 风险分数: {}",
                ownerId, riskLevel, totalRiskScore);

        return riskAssessment;
    }

    /**
     * 计算缴费及时性得分（0-100分）
     * 基于已缴费次数与总记录次数的比例
     * 返回的是风险分数：已缴比例越低，风险越高
     *
     * 计算方式：
     * - 已缴费次数 / 总记录数 = 已缴比例
     * - 风险分数 = (1 - 已缴比例) * 100
     *
     * 示例：
     * - 已缴10次，总10次 → 已缴比例100% → 风险分0
     * - 已缴5次，总10次 → 已缴比例50% → 风险分50
     * - 已缴0次，总10次 → 已缴比例0% → 风险分100
     */
    private double calculatePaymentTimeliness(List<FeeRecord> records) {
        if (records.isEmpty()) {
            return 50.0; // 无记录，给中等风险分
        }

        // 统计已缴费次数
        long paidCount = records.stream()
                .filter(r -> r.getStatus() == 1)
                .count();

        // 计算已缴比例
        double paidRate = (double) paidCount / records.size();

        // 返回风险分数：已缴比例越低，风险越高
        // 已缴比例100% → 风险分0
        // 已缴比例0% → 风险分100
        return (1 - paidRate) * 100;
    }

    /**
     * 计算欠费频率得分（0-100分）
     * 欠费次数越少，得分越高
     */
    private double calculateArrearsFrequency(List<FeeRecord> records) {
        if (records.isEmpty()) {
            return 50.0;
        }

        long unpaidCount = records.stream()
                .filter(r -> r.getStatus() == 0)
                .count();

        double unpaidRate = (double) unpaidCount / records.size();

        // 欠费率越高，风险越高（直接返回欠费率*100）
        return unpaidRate * 100;
    }

    /**
     * 计算逾期天数得分（0-100分）
     * 平均逾期天数越短，得分越高
     */
    private double calculateOverdueDays(List<FeeRecord> records) {
        List<FeeRecord> overdueRecords = records.stream()
                .filter(r -> r.getStatus() == 0 && r.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        if (overdueRecords.isEmpty()) {
            return 0.0; // 无逾期记录，无风险
        }

        LocalDate now = LocalDate.now();
        double avgOverdueDays = overdueRecords.stream()
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getDueDate(), now))
                .average()
                .orElse(0);

        // 逾期天数映射到风险分数（线性递增）
        // 0天=0分，15天=50分，30天=100分
        if (avgOverdueDays <= 0) {
            return 0.0;
        } else if (avgOverdueDays >= 30) {
            return 100.0;
        } else {
            // 线性映射：riskScore = (avgOverdueDays / 30) * 100
            return (avgOverdueDays / 30.0) * 100.0;
        }
    }

    /**
     * 计算金额风险得分（0-100分）
     * 基于当前欠费金额计算
     * 欠费金额越大，风险越高
     *
     * 映射规则：
     * - 0元 → 风险分0
     * - 250元 → 风险分50
     * - 500元 → 风险分100
     * - >500元 → 风险分100(封顶)
     */
    private double calculateAmountTrend(List<FeeRecord> records) {
        // 计算当前欠费金额（状态为0且已过截止日期的记录）
        BigDecimal currentArrearsAmount = records.stream()
                .filter(r -> r.getStatus() == 0 && r.getDueDate().isBefore(LocalDate.now()))
                .map(FeeRecord::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double arrearsAmount = currentArrearsAmount.doubleValue();

        // 如果没有欠费，风险分为0
        if (arrearsAmount <= 0) {
            return 0.0;
        }

        // 线性映射：欠费金额/500 * 100
        // 500元 → 100分
        if (arrearsAmount >= 500) {
            return 100.0;
        } else {
            return (arrearsAmount / 500.0) * 100.0;
        }
    }
}
