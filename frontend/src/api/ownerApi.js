// src/api/ownerApi.js
import request from '@/utils/request'

// 查询业主列表（适配驼峰字段）
export function getOwnerList(params) {
    return request({
        url: '/user/owner',
        method: 'get',
        params
    })
}

// 新增业主
export function addOwner(data) {
    return request({
        url: '/user/owner',
        method: 'post',
        data
    })
}

// 修改业主信息
export function updateOwner(data) {
    return request({
        url: `/user/owner/${data.id}`,
        method: 'put',
        data
    })
}

// 删除业主
export function deleteOwner(id) {
    return request({
        url: `/user/owner/${id}`,
        method: 'delete'
    })
}
// ====================== 测试接口 ======================
// 测试 POST 接口
export const testPost = (data) => {
    return request({
        url: '/owner/test01', // 最终请求路径：/api/owner/test
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/owner/test02', // 最终请求路径：/api/owner/list
        method: 'get'
    });
};
// ====================== DeepSeek AI 调试接口 ======================
/**
 * 调用 DeepSeek AI 聊天接口
 * @param {Object} data - 请求参数
 * @param {string} data.message - 用户输入的消息
 */
export const chatWithDeepSeek = (data) => {
    return request({
        url: '/chat', // 最终请求路径：/api/chat
        method: 'post',
        data,
        timeout: 30000 // DeepSeek API 可能需要较长时间，设置为 30 秒
    });
};
