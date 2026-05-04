// src/api/repairApi.js
import request from '@/utils/request'; // 引入配置好的 axios 实例

// ====================== 通用测试接口 ======================
// 测试 POST 接口（前端请求路径：/api/repair/test → 代理到后端 /repair/test）
export const testPost = (data) => {
    return request({
        url: '/repair/test', // baseURL 是 /api，最终请求路径：/api/repair/test
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/repair/list',
        method: 'get'
    });
};

// ====================== 业主端接口 ======================
/**
 * 提交报修申请
 * @param {Object} data - 报修参数
 */
export const submitRepairApply = (data) => {
    return request({
        url: '/repair/owner/submit',
        method: 'post',
        data
    });
};

/**
 * 获取报修类型列表
 */
export const getRepairTypeList = () => {
    return request({
        url: '/repair/type/list',
        method: 'get'
    });
};

/**
 * 查看报修详情（物业端）
 * @param {number} repairOrderId - 报修单号
 */
export const getRepairDetail = (repairOrderId) => {
    return request({
        url: '/repair/admin/detail',
        method: 'get',
        params: { repairOrderId }
    });
};

/**
 * 获取物业端报修列表
 */
export const getAdminRepairList = (params) => {
    return request({
        url: '/repair/admin/list',
        method: 'get',
        params
    });
};

/**
 * 获取业主报修列表
 */
export const getOwnerRepairList = () => {
    return request({
        url: '/repair/owner/list',
        method: 'get'
    });
};

/**
 * 业主验收报修单
 * @param {Object} data - 验收参数
 */
export const acceptRepair = (data) => {
    return request({
        url: '/repair/owner/acceptance',
        method: 'post',
        data
    });
};

/**
 * 业主取消报修申请
 */
export const cancelRepairApply = (data) => {
    return request({
        url: '/repair/owner/cancel',
        method: 'post',
        data
    });
};
/**
 * 获取报修进度预测
 */
export function getRepairProgress(repairOrderId) {
    return request({
        url: `/repair/progress/${repairOrderId}`,
        method: 'get'
    })
}
/**
 * 重新提交报修申请
 * @param {Object} data - 重新提交参数
 */
export const resubmitRepairApply = (data) => {
    return request({
        url: '/repair/owner/resubmit',
        method: 'post',
        data
    });
};

// ====================== 物业端接口 ======================
/**
 * 物业审核报修单
 * @param {Object} data - 审核参数
 */
export const reviewRepairOrder = (data) => {
    return request({
        url: '/repair/admin/review',
        method: 'post',
        data
    });
};

/**
 * 物业派单
 * @param {Object} data - 派单参数
 */
export const assignRepairOrder = (data) => {
    return request({
        url: '/repair/admin/assign',
        method: 'post',
        data
    });
};

/**
 * 标记维修进度为待验收
 * @param {number} repairOrderId - 报修单号
 */
export const updateRepairProgress = (repairOrderId) => {
    return request({
        url: '/repair/admin/updateProgress',
        method: 'post',
        params: { repairOrderId }
    });
};