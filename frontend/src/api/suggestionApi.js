// src/api/suggestionApi.js
import request from '@/utils/request';

// ====================== 业主端接口 ======================

/**
 * 提交意见反馈
 * @param {Object} data - 反馈参数
 */
export const submitSuggestion = (data) => {
    return request({
        url: '/suggestion/owner/submit',
        method: 'post',
        data
    });
};

/**
 * 获取业主意见反馈列表
 */
export const getOwnerSuggestionList = () => {
    return request({
        url: '/suggestion/owner/list',
        method: 'get'
    });
};

/**
 * 获取意见反馈详情
 * @param {number} id - 反馈 ID
 */
export const getSuggestionDetail = (id) => {
    return request({
        url: '/suggestion/owner/detail',
        method: 'get',
        params: { id }
    });
};

// ====================== 物业端接口 ======================

/**
 * 获取意见反馈列表（物业端）
 * @param {Object} params - 查询参数
 */
export const getSuggestionList = (params) => {
    return request({
        url: '/suggestion/admin/list',
        method: 'get',
        params
    });
};

/**
 * 回复意见反馈
 * @param {Object} data - 回复参数
 */
export const replySuggestion = (data) => {
    return request({
        url: '/suggestion/admin/reply',
        method: 'post',
        data
    });
};

/**
 * 更新处理状态
 * @param {Object} data - 状态参数
 */
export const updateSuggestionStatus = (data) => {
    return request({
        url: '/suggestion/admin/updateStatus',
        method: 'post',
        data
    });
};
// ====================== 测试接口 ======================
// 测试 POST 接口
export const testPost = (data) => {
    return request({
        url: '/suggestion/test01', // 最终请求路径：/api/suggestion/test
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/suggestion/test02', // 最终请求路径：/api/suggestion/list
        method: 'get'
    });
};