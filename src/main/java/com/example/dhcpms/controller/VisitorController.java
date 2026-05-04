package com.example.dhcpms.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.dto.VisitorCheckoutDTO;
import com.example.dhcpms.dto.VisitorInviteDTO;
import com.example.dhcpms.dto.VisitorInviteQueryDTO;
import com.example.dhcpms.dto.VisitorVerifyDTO;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.VisitorInvite;
import com.example.dhcpms.entity.VisitorRecord;
import com.example.dhcpms.service.OwnerService;
import com.example.dhcpms.service.UserService;
import com.example.dhcpms.service.VisitorInviteService;
import com.example.dhcpms.service.VisitorRecordService;
import com.example.dhcpms.util.EncryptUtil;
import com.example.dhcpms.util.QRCodeGenerator;
import com.example.dhcpms.vo.InviteRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 访客控制核心接口（适配Spring Boot 2.7.18 + Java 17 + 指定POM依赖）
 * 包含：业主端业务接口、物业端业务接口、测试接口
 */
@RestController
@RequestMapping("/visitor")
@Slf4j
@Validated
public class VisitorController {

    @Autowired
    private VisitorInviteService visitorInviteService;
    @Autowired
    private VisitorRecordService visitorRecordService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private UserService userService;
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    @Autowired
    private EncryptUtil encryptUtil;

    // ====================== 新增：测试接口 ======================
    /**
     * POST测试接口
     */
    @PostMapping("/test")
    public String test(@RequestBody String body) {
        // 后端打印"post已请求"
        System.out.println("post已请求，请求体内容：" + body);
        return "post请求成功";
    }

    /**
     * GET测试接口
     */
    @GetMapping("/list")
    public String get() {
        // 后端打印"get已请求"
        System.out.println("get已请求");
        return "get请求成功";
    }

    // ====================== 原有：业主端接口 ======================

    /**
     * 业主创建访客邀请并生成二维码
     */
    @PostMapping("/owner/create-invite")
    public Result<?> createInvite(
            @RequestHeader("user_id") Long userId,
            @Valid @RequestBody VisitorInviteDTO inviteDTO) {
        try {
            log.info("创建访客邀请，userId: {}", userId);

            // 1. 查询业主信息（自动填充房号）
            Owner owner = ownerService.getByUserId(userId);
            if (owner == null) {
                log.warn("未查询到业主信息，userId: {}", userId);
                return Result.error("未查询到业主信息，请先绑定房产");
            }

            // 2. 计算有效期（默认 2 小时）
            Integer validHours = inviteDTO.getValidHours() == null ? 2 : inviteDTO.getValidHours();
            LocalDateTime now = LocalDateTimeUtil.now();
            LocalDateTime expireTime = now.plusHours(validHours);

            // 3. 构建邀请记录
            VisitorInvite invite = new VisitorInvite();
            invite.setOwnerId(owner.getId());
            invite.setVisitorName(inviteDTO.getVisitorName());
            invite.setVisitorPhone(inviteDTO.getVisitorPhone());
            invite.setVisitTime(inviteDTO.getVisitTime());
            invite.setCreateTime(now);
            invite.setExpireTime(expireTime);
            invite.setStatus(0); // 待核销
            invite.setQrcode(""); // 先设置空值

            // 4. 保存邀请记录
            boolean saveSuccess = visitorInviteService.save(invite);
            if (!saveSuccess) {
                log.error("保存邀请记录失败，userId: {}", userId);
                return Result.error("创建邀请失败，请重试");
            }

            // 5. 生成动态加密二维码内容
            String qrCodeContent = encryptUtil.generateQRCodeContent(
                    invite.getId(),
                    inviteDTO.getVisitorName(),
                    inviteDTO.getVisitorPhone(),
                    owner.getId(),
                    validHours
            );
            invite.setQrcode(qrCodeContent);
            boolean updateSuccess = visitorInviteService.updateById(invite);
            if (!updateSuccess) {
                log.error("更新二维码失败，inviteId: {}", invite.getId());
                return Result.error("生成二维码失败，请重试");
            }

            // 6. 生成二维码 Base64
            String qrCodeBase64 = qrCodeGenerator.generateBase64(qrCodeContent);

            // 7. 组装返回数据
            InviteRecordVO resultVO = new InviteRecordVO();
            BeanUtils.copyProperties(invite, resultVO);
            resultVO.setRoomNo(StrUtil.format("{}-{}", owner.getBuildingNo(), owner.getRoomNo()));
            resultVO.setStatusDesc("待核销");
            resultVO.setQrCodeValid(true);

            HashMap<String, Object> data = new HashMap<>();
            data.put("inviteInfo", resultVO);
            data.put("qrCodeBase64", qrCodeBase64);
            data.put("expireTime", expireTime);
            data.put("qrCodeContent", qrCodeContent); // 返回加密内容供前端调试

            log.info("创建访客邀请成功，inviteId: {}, ownerId: {}", invite.getId(), owner.getId());
            return Result.success(StrUtil.format("二维码生成成功，有效期至{}", expireTime), data);
        } catch (Exception e) {
            log.error("创建访客邀请失败", e);
            return Result.error("系统异常，创建邀请失败");
        }
    }

    /**
     * 获取单个邀请的二维码
     */
    @GetMapping("/owner/get-qrcode")
    public Result<?> getInviteQRCode(
            @RequestHeader("user_id") Long userId,
            @RequestParam Long inviteId) {
        try {
            // 1. 权限校验
            Owner owner = ownerService.getByUserId(userId);
            VisitorInvite invite = visitorInviteService.getById(inviteId);

            if (invite == null) {
                return Result.error("邀请记录不存在");
            }
            if (!invite.getOwnerId().equals(owner.getId())) {
                return Result.forbidden("无权查看该邀请");
            }
            if (invite.getStatus() != 0) {
                return Result.error("邀请已" + (invite.getStatus() == 1 ? "核销" : "过期"));
            }

            // 2. 检查是否过期
            if (LocalDateTime.now().isAfter(invite.getExpireTime())) {
                return Result.error("二维码已过期");
            }

            // 3. 生成二维码
            String qrCodeContent = invite.getQrcode();
            String qrCodeBase64 = qrCodeGenerator.generateBase64(qrCodeContent);

            Map<String, Object> data = new HashMap<>();
            data.put("qrCodeBase64", qrCodeBase64);
            data.put("expireTime", invite.getExpireTime());
            data.put("valid", true);

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取二维码失败", e);
            return Result.error("系统异常");
        }
    }

    /**
     * 业主查询我的邀请记录
     */
    @GetMapping("/owner/invite-records")
    public Result<?> getOwnerInviteRecords(
            @RequestHeader("user_id") Long userId) {
        try {
            log.info("查询业主邀请记录，userId: {}", userId);

            // 1. 获取业主 ID
            Owner owner = ownerService.getByUserId(userId);
            if (owner == null) {
                log.warn("未查询到业主信息，userId: {}", userId);
                return Result.error("未查询到业主信息");
            }

            // 2. 查询该业主的所有邀请记录（按创建时间倒序）
            LambdaQueryWrapper<VisitorInvite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VisitorInvite::getOwnerId, owner.getId())
                    .orderByDesc(VisitorInvite::getCreateTime);
            List<VisitorInvite> inviteList = visitorInviteService.list(wrapper);
            log.info("查询到邀请记录数量：{}", inviteList.size());

            // 3. 转换为 VO 并补充状态描述
            LocalDateTime now = LocalDateTimeUtil.now();
            List<InviteRecordVO> voList = inviteList.stream().map(invite -> {
                InviteRecordVO vo = new InviteRecordVO();
                BeanUtils.copyProperties(invite, vo);
                vo.setRoomNo(StrUtil.format("{}-{}", owner.getBuildingNo(), owner.getRoomNo()));

                // 状态转换
                switch (invite.getStatus()) {
                    case 0:
                        boolean valid = now.isBefore(invite.getExpireTime());
                        vo.setStatusDesc(valid ? "待核销" : "已过期");
                        vo.setQrCodeValid(valid);
                        break;
                    case 1:
                        vo.setStatusDesc("已核销");
                        vo.setQrCodeValid(false);
                        // 补充核销时间
                        VisitorRecord record = visitorRecordService.getByInviteId(invite.getId());
                        if (record != null) {
                            vo.setCheckinTime(record.getCheckinTime());
                        }
                        break;
                    case 2:
                        vo.setStatusDesc("已过期");
                        vo.setQrCodeValid(false);
                        break;
                }
                return vo;
            }).collect(Collectors.toList());

            log.info("返回邀请记录 VO 数量：{}", voList.size());
            // 成功返回（默认消息 + 数据）
            return Result.success(voList);
        } catch (Exception e) {
            log.error("查询业主邀请记录失败", e);
            return Result.error("系统异常，查询邀请记录失败");
        }
    }

    /**
     * 业主手动取消邀请
     */
    @PostMapping("/owner/cancel-invite")
    public Result<?> cancelInvite(
            @RequestHeader("user_id") Long userId,
            @RequestParam Long inviteId) {
        try {
            log.info("取消访客邀请，userId: {}, inviteId: {}", userId, inviteId);

            // 1. 权限校验
            Owner owner = ownerService.getByUserId(userId);
            VisitorInvite invite = visitorInviteService.getById(inviteId);

            if (invite == null) {
                return Result.error("邀请记录不存在");
            }
            if (!invite.getOwnerId().equals(owner.getId())) {
                // 权限不足返回403
                return Result.forbidden("无权操作该邀请记录");
            }

            // 2. 更新状态为已过期
            invite.setStatus(2);
            boolean updateSuccess = visitorInviteService.updateById(invite);
            if (updateSuccess) {
                // 成功返回（自定义消息）
                return Result.success("邀请已取消，二维码失效");
            } else {
                return Result.error("取消失败，请重试");
            }
        } catch (Exception e) {
            log.error("取消访客邀请失败", e);
            return Result.error("系统异常，取消邀请失败");
        }
    }

    // ====================== 原有：物业端接口 ======================

    /**
     * 物业查询访客邀请记录
     */
    @PostMapping("/admin/query-invites")
    public Result<?> queryInvites(@RequestBody VisitorInviteQueryDTO queryDTO,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("查询邀请记录，status: {}, keyword: {}, page: {}, size: {}",
                    queryDTO.getStatus(), queryDTO.getKeyword(), page, size);

            // 1. 先查询符合条件的邀请记录 ID
            LambdaQueryWrapper<VisitorInvite> wrapper = new LambdaQueryWrapper<>();

            // 状态筛选（仅当 status 不为空时才筛选）
            if (queryDTO.getStatus() != null) {
                wrapper.eq(VisitorInvite::getStatus, queryDTO.getStatus());
            }

            // 时间范围筛选（基于预约时间）
            if (StrUtil.isNotBlank(queryDTO.getStartTime())) {
                wrapper.ge(VisitorInvite::getVisitTime, queryDTO.getStartTime());
            }
            if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
                wrapper.le(VisitorInvite::getVisitTime, queryDTO.getEndTime());
            }

            // 按创建时间倒序
            wrapper.orderByDesc(VisitorInvite::getCreateTime);

            // 分页查询
            Page<VisitorInvite> pageParam = new Page<>(page, size);
            Page<VisitorInvite> invitePage = visitorInviteService.page(pageParam, wrapper);
            log.info("查询到邀请记录数量：{}", invitePage.getRecords().size());

            // 2. 转换为 VO 并关联业主信息，同时进行关键词模糊查询
            List<com.example.dhcpms.vo.AdminInviteVO> voList = new java.util.ArrayList<>();
            for (VisitorInvite invite : invitePage.getRecords()) {
                // 关联业主信息
                Owner owner = ownerService.getById(invite.getOwnerId());

                // 关键词模糊查询：访客姓名、访客手机号、邀请人姓名、房号
                if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
                    boolean match = false;

                    // 访客姓名
                    if (StrUtil.contains(invite.getVisitorName(), queryDTO.getKeyword())) {
                        match = true;
                    }
                    // 访客手机号
                    if (StrUtil.contains(invite.getVisitorPhone(), queryDTO.getKeyword())) {
                        match = true;
                    }
                    // 邀请人姓名（业主姓名）
                    if (owner != null && StrUtil.contains(userService.getUserNameById(owner.getUserId()), queryDTO.getKeyword())) {
                        match = true;
                    }
                    // 房号（楼栋号 + 房间号）
                    if (owner != null && (StrUtil.contains(owner.getBuildingNo(), queryDTO.getKeyword())
                            || StrUtil.contains(owner.getRoomNo(), queryDTO.getKeyword()))) {
                        match = true;
                    }

                    // 如果不匹配，跳过该记录
                    if (!match) {
                        continue;
                    }
                }

                // 构建 VO
                com.example.dhcpms.vo.AdminInviteVO vo = new com.example.dhcpms.vo.AdminInviteVO();
                BeanUtils.copyProperties(invite, vo);
                if (owner != null) {
                    vo.setOwnerRoomNo(StrUtil.format("{}-{}", owner.getBuildingNo(), owner.getRoomNo()));
                    vo.setOwnerName(userService.getUserNameById(owner.getUserId()));
                }
                voList.add(vo);
            }

            // 3. 返回分页结果（需要手动计算总数）
            Map<String, Object> resultMap = Map.of(
                    "records", voList,
                    "total", voList.size(),
                    "size", voList.size(),
                    "current", page,
                    "pages", (voList.size() + size - 1) / size
            );

            // 成功返回（默认消息 + 数据）
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("物业查询邀请记录失败", e);
            return Result.error("系统异常，查询邀请记录失败");
        }
    }

    /**
     * 物业核销访客邀请（自动/手动）
     */
    @PostMapping("/admin/verify-invite")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> verifyInvite(@Valid @RequestBody VisitorVerifyDTO verifyDTO) {
        try {
            LocalDateTime now = LocalDateTimeUtil.now();

            if (StrUtil.isNotBlank(verifyDTO.getEncryptInviteId())) {
                // ==================== 扫码核销（使用动态加密验证） ====================
                log.info("扫码核销，二维码内容: {}", verifyDTO.getEncryptInviteId());

                // 1. 解密并验证二维码
                Map<String, Object> verifyResult = visitorInviteService.verifyQRCode(verifyDTO.getEncryptInviteId());

                if (!Boolean.TRUE.equals(verifyResult.get("success"))) {
                    return Result.error((String) verifyResult.get("message"));
                }

                // 2. 获取邀请ID
                Long inviteId = Long.valueOf(verifyResult.get("inviteId").toString());
                VisitorInvite invite = visitorInviteService.getById(inviteId);

                if (invite == null) {
                    return Result.error("邀请记录不存在");
                }

                // 3. 更新邀请状态为已核销
                invite.setStatus(1);
                visitorInviteService.updateById(invite);

                // 4. 新增访客通行记录
                VisitorRecord record = new VisitorRecord();
                record.setInviteId(invite.getId());
                record.setCheckinTime(now);
                record.setCheckerId(verifyDTO.getCheckerId());
                visitorRecordService.save(record);

                log.info("扫码核销成功，inviteId: {}, visitor: {}, 核销人ID: {}",
                        invite.getId(), invite.getVisitorName(), verifyDTO.getCheckerId());
                return Result.success("核销成功，访客准予入园");

            } else if (StrUtil.isNotBlank(verifyDTO.getVisitorPhone())) {
                // ==================== 手动核销 ====================
                log.info("手动核销，手机号: {}", verifyDTO.getVisitorPhone());

                VisitorInvite invite = visitorInviteService.getValidInviteByPhone(verifyDTO.getVisitorPhone());

                if (invite == null) {
                    return Result.error("未查询到有效邀请记录");
                }

                if (invite.getStatus() != 0) {
                    return Result.error("邀请已核销/过期，无法操作");
                }

                if (now.isAfter(invite.getExpireTime())) {
                    invite.setStatus(2);
                    visitorInviteService.updateById(invite);
                    return Result.error("邀请已过期，核销失败");
                }

                // 更新状态并创建通行记录
                invite.setStatus(1);
                visitorInviteService.updateById(invite);

                VisitorRecord record = new VisitorRecord();
                record.setInviteId(invite.getId());
                record.setCheckinTime(now);
                record.setCheckerId(verifyDTO.getCheckerId());
                visitorRecordService.save(record);

                log.info("手动核销成功，inviteId: {}, visitor: {}", invite.getId(), invite.getVisitorName());
                return Result.success("核销成功，访客准予入园");
            }

            return Result.error("请提供二维码或手机号");

        } catch (Exception e) {
            log.error("物业核销邀请失败", e);
            return Result.error("系统异常，核销邀请失败");
        }
    }

    /**
     * 物业登记访客离园
     */
    @PostMapping("/admin/checkout")
    public Result<?> checkout(@Valid @RequestBody VisitorCheckoutDTO checkoutDTO) {
        try {
            log.info("离园登记请求，inviteId: {}, visitorPhone: {}, checkerId: {}",
                    checkoutDTO.getInviteId(), checkoutDTO.getVisitorPhone(), checkoutDTO.getCheckerId());

            // 1. 查询入园记录
            VisitorRecord record = null;
            if (checkoutDTO.getInviteId() != null) {
                record = visitorRecordService.getByInviteId(checkoutDTO.getInviteId());
                log.info("按邀请 ID 查询，inviteId: {}, 查询结果：{}", checkoutDTO.getInviteId(), record != null ? "找到" : "未找到");
            } else if (StrUtil.isNotBlank(checkoutDTO.getVisitorPhone())) {
                // 按手机号查询邀请 ID（不限状态，取最新一条）
                LambdaQueryWrapper<VisitorInvite> inviteWrapper = new LambdaQueryWrapper<>();
                inviteWrapper.eq(VisitorInvite::getVisitorPhone, checkoutDTO.getVisitorPhone())
                        .orderByDesc(VisitorInvite::getCreateTime)
                        .last("LIMIT 1");
                VisitorInvite invite = visitorInviteService.getOne(inviteWrapper);
                log.info("按手机号查询邀请，visitorPhone: {}, 查询结果：{}", checkoutDTO.getVisitorPhone(), invite != null ? "找到 (inviteId=" + invite.getId() + ")" : "未找到");

                if (invite != null) {
                    record = visitorRecordService.getByInviteId(invite.getId());
                    log.info("按邀请 ID 查询记录，inviteId: {}, 查询结果：{}", invite.getId(), record != null ? "找到" : "未找到");
                }
            }

            if (record == null) {
                log.warn("未查询到访客入园记录，inviteId: {}, visitorPhone: {}",
                        checkoutDTO.getInviteId(), checkoutDTO.getVisitorPhone());
                return Result.error("未查询到访客入园记录");
            }
            if (record.getCheckoutTime() != null) {
                log.info("访客已登记离园，inviteId: {}", checkoutDTO.getInviteId());
                return Result.error("访客已登记离园");
            }

            // 2. 更新离园时间
            record.setCheckoutTime(LocalDateTimeUtil.now());
            visitorRecordService.updateById(record);

            log.info("离园登记成功，inviteId: {}", checkoutDTO.getInviteId());
            // 成功返回（自定义消息）
            return Result.success("离园登记成功");
        } catch (Exception e) {
            log.error("物业登记访客离园失败", e);
            return Result.error("系统异常，登记离园失败");
        }
    }

    /**
     * 物业查询访客通行记录
     */
    @GetMapping("/admin/record-list")
    public Result<?> getRecordList(
            @RequestParam(required = false) String date) {
        try {
            List<VisitorRecord> recordList;

            // 如果传入了日期，查询指定日期；否则查询所有记录
            if (StrUtil.isNotBlank(date)) {
                LocalDate queryDate = LocalDate.parse(date);
                LocalDateTime start = LocalDateTime.of(queryDate, java.time.LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(queryDate, java.time.LocalTime.MAX);

                LambdaQueryWrapper<VisitorRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.between(VisitorRecord::getCheckinTime, start, end)
                        .orderByDesc(VisitorRecord::getCheckinTime);
                recordList = visitorRecordService.list(wrapper);
            } else {
                // 查询所有记录，按入园时间倒序
                LambdaQueryWrapper<VisitorRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.orderByDesc(VisitorRecord::getCheckinTime);
                recordList = visitorRecordService.list(wrapper);
            }

            // 2. 关联邀请&业主信息
            List<com.example.dhcpms.vo.VisitorRecordVO> voList = recordList.stream().map(record -> {
                com.example.dhcpms.vo.VisitorRecordVO vo = new com.example.dhcpms.vo.VisitorRecordVO();
                BeanUtils.copyProperties(record, vo);

                // 关联邀请信息
                VisitorInvite invite = visitorInviteService.getById(record.getInviteId());
                if (invite != null) {
                    vo.setVisitorName(invite.getVisitorName());
                    vo.setVisitorPhone(invite.getVisitorPhone());
                    vo.setVisitTime(invite.getVisitTime()); // 预约时间
                    // 关联业主房号
                    Owner owner = ownerService.getById(invite.getOwnerId());
                    if (owner != null) {
                        vo.setOwnerRoomNo(StrUtil.format("{}-{}", owner.getBuildingNo(), owner.getRoomNo()));
                    }
                }

                // 核销人员姓名
                vo.setCheckerName(userService.getUserNameById(record.getCheckerId()));

                // 通行状态
                vo.setPassStatus(record.getCheckoutTime() != null ? "已离园" : "已入园");

                return vo;
            }).collect(Collectors.toList());



            // 成功返回（默认消息 + 数据）
            return Result.success(voList);
        } catch (Exception e) {
            log.error("物业查询通行记录失败", e);
            return Result.error("系统异常，查询通行记录失败");
        }
    }

    /**
     * 定时任务：自动失效过期邀请（实际使用@Scheduled注解）
     */
    @PostMapping("/admin/auto-expire")
    public Result<?> autoExpireInvites() {
        try {
            LocalDateTime now = LocalDateTimeUtil.now();
            int updateCount = visitorInviteService.updateExpiredInvites(now);
            log.info("自动失效过期邀请记录数：{}", updateCount);
            // 成功返回（自定义消息+数据）
            return Result.success("自动失效完成", updateCount);
        } catch (Exception e) {
            log.error("自动失效过期邀请失败", e);
            return Result.error("系统异常，自动失效邀请失败");
        }
    }
}