package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.service.MsgPushService;
import com.example.dhcpms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息推送Service实现类（模拟推送）
 */
@Service
@RequiredArgsConstructor
public class MsgPushServiceImpl implements MsgPushService {

    private final UserService userService;

    @Override
    public void pushAnnouncementMsgToAllOwners(Long announcementId, String title, Integer isTop) {
        List<User> owners = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1));

        String msg = (isTop == 1 ? "【置顶公告】" : "【小区公告】") + title + "（ID：" + announcementId + "）";
        owners.forEach(owner -> System.out.println("推送至业主[" + owner.getName() + "]：" + msg));
    }

    @Override
    public void pushAnnouncementTopStatusUpdateMsg(Long announcementId, Integer isTop) {
        String msg = "公告ID：" + announcementId + (isTop == 1 ? "已置顶" : "已取消置顶");
        List<User> owners = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1));
        owners.forEach(owner -> System.out.println("推送至业主[" + owner.getName() + "]：" + msg));
    }

    @Override
    public void pushAnnouncementUpdateMsgToAllOwners(Long announcementId) {
        String msg = "公告ID：" + announcementId + "已更新，请查看最新内容";
        List<User> owners = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1));
        owners.forEach(owner -> System.out.println("推送至业主[" + owner.getName() + "]：" + msg));
    }

    @Override
    public void pushAnnouncementRecallMsgToAllOwners(Long announcementId) {
        String msg = "公告ID：" + announcementId + "已撤回，请注意";
        List<User> owners = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getStatus, 1));
        owners.forEach(owner -> System.out.println("推送至业主[" + owner.getName() + "]：" + msg));
    }
}