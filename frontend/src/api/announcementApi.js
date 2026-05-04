// src/api/announcementApi.js
import request from '@/utils/request';

// ======================== 测试接口 ========================
export const testPost = (data) => {
    return request({
        url: '/announcement/test', // 最终请求路径：/api/announcement/test
        method: 'post',
        data
    });
};

export const testGet = () => {
    return request({
        url: '/announcement/list', // 最终请求路径：/api/announcement/list
        method: 'get'
    });
};

// ======================== 业主端接口 ========================
/**
 * 业主查询公告列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, startTime, endTime, keyword }
 */
export const ownerGetAnnouncementList = (params) => {
    return request({
        url: '/announcement/owner/list',
        method: 'get',
        params
    });
};

/**
 * 业主查看公告详情
 * @param {Number} announcementId - 公告ID
 */
export const ownerGetAnnouncementDetail = (announcementId) => {
    return request({
        url: `/announcement/owner/detail/${announcementId}`,
        method: 'get'
    });
};

// ======================== 物业端接口 ========================
/**
 * 物业发布公告
 * @param {Object} data - 公告信息 { title, content, isTop }
 */
export const managerPublishAnnouncement = (data) => {
    return request({
        url: '/announcement/manager/publish',
        method: 'post',
        data
    });
};

/**
 * 物业定时发布公告
 * @param {Object} data - 公告信息 { title, content, isTop, scheduledPublishTime }
 */
export const managerSchedulePublish = (data) => {
    return request({
        url: '/announcement/manager/schedule-publish',
        method: 'post',
        data
    });
};

/**
 * 物业设置/取消置顶
 * @param {Number} announcementId - 公告ID
 * @param {Number} isTop - 0取消置顶/1置顶
 */
export const managerSetTop = (announcementId, isTop) => {
    return request({
        url: `/announcement/manager/set-top/${announcementId}/${isTop}`,
        method: 'put'
    });
};

/**
 * 物业修改公告
 * @param {Number} announcementId - 公告ID
 * @param {Object} data - 公告信息 { title, content, isTop, resetReadCount }
 */
export const managerUpdateAnnouncement = (announcementId, data) => {
    return request({
        url: `/announcement/manager/update/${announcementId}`,
        method: 'put',
        data
    });
};

/**
 * 物业撤回公告
 * @param {Number} announcementId - 公告ID
 */
export const managerRecallAnnouncement = (announcementId) => {
    return request({
        url: `/announcement/manager/recall/${announcementId}`,
        method: 'put'
    });
};

/**
 * 物业查询公告列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, startTime, endTime, publisherId, isTop, recallStatus, keyword }
 */
export const managerGetAnnouncementList = (params) => {
    return request({
        url: '/announcement/manager/list',
        method: 'get',
        params
    });
};

/**
 * 物业查看公告详情
 * @param {Number} announcementId - 公告ID
 */
export const managerGetAnnouncementDetail = (announcementId) => {
    return request({
        url: `/announcement/manager/detail/${announcementId}`,
        method: 'get'
    });
};