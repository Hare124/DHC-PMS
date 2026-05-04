// src/utils/request.js
import axios from 'axios';

const request = axios.create({
    baseURL: '/api', // 匹配 Vite 代理的 /api 前缀
    timeout: 5000,
    withCredentials: true
});

// 定义无需携带 Token 的白名单（精准路径）
const NO_TOKEN_URLS = [
    '/user/send-sms',
    '/user/validate-code',
    '/user/login',
    '/user/register'
];

// 请求拦截器：白名单接口不携带 Token
request.interceptors.request.use(
    (config) => {
        // 1. 精准匹配 URL（仅完全相等时判定为白名单）
        // 去除 URL 可能的参数（如 /user/login?xxx=123 → /user/login）
        const pureUrl = config.url.split('?')[0];
        const isNoToken = NO_TOKEN_URLS.includes(pureUrl);

        if (!isNoToken) {
            // 2. 校验 Token 有效性（非空 + 去除隐形字符）
            const token = localStorage.getItem('token')?.trim() || '';
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
                console.log('✓ 请求携带 Token:', pureUrl);
            } else {
                console.warn('✗ Token 为空，未携带 Authorization 头:', pureUrl);
            }

            // 3. 携带 user_id（后端报修接口需要）
            const userId = localStorage.getItem('userId')?.trim() || '';
            if (userId) {
                config.headers['user_id'] = userId;
                console.log('✓ 请求携带 user_id:', userId, '| URL:', pureUrl);
            } else {
                console.warn('✗ userId 为空！请检查登录是否成功保存:', pureUrl);
            }
        } else {
            // 白名单接口：强制删除 Token 相关头，避免残留
            delete config.headers.Authorization;
            delete config.headers.token;
            delete config.headers['user_id'];
            console.log('✓ 白名单接口，不携带 Token 和 user_id:', pureUrl);
        }
        return config;
    },
    (error) => {
        console.error('请求拦截器异常：', error);
        return Promise.reject(error);
    }
);

// 修改 src/utils/request.js 的响应拦截器
request.interceptors.response.use(
    (response) => response,
    (error) => {
        const pureUrl = error.config?.url?.split('?')[0] || '';
        const isNoToken = NO_TOKEN_URLS.includes(pureUrl);

        if (error.response?.status === 401 && !isNoToken) {
            // 1. 先暂停跳转，用 alert 阻断流程，保留 Network 记录
            alert(`401 错误详情：
                URL: ${pureUrl}
                后端返回：${JSON.stringify(error.response.data)}
                Token: ${localStorage.getItem('token')?.substring(0, 20)}...`);

            // 2. 打印完整错误信息到控制台（F12 查看）
            console.error('401 完整日志：', {
                url: pureUrl,
                status: error.response.status,
                responseData: error.response.data, // 后端返回的具体提示（关键）
                requestHeaders: error.config.headers, // 发送的请求头
                token: localStorage.getItem('token')
            });

            // 3. 临时注释跳转，先看错误再处理
            // localStorage.removeItem('token');
            // if (!window.location.pathname.includes('/login')) {
            //     window.location.href = '/login';
            // }
        } else {
            // 增强错误日志输出
            console.error('❌ 请求失败:', {
                url: pureUrl,
                status: error.response?.status || '无响应',
                message: error.response?.data?.msg || error.message,
                responseData: error.response?.data,
                config: {
                    method: error.config?.method,
                    headers: error.config?.headers
                }
            });
        }
        return Promise.reject(error);
    }
);

export default request;