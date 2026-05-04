package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.dto.FeeQueryDTO;
import com.example.dhcpms.dto.PaymentOrderDTO;
import com.example.dhcpms.entity.*;
import com.example.dhcpms.service.*;
import com.example.dhcpms.util.ExcelExportUtil;
import com.example.dhcpms.vo.FeeRecordVO;
import com.example.dhcpms.vo.FeeSummaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 费用控制器
 * 适配完整数据库表结构（含user表角色权限）
 * 根路径改为 /fee，区分物业管理人员/业主权限，确保数据安全
 * 修正：创建催缴记录时直接设置status=1（已发送）
 */
@Slf4j
@RestController
@RequestMapping("/fee") // 根路径改为 /fee（移除/api前缀）
public class FeeController {

    @Autowired
    private FeeRecordService feeRecordService;
    @Autowired
    private FeeTypeService feeTypeService;
    @Autowired
    private FeeReminderService feeReminderService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private UserService userService;

    // ====================== 通用工具方法 ======================

    /**
     * 校验用户状态和角色
     * @param userId 用户 ID
     * @param requiredRole 要求的角色（1-物业人员/2-业主/3-物业经理）
     * @return 校验通过返回 User 对象，否则返回 null
     */
    private User checkUserRole(Long userId, Integer requiredRole) {
        // 1. 查询用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }
        // 2. 校验用户状态（是否禁用）
        if (0 == user.getStatus()) {
            return null;
        }
        // 3. 校验角色
        if (!requiredRole.equals(user.getRole())) {
            return null;
        }
        return user;
    }

    // ====================== 业主端接口（角色=2） ======================

    /**
     * 业主费用查询（带筛选）
     * 完整路径：/fee/owner/query
     * @param userId 登录用户ID（业主）
     * @param queryDTO 筛选条件
     * @return 分页费用列表
     */
    @GetMapping("/owner/query")
    public Result<IPage<FeeRecordVO>> queryOwnerFee(
            @RequestParam Long userId,
            FeeQueryDTO queryDTO) {

        // 添加调试日志
        System.out.println("\n========== 开始查询业主费用 ==========");
        System.out.println("【1】传入 userId: " + userId);
        System.out.println("【2】查询参数：" + queryDTO);

        // 1. 校验业主权限（角色=2，状态=1）
        User user = checkUserRole(userId, 2);
        if (user == null) {
            System.out.println("【错误】用户权限校验失败");
            return Result.forbidden("无业主权限或用户已禁用");
        }
        System.out.println("【3】用户权限校验通过：" + user.getName() + " (role=" + user.getRole() + ")");

        // 2. 通过 userId 获取业主信息（确保数据归属）- 核心修复：添加 LIMIT 1 避免多条记录
        Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getUserId, userId)
                .last("LIMIT 1"));

        System.out.println("\n【4】查询 owner 信息:");
        System.out.println("   - userId: " + userId);
        System.out.println("   - 查询到的 owner: " + owner);
        if (owner != null) {
            System.out.println("   - owner.id: " + owner.getId());
            System.out.println("   - owner.buildingNo: " + owner.getBuildingNo());
            System.out.println("   - owner.roomNo: " + owner.getRoomNo());
        }

        if (owner == null) {
            System.out.println("【错误】业主信息不存在，userId: " + userId);
            return Result.error("业主信息不存在");
        }

        // 3. 设置业主 ID 过滤条件，确保只查当前业主的费用
        queryDTO.setOwnerId(owner.getId());
        System.out.println("\n【5】设置查询条件:");
        System.out.println("   - ownerId: " + owner.getId());
        System.out.println("   - 最终查询参数：" + queryDTO);

        // 4. 分页查询费用记录
        IPage<FeeRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<FeeRecord> feePage = feeRecordService.queryFeeRecordPage(page, queryDTO);

        System.out.println("\n【6】查询结果:");
        System.out.println("   - 总记录数：" + feePage.getTotal());
        System.out.println("   - 当前页条数：" + feePage.getRecords().size());
        if (feePage.getTotal() > 0) {
            System.out.println("   - 第一条记录的 ownerId: " + feePage.getRecords().get(0).getOwnerId());
        } else {
            // 新增：检查数据库中是否有该业主的费用记录
            Long countByOwnerId = feeRecordService.count(new LambdaQueryWrapper<FeeRecord>()
                    .eq(FeeRecord::getOwnerId, owner.getId()));
            System.out.println("   - 数据库中该业主 (ownerId=" + owner.getId() + ") 的费用记录总数：" + countByOwnerId);

            // 检查所有费用记录
            Long totalCount = feeRecordService.count();
            System.out.println("   - 数据库中所有费用记录总数：" + totalCount);
        }
        System.out.println("========== 查询结束 ==========\n");

        // 5. 转换为VO，适配前端展示
        IPage<FeeRecordVO> voPage = feePage.convert(feeRecord -> {
            FeeRecordVO vo = new FeeRecordVO();
            BeanUtils.copyProperties(feeRecord, vo);

            // 拼接房号
            vo.setRoomNo(owner.getBuildingNo() + "-" + owner.getRoomNo());

            // 关联费用类型名称
            FeeType feeType = feeTypeService.getById(feeRecord.getFeeTypeId());
            vo.setFeeTypeName(feeType != null ? feeType.getTypeName() : "未知类型");

            // 计算费用状态描述
            LocalDate now = LocalDate.now();
            if (1 == feeRecord.getStatus()) {
                vo.setStatusDesc("已缴费");
                // 关联操作员姓名（物业人员）
                if (feeRecord.getOperatorId() != null) {
                    User operator = userService.getById(feeRecord.getOperatorId());
                    vo.setOperatorName(operator != null ? operator.getName() : "系统");
                }
            } else {
                if (feeRecord.getDueDate().isAfter(now) || feeRecord.getDueDate().isEqual(now)) {
                    vo.setStatusDesc("待缴费");
                } else {
                    vo.setStatusDesc("逾期欠费");
                    // 计算逾期天数
                    long days = ChronoUnit.DAYS.between(feeRecord.getDueDate(), now);
                    vo.setOverdueDays((int) days);
                }

                // 关联最新催缴记录
                List<FeeReminder> reminders = feeReminderService.list(
                        new LambdaQueryWrapper<FeeReminder>()
                                .eq(FeeReminder::getFeeRecordId, feeRecord.getId())
                                .orderByDesc(FeeReminder::getReminderTime)
                                .last("LIMIT 1"));
                if (!reminders.isEmpty()) {
                    FeeReminder reminder = reminders.get(0);
                    String reminderType = reminder.getReminderType() == 1 ? "模板消息" : "短信";
                    String sendStatus = reminder.getStatus() == 1 ? "已发送" : "未发送";
                    vo.setLatestReminder(reminderType + "(" + sendStatus + ")");
                } else {
                    vo.setLatestReminder("无催缴");
                }
            }
            return vo;
        });

        return Result.success(voPage);
    }

    /**
     * 获取业主费用汇总（待缴/逾期/已缴金额）
     * 完整路径：/fee/owner/summary
     * @param userId 登录用户ID（业主）
     * @return 汇总数据
     */
    @GetMapping("/owner/summary")
    public Result<FeeSummaryVO> getOwnerFeeSummary(@RequestParam Long userId) {
        // 1. 校验业主权限
        User user = checkUserRole(userId, 2);
        if (user == null) {
            return Result.forbidden("无业主权限或用户已禁用");
        }

        // 2. 获取业主信息
        Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId));
        if (owner == null) {
            return Result.error("业主信息不存在");
        }

        // 3. 计算汇总金额
        FeeSummaryVO summary = feeRecordService.calculateFeeSummary(owner.getId());
        return Result.success(summary);
    }

    /**
     * 生成缴费单（单条/批量）
     * 完整路径：/fee/owner/createPaymentOrder
     * @param userId 登录用户ID（业主）
     * @param feeRecordIds 费用记录ID列表
     * @return 缴费单信息
     */
    @PostMapping("/owner/createPaymentOrder")
    public Result<PaymentOrderDTO> createPaymentOrder(
            @RequestParam Long userId,
            @RequestBody List<Long> feeRecordIds) {

        System.out.println("\n========== 开始生成缴费单 ==========");
        System.out.println("【1】传入参数:");
        System.out.println("   - userId: " + userId);
        System.out.println("   - feeRecordIds: " + feeRecordIds);

        // 1. 校验业主权限
        User user = checkUserRole(userId, 2);
        if (user == null) {
            System.out.println("【错误】用户权限校验失败");
            return Result.forbidden("无业主权限或账号已禁用");
        }
        System.out.println("【2】用户权限校验通过：" + user.getName());

        if (feeRecordIds == null || feeRecordIds.isEmpty()) {
            System.out.println("【错误】费用记录 ID 为空");
            return Result.error("请选择要缴费的费用记录");
        }

        // 2. 获取业主信息
        Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getUserId, userId)
                .last("LIMIT 1"));

        System.out.println("【3】查询到的 owner 信息:");
        System.out.println("   - owner.id: " + owner.getId());
        System.out.println("   - owner.buildingNo: " + owner.getBuildingNo());
        System.out.println("   - owner.roomNo: " + owner.getRoomNo());

        if (owner == null) {
            System.out.println("【错误】业主信息不存在");
            return Result.error("业主信息不存在");
        }

        // 3. 验证费用记录归属（仅查当前业主的未缴费记录）
        System.out.println("【4】开始验证费用记录归属:");
        System.out.println("   - 查询条件：owner_id=" + owner.getId() + ", status=0, ids=" + feeRecordIds);

        List<FeeRecord> feeRecords = feeRecordService.list(
                new LambdaQueryWrapper<FeeRecord>()
                        .eq(FeeRecord::getOwnerId, owner.getId())
                        .eq(FeeRecord::getStatus, 0) // 仅未缴费
                        .in(FeeRecord::getId, feeRecordIds));

        System.out.println("【5】验证结果:");
        System.out.println("   - 符合条件的记录数：" + feeRecords.size());
        if (feeRecords.size() > 0) {
            feeRecords.forEach(r ->
                    System.out.println("   - 记录 ID=" + r.getId() + ", owner_id=" + r.getOwnerId() + ", status=" + r.getStatus())
            );
        } else {
            // 检查这些费用记录实际的 owner_id
            List<FeeRecord> allRecords = feeRecordService.list(
                    new LambdaQueryWrapper<FeeRecord>()
                            .in(FeeRecord::getId, feeRecordIds));
            System.out.println("   - 这些费用记录实际的 owner_id:");
            allRecords.forEach(r ->
                    System.out.println("     * 记录 ID=" + r.getId() + ", owner_id=" + r.getOwnerId() + ", status=" + r.getStatus())
            );
        }

        if (feeRecords.isEmpty()) {
            System.out.println("【错误】未找到可缴费的费用记录");
            System.out.println("========== 生成缴费单失败 ==========\n");
            return Result.error("未找到可缴费的费用记录");
        }
        System.out.println("========== 生成缴费单成功 ==========\n");

        // 4. 生成缴费单
        PaymentOrderDTO order = new PaymentOrderDTO();
        order.setRoomNo(owner.getBuildingNo() + "-" + owner.getRoomNo());
        order.setFeeRecordIds(feeRecordIds);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalOverdueFine = BigDecimal.ZERO;
        LocalDate latestDueDate = null;

        for (FeeRecord record : feeRecords) {
            // 累加应收金额
            totalAmount = totalAmount.add(record.getAmount());

            // 计算滞纳金（逾期1天按0.1%计算）
            LocalDate now = LocalDate.now();
            if (record.getDueDate().isBefore(now)) {
                long days = ChronoUnit.DAYS.between(record.getDueDate(), now);
                BigDecimal fine = record.getAmount()
                        .multiply(new BigDecimal("0.001"))
                        .multiply(new BigDecimal(days))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                totalOverdueFine = totalOverdueFine.add(fine);
            }

            // 取最晚的截止日期
            if (latestDueDate == null || record.getDueDate().isAfter(latestDueDate)) {
                latestDueDate = record.getDueDate();
            }

            // 单条缴费：设置费用类型名称
            if (feeRecordIds.size() == 1) {
                FeeType feeType = feeTypeService.getById(record.getFeeTypeId());
                order.setFeeTypeName(feeType != null ? feeType.getTypeName() : "未知类型");
            }
        }

        // 批量缴费：标注费用笔数
        if (feeRecordIds.size() > 1) {
            order.setFeeTypeName("批量缴费（共" + feeRecords.size() + "笔）");
        }

        // 计算总金额（应收+滞纳金）
        order.setAmount(totalAmount);
        order.setOverdueFine(totalOverdueFine);
        order.setTotalAmount(totalAmount.add(totalOverdueFine).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setDueDate(latestDueDate);

        return Result.success(order);
    }

    /**
     * 模拟缴费（单条/批量）
     * 完整路径：/fee/owner/pay
     * @param userId 登录用户ID（业主）
     * @param order 缴费单信息
     * @return 缴费结果
     */
    @PostMapping("/owner/pay")
    public Result<String> payFee(
            @RequestParam Long userId,
            @RequestBody PaymentOrderDTO order) {

        // 1. 校验业主权限
        User user = checkUserRole(userId, 2);
        if (user == null) {
            return Result.forbidden("无业主权限或账号已禁用");
        }

        if (order.getFeeRecordIds() == null || order.getFeeRecordIds().isEmpty()) {
            return Result.error("缴费记录ID不能为空");
        }

        try {
            // 模拟支付（不同支付方式成功率不同）
            boolean paySuccess = simulatePayment(order.getPayType());

            if (paySuccess) {
                // 支付成功：更新费用记录状态
                LocalDateTime now = LocalDateTime.now();
                // 系统默认物业操作员（角色=1，ID=1）
                User systemOperator = userService.getOne(new LambdaQueryWrapper<User>()
                        .eq(User::getRole, 1)
                        .eq(User::getStatus, 1)
                        .last("LIMIT 1"));
                Long operatorId = systemOperator != null ? systemOperator.getId() : 1L;

                for (Long feeRecordId : order.getFeeRecordIds()) {
                    FeeRecord record = new FeeRecord();
                    record.setId(feeRecordId);
                    record.setStatus(1); // 改为已缴费
                    record.setPayTime(now); // 填充缴费时间
                    record.setOperatorId(operatorId); // 填充物业操作员 ID

                    // 设置缴费方式（将英文转换为中文显示）
                    String payTypeDesc = convertPayTypeToChinese(order.getPayType());
                    record.setPayType(payTypeDesc);

                    // 设置实收金额（从订单中获取，如果没有则使用应收金额）
                    if (order.getAmount() != null) {
                        record.setActualAmount(order.getAmount());
                    } else {
                        // 如果订单中没有金额，查询费用记录获取应收金额
                        FeeRecord feeRecord = feeRecordService.getById(feeRecordId);
                        if (feeRecord != null && feeRecord.getAmount() != null) {
                            record.setActualAmount(feeRecord.getAmount());
                        }
                    }

                    // 设置欠费金额为0（已缴清）
                    record.setArrearsAmount(BigDecimal.ZERO);

                    // 生成唯一缴费流水号：时间戳 + 记录 ID+ 用户 ID 后 4 位
                    Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>()
                            .eq(Owner::getId, record.getOwnerId())
                            .last("LIMIT 1"));
                    if (owner != null) {
                        String roomNo = owner.getBuildingNo() + "-" + owner.getRoomNo();
                        String roomNoSuffix = roomNo.length() >= 4 ? roomNo.substring(roomNo.length() - 4) : roomNo;
                        String transactionNo = System.currentTimeMillis() + "-" + feeRecordId + "-" + roomNoSuffix;
                        record.setTransactionNo(transactionNo); // 填充缴费流水号
                    }
                    feeRecordService.updateById(record);

                    // 生成电子缴费凭证（模拟）
                    generatePaymentVoucher(feeRecordId, order.getRoomNo());
                }

                return Result.success("缴费成功，可查看缴费凭证");
            } else {
                return Result.error("模拟支付超时，请重新支付");
            }
        } catch (Exception e) {
            return Result.error("缴费失败：" + e.getMessage());
        }
    }

    /**
     * 将支付方式英文转换为中文显示
     * @param payType 支付方式英文编码
     * @return 中文描述
     */
    private String convertPayTypeToChinese(String payType) {
        if (payType == null || payType.trim().isEmpty()) {
            return "未知";
        }
        switch (payType.toUpperCase()) {
            case "WECHAT":
                return "微信支付";
            case "ALIPAY":
                return "支付宝支付";
            case "BANK":
                return "银行卡支付";
            case "CASH":
                return "现金支付";
            case "TRANSFER":
                return "银行转账";
            default:
                return payType;
        }
    }

    /**
     * 查看缴费凭证
     * 完整路径：/fee/owner/voucher
     * @param userId 登录用户ID（业主）
     * @param feeRecordId 费用记录ID
     * @return 凭证信息
     */
    @GetMapping("/owner/voucher")
    public Result<String> getPaymentVoucher(
            @RequestParam Long userId,
            @RequestParam Long feeRecordId) {

        // 1. 校验业主权限
        User user = checkUserRole(userId, 2);
        if (user == null) {
            return Result.forbidden("无业主权限或账号已禁用");
        }

        // 2. 查询费用记录（校验归属）
        FeeRecord record = feeRecordService.getById(feeRecordId);
        if (record == null) {
            return Result.error("费用记录不存在");
        }

        // 3. 校验费用记录归属当前业主
        Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId));
        if (!owner.getId().equals(record.getOwnerId())) {
            return Result.forbidden("无权查看该缴费凭证");
        }

        // 4. 拼接房号
        String roomNo = owner.getBuildingNo() + "-" + owner.getRoomNo();

        // 5. 生成唯一凭证编号
        String roomNoSuffix = roomNo.length() >= 4 ? roomNo.substring(roomNo.length() - 4) : roomNo;
        String voucherNo = System.currentTimeMillis() + "-" + feeRecordId + "-" + roomNoSuffix;

        // 6. 关联操作员信息
        User operator = userService.getById(record.getOperatorId());
        String operatorName = operator != null ? operator.getName() : "系统";

        // 7. 拼接凭证信息
        String voucherInfo = "===== 电子缴费凭证 =====\n" +
                "凭证编号：" + voucherNo + "\n" +
                "房号：" + roomNo + "\n" +
                "缴费金额：¥" + record.getAmount() + "\n" +
                "缴费时间：" + (record.getPayTime() != null ? record.getPayTime().toString().replace("T", " ") : "未知") + "\n" +
                "核销人员：" + operatorName + "\n" +
                "========================";

        return Result.success(voucherInfo);
    }

    // ====================== 物业端接口（角色=1） ======================

    /**
     * 新增费用类型
     * 完整路径：/fee/admin/addFeeType
     * @param adminId 物业管理员 ID
     * @param typeName 费用类型名称
     * @param unitPrice 单价
     * @param calcType 计费类型：1-关联计算、2-固定金额、3-自定义
     * @param relatedField 关联字段（多个用逗号分隔）
     * @param conditionType 条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于
     * @param conditionField 条件字段
     * @param conditionValue 条件值
     * @param remark 备注
     * @return 操作结果
     */
    @PostMapping("/admin/addFeeType")
    public Result<Boolean> addFeeType(
            @RequestParam Long adminId,
            @RequestParam String typeName,
            @RequestParam(required = false) BigDecimal unitPrice,
            @RequestParam(required = false) String calcType,
            @RequestParam(required = false) String relatedField,
            @RequestParam(required = false) String conditionType,
            @RequestParam(required = false) String conditionField,
            @RequestParam(required = false) String conditionValue,
            @RequestParam(required = false) String remark) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            // 如果不是角色 1，再检查是否是角色 3
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }

        if (!StringUtils.hasText(typeName)) {
            return Result.error("费用类型名称不能为空");
        }

        // 2. 设置费用类型信息
        FeeType feeType = new FeeType();
        feeType.setTypeName(typeName);
        feeType.setUnitPrice(unitPrice);
        feeType.setCalcType(calcType != null ? calcType : "1");
        feeType.setRelatedField(relatedField);
        feeType.setConditionType(conditionType != null ? conditionType : "NONE");
        feeType.setConditionField(conditionField);
        feeType.setConditionValue(conditionValue);
        feeType.setRemark(remark != null ? remark : "");

        boolean success = feeTypeService.save(feeType);
        return success ? Result.success("新增费用类型成功") : Result.error("新增失败");
    }

    /**
     * 修改费用类型
     * 完整路径：/fee/admin/updateFeeType
     * @param id 费用类型 ID
     * @param typeName 费用类型名称
     * @param unitPrice 单价
     * @param calcType 计费类型：1-关联计算、2-固定金额
     * @param relatedField 关联字段（多个用逗号分隔）
     * @param conditionType 条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于
     * @param conditionField 条件字段
     * @param conditionValue 条件值
     * @param remark 备注
     * @return 操作结果
     */
    @PostMapping("/admin/updateFeeType")
    public Result<Boolean> updateFeeType(
            @RequestParam Long id,
            @RequestParam String typeName,
            @RequestParam(required = false) BigDecimal unitPrice,
            @RequestParam(required = false) String calcType,
            @RequestParam(required = false) String relatedField,
            @RequestParam(required = false) String conditionType,
            @RequestParam(required = false) String conditionField,
            @RequestParam(required = false) String conditionValue,
            @RequestParam(required = false) String remark) {

        // 1. 查询费用类型
        FeeType feeType = feeTypeService.getById(id);
        if (feeType == null) {
            return Result.error("费用类型不存在");
        }

        // 2. 更新费用类型信息
        feeType.setTypeName(typeName);
        feeType.setUnitPrice(unitPrice);
        feeType.setCalcType(calcType != null ? calcType : feeType.getCalcType());
        feeType.setRelatedField(relatedField);
        feeType.setConditionType(conditionType != null ? conditionType : "NONE");
        feeType.setConditionField(conditionField);
        feeType.setConditionValue(conditionValue);
        feeType.setRemark(remark != null ? remark : "");

        boolean success = feeTypeService.updateById(feeType);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }


    /**
     * 批量生成费用账单
     * 完整路径：/fee/admin/batchGenerateFee
     * @param adminId 物业管理员 ID
     * @param buildingNo 楼栋号（必填）
     * @param roomNos 房间号列表，多个用逗号分隔（为空则生成该楼栋所有房间）
     * @param feeTypeId 费用类型 ID
     * @param amount 金额（可选，按面积计费时可为空）
     * @param calcType 计费类型：1-关联计算、2-固定金额
     * @param relatedField 关联字段（多个用逗号分隔）
     * @param unitPrice 单价
     * @param conditionType 条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于
     * @param conditionField 条件字段
     * @param conditionValue 条件值
     * @param dueDate 缴费截止日期
     * @return 生成结果
     */
    @PostMapping("/admin/batchGenerateFee")
    public Result<String> batchGenerateFee(
            @RequestParam Long adminId,
            @RequestParam String buildingNo,
            @RequestParam(required = false) String roomNos,
            @RequestParam Long feeTypeId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String calcType,
            @RequestParam(required = false) String relatedField,
            @RequestParam(required = false) BigDecimal unitPrice,
            @RequestParam(required = false) String conditionType,
            @RequestParam(required = false) String conditionField,
            @RequestParam(required = false) String conditionValue,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            // 如果不是角色 1，再检查是否是角色 3
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }

        // 2. 校验楼栋号
        if (!StringUtils.hasText(buildingNo)) {
            return Result.error("楼栋号不能为空");
        }

        // 3. 查询费用类型
        FeeType feeType = feeTypeService.getById(feeTypeId);
        if (feeType == null) {
            return Result.error("费用类型不存在");
        }

        // 4. 解析多个房间号
        List<String> targetRooms = new ArrayList<>();
        if (StringUtils.hasText(roomNos)) {
            // 支持英文逗号和中文逗号分隔
            String[] rooms = roomNos.split("[,,]");
            for (String room : rooms) {
                if (StringUtils.hasText(room.trim())) {
                    targetRooms.add(room.trim());
                }
            }
        }

        // 5. 筛选符合条件的业主
        LambdaQueryWrapper<Owner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Owner::getBuildingNo, buildingNo);

        // 如果指定了房间号，筛选房间号
        if (!targetRooms.isEmpty()) {
            queryWrapper.and(wrapper -> {
                for (String room : targetRooms) {
                    wrapper.or(w -> w.eq(Owner::getRoomNo, room)
                            .or()
                            .eq(Owner::getRoomNo, buildingNo + "-" + room));
                }
            });
        }

        List<Owner> owners = ownerService.list(queryWrapper);

        if (owners.isEmpty()) {
            return Result.error("未找到符合条件的业主");
        }

        // 6. 批量生成费用记录
        LocalDateTime now = LocalDateTime.now();
        List<FeeRecord> feeRecords = owners.stream().map(owner -> {
            FeeRecord record = new FeeRecord();
            record.setOwnerId(owner.getId());
            record.setFeeTypeId(feeTypeId);

            // 根据费用类型的计费类型计算金额
            BigDecimal calculatedAmount = amount;
            if ("1".equals(feeType.getCalcType()) && feeType.getUnitPrice() != null) {
                // 关联计算：先判断是否满足计费条件
                boolean shouldCharge = true;
                if (StringUtils.hasText(conditionType) && !"NONE".equals(conditionType) &&
                        StringUtils.hasText(conditionField) && StringUtils.hasText(conditionValue)) {

                    String fieldValue = null;
                    switch (conditionField) {
                        case "PARKING_SPACE_TYPE":
                            fieldValue = owner.getParkingSpaceType();
                            break;
                        case "HOUSE_USAGE":
                            fieldValue = owner.getHouseUsage();
                            break;
                        case "HOUSE_STRUCTURE":
                            fieldValue = owner.getHouseStructure();
                            break;
                    }

                    if ("EQUAL".equals(conditionType)) {
                        shouldCharge = conditionValue.equals(fieldValue);
                    } else if ("NOT_EQUAL".equals(conditionType)) {
                        shouldCharge = !conditionValue.equals(fieldValue);
                    }
                }

                if (shouldCharge) {
                    // 解析关联字段（支持多个，逗号分隔）
                    String[] fields = feeType.getRelatedField() != null ?
                            feeType.getRelatedField().split(",") : new String[]{"HOUSE_AREA"};

                    BigDecimal totalArea = BigDecimal.ZERO;
                    for (String field : fields) {
                        BigDecimal area = null;
                        if ("HOUSE_AREA".equals(field)) {
                            area = owner.getHouseArea();
                        } else if ("INTERNAL_AREA".equals(field)) {
                            area = owner.getInternalArea();
                        }

                        if (area != null) {
                            totalArea = totalArea.add(area);
                        }
                    }

                    calculatedAmount = feeType.getUnitPrice().multiply(totalArea)
                            .setScale(2, BigDecimal.ROUND_HALF_UP);
                } else {
                    // 不满足条件，金额为 0
                    calculatedAmount = BigDecimal.ZERO;
                }
            } else if ("2".equals(feeType.getCalcType()) && feeType.getUnitPrice() != null) {
                // 固定金额：直接使用费用类型的单价
                calculatedAmount = feeType.getUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP);
            } else {
                // 自定义或其他计费类型，使用传入的金额
                calculatedAmount = amount != null ? amount : BigDecimal.ZERO;
            }

            record.setAmount(calculatedAmount);
            record.setDueDate(dueDate);
            record.setStatus(0); // 初始状态：未缴
            record.setOperatorId(adminId); // 生成账单的物业操作员 ID
            record.setCreateTime(now);
            record.setUpdateTime(now);
            return record;
        }).collect(Collectors.toList());

        // 7. 批量保存
        boolean success = feeRecordService.saveBatch(feeRecords);
        return success ? Result.success("成功生成" + feeRecords.size() + "条账单") : Result.error("账单生成失败");
    }

    /**
     * 缴费核销（单条）
     * 完整路径：/fee/admin/writeOff
     * @param adminId 物业管理员ID
     * @param feeRecordId 费用记录ID
     * @return 核销结果
     */
    @PutMapping("/admin/writeOff")
    public Result<Boolean> writeOffFee(
            @RequestParam Long adminId,
            @RequestParam Long feeRecordId) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            return Result.forbidden("无物业管理员权限或账号已禁用");
        }

        FeeRecord record = new FeeRecord();
        record.setId(feeRecordId);
        record.setStatus(1); // 改为已缴
        record.setPayTime(LocalDateTime.now()); // 核销时间
        record.setOperatorId(adminId); // 核销人员ID（当前物业管理员）

        boolean success = feeRecordService.updateById(record);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    /**
     * 批量核销
     * 完整路径：/fee/admin/batchWriteOff
     * @param adminId 物业管理员ID
     * @param feeRecordIds 费用记录ID列表
     * @return 核销结果
     */
    @PutMapping("/admin/batchWriteOff")
    public Result<String> batchWriteOffFee(
            @RequestParam Long adminId,
            @RequestBody List<Long> feeRecordIds) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            // 如果不是角色 1，再检查是否是角色 3
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }

        if (feeRecordIds.isEmpty()) {
            return Result.error("请选择要核销的账单");
        }

        // 批量更新核销状态
        LocalDateTime now = LocalDateTime.now();
        List<FeeRecord> records = feeRecordIds.stream().map(id -> {
            FeeRecord record = new FeeRecord();
            record.setId(id);
            record.setStatus(1);
            record.setPayTime(now);
            record.setOperatorId(adminId); // 当前核销的物业管理员ID
            return record;
        }).collect(Collectors.toList());

        boolean success = feeRecordService.updateBatchById(records);
        return success ? Result.success("批量核销成功，共核销" + records.size() + "条记录") : Result.error("批量核销失败");
    }

    /**
     * 创建催缴记录（核心修正：status直接设为1，已发送）
     * 完整路径：/fee/admin/createReminder
     * @param adminId 物业管理员ID
     * @param feeRecordId 费用记录ID
     * @param reminderType 催缴类型（1-模板消息/2-短信）
     * @param remark 催缴备注（接收但不存储，因实体类不可改）
     * @return 操作结果
     */
    @PostMapping("/admin/createReminder")
    public Result<String> createReminder(
            @RequestParam Long adminId,
            @RequestParam Long feeRecordId,
            @RequestParam Integer reminderType,
            @RequestParam(required = false) String remark) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            return Result.forbidden("无物业管理员权限或账号已禁用");
        }

        // 2. 校验催缴类型
        if (reminderType != 1 && reminderType != 2) {
            return Result.error("催缴类型只能是1（模板消息）或2（短信）");
        }

        FeeReminder reminder = new FeeReminder();
        reminder.setFeeRecordId(feeRecordId);
        reminder.setReminderTime(LocalDateTime.now()); // 催缴记录创建时间
        reminder.setReminderType(reminderType);
        // 核心修正：创建时直接设置status=1（已发送），而非0
        reminder.setStatus(1);

        boolean success = feeReminderService.save(reminder);
        // 适配前端：返回String类型结果，包含code和msg
        if (success) {
            return Result.success("催缴记录创建成功（已发送）");
        } else {
            return Result.error("创建失败");
        }
    }

    /**
     * 发送催缴通知（保留接口，兼容原有逻辑）
     * 完整路径：/fee/admin/sendReminder
     * @param adminId 物业管理员ID
     * @param reminderId 催缴记录ID
     * @return 发送结果
     */
    @PostMapping("/admin/sendReminder")
    public Result<String> sendReminder(
            @RequestParam Long adminId,
            @RequestParam Long feeRecordId) {

        System.out.println("\n========== 发送催缴通知 ==========");
        System.out.println("【1】传入参数:");
        System.out.println("   - adminId: " + adminId);
        System.out.println("   - feeRecordId: " + feeRecordId);

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            // 如果不是角色 1，再检查是否是角色 3
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                System.out.println("【错误】用户权限校验失败");
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }
        System.out.println("【2】用户权限校验通过：" + admin.getName());

        // 2. 验证费用记录是否存在
        FeeRecord feeRecord = feeRecordService.getById(feeRecordId);
        if (feeRecord == null) {
            System.out.println("【错误】费用记录不存在，feeRecordId: " + feeRecordId);
            return Result.error("费用记录不存在");
        }
        System.out.println("【3】查询到费用记录:");
        System.out.println("   - ownerId: " + feeRecord.getOwnerId());
        System.out.println("   - amount: " + feeRecord.getAmount());
        System.out.println("   - status: " + feeRecord.getStatus());

        // 3. 查询该费用记录的催缴记录
        List<FeeReminder> reminders = feeReminderService.list(
                new LambdaQueryWrapper<FeeReminder>()
                        .eq(FeeReminder::getFeeRecordId, feeRecordId)
                        .orderByDesc(FeeReminder::getReminderTime)
                        .last("LIMIT 1"));

        FeeReminder reminder;
        boolean isNewReminder = false;

        if (reminders.isEmpty()) {
            // 4a. 如果不存在催缴记录，则创建新的催缴记录
            System.out.println("【4a】未找到催缴记录，创建新的催缴记录...");
            reminder = new FeeReminder();
            reminder.setFeeRecordId(feeRecordId);
            reminder.setReminderTime(LocalDateTime.now());
            reminder.setReminderType(1); // 默认模板消息
            reminder.setStatus(1); // 直接设置为已发送
            feeReminderService.save(reminder);
            isNewReminder = true;
            System.out.println("【5a】新催缴记录创建成功，reminderId: " + reminder.getId());
        } else {
            // 4b. 如果存在催缴记录，使用最新的
            reminder = reminders.get(0);
            System.out.println("【4b】查询到催缴记录:");
            System.out.println("   - reminderId: " + reminder.getId());
            System.out.println("   - status: " + reminder.getStatus());

            // 若已发送，直接返回成功
            if (reminder.getStatus() == 1) {
                System.out.println("【提示】该催缴记录已发送");
                return Result.success("该催缴记录已发送");
            }
        }

        // 5. 模拟发送催缴通知
        System.out.println("【6】开始发送催缴通知...");
        boolean sendSuccess = simulateSendReminder(reminder.getId());

        if (sendSuccess) {
            // 更新催缴记录状态为已发送
            reminder.setStatus(1);
            feeReminderService.updateById(reminder);
            System.out.println("【7】催缴通知发送成功");
            System.out.println("========== 发送结束 ==========\n");

            String message = isNewReminder ? "催缴记录创建并发送成功" : "催缴通知发送成功";
            return Result.success(message);
        } else {
            System.out.println("【错误】催缴通知发送失败");
            System.out.println("========== 发送结束 ==========\n");
            return Result.error("催缴通知发送失败");
        }
    }

    /**
     * 物业端费用记录查询（多维度筛选，优化催缴信息展示）
     * 完整路径：/fee/admin/query
     * @param adminId 物业管理员ID
     * @param queryDTO 筛选条件
     * @param startDueDateStr 前端传递的开始日期字符串（yyyy-MM-dd）
     * @param endDueDateStr 前端传递的结束日期字符串（yyyy-MM-dd）
     * @return 分页费用列表
     */
    @GetMapping("/admin/query")
    public Result<IPage<FeeRecordVO>> queryAdminFee(
            @RequestParam Long adminId,
            FeeQueryDTO queryDTO,
            // 新增：接收前端传递的日期字符串参数
            @RequestParam(required = false) String startDueDateStr,
            @RequestParam(required = false) String endDueDateStr) {

        // 1. 日期字符串转 LocalDate 并设置到 queryDTO（核心修复）
        try {
            if (StringUtils.hasText(startDueDateStr)) {
                queryDTO.setStartDueDate(LocalDate.parse(startDueDateStr));
            }
            if (StringUtils.hasText(endDueDateStr)) {
                queryDTO.setEndDueDate(LocalDate.parse(endDueDateStr));
            }
        } catch (Exception e) {
            // 日期格式错误时返回友好提示
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式（如：2026-03-31）");
        }

        // 3. 校验物业管理员权限（允许角色 1 或 3）
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            // 如果不是角色 1，再检查是否是角色 3
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }

        // 3. 分页查询费用记录（先查询所有符合条件的记录，再内存分页）
        // 查询所有记录（不分页）
        IPage<FeeRecord> allPage = new Page<>(1, 1000);
        IPage<FeeRecord> feePage = feeRecordService.queryFeeRecordPage(allPage, queryDTO);

        // 4. 如果没有数据，直接返回空结果
        if (feePage.getRecords() == null || feePage.getRecords().isEmpty()) {
            IPage<FeeRecordVO> voPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
            voPage.setRecords(new java.util.ArrayList<>());
            return Result.success(voPage);
        }

        // 5. 查询所有涉及的业主信息
        List<Long> ownerIds = feePage.getRecords().stream()
                .map(FeeRecord::getOwnerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Owner> owners = new java.util.ArrayList<>();
        if (!ownerIds.isEmpty()) {
            owners = ownerService.listByIds(ownerIds);
        }

        java.util.Map<Long, Owner> ownerMap = owners.stream()
                .collect(java.util.stream.Collectors.toMap(Owner::getId, o -> o));

        // 6. 在内存中对所有数据排序：先按楼栋号升序，再按房号升序
        List<FeeRecord> allRecords = feePage.getRecords();
        allRecords.sort((r1, r2) -> {
            Owner o1 = ownerMap.get(r1.getOwnerId());
            Owner o2 = ownerMap.get(r2.getOwnerId());

            if (o1 == null || o2 == null) {
                return 0;
            }

            // 先按楼栋号升序
            String b1 = o1.getBuildingNo() != null ? o1.getBuildingNo() : "";
            String b2 = o2.getBuildingNo() != null ? o2.getBuildingNo() : "";
            int buildingCompare = b1.compareTo(b2);
            if (buildingCompare != 0) {
                return buildingCompare;
            }

            // 再按房号升序
            String rNo1 = o1.getRoomNo() != null ? o1.getRoomNo() : "";
            String rNo2 = o2.getRoomNo() != null ? o2.getRoomNo() : "";
            return rNo1.compareTo(rNo2);
        });

        // 7. 手动分页
        long total = allRecords.size();
        long pageNum = queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize();

        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);

        List<FeeRecord> pageRecords = fromIndex < total && fromIndex >= 0 ?
                allRecords.subList(fromIndex, toIndex) :
                new java.util.ArrayList<>();

        // 8. 转换为 VO（完善字段映射）
        IPage<FeeRecordVO> voPage = new Page<>(pageNum, pageSize, total);
        List<FeeRecordVO> voList = pageRecords.stream().map(feeRecord -> {
            FeeRecordVO vo = new FeeRecordVO();
            BeanUtils.copyProperties(feeRecord, vo);

            // 显式设置 ownerId（用于风险评估）
            vo.setOwnerId(feeRecord.getOwnerId());

            // 关联业主信息
            Owner owner = ownerMap.get(feeRecord.getOwnerId());
            if (owner != null) {
                vo.setBuildingNo(owner.getBuildingNo());
                vo.setRoomNo(owner.getBuildingNo() + "-" + owner.getRoomNo());
                vo.setHouseArea(owner.getHouseArea());
                vo.setInternalArea(owner.getInternalArea());

                // 关联业主信息（姓名 + 电话）
                User ownerUser = userService.getById(owner.getUserId());
                if (ownerUser != null) {
                    vo.setOwnerName(ownerUser.getName());
                    vo.setPhone(ownerUser.getPhone());
                } else {
                    vo.setOwnerName("未知");
                    vo.setPhone("未知");
                }
            } else {
                vo.setBuildingNo("未知");
                vo.setRoomNo("未知");
                vo.setOwnerName("未知");
                vo.setPhone("未知");
            }

            // 关联费用类型
            FeeType feeType = feeTypeService.getById(feeRecord.getFeeTypeId());
            vo.setFeeTypeName(feeType != null ? feeType.getTypeName() : "未知类型");

            // 计算单价（根据面积和金额）
            if (feeType != null && vo.getHouseArea() != null && vo.getHouseArea().compareTo(BigDecimal.ZERO) > 0) {
                vo.setUnitPrice(vo.getAmount().divide(vo.getHouseArea(), 2, BigDecimal.ROUND_HALF_UP));
            }

            // 设置计费周期（按年度或月度）
            if (feeType != null) {
                String typeName = feeType.getTypeName();
                if (typeName.contains("年") || typeName.contains("物业")) {
                    vo.setChargePeriod(LocalDate.now().getYear() + "年度");
                } else if (typeName.contains("月") || typeName.contains("水电")) {
                    vo.setChargePeriod(LocalDate.now().getYear() + "年" + LocalDate.now().getMonthValue() + "月");
                } else {
                    vo.setChargePeriod("一次性");
                }
            }

            // 状态转换
            if (1 == feeRecord.getStatus()) {
                vo.setStatusDesc("已缴费");
                // 核销人员姓名（物业人员）
                User operator = userService.getById(feeRecord.getOperatorId());
                vo.setOperatorName(operator != null ? operator.getName() : "未知");
            } else if (2 == feeRecord.getStatus()) {
                vo.setStatusDesc("已撤销");
                vo.setOperatorName("未知");
            } else {
                LocalDate now = LocalDate.now();
                if (feeRecord.getDueDate().isAfter(now)) {
                    vo.setStatusDesc("待缴费");
                } else {
                    vo.setStatusDesc("逾期欠费");
                    long days = ChronoUnit.DAYS.between(feeRecord.getDueDate(), now);
                    vo.setOverdueDays((int) days);
                }

                // 最新催缴信息
                List<FeeReminder> reminders = feeReminderService.list(
                        new LambdaQueryWrapper<FeeReminder>()
                                .eq(FeeReminder::getFeeRecordId, feeRecord.getId())
                                .orderByDesc(FeeReminder::getReminderTime)
                                .last("LIMIT 1"));
                if (!reminders.isEmpty()) {
                    FeeReminder reminder = reminders.get(0);
                    String type = reminder.getReminderType() == 1 ? "模板消息" : "短信";
                    String status = reminder.getStatus() == 1 ? "已发送" : "未发送";
                    String timeStr = reminder.getReminderTime().toLocalDate().toString();
                    vo.setLatestReminder(timeStr + " " + type + "（" + status + "）");
                } else {
                    vo.setLatestReminder("无催缴");
                }
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    // ====================== 保留原测试接口（兼容/test和/list） ======================
    /**
     * POST测试接口（兼容原有测试路径）
     * 完整路径：/fee/test
     */
    @PostMapping("/test")
    public String test(@RequestBody String body) {
        System.out.println("post已请求");
        return "post请求成功";
    }

    /**
     * GET测试接口（兼容原有测试路径）
     * 完整路径：/fee/list
     */
    @GetMapping("/list")
    public String get() {
        System.out.println("get已请求");
        return "get请求成功";
    }

    // ====================== 私有辅助方法 ======================

    /**
     * 模拟支付（不同支付方式成功率不同）
     * @param payType 支付方式：WECHAT/ALIPAY/BANK
     * @return 支付是否成功
     */
    private boolean simulatePayment(String payType) {
        if (payType == null) {
            return false;
        }
        switch (payType) {
            case "WECHAT":
                return true; // 微信支付100%成功
            case "ALIPAY":
                return Math.random() > 0.1; // 支付宝90%成功
            case "BANK":
                return Math.random() > 0.2; // 银行卡80%成功
            default:
                return false;
        }
    }

    /**
     * 生成缴费凭证（模拟）
     * @param feeRecordId 费用记录ID
     * @param roomNo 房号
     */
    private void generatePaymentVoucher(Long feeRecordId, String roomNo) {
        // 实际项目中可生成PDF/图片文件，此处仅打印日志
        System.out.println("生成缴费凭证：feeRecordId=" + feeRecordId + ", roomNo=" + roomNo);
    }

    /**
     * 模拟发送催缴通知
     * @param reminderId 催缴记录ID
     * @return 发送是否成功
     */
    private boolean simulateSendReminder(Long reminderId) {
        // 模拟95%的发送成功率
        return Math.random() > 0.05;
    }
    /**
     * 获取所有费用类型列表（用于前端下拉选择）
     * 完整路径：/fee/type/list
     * @return 费用类型列表
     */
    @GetMapping("/type/list")
    public Result<List<FeeType>> listFeeTypes() {
        List<FeeType> feeTypes = feeTypeService.list();
        return Result.success(feeTypes);
    }
    @DeleteMapping("/admin/deleteFeeType")
    public Result<Boolean> deleteFeeType(@RequestParam Long id) {
        boolean success = feeTypeService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }


    /**
     * 导出费用记录到 Excel
     * GET /fee/admin/export
     */
    @GetMapping("/admin/export")
    public void exportFeeRecords(
            @RequestParam Long adminId,
            @RequestParam(required = false) String feeRecordIds, // 费用记录 ID 列表，逗号分隔
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDueDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDueDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            HttpServletResponse response) throws IOException {
        try {
            log.info("导出费用记录，adminId: {}, feeRecordIds: {}", adminId, feeRecordIds);

            List<FeeRecord> filteredRecords = new ArrayList<>();

            // 判断是否传了 feeRecordIds，如果传了就按 ID 导出，否则按筛选条件导出
            if (StringUtils.hasText(feeRecordIds)) {
                // 按 ID 列表导出
                String[] idArray = feeRecordIds.split(",");
                List<Long> ids = new ArrayList<>();
                for (String id : idArray) {
                    if (StringUtils.hasText(id.trim())) {
                        ids.add(Long.parseLong(id.trim()));
                    }
                }

                if (!ids.isEmpty()) {
                    LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(FeeRecord::getId, ids);
                    filteredRecords = feeRecordService.list(queryWrapper);
                    log.info("按 ID 导出，共{}条记录", filteredRecords.size());
                }
            } else {
                // 按筛选条件导出（原有逻辑）
                log.info("按筛选条件导出，keyword: {}, typeName: {}, status: {}", keyword, typeName, status);

                // 1. 先查询所有符合条件的费用记录 ID
                LambdaQueryWrapper<FeeRecord> queryWrapper = new LambdaQueryWrapper<>();

                // 基础权限校验
                queryWrapper.eq(FeeRecord::getOperatorId, adminId);

                // 缴费状态
                if (status != null) {
                    queryWrapper.eq(FeeRecord::getStatus, status);
                }

                // 截止日期范围
                if (startDueDate != null) {
                    queryWrapper.ge(FeeRecord::getDueDate, startDueDate);
                }
                if (endDueDate != null) {
                    queryWrapper.le(FeeRecord::getDueDate, endDueDate);
                }

                // 金额范围
                if (minAmount != null) {
                    queryWrapper.ge(FeeRecord::getAmount, minAmount);
                }
                if (maxAmount != null) {
                    queryWrapper.le(FeeRecord::getAmount, maxAmount);
                }

                // 查询所有符合条件的记录（不分页）
                List<FeeRecord> allFeeRecords = feeRecordService.list(queryWrapper);
                log.info("查询到{}条费用记录", allFeeRecords.size());

                // 在内存中过滤费用类型和关键词
                for (FeeRecord record : allFeeRecords) {
                    // 过滤费用类型
                    if (StringUtils.hasText(typeName)) {
                        FeeType feeType = feeTypeService.getById(record.getFeeTypeId());
                        if (feeType == null || !typeName.equals(feeType.getTypeName())) {
                            continue;
                        }
                    }

                    // 模糊搜索：房号/业主姓名（需要关联 Owner 表）
                    if (StringUtils.hasText(keyword)) {
                        Owner owner = ownerService.getById(record.getOwnerId());
                        if (owner == null) {
                            continue;
                        }

                        String roomNo = owner.getBuildingNo() + "-" + owner.getRoomNo();
                        User ownerUser = userService.getById(owner.getUserId());
                        String ownerName = ownerUser != null ? ownerUser.getName() : "";

                        if (!roomNo.contains(keyword) && !ownerName.contains(keyword)) {
                            continue;
                        }
                    }

                    filteredRecords.add(record);
                }

                log.info("过滤后剩余{}条费用记录", filteredRecords.size());
            }

            // 生成文件名：费用记录_年月日时分秒
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "费用记录_" + timestamp;

            // 导出 Excel
            ExcelExportUtil.exportFeeRecordsToExcel(
                    response,
                    filteredRecords,
                    fileName,
                    ownerService,
                    userService,
                    feeTypeService
            );

            log.info("导出费用记录成功，共{}条", filteredRecords.size());
        } catch (IOException e) {
            log.error("导出费用记录失败", e);
            throw e; // 重新抛出，让 Spring Boot 全局异常处理器处理
        } catch (Exception e) {
            log.error("导出费用记录失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 获取业主欠费风险预测
     * GET /fee/owner/arrearsRisk
     * @param userId 登录用户ID（业主）
     * @return 风险评估结果
     */
    @GetMapping("/owner/arrearsRisk")
    public Result<Map<String, Object>> getArrearsRisk(@RequestParam Long userId) {
        // 1. 校验业主权限
        User user = checkUserRole(userId, 2);
        if (user == null) {
            return Result.forbidden("无业主权限或用户已禁用");
        }

        // 2. 获取业主信息
        Owner owner = ownerService.getOne(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getUserId, userId)
                .last("LIMIT 1"));

        if (owner == null) {
            return Result.error("业主信息不存在");
        }

        // 3. 调用风险预测算法
        Map<String, Object> riskAssessment = feeReminderService.predictArrearsRisk(owner.getId());

        return Result.success("风险评估成功", riskAssessment);
    }

    /**
     * 物业端查看业主欠费风险
     * GET /fee/admin/arrearsRisk
     * @param adminId 物业管理员ID
     * @param ownerId 业主ID
     * @return 风险评估结果
     */
    @GetMapping("/admin/arrearsRisk")
    public Result<Map<String, Object>> getOwnerArrearsRisk(
            @RequestParam Long adminId,
            @RequestParam Long ownerId) {

        // 1. 校验物业管理员权限
        User admin = checkUserRole(adminId, 1);
        if (admin == null) {
            admin = checkUserRole(adminId, 3);
            if (admin == null) {
                return Result.forbidden("无物业管理员权限或账号已禁用");
            }
        }

        // 2. 验证业主存在性
        Owner owner = ownerService.getById(ownerId);
        if (owner == null) {
            return Result.error("业主信息不存在");
        }

        // 3. 调用风险预测算法
        Map<String, Object> riskAssessment = feeReminderService.predictArrearsRisk(ownerId);

        return Result.success("风险评估成功", riskAssessment);
    }
}