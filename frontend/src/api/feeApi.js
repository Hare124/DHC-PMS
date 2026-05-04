// src/api/feeApi.js
import request from '@/utils/request'; // 引入配置好的 axios 实例

// ====================== 通用测试接口 ======================
// 测试 POST 接口
export const testPost = (data) => {
    return request({
        url: '/fee/test',
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/fee/list',
        method: 'get'
    });
};

// ====================== 业主端接口 ======================
/**
 * 业主费用查询（带筛选）
 * @param {Object} params - 查询参数
 * @param {Number} params.userId - 业主用户ID
 * @param {Number} params.pageNum - 页码
 * @param {Number} params.pageSize - 每页条数
 * @param {String} [params.typeName] - 费用类型名称
 * @param {Number} [params.status] - 缴费状态（0-未缴/1-已缴）
 * @param {String} [params.startDueDate] - 截止日期开始（yyyy-MM-dd）
 * @param {String} [params.endDueDate] - 截止日期结束（yyyy-MM-dd）
 */
export const queryOwnerFee = (params) => {
    return request({
        url: '/fee/owner/query',
        method: 'get',
        params
    });
};

/**
 * 获取业主费用汇总
 * @param {Number} userId - 业主用户ID
 */
export const getOwnerFeeSummary = (userId) => {
    return request({
        url: '/fee/owner/summary',
        method: 'get',
        params: { userId }
    });
};

/**
 * 生成缴费单
 * @param {Number} userId - 业主用户ID
 * @param {Array<Number>} feeRecordIds - 费用记录ID列表
 */
export const createPaymentOrder = (userId, feeRecordIds) => {
    return request({
        url: '/fee/owner/createPaymentOrder',
        method: 'post',
        params: { userId },
        data: feeRecordIds
    });
};

/**
 * 模拟缴费
 * @param {Number} userId - 业主用户ID
 * @param {Object} order - 缴费单信息
 */
export const payFee = (userId, order) => {
    return request({
        url: '/fee/owner/pay',
        method: 'post',
        params: { userId },
        data: order
    });
};

/**
 * 查看缴费凭证
 * @param {Number} userId - 业主用户ID
 * @param {Number} feeRecordId - 费用记录ID
 */
export const getPaymentVoucher = (userId, feeRecordId) => {
    return request({
        url: '/fee/owner/voucher',
        method: 'get',
        params: { userId, feeRecordId }
    });
};

// ====================== 物业端接口 ======================
/**
 * 新增费用类型
 * @param {Object} data - 费用类型参数
 */
export const addFeeType = (data) => {
    return request({
        url: '/fee/admin/addFeeType',
        method: 'post',
        params: data  // 使用 params 传递参数
    })
}

/**
 * 修改费用类型
 * @param {Object} data - 费用类型参数
 */
export const updateFeeType = (data) => {
    return request({
        url: '/fee/admin/updateFeeType',
        method: 'post',
        params: data
    })
}

/**
 * 批量生成费用账单
 * @param {Object} data - 生成参数
 */
export const batchGenerateFee = (params) => {
    return request({
        url: '/fee/admin/batchGenerateFee',
        method: 'POST',
        params: params
    });
};


/**
 * 单个核销费用
 * @param {Object} params - 核销参数
 */
export const writeOffFee = (params) => {
    return request({
        url: '/fee/admin/writeOff',
        method: 'PUT',
        params: params
    });
};

/**
 * 批量核销费用
 * @param {Object} data - 批量核销参数
 */
export const batchWriteOffFee = (params) => {
    return request({
        url: '/fee/admin/batchWriteOff',
        method: 'PUT',
        params: { adminId: params.adminId },
        data: params.feeRecordIds
    });
};

/**
 * 创建催缴记录（适配后端@RequestParam接收）
 * @param {Object} params - 催缴参数（URL拼接）
 */
export const createReminder = (params) => {
    return request({
        url: '/fee/admin/createReminder',
        method: 'POST',
        params: params,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
        },
        timeout: 10000
    });
};

/**
 * 发送催缴通知
 * @param {Object} params - 发送参数
 */
export const sendReminder = (params) => {
    return request({
        url: '/fee/admin/sendReminder',
        method: 'POST',
        params: params
    });
};

/**
 * 物业端费用记录查询
 * @param {Object} params - 查询参数
 * @param {Number} params.adminId - 物业管理员ID
 * @param {Number} params.pageNum - 页码
 * @param {Number} params.pageSize - 每页条数
 * @param {String} [params.keyword] - 模糊搜索关键词（房号/业主姓名）
 * @param {Number} [params.status] - 缴费状态
 * @param {String} [params.startDueDate] - 截止日期开始
 * @param {String} [params.endDueDate] - 截止日期结束
 */
export const queryAdminFee = (params) => {
    return request({
        url: '/fee/admin/query',
        method: 'get',
        params
    });
};

/**
 * 获取费用类型列表
 */
export const getFeeTypes = () => {
    return request({
        url: '/fee/type/list',
        method: 'get'
    });
};

/**
 * 删除费用类型
 * @param {Object} params - 删除参数
 */
export const deleteFeeType = (params) => {
    return request({
        url: '/fee/admin/deleteFeeType',
        method: 'DELETE',
        params: params
    });
}
/**
 * 导出费用记录到 Excel
 * @param {Object} params - 导出参数
 * @param {Number} params.adminId - 物业管理员 ID
 * @param {String} [params.feeRecordIds] - 费用记录 ID 列表（逗号分隔）
 * @param {String} [params.keyword] - 模糊搜索关键词
 * @param {String} [params.typeName] - 费用类型名称
 * @param {Number} [params.status] - 缴费状态
 * @param {String} [params.startDueDate] - 截止日期开始
 * @param {String} [params.endDueDate] - 截止日期结束
 * @param {Number} [params.minAmount] - 最小金额
 * @param {Number} [params.maxAmount] - 最大金额
 */
export const exportFeeRecords = (params) => {
    return request({
        url: '/fee/admin/export',
        method: 'get',
        params,
        responseType: 'blob' // 重要：指定响应类型为 blob
    });
}
/**
 * 获取业主欠费风险预测（物业端）
 * @param {Number} adminId - 物业管理员ID
 * @param {Number} ownerId - 业主ID
 */
export const getOwnerArrearsRisk = (adminId, ownerId) => {
    return request({
        url: '/fee/admin/arrearsRisk',
        method: 'get',
        params: { adminId, ownerId }
    });
}

/**
 * 获取业主欠费风险预测（业主端）
 * @param {Number} userId - 业主用户ID
 */
export const getMyArrearsRisk = (userId) => {
    return request({
        url: '/fee/owner/arrearsRisk',
        method: 'get',
        params: { userId }
    });
}
