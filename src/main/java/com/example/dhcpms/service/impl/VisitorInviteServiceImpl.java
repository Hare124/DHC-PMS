package com.example.dhcpms.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.VisitorInvite;
import com.example.dhcpms.mapper.VisitorInviteMapper;
import com.example.dhcpms.service.VisitorInviteService;
import com.example.dhcpms.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 访客邀请Service实现类
 */
@Slf4j
@Service
public class VisitorInviteServiceImpl extends ServiceImpl<VisitorInviteMapper, VisitorInvite> implements VisitorInviteService {

    @Autowired
    private EncryptUtil encryptUtil;

    @Override
    public int updateExpiredInvites(LocalDateTime now) {
        LambdaUpdateWrapper<VisitorInvite> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VisitorInvite::getStatus, 0) // 待核销
                .lt(VisitorInvite::getExpireTime, now) // 已过期
                .set(VisitorInvite::getStatus, 2); // 更新为已过期
        boolean success = this.update(wrapper);
        return success ? 1 : 0;
    }

    @Override
    public VisitorInvite getValidInviteByPhone(String phone) {
        LambdaQueryWrapper<VisitorInvite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitorInvite::getVisitorPhone, phone)
                .eq(VisitorInvite::getStatus, 0) // 待核销
                .gt(VisitorInvite::getExpireTime, LocalDateTimeUtil.now()); // 未过期
        return this.getOne(wrapper);
    }

    @Override
    public Map<String, Object> verifyQRCode(String qrCodeContent) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解密并验证二维码
            Map<String, Object> qrData = encryptUtil.decryptAndVerify(qrCodeContent);
            if (qrData == null) {
                result.put("success", false);
                result.put("message", "二维码无效或已被篡改");
                return result;
            }

            // 2. 检查是否过期
            if (Boolean.TRUE.equals(qrData.get("expired"))) {
                result.put("success", false);
                result.put("message", "二维码已过期");
                result.put("code", "EXPIRED");
                return result;
            }

            // 3. 获取邀请ID
            Long inviteId = Long.valueOf(qrData.get("inviteId").toString());
            VisitorInvite invite = this.getById(inviteId);

            if (invite == null) {
                result.put("success", false);
                result.put("message", "邀请记录不存在");
                return result;
            }

            // 4. 验证状态
            if (invite.getStatus() != 0) {
                result.put("success", false);
                result.put("message", "邀请已核销或已过期");
                result.put("code", "INVALID_STATUS");
                return result;
            }

            // 5. 再次验证时效性（数据库中的过期时间）
            if (LocalDateTime.now().isAfter(invite.getExpireTime())) {
                invite.setStatus(2);
                this.updateById(invite);
                result.put("success", false);
                result.put("message", "邀请已过期");
                result.put("code", "EXPIRED");
                return result;
            }

            // 6. 验证通过，返回访客信息
            result.put("success", true);
            result.put("message", "验证通过");
            result.put("inviteId", invite.getId());
            result.put("visitorName", invite.getVisitorName());
            result.put("visitorPhone", invite.getVisitorPhone());
            result.put("ownerId", invite.getOwnerId());
            result.put("expireTime", invite.getExpireTime());

            log.info("二维码验证成功，inviteId: {}, visitor: {}", inviteId, invite.getVisitorName());
            return result;

        } catch (Exception e) {
            log.error("验证二维码失败", e);
            result.put("success", false);
            result.put("message", "系统异常，验证失败");
            return result;
        }
    }
}
