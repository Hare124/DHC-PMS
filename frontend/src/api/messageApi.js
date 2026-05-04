// src/api/messageApi.js
import request from '@/utils/request';

/**
 * 获取群聊消息列表
 * @param {Object} params - 查询参数
 * @param {string} params.chatRoomName - 群聊名称
 * @param {string} params.messageType - 消息类型
 * @param {string} params.senderName - 发送人姓名
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 */
export const getMessageList = (params) => {
    return request({
        url: '/message/owner/list',
        method: 'get',
        params // 会自动序列化为查询参数
    });
};

/**
 * 发送消息
 * @param {Object} data - 消息数据
 */
export const sendMessage = (data) => {
    return request({
        url: '/message/owner/send',
        method: 'post',
        data
    });
};

/**
 * 撤回消息
 * @param {number} id - 消息 ID
 */
export const withdrawMessage = (id) => {
    return request({
        url: '/message/owner/withdraw',
        method: 'post',
        params: { id }
    });
};

/**
 * 上传文件/图片
 * @param {FormData} formData - 文件数据
 */
export const uploadFile = (formData) => {
    return request({
        url: '/message/owner/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};
