package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.OwnerMessage;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.mapper.OwnerMessageMapper;
import com.example.dhcpms.service.OwnerMessageService;
import com.example.dhcpms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 群聊消息服务实现类
 */
@Slf4j
@Service
public class OwnerMessageServiceImpl extends ServiceImpl<OwnerMessageMapper, OwnerMessage>
    implements OwnerMessageService {

    @Value("${file.upload.path:./uploads/messages/}")
    private String uploadPath;


    @Autowired
    private UserService userService;

    @Override
    public Page<OwnerMessage> getMessageList(String chatRoomName, Page<OwnerMessage> page) {
        try {
            log.info("查询群聊消息列表，chatRoomName: {}, page: {}", chatRoomName, page);

            // 使用多条件筛选方法，其他参数传 null
            return getMessageListWithFilter(chatRoomName, null, null, page);
        } catch (Exception e) {
            log.error("查询群聊消息列表失败", e);
            throw new RuntimeException("查询群聊消息列表失败：" + e.getMessage());
        }
    }

    @Override
    public Page<OwnerMessage> getMessageListWithFilter(String chatRoomName, String messageType, String senderName, Page<OwnerMessage> page) {
        try {
            log.info("查询群聊消息列表（多条件筛选），chatRoomName: {}, messageType: {}, senderName: {}, page: {}",
                    chatRoomName, messageType, senderName, page);

            LambdaQueryWrapper<OwnerMessage> queryWrapper = new LambdaQueryWrapper<>();

            // 群聊名称（必填条件）
            if (chatRoomName != null && !chatRoomName.isEmpty()) {
                queryWrapper.eq(OwnerMessage::getChatRoomName, chatRoomName);
            }

            // 消息类型筛选
            if (messageType != null && !messageType.isEmpty()) {
                queryWrapper.eq(OwnerMessage::getMessageType, messageType);
            }

            // 发送人姓名模糊筛选
            if (senderName != null && !senderName.isEmpty()) {
                queryWrapper.like(OwnerMessage::getSenderName, senderName);
            }

            // 按发送时间升序排列
            queryWrapper.orderByAsc(OwnerMessage::getSendTime);

            Page<OwnerMessage> result = this.page(page, queryWrapper);
            log.info("查询到 {} 条消息", result.getTotal());

            return result;
        } catch (Exception e) {
            log.error("查询群聊消息列表失败", e);
            throw new RuntimeException("查询群聊消息列表失败：" + e.getMessage());
        }
    }


    @Override
    public Long sendMessage(OwnerMessage message) {
        try {
            log.info("发送消息：{}", message);

            // 设置默认值
            if (message.getSendTime() == null) {
                message.setSendTime(LocalDateTime.now());
            }
            if (message.getIsWithdrawn() == null) {
                message.setIsWithdrawn(false);
            }
            if (message.getCreateTime() == null) {
                message.setCreateTime(LocalDateTime.now());
            }

            // 保存消息
            boolean success = this.save(message);

            if (success) {
                log.info("消息发送成功，ID: {}", message.getId());
                return message.getId();
            } else {
                log.error("消息发送失败");
                throw new RuntimeException("消息发送失败");
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送消息失败：" + e.getMessage());
        }
    }

    @Override
    public Long sendMessageWithUsername(OwnerMessage message, Long userId) {
        try {
            log.info("发送消息（带用户名），userId: {}, message: {}", userId, message);

            // 根据 userId 查询用户名
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    // 使用真实姓名（name）而不是用户名（username）
                    message.setSenderName(user.getName());
                    log.info("自动填充发送人姓名：{}", user.getName());
                } else {
                    log.warn("用户不存在，userId: {}", userId);
                }
            }

            // 使用原有方法发送消息
            return sendMessage(message);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送消息失败：" + e.getMessage());
        }
    }

    @Override
    public void withdrawMessage(Long messageId) {
        try {
            log.info("撤回消息，ID: {}", messageId);

            // 查询消息
            OwnerMessage message = this.getById(messageId);
            if (message == null) {
                log.warn("消息不存在，ID: {}", messageId);
                throw new RuntimeException("消息不存在");
            }

            // 检查是否已经撤回
            if (message.getIsWithdrawn()) {
                log.warn("消息已撤回，ID: {}", messageId);
                throw new RuntimeException("消息已撤回");
            }

            // 检查撤回时间（2 分钟内）
            LocalDateTime now = LocalDateTime.now();
            long minutes = java.time.Duration.between(message.getSendTime(), now).toMinutes();
            if (minutes > 2) {
                log.warn("超过撤回时间，ID: {}, 发送时间：{}", messageId, message.getSendTime());
                throw new RuntimeException("超过撤回时间，无法撤回");
            }

            // 更新撤回状态
            message.setIsWithdrawn(true);
            message.setWithdrawTime(now);

            boolean success = this.updateById(message);

            if (success) {
                log.info("消息撤回成功，ID: {}", messageId);
            } else {
                log.error("消息撤回失败，ID: {}", messageId);
                throw new RuntimeException("消息撤回失败");
            }
        } catch (Exception e) {
            log.error("撤回消息失败", e);
            throw new RuntimeException("撤回消息失败：" + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            log.info("上传文件：{}", file.getOriginalFilename());

            // 检查文件
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }

            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            File destFile = new File(uploadPath + fileName);
            file.transferTo(destFile);

            // 返回访问 URL
            String fileUrl = "/api/message/owner/files/" + fileName;
            log.info("文件上传成功，URL: {}", fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败：" + e.getMessage());
        }
    }
}
