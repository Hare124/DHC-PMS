import request from '@/utils/request'; // 引入配置好的 axios 实例

// ====================== 测试接口 ======================
// 测试 POST 接口
export const testPost = (data) => {
    return request({
        url: '/visitor/test', // 最终请求路径：/api/visitor/test
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/visitor/list', // 最终请求路径：/api/visitor/list
        method: 'get'
    });
};

// ====================== 业主端接口 ======================
/**
 * 业主创建访客邀请（生成二维码）
 * @param {Object} data - 邀请参数
 * @param {string} data.visitorName - 访客姓名
 * @param {string} data.visitorPhone - 访客手机号
 * @param {string} data.visitTime - 预约时间（yyyy-MM-dd HH:mm:ss）
 * @param {number} [data.validHours=2] - 有效期（小时）
 */
export const ownerCreateInvite = (data) => {
    return request({
        url: '/visitor/owner/create-invite',
        method: 'post',
        data,
        // user_id 由 request 拦截器自动从 localStorage 读取并携带，无需手动设置
    });
};

/**
 * 业主查询我的邀请记录
 */
export const ownerGetInviteRecords = () => {
    return request({
        url: '/visitor/owner/invite-records',
        method: 'get'
    });
};

/**
 * 业主取消邀请
 * @param {number} inviteId - 邀请ID
 */
export const ownerCancelInvite = (inviteId) => {
    return request({
        url: '/visitor/owner/cancel-invite',
        method: 'post',
        params: { inviteId } // GET/POST 传参统一用 params
    });
};

// 获取单个邀请的二维码
export const getInviteQRCode = (inviteId) => {
    return request({
        url: '/visitor/owner/get-qrcode',
        method: 'get',
        params: { inviteId }
    });
}

// ====================== 物业端接口 ======================
/**
 * 物业查询邀请记录（带分页）
 * @param {Object} params - 查询参数
 * @param {string} [params.status] - 状态（0-待核销/1-已核销/2-已过期）
 * @param {string} [params.keyword] - 关键词（姓名/手机号）
 * @param {string} [params.startTime] - 开始时间（YYYY-MM-DD HH:mm:ss）
 * @param {string} [params.endTime] - 结束时间（YYYY-MM-DD HH:mm:ss）
 * @param {number} [params.page] - 页码
 * @param {number} [params.size] - 每页数量
 */
export const adminQueryInvites = (params) => {
    return request({
        url: '/visitor/admin/query-invites',
        method: 'post',
        params: {
            page: params.page || 1,
            size: params.size || 10
        },
        data: {
            status: params.status,
            keyword: params.keyword,
            startTime: params.startTime,
            endTime: params.endTime
        }
    });
};

/**
 * 物业核销邀请
 * @param {Object} data - 核销参数
 * @param {string} [data.encryptInviteId] - 加密邀请ID（扫码）
 * @param {string} [data.visitorPhone] - 访客手机号（手动）
 * @param {number} data.checkerId - 核销人ID
 * @param {string} data.checkType - 核销类型（自动/手动）
 * @param {string} [data.remark] - 备注
 */
export const adminVerifyInvite = (data) => {
    return request({
        url: '/visitor/admin/verify-invite',
        method: 'post',
        data
    });
};

/**
 * 物业登记访客离园
 * @param {Object} data - 离园参数
 * @param {number} [data.inviteId] - 邀请ID
 * @param {string} [data.visitorPhone] - 访客手机号
 * @param {number} data.checkerId - 操作人ID
 */
export const adminCheckout = (data) => {
    return request({
        url: '/visitor/admin/checkout',
        method: 'post',
        data
    });
};

/**
 * 物业查询访客通行记录
 * @param {string} [date] - 查询日期（yyyy-MM-dd），默认今日
 */
export const adminGetRecordList = (date) => {
    return request({
        url: '/visitor/admin/record-list',
        method: 'get',
        params: { date }
    });
};

/**
 * 物业自动失效过期邀请（定时任务手动触发）
 */
export const adminAutoExpireInvites = () => {
    return request({
        url: '/visitor/admin/auto-expire',
        method: 'post'
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