package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.OwnerMessage;
import org.springframework.web.multipart.MultipartFile;
/**
 * 群聊消息服务接口
 */
public interface OwnerMessageService extends IService<OwnerMessage> {

    /**
     * 获取群聊消息列表
     * @param chatRoomName 群聊名称
     * @param page 分页参数
     * @return 消息列表
     */
    Page<OwnerMessage> getMessageList(String chatRoomName, Page<OwnerMessage> page);

    /**
     * 获取群聊消息列表（支持多条件筛选）
     * @param chatRoomName 群聊名称
     * @param messageType 消息类型
     * @param senderName 发送人姓名
     * @param page 分页参数
     * @return 消息列表
     */
    Page<OwnerMessage> getMessageListWithFilter(String chatRoomName, String messageType, String senderName, Page<OwnerMessage> page);

    /**
     * 发送消息
     * @param message 消息对象
     * @return 消息 ID
     */
    Long sendMessage(OwnerMessage message);

    /**
     * 发送消息（自动填充用户名）
     * @param message 消息对象
     * @param userId 用户 ID
     * @return 消息 ID
     */
    Long sendMessageWithUsername(OwnerMessage message, Long userId);

    /**
     * 撤回消息
     * @param messageId 消息 ID
     */
    void withdrawMessage(Long messageId);

    /**
     * 上传文件
     * @param file 文件
     * @return 文件 URL
     */
    String uploadFile(MultipartFile file);
}
