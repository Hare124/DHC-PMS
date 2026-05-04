package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.dto.AnnouncementPublishDTO;
import com.example.dhcpms.dto.AnnouncementQueryDTO;
import com.example.dhcpms.entity.Announcement;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.service.AnnouncementService;
import com.example.dhcpms.service.MsgPushService;
import com.example.dhcpms.service.UserService;
import com.example.dhcpms.vo.AnnouncementDetailVO;
import com.example.dhcpms.vo.AnnouncementListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 公告控制器（整合测试接口+业务接口，根路径：/announcement）
 */
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;
    // private final AnnouncementReadRecordService readRecordService;
    private final MsgPushService msgPushService;

    private static final String JWT_SECRET = "your-secret-key";

    // ======================== 测试接口（保留） ========================
    @PostMapping("/test")
    public String test(@RequestBody String body) {
        // 后端打印"post已请求"
        System.out.println("post已请求");
        return "post请求成功";
    }

    @GetMapping("/list")
    public String get() {
        // 后端打印"get已请求"
        System.out.println("get已请求");
        return "get请求成功";
    }

    // ======================== 业主端接口（路径：/announcement/owner/xxx） ========================
    @GetMapping("/owner/list")
    public Result<IPage<AnnouncementListVO>> getOwnerAnnouncementList(
            AnnouncementQueryDTO queryDTO, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isOwner(userId)) {
            return Result.fail("未登录/非业主身份，无访问权限");
        }

        Page<Announcement> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        // 1. 日期范围查询
        if (queryDTO.getStartTime() != null) {
            LocalDateTime startDateTime = queryDTO.getStartTime().atStartOfDay();
            wrapper.ge(Announcement::getPublishTime, startDateTime);
        }
        if (queryDTO.getEndTime() != null) {
            LocalDateTime endDateTime = queryDTO.getEndTime().atTime(23, 59, 59);
            wrapper.le(Announcement::getPublishTime, endDateTime);
        }

        // 2. 关键词查询
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Announcement::getTitle, queryDTO.getKeyword()).or().like(Announcement::getContent, queryDTO.getKeyword()));
        }

        // 3. 置顶状态查询
        if (queryDTO.getIsTop() != null) {
            wrapper.eq(Announcement::getIsTop, queryDTO.getIsTop());
        }

        // 4. 只查询未撤回的公告
        wrapper.eq(Announcement::getRecallStatus, 0)
                .orderByDesc(Announcement::getIsTop, Announcement::getPublishTime);

        IPage<Announcement> announcementPage = announcementService.page(page, wrapper);
        // List<Long> readIds = readRecordService.getReadAnnouncementIdsByOwnerUserId(userId);

        // 转换为 VO
        IPage<AnnouncementListVO> voPage = announcementPage.convert(announcement -> {
            AnnouncementListVO vo = new AnnouncementListVO();
            vo.setId(announcement.getId());
            vo.setTitle(announcement.getTitle());
            vo.setContent(announcement.getContent());
            vo.setPublishTime(announcement.getPublishTime());
            vo.setIsTop(announcement.getIsTop());
            vo.setReadCount(announcement.getReadCount());
            // 发布人姓名
            User publisher = userService.getById(announcement.getPublisherId());
            vo.setPublisherName(publisher != null ? "物业管理员 " + publisher.getName() : "未知管理员");
            // 未读标记
            // vo.setUnread(!readIds.contains(announcement.getId()));
            return vo;
        });
        return Result.success(voPage);
    }

    @GetMapping("/owner/detail/{announcementId}")
    public Result<AnnouncementDetailVO> getOwnerAnnouncementDetail(
            @PathVariable Long announcementId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isOwner(userId)) {
            return Result.fail("未登录/非业主身份，无访问权限");
        }

        // 查询公告（排除已撤回）
        Announcement announcement = announcementService.getOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getId, announcementId)
                .eq(Announcement::getRecallStatus, 0));
        if (announcement == null) {
            return Result.fail("公告不存在或已撤回");
        }

        // 阅读次数+1
        announcement.setReadCount(announcement.getReadCount() + 1);
        announcementService.updateById(announcement);
        // 标记已读
        // readRecordService.markAsRead(userId, announcementId);

        // 组装详情VO
        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        vo.setId(announcement.getId());
        vo.setTitle(announcement.getTitle());
        vo.setContent(announcement.getContent());
        vo.setPublishTime(announcement.getPublishTime());
        vo.setIsTop(announcement.getIsTop());
        vo.setReadCount(announcement.getReadCount());
        User publisher = userService.getById(announcement.getPublisherId());
        vo.setPublisherName(publisher != null ? "物业管理员 " + publisher.getName() : "未知管理员");
        return Result.success(vo);
    }

    // ======================== 物业端接口（路径：/announcement/manager/xxx） ========================
    @PostMapping("/manager/publish")
    public Result<String> publishAnnouncement(
            @RequestBody AnnouncementPublishDTO publishDTO, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无发布权限");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(publishDTO.getTitle());
        announcement.setContent(publishDTO.getContent());
        announcement.setPublisherId(userId);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setIsTop(publishDTO.getIsTop() == null ? 0 : publishDTO.getIsTop());
        announcement.setReadCount(0);
        announcement.setRecallStatus(0);

        if (announcementService.save(announcement)) {
            msgPushService.pushAnnouncementMsgToAllOwners(announcement.getId(), announcement.getTitle(), announcement.getIsTop());
            return Result.success("公告发布成功");
        } else {
            return Result.fail("公告发布失败");
        }
    }

    @PostMapping("/manager/schedule-publish")
    public Result<String> schedulePublishAnnouncement(
            @RequestBody AnnouncementPublishDTO publishDTO, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无操作权限");
        }

        if (publishDTO.getScheduledPublishTime() == null || publishDTO.getScheduledPublishTime().isBefore(LocalDateTime.now())) {
            return Result.fail("定时发布时间必须晚于当前时间");
        }

        announcementService.createScheduledPublishTask(publishDTO, userId);
        return Result.success("定时发布任务创建成功");
    }

    @PutMapping("/manager/set-top/{announcementId}/{isTop}")
    public Result<String> setAnnouncementTop(
            @PathVariable Long announcementId, @PathVariable Integer isTop, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无操作权限");
        }

        if (isTop != 0 && isTop != 1) {
            return Result.fail("置顶状态只能是0（取消）或1（置顶）");
        }

        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            return Result.fail("公告不存在");
        }

        announcement.setIsTop(isTop);
        if (announcementService.updateById(announcement)) {
            msgPushService.pushAnnouncementTopStatusUpdateMsg(announcementId, isTop);
            return Result.success(isTop == 1 ? "置顶成功" : "取消置顶成功");
        } else {
            return Result.fail("操作失败");
        }
    }

    @PutMapping("/manager/update/{announcementId}")
    public Result<String> updateAnnouncement(
            @PathVariable Long announcementId, @RequestBody AnnouncementPublishDTO publishDTO, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无操作权限");
        }

        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            return Result.fail("公告不存在");
        }

        announcement.setTitle(publishDTO.getTitle());
        announcement.setContent(publishDTO.getContent());
        announcement.setIsTop(publishDTO.getIsTop() == null ? announcement.getIsTop() : publishDTO.getIsTop());
        if (publishDTO.isResetReadCount()) {
            announcement.setReadCount(0);
        }

        if (announcementService.updateById(announcement)) {
            msgPushService.pushAnnouncementUpdateMsgToAllOwners(announcementId);
            return Result.success("公告修改成功");
        } else {
            return Result.fail("公告修改失败");
        }
    }

    @PutMapping("/manager/recall/{announcementId}")
    public Result<String> recallAnnouncement(
            @PathVariable Long announcementId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无操作权限");
        }

        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            return Result.fail("公告不存在");
        }

        announcement.setRecallStatus(1);
        announcement.setRecallTime(LocalDateTime.now());
        if (announcementService.updateById(announcement)) {
            msgPushService.pushAnnouncementRecallMsgToAllOwners(announcementId);
            return Result.success("公告撤回成功");
        } else {
            return Result.fail("公告撤回失败");
        }
    }

    @GetMapping("/manager/list")
    public Result<IPage<Announcement>> getManagerAnnouncementList(
            AnnouncementQueryDTO queryDTO, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无访问权限");
        }

        Page<Announcement> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        // 1. 日期范围查询
        if (queryDTO.getStartTime() != null) {
            LocalDateTime startDateTime = queryDTO.getStartTime().atStartOfDay();
            wrapper.ge(Announcement::getPublishTime, startDateTime);
        }
        if (queryDTO.getEndTime() != null) {
            LocalDateTime endDateTime = queryDTO.getEndTime().atTime(23, 59, 59);
            wrapper.le(Announcement::getPublishTime, endDateTime);
        }

        // 2. 关键词查询
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Announcement::getTitle, queryDTO.getKeyword()).or().like(Announcement::getContent, queryDTO.getKeyword()));
        }

        // 3. 置顶状态查询
        if (queryDTO.getIsTop() != null) {
            wrapper.eq(Announcement::getIsTop, queryDTO.getIsTop());
        }

        // 4. 撤回状态查询
        // 只有当 recallStatus 有值时才筛选（0 或 1），为空时不筛选（显示全部）
        if (queryDTO.getRecallStatus() != null) {
            wrapper.eq(Announcement::getRecallStatus, queryDTO.getRecallStatus());
        }
        // Deleted:else {
        // Deleted:// 默认查询未撤回的公告
        // Deleted:wrapper.eq(Announcement::getRecallStatus, 0);
        // Deleted:}

        // 5. 排序：先按置顶降序，再按发布时间降序
        wrapper.orderByDesc(Announcement::getIsTop, Announcement::getPublishTime);

        IPage<Announcement> pageResult = announcementService.page(page, wrapper);
        return Result.success(pageResult);
    }

    @GetMapping("/manager/detail/{announcementId}")
    public Result<AnnouncementDetailVO> getManagerAnnouncementDetail(
            @PathVariable Long announcementId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null || !userService.isManager(userId)) {
            return Result.fail("未登录/非物业身份，无访问权限");
        }

        // 查询公告
        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            return Result.fail("公告不存在");
        }

        // 组装详情 VO
        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        vo.setId(announcement.getId());
        vo.setTitle(announcement.getTitle());
        vo.setContent(announcement.getContent());
        vo.setPublishTime(announcement.getPublishTime());
        vo.setIsTop(announcement.getIsTop());
        vo.setReadCount(announcement.getReadCount());
        User publisher = userService.getById(announcement.getPublisherId());
        vo.setPublisherName(publisher != null ? "物业管理员 " + publisher.getName() : "未知管理员");
        return Result.success(vo);
    }

    // ======================== 私有方法 ========================
    /**
     * 从请求头获取用户 ID（与 RepairController 保持一致）
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            // 直接从请求头获取 user_id（前端 localStorage 存储的）
            String userIdStr = request.getHeader("user_id");

            if (userIdStr == null || userIdStr.isEmpty()) {
                System.out.println("=== [用户 ID 调试] user_id 为空");
                return null;
            }

            Long userId = Long.parseLong(userIdStr);
            System.out.println("=== [用户 ID 调试] 解析出的用户 ID: " + userId);

            // 校验用户是否存在
            User user = userService.getById(userId);
            System.out.println("=== [用户 ID 调试] 查询到的用户：" + (user != null ? "ID=" + user.getId() + ", 角色=" + user.getRole() : "null"));

            return user != null ? user.getId() : null;
        } catch (NumberFormatException e) {
            System.out.println("=== [用户 ID 调试] 用户 ID 格式错误：" + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("=== [用户 ID 调试] 用户 ID 解析异常：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通用返回结果类
     */
    public static class Result<T> {
        private Integer code;
        private String msg;
        private T data;

        public static <T> Result<T> success(T data) {
            Result<T> result = new Result<>();
            result.setCode(200);
            result.setMsg("操作成功");
            result.setData(data);
            return result;
        }

        public static <T> Result<T> fail(String msg) {
            Result<T> result = new Result<>();
            result.setCode(500);
            result.setMsg(msg);
            result.setData(null);
            return result;
        }

        // Getter & Setter
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}