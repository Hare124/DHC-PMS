package com.example.dhcpms.service;

/**
 * 消息推送Service接口
 */
public interface MsgPushService {
    void pushAnnouncementMsgToAllOwners(Long announcementId, String title, Integer isTop);
    void pushAnnouncementTopStatusUpdateMsg(Long announcementId, Integer isTop);
    void pushAnnouncementUpdateMsgToAllOwners(Long announcementId);
    void pushAnnouncementRecallMsgToAllOwners(Long announcementId);
}