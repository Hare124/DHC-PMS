package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.dto.SuggestionReplyDTO;
import com.example.dhcpms.dto.SuggestionStatusDTO;
import com.example.dhcpms.dto.SuggestionSubmitDTO;
import com.example.dhcpms.entity.Suggestion;
import com.example.dhcpms.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 意见反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/suggestion")
public class SuggestionController {

    // 上传文件存储目录（从配置文件读取，默认为项目根目录下的 uploads）
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    // 允许的文件扩展名
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private SuggestionService suggestionService;

    // ====================== 业主端接口 ======================

    /**
     * 提交意见反馈
     */
    @PostMapping("/owner/submit")
    public Result<Map<String, Object>> submitSuggestion(
        @RequestBody @Validated SuggestionSubmitDTO dto,
        HttpServletRequest request) {
        try {
            log.info("收到业主意见反馈提交请求：{}", dto);

            // 从请求头获取登录用户信息（实际项目中从 Token 解析）
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.error("用户未登录");
            }

            Long userId = Long.parseLong(userIdStr);
            dto.setUserId(userId);

            // TODO: 根据 userId 查询 ownerId
            // 这里假设 ownerId 等于 userId，实际需要根据 owner 表查询
            dto.setOwnerId(userId);

            // 提交意见反馈
            Long suggestionId = suggestionService.submitSuggestion(dto);

            Map<String, Object> result = new HashMap<>();
            result.put("suggestionId", suggestionId);

            return Result.success("意见反馈提交成功", result);
        } catch (Exception e) {
            log.error("提交意见反馈失败", e);
            return Result.error("提交意见反馈失败：" + e.getMessage());
        }
    }

    /**
     * 获取业主意见反馈列表
     */
    @GetMapping("/owner/list")
    public Result<List<Suggestion>> getOwnerSuggestionList(HttpServletRequest request) {
        try {
            // 从请求头获取登录用户信息
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.error("用户未登录");
            }

            Long userId = Long.parseLong(userIdStr);
            List<Suggestion> list = suggestionService.getOwnerSuggestionList(userId);

            return Result.success(list);
        } catch (Exception e) {
            log.error("查询意见反馈列表失败", e);
            return Result.error("查询意见反馈列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取意见反馈详情
     */
    @GetMapping("/owner/detail")
    public Result<Suggestion> getSuggestionDetail(@RequestParam Long id) {
        try {
            log.info("查询意见反馈详情，id: {}", id);

            Suggestion suggestion = suggestionService.getSuggestionDetail(id);
            return Result.success(suggestion);
        } catch (Exception e) {
            log.error("查询意见反馈详情失败", e);
            return Result.error("查询意见反馈详情失败：" + e.getMessage());
        }
    }
    // ====================== 物业端接口 ======================

    /**
     * 获取意见反馈列表（物业端）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getSuggestionList(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String status) {
        try {
            log.info("物业端查询意见反馈列表，pageNum: {}, pageSize: {}, type: {}, status: {}",
                pageNum, pageSize, type, status);

            Page<Suggestion> page = new Page<>(pageNum, pageSize);
            com.baomidou.mybatisplus.core.metadata.IPage<Suggestion> result =
                suggestionService.getSuggestionList(page, type, status);

            Map<String, Object> response = new HashMap<>();
            response.put("list", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pageNum", result.getCurrent());
            response.put("pageSize", result.getSize());
            response.put("pages", result.getPages());

            return Result.success(response);
        } catch (Exception e) {
            log.error("查询意见反馈列表失败", e);
            return Result.error("查询意见反馈列表失败：" + e.getMessage());
        }
    }

    /**
     * 回复意见反馈
     */
    @PostMapping("/admin/reply")
    public Result<?> replySuggestion(
        @RequestBody @Validated SuggestionReplyDTO dto,
        HttpServletRequest request) {
        try {
            log.info("收到意见反馈回复请求：{}", dto);

            // 从请求头获取登录用户信息
            String userIdStr = request.getHeader("user_id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.error("用户未登录");
            }

            Long userId = Long.parseLong(userIdStr);
            dto.setReplyUserId(userId);

            suggestionService.replySuggestion(dto);

            return Result.success("回复成功");
        } catch (Exception e) {
            log.error("回复意见反馈失败", e);
            return Result.error("回复意见反馈失败：" + e.getMessage());
        }
    }

    /**
     * 更新意见反馈状态
     */
    @PostMapping("/admin/updateStatus")
    public Result<?> updateSuggestionStatus(
        @RequestBody @Validated SuggestionStatusDTO dto) {
        try {
            log.info("收到意见反馈状态更新请求：{}", dto);

            suggestionService.updateSuggestionStatus(dto);

            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新意见反馈状态失败", e);
            return Result.error("更新意见反馈状态失败：" + e.getMessage());
        }
    }
    // ====================== 新增：测试接口 ======================
    /**
     * POST测试接口
     */
    @PostMapping("/test01")
    public String test(@RequestBody String body) {
        // 后端打印"post已请求"
        System.out.println("post已请求，请求体内容：" + body);
        return "post请求成功";
    }

    /**
     * GET测试接口
     */
    @GetMapping("/test02")
    public String get() {
        // 后端打印"get已请求"
        System.out.println("get已请求");
        return "get请求成功";
    }
}
