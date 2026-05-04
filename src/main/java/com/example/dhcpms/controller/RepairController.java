package com.example.dhcpms.controller;

import com.example.dhcpms.common.RepairStatusEnum;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.dto.*;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.RepairOrder;
import com.example.dhcpms.entity.RepairType;
import com.example.dhcpms.mapper.OwnerMapper;
import com.example.dhcpms.mapper.RepairOrderMapper;
import com.example.dhcpms.mapper.RepairTypeMapper;
import com.example.dhcpms.service.RepairService;
import com.example.dhcpms.vo.RepairOrderDetailVO;
import com.example.dhcpms.vo.RepairOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报修管理控制器
 * 路径统一改为 /repair（去掉api前缀）
 * 基于全局统一返回结果类 Result<T> 实现
 */
@RestController
@RequestMapping("/repair") // 核心修改：将/api/repair改为/repair
@Slf4j
public class RepairController {

    @Autowired
    private RepairService repairService;

    @Autowired
    private RepairTypeMapper repairTypeMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;


    @Autowired
    private OwnerMapper ownerMapper;


    // ====================== 业主端接口 ======================

    /**
     * 提交报修申请
     * POST /repair/owner/submit（原/api/repair/owner/submit）
     */
    @PostMapping("/owner/submit")
    public Result<Map<String, Object>> submitRepairApply(HttpServletRequest request,
                                                         @RequestBody RepairApplyDTO repairDTO) {
        try {
            // 1. 获取业主 ID
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户 ID 格式错误");
            }

            // 2. 提交报修申请
            Long repairOrderId = repairService.submitRepairApply(userId, repairDTO);

            // 3. 查询报修单号返回给前端
            RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
            Map<String, Object> result = new HashMap<>();
            result.put("repairOrderId", repairOrderId);
            result.put("repairOrderNo", repairOrder.getRepairOrderNo());

            return Result.success("报修申请提交成功，报修单号为：" + repairOrder.getRepairOrderNo(), result);
        } catch (IllegalArgumentException e) {
            log.warn("提交报修申请参数异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            log.error("提交报修申请业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("提交报修申请系统异常", e);
            return Result.error("系统异常，提交报修申请失败");
        }
    }

    /**
     * 查看我的报修列表
     * GET /repair/owner/list（原/api/repair/owner/list）
     */
    @GetMapping("/owner/list")
    public Result<List<RepairOrderVO>> getMyRepairList(HttpServletRequest request) {
        try {
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户ID格式错误");
            }

            List<RepairOrderVO> repairList = repairService.getMyRepairList(userId);
            // 空列表也返回成功，自定义友好消息
            return Result.success("查询报修列表成功，共" + repairList.size() + "条记录", repairList);
        } catch (RuntimeException e) {
            log.error("查询报修列表业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询报修列表系统异常", e);
            return Result.error("系统异常，查询报修列表失败");
        }
    }

    /**
     * 查看报修单详情
     * GET /repair/owner/detail?repairOrderId=1（原/api/repair/owner/detail）
     */
    @GetMapping("/owner/detail")
    public Result<RepairOrderDetailVO> getRepairDetail(@RequestParam Long repairOrderId,
                                                       HttpServletRequest request) {
        try {
            // 1. 登录校验
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户ID格式错误");
            }

            // 2. 权限校验+查询详情
            RepairOrderDetailVO detail = repairService.getRepairDetail(repairOrderId, userId);
            if (detail == null) {
                return Result.error("报修单不存在");
            }

            return Result.success("查询报修单详情成功", detail);
        } catch (SecurityException e) {
            // 权限不足使用403响应码
            return Result.forbidden(e.getMessage());
        } catch (RuntimeException e) {
            log.error("查询报修单详情业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询报修单详情系统异常", e);
            return Result.error("系统异常，查询报修单详情失败");
        }
    }

    /**
     * 业主验收报修单
     * POST /repair/owner/acceptance（原/api/repair/owner/acceptance）
     */
    @PostMapping("/owner/acceptance")
    public Result<String> acceptRepair(HttpServletRequest request,
                                       @RequestBody RepairAcceptanceDTO acceptanceDTO) {
        try {
            // 1. 登录校验
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户ID格式错误");
            }

            // 2. 校验验收参数
            if (acceptanceDTO.getRepairOrderId() == null) {
                return Result.error("报修单号不能为空");
            }
            if (acceptanceDTO.getAcceptResult() == null) {
                return Result.error("验收结果不能为空");
            }
            // 验收通过时，评分不能为空且在1-5分之间
            if (acceptanceDTO.getAcceptResult() && (acceptanceDTO.getScore() == null
                    || acceptanceDTO.getScore() < 1 || acceptanceDTO.getScore() > 5)) {
                return Result.error("验收通过时，评分不能为空且必须为1-5分");
            }

            // 3. 处理验收逻辑
            String resultMsg = repairService.handleRepairAcceptance(userId, acceptanceDTO);
            return Result.success(resultMsg);
        } catch (SecurityException e) {
            return Result.forbidden(e.getMessage());
        } catch (RuntimeException e) {
            log.error("验收报修单业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("验收报修单系统异常", e);
            return Result.error("系统异常，验收操作失败");
        }
    }

    /**
     * 重新提交报修申请
     * POST /repair/owner/resubmit（原/api/repair/owner/resubmit）
     */
    @PostMapping("/owner/resubmit")
    public Result<String> resubmitRepairApply(HttpServletRequest request,
                                              @RequestBody RepairResubmitDTO resubmitDTO) {
        try {
            // 1. 登录校验
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户ID格式错误");
            }

            // 2. 校验参数
            if (resubmitDTO.getRepairOrderId() == null) {
                return Result.error("报修单号不能为空");
            }
            if (resubmitDTO.getTitle() == null || resubmitDTO.getTitle().isEmpty()) {
                return Result.error("报修标题不能为空");
            }

            // 3. 重新提交
            repairService.resubmitRepairApply(userId, resubmitDTO);
            return Result.success("报修申请重新提交成功，等待物业审核");
        } catch (RuntimeException e) {
            log.error("重新提交报修申请业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("重新提交报修申请系统异常", e);
            return Result.error("系统异常，重新提交失败");
        }
    }

    // ====================== 物业端接口 ======================

    /**
     * 物业查看报修单详情
     * GET /repair/admin/detail?repairOrderId=1
     */
    @GetMapping("/admin/detail")
    public Result<RepairOrderDetailVO> getAdminRepairDetail(@RequestParam Long repairOrderId,
                                                            HttpServletRequest request) {
        try {
            // 1. 物业人员登录校验
            String adminIdStr = request.getHeader("user_id");
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                return Result.unauthorized("物业人员未登录");
            }

            // 2. 校验参数
            if (repairOrderId == null) {
                return Result.error("报修单号不能为空");
            }

            // 3. 查询详情
            RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
            if (repairOrder == null) {
                return Result.error("报修单不存在");
            }

            // 4. 转换为 VO
            RepairOrderDetailVO detail = new RepairOrderDetailVO();
            detail.setId(repairOrder.getId());
            detail.setRepairOrderNo(repairOrder.getRepairOrderNo());
            detail.setTitle(repairOrder.getTitle());
            detail.setContent(repairOrder.getContent());
            detail.setAddress(repairOrder.getAddress());
            detail.setCreateTime(repairOrder.getCreateTime());
            detail.setStatus(repairOrder.getStatus());
            detail.setCompleteTime(repairOrder.getCompleteTime());

            // 查询报修类型名称
            RepairType repairType = repairTypeMapper.selectById(repairOrder.getRepairTypeId());
            detail.setRepairTypeName(repairType != null ? repairType.getTypeName() : "未知类型");

            // 填充状态描述
            detail.setStatusDesc(RepairStatusEnum.getDescByCode(repairOrder.getStatus()));

            // 如果有维修人员信息，查询并填充
            if (repairOrder.getHandlerId() != null) {
                detail.setHandlerId(repairOrder.getHandlerId());
                detail.setHandlerName(repairOrder.getHandlerName());
                detail.setHandlerPhone(repairOrder.getHandlerPhone());
            }

            // 评价信息
            if (repairOrder.getEvaluationScore() != null) {
                detail.setEvaluationScore(repairOrder.getEvaluationScore());
                detail.setEvaluationComment(repairOrder.getEvaluationComment());
                detail.setEvaluationTime(repairOrder.getEvaluationTime());
            }

            return Result.success("查询报修单详情成功", detail);
        } catch (RuntimeException e) {
            log.error("物业查看报修单详情业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("物业查看报修单详情系统异常", e);
            return Result.error("系统异常，查询报修单详情失败");
        }
    }

    /**
     * 物业审核报修单
     * POST /repair/admin/review（原/api/repair/admin/review）
     */
    @PostMapping("/admin/review")
    public Result<String> reviewRepairOrder(@RequestBody RepairReviewDTO reviewDTO,
                                            HttpServletRequest request) {
        try {
            // 1. 物业人员登录校验
            String adminIdStr = request.getHeader("user_id");
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                return Result.unauthorized("物业人员未登录");
            }

            // 2. 校验参数
            if (reviewDTO.getRepairOrderId() == null) {
                return Result.error("报修单号不能为空");
            }
            if (reviewDTO.getReviewResult() == null) {
                return Result.error("审核结果不能为空");
            }
            // 驳回时必须填写驳回原因
            if (!reviewDTO.getReviewResult() && (reviewDTO.getRejectReason() == null
                    || reviewDTO.getRejectReason().isEmpty())) {
                return Result.error("驳回报修单时，必须填写驳回原因");
            }

            // 3. 处理审核
            repairService.handleRepairReview(reviewDTO);
            String successMsg = reviewDTO.getReviewResult()
                    ? "报修单审核通过，已转入派单流程"
                    : "报修单审核驳回，已通知业主";
            return Result.success(successMsg);
        } catch (RuntimeException e) {
            log.error("审核报修单业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("审核报修单系统异常", e);
            return Result.error("系统异常，审核操作失败");
        }
    }

    /**
     * 物业派单
     * POST /repair/admin/assign（原/api/repair/admin/assign）
     */
    @PostMapping("/admin/assign")
    public Result<String> assignRepairOrder(@RequestBody RepairAssignDTO assignDTO,
                                            HttpServletRequest request) {
        try {
            // 1. 物业人员登录校验
            String adminIdStr = request.getHeader("user_id");
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                return Result.unauthorized("物业人员未登录");
            }

            // 2. 校验参数
            if (assignDTO.getRepairOrderId() == null) {
                return Result.error("报修单号不能为空");
            }
            if (assignDTO.getHandlerName() == null || assignDTO.getHandlerName().isEmpty()) {
                return Result.error("维修人员姓名不能为空");
            }
            if (assignDTO.getHandlerPhone() == null || assignDTO.getHandlerPhone().isEmpty()) {
                return Result.error("维修人员联系方式不能为空");
            }
            if (assignDTO.getExpectedVisitTime() == null || assignDTO.getExpectedVisitTime().isEmpty()) {
                return Result.error("预计上门时间不能为空");
            }

            // 3. 派单
            repairService.assignRepairOrder(assignDTO);
            return Result.success("派单成功，已通知维修人员");
        } catch (RuntimeException e) {
            log.error("派单业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("派单系统异常", e);
            return Result.error("系统异常，派单失败");
        }
    }

    /**
     * 标记维修完成（待验收）
     * POST /repair/admin/updateProgress（原/api/repair/admin/updateProgress）
     */
    @PostMapping("/admin/updateProgress")
    public Result<String> updateRepairProgress(@RequestParam Long repairOrderId,
                                               HttpServletRequest request) {
        try {
            // 1. 物业人员登录校验
            String adminIdStr = request.getHeader("user_id");
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                return Result.unauthorized("物业人员未登录");
            }

            // 2. 校验参数
            if (repairOrderId == null) {
                return Result.error("报修单号不能为空");
            }

            // 3. 更新进度
            repairService.updateRepairProgress(repairOrderId);
            return Result.success("已标记该报修单为待验收状态，已通知业主进行验收");
        } catch (RuntimeException e) {
            log.error("更新维修进度业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新维修进度系统异常", e);
            return Result.error("系统异常，更新进度操作失败");
        }
    }

    // ====================== 保留你最初的测试接口 ======================
    /**
     * post测试接口
     * POST /repair/test
     */
    @PostMapping("/test")
    public String test(@RequestBody String body) {
        //后端打印"post已请求"
        System.out.println("post已请求");
        return "post请求成功";
    }

    /**
     * get测试接口
     * GET /repair/list
     */
    @GetMapping("/list")
    public String get() {
        //后端打印"get已请求"
        System.out.println("get已请求");
        return "get请求成功";
    }

    /**
     * 业主取消报修申请
     * POST /repair/owner/cancel
     */
    @PostMapping("/owner/cancel")
    public Result<String> cancelRepairApply(HttpServletRequest request,
                                            @RequestBody RepairCancelDTO cancelDTO) {
        try {
            // 1. 登录校验
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户 ID 格式错误");
            }

            // 2. 校验参数
            if (cancelDTO.getRepairOrderId() == null) {
                return Result.error("报修单号不能为空");
            }

            // 3. 处理取消逻辑
            repairService.cancelRepairApply(userId, cancelDTO);
            return Result.success("取消报修申请成功");
        } catch (SecurityException e) {
            return Result.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("取消报修申请参数异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            log.error("取消报修申请业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("取消报修申请系统异常", e);
            return Result.error("系统异常，取消报修申请失败");
        }
    }

    /**
     * 获取所有报修类型列表（用于前端下拉选择）
     * GET /repair/type/list
     */
    @GetMapping("/type/list")
    public Result<List<RepairType>> getRepairTypeList() {
        try {
            List<RepairType> typeList = repairTypeMapper.selectList(null);
            return Result.success("查询报修类型列表成功", typeList);
        } catch (Exception e) {
            log.error("查询报修类型列表异常", e);
            return Result.error("查询报修类型列表失败");
        }
    }

    /**
     * 获取报修进度预测
     * GET /repair/progress/{repairOrderId}
     */
    @GetMapping("/progress/{repairOrderId}")
    public Result<Map<String, Object>> getRepairProgress(@PathVariable Long repairOrderId,
                                                         HttpServletRequest request) {
        try {
            // 1. 登录校验
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.unauthorized("用户未登录");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户ID格式错误");
            }

            // 2. 权限校验：验证报修单归属
            RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
            if (repairOrder == null) {
                return Result.error("报修单不存在");
            }

            Owner owner = ownerMapper.selectByUserId(userId);
            if (owner == null || !repairOrder.getOwnerId().equals(owner.getId())) {
                return Result.forbidden("无权限查看该报修单");
            }

            // 3. 获取进度预测
            Map<String, Object> prediction = repairService.predictRepairProgress(repairOrderId);

            return Result.success("查询进度预测成功", prediction);
        } catch (RuntimeException e) {
            log.error("查询报修进度预测业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询报修进度预测系统异常", e);
            return Result.error("系统异常，查询进度预测失败");
        }
    }

    /**
     * 物业端获取所有报修列表（带分页和筛选）
     * GET /repair/admin/list
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminRepairList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long repairTypeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            // 1. 物业人员登录校验
            String adminIdStr = request.getHeader("user_id");
            if (adminIdStr == null || adminIdStr.isEmpty()) {
                return Result.unauthorized("物业人员未登录");
            }

            Long adminId;
            try {
                adminId = Long.parseLong(adminIdStr);
            } catch (NumberFormatException e) {
                return Result.error("用户 ID 格式错误");
            }

            // 2. 查询所有报修列表（可添加筛选条件）
            Map<String, Object> resultMap = repairService.getAdminRepairListWithPage(status, repairTypeId, page, size);

            return Result.success("查询报修列表成功", resultMap);
        } catch (RuntimeException e) {
            log.error("查询报修列表业务异常", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询报修列表系统异常", e);
            return Result.error("系统异常，查询报修列表失败");
        }
    }
}