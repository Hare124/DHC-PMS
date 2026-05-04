package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.entity.OwnerMessage;
import com.example.dhcpms.service.OwnerMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 群聊消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/message/owner")
public class OwnerMessageController {

    @Autowired
    private OwnerMessageService ownerMessageService;

    /**
     * 获取群聊消息列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getMessageList(
            @RequestParam(defaultValue = "鼎湖社区业主群") String chatRoomName,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String senderName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        try {
            log.info("查询群聊消息列表，chatRoomName: {}, messageType: {}, senderName: {}, pageNum: {}, pageSize: {}",
                    chatRoomName, messageType, senderName, pageNum, pageSize);

            Page<OwnerMessage> page = new Page<>(pageNum, pageSize);

            // 使用多条件筛选方法
            Page<OwnerMessage> result = ownerMessageService.getMessageListWithFilter(
                    chatRoomName, messageType, senderName, page);

            Map<String, Object> response = new HashMap<>();
            response.put("list", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pageNum", result.getCurrent());
            response.put("pageSize", result.getSize());
            response.put("pages", result.getPages());

            return Result.success(response);
        } catch (Exception e) {
            log.error("查询群聊消息列表失败", e);
            return Result.error("查询群聊消息列表失败：" + e.getMessage());
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(
            @RequestBody OwnerMessage message,
            HttpServletRequest request) {
        try {
            log.info("收到发送消息请求：{}", message);

            // 从请求头获取登录用户信息（实际项目中从 Token 解析）
            String userIdStr = request.getHeader("user_id");
            Long userId = null;

            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdStr);
                    message.setSenderId(userId);
                    log.info("从请求头获取用户 ID: {}", userId);
                } catch (NumberFormatException e) {
                    log.warn("用户 ID 格式错误：{}", userIdStr);
                }
            }

            // 如果前端传递了 senderName，优先使用；否则自动查询
            if (message.getSenderName() == null || message.getSenderName().isEmpty()) {
                log.info("前端未传递 senderName，自动查询用户名");
                // 使用新方法，自动填充用户名
                Long messageId = ownerMessageService.sendMessageWithUsername(message, userId);

                Map<String, Object> response = new HashMap<>();
                response.put("messageId", messageId);

                return Result.success("发送成功", response);
            } else {
                log.info("使用前端传递的 senderName: {}", message.getSenderName());
                // 使用原有方法
                Long messageId = ownerMessageService.sendMessage(message);

                Map<String, Object> response = new HashMap<>();
                response.put("messageId", messageId);

                return Result.success("发送成功", response);
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 撤回消息
     */
    @PostMapping("/withdraw")
    public Result<?> withdrawMessage(@RequestParam Long id) {
        try {
            log.info("收到撤回消息请求，id: {}", id);

            ownerMessageService.withdrawMessage(id);

            return Result.success("撤回成功");
        } catch (Exception e) {
            log.error("撤回消息失败", e);
            return Result.error("撤回消息失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件/图片
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
        @RequestParam("file") MultipartFile file) {
        try {
            log.info("收到上传文件请求，文件名：{}", file.getOriginalFilename());

            // 检查文件
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                return Result.error("文件大小不能超过 10MB");
            }

            // 上传文件
            String fileUrl = ownerMessageService.uploadFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());

            return Result.success("上传成功", response);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error("上传文件失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/files/{filename}")
    public byte[] downloadFile(@PathVariable String filename) throws IOException {
        String uploadPath = "./uploads/messages/";
        Path path = Paths.get(uploadPath + filename);
        return Files.readAllBytes(path);
    }
}
