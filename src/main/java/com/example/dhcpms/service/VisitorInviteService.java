package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.VisitorInvite;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 访客邀请Service接口
 */
public interface VisitorInviteService extends IService<VisitorInvite> {
    // 批量更新过期邀请状态
    int updateExpiredInvites(LocalDateTime now);

    // 根据手机号查询待核销的邀请
    VisitorInvite getValidInviteByPhone(String phone);

    // 根据二维码内容解密并核销
    Map<String, Object> verifyQRCode(String qrCodeContent);
}
